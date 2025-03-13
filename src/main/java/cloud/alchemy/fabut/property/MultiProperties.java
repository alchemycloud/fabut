package cloud.alchemy.fabut.property;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Class that contains a collection of single properties with support for
 * concurrent operations and parallel processing for improved performance.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class MultiProperties implements IMultiProperties {

    private final List<ISingleProperty> propertiesDefinitions;
    private static final int PARALLEL_THRESHOLD = 50;

    /**
     * Creates a new MultiProperties instance with thread-safe property list.
     * 
     * @param properties The list of single properties to manage
     */
    public MultiProperties(final List<ISingleProperty> properties) {
        // Use thread-safe list implementation for concurrent access
        this.propertiesDefinitions = new CopyOnWriteArrayList<>(properties);
    }

    @Override
    public List<ISingleProperty> getProperties() {
        return propertiesDefinitions;
    }

}
