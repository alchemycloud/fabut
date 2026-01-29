package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.TierOneType;
import cloud.alchemy.fabut.model.TypeWithMixedFields;
import cloud.alchemy.fabut.model.TypeWithOptionalEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the fluent assertion builder with reflection-based field detection.
 */
public class FluentAssertBuilderTest extends AbstractFabutTest {

    @BeforeEach
    public void setUpTest() {
        if (!complexTypes.isEmpty()) return;
        complexTypes.add(TierOneType.class);
        complexTypes.add(TypeWithMixedFields.class);
        complexTypes.add(TypeWithOptionalEntity.class);
    }

    // ==================== Basic assertThat Tests ====================

    @Test
    public void testAssertThat_AllFieldsSpecified_Success() {
        // All fields explicitly specified - should pass
        TypeWithMixedFields obj = new TypeWithMixedFields("test", 42, Optional.of("desc"), Optional.of(100));

        assertThat(obj)
                .value(TypeWithMixedFields.NAME, "test")
                .value(TypeWithMixedFields.COUNT, 42)
                .value(TypeWithMixedFields.DESCRIPTION, Optional.of("desc"))
                .value(TypeWithMixedFields.SCORE, Optional.of(100))
                .verify();
    }

    @Test
    public void testAssertThat_OptionalFieldsNotSpecified_AutoAddsIsEmpty() {
        // Optional fields not specified - should auto-add isEmpty() for them
        TypeWithMixedFields obj = new TypeWithMixedFields("test", 42, Optional.empty(), Optional.empty());

        assertThat(obj)
                .value(TypeWithMixedFields.NAME, "test")
                .value(TypeWithMixedFields.COUNT, 42)
                // DESCRIPTION and SCORE not specified - should auto-add isEmpty()
                .verify();
    }

    @Test
    public void testAssertThat_MandatoryFieldMissing_ThrowsError() {
        // Non-Optional field not specified - should throw error
        TypeWithMixedFields obj = new TypeWithMixedFields("test", 42, Optional.empty(), Optional.empty());

        AssertionError error = assertThrows(AssertionError.class, () ->
                assertThat(obj)
                        .value(TypeWithMixedFields.NAME, "test")
                        // COUNT is mandatory (non-Optional) but not specified
                        .verify()
        );

        assertTrue(error.getMessage().contains("Missing mandatory field assertions"),
                "Expected error about missing mandatory fields, got: " + error.getMessage());
        assertTrue(error.getMessage().contains("count"),
                "Expected error to mention 'count' field, got: " + error.getMessage());
    }

    @Test
    public void testAssertThat_AllMandatoryMissing_ListsAllMissing() {
        // Neither mandatory field specified - should list both
        TypeWithMixedFields obj = new TypeWithMixedFields("test", 42, Optional.empty(), Optional.empty());

        AssertionError error = assertThrows(AssertionError.class, () ->
                assertThat(obj)
                        // Neither NAME nor COUNT specified
                        .verify()
        );

        String message = error.getMessage();
        assertTrue(message.contains("name"), "Expected error to mention 'name', got: " + message);
        assertTrue(message.contains("count"), "Expected error to mention 'count', got: " + message);
    }

    @Test
    public void testAssertThat_OptionalFieldWithValue_FailsIfIsEmptyAutoAdded() {
        // Optional field has value but not specified - auto-added isEmpty() should fail
        TypeWithMixedFields obj = new TypeWithMixedFields("test", 42, Optional.of("has value"), Optional.empty());

        assertThrows(AssertionFailedError.class, () ->
                assertThat(obj)
                        .value(TypeWithMixedFields.NAME, "test")
                        .value(TypeWithMixedFields.COUNT, 42)
                        // DESCRIPTION has value but not specified - isEmpty() assertion should fail
                        .verify()
        );
    }

    // ==================== Different Property Types ====================

    @Test
    public void testAssertThat_NotNull_Success() {
        TypeWithMixedFields obj = new TypeWithMixedFields("test", 42, Optional.empty(), Optional.empty());

        assertThat(obj)
                .notNull(TypeWithMixedFields.NAME, TypeWithMixedFields.COUNT)
                .verify();
    }

