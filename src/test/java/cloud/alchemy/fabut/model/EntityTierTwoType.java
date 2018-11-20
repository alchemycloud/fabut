package cloud.alchemy.fabut.model;

/**
 * Enity type with one {@link EntityTierOneType} sub property and one {@link String} property.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class EntityTierTwoType extends TierOneType {

    /** The Constant ID. */
    public static final String ID = "id";

    /** The id. */
    private int id;

    /** The sub property. */
    private EntityTierOneType subProperty;

    /**
     * Instantiates a new entity tier two type.
     * 
     * @param property
     *            the property
     * @param id
     *            the id
     * @param subProperty
     *            the sub property
     */
    public EntityTierTwoType(final String property, final int id, final EntityTierOneType subProperty) {
        super(property);
        this.id = id;
        this.subProperty = subProperty;
    }

    /**
     * Instantiates a new entity tier two type.
     */
    public EntityTierTwoType() {

    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the sub property.
     * 
     * @return the sub property
     */
    public EntityTierOneType getSubProperty() {
        return subProperty;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the new id
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Sets the sub property.
     * 
     * @param subProperty
     *            the new sub property
     */
    public void setSubProperty(final EntityTierOneType subProperty) {
        this.subProperty = subProperty;
    }

}