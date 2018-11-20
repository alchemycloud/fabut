package cloud.alchemy.fabut.graph;

import cloud.alchemy.fabut.enums.NodeCheckType;

/**
 * Interface representing isomorphic object graphs structure.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public interface IsomorphicGraph {

    /**
     * Checks if object graph has specified object node pair. Ordering is important.
     * 
     * @param actual
     *            - actual object
     * @param expected
     *            - expected object
     * @return - <code>true</code> if {@link IsomorphicGraph} contains specified pair
     * 
     * @param <T>
     *            generic type
     */
    <T> boolean containsPair(T actual, T expected);

    /**
     * Adds specified object nodes pair if it doesn't exist in object graph.
     * 
     * @param actual
     *            - actual object
     * @param expected
     *            - expected object
     * @param <T>
     *            generic type s
     */
    <T> void addPair(T actual, T expected);

    /**
     * Get expected object pair node for specified actual object node.Ordering is important.
     * 
     * @param actual
     *            - object
     * @return - expected pair object
     * @param <T>
     *            generic type
     */
    <T> T getExpected(T actual);

    /**
     * Checks if object graph has specified actual object node pair. Ordering is important.
     * 
     * @param actual
     *            - actual object
     * @return - <code>true</code> if {@link IsomorphicGraph} contains actual object, <code>false</code> otherwise
     * @param <T>
     *            generic type
     */
    <T> boolean containsActual(T actual);

    /**
     * Checks if object graph has specified expected object node pair. Ordering is important.
     * 
     * @param expected
     *            - expected object
     * @return - <code>true</code> if {@link IsomorphicGraph} contains expected object, <code>false</code> otherwise
     * @param <T>
     *            generic type
     */
    <T> boolean containsExpected(T expected);

    /**
     * For two specified objects check if any or both are contained in node list graph.
     * 
     * @param expected
     *            the expected
     * @param actual
     *            the actual
     * @return - if graph contains object pair return {@link NodeCheckType}.CONTAINS_PAIR, if and only if one of the
     *         nodes is contained in nodes list return {@link NodeCheckType}.SINGLE_NODE, otherwise it is new pair and
     *         return {@link NodeCheckType}.NEW_PAIR.
     * @param <T>
     *            generic type
     */
    <T> NodeCheckType nodeCheck(T expected, T actual);
}
