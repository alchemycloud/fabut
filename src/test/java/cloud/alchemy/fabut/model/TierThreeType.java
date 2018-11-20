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

    /** The Constant PROPERTY. */
    public static final String PROPERTY = "property";

    /** The property. */
    private final TierTwoType property;

    /**
     * Instantiates a new tier three type.
     * 
     * @param property
     *            the property
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

}
