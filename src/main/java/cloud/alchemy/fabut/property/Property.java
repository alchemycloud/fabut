package cloud.alchemy.fabut.property;

/**
 * {@link AbstractSingleProperty} extension with focus on new value of the property..
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @param <T>
 *            property type
 */
public class Property<T> extends AbstractSingleProperty {

    private final T value;

    /**
     * Change property default constructor.
     * 
     * @param path
     *            property path
     * @param value
     *            expected property value
     */
    public Property(final String path, final T value) {
        super(path);
        this.value = value;
    }

    /**
     * Get expected value.
     * 
     * @return expected value.
     */
    public T getValue() {
        return value;
    }

    @Override
    public ISingleProperty getCopy() {
        return new Property<T>(getPath(), value);
    }

}
