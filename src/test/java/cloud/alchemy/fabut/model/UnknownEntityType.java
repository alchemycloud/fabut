package cloud.alchemy.fabut.model;

/**
 * The Class UnknownEntityType.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class UnknownEntityType extends Type {

    /** The id. */
    private int id;

    /**
     * Instantiates a new unknown entity type.
     */
    public UnknownEntityType() {

    }

    /**
     * Instantiates a new unknown entity type.
     * 
     * @param id
     *            the id
     */
    public UnknownEntityType(final int id) {
        this.id = id;
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
     * Sets the id.
     * 
     * @param id
     *            the new id
     */
    public void setId(final int id) {
        this.id = id;
    }

}
