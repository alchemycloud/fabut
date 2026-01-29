package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.EntityTierOneType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Performance and stress tests for Fabut.
 * These tests verify that optimizations work correctly under load.
 */
public class FabutPerformanceTest extends Fabut {

    private static final String TEST = "test";
    private final List<EntityTierOneType> entities = new ArrayList<>();

    @Override
    public List<?> findAll(Class<?> clazz) {
        if (clazz == EntityTierOneType.class) {
            return entities;
        }
        return new ArrayList<>();
    }

    @Override
    public Object findById(Class<?> clazz, Object id) {
        if (clazz == EntityTierOneType.class) {
            return entities.stream()
                    .filter(e -> e.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @BeforeEach
    @Override
    public void before() {
        // Add entity type BEFORE calling super.before() so dbSnapshot gets populated
        entityTypes.add(EntityTierOneType.class);
        super.before();
        entities.clear();
    }

    // ==================== Snapshot Performance Tests ====================

    @Test
    void testSnapshotPerformanceWith100Entities() {
        // setup - create 100 entities
        for (int i = 0; i < 100; i++) {
            entities.add(new EntityTierOneType(TEST + i, i));
        }

        // method - time snapshot operation
        long startTime = System.nanoTime();
        takeSnapshot();
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        // assert - should complete in reasonable time
        assertTrue(durationMs < 1000, "Snapshot of 100 entities took too long: " + durationMs + "ms");
    }

    @Test
    void testSnapshotPerformanceWith500Entities() {
        // setup - create 500 entities (user's actual scenario)
        for (int i = 0; i < 500; i++) {
            entities.add(new EntityTierOneType(TEST + i, i));
        }

        // method - time snapshot operation
        long startTime = System.nanoTime();
        takeSnapshot();
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        // assert - should complete in reasonable time
        assertTrue(durationMs < 5000, "Snapshot of 500 entities took too long: " + durationMs + "ms");
    }

    @Test
    void testSnapshotAssertPerformanceWith100Entities() {
        // setup - create 100 entities and take snapshot
        for (int i = 0; i < 100; i++) {
            entities.add(new EntityTierOneType(TEST + i, i));
        }
        takeSnapshot();

        // Modify entities AFTER snapshot so there's a real change to assert
        for (EntityTierOneType entity : entities) {
            entity.setProperty(entity.getProperty() + "_modified");
        }

        // method - time assertion operation (assert all entities with a property change)
        long startTime = System.nanoTime();
        for (EntityTierOneType entity : entities) {
            // assertEntityWithSnapshot requires at least one changed property
            assertEntityWithSnapshot(entity, value("property", entity.getProperty()));
        }
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        // assert - should complete in reasonable time
        assertTrue(durationMs < 2000, "Assert of 100 entities took too long: " + durationMs + "ms");
    }

    // ==================== Type Checking Cache Performance ====================

    @Test
    void testTypeCheckingCachePerformance() {
        // setup
        complexTypes.add(String.class);

        // method - call isComplexType many times (should hit cache after first call)
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            // This internally calls isComplexType/isEntityType which should use cache
            assertObject("test" + i);
        }
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        // assert - 10000 assertions should be fast with caching
        assertTrue(durationMs < 5000, "10000 assertions took too long: " + durationMs + "ms");
    }

    // ==================== Reflection Cache Performance ====================

    @Test
    void testReflectionCachePerformance() {
        // method - multiple snapshot cycles to test caching
        long startTime = System.nanoTime();
        for (int cycle = 0; cycle < 5; cycle++) {
            // Reset before each cycle (before() already adds EntityTierOneType)
            before();

            // Create entities fresh for each cycle
            for (int i = 0; i < 100; i++) {
                entities.add(new EntityTierOneType(TEST + i, i));
            }

            takeSnapshot();

            // Modify entities after snapshot so there's a real change to assert
            for (int i = 0; i < 10; i++) {
                entities.get(i).setProperty(entities.get(i).getProperty() + "_modified");
            }

            // Assert some entities with the modified property
            for (int i = 0; i < 10; i++) {
                EntityTierOneType entity = entities.get(i);
                assertEntityWithSnapshot(entity, value("property", entity.getProperty()));
            }
        }
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        // assert - repeated operations should be fast due to caching
        assertTrue(durationMs < 10000, "5 cycles took too long: " + durationMs + "ms");
    }

    // ==================== Parallel Processing Test ====================

    @Test
    void testParallelSnapshotProcessing() {
        // setup - create enough entities to trigger parallel processing (>50)
        for (int i = 0; i < 200; i++) {
            entities.add(new EntityTierOneType(TEST + i, i));
        }

        // method - take snapshot (should use parallel processing)
        long startTime = System.nanoTime();
        takeSnapshot();
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        // assert - parallel processing should be efficient
        assertTrue(durationMs < 3000, "Parallel snapshot took too long: " + durationMs + "ms");
    }

    // ==================== Complex Object Graph Test ====================

    @Test
    void testComplexObjectCopyPerformance() {
        // setup - create entities with nested complex objects
        complexTypes.add(EntityTierOneType.class);

        for (int i = 0; i < 50; i++) {
            entities.add(new EntityTierOneType(TEST + i, i));
        }

        // method - snapshot creates deep copies
        long startTime = System.nanoTime();
        takeSnapshot();
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        // assert
        assertTrue(durationMs < 2000, "Complex object copy took too long: " + durationMs + "ms");
    }

    // ==================== Memory Stress Test ====================

    @Test
    void testMemoryEfficiencyWithLargeDataset() {
        // setup - create many entities to test memory efficiency
        for (int i = 0; i < 1000; i++) {
            entities.add(new EntityTierOneType(TEST + i, i));
        }

        // Record memory before
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        // method
        takeSnapshot();

        // Record memory after
        runtime.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        long memoryUsedMB = (memoryAfter - memoryBefore) / (1024 * 1024);

        // assert - should not use excessive memory (< 100MB for 1000 simple entities)
        assertTrue(memoryUsedMB < 100, "Snapshot used too much memory: " + memoryUsedMB + "MB");
    }
}
