package cloud.alchemy.fabut.property;

/**
 * Marker interface for all property types in the Fabut framework.
 * <p>
 * This interface serves as the base for the property hierarchy and is intended to be
 * extended by more specific property interfaces and implementations. Properties in Fabut
 * are used to define assertions and expectations about object attributes during testing.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @since 1.0
 */
public interface IProperty {
    
    /**
     * Returns a human-readable description of this property.
     * <p>
     * This method should provide a clear, concise description of what the property
     * represents, which can be used in test reports and error messages.
     *
     * @return A string describing this property
     */
    default String getDescription() {
        return getClass().getSimpleName();
    }
}
