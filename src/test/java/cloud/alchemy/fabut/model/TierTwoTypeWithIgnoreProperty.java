package cloud.alchemy.fabut.model;

import cloud.alchemy.fabut.property.PropertyPath;

/**
 * Type with one ignored type property.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class TierTwoTypeWithIgnoreProperty extends Type {

    /** The Constant IGNORED_TYPE. */
    public static final PropertyPath<IgnoredType> IGNORED_TYPE = new PropertyPath<>("ignoredType");

    /** The ignored type. */
    private final IgnoredType ignoredType;

    /**
     * Instantiates a new tier two type with ignore property.
     *
     * @param ignoredType the ignored type
     */
    public TierTwoTypeWithIgnoreProperty(final IgnoredType ignoredType) {
        this.ignoredType = ignoredType;
    }

    /**
     * Gets the ignored type.
     *
     * @return the ignored type
     */
    public IgnoredType getIgnoredType() {
        return ignoredType;
    }
    
    /**
     * Returns a string representation of this TierTwoTypeWithIgnoreProperty instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "TierTwoTypeWithIgnoreProperty{" +
               "ignoredType=" + (ignoredType != null ? ignoredType : "null") +
               '}';
    }
}
