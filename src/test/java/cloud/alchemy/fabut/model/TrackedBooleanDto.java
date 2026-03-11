package cloud.alchemy.fabut.model;

/**
 * Test DTO with boolean field using 'is' prefix getter, for tracking coverage.
 */
public class TrackedBooleanDto {

    private boolean active;
    private String name;

    public TrackedBooleanDto() {}

    public TrackedBooleanDto(boolean active, String name) {
        this.active = active;
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
