package cloud.alchemy.fabut.tracking;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TrackedObjectTest {

    @Test
    void constructor_storesFields() {
        Set<String> fields = Set.of("id", "name", "count");
        TrackedObject tracked = new TrackedObject(42, String.class, fields);

        assertEquals(42, tracked.getIdentityHash());
        assertEquals(String.class, tracked.getObjectClass());
        assertEquals(fields, tracked.getAllFields());
        assertTrue(tracked.getAccessedFields().isEmpty());
    }

    @Test
    void allFields_isUnmodifiable() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id"));
        assertThrows(UnsupportedOperationException.class, () -> tracked.getAllFields().add("x"));
    }

    @Test
    void accessedFields_isUnmodifiable() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id"));
        tracked.recordAccess("id");
        assertThrows(UnsupportedOperationException.class, () -> tracked.getAccessedFields().add("x"));
    }

    @Test
    void recordAccess_tracksKnownField() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id", "name"));
        tracked.recordAccess("id");

        assertEquals(Set.of("id"), tracked.getAccessedFields());
        assertEquals(1, tracked.getAccessedCount());
    }

    @Test
    void recordAccess_ignoresUnknownField() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id"));
        tracked.recordAccess("unknown");

        assertTrue(tracked.getAccessedFields().isEmpty());
        assertEquals(0, tracked.getAccessedCount());
    }

    @Test
    void recordAccess_duplicateAccessCountsOnce() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id"));
        tracked.recordAccess("id");
        tracked.recordAccess("id");

        assertEquals(1, tracked.getAccessedCount());
    }

    @Test
    void getUnusedFields_returnsFieldsNotAccessed() {
        Set<String> fields = new LinkedHashSet<>();
        fields.add("id");
        fields.add("name");
        fields.add("count");
        TrackedObject tracked = new TrackedObject(1, String.class, fields);
        tracked.recordAccess("name");

        Set<String> unused = tracked.getUnusedFields();
        assertEquals(Set.of("id", "count"), unused);
    }

    @Test
    void getUnusedFields_emptyWhenAllAccessed() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id", "name"));
        tracked.recordAccess("id");
        tracked.recordAccess("name");

        assertTrue(tracked.getUnusedFields().isEmpty());
    }

    @Test
    void getUsagePercentage_zeroWhenNothingAccessed() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id", "name"));
        assertEquals(0.0, tracked.getUsagePercentage(), 0.001);
    }

    @Test
    void getUsagePercentage_hundredWhenAllAccessed() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id", "name"));
        tracked.recordAccess("id");
        tracked.recordAccess("name");
        assertEquals(100.0, tracked.getUsagePercentage(), 0.001);
    }

    @Test
    void getUsagePercentage_fiftyWhenHalfAccessed() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id", "name"));
        tracked.recordAccess("id");
        assertEquals(50.0, tracked.getUsagePercentage(), 0.001);
    }

    @Test
    void getUsagePercentage_hundredForEmptyFields() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of());
        assertEquals(100.0, tracked.getUsagePercentage(), 0.001);
    }

    @Test
    void isFullyUsed_trueWhenAllAccessed() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id"));
        tracked.recordAccess("id");
        assertTrue(tracked.isFullyUsed());
    }

    @Test
    void isFullyUsed_falseWhenNotAllAccessed() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id", "name"));
        tracked.recordAccess("id");
        assertFalse(tracked.isFullyUsed());
    }

    @Test
    void isFullyUsed_trueForEmptyFields() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of());
        assertTrue(tracked.isFullyUsed());
    }

    @Test
    void isNeverAccessed_trueWhenNoAccess() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id"));
        assertTrue(tracked.isNeverAccessed());
    }

    @Test
    void isNeverAccessed_falseWhenSomeAccess() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("id", "name"));
        tracked.recordAccess("id");
        assertFalse(tracked.isNeverAccessed());
    }

    @Test
    void isNeverAccessed_falseForEmptyFields() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of());
        assertFalse(tracked.isNeverAccessed());
    }

    @Test
    void getTotalFieldCount() {
        TrackedObject tracked = new TrackedObject(1, String.class, Set.of("a", "b", "c"));
        assertEquals(3, tracked.getTotalFieldCount());
    }

    @Test
    void constructor_nullClassThrows() {
        assertThrows(NullPointerException.class, () -> new TrackedObject(1, null, Set.of()));
    }

    @Test
    void constructor_nullFieldsThrows() {
        assertThrows(NullPointerException.class, () -> new TrackedObject(1, String.class, null));
    }
}
