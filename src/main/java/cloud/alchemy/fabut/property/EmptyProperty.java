package cloud.alchemy.fabut.property;


/**
 * {@link AbstractSingleProperty} extension with limited checking is property equal with <code>null</code>.
 *
 * @author Andrej Miletic
 */
public class EmptyProperty extends AbstractSingleProperty {

    /**
     * Empty property property default constructor.
     *
     * @param path
     *            property path
     */
    public EmptyProperty(final String path) {
        super(path);
    }

    @Override
    public ISingleProperty getCopy() {
        return new EmptyProperty(getPath());
    }
}