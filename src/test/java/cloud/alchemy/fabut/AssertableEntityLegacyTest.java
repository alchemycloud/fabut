package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.AssertableEntity;
import cloud.alchemy.fabut.property.ISingleProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AssertableEntity using the v4 legacy API (manual property assertions).
 *
 * This test class mirrors {@link GeneratedAssertBuilderTest} but uses the old-style
 * manual API with value(), isNull(), notNull(), ignored() methods.
 *
 * Both test suites should pass - ensuring backward compatibility.
 */
public class AssertableEntityLegacyTest extends AbstractFabutTest {

    @BeforeEach
    public void setUpTest() {
        if (!complexTypes.isEmpty()) return;
        complexTypes.add(AssertableEntity.class);
    }

    // ==================== Basic Value Assertions ====================

    @Test
    public void testLegacyApi_AllFieldsMatch_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.of(100));

        assertObject(entity,
                value("id", 1L),
                value("name", "test"),
                value("count", 42),
                value("description", Optional.of("desc")),
                value("score", Optional.of(100)),
                ignored("version"));
    }

    @Test
    public void testLegacyApi_OptionalEmpty_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        assertObject(entity,
                value("id", 1L),
                value("name", "test"),
                value("count", 42),
                value("description", Optional.empty()),
                value("score", Optional.empty()),
                ignored("version"));
    }

    @Test
    public void testLegacyApi_NullFields_Success() {
        AssertableEntity entity = new AssertableEntity(null, null, null, Optional.empty(), Optional.empty());

        assertObject(entity,
                isNull("id"),
                isNull("name"),
                isNull("count"),
                value("description", Optional.empty()),
                value("score", Optional.empty()),
                ignored("version"));
    }

    @Test
    public void testLegacyApi_NotNullAssertions_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        assertObject(entity,
                notNull("id"),
                notNull("name"),
                notNull("count"),
                value("description", Optional.empty()),
                value("score", Optional.empty()),
                ignored("version"));
    }

    @Test
    public void testLegacyApi_IgnoredFields_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        assertObject(entity,
                ignored("id"),
                ignored("name"),
                ignored("count"),
                ignored("description"),
                ignored("score"),
                ignored("version"));
    }

    @Test
    public void testLegacyApi_NotEmptyOptional_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.of(100));

        assertObject(entity,
                value("id", 1L),
                value("name", "test"),
                value("count", 42),
                notNull("description"),
                notNull("score"),
                ignored("version"));
    }

    // ==================== Failure Cases ====================

    @Test
    public void testLegacyApi_ValueMismatch_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "actual", 42, Optional.empty(), Optional.empty());

        assertThrows(AssertionError.class, () ->
                assertObject(entity,
                        value("id", 1L),
                        value("name", "expected"),  // mismatch
                        value("count", 42),
                        value("description", Optional.empty()),
                        value("score", Optional.empty()),
                        ignored("version"))
        );
    }

    @Test
    public void testLegacyApi_IsNullButHasValue_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        assertThrows(AssertionError.class, () ->
                assertObject(entity,
                        isNull("id"),  // has value, should fail
                        value("name", "test"),
                        value("count", 42),
                        value("description", Optional.empty()),
                        value("score", Optional.empty()),
                        ignored("version"))
        );
    }

    @Test
    public void testLegacyApi_NotNullButNull_Fails() {
        AssertableEntity entity = new AssertableEntity(null, "test", 42, Optional.empty(), Optional.empty());

        assertThrows(AssertionError.class, () ->
                assertObject(entity,
                        notNull("id"),  // null, should fail
                        value("name", "test"),
                        value("count", 42),
                        value("description", Optional.empty()),
                        value("score", Optional.empty()),
                        ignored("version"))
        );
    }

    @Test
    public void testLegacyApi_OptionalValueMismatch_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("actual"), Optional.empty());

        assertThrows(AssertionError.class, () ->
                assertObject(entity,
                        value("id", 1L),
                        value("name", "test"),
                        value("count", 42),
                        value("description", Optional.of("expected")),  // mismatch
                        value("score", Optional.empty()),
                        ignored("version"))
        );
    }

    // ==================== Mixed Assertions ====================

    @Test
    public void testLegacyApi_MixedAssertions_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.empty());

        assertObject(entity,
                notNull("id"),                    // notNull check
                value("name", "test"),            // exact value
                ignored("count"),                 // ignored
                value("description", Optional.of("desc")),  // Optional with value
                value("score", Optional.empty()), // Optional empty
                ignored("version"));
    }

    // ==================== Internal assertObjects (for testing parity with v4 internals) ====================

    @Test
    public void testLegacyApi_TwoObjectsEqual_Success() {
        AssertableEntity expected = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.of(100));
        AssertableEntity actual = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.of(100));

        // In v4 API, comparing two objects is done via internal assertObjects with FabutReport
        FabutReport report = new FabutReport();
        assertObjects(report, expected, actual, extractProperties(ignored("version")));
        assertTrue(report.isSuccess(), "Objects should be equal");
    }

    @Test
    public void testLegacyApi_TwoObjectsNotEqual_Fails() {
        AssertableEntity expected = new AssertableEntity(1L, "expected", 42, Optional.empty(), Optional.empty());
        AssertableEntity actual = new AssertableEntity(1L, "actual", 42, Optional.empty(), Optional.empty());

        FabutReport report = new FabutReport();
        assertObjects(report, expected, actual, extractProperties(ignored("version")));
        assertFalse(report.isSuccess(), "Objects should not be equal");
    }

    // ==================== Verify ignoredFields from @Assertable ====================

    @Test
    public void testLegacyApi_VersionFieldIgnored() {
        // The 'version' field needs to be explicitly ignored in legacy API
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());
        entity.setVersion(999L);  // Set version

        assertObject(entity,
                value("id", 1L),
                value("name", "test"),
                value("count", 42),
                value("description", Optional.empty()),
                value("score", Optional.empty()),
                ignored("version"));  // Must explicitly ignore in v4 API
    }
}
