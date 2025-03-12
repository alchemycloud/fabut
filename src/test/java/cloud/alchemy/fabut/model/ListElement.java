package cloud.alchemy.fabut.model;

/**
 * List element class.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class ListElement {

    /** The list element. */
    private ListElement listElement;

    /**
     * Gets the list element.
     *
     * @return the list element
     */
    public ListElement getListElement() {
        return listElement;
    }

    /**
     * Sets the list element.
     *
     * @param listElement the new list element
     */
    public void setListElement(final ListElement listElement) {
        this.listElement = listElement;
    }
    
    /**
     * Returns a string representation of this ListElement instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "ListElement{" +
               "listElement=" + (listElement != null ? listElement : "null") +
               '}';
    }
}
