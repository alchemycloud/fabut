/*
 *
 */
package cloud.alchemy.fabut.model;

/**
 * Tier one entity type with id and one {@link String} property.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class EntityTierOneType extends TierOneType {
    /** The id. */
    private Integer id;

    /** Instantiates a new entity tier one type. */
    public EntityTierOneType() {}

    /**
     * Instantiates a new entity tier one type.
     *
     * @param property the property
     * @param id the id
     */
    public EntityTierOneType(final String property, final Integer id) {
        super(property);
        this.id = id;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Returns a string representation of this EntityTierOneType instance.
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
                   "id=" + id +
                   ", property=" + formatString(getProperty()) +
                   '}';
            cacheToString(this, result);
            return result;
        } finally {
            finishRendering(this);
        }
    }
    
    /**
     * Returns a string representation optimized for test assertions.
     * This format is more concise and focuses on the values being tested.
     *
     * @return a string representation optimized for test assertions
     */
    @Override
    public String toTestString() {
        return "EntityTierOneType[id=" + id + ", property=" + getProperty() + "]";
    }
}
