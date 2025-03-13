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
    public static final PropertyPath<String> PROPERTY = new PropertyPath<>("property");

    /** The property. */
    private String property;

    /** Instantiates a new tier one type. */
    public TierOneType() {}

    /**
     * Instantiates a new tier one type.
     *
     * @param property the property
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
     * @param property the new property
     */
    public void setProperty(final String property) {
        this.property = property;
    }

    /**
     * Returns a string representation of this TierOneType instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        // Check for circular references
        if (isCircularReference(this)) {
            return getClass().getSimpleName() + "{...circular reference...}";
        }
        
        // Check cache for existing representation
        String cached = getCachedToString(this);
        if (cached != null) {
            return cached;
        }
        
        try {
            startRendering(this);
            String result = getClass().getSimpleName() + "{" +
                   "property=" + formatString(property) +
                   '}';
            cacheToString(this, result);
            return result;
        } finally {
            finishRendering(this);
        }
    }
}
