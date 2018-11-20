package cloud.alchemy.fabut.property;

/**
 * Single property interface.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public interface ISingleProperty extends IProperty {

    /**
     * Get property path.
     * 
     * @return path.
     */
    String getPath();

    /**
     * Set property path.
     * 
     * @param path
     *            of property.
     */
    void setPath(String path);

    /**
     * Get copy of of property.
     * 
     * @return Property copy.
     */
    ISingleProperty getCopy();

}
