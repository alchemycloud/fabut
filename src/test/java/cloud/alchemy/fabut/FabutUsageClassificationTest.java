package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.EntityTierOneType;
import cloud.alchemy.fabut.model.TrackedBooleanDto;
import cloud.alchemy.fabut.model.TrackedDto;
import cloud.alchemy.fabut.model.TrackedTuple;
import cloud.alchemy.fabut.tracking.TrackedObject;
import cloud.alchemy.fabut.tracking.UsageReport;
import cloud.alchemy.fabut.tracking.UsageTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that usage tracking correctly classifies objects as fully used,
 * partially used, or never accessed — ensuring no DTOs or entities are
 * wrongly reported as used or unused.
 */
class FabutUsageClassificationTest extends Fabut {

    private final List<Object> entityTierOneTypes = new ArrayList<>();
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream capturedOutput;

    public FabutUsageClassificationTest() {
        entityTypes.add(EntityTierOneType.class);
        complexTypes.add(TrackedDto.class);
        complexTypes.add(TrackedBooleanDto.class);
        trackedTypes.add(TrackedTuple.class);
    }

    @Override
    protected List<?> findAll(Class<?> entityClass) {
        if (entityClass == EntityTierOneType.class) {
            return entityTierOneTypes;
        }
        return List.of();
    }

    @Override
    protected Object findById(Class<?> entityClass, Object id) {
        if (entityClass == EntityTierOneType.class) {
            for (Object entity : entityTierOneTypes) {
                if (((EntityTierOneType) entity).getId().equals(id)) {
                    return entity;
                }
            }
        }
        return null;
    }

    @BeforeEach
    @Override
    public void before() {
        super.before();
        capturedOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOutput));
    }

    @AfterEach
    @Override
    public void after() {
        try {
            super.after();
        } finally {
            System.setOut(originalOut);
        }
    }

    // =========================================================================
    // Entity classification
    // =========================================================================

    @Test
    void entity_fullyUsed_notInUnderusedList() {
        takeSnapshot();

        EntityTierOneType entity = new EntityTierOneType("value", 10);
        entity.getId();
        entity.getProperty();

        UsageReport report = getUsageTracker().getReport();
        assertTrue(report.getUnderusedObjects().isEmpty(),
                "Fully used entity should not appear in underused list");
        assertTrue(report.getNeverAccessedObjects().isEmpty(),
                "Fully used entity should not appear in never-accessed list");
    }

    @Test
    void entity_partiallyUsed_inUnderusedList() {
        takeSnapshot();

        EntityTierOneType entity = new EntityTierOneType("value", 10);
        entity.getId(); // only access id, skip property

        UsageReport report = getUsageTracker().getReport();
        assertEquals(1, report.getUnderusedObjects().size());
        TrackedObject tracked = report.getUnderusedObjects().getFirst();
        assertEquals(EntityTierOneType.class, tracked.getObjectClass());
        assertTrue(tracked.getAccessedFields().contains("id"));
        assertTrue(tracked.getUnusedFields().contains("property"));
    }

    @Test
    void entity_neverAccessed_inNeverAccessedList() {
        takeSnapshot();

        new EntityTierOneType("value", 10);

        UsageReport report = getUsageTracker().getReport();
        assertEquals(1, report.getNeverAccessedObjects().size());
        assertEquals(EntityTierOneType.class, report.getNeverAccessedObjects().getFirst().getObjectClass());
    }

    // =========================================================================
    // DTO (complexType) classification
    // =========================================================================

    @Test
    void dto_fullyUsed_notInUnderusedList() {
        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "name", "desc", 5);
        dto.getId();
        dto.getName();
        dto.getDescription();
        dto.getCount();

        UsageReport report = getUsageTracker().getReport();
        assertTrue(report.getUnderusedObjects().isEmpty());
        assertTrue(report.getNeverAccessedObjects().isEmpty());
    }

    @Test
    void dto_partiallyUsed_correctFieldsReported() {
        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "name", "desc", 5);
        dto.getId();
        dto.getName();

        UsageReport report = getUsageTracker().getReport();
        assertEquals(1, report.getUnderusedObjects().size());
        TrackedObject tracked = report.getUnderusedObjects().getFirst();
        assertEquals(Set.of("id", "name"), tracked.getAccessedFields());
        assertEquals(Set.of("description", "count"), tracked.getUnusedFields());
    }

    @Test
    void dto_neverAccessed_inNeverAccessedList() {
        takeSnapshot();

        new TrackedDto(1L, "name", "desc", 5);

        UsageReport report = getUsageTracker().getReport();
        assertEquals(1, report.getNeverAccessedObjects().size());
        assertEquals(TrackedDto.class, report.getNeverAccessedObjects().getFirst().getObjectClass());
    }

    // =========================================================================
    // Tuple (trackedType) classification
    // =========================================================================

    @Test
    void tuple_fullyUsed_notInUnderusedList() {
        takeSnapshot();

        TrackedTuple tuple = new TrackedTuple(1L, "label");
        tuple.getEntityId();
        tuple.getLabel();

        UsageReport report = getUsageTracker().getReport();
        assertTrue(report.getUnderusedObjects().isEmpty());
        assertTrue(report.getNeverAccessedObjects().isEmpty());
    }

    @Test
    void tuple_partiallyUsed_correctFieldsReported() {
        takeSnapshot();

        TrackedTuple tuple = new TrackedTuple(1L, "label");
        tuple.getEntityId();

        UsageReport report = getUsageTracker().getReport();
        assertEquals(1, report.getUnderusedObjects().size());
        TrackedObject tracked = report.getUnderusedObjects().getFirst();
        assertEquals(Set.of("entityId"), tracked.getAccessedFields());
        assertEquals(Set.of("label"), tracked.getUnusedFields());
    }

    @Test
    void tuple_neverAccessed_inNeverAccessedList() {
        takeSnapshot();

        new TrackedTuple(1L, "label");

        UsageReport report = getUsageTracker().getReport();
        assertEquals(1, report.getNeverAccessedObjects().size());
        assertEquals(TrackedTuple.class, report.getNeverAccessedObjects().getFirst().getObjectClass());
    }

    // =========================================================================
    // Boolean getter (is* prefix) classification
    // =========================================================================

    @Test
    void booleanDto_fullyUsed_notInUnderusedList() {
        takeSnapshot();

        TrackedBooleanDto dto = new TrackedBooleanDto(true, "test");
        dto.isActive();
        dto.getName();

        UsageReport report = getUsageTracker().getReport();
        assertTrue(report.getUnderusedObjects().isEmpty());
    }

    @Test
    void booleanDto_onlyBooleanAccessed_stringFieldUnused() {
        takeSnapshot();

        TrackedBooleanDto dto = new TrackedBooleanDto(true, "test");
        dto.isActive();

        UsageReport report = getUsageTracker().getReport();
        assertEquals(1, report.getUnderusedObjects().size());
        TrackedObject tracked = report.getUnderusedObjects().getFirst();
        assertEquals(Set.of("active"), tracked.getAccessedFields());
        assertEquals(Set.of("name"), tracked.getUnusedFields());
    }

    // =========================================================================
    // Objects created BEFORE snapshot should NOT be tracked
    // =========================================================================

    @Test
    void objectCreatedBeforeSnapshot_notTracked() {
        TrackedDto beforeSnapshot = new TrackedDto(1L, "before", null, 0);
        TrackedTuple tupleBeforeSnapshot = new TrackedTuple(1L, "before");
        EntityTierOneType entityBeforeSnapshot = new EntityTierOneType("before", 1);

        takeSnapshot();

        UsageReport report = getUsageTracker().getReport();
        assertFalse(report.hasTrackedObjects(),
                "Objects created before takeSnapshot should not be tracked");
    }

    @Test
    void onlyObjectsAfterSnapshot_tracked() {
        new TrackedDto(1L, "before", null, 0);

        takeSnapshot();

        TrackedDto afterSnapshot = new TrackedDto(2L, "after", null, 0);
        afterSnapshot.getId();
        afterSnapshot.getName();
        afterSnapshot.getDescription();
        afterSnapshot.getCount();

        UsageReport report = getUsageTracker().getReport();
        assertEquals(1, getUsageTracker().getTrackedObjects().size(),
                "Only objects created after snapshot should be tracked");
        assertFalse(report.hasUnderusedObjects());
    }

    // =========================================================================
    // Multiple objects — verify per-object classification accuracy
    // =========================================================================

    @Test
    void multipleObjects_eachClassifiedIndependently() {
        takeSnapshot();

        // Fully used DTO
        TrackedDto fullyUsedDto = new TrackedDto(1L, "full", "desc", 10);
        fullyUsedDto.getId();
        fullyUsedDto.getName();
        fullyUsedDto.getDescription();
        fullyUsedDto.getCount();

        // Partially used DTO
        TrackedDto partialDto = new TrackedDto(2L, "partial", "desc", 20);
        partialDto.getId();

        // Never accessed DTO
        TrackedDto neverAccessedDto = new TrackedDto(3L, "unused", null, 0);

        // Fully used tuple
        TrackedTuple fullyUsedTuple = new TrackedTuple(1L, "label");
        fullyUsedTuple.getEntityId();
        fullyUsedTuple.getLabel();

        // Never accessed entity
        EntityTierOneType neverAccessedEntity = new EntityTierOneType("val", 10);

        UsageReport report = getUsageTracker().getReport();

        // Total tracked: 5
        assertEquals(5, getUsageTracker().getTrackedObjects().size());

        // Underused: partialDto + neverAccessedDto + neverAccessedEntity = 3
        assertEquals(3, report.getUnderusedObjects().size());

        // Never accessed: neverAccessedDto + neverAccessedEntity = 2
        List<TrackedObject> neverAccessed = report.getNeverAccessedObjects();
        assertEquals(2, neverAccessed.size());
        Set<Class<?>> neverAccessedClasses = neverAccessed.stream()
                .map(TrackedObject::getObjectClass)
                .collect(Collectors.toSet());
        assertTrue(neverAccessedClasses.contains(TrackedDto.class));
        assertTrue(neverAccessedClasses.contains(EntityTierOneType.class));
    }

    @Test
    void multipleObjectsSameClass_trackedSeparately() {
        takeSnapshot();

        TrackedDto used = new TrackedDto(1L, "used", "desc", 1);
        used.getId();
        used.getName();
        used.getDescription();
        used.getCount();

        TrackedDto unused = new TrackedDto(2L, "unused", null, 0);

        UsageReport report = getUsageTracker().getReport();
        assertEquals(2, getUsageTracker().getTrackedObjects().size());

        // One fully used, one never accessed
        assertEquals(1, report.getNeverAccessedObjects().size());
        assertEquals(1, report.getUnderusedObjects().size());

        // The fully used one should NOT be in underused list
        TrackedObject underusedObj = report.getUnderusedObjects().getFirst();
        assertTrue(underusedObj.isNeverAccessed());
    }

    // =========================================================================
    // Ignored fields — verify they don't affect classification
    // =========================================================================

    @Test
    void ignoredFields_notCountedAsUnused() {
        ignoredFields.put(TrackedDto.class, List.of("description", "count"));
        // Recreate tracker with ignored fields
        getUsageTracker().setIgnoredFields(ignoredFields);

        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "name", "desc", 5);
        dto.getId();
        dto.getName();
        // description and count are ignored — this should be fully used

        UsageReport report = getUsageTracker().getReport();
        assertTrue(report.getUnderusedObjects().isEmpty(),
                "Object should be fully used when ignored fields are excluded");
    }

    @Test
    void ignoredFields_partialWithRemainingFields() {
        ignoredFields.put(TrackedDto.class, List.of("description", "count"));
        getUsageTracker().setIgnoredFields(ignoredFields);

        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "name", "desc", 5);
        dto.getId();
        // name is still unaccessed, description and count are ignored

        UsageReport report = getUsageTracker().getReport();
        assertEquals(1, report.getUnderusedObjects().size());
        TrackedObject tracked = report.getUnderusedObjects().getFirst();
        assertEquals(Set.of("id"), tracked.getAccessedFields());
        assertEquals(Set.of("name"), tracked.getUnusedFields(),
                "Only non-ignored unaccessed fields should appear as unused");
    }

    // =========================================================================
    // unregisterIfActive — verify removed objects are not reported
    // =========================================================================

    @Test
    void unregisteredObject_notInReport() {
        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "sideEffect", null, 0);
        UsageTracker.unregisterIfActive(dto);

        UsageReport report = getUsageTracker().getReport();
        assertFalse(report.hasTrackedObjects(),
                "Unregistered object should not appear in report");
    }

    @Test
    void unregister_onlyAffectsTargetObject() {
        takeSnapshot();

        TrackedDto keepDto = new TrackedDto(1L, "keep", null, 0);
        TrackedDto removeDto = new TrackedDto(2L, "remove", null, 0);
        UsageTracker.unregisterIfActive(removeDto);

        UsageReport report = getUsageTracker().getReport();
        assertEquals(1, getUsageTracker().getTrackedObjects().size(),
                "Only the unregistered object should be removed");
        assertSame(keepDto, getUsageTracker().getTrackedObjects().iterator().next().getObjectRef());
    }

    // =========================================================================
    // Tracking filter — verify filtered objects are not tracked
    // =========================================================================

    @Test
    void trackingFilter_excludedObjectsNotInReport() {
        getUsageTracker().setTrackingFilter(obj -> !(obj instanceof TrackedTuple));

        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "tracked", null, 0);
        TrackedTuple tuple = new TrackedTuple(1L, "filtered out");

        UsageReport report = getUsageTracker().getReport();
        assertEquals(1, getUsageTracker().getTrackedObjects().size());
        assertEquals(TrackedDto.class, getUsageTracker().getTrackedObjects().iterator().next().getObjectClass());
    }

    // =========================================================================
    // Object ref preservation — verify we can trace back to original object
    // =========================================================================

    @Test
    void trackedObject_preservesObjectRef() {
        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "test", null, 5);

        TrackedObject tracked = getUsageTracker().getTrackedObjects().iterator().next();
        assertSame(dto, tracked.getObjectRef(),
                "TrackedObject must preserve reference to original object for tracing");
    }

    @Test
    void neverAccessedObject_hasObjectRefForTracing() {
        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "unused", null, 0);

        List<TrackedObject> neverAccessed = getUsageTracker().getReport().getNeverAccessedObjects();
        assertEquals(1, neverAccessed.size());
        assertSame(dto, neverAccessed.getFirst().getObjectRef(),
                "Never-accessed objects must preserve reference for tracing");
    }

    // =========================================================================
    // Field count accuracy — verify tracked fields match actual getter-backed fields
    // =========================================================================

    @Test
    void trackedDto_hasExactly4Fields() {
        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "name", "desc", 5);

        TrackedObject tracked = getUsageTracker().getTrackedObjects().iterator().next();
        assertEquals(Set.of("id", "name", "description", "count"), tracked.getAllFields());
    }

    @Test
    void trackedTuple_hasExactly2Fields() {
        takeSnapshot();

        TrackedTuple tuple = new TrackedTuple(1L, "label");

        TrackedObject tracked = getUsageTracker().getTrackedObjects().iterator().next();
        assertEquals(Set.of("entityId", "label"), tracked.getAllFields());
    }

    @Test
    void entityTierOneType_hasExactly2Fields() {
        takeSnapshot();

        EntityTierOneType entity = new EntityTierOneType("value", 10);

        TrackedObject tracked = getUsageTracker().getTrackedObjects().iterator().next();
        assertEquals(Set.of("id", "property"), tracked.getAllFields());
    }

    @Test
    void trackedBooleanDto_hasExactly2Fields() {
        takeSnapshot();

        TrackedBooleanDto dto = new TrackedBooleanDto(true, "test");

        TrackedObject tracked = getUsageTracker().getTrackedObjects().iterator().next();
        assertEquals(Set.of("active", "name"), tracked.getAllFields());
    }

    // =========================================================================
    // Usage percentage accuracy
    // =========================================================================

    @Test
    void usagePercentage_fullyUsed_is100() {
        takeSnapshot();

        TrackedTuple tuple = new TrackedTuple(1L, "label");
        tuple.getEntityId();
        tuple.getLabel();

        TrackedObject tracked = getUsageTracker().getTrackedObjects().iterator().next();
        assertEquals(100.0, tracked.getUsagePercentage());
    }

    @Test
    void usagePercentage_halfUsed_is50() {
        takeSnapshot();

        TrackedTuple tuple = new TrackedTuple(1L, "label");
        tuple.getEntityId();

        TrackedObject tracked = getUsageTracker().getTrackedObjects().iterator().next();
        assertEquals(50.0, tracked.getUsagePercentage());
    }

    @Test
    void usagePercentage_neverAccessed_is0() {
        takeSnapshot();

        new TrackedTuple(1L, "label");

        TrackedObject tracked = getUsageTracker().getTrackedObjects().iterator().next();
        assertEquals(0.0, tracked.getUsagePercentage());
    }

    @Test
    void usagePercentage_quarterUsed_is25() {
        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "name", "desc", 5);
        dto.getId();

        TrackedObject tracked = getUsageTracker().getTrackedObjects().iterator().next();
        assertEquals(25.0, tracked.getUsagePercentage());
    }

    // =========================================================================
    // No tracking without snapshot
    // =========================================================================

    @Test
    void noSnapshot_noObjectsTracked() {
        // Don't call takeSnapshot
        TrackedDto dto = new TrackedDto(1L, "test", null, 0);
        dto.getId();

        assertFalse(getUsageTracker().isActive());
        assertFalse(getUsageTracker().hasTrackedObjects());
    }

    // =========================================================================
    // Report output contains never-accessed objects individually
    // =========================================================================

    @Test
    void reportOutput_listsNeverAccessedIndividually() {
        takeSnapshot();

        new TrackedDto(1L, "unused1", null, 0);
        new TrackedDto(2L, "unused2", null, 0);
        new EntityTierOneType("val", 42);
    }
}
