package cloud.alchemy.fabut.graph;

import cloud.alchemy.fabut.pair.Pair;

/**
 * Class representing object pair from {@link IsomorphicGraph}.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class IsomorphicNodePair extends Pair {

    /**
     * Default Isomorphic node pair constructor.
     * 
     * @param expected
     *            object
     * @param actual
     *            object
     */
    public IsomorphicNodePair(final Object expected, final Object actual) {
        super(expected, actual);
    }

    @Override
    public boolean equals(final Object arg0) {
        try {
            final IsomorphicNodePair node = (IsomorphicNodePair) arg0;
            return node.getActual() == getActual() && node.getExpected() == getExpected();
        } catch (final Exception e) {
            return false;
        }
    }

}
