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

    /** The Constant PROPERTY. */
    public static final String PROPERTY = "property";

    /** The property. */
    private final TierFiveType property;

    /**
     * Instantiates a new tier six type.
     * 
     * @param property
     *            the property
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

}
