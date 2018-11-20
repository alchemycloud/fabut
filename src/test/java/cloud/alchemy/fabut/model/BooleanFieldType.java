package cloud.alchemy.fabut.model;

/**
 * The Class BooleanFieldType.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class BooleanFieldType extends Type {

    /** The Constant PROPERTY. */
    public static final String PROPERTY = "property";

    /** The property. */
    private final boolean property;

    /**
     * Instantiates a new boolean field type.
     * 
     * @param property
     *            the property
     */
    public BooleanFieldType(final boolean property) {
        this.property = property;
    }

    /**
     * Checks if is property.
     * 
     * @return true, if is property
     */
    public boolean isProperty() {
        return property;
    }

}
