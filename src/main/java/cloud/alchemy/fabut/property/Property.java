package cloud.alchemy.fabut.property;

import java.util.Objects;

/**
 * A property that represents an expected value for a specific field or attribute.
 * <p>
 * This class extends {@link AbstractSingleProperty} to provide the ability to associate
 * a value with a property path. It's used to specify expectations about the value
 * of a particular property during testing.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @param <T> The type of the property value
 * @since 1.0
 */
public class Property<T> extends AbstractSingleProperty {

    /** The expected value for this property. */
    private final T value;

    /**
     * Creates a new property with the specified path and expected value.
     *
     * @param path The property path
     * @param value The expected value for this property
     * @throws NullPointerException if path is null
     * @throws IllegalArgumentException if path is empty
     */
    public Property(final String path, final T value) {
        super(path);
        this.value = value;
    }

    /**
     * Returns the expected value for this property.
     *
     * @return The expected value
     */
    public T getValue() {
        return value;
    }

    /**
     * Creates a deep copy of this property.
     *
     * @return A new property instance that is a copy of this property
     */
    @Override
    public ISingleProperty getCopy() {
        return new Property<>(getPath(), value);
    }
    
    /**
     * Returns a detailed description of this property including its path and value.
     *
     * @return A string description of this property
     */
    @Override
    public String getDescription() {
        return String.format("%s[path=%s, value=%s]", 
                getClass().getSimpleName(), 
                getPath(), 
                value == null ? "null" : value.toString());
    }
    
    /**
     * Compares this property with another object for equality.
     * <p>
     * Two properties are considered equal if they have the same path and the same value.
     *
     * @param obj The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Property)) {
            return false;
        }
        final Property<?> other = (Property<?>) obj;
        return Objects.equals(value, other.value);
    }
    
    /**
     * Returns the hash code of this property.
     *
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
