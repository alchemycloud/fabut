package cloud.alchemy.fabut.graph;

import cloud.alchemy.fabut.enums.NodeCheckType;
import cloud.alchemy.fabut.model.TierOneType;
import org.junit.Assert;

import org.junit.Test;

/**
 * Tests for {@link NodesList}.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class NodesListTest extends Assert {

    private static final String TEST = "test";

    /**
     * Test for containsPair of {@link NodesList} when list doesn't contain specified pair.
     */
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

    /**
     * Test for containsPair of {@link NodesList} when list contains specified pair.
     */
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

    /**
     * Test for addPair of {@link NodesList} when list doesn't contain specified pair.
     */
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

    /**
     * Test for getExpected of {@link NodesList} for specified actual pair.
     */
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

    /**
     * Test for getExpected of {@link NodesList} when specified actual paid doesn't have its expected match.
     */
    @Test
    public void testGetExpectedNull() {
        // setup
        final NodesList nodesList = new NodesList();

        // method
        final Object assertObject = nodesList.getExpected(new Object());

        // assert
        assertNull(assertObject);
    }

    /**
     * Test for containActual of {@link NodesList} when specified actual object is in the list.
     */
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

    /**
     * Test for containsActual of {@link NodesList} when specified actual object isn't in the list.
     */
    @Test
    public void testContainsActualFalse() {
        // setup
        final NodesList nodesList = new NodesList();

        // method
        final boolean contains = nodesList.containsActual(new Object());

        // assert
        assertFalse(contains);
    }

    /**
     * Test for containsExpected of {@link NodesList} when specified expected object is in the list.
     */
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

    /**
     * Test for containsExpected of {@link NodesList} when specified expected object isn't in the list.
     */
    @Test
    public void testContainsExpectedFalse() {
        // setup
        final NodesList nodesList = new NodesList();

        // method
        final boolean contains = nodesList.containsExpected(new Object());

        // assert
        assertFalse(contains);
    }

    /**
     * Test for checkIfContains of {@link FabutObjectAssert} when object pair is contained in list.
     */
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

    /**
     * Test for checkIfContains of {@link FabutObjectAssert} when one of object nodes from object pair is contained
     * in list.
     */
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

    /**
     * Test for checkIfContains of {@link FabutObjectAssert} when object pair is not contained in list.
     */
    @Test
    public void testCheckIfContainsNull() {
        // setup
        final NodesList nodesList = new NodesList();

        // method
        final NodeCheckType assertValue = nodesList.nodeCheck(new Object(), new Object());

        // assert
        assertEquals(NodeCheckType.NEW_PAIR, assertValue);
    }

}
