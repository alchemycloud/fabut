package cloud.alchemy.fabut.model;

/**
 * Tier six complex type.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class TierSixType extends Type {
    /** The property. */
    private final TierFiveType property;

    /**
     * Instantiates a new tier six type.
     *
     * @param property the property
     */
    public TierSixType(final TierFiveType property) {
        this.property = property;
    }

    /**
     * Gets the property.
     *
     * @return the property
     */
    public TierFiveType getProperty() {
        return property;
    }
    
    /**
     * Returns a string representation of this TierSixType instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "TierSixType{" +
               "property=" + (property != null ? property : "null") +
               '}';
    }
}
