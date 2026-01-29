package cloud.alchemy.fabut.model;

import java.util.Optional;

/**
 * Test type with both Optional and non-Optional fields for testing reflection-based field detection.
 */
public class TypeWithMixedFields {
    private String name;
    private Integer count;
    private Optional<String> description;
    private Optional<Integer> score;

    public TypeWithMixedFields() {
        this.description = Optional.empty();
        this.score = Optional.empty();
    }

    public TypeWithMixedFields(String name, Integer count, Optional<String> description, Optional<Integer> score) {
        this.name = name;
        this.count = count;
        this.description = description;
        this.score = score;
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
}
