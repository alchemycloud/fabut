package cloud.alchemy.fabut.enums;

/**
 * Represents the type of entity change detected during snapshot comparison.
 */
public enum EntityChangeType {
    /** Entity was created after the snapshot was taken */
    CREATED("CREATED", "Entity was created but not asserted"),

    /** Entity was deleted after the snapshot was taken */
    DELETED("DELETED", "Entity was deleted but not asserted as deleted");

    private final String label;
    private final String description;

    EntityChangeType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
