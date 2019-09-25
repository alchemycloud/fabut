package cloud.alchemy.fabut;

public class SnapshotAssert {
    private final Boolean result;
    private final Object entity;

    public SnapshotAssert(Boolean result, Object entity) {
        this.result = result;
        this.entity = entity;
    }

    public Boolean getResult() {
        return result;
    }

    public Object getEntity() {
        return entity;
    }

}
