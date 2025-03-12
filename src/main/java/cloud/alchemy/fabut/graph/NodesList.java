package cloud.alchemy.fabut.graph;

import cloud.alchemy.fabut.enums.NodeCheckType;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

/**
 * Implementing class for {@link IsomorphicGraph} using concurrent collections for thread-safety
 * and parallel processing for improved performance.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class NodesList implements IsomorphicGraph {
    private final Queue<IsomorphicNodePair> isomorphicNodes;
    private static final int PARALLEL_THRESHOLD = 100;

    /** Default constructor. */
    public NodesList() {
        isomorphicNodes = new ConcurrentLinkedQueue<>();
    }

    @Override
    public boolean containsPair(final Object actual, final Object expected) {
        return isomorphicNodes.contains(new IsomorphicNodePair(actual, expected));
    }

    @Override
    public void addPair(final Object actual, final Object expected) {
        isomorphicNodes.add(new IsomorphicNodePair(actual, expected));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getExpected(final Object actual) {
        // For large collections, use parallel stream for better performance
        if (isomorphicNodes.size() > PARALLEL_THRESHOLD) {
            return isomorphicNodes.parallelStream()
                    .filter(node -> node.getActual() == actual)
                    .findFirst()
                    .map(IsomorphicNodePair::getExpected)
                    .orElse(null);
        } else {
            // Sequential approach for smaller collections to avoid overhead
            for (final IsomorphicNodePair isomorphicNode : isomorphicNodes) {
                if (isomorphicNode.getActual() == actual) {
                    return isomorphicNode.getExpected();
                }
            }
            return null;
        }
    }

    @Override
    public boolean containsActual(final Object actual) {
        // For large collections, use parallel stream for better performance
        if (isomorphicNodes.size() > PARALLEL_THRESHOLD) {
            return isomorphicNodes.parallelStream()
                    .anyMatch(node -> node.getActual() == actual);
        } else {
            // Sequential approach for smaller collections to avoid overhead
            for (final IsomorphicNodePair isomorphicNode : isomorphicNodes) {
                if (isomorphicNode.getActual() == actual) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean containsExpected(final Object expected) {
        // For large collections, use parallel stream for better performance
        if (isomorphicNodes.size() > PARALLEL_THRESHOLD) {
            return isomorphicNodes.parallelStream()
                    .anyMatch(node -> node.getExpected() == expected);
        } else {
            // Sequential approach for smaller collections to avoid overhead
            for (final IsomorphicNodePair isomorphicNode : isomorphicNodes) {
                if (isomorphicNode.getExpected() == expected) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public NodeCheckType nodeCheck(final Object expected, final Object actual) {
        if (containsPair(expected, actual)) {
            return NodeCheckType.CONTAINS_PAIR;
        } else if (containsExpected(actual) || containsActual(expected)) {
            return NodeCheckType.SINGLE_NODE;
        }
        return NodeCheckType.NEW_PAIR;
    }
}
