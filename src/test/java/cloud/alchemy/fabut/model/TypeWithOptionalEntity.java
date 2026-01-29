package cloud.alchemy.fabut.model;

import java.util.Optional;

/**
 * Type with an Optional entity property for testing Optional entity formatting.
 */
public class TypeWithOptionalEntity {

    private Optional<EntityTierOneType> optionalEntity;

    public TypeWithOptionalEntity() {
        this.optionalEntity = Optional.empty();
    }

    public TypeWithOptionalEntity(Optional<EntityTierOneType> optionalEntity) {
        this.optionalEntity = optionalEntity;
    }

    public Optional<EntityTierOneType> getOptionalEntity() {
        return optionalEntity;
    }

    public void setOptionalEntity(Optional<EntityTierOneType> optionalEntity) {
        this.optionalEntity = optionalEntity;
    }
}
