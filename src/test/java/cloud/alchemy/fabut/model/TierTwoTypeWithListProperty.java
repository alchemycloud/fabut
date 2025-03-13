package cloud.alchemy.fabut.model;

import cloud.alchemy.fabut.property.PropertyPath;

import java.util.List;

/**
 * Tier two type with list as property.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class TierTwoTypeWithListProperty extends Type {

    /** The Constant PROPERTY. */
    public static final PropertyPath<List<String>> PROPERTY = new PropertyPath<>("property");

    /** The property. */
    private final List<String> property;

    /**
     * Instantiates a new tier two type with list property.
     *
     * @param property the property
     */
    public TierTwoTypeWithListProperty(final List<String> property) {
        this.property = property;
    }

    /**
     * Gets the property.
     *
     * @return the property
     */
    public List<String> getProperty() {
        return property;
    }
    
    /**
     * Returns a string representation of this TierTwoTypeWithListProperty instance.
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
            String propertyStr = property != null ? property.toString() : "null";
            
            // Format large collections
            if (property != null && property.size() > MAX_COLLECTION_SIZE) {
                propertyStr = formatCollection(propertyStr, property.size());
            }
            
            String result = getClass().getSimpleName() + "{" +
                   "property=" + propertyStr +
                   '}';
            cacheToString(this, result);
            return result;
        } finally {
            finishRendering(this);
        }
    }
}
