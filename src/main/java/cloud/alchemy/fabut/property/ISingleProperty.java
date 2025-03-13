package cloud.alchemy.fabut.property;

/**
 * Interface representing a single property with a specific path.
 * <p>
 * A single property represents a specific attribute or field of an object that can be
 * tested or manipulated within the Fabut framework. Each property has a path that
 * identifies its location within an object graph.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @since 1.0
 */
public interface ISingleProperty extends IProperty {

    /**
     * Returns the path to this property within an object graph.
     * <p>
     * The path is typically in the format "object.field.subfield" to represent
     * nested properties in the object hierarchy.
     *
     * @return The property path as a string
     */
    String getPath();

    /**
     * Sets the path for this property.
     * <p>
     * This method allows modifying the property path after creation, which is useful
     * when building property paths dynamically or when copying properties.
     *
     * @param path The new property path
     * @throws NullPointerException if path is null
     * @throws IllegalArgumentException if path is empty
     */
    void setPath(String path);

    /**
     * Creates a deep copy of this property.
     * <p>
     * The copy should be a new instance with the same path and behavior as this property.
     * This is useful for creating modified versions of properties without affecting the original.
     *
     * @return A new property instance that is a copy of this property
     */
    ISingleProperty getCopy();
    
    /**
     * Returns a string representation of this property including its path.
     *
     * @return A string representation of this property
     */
    @Override
    default String getDescription() {
        return getClass().getSimpleName() + "[path=" + getPath() + "]"; 
    }
}