    @Test
    public void testAssertThat_IsNull_Success() {
        TypeWithMixedFields obj = new TypeWithMixedFields(null, null, Optional.empty(), Optional.empty());

        assertThat(obj)
                .isNull(TypeWithMixedFields.NAME, TypeWithMixedFields.COUNT)
                .verify();
    }

    @Test
    public void testAssertThat_Ignored_Success() {
        TypeWithMixedFields obj = new TypeWithMixedFields("test", 42, Optional.empty(), Optional.empty());

        assertThat(obj)
                .ignored(TypeWithMixedFields.NAME, TypeWithMixedFields.COUNT)
                .verify();
    }

    @Test
    public void testAssertThat_IsEmpty_Success() {
        TypeWithMixedFields obj = new TypeWithMixedFields("test", 42, Optional.empty(), Optional.empty());

        assertThat(obj)
                .value(TypeWithMixedFields.NAME, "test")
                .value(TypeWithMixedFields.COUNT, 42)
                .isEmpty(TypeWithMixedFields.DESCRIPTION, TypeWithMixedFields.SCORE)
                .verify();
    }

    @Test
    public void testAssertThat_NotEmpty_Success() {
        TypeWithMixedFields obj = new TypeWithMixedFields("test", 42, Optional.of("desc"), Optional.of(100));

        assertThat(obj)
                .value(TypeWithMixedFields.NAME, "test")
                .value(TypeWithMixedFields.COUNT, 42)
                .notEmpty(TypeWithMixedFields.DESCRIPTION, TypeWithMixedFields.SCORE)
                .verify();
    }

    // ==================== Simple Type (non-Optional only) ====================

    @Test
    public void testAssertThat_SimpleType_AllFieldsRequired() {
        // TierOneType has only 'property' field (String, not Optional)
        TierOneType obj = new TierOneType("test");

        assertThat(obj)
                .value(TierOneType.PROPERTY, "test")
                .verify();
    }

    @Test
    public void testAssertThat_SimpleType_MandatoryMissing_ThrowsError() {
        TierOneType obj = new TierOneType("test");

        AssertionError error = assertThrows(AssertionError.class, () ->
                assertThat(obj).verify()  // property is mandatory but not specified
        );

        assertTrue(error.getMessage().contains("property"),
                "Expected error to mention 'property', got: " + error.getMessage());
    }

    // ==================== Only Optional Fields ====================

    @Test
    public void testAssertThat_OnlyOptionalFields_NoMandatory() {
        // TypeWithOptionalEntity has only Optional field - no mandatory fields
        TypeWithOptionalEntity obj = new TypeWithOptionalEntity(Optional.empty());

        // No fields specified - should auto-add isEmpty() and pass
        assertThat(obj).verify();
    }

    // ==================== Return Value ====================

    @Test
    public void testAssertThat_ReturnsObject() {
        TypeWithMixedFields obj = new TypeWithMixedFields("test", 42, Optional.empty(), Optional.empty());

        TypeWithMixedFields result = assertThat(obj)
                .value(TypeWithMixedFields.NAME, "test")
                .value(TypeWithMixedFields.COUNT, 42)
                .verify();

        assertSame(obj, result, "verify() should return the asserted object");
    }

    // ==================== Mixed Assertion Types ====================

    @Test
    public void testAssertThat_MixedAssertionTypes() {
        TypeWithMixedFields obj = new TypeWithMixedFields("test", 42, Optional.of("desc"), Optional.empty());

        assertThat(obj)
                .value(TypeWithMixedFields.NAME, "test")  // exact value
                .notNull(TypeWithMixedFields.COUNT)        // not null
                .notEmpty(TypeWithMixedFields.DESCRIPTION) // optional with value
                .isEmpty(TypeWithMixedFields.SCORE)        // optional empty
                .verify();
    }

    // ==================== Value Mismatch ====================

    @Test
    public void testAssertThat_ValueMismatch_Fails() {
        TypeWithMixedFields obj = new TypeWithMixedFields("actual", 42, Optional.empty(), Optional.empty());

        assertThrows(AssertionFailedError.class, () ->
                assertThat(obj)
                        .value(TypeWithMixedFields.NAME, "expected")  // mismatch
                        .value(TypeWithMixedFields.COUNT, 42)
                        .verify()
        );
    }
}
