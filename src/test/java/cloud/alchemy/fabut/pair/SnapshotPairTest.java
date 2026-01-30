package cloud.alchemy.fabut.pair;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SnapshotPair class.
 */
public class SnapshotPairTest {

    @Test
    void constructor_setsExpectedAndActual() {
        Object expected = "expected";
        Object actual = "actual";

        SnapshotPair pair = new SnapshotPair(expected, actual);

        assertEquals(expected, pair.getExpected());
        assertEquals(actual, pair.getActual());
    }

    @Test
    void constructor_assertedIsFalseByDefault() {
        SnapshotPair pair = new SnapshotPair("expected", "actual");

        assertFalse(pair.isAsserted());
    }

    @Test
    void setAsserted_changesAssertedStatus() {
        SnapshotPair pair = new SnapshotPair("expected", "actual");

        assertFalse(pair.isAsserted());

        pair.setAsserted(true);
        assertTrue(pair.isAsserted());

        pair.setAsserted(false);
        assertFalse(pair.isAsserted());
    }

    @Test
    void constructor_allowsNullValues() {
        SnapshotPair pair = new SnapshotPair(null, null);

        assertNull(pair.getExpected());
        assertNull(pair.getActual());
        assertFalse(pair.isAsserted());
    }

    @Test
    void isAsserted_returnsCurrentState() {
        SnapshotPair pair = new SnapshotPair("a", "b");

        pair.setAsserted(true);
        assertTrue(pair.isAsserted());
    }
}
