package cloud.alchemy.fabut.graph;

import cloud.alchemy.fabut.enums.NodeCheckType;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementing class for {@link IsomorphicGraph} using IdentityHashMap for O(1) lookups.
 * Uses object identity (==) rather than equals() for comparisons.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class NodesList implements IsomorphicGraph {
    // O(1) lookup: actual object -> expected object
    private final Map<Object, Object> actualToExpected = new IdentityHashMap<>();
    // O(1) lookup: expected objects set
    private final Set<Object> expectedSet = Collections.newSetFromMap(new IdentityHashMap<>());

    @Override
    public boolean containsPair(final Object expected, final Object actual) {
        Object mapped = actualToExpected.get(actual);
        return mapped != null && mapped == expected;
    }

    @Override
    public void addPair(final Object expected, final Object actual) {
        actualToExpected.put(actual, expected);
        expectedSet.add(expected);
    }

    @Override
    public Object getExpected(final Object actual) {
        return actualToExpected.get(actual);
    }

    @Override
    public boolean containsActual(final Object actual) {
        return actualToExpected.containsKey(actual);
    }

    @Override
    public boolean containsExpected(final Object expected) {
        return expectedSet.contains(expected);
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
