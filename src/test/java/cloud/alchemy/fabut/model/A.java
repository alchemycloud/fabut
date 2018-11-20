package cloud.alchemy.fabut.model;

/**
 * The Class A.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class A extends Type {

    /** The b. */
    private B b;

    /** The property. */
    private String property;

    /**
     * Instantiates a new a.
     */
    public A() {
    }

    /**
     * Instantiates a new a.
     * 
     * @param b
     *            the b
     */
    public A(final B b) {
        this.b = b;
        this.property = "ttt";
    }

    /**
     * Gets the b.
     * 
     * @return the b
     */
    public B getB() {
        return b;
    }

    /**
     * Sets the b.
     * 
     * @param b
     *            the new b
     */
    public void setB(final B b) {
        this.b = b;
    }

    /**
     * Gets the property.
     * 
     * @return the property
     */
    public String getProperty() {
        return property;
    }

    /**
     * Sets the property.
     */
    public void setProperty(final String property) {
        this.property = property;
    }
}
