package cloud.alchemy.fabut.model;

import cloud.alchemy.fabut.property.PropertyPath;

import java.util.List;

/** Created by olahnikola on 8.6.17.. */
public class EntityWithList {

    public static final PropertyPath<Integer> ID = new PropertyPath<>("id");
    public static final PropertyPath<List<EntityTierOneType>> LIST = new PropertyPath<>("list");

    private Integer id;

    private List<EntityTierOneType> list;

    public EntityWithList() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<EntityTierOneType> getList() {
        return list;
    }

    public void setList(List<EntityTierOneType> list) {
        this.list = list;
    }

    /**
     * Returns a string representation of this EntityWithList instance.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "EntityWithList{" +
               "id=" + id +
               ", list=" + (list != null ? list : "null") +
               '}';
    }
}
