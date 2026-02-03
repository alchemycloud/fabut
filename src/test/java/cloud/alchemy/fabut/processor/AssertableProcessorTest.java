package cloud.alchemy.fabut.processor;

import cloud.alchemy.fabut.Fabut;
import cloud.alchemy.fabut.diff.Diff;
import cloud.alchemy.fabut.model.AssertableEntity;
import cloud.alchemy.fabut.model.AssertableEntityAssert;
import cloud.alchemy.fabut.model.AssertableEntityDiff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AssertableProcessor} - verifies that generated code is correct.
 *
 * The processor runs at compile time, so we test it indirectly by:
 * 1. Verifying generated classes exist with expected structure
 * 2. Verifying generated methods work correctly
 * 3. Testing edge cases through the generated API
 */
public class AssertableProcessorTest extends Fabut {

    @BeforeEach
    public void setUpTest() {
        if (!complexTypes.isEmpty()) return;
        complexTypes.add(AssertableEntity.class);
    }

    // ==================== Generated Class Structure Tests ====================

    @Test
    public void testGeneratedAssertClassExists() {
        // Verify AssertableEntityAssert class was generated
        assertNotNull(AssertableEntityAssert.class);
        assertTrue(Modifier.isPublic(AssertableEntityAssert.class.getModifiers()));
    }

    @Test
    public void testGeneratedDiffClassExists() {
        // Verify AssertableEntityDiff class was generated
        assertNotNull(AssertableEntityDiff.class);
        assertTrue(Modifier.isPublic(AssertableEntityDiff.class.getModifiers()));
        assertTrue(Modifier.isFinal(AssertableEntityDiff.class.getModifiers()));
    }

    @Test
    public void testGeneratedClassesInCorrectPackage() {
        // Verify generated classes are in same package as source class
        assertEquals("cloud.alchemy.fabut.model", AssertableEntityAssert.class.getPackageName());
        assertEquals("cloud.alchemy.fabut.model", AssertableEntityDiff.class.getPackageName());
    }

    @Test
    public void testGeneratedClassNaming() {
        // Verify naming convention: ClassName + "Assert" / ClassName + "Diff"
        assertEquals("AssertableEntityAssert", AssertableEntityAssert.class.getSimpleName());
        assertEquals("AssertableEntityDiff", AssertableEntityDiff.class.getSimpleName());
    }

    // ==================== Assert Builder Method Generation Tests ====================

    @Test
    public void testAssertBuilderHasStaticFactoryMethods() throws NoSuchMethodException {
        // created(object) - uses ThreadLocal
        Method created = AssertableEntityAssert.class.getMethod("created", AssertableEntity.class);
        assertTrue(Modifier.isStatic(created.getModifiers()));
        assertTrue(Modifier.isPublic(created.getModifiers()));
        assertEquals(AssertableEntityAssert.class, created.getReturnType());

        // created(fabut, object) - explicit Fabut
        Method createdWithFabut = AssertableEntityAssert.class.getMethod("created", Fabut.class, AssertableEntity.class);
        assertTrue(Modifier.isStatic(createdWithFabut.getModifiers()));

        // updated methods
        Method updated = AssertableEntityAssert.class.getMethod("updated", AssertableEntity.class);
        assertTrue(Modifier.isStatic(updated.getModifiers()));

        Method updatedWithFabut = AssertableEntityAssert.class.getMethod("updated", Fabut.class, AssertableEntity.class);
        assertTrue(Modifier.isStatic(updatedWithFabut.getModifiers()));

        // deleted methods
        Method deleted = AssertableEntityAssert.class.getMethod("deleted", AssertableEntity.class);
        assertTrue(Modifier.isStatic(deleted.getModifiers()));

        Method deletedWithFabut = AssertableEntityAssert.class.getMethod("deleted", Fabut.class, AssertableEntity.class);
        assertTrue(Modifier.isStatic(deletedWithFabut.getModifiers()));
    }

    @Test
    public void testAssertBuilderHasVerifyMethod() throws NoSuchMethodException {
        Method verify = AssertableEntityAssert.class.getMethod("verify");
        assertTrue(Modifier.isPublic(verify.getModifiers()));
        assertEquals(AssertableEntity.class, verify.getReturnType());
    }

