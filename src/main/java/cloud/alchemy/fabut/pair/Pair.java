package cloud.alchemy.fabut.pair;

/**
 * The Class Pair.
 * 
 */
public class Pair {

    /** The expected. */
    private final Object actual, expected;

    /**
     * Isomorphic node pair constructor with..
     * 
     * @param expected
     *            object
     * @param actual
     *            object
     */
    public Pair(final Object expected, final Object actual) {
        this.actual = actual;
        this.expected = expected;
    }

    /**
     * Get actual.
     * 
     * @return - actual object
     */
    public Object getActual() {
        return actual;
    }

    /**
     * Get expected.
     * 
     * @return - expected object
     */
    public Object getExpected() {
        return expected;
    }
}
