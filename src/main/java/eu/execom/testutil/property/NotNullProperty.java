package eu.execom.testutil.property;

/**
 * {@link Property} extension with limited checking is property equal with <code>null</code>.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class NotNullProperty extends Property {

    /**
     * Not null property default constructor.
     * 
     * @param path
     *            property path
     */
    protected NotNullProperty(final String path) {
        super(path);
    }

    @Override
    public ISingleProperty getCopy() {
        return new NotNullProperty(getPath());
    }
}
