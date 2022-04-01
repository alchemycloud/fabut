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
    public boolean equals(final Object arg0) {

        try {
            final IsomorphicNodePair node = (IsomorphicNodePair) arg0;
            if (ReflectionUtil.hasIdMethod(arg0)) {
                final Object nodeActualId = ReflectionUtil.getIdValue(node.getActual());
                final Object actualId = ReflectionUtil.getIdValue(getActual());
                final Object nodeExpectedId = ReflectionUtil.getIdValue(node.getExpected());
                final Object expectedId = ReflectionUtil.getIdValue(getExpected());

                return (nodeActualId == actualId && nodeExpectedId == expectedId) || (nodeActualId == expectedId && nodeExpectedId == actualId);

            } else {

                return (node.getActual() == getActual() && node.getExpected() == getExpected())
                        || (node.getActual() == getExpected() && node.getExpected() == getActual());
            }

        } catch (final Exception e) {
            return false;
        }
    }
}
