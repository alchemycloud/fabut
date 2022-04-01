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

    private final boolean assertResult;

    ReferenceCheckType(final boolean assertResult) {
        this.assertResult = assertResult;
    }

    public boolean isAssertResult() {
        return assertResult;
    }
}
