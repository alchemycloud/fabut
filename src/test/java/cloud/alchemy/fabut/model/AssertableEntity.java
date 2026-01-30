package cloud.alchemy.fabut.model;

import cloud.alchemy.fabut.annotation.AssertGroup;
import cloud.alchemy.fabut.annotation.Assertable;

import java.util.Optional;

/**
 * Test entity annotated with @Assertable to test annotation processor.
 */
@Assertable(
    ignoredFields = {"version"},
    create = {
        @AssertGroup(name = "Key", fields = {"id", "name"}),
        @AssertGroup(name = "Stats", fields = {"count", "score"})
    },
    update = {
        @AssertGroup(name = "Name", fields = {"name"}),
        @AssertGroup(name = "Stats", fields = {"count", "score"})
    }
)
public class AssertableEntity {

    private Long id;
    private String name;
    private Integer count;
    private Optional<String> description;
    private Optional<Integer> score;
    private Long version;

    public AssertableEntity() {
        this.description = Optional.empty();
        this.score = Optional.empty();
    }

    public AssertableEntity(Long id, String name, Integer count, Optional<String> description, Optional<Integer> score) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.description = description;
        this.score = score;
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

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public void setDescription(Optional<String> description) {
        this.description = description;
    }

    public Optional<Integer> getScore() {
        return score;
    }

    public void setScore(Optional<Integer> score) {
        this.score = score;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
