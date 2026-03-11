package cloud.alchemy.fabut.model;

import java.util.Optional;

/**
 * Test DTO with multiple fields for usage tracking tests.
 */
public class TrackedDto {

    private Long id;
    private String name;
    private Optional<String> description;
    private Integer count;

    public TrackedDto() {}

    public TrackedDto(Long id, String name, String description, Integer count) {
        this.id = id;
        this.name = name;
        this.description = Optional.ofNullable(description);
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public void setDescription(Optional<String> description) {
        this.description = description;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