    @Test
    public void testAssertBuilderGeneratesFieldMethods() {
        Set<String> methodNames = Arrays.stream(AssertableEntityAssert.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        // For regular field 'name': name_is, name_is_null, name_is_not_null, name_ignored
        assertTrue(methodNames.contains("name_is"), "Should have name_is method");
        assertTrue(methodNames.contains("name_is_null"), "Should have name_is_null method");
        assertTrue(methodNames.contains("name_is_not_null"), "Should have name_is_not_null method");
        assertTrue(methodNames.contains("name_is_ignored"), "Should have name_is_ignored method");

        // For Optional field 'description': additional is_empty, is_not_empty, and overloaded _is(InnerType)
        assertTrue(methodNames.contains("description_is"), "Should have description_is method");
        assertTrue(methodNames.contains("description_is_empty"), "Should have description_is_empty method");
        assertTrue(methodNames.contains("description_is_not_empty"), "Should have description_is_not_empty method");
    }

    @Test
    public void testIgnoredFieldsNotGenerated() {
        // 'version' is in @Assertable(ignoredFields = {"version"})
        // So no versionIs, versionIsNull, etc. methods should exist
        Set<String> methodNames = Arrays.stream(AssertableEntityAssert.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        assertFalse(methodNames.contains("version_is"), "version field should not have generated methods");
        assertFalse(methodNames.contains("version_is_null"), "version field should not have generated methods");
        assertFalse(methodNames.contains("version_is_not_null"), "version field should not have generated methods");
    }

    @Test
    public void testFieldMethodsReturnBuilder() throws NoSuchMethodException {
        // All field methods should return the builder for fluent chaining
        Method nameIs = AssertableEntityAssert.class.getMethod("name_is", String.class);
        assertEquals(AssertableEntityAssert.class, nameIs.getReturnType());

        Method idIsNotNull = AssertableEntityAssert.class.getMethod("id_is_not_null");
        assertEquals(AssertableEntityAssert.class, idIsNotNull.getReturnType());

        Method descriptionIsEmpty = AssertableEntityAssert.class.getMethod("description_is_empty");
        assertEquals(AssertableEntityAssert.class, descriptionIsEmpty.getReturnType());
    }

    // ==================== Diff Class Method Generation Tests ====================

    @Test
    public void testDiffClassHasCompareMethod() throws NoSuchMethodException {
        // compare(before, after)
        Method compare = AssertableEntityDiff.class.getMethod("compare",
                AssertableEntity.class, AssertableEntity.class);
        assertTrue(Modifier.isStatic(compare.getModifiers()));
        assertTrue(Modifier.isPublic(compare.getModifiers()));

        // compare(before, after, identifier)
        Method compareWithId = AssertableEntityDiff.class.getMethod("compare",
                AssertableEntity.class, AssertableEntity.class, String.class);
        assertTrue(Modifier.isStatic(compareWithId.getModifiers()));
    }

    @Test
    public void testDiffClassHasPrivateConstructor() {
        // Diff class should have private constructor (utility class pattern)
        var constructors = AssertableEntityDiff.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));
    }

    // ==================== Generated Code Behavior Tests ====================

    @Test
    public void testGeneratedAssertBuilderWorks() {
        AssertableEntity entity = new AssertableEntity(1L, "test", 42, Optional.of("desc"), Optional.of(100));

        // This should work without throwing
        AssertableEntityAssert.created(this, entity)
                .id_is(1L)
                .name_is("test")
                .count_is(42)
                .description_is("desc")
                .score_is(100)
                .verify();
    }

    @Test
    public void testGeneratedDiffComparesFields() {
        AssertableEntity before = new AssertableEntity(1L, "before", 10, Optional.of("old"), Optional.of(5));
        AssertableEntity after = new AssertableEntity(1L, "after", 20, Optional.of("new"), Optional.of(99));

        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);

