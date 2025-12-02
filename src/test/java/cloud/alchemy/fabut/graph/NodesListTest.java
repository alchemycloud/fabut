package cloud.alchemy.fabut.graph;

import cloud.alchemy.fabut.enums.NodeCheckType;
import cloud.alchemy.fabut.model.TierOneType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NodesList}.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class NodesListTest extends Assertions {

    private static final String TEST = "test";

    /** Test for containsPair of {@link NodesList} when list doesn't contain specified pair. */
    @Test
    public void testContainsPairFalse() {
        // setup
        final NodesList nodesList = new NodesList();
        nodesList.addPair(new TierOneType(TEST), new TierOneType(TEST));

        // method
        final boolean assertValue = nodesList.containsPair(new TierOneType(TEST), new TierOneType(TEST));

        // assert
        assertFalse(assertValue);
    }

    /** Test for containsPair of {@link NodesList} when list contains specified pair. */
    @Test
    public void testContainsPairTrue() {
        // setup
        final NodesList nodesList = new NodesList();
        final TierOneType actual = new TierOneType(TEST);
        final TierOneType expected = new TierOneType(TEST);
        nodesList.addPair(actual, expected);

        // method
        final boolean assertValue = nodesList.containsPair(actual, expected);

        // assert
        assertTrue(assertValue);
    }

    /** Test for addPair of {@link NodesList} when list doesn't contain specified pair. */
    @Test
    public void testAddPair() {
        // setup
        final NodesList nodesList = new NodesList();
        final TierOneType actual = new TierOneType(TEST);
        final TierOneType expected = new TierOneType(TEST);

        // method
        nodesList.addPair(actual, expected);

        // assert
        final boolean assertValue = nodesList.containsPair(actual, expected);
        assertTrue(assertValue);
    }

    /** Test for getExpected of {@link NodesList} for specified actual pair. */
    @Test
    public void testGetExpectedNotNull() {
        // setup
        final NodesList nodesList = new NodesList();
        final Object actual = new Object();
        final Object expected = new Object();
        nodesList.addPair(expected, actual);

        // method
        final Object assertObject = nodesList.getExpected(actual);

        // assert
        assertNotNull(assertObject);
        assertEquals(expected, assertObject);
    }

    /** Test for getExpected of {@link NodesList} when specified actual paid doesn't have its expected match. */
    @Test
    public void testGetExpectedNull() {
        // setup
        final NodesList nodesList = new NodesList();

        // method
        final Object assertObject = nodesList.getExpected(new Object());

        // assert
        assertNull(assertObject);
    }

    /** Test for containActual of {@link NodesList} when specified actual object is in the list. */
    @Test
    public void testContainsActualTrue() {
        // setup
        final NodesList nodesList = new NodesList();
        final Object actual = new Object();
        nodesList.addPair(new Object(), actual);

        // method
        final boolean contains = nodesList.containsActual(actual);

        // assert
        assertTrue(contains);
    }

    /** Test for containsActual of {@link NodesList} when specified actual object isn't in the list. */
    @Test
    public void testContainsActualFalse() {
        // setup
        final NodesList nodesList = new NodesList();

        // method
        final boolean contains = nodesList.containsActual(new Object());

        // assert
        assertFalse(contains);
    }

    /** Test for containsExpected of {@link NodesList} when specified expected object is in the list. */
    @Test
    public void testContainsExpectedTrue() {
        // setup
        final NodesList nodesList = new NodesList();
        final Object expected = new Object();
        nodesList.addPair(expected, new Object());

        // method
        final boolean contains = nodesList.containsExpected(expected);

        // assert
        assertTrue(contains);
    }

    /** Test for containsExpected of {@link NodesList} when specified expected object isn't in the list. */
    @Test
    public void testContainsExpectedFalse() {
        // setup
        final NodesList nodesList = new NodesList();

        // method
        final boolean contains = nodesList.containsExpected(new Object());

        // assert
        assertFalse(contains);
    }

    @Test
    public void testCheckIfContainsTrue() {
        // setup
        final NodesList nodesList = new NodesList();
        final Object actual = new Object();
        final Object expected = new Object();
        nodesList.addPair(expected, actual);

        // method
        final NodeCheckType assertValue = nodesList.nodeCheck(expected, actual);

        // assert
        assertEquals(NodeCheckType.CONTAINS_PAIR, assertValue);
    }

    @Test
    public void testCheckIfContainsFalse() {
        // setup
        final NodesList nodesList = new NodesList();
        final Object actual = new Object();
        final Object expected = new Object();
        nodesList.addPair(expected, actual);

        // method
        final NodeCheckType assertValue1 = nodesList.nodeCheck(new Object(), expected);
        final NodeCheckType assertValue2 = nodesList.nodeCheck(actual, new Object());

        // assert
        assertEquals(NodeCheckType.SINGLE_NODE, assertValue1);
        assertEquals(NodeCheckType.SINGLE_NODE, assertValue2);
    }

    @Test
    public void testCheckIfContainsNull() {
        // setup
        final NodesList nodesList = new NodesList();

        // method
        final NodeCheckType assertValue = nodesList.nodeCheck(new Object(), new Object());

        // assert
        assertEquals(NodeCheckType.NEW_PAIR, assertValue);
    }

    // ==================== Identity-based tests (IdentityHashMap behavior) ====================

    /**
     * Test that NodesList uses identity (==) not equals() for comparison.
     * Two different objects with same content should NOT be considered equal.
     */
    @Test
    public void testIdentityBasedComparison() {
        // setup - two objects with same content but different identity
        final NodesList nodesList = new NodesList();
        final TierOneType obj1 = new TierOneType(TEST);
        final TierOneType obj2 = new TierOneType(TEST);

        nodesList.addPair(obj1, obj1);

        // method - check with different object (same content)
        final boolean containsObj2 = nodesList.containsActual(obj2);

        // assert - should NOT contain because identity is different
        assertFalse(containsObj2);
    }

    @Test
    public void testIdentityBasedContainsPair() {
        // setup
        final NodesList nodesList = new NodesList();
        final TierOneType expected1 = new TierOneType(TEST);
        final TierOneType actual1 = new TierOneType(TEST);
        final TierOneType expected2 = new TierOneType(TEST);
        final TierOneType actual2 = new TierOneType(TEST);

        nodesList.addPair(expected1, actual1);

        // method - check with different objects (same content)
        final boolean containsPair = nodesList.containsPair(expected2, actual2);

        // assert - should NOT match because identity is different
        assertFalse(containsPair);
    }

    @Test
    public void testIdentityBasedGetExpected() {
        // setup
        final NodesList nodesList = new NodesList();
        final Object expected = new Object();
        final Object actual = new Object();
        nodesList.addPair(expected, actual);

        // method - lookup with same actual reference
        final Object result = nodesList.getExpected(actual);

        // assert
        assertSame(expected, result);
    }

    /**
     * Test that same object used as both expected and actual works correctly.
     */
    @Test
    public void testSameObjectAsExpectedAndActual() {
        // setup
        final NodesList nodesList = new NodesList();
        final Object obj = new Object();

        nodesList.addPair(obj, obj);

        // method & assert
        assertTrue(nodesList.containsActual(obj));
        assertTrue(nodesList.containsExpected(obj));
        assertTrue(nodesList.containsPair(obj, obj));
        assertSame(obj, nodesList.getExpected(obj));
    }

    // ==================== Multiple pairs tests ====================

    @Test
    public void testMultiplePairs() {
        // setup
        final NodesList nodesList = new NodesList();
        final Object expected1 = new Object();
        final Object actual1 = new Object();
        final Object expected2 = new Object();
        final Object actual2 = new Object();
        final Object expected3 = new Object();
        final Object actual3 = new Object();

        nodesList.addPair(expected1, actual1);
        nodesList.addPair(expected2, actual2);
        nodesList.addPair(expected3, actual3);

        // assert all pairs are present
        assertTrue(nodesList.containsPair(expected1, actual1));
        assertTrue(nodesList.containsPair(expected2, actual2));
        assertTrue(nodesList.containsPair(expected3, actual3));

        // assert all actuals are present
        assertTrue(nodesList.containsActual(actual1));
        assertTrue(nodesList.containsActual(actual2));
        assertTrue(nodesList.containsActual(actual3));

        // assert all expecteds are present
        assertTrue(nodesList.containsExpected(expected1));
        assertTrue(nodesList.containsExpected(expected2));
        assertTrue(nodesList.containsExpected(expected3));

        // assert getExpected returns correct values
        assertSame(expected1, nodesList.getExpected(actual1));
        assertSame(expected2, nodesList.getExpected(actual2));
        assertSame(expected3, nodesList.getExpected(actual3));
    }

    // ==================== Performance test ====================

    @Test
    public void testPerformanceWithManyPairs() {
        // setup - add 1000 pairs
        final NodesList nodesList = new NodesList();
        final int count = 1000;
        final Object[] expecteds = new Object[count];
        final Object[] actuals = new Object[count];

        for (int i = 0; i < count; i++) {
            expecteds[i] = new Object();
            actuals[i] = new Object();
            nodesList.addPair(expecteds[i], actuals[i]);
        }

        // method - verify all lookups work (should be O(1))
        long startTime = System.nanoTime();
        for (int i = 0; i < count; i++) {
            assertTrue(nodesList.containsActual(actuals[i]));
            assertTrue(nodesList.containsExpected(expecteds[i]));
            assertSame(expecteds[i], nodesList.getExpected(actuals[i]));
        }
        long endTime = System.nanoTime();

        // assert - should complete very fast (< 100ms for 3000 operations)
        long durationMs = (endTime - startTime) / 1_000_000;
        assertTrue(durationMs < 100, "Performance test took too long: " + durationMs + "ms");
    }

    @Test
    public void testNodeCheckWithMultiplePairs() {
        // setup
        final NodesList nodesList = new NodesList();
        final Object expected1 = new Object();
        final Object actual1 = new Object();
        final Object expected2 = new Object();
        final Object actual2 = new Object();

        nodesList.addPair(expected1, actual1);
        nodesList.addPair(expected2, actual2);

        // assert existing pairs
        assertEquals(NodeCheckType.CONTAINS_PAIR, nodesList.nodeCheck(expected1, actual1));
        assertEquals(NodeCheckType.CONTAINS_PAIR, nodesList.nodeCheck(expected2, actual2));

        // assert single node cases - detects cross-references (object on opposite side)
        // nodeCheck checks: containsExpected(actual) OR containsActual(expected)
        // This detects cycles where an object appears in both expected and actual roles
        assertEquals(NodeCheckType.SINGLE_NODE, nodesList.nodeCheck(actual1, new Object())); // actual1 was seen as actual, now as expected
        assertEquals(NodeCheckType.SINGLE_NODE, nodesList.nodeCheck(new Object(), expected1)); // expected1 was seen as expected, now as actual

        // These are NEW_PAIR because the objects haven't crossed roles
        assertEquals(NodeCheckType.NEW_PAIR, nodesList.nodeCheck(expected1, new Object())); // expected1 is expected role, same as before
        assertEquals(NodeCheckType.NEW_PAIR, nodesList.nodeCheck(new Object(), actual1)); // actual1 is actual role, same as before

        // assert new pair case - completely new objects
        assertEquals(NodeCheckType.NEW_PAIR, nodesList.nodeCheck(new Object(), new Object()));
    }
}
