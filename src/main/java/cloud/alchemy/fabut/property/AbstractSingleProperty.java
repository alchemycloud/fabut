package cloud.alchemy.fabut.property;

import java.util.Objects;

/**
 * Base implementation of a single property with a specific path.
 * <p>
 * This abstract class provides common functionality for property implementations,
 * including path management and basic equality operations. Subclasses should implement
 * the specific property behavior and provide a way to copy themselves.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @since 1.0
 */
public abstract class AbstractSingleProperty implements ISingleProperty {

    /** The path to this property within an object graph. */
    private String path;

    /**
     * Creates a new property with the specified path.
     *
     * @param path The property path
     * @throws NullPointerException if path is null
     * @throws IllegalArgumentException if path is empty
     */
    public AbstractSingleProperty(final String path) {
        setPath(path);
    }

    @Override
    public String getPath() {
        return path;
    }

    /**
     * Sets the path for this property.
     *
     * @param path The new property path
     * @throws NullPointerException if path is null
     * @throws IllegalArgumentException if path is empty
     */
    @Override
    public void setPath(final String path) {
        Objects.requireNonNull(path, "Property path cannot be null");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Property path cannot be empty");
        }
        this.path = path;
    }

    /**
     * Compares this property with another object for equality.
     * <p>
     * Two properties are considered equal if they have the same path (case-insensitive comparison).
     *
     * @param obj The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ISingleProperty)) {
            return false;
        }
        final ISingleProperty property = (ISingleProperty) obj;
        return path.equalsIgnoreCase(property.getPath());
    }
    
    /**
     * Returns the hash code of this property.
     * <p>
     * The hash code is based on the lowercase version of the property path.
     *
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(path.toLowerCase());
    }
    
    /**
     * Returns a string representation of this property.
     *
     * @return A string representation of this property
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
