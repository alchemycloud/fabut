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

        AssertableEntityAssert.assertThat(this, entity)
                .idIs(1L)
                .nameIs("test")
                .countIs(42)
                .descriptionHasValue("desc")
                .scoreHasValue(100)
                .verify();
    }

    @Test
    public void testGeneratedBuilder_OptionalEmpty_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        AssertableEntityAssert.assertThat(this, entity)
                .idIs(1L)
                .nameIs("test")
                .countIs(42)
                .descriptionIsEmpty()
                .scoreIsEmpty()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_NullFields_Success() {
        AssertableEntity entity = new AssertableEntity(null, null, null, Optional.empty(), Optional.empty());

        AssertableEntityAssert.assertThat(this, entity)
                .idIsNull()
                .nameIsNull()
                .countIsNull()
                .descriptionIsEmpty()
                .scoreIsEmpty()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_NotNullAssertions_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        AssertableEntityAssert.assertThat(this, entity)
                .idIsNotNull()
                .nameIsNotNull()
                .countIsNotNull()
                .descriptionIsEmpty()
                .scoreIsEmpty()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_IgnoredFields_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        AssertableEntityAssert.assertThat(this, entity)
                .idIgnored()
                .nameIgnored()
                .countIgnored()
                .descriptionIgnored()
                .scoreIgnored()
                .verify();
    }

    @Test
    public void testGeneratedBuilder_NotEmptyOptional_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.of(100));

        AssertableEntityAssert.assertThat(this, entity)
                .idIs(1L)
                .nameIs("test")
                .countIs(42)
                .descriptionIsNotEmpty()
                .scoreIsNotEmpty()
                .verify();
    }

    // ==================== Failure Cases ====================

    @Test
    public void testGeneratedBuilder_ValueMismatch_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "actual", 42, Optional.empty(), Optional.empty());

        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.assertThat(this, entity)
                        .idIs(1L)
                        .nameIs("expected")  // mismatch
                        .countIs(42)
                        .descriptionIsEmpty()
                        .scoreIsEmpty()
                        .verify()
        );
    }

    @Test
    public void testGeneratedBuilder_IsEmptyButHasValue_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("value"), Optional.empty());

        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.assertThat(this, entity)
                        .idIs(1L)
                        .nameIs("test")
                        .countIs(42)
                        .descriptionIsEmpty()  // has value, should fail
                        .scoreIsEmpty()
                        .verify()
        );
    }

    @Test
    public void testGeneratedBuilder_IsNotEmptyButEmpty_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.assertThat(this, entity)
                        .idIs(1L)
                        .nameIs("test")
                        .countIs(42)
                        .descriptionIsNotEmpty()  // empty, should fail
                        .scoreIsEmpty()
                        .verify()
        );
    }

    @Test
    public void testGeneratedBuilder_HasValueMismatch_Fails() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("actual"), Optional.empty());

        assertThrows(AssertionFailedError.class, () ->
                AssertableEntityAssert.assertThat(this, entity)
                        .idIs(1L)
                        .nameIs("test")
                        .countIs(42)
                        .descriptionHasValue("expected")  // mismatch
                        .scoreIsEmpty()
                        .verify()
        );
    }

    // ==================== Mixed Assertions ====================

    @Test
    public void testGeneratedBuilder_MixedAssertions_Success() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.empty());

        AssertableEntityAssert.assertThat(this, entity)
                .idIsNotNull()           // notNull check
                .nameIs("test")          // exact value
                .countIgnored()          // ignored
                .descriptionHasValue("desc")  // Optional with value
                .scoreIsEmpty()          // Optional empty
                .verify();
    }

    // ==================== Return Value ====================

    @Test
    public void testGeneratedBuilder_ReturnsObject() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        AssertableEntity result = AssertableEntityAssert.assertThat(this, entity)
                .idIs(1L)
                .nameIs("test")
                .countIs(42)
                .descriptionIsEmpty()
                .scoreIsEmpty()
                .verify();

        assertSame(entity, result, "verify() should return the asserted object");
    }

    // ==================== Verify ignoredFields from @Assertable ====================

    @Test
    public void testGeneratedBuilder_VersionFieldIgnored() {
        // The 'version' field was specified in @Assertable(ignoredFields = {"version"})
        // So no versionIs(), versionIsNull() etc. methods should be generated
        // This test verifies the entity can be asserted without specifying version
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());
        entity.setVersion(999L);  // Set version, but we don't need to assert it

        AssertableEntityAssert.assertThat(this, entity)
                .idIs(1L)
                .nameIs("test")
                .countIs(42)
                .descriptionIsEmpty()
                .scoreIsEmpty()
                .verify();
        // No versionIs() call needed - field is ignored
    }
}
