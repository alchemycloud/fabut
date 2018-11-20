package cloud.alchemy.fabut.enums;

/**
 * Return result for node check.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public enum NodeCheckType {

    /** The contains pair. */
    CONTAINS_PAIR(true),

    /** The single node. */
    SINGLE_NODE(false),

    /** The new pair. */
    NEW_PAIR(true);

    /** The assert result. */
    private boolean assertResult;

    /**
     * Instantiates a new node check type.
     * 
     * @param assertResult
     *            the assert result
     */
    private NodeCheckType(final boolean assertResult) {
        this.assertResult = assertResult;
    }

    /**
     * Gets the assert value.
     * 
     * @return the assert value
     */
    public boolean getAssertValue() {
        return assertResult;
    }
}