        assertTrue(diff.hasChanges());
        assertEquals(4, diff.changeCount()); // name, count, description, score changed
        assertFalse(diff.isFieldChanged("id")); // id unchanged
        assertTrue(diff.isFieldChanged("name"));
        assertTrue(diff.isFieldChanged("count"));
        assertTrue(diff.isFieldChanged("description"));
        assertTrue(diff.isFieldChanged("score"));
    }

    @Test
    public void testGeneratedDiffWithCustomIdentifier() {
        AssertableEntity before = new AssertableEntity(1L, "before", 10, Optional.empty(), Optional.empty());
        AssertableEntity after = new AssertableEntity(1L, "after", 10, Optional.empty(), Optional.empty());

        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after, "Custom ID #123");

        assertEquals("Custom ID #123", diff.getObjectIdentifier());
    }

    @Test
    public void testGeneratedDiffHandlesNullBefore() {
        AssertableEntity after = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(null, after);

        assertTrue(diff.hasChanges());
        // All fields should show as SET (from null to value)
        assertEquals(5, diff.changeCount());
    }

    @Test
    public void testGeneratedDiffHandlesNullAfter() {
        AssertableEntity before = new AssertableEntity(1L, "test", 42, Optional.empty(), Optional.empty());

        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, null);

        assertTrue(diff.hasChanges());
        // All fields should show as CLEARED (from value to null)
        assertEquals(5, diff.changeCount());
    }

    @Test
    public void testGeneratedDiffNoChanges() {
        AssertableEntity before = new AssertableEntity(1L, "same", 42, Optional.of("desc"), Optional.of(100));
        AssertableEntity after = new AssertableEntity(1L, "same", 42, Optional.of("desc"), Optional.of(100));

        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);

        assertFalse(diff.hasChanges());
        assertEquals(0, diff.changeCount());
    }

    @Test
    public void testGeneratedDiffReportsContainFieldInfo() {
        AssertableEntity before = new AssertableEntity(1L, "before", 10, Optional.empty(), Optional.empty());
        AssertableEntity after = new AssertableEntity(1L, "after", 10, Optional.empty(), Optional.empty());

        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);

        String consoleReport = diff.toConsoleReport();
        assertTrue(consoleReport.contains("name"));
        assertTrue(consoleReport.contains("before"));
        assertTrue(consoleReport.contains("after"));
        assertTrue(consoleReport.contains("MODIFIED"));

        String htmlReport = diff.toHtmlReport();
        assertTrue(htmlReport.contains("name"));
        assertTrue(htmlReport.contains("<!DOCTYPE html>"));
    }

    // ==================== Field Extraction Tests ====================

    @Test
    public void testProcessorExtractsAllNonIgnoredFields() {
        Set<String> methodNames = Arrays.stream(AssertableEntityAssert.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        // Expected fields: id, name, count, description, score
        // NOT expected: version (ignored)
        assertTrue(methodNames.contains("id_is"));
        assertTrue(methodNames.contains("name_is"));
        assertTrue(methodNames.contains("count_is"));
        assertTrue(methodNames.contains("description_is"));
        assertTrue(methodNames.contains("score_is"));
        assertFalse(methodNames.contains("version_is"));
    }

    @Test
    public void testProcessorDetectsOptionalFields() {
        Set<String> methodNames = Arrays.stream(AssertableEntityAssert.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        // Optional fields (description, score) should have _is_empty/_is_not_empty and overloaded _is(InnerType)
        assertTrue(methodNames.contains("description_is_empty"));
        assertTrue(methodNames.contains("description_is_not_empty"));
        assertTrue(methodNames.contains("description_is"));
        assertTrue(methodNames.contains("score_is_empty"));
        assertTrue(methodNames.contains("score_is_not_empty"));
        assertTrue(methodNames.contains("score_is"));

        // Non-Optional fields (id, name, count) should NOT have these methods
        assertFalse(methodNames.contains("id_is_empty"));
        assertFalse(methodNames.contains("name_is_empty"));
        assertFalse(methodNames.contains("count_is_empty"));
    }

    @Test
    public void testProcessorGeneratesCorrectParameterTypes() throws NoSuchMethodException {
        // id is Long
        Method idIs = AssertableEntityAssert.class.getMethod("id_is", Long.class);
        assertNotNull(idIs);

        // name is String
        Method nameIs = AssertableEntityAssert.class.getMethod("name_is", String.class);
        assertNotNull(nameIs);

        // count is Integer
        Method countIs = AssertableEntityAssert.class.getMethod("count_is", Integer.class);
        assertNotNull(countIs);

        // description is Optional<String>, so description_is(Optional) takes Optional
        Method descriptionIsOptional = AssertableEntityAssert.class.getMethod("description_is", Optional.class);
        assertNotNull(descriptionIsOptional);

        // description_is(String) is the overloaded convenience method taking the inner type
        Method descriptionIsString = AssertableEntityAssert.class.getMethod("description_is", String.class);
        assertNotNull(descriptionIsString);

        // score_is(Integer) is the overloaded convenience method (inner type of Optional<Integer>)
        Method scoreIsInteger = AssertableEntityAssert.class.getMethod("score_is", Integer.class);
        assertNotNull(scoreIsInteger);
    }
}
