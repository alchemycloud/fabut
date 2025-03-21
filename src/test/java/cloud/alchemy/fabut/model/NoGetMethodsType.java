package cloud.alchemy.fabut.model;

import cloud.alchemy.fabut.property.PropertyPath;

/**
 * No conventional get methods type.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class NoGetMethodsType extends Type {

    /** The Constant PROPERTY. */
    public static final PropertyPath<String> PROPERTY = new PropertyPath<>("property");

    /** The property. */
    private final String property;

    /** The not boolean property. */
    private final String notBooleanProperty;

    /**
     * Instantiates a new no get methods type.
     *
     * @param property the property
     */
    public NoGetMethodsType(final String property) {
        this.property = property;
        this.notBooleanProperty = property;
    }

    public NoGetMethodsType() {
        property = PROPERTY.getPath();
        notBooleanProperty = PROPERTY.getPath();
    }

    /**
     * Property.
     *
     * @return the string
     */
    public String property() {
        return property;
    }

    /**
     * Fake get method.
     *
     * @return {@link String}
     */
    public String getString() {
        return "String";
    }

    /**
     * Checks if is not boolean property.
     *
     * @return the string
     */
    public String isNotBooleanProperty() {
        return notBooleanProperty;
    }

    /**
     * Checks if is field.
     *
     * @return the string
     */
    public String isField() {
        return "not";
    }
    
    /**
     * Returns a string representation of this NoGetMethodsType instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "NoGetMethodsType{" +
               "property='" + property + "'" +
               ", notBooleanProperty='" + notBooleanProperty + "'" +
               ", " + super.toString() +
               '}';
    }
}
