package cloud.alchemy.fabut.property;

/**
 * Abstract property definition that contains path to some property that is used.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public abstract class AbstractSingleProperty implements ISingleProperty {

    private String path;

    /**
     * Default constructor.
     * 
     * @param path
     *            property path
     */
    public AbstractSingleProperty(final String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    /**
     * Set property path.
     * 
     * @param path
     *            new path.
     */
    @Override
    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public boolean equals(final Object obj) {
        final ISingleProperty property = (ISingleProperty) obj;
        return path.equalsIgnoreCase(property.getPath());
    }

}
