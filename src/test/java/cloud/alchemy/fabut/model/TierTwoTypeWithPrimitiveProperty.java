package cloud.alchemy.fabut.model;

import cloud.alchemy.fabut.property.PropertyPath;

/**
 * Tier two complex type with one primitive property.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class TierTwoTypeWithPrimitiveProperty extends TierTwoType {

    /**
     * The Constant PROPERTY2.
     */
    public static final PropertyPath<String> PROPERTY2 = new PropertyPath<>("property2");

    /**
     * The property2.
     */
    private final String property2;

    /**
     * Instantiates a new tier two type with primitive property.
     *
     * @param property  the property
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

}
