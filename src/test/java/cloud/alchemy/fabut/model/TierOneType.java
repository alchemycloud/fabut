package cloud.alchemy.fabut.model;

import cloud.alchemy.fabut.property.PropertyPath;

/**
 * Tier one complex type with only one {@link String} property.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class TierOneType extends Type {

    /** The property. */
    public static PropertyPath<String> PROPERTY = new PropertyPath<>("property");

    /** The property. */
    private String property;

    /**
     * Instantiates a new tier one type.
     */
    public TierOneType() {

    }

    /**
     * Instantiates a new tier one type.
     * 
     * @param property
     *            the property
     */
    public TierOneType(final String property) {
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

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return property;
    }

}
