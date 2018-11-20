/*
 * 
 */
package cloud.alchemy.fabut.model;

import cloud.alchemy.fabut.property.PropertyPath;

/**
 * Tier one entity type with id and one {@link String} property.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class EntityTierOneType extends TierOneType {

    /**
     * The Constant ID.
     */
    public static final PropertyPath<Integer> ID = new PropertyPath<Integer>("id");

    /**
     * The id.
     */
    private Integer id;

    /**
     * Instantiates a new entity tier one type.
     */
    public EntityTierOneType() {

    }

    /**
     * Instantiates a new entity tier one type.
     *
     * @param property the property
     * @param id       the id
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

    @Override
    public String toString() {
        return id + " " + getProperty();
    }
}
