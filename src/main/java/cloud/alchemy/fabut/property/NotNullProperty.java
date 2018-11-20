package cloud.alchemy.fabut.property;

/**
 * {@link AbstractSingleProperty} extension with limited checking is property equal with <code>null</code>.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class NotNullProperty extends AbstractSingleProperty {

    /**
     * Not null property default constructor.
     * 
     * @param path
     *            property path
     */
    public NotNullProperty(final String path) {
        super(path);
    }

    @Override
    public ISingleProperty getCopy() {
        return new NotNullProperty(getPath());
    }
}
