package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.AssertableEntity;
import cloud.alchemy.fabut.model.AssertableEntityAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the annotation processor generated assertion builder.
 */
public class GeneratedAssertBuilderTest extends AbstractFabutTest {

    @BeforeEach
    public void setUpTest() {
        if (!complexTypes.isEmpty()) return;
        complexTypes.add(AssertableEntity.class);
    }

    // ==================== Basic Value Assertions ====================

    @Test
    public void testGeneratedBuilder_AllFieldsMatch_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.of(100));

        AssertableEntityAssert.created(this, entity)
                .id_is(1L)
                .name_is("test")
                .count_is(42)
                .description_is("desc")
                .score_is(100)
                .verify();
    }

    @Test
    public void testGeneratedBuilder_OptionalEmpty_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        AssertableEntityAssert.created(this, entity)
                .id_is(1L)
                .name_is("test")
                .count_is(42)
                .description_is_empty()
                .score_is_empty()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_NullFields_Success() {
        AssertableEntity entity = new AssertableEntity(null, null, null, Optional.empty(), Optional.empty());

        AssertableEntityAssert.created(this, entity)
                .id_is_null()
                .name_is_null()
                .count_is_null()
                .description_is_empty()
                .score_is_empty()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_NotNullAssertions_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        AssertableEntityAssert.created(this, entity)
                .id_is_not_null()
                .name_is_not_null()
                .count_is_not_null()
                .description_is_empty()
                .score_is_empty()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_IgnoredFields_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        AssertableEntityAssert.created(this, entity)
                .id_is_ignored()
                .name_is_ignored()
                .count_is_ignored()
                .description_is_ignored()
                .score_is_ignored()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_NotEmptyOptional_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.of(100));

        AssertableEntityAssert.created(this, entity)
                .id_is(1L)
                .name_is("test")
                .count_is(42)
                .description_is_not_empty()
                .score_is_not_empty()
                .verify();
    }

    // ==================== Failure Cases ====================

    @Test
    public void testGeneratedBuilder_ValueMismatch_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "actual", 42, Optional.empty(), Optional.empty());

        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.created(this, entity)
                        .id_is(1L)
                        .name_is("expected")  // mismatch
                        .count_is(42)
                        .description_is_empty()
                        .score_is_empty()
                        .verify()
        );
    }

    @Test
    public void testGeneratedBuilder_IsEmptyButHasValue_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("value"), Optional.empty());

        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.created(this, entity)
                        .id_is(1L)
                        .name_is("test")
                        .count_is(42)
                        .description_is_empty()  // has value, should fail
                        .score_is_empty()
                        .verify()
        );
    }

    @Test
    public void testGeneratedBuilder_IsNotEmptyButEmpty_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.created(this, entity)
                        .id_is(1L)
                        .name_is("test")
                        .count_is(42)
                        .description_is_not_empty()  // empty, should fail
                        .score_is_empty()
                        .verify()
        );
    }

    @Test
    public void testGeneratedBuilder_HasValueMismatch_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("actual"), Optional.empty());

        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.created(this, entity)
                        .id_is(1L)
                        .name_is("test")
                        .count_is(42)
                        .description_is("expected")  // mismatch
                        .score_is_empty()
                        .verify()
        );
    }

    // ==================== Mixed Assertions ====================

    @Test
    public void testGeneratedBuilder_MixedAssertions_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.empty());

        AssertableEntityAssert.created(this, entity)
                .id_is_not_null()           // notNull check
                .name_is("test")          // exact value
                .count_is_ignored()          // ignored
                .description_is("desc")  // Optional with value
                .score_is_empty()          // Optional empty
                .verify();
    }

    // ==================== Return Value ====================

    @Test
    public void testGeneratedBuilder_ReturnsObject() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        AssertableEntity result = AssertableEntityAssert.created(this, entity)
                .id_is(1L)
                .name_is("test")
                .count_is(42)
                .description_is_empty()
                .score_is_empty()
                .verify();

        assertSame(entity, result, "verify() should return the asserted object");
    }

    // ==================== assertCreate with All Mandatory Fields ====================

    @Test
    public void testGeneratedBuilder_AssertNewWithMandatoryFields_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.of(100));

        // Use assertCreate with all mandatory fields as parameters
        AssertableEntityAssert.created(entity, 1L, "test", 42, false)
                .description_is("desc")
                .score_is(100)
                .visible_is(true)
                .category_is_empty()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_AssertNewWithMandatoryFieldsAndFabut_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        // Use created with explicit Fabut and all mandatory fields
        AssertableEntityAssert.created(this, entity, 1L, "test", 42, false)
                .description_is_empty()
                .score_is_empty()
                .visible_is(true)
                .category_is_empty()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_AssertNewWithMandatoryFields_IgnoreOptional() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.of(100));

        // Mandatory fields are set, optional fields can be ignored
        AssertableEntityAssert.created(entity, 1L, "test", 42, false)
                .description_is_ignored()
                .score_is_ignored()
                .visible_is_ignored()
                .category_is_ignored()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_AssertNewWithMandatoryFields_ValueMismatch_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "actual", 42, Optional.empty(), Optional.empty());

        // Mismatch in mandatory field should fail
        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.created(entity, 1L, "expected", 42, false)
                        .description_is_empty()
                        .score_is_empty()
                        .visible_is(true)
                        .category_is_empty()
                        .verify()
        );
    }

    // ==================== Custom Assert Groups ====================

    @Test
    public void testGeneratedBuilder_AssertNewKey_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.of(100));

        // Use assertCreateKey - only asserts id and name
        AssertableEntityAssert.createdKey(entity, 1L, "test")
                .count_is(42)
                .description_is("desc")
                .score_is(100)
                .verify();
    }

    @Test
    public void testGeneratedBuilder_AssertNewKey_WithFabut_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        AssertableEntityAssert.createdKey(this, entity, 1L, "test")
                .count_is(42)
                .description_is_empty()
                .score_is_empty()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_AssertNewStats_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.of(100));

        // Use assertCreateStats - asserts count and score
        AssertableEntityAssert.createdStats(entity, 42, 100)
                .id_is(1L)
                .name_is("test")
                .description_is("desc")
                .verify();
    }

    @Test
    public void testGeneratedBuilder_AssertNewStats_NullOptional_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        // assertCreateStats with null for Optional score
        AssertableEntityAssert.createdStats(entity, 42, null)
                .id_is(1L)
                .name_is("test")
                .description_is_empty()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_AssertNewKey_ValueMismatch_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "actual", 42, Optional.empty(), Optional.empty());

        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.createdKey(entity, 1L, "expected")
                        .count_is(42)
                        .description_is_empty()
                        .score_is_empty()
                        .verify()
        );
    }

    // ==================== Verify ignoredFields from @Assertable ====================

    @Test
    public void testGeneratedBuilder_VersionFieldIgnored() {
        // The 'version' field was specified in @Assertable(ignoredFields = {"version"})
        // So no versionIs(), versionIsNull() etc. methods should be generated
        // This test verifies the entity can be asserted without specifying version
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());
        entity.setVersion(999L);  // Set version, but we don't need to assert it

        AssertableEntityAssert.created(this, entity)
                .id_is(1L)
                .name_is("test")
                .count_is(42)
                .description_is_empty()
                .score_is_empty()
                .verify();
        // No versionIs() call needed - field is ignored
    }

    // ==================== @AssertDefault Tests ====================

    @Test
    public void testAssertDefault_CreatedWithDefaults_Success() {
        // All defaults match constructor values: active=false, visible=Optional.of(true), category=Optional.empty()
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        AssertableEntityAssert.created(this, entity)
                .id_is(1L)
                .name_is("test")
                .count_is(42)
                .description_is_empty()
                .score_is_empty()
                // active, visible, category auto-asserted by defaults
                .verify();
    }

    @Test
    public void testAssertDefault_BooleanMismatch_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());
        entity.setActive(true);  // Default expects false

        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.created(this, entity)
                        .id_is(1L)
                        .name_is("test")
                        .count_is(42)
                        .description_is_empty()
                        .score_is_empty()
                        .verify()
        );
    }

    @Test
    public void testAssertDefault_OptionalBooleanMismatch_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());
        entity.setVisible(Optional.of(false));  // Default expects true

        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.created(this, entity)
                        .id_is(1L)
                        .name_is("test")
                        .count_is(42)
                        .description_is_empty()
                        .score_is_empty()
                        .verify()
        );
    }

    @Test
    public void testAssertDefault_OptionalEmptyMismatch_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());
        entity.setCategory(Optional.of("X"));  // Default expects empty

        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.created(this, entity)
                        .id_is(1L)
                        .name_is("test")
                        .count_is(42)
                        .description_is_empty()
                        .score_is_empty()
                        .verify()
        );
    }

    @Test
    public void testAssertDefault_ExplicitOverride_Boolean_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());
        entity.setActive(true);

        AssertableEntityAssert.created(this, entity)
                .id_is(1L)
                .name_is("test")
                .count_is(42)
                .description_is_empty()
                .score_is_empty()
                .active_is(true)  // Override default
                .verify();
    }

    @Test
    public void testAssertDefault_ExplicitOverride_OptionalBoolean_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());
        entity.setVisible(Optional.of(false));

        AssertableEntityAssert.created(this, entity)
                .id_is(1L)
                .name_is("test")
                .count_is(42)
                .description_is_empty()
                .score_is_empty()
                .visible_is(false)  // Override default
                .verify();
    }

    @Test
    public void testAssertDefault_ExplicitOverride_OptionalString_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());
        entity.setCategory(Optional.of("X"));

        AssertableEntityAssert.created(this, entity)
                .id_is(1L)
                .name_is("test")
                .count_is(42)
                .description_is_empty()
                .score_is_empty()
                .category_is("X")  // Override default
                .verify();
    }

    @Test
    public void testAssertDefault_IgnoredOverridesDefault_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());
        entity.setActive(true);  // Doesn't matter — field is ignored

        AssertableEntityAssert.created(this, entity)
                .id_is(1L)
                .name_is("test")
                .count_is(42)
                .description_is_empty()
                .score_is_empty()
                .active_is_ignored()  // Suppresses default assertion
                .visible_is_ignored()
                .category_is_ignored()
                .verify();
    }

    @Test
    public void testAssertDefault_GroupMethodWithDefaults_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        // createdKey only sets id and name — defaults for active, visible, category auto-asserted
        AssertableEntityAssert.createdKey(entity, 1L, "test")
                .count_is(42)
                .description_is_empty()
                .score_is_empty()
                // active, visible, category auto-asserted by defaults
                .verify();
    }

    // ==================== Auto-Verify ====================

    @Test
    public void testGeneratedBuilder_ForgottenVerify_FailsInAfterEach() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        // Create builder but intentionally skip verify()
        AssertableEntityAssert.created(this, entity)
                .id_is(1L)
                .name_is("test")
                .count_is(42)
                .description_is_empty()
                .score_is_empty();
        // no .verify()

        // Simulate @AfterEach — should detect unverified builder
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> after());
        assertTrue(error.getMessage().contains("UNVERIFIED BUILDER"));

        // Re-run before() to reset state so the real @AfterEach doesn't fail
        before();
    }
}
