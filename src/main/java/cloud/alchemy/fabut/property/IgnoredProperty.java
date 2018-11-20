package cloud.alchemy.fabut.property;

/**
 * The Class IgnoreProperty. {@link AbstractSingleProperty} extension with limited functionality only to mark property
 * as ignored for testing.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class IgnoredProperty extends AbstractSingleProperty {

    /**
     * Ignore property default constructor.
     * 
     * @param path
     *            property path
     */
    public IgnoredProperty(final String path) {
        super(path);
    }

    @Override
    public ISingleProperty getCopy() {
        return new IgnoredProperty(getPath());
    }

}
