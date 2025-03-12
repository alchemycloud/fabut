package cloud.alchemy.fabut.model;

/**
 * The Class C.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class C {

    /** The a. */
    private A a;

    /** Instantiates a new c. */
    public C() {}

    /**
     * Instantiates a new c.
     *
     * @param a the a
     */
    public C(final A a) {
        this.a = a;
    }

    /**
     * Gets the a.
     *
     * @return the a
     */
    public A getA() {
        return a;
    }

    /**
     * Sets the a.
     *
     * @param a the new a
     */
    public void setA(final A a) {
        this.a = a;
    }
    
    /**
     * Returns a string representation of this C instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "C{" +
               "a=" + (a != null ? a : "null") +
               '}';
    }
}
