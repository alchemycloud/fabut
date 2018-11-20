package cloud.alchemy.fabut.property;

import java.util.List;

/**
 * Class that contains a collection of single properties.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class MultiProperties implements IMultiProperties {

    private final List<ISingleProperty> propertiesDefinitions;

    public MultiProperties(final List<ISingleProperty> properies) {
        propertiesDefinitions = properies;
    }

    @Override
    public List<ISingleProperty> getProperties() {
        return propertiesDefinitions;
    }

}