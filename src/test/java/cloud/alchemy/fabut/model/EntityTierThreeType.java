package cloud.alchemy.fabut.model;

/**
 * The Class EntityTierThreeType.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class EntityTierThreeType extends EntityTierTwoType {

    /**
     * Instantiates a new entity tier three type.
     */
    public EntityTierThreeType() {

    }

    /**
     * Instantiates a new entity tier three type.
     * 
     * @param property
     *            the property
     * @param id
     *            the id
     * @param subProperty
     *            the sub property
     */
    public EntityTierThreeType(final String property, final int id, final EntityTierOneType subProperty) {
        super(property, id, subProperty);
    }

}
