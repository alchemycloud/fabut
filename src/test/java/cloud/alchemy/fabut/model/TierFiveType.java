package cloud.alchemy.fabut.model;

/**
 * Tier five complex type.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class TierFiveType extends Type {

    /** The Constant PROPERTY. */
    public static final String PROPERTY = "property";

    /** The property. */
    private final TierFourType property;

    /**
     * Instantiates a new tier five type.
     *
     * @param property the property
     */
    public TierFiveType(final TierFourType property) {
        this.property = property;
    }

    /**
     * Gets the property.
     *
     * @return the property
     */
    public TierFourType getProperty() {
        return property;
    }
    
    /**
     * Returns a string representation of this TierFiveType instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "TierFiveType{" +
               "property=" + (property != null ? property : "null") +
               '}';
    }
}
