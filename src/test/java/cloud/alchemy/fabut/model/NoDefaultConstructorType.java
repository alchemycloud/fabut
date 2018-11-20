package cloud.alchemy.fabut.model;

/**
 * Class with no default constructor.
 */
public class NoDefaultConstructorType {

    /** The Constant PROPERTY. */
    public static final String PROPERTY = "property";

    /** The property. */
    private String property;

    /**
     * Instantiates a new {@link NoDefaultConstructorType}.
     * 
     * @param property
     *            the property
     */
    public NoDefaultConstructorType(final String property) {
        this.property = property;
    }

    /**
     * Gets the property.
     * 
     * @return the property
     */
    public String getProperty() {
        return property;
    }

    /**
     * Sets the property.
     * 
     * @param property
     *            the new property
     */
    public void setProperty(final String property) {
        this.property = property;
    }

}
