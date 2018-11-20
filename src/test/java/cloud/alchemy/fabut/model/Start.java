package cloud.alchemy.fabut.model;

import java.util.List;

/**
 * The Class Start.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class Start extends Type {

    /** The links. */
    private List<DoubleLink> links;

    /**
     * Instantiates a new start.
     * 
     * @param links
     *            the links
     */
    public Start(final List<DoubleLink> links) {
        this.links = links;
    }

    /**
     * Instantiates a new start.
     */
    public Start() {

    }

    /**
     * Gets the links.
     * 
     * @return the links
     */
    public List<DoubleLink> getLinks() {
        return links;
    }

    /**
     * Sets the links.
     * 
     * @param links
     *            the new links
     */
    public void setLinks(final List<DoubleLink> links) {
        this.links = links;
    }

}
