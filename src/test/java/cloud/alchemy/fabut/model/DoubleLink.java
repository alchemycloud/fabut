package cloud.alchemy.fabut.model;

import java.util.List;

/**
 * The Class DoubleLink.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class DoubleLink extends Type {

    /** The as. */
    private List<A> as;

    /** The start. */
    private Start start;

    /** The property. */
    private String property;

    /**
     * Instantiates a new double link.
     * 
     * @param as
     *            the as
     * @param start
     *            the start
     * @param property
     *            the property
     */
    public DoubleLink(final List<A> as, final Start start, final String property) {
        this.as = as;
        this.start = start;
        this.property = property;
    }

    /**
     * Instantiates a new double link.
     */
    public DoubleLink() {

    }

    /**
     * Gets the as.
     * 
     * @return the as
     */
    public List<A> getAs() {
        return as;
    }

    /**
     * Gets the start.
     * 
     * @return the start
     */
    public Start getStart() {
        return start;
    }

    /**
     * Sets the as.
     * 
     * @param as
     *            the new as
     */
    public void setAs(final List<A> as) {
        this.as = as;
    }

    /**
     * Sets the start.
     * 
     * @param start
     *            the new start
     */
    public void setStart(final Start start) {
        this.start = start;
    }

    /**
     * Gets the property.
     * 
     * @return the property
     */
    public String getProperty() {
        return property;
    }

}
