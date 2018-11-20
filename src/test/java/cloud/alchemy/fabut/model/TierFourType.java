package cloud.alchemy.fabut.model;

/**
 * Tier four complex type.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class TierFourType extends Type {

    /** The Constant PROPERTY. */
    public static final String PROPERTY = "property";

    /** The property. */
    private final TierThreeType property;

    /**
     * Instantiates a new tier four type.
     * 
     * @param property
     *            the property
     */
    public TierFourType(final TierThreeType property) {
        this.property = property;
    }

    /**
     * Gets the property.
     * 
     * @return the property
     */
    public TierThreeType getProperty() {
        return property;
    }

}
