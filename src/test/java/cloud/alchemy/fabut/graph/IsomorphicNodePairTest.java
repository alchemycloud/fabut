package cloud.alchemy.fabut.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for IsomorphicNodePair class.
 */
@SuppressWarnings("deprecation")
public class IsomorphicNodePairTest {

    // ==================== Constructor Tests ====================

    @Test
    void constructor_setsExpectedAndActual() {
        Object expected = "expected";
        Object actual = "actual";

        IsomorphicNodePair pair = new IsomorphicNodePair(expected, actual);

        assertEquals(expected, pair.getExpected());
        assertEquals(actual, pair.getActual());
    }

    @Test
    void constructor_allowsNullValues() {
        IsomorphicNodePair pair = new IsomorphicNodePair(null, null);

        assertNull(pair.getExpected());
        assertNull(pair.getActual());
    }

    // ==================== equals() Tests - Without ID ====================

    @Test
    void equals_sameObjectsWithoutId_returnsTrue() {
        String obj1 = "object1";
        String obj2 = "object2";

        IsomorphicNodePair pair1 = new IsomorphicNodePair(obj1, obj2);
        IsomorphicNodePair pair2 = new IsomorphicNodePair(obj1, obj2);

        assertEquals(pair1, pair2);
    }

    @Test
    void equals_swappedObjectsWithoutId_returnsTrue() {
        String obj1 = "object1";
        String obj2 = "object2";

        IsomorphicNodePair pair1 = new IsomorphicNodePair(obj1, obj2);
        IsomorphicNodePair pair2 = new IsomorphicNodePair(obj2, obj1);

        assertEquals(pair1, pair2);
    }

    @Test
    void equals_differentObjectsWithoutId_returnsFalse() {
        IsomorphicNodePair pair1 = new IsomorphicNodePair("a", "b");
        IsomorphicNodePair pair2 = new IsomorphicNodePair("c", "d");

        assertNotEquals(pair1, pair2);
    }

    @Test
    void equals_notIsomorphicNodePair_returnsFalse() {
        IsomorphicNodePair pair = new IsomorphicNodePair("a", "b");

        assertNotEquals(pair, "not a pair");
        assertNotEquals(pair, null);
        assertNotEquals(pair, 42);
    }

    // ==================== equals() Tests - With ID ====================

    @Test
    void equals_sameIdsInSameOrder_returnsTrue() {
        EntityWithId obj1 = new EntityWithId(1L);
        EntityWithId obj2 = new EntityWithId(2L);

        IsomorphicNodePair pair1 = new IsomorphicNodePair(obj1, obj2);
        IsomorphicNodePair pair2 = new IsomorphicNodePair(obj1, obj2);

        assertEquals(pair1, pair2);
    }

    @Test
    void equals_sameIdsSwapped_returnsTrue() {
        EntityWithId obj1 = new EntityWithId(1L);
        EntityWithId obj2 = new EntityWithId(2L);

        IsomorphicNodePair pair1 = new IsomorphicNodePair(obj1, obj2);
        IsomorphicNodePair pair2 = new IsomorphicNodePair(obj2, obj1);

        assertEquals(pair1, pair2);
    }

    @Test
    void equals_differentIds_returnsFalse() {
        EntityWithId obj1 = new EntityWithId(1L);
        EntityWithId obj2 = new EntityWithId(2L);
        EntityWithId obj3 = new EntityWithId(3L);
        EntityWithId obj4 = new EntityWithId(4L);

        IsomorphicNodePair pair1 = new IsomorphicNodePair(obj1, obj2);
        IsomorphicNodePair pair2 = new IsomorphicNodePair(obj3, obj4);

        assertNotEquals(pair1, pair2);
    }

    // ==================== Test Helper Class ====================

    /**
     * Simple test entity with an ID field.
     */
    public static class EntityWithId {
        private final Long id;

        public EntityWithId(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}
