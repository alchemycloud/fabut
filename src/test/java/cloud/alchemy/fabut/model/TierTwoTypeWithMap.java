package cloud.alchemy.fabut.model;

import java.util.Map;

public class TierTwoTypeWithMap {

    private String property;

    private Map<Integer, TierOneType> map;

    public TierTwoTypeWithMap() {

    }

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

}