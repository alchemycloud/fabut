package cloud.alchemy.fabut.model;

/**
 * Type who's get methods are ignored.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class IgnoredMethodsType extends Type {

    /** The Constant PROPERTY. */
    public static final String PROPERTY = "ignoreMethodsProperty";

    /** The ignore methods property. */
    private final String ignoreMethodsProperty;

    /**
     * Instantiates a new ignored methods type.
     * 
     * @param ignoreMethodsProperty
     *            the ignore methods property
     */
    public IgnoredMethodsType(final String ignoreMethodsProperty) {
        this.ignoreMethodsProperty = ignoreMethodsProperty;
    }

    /**
     * Gets the ignore methods property.
     * 
     * @return the ignore methods property
     */
    public String getIgnoreMethodsProperty() {
        return ignoreMethodsProperty;
    }

}
