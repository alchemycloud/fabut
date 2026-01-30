package cloud.alchemy.fabut.property;

import java.util.Objects;

/**
 * Represents a hierarchical property path that can be used to navigate through object properties.
 * <p>
 * This class is used to construct property paths like "person.address.street" by chaining
 * individual property paths together. The generic type parameter allows for type safety when
 * constructing property paths.
 *
 * @param <T> The type of the property at the end of this path
 * @since 1.0
 * @deprecated Use String-based API instead. This class is kept for backward compatibility with v4.x.
 *             Example: Replace {@code value(Entity.NAME, "test")} with {@code value("name", "test")}
 */
@Deprecated
public class PropertyPath<T> {

    /** Separator used for path elements. */
    private static final String DOT = ".";

    /** The string representation of the path. */
    private final String path;

    /**
     * Creates a new property path with the specified path string.
     *
     * @param path The property path as a string
     * @throws NullPointerException if path is null
     */
    public PropertyPath(String path) {
        this.path = Objects.requireNonNull(path, "Property path cannot be null");
    }

    /**
     * Returns the string representation of this property path.
     *
     * @return The property path as a string
     */
    public String getPath() {
        return path;
    }

    /**
     * Chains this property path with another property path, creating a new combined path.
     * <p>
     * For example, chaining "person" with "address" results in "person.address".
     *
     * @param <S> The type of the property at the end of the added path
     * @param addPath The property path to append to this path
     * @return A new property path representing the combined path
     * @throws NullPointerException if addPath is null
     */
    public <S> PropertyPath<S> chain(PropertyPath<S> addPath) {
        Objects.requireNonNull(addPath, "Path to chain cannot be null");
        return new PropertyPath<>(path + DOT + addPath.getPath());
    }

    /**
     * Creates a new property path by appending a property name to this path.
     * <p>
     * For example, appending "street" to "person.address" results in "person.address.street".
     *
     * @param <S> The type of the property being added
     * @param propertyName The name of the property to append
     * @return A new property path with the appended property
     * @throws NullPointerException if propertyName is null
     * @throws IllegalArgumentException if propertyName is empty
     */
    public <S> PropertyPath<S> append(String propertyName) {
        Objects.requireNonNull(propertyName, "Property name cannot be null");
        if (propertyName.isEmpty()) {
            throw new IllegalArgumentException("Property name cannot be empty");
        }
        return new PropertyPath<>(path + DOT + propertyName);
    }

    /**
     * Returns a string representation of this property path.
     *
     * @return The string representation of this property path
     */
    @Override
    public String toString() {
        return path;
    }

    /**
     * Checks if this property path equals another object.
     *
     * @param obj The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PropertyPath<?> other = (PropertyPath<?>) obj;
        return Objects.equals(path, other.path);
    }

    /**
     * Returns the hash code of this property path.
     *
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
