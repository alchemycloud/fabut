package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.EntityTierOneType;
import cloud.alchemy.fabut.model.TrackedDto;
import cloud.alchemy.fabut.model.TrackedTuple;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FabutUsageDisabledTest extends Fabut {

    private final List<Object> entityTierOneTypes = new ArrayList<>();

    public FabutUsageDisabledTest() {
        entityTypes.add(EntityTierOneType.class);
        complexTypes.add(TrackedDto.class);
        trackedTypes.add(TrackedTuple.class);
        trackUsage = false;
    }

    @Override
    protected List<?> findAll(Class<?> entityClass) {
        if (entityClass == EntityTierOneType.class) {
            return entityTierOneTypes;
        }
        return List.of();
    }

    @Override
    protected Object findById(Class<?> entityClass, Object id) {
        if (entityClass == EntityTierOneType.class) {
            for (Object entity : entityTierOneTypes) {
                if (((EntityTierOneType) entity).getId().equals(id)) {
                    return entity;
                }
            }
        }
        return null;
    }

    @Test
    void trackUsageDisabled_noTrackerCreated() {
        assertNull(getUsageTracker());
    }

    @Test
    void trackUsageDisabled_snapshotStillWorks() {
        EntityTierOneType entity = new EntityTierOneType("test", 1);
        entityTierOneTypes.add(entity);

        takeSnapshot();

        entity.setProperty("updated");

        assertEntityWithSnapshot(entity, value("property", "updated"));
    }

    @Test
    void trackUsageDisabled_pauseTrackingNoError() {
        takeSnapshot();
        pauseTracking();
    }
}
