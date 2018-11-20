package cloud.alchemy.fabut.property;


/**
 * {@link AbstractSingleProperty} extension with limited checking is property equal with <code>null</code>.
 *
 * @author Andrej Miletic
 */
public class NotEmptyProperty extends AbstractSingleProperty {

    /**
     * Not empty Property property default constructor.
     *
     * @param path
     *            property path
     */
    public NotEmptyProperty(final String path) {
        super(path);
    }

    @Override
    public ISingleProperty getCopy() {
        return new NotEmptyProperty(getPath());
    }
}