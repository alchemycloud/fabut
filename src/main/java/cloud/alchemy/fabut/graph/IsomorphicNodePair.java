package cloud.alchemy.fabut.graph;

import cloud.alchemy.fabut.ReflectionUtil;
import cloud.alchemy.fabut.pair.Pair;

/**
 * Class representing object pair from {@link IsomorphicGraph}.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Nikola Trkulja
 */
public class IsomorphicNodePair extends Pair {

    /**
     * Default Isomorphic node pair constructor.
     *
     * @param expected object
     * @param actual object
     */
    public IsomorphicNodePair(final Object expected, final Object actual) {
        super(expected, actual);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof IsomorphicNodePair node)) {
            return false;
        }

        if (ReflectionUtil.hasIdMethod(obj)) {
            final Object nodeActualId = ReflectionUtil.getIdValue(node.getActual());
            final Object actualId = ReflectionUtil.getIdValue(getActual());
            final Object nodeExpectedId = ReflectionUtil.getIdValue(node.getExpected());
            final Object expectedId = ReflectionUtil.getIdValue(getExpected());

            return (nodeActualId == actualId && nodeExpectedId == expectedId)
                    || (nodeActualId == expectedId && nodeExpectedId == actualId);
        }

        return (node.getActual() == getActual() && node.getExpected() == getExpected())
                || (node.getActual() == getExpected() && node.getExpected() == getActual());
    }
}
