package cloud.alchemy.fabut.graph;

import cloud.alchemy.fabut.pair.Pair;
import cloud.alchemy.fabut.ReflectionUtil;

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
     * @param actual   object
     */
    public IsomorphicNodePair(final Object expected, final Object actual) {
        super(expected, actual);
    }

    @Override
    public boolean equals(final Object arg0) {

        try {
            final IsomorphicNodePair node = (IsomorphicNodePair) arg0;
            if (ReflectionUtil.hasIdMethod(arg0)) {
                return ReflectionUtil.getIdValue(node.getActual()) == ReflectionUtil.getIdValue(getActual()) &&
                        ReflectionUtil.getIdValue(node.getExpected()) == ReflectionUtil.getIdValue(getExpected());
            } else {
                return node.getActual() == getActual() && node.getExpected() == getExpected();
            }

        } catch (final Exception e) {
            return false;
        }
    }

}
