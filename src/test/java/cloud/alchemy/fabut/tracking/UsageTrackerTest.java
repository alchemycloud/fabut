package cloud.alchemy.fabut.tracking;

import cloud.alchemy.fabut.model.TrackedBooleanDto;
import cloud.alchemy.fabut.model.TrackedDto;
import cloud.alchemy.fabut.model.TrackedTuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsageTrackerTest {

    private UsageTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new UsageTracker();
        UsageTracker.setCurrent(tracker);
    }

    @AfterEach
    void tearDown() {
        UsageTracker.removeCurrent();
    }

    @Test
    void initialState_notActive() {
        assertFalse(tracker.isActive());
    }

    @Test
    void activate_makesActive() {
        tracker.activate();
        assertTrue(tracker.isActive());
    }

    @Test
    void deactivate_makesInactive() {
        tracker.activate();
        tracker.deactivate();
        assertFalse(tracker.isActive());
    }

    @Test
    void reset_clearsEverything() {
        tracker.activate();
        tracker.register(new TrackedDto(1L, "test", null, 5));
        tracker.reset();

        assertFalse(tracker.isActive());
        assertFalse(tracker.hasTrackedObjects());
    }

    @Test
    void register_addsObject() {
        tracker.activate();
        tracker.register(new TrackedDto(1L, "test", null, 5));

        assertTrue(tracker.hasTrackedObjects());
        assertEquals(1, tracker.getTrackedObjects().size());
    }

    @Test
    void register_sameObjectTwice_onlyOnce() {
        tracker.activate();
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        tracker.register(dto);
        tracker.register(dto);

        assertEquals(1, tracker.getTrackedObjects().size());
    }

    @Test
    void register_differentObjects_trackedSeparately() {
        tracker.activate();
        tracker.register(new TrackedDto(1L, "a", null, 1));
        tracker.register(new TrackedDto(2L, "b", null, 2));

        assertEquals(2, tracker.getTrackedObjects().size());
    }

    @Test
    void registerIfActive_registersWhenActive() {
        tracker.activate();
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        UsageTracker.registerIfActive(dto);

        assertTrue(tracker.hasTrackedObjects());
    }

    @Test
    void registerIfActive_skipsWhenInactive() {
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        UsageTracker.registerIfActive(dto);

        assertFalse(tracker.hasTrackedObjects());
    }

    @Test
    void registerIfActive_skipsNull() {
        tracker.activate();
        UsageTracker.registerIfActive(null);

        assertFalse(tracker.hasTrackedObjects());
    }

    @Test
    void recordAccess_tracksFieldOnRegisteredObject() {
        tracker.activate();
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        tracker.register(dto);
        tracker.recordAccess(dto, "name");

        TrackedObject tracked = tracker.getTrackedObjects().iterator().next();
        assertEquals(Set.of("name"), tracked.getAccessedFields());
    }

    @Test
    void recordAccess_ignoresUnregisteredObject() {
        // Create object while tracker is inactive so it's not auto-registered
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        tracker.activate();
        // Object was not registered (created before activation)
        tracker.recordAccess(dto, "name");

        assertFalse(tracker.hasTrackedObjects());
    }

    @Test
    void recordAccessIfActive_recordsWhenActive() {
        tracker.activate();
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        tracker.register(dto);
        UsageTracker.recordAccessIfActive(dto, "name");

        TrackedObject tracked = tracker.getTrackedObjects().iterator().next();
        assertTrue(tracked.getAccessedFields().contains("name"));
    }

    @Test
    void recordAccessIfActive_skipsWhenInactive() {
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        tracker.register(dto);
        UsageTracker.recordAccessIfActive(dto, "name");

        TrackedObject tracked = tracker.getTrackedObjects().iterator().next();
        assertTrue(tracked.getAccessedFields().isEmpty());
    }

    @Test
    void recordAccessIfActive_skipsNull() {
        tracker.activate();
        // Should not throw
        UsageTracker.recordAccessIfActive(null, "name");
    }

    @Test
    void isCurrentActive_trueWhenActive() {
        tracker.activate();
        assertTrue(UsageTracker.isCurrentActive());
    }

    @Test
    void isCurrentActive_falseWhenInactive() {
        assertFalse(UsageTracker.isCurrentActive());
    }

    @Test
    void isCurrentActive_falseWhenNoTracker() {
        UsageTracker.removeCurrent();
        assertFalse(UsageTracker.isCurrentActive());
    }

    @Test
    void getCurrent_returnsSetTracker() {
        assertEquals(tracker, UsageTracker.getCurrent());
    }

    @Test
    void getCurrent_returnsNullWhenNotSet() {
        UsageTracker.removeCurrent();
        assertNull(UsageTracker.getCurrent());
    }

    @Test
    void getFieldNames_extractsFromGetters() {
        Set<String> fields = tracker.getFieldNames(TrackedDto.class);
        assertEquals(Set.of("id", "name", "description", "count"), fields);
    }

    @Test
    void getFieldNames_extractsFromImmutableTuple() {
        Set<String> fields = tracker.getFieldNames(TrackedTuple.class);
        assertEquals(Set.of("entityId", "label"), fields);
    }

    @Test
    void getFieldNames_isCached() {
        Set<String> first = tracker.getFieldNames(TrackedDto.class);
        Set<String> second = tracker.getFieldNames(TrackedDto.class);
        assertSame(first, second);
    }

    @Test
    void getFieldNames_extractsBooleanIsGetter() {
        Set<String> fields = tracker.getFieldNames(TrackedBooleanDto.class);
        assertTrue(fields.contains("active"));
        assertTrue(fields.contains("name"));
        assertEquals(2, fields.size());
    }

    @Test
    void getFieldNames_emptyForObjectClass() {
        Set<String> fields = tracker.getFieldNames(Object.class);
        assertTrue(fields.isEmpty());
    }

    @Test
    void getFieldNames_ignoresMethodsWithParameters() {
        // String has methods like getBytes(charset) which take params — should be ignored
        Set<String> fields = tracker.getFieldNames(String.class);
        // String has no getter-backed fields
        assertTrue(fields.isEmpty());
    }

    @Test
    void register_computesFieldsFromClass() {
        tracker.activate();
        TrackedBooleanDto dto = new TrackedBooleanDto(true, "test");
        tracker.register(dto);

        TrackedObject tracked = tracker.getTrackedObjects().iterator().next();
        assertTrue(tracked.getAllFields().contains("active"));
        assertTrue(tracked.getAllFields().contains("name"));
    }

    @Test
    void getReport_returnsUsageReport() {
        tracker.activate();
        tracker.register(new TrackedDto(1L, "test", null, 5));

        UsageReport report = tracker.getReport();
        assertNotNull(report);
        assertTrue(report.hasTrackedObjects());
    }

    @Test
    void getTrackedObjects_isUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> tracker.getTrackedObjects().clear());
    }

    @Test
    void getFieldNames_excludesIgnoredFields() {
        tracker.setIgnoredFields(Map.of(TrackedDto.class, List.of("id", "count")));

        Set<String> fields = tracker.getFieldNames(TrackedDto.class);

        assertEquals(Set.of("name", "description"), fields);
    }

    @Test
    void register_excludesIgnoredFieldsFromTrackedObject() {
        tracker.setIgnoredFields(Map.of(TrackedDto.class, List.of("description")));
        tracker.activate();
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        tracker.register(dto);

        TrackedObject tracked = tracker.getTrackedObjects().iterator().next();
        assertEquals(Set.of("id", "name", "count"), tracked.getAllFields());
    }

    @Test
    void ignoredFields_notCountedInUsagePercentage() {
        tracker.setIgnoredFields(Map.of(TrackedDto.class, List.of("id", "description")));
        tracker.activate();
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        tracker.register(dto);
        tracker.recordAccess(dto, "name");

        TrackedObject tracked = tracker.getTrackedObjects().iterator().next();
        // 1 accessed out of 2 tracked (name, count) = 50%
        assertEquals(50.0, tracked.getUsagePercentage());
    }

    @Test
    void threadIsolation() throws InterruptedException {
        tracker.activate();
        TrackedDto dto = new TrackedDto(1L, "main", null, 1);
        tracker.register(dto);

        boolean[] otherThreadSawTracker = {false};
        Thread other = new Thread(() -> {
            otherThreadSawTracker[0] = UsageTracker.isCurrentActive();
        });
        other.start();
        other.join();

        assertFalse(otherThreadSawTracker[0], "Other thread should not see this thread's tracker");
        assertTrue(tracker.hasTrackedObjects(), "Main thread tracker should be unaffected");
    }
}
