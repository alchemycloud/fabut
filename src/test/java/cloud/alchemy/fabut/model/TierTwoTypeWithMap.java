package cloud.alchemy.fabut.model;

import java.util.Map;

public class TierTwoTypeWithMap {

    private String property;

    private Map<Integer, TierOneType> map;

    public TierTwoTypeWithMap() {}

    public String getProperty() {
        return property;
    }

    public void setProperty(final String property) {
        this.property = property;
    }

    public Map<Integer, TierOneType> getMap() {
        return map;
    }

    public void setMap(final Map<Integer, TierOneType> map) {
        this.map = map;
    }
    
    /**
     * Returns a string representation of this TierTwoTypeWithMap instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "TierTwoTypeWithMap{" +
               "property='" + property + "'" +
               ", map=" + (map != null ? map : "null") +
               '}';
    }
}
