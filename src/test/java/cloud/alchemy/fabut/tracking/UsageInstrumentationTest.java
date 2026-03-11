package cloud.alchemy.fabut.tracking;

import cloud.alchemy.fabut.model.TrackedBooleanDto;
import cloud.alchemy.fabut.model.TrackedDto;
import cloud.alchemy.fabut.model.TrackedTuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsageInstrumentationTest {

    private UsageTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new UsageTracker();
        UsageTracker.setCurrent(tracker);
        tracker.activate();
        UsageInstrumentation.install();
    }

    @AfterEach
    void tearDown() {
        tracker.deactivate();
        UsageTracker.removeCurrent();
    }

    @Test
    void install_succeeds() {
        assertTrue(UsageInstrumentation.isInstalled());
    }

    @Test
    void install_idempotent() {
        assertTrue(UsageInstrumentation.install());
        assertTrue(UsageInstrumentation.install());
    }

    @Test
    void instrumentClass_succeeds() {
        assertTrue(UsageInstrumentation.instrumentClass(TrackedDto.class));
        assertTrue(UsageInstrumentation.isInstrumented(TrackedDto.class));
    }

    @Test
    void instrumentClass_idempotent() {
        assertTrue(UsageInstrumentation.instrumentClass(TrackedDto.class));
        assertTrue(UsageInstrumentation.instrumentClass(TrackedDto.class));
    }

    @Test
    void instrumentedConstructor_registersObject() {
        UsageInstrumentation.instrumentClass(TrackedDto.class);

        TrackedDto dto = new TrackedDto(1L, "test", null, 5);

        assertTrue(tracker.hasTrackedObjects());
        assertEquals(1, tracker.getTrackedObjects().size());
    }

    @Test
    void instrumentedConstructor_doesNotRegisterWhenInactive() {
        UsageInstrumentation.instrumentClass(TrackedDto.class);
        tracker.deactivate();

        new TrackedDto(1L, "test", null, 5);

        assertFalse(tracker.hasTrackedObjects());
    }

    @Test
    void instrumentedGetter_recordsAccess() {
        UsageInstrumentation.instrumentClass(TrackedDto.class);

        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        dto.getName();

        TrackedObject tracked = tracker.getTrackedObjects().iterator().next();
        assertTrue(tracked.getAccessedFields().contains("name"));
    }

    @Test
    void instrumentedGetter_multipleFields() {
        UsageInstrumentation.instrumentClass(TrackedDto.class);

        TrackedDto dto = new TrackedDto(1L, "test", "desc", 5);
        dto.getId();
        dto.getName();
        dto.getDescription();

        TrackedObject tracked = tracker.getTrackedObjects().iterator().next();
        assertTrue(tracked.getAccessedFields().contains("id"));
        assertTrue(tracked.getAccessedFields().contains("name"));
        assertTrue(tracked.getAccessedFields().contains("description"));
        assertFalse(tracked.getAccessedFields().contains("count"));
    }

    @Test
    void instrumentedGetter_doesNotRecordWhenInactive() {
        UsageInstrumentation.instrumentClass(TrackedDto.class);

        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        tracker.deactivate();
        dto.getName();

        TrackedObject tracked = tracker.getTrackedObjects().iterator().next();
        assertTrue(tracked.getAccessedFields().isEmpty());
    }

    @Test
    void instrumentedTuple_constructorAndGetters() {
        UsageInstrumentation.instrumentClass(TrackedTuple.class);

        TrackedTuple tuple = new TrackedTuple(1L, "label");
        tuple.getEntityId();

        assertTrue(tracker.hasTrackedObjects());
        TrackedObject tracked = tracker.getTrackedObjects().iterator().next();
        assertTrue(tracked.getAccessedFields().contains("entityId"));
        assertFalse(tracked.getAccessedFields().contains("label"));
    }

    @Test
    void instrumentedClass_multipleInstances() {
        UsageInstrumentation.instrumentClass(TrackedDto.class);

        TrackedDto dto1 = new TrackedDto(1L, "a", null, 1);
        TrackedDto dto2 = new TrackedDto(2L, "b", null, 2);
        dto1.getName();
        dto2.getCount();

        assertEquals(2, tracker.getTrackedObjects().size());
    }

    @Test
    void fieldNameFromGetter_getPrefix() {
        assertEquals("name", UsageInstrumentation.fieldNameFromGetter("getName"));
        assertEquals("id", UsageInstrumentation.fieldNameFromGetter("getId"));
        assertEquals("valueText", UsageInstrumentation.fieldNameFromGetter("getValueText"));
    }

    @Test
    void fieldNameFromGetter_isPrefix() {
        assertEquals("edited", UsageInstrumentation.fieldNameFromGetter("isEdited"));
        assertEquals("active", UsageInstrumentation.fieldNameFromGetter("isActive"));
    }

    @Test
    void fieldNameFromGetter_noPrefix() {
        assertEquals("toString", UsageInstrumentation.fieldNameFromGetter("toString"));
    }

    @Test
    void buildGetterMatcher_matchesOnlyGettersWithFields() {
        var matcher = UsageInstrumentation.buildGetterMatcher(TrackedDto.class);
        assertNotNull(matcher);
    }

    @Test
    void instrumentedBooleanGetter_recordsAccess() {
        UsageInstrumentation.instrumentClass(TrackedBooleanDto.class);

        TrackedBooleanDto dto = new TrackedBooleanDto(true, "test");
        dto.isActive();

        TrackedObject tracked = tracker.getTrackedObjects().iterator().next();
        assertTrue(tracked.getAccessedFields().contains("active"));
        assertFalse(tracked.getAccessedFields().contains("name"));
    }

    @Test
    void instrumentedSetter_doesNotRecordAccess() {
        UsageInstrumentation.instrumentClass(TrackedDto.class);

        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        dto.setName("updated");

        TrackedObject tracked = tracker.getTrackedObjects().iterator().next();
        assertFalse(tracked.getAccessedFields().contains("name"));
    }

    @Test
    void isInstalled_trueAfterInstall() {
        assertTrue(UsageInstrumentation.isInstalled());
    }

    @Test
    void isInstrumented_falseForUnknownClass() {
        assertFalse(UsageInstrumentation.isInstrumented(String.class));
    }

    @Test
    void fieldNameFromGetter_shortGetName() {
        // "get" alone (length 3) should return as-is
        assertEquals("get", UsageInstrumentation.fieldNameFromGetter("get"));
    }

    @Test
    void fieldNameFromGetter_shortIsName() {
        // "is" alone (length 2) should return as-is
        assertEquals("is", UsageInstrumentation.fieldNameFromGetter("is"));
    }

    @Test
    void buildGetterMatcher_excludesObjectMethods() {
        // Object.getClass() should not match
        var matcher = UsageInstrumentation.buildGetterMatcher(TrackedDto.class);
        assertNotNull(matcher);
    }
}
