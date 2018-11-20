package cloud.alchemy.fabut.property;

/**
 * {@link AbstractSingleProperty} extension with limited checking is property only different then null.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class NullProperty extends AbstractSingleProperty {

    /**
     * Null property default constructor.
     * 
     * @param path
     *            property path
     */
    public NullProperty(final String path) {
        super(path);
    }

    @Override
    public ISingleProperty getCopy() {
        return new NullProperty(getPath());
    }
}
