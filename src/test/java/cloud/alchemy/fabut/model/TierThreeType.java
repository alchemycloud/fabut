package cloud.alchemy.fabut.model;

/**
 * Tier three complex type.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class TierThreeType extends Type {
    /** The property. */
    private final TierTwoType property;

    /**
     * Instantiates a new tier three type.
     *
     * @param property the property
     */
    public TierThreeType(final TierTwoType property) {
        this.property = property;
    }

    /**
     * Gets the property.
     *
     * @return the property
     */
    public TierTwoType getProperty() {
        return property;
    }
    
    /**
     * Returns a string representation of this TierThreeType instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "TierThreeType{" +
               "property=" + (property != null ? property : "null") +
               '}';
    }
}
