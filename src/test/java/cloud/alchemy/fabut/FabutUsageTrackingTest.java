package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.EntityTierOneType;
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

import static org.junit.jupiter.api.Assertions.*;

class FabutUsageTrackingTest extends Fabut {

    private final List<Object> entityTierOneTypes = new ArrayList<>();
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream capturedOutput;

    public FabutUsageTrackingTest() {
        entityTypes.add(EntityTierOneType.class);
        complexTypes.add(TrackedDto.class);
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

    @Test
    void usageTracker_availableAfterBefore() {
        assertNotNull(getUsageTracker());
    }

    @Test
    void usageTracker_notActiveBeforeSnapshot() {
        assertFalse(getUsageTracker().isActive());
    }

    @Test
    void usageTracker_activeAfterSnapshot() {
        takeSnapshot();
        assertTrue(getUsageTracker().isActive());
    }

    @Test
    void trackedTypes_instrumentedAfterSnapshot() {
        takeSnapshot();

        // Create a tracked tuple after snapshot — should be registered
        TrackedTuple tuple = new TrackedTuple(1L, "test");

        assertTrue(getUsageTracker().hasTrackedObjects());
    }

    @Test
    void complexTypes_instrumentedAfterSnapshot() {
        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "test", null, 5);

        assertTrue(getUsageTracker().hasTrackedObjects());
    }

    @Test
    void fullLifecycle_partialUsage() {
        takeSnapshot();

        // Simulate: service fetches DTO but only uses id and name
        TrackedDto dto = new TrackedDto(1L, "test", "desc", 42);
        dto.getId();
        dto.getName();

        // Verify tracking recorded partial usage
        UsageReport report = getUsageTracker().getReport();
        assertTrue(report.hasUnderusedObjects());

        List<TrackedObject> underused = report.getUnderusedObjects();
        assertEquals(1, underused.size());

        TrackedObject tracked = underused.getFirst();
        assertEquals(TrackedDto.class, tracked.getObjectClass());
        assertTrue(tracked.getAccessedFields().contains("id"));
        assertTrue(tracked.getAccessedFields().contains("name"));
        assertFalse(tracked.getAccessedFields().contains("count"));
        assertFalse(tracked.getAccessedFields().contains("description"));
    }

    @Test
    void fullLifecycle_fullyUsed() {
        takeSnapshot();

        TrackedTuple tuple = new TrackedTuple(1L, "label");
        tuple.getEntityId();
        tuple.getLabel();

        UsageReport report = getUsageTracker().getReport();
        assertFalse(report.hasUnderusedObjects());
    }

    @Test
    void fullLifecycle_neverAccessed() {
        takeSnapshot();

        // Fetched but never used
        new TrackedDto(1L, "unused", null, 0);

        UsageReport report = getUsageTracker().getReport();
        assertTrue(report.hasUnderusedObjects());

        TrackedObject tracked = report.getUnderusedObjects().getFirst();
        assertTrue(tracked.isNeverAccessed());
    }

    @Test
    void reportPrintedToStdout() {
        takeSnapshot();

        // Create objects so report has content
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        dto.getId();
    }

    @Test
    void noReport_whenNoSnapshot() {
        // Don't call takeSnapshot — no tracking happens
        new TrackedDto(1L, "test", null, 5);
    }

    @Test
    void noReport_whenNoObjectsFetched() {
        takeSnapshot();
        // Don't create any tracked objects
    }

    @Test
    void multipleTrackedTypes_inSameTest() {
        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        dto.getId();

        TrackedTuple tuple = new TrackedTuple(2L, "label");
        tuple.getEntityId();
        tuple.getLabel();

        UsageReport report = getUsageTracker().getReport();
        var summary = report.getSummaryByClass();
        assertEquals(2, summary.size());
        assertNotNull(summary.get("TrackedDto"));
        assertNotNull(summary.get("TrackedTuple"));
    }

    @Test
    void pauseTracking_stopsRecordingFieldAccess() {
        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "test", "desc", 42);
        dto.getId();
        dto.getName();

        pauseTracking();

        // Access during pause should NOT be recorded
        dto.getDescription();
        dto.getCount();

        UsageReport report = getUsageTracker().getReport();
        TrackedObject tracked = report.getUnderusedObjects().getFirst();
        assertEquals(2, tracked.getAccessedFields().size());
        assertTrue(tracked.getAccessedFields().contains("id"));
        assertTrue(tracked.getAccessedFields().contains("name"));
        assertFalse(tracked.getAccessedFields().contains("description"));
        assertFalse(tracked.getAccessedFields().contains("count"));
    }

    @Test
    void pauseTracking_objectsCreatedAfterPauseNotTracked() {
        takeSnapshot();

        TrackedDto dto1 = new TrackedDto(1L, "before", null, 1);

        pauseTracking();

        TrackedDto dto2 = new TrackedDto(2L, "after", null, 2);

        assertEquals(1, getUsageTracker().getTrackedObjects().size());
    }

    @Test
    void pauseTracking_beforeAnySnapshot_noError() {
        // Should not throw even if no snapshot was taken
        pauseTracking();
    }

    @Test
    void pauseTracking_calledTwice_noError() {
        takeSnapshot();
        pauseTracking();
        pauseTracking();
        assertFalse(getUsageTracker().isActive());
    }
}
