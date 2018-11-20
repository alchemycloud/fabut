package cloud.alchemy.fabut.pair;

import cloud.alchemy.fabut.enums.AssertableType;

/**
 * Represents expected and actual pair during asserting with information of what type is the pair and if expected/actual are
 * fields of some already asserted object.
 */
public class AssertPair extends Pair {

    private AssertableType assertableType;
    private boolean property;

    public AssertPair(final Object expected, final Object actual, final AssertableType objectType) {
        super(expected, actual);
        assertableType = objectType;
        property = false;
    }

    public AssertPair(final Object expected, final Object actual, final AssertableType assertableType,
            final boolean property) {
        super(expected, actual);
        this.assertableType = assertableType;
        this.property = property;
    }

    public AssertableType getObjectType() {
        return assertableType;
    }

    public void setObjectType(final AssertableType objectType) {
        assertableType = objectType;
    }

    public boolean isProperty() {
        return property;
    }

    public void setProperty(final boolean property) {
        this.property = property;
    }

}
