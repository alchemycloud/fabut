package cloud.alchemy.fabut.enums;

/**
 * Return result for reference check.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public enum ReferenceCheckType {

    EQUAL_REFERENCE(true),

    EXCLUSIVE_NULL(false),

    NOT_NULL_PAIR(true);

    private boolean assertResult;

    /**
     * Default constructor.
     * 
     * @param assertResult
     */
    private ReferenceCheckType(final boolean assertResult) {
        this.assertResult = assertResult;
    }

    /**
     * @return the assertResult
     */
    public boolean isAssertResult() {
        return assertResult;
    }

}
