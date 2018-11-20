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
     * @param property
     *            the property
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

}
