package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.EntityTierOneType;
import cloud.alchemy.fabut.model.TrackedDto;
import cloud.alchemy.fabut.model.TrackedTuple;
import cloud.alchemy.fabut.tracking.UsageTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FabutUsageThresholdTest extends Fabut {

    private final List<Object> entityTierOneTypes = new ArrayList<>();
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream capturedOutput;

    public FabutUsageThresholdTest() {
        entityTypes.add(EntityTierOneType.class);
        complexTypes.add(TrackedDto.class);
        trackedTypes.add(TrackedTuple.class);
        usageThreshold = 50; // Fail if any class avg usage < 50%
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

    @BeforeEach
    @Override
    public void before() {
        super.before();
        capturedOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOutput));
    }

    @AfterEach
    @Override
    public void after() {
        // Don't call super.after() — tests manually control it
        System.setOut(originalOut);
        getUsageTracker().deactivate();
        UsageTracker.removeCurrent();
    }

    @Test
    void threshold_passesWhenAbove() {
        takeSnapshot();

        // Use 3 of 4 fields = 75% > 50% threshold
        TrackedDto dto = new TrackedDto(1L, "test", "desc", 5);
        dto.getId();
        dto.getName();
        dto.getDescription();

        // Should not throw
        assertDoesNotThrow(() -> callSuperAfter());
    }

    @Test
    void threshold_passesWhenFullyUsed() {
        takeSnapshot();

        TrackedTuple tuple = new TrackedTuple(1L, "label");
        tuple.getEntityId();
        tuple.getLabel();

        assertDoesNotThrow(() -> callSuperAfter());
    }

    @Test
    void threshold_failsWhenBelow() {
        takeSnapshot();

        // Use 1 of 4 fields = 25% < 50% threshold
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        dto.getId();

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> callSuperAfter());
        assertTrue(error.getMessage().contains("USAGE THRESHOLD VIOLATION"));
        assertTrue(error.getMessage().contains("TrackedDto"));
        assertTrue(error.getMessage().contains("25%"));
    }

    @Test
    void threshold_failsWhenNeverAccessed() {
        takeSnapshot();

        // 0 of 4 fields = 0% < 50% threshold
        new TrackedDto(1L, "unused", null, 0);

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> callSuperAfter());
        assertTrue(error.getMessage().contains("USAGE THRESHOLD VIOLATION"));
        assertTrue(error.getMessage().contains("0%"));
    }

    @Test
    void threshold_reportsMultipleViolations() {
        takeSnapshot();

        // TrackedDto: 1/4 = 25%
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        dto.getId();

        // TrackedTuple: 0/2 = 0%
        new TrackedTuple(2L, "label");

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> callSuperAfter());
        String msg = error.getMessage();
        assertTrue(msg.contains("TrackedDto"));
        assertTrue(msg.contains("TrackedTuple"));
    }

    @Test
    void threshold_includesUnusedFieldsInMessage() {
        takeSnapshot();

        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        dto.getId();

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> callSuperAfter());
        assertTrue(error.getMessage().contains("unused:"));
    }

    @Test
    void threshold_noFailWhenNoTrackedObjects() {
        takeSnapshot();

        // No objects created — nothing to check
        assertDoesNotThrow(() -> callSuperAfter());
    }

    @Test
    void threshold_noFailWhenNoSnapshot() {
        // No takeSnapshot() — tracking not active
        new TrackedDto(1L, "test", null, 5);

        assertDoesNotThrow(() -> callSuperAfter());
    }

    @Test
    void threshold_instanceCountInMessage() {
        takeSnapshot();

        new TrackedDto(1L, "a", null, 1);
        new TrackedDto(2L, "b", null, 2);
        TrackedDto c = new TrackedDto(3L, "c", null, 3);
        c.getId();

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> callSuperAfter());
        assertTrue(error.getMessage().contains("3 instances"));
    }

    @Test
    void threshold_singleInstanceMessage() {
        takeSnapshot();

        new TrackedDto(1L, "a", null, 1);

        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> callSuperAfter());
        assertTrue(error.getMessage().contains("1 instance)"));
    }

    @Test
    void threshold_exactlyAtThreshold_passes() {
        takeSnapshot();

        // Use 2 of 4 fields = 50% == 50% threshold (not below)
        TrackedDto dto = new TrackedDto(1L, "test", null, 5);
        dto.getId();
        dto.getName();

        assertDoesNotThrow(() -> callSuperAfter());
    }

    private void callSuperAfter() {
        // Temporarily restore System.out for the report output
        super.after();
    }
}
