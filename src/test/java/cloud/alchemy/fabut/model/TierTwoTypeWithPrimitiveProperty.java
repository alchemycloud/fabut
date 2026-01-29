package cloud.alchemy.fabut.model;

/**
 * Tier two complex type with one primitive property.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class TierTwoTypeWithPrimitiveProperty extends TierTwoType {

    public static final String PROPERTY2 = "property2";

    /** The property2. */
    private final String property2;

    /**
     * Instantiates a new tier two type with primitive property.
     *
     * @param property the property
     * @param property2 the property2
     */
    public TierTwoTypeWithPrimitiveProperty(final TierOneType property, final String property2) {
        super(property);
        this.property2 = property2;
    }

    /**
     * Gets the property2.
     *
     * @return the property2
     */
    public String getProperty2() {
        return property2;
    }
    
    /**
     * Returns a string representation of this TierTwoTypeWithPrimitiveProperty instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "TierTwoTypeWithPrimitiveProperty{" +
               "property2='" + property2 + "'" +
               ", " + super.toString() +
               '}';
    }
}
