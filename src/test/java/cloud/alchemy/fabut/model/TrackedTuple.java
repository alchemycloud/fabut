package cloud.alchemy.fabut.model;

/**
 * Test tuple with 2 fields for usage tracking tests.
 * Immutable, like real query projection tuples.
 */
public class TrackedTuple {

    private final Long entityId;
    private final String label;

    public TrackedTuple(Long entityId, String label) {
        this.entityId = entityId;
        this.label = label;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getLabel() {
        return label;
    }
}
