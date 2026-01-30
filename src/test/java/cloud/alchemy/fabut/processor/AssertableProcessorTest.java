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
        // assertCreate(object) - uses ThreadLocal
        Method assertCreate = AssertableEntityAssert.class.getMethod("assertCreate", AssertableEntity.class);
        assertTrue(Modifier.isStatic(assertCreate.getModifiers()));
        assertTrue(Modifier.isPublic(assertCreate.getModifiers()));
        assertEquals(AssertableEntityAssert.class, assertCreate.getReturnType());

        // assertCreate(fabut, object) - explicit Fabut
        Method assertCreateWithFabut = AssertableEntityAssert.class.getMethod("assertCreate", Fabut.class, AssertableEntity.class);
        assertTrue(Modifier.isStatic(assertCreateWithFabut.getModifiers()));

        // assertUpdate methods
        Method assertUpdate = AssertableEntityAssert.class.getMethod("assertUpdate", AssertableEntity.class);
        assertTrue(Modifier.isStatic(assertUpdate.getModifiers()));

        Method assertUpdateWithFabut = AssertableEntityAssert.class.getMethod("assertUpdate", Fabut.class, AssertableEntity.class);
        assertTrue(Modifier.isStatic(assertUpdateWithFabut.getModifiers()));

        // assertDelete methods
        Method assertDelete = AssertableEntityAssert.class.getMethod("assertDelete", AssertableEntity.class);
        assertTrue(Modifier.isStatic(assertDelete.getModifiers()));

        Method assertDeleteWithFabut = AssertableEntityAssert.class.getMethod("assertDelete", Fabut.class, AssertableEntity.class);
        assertTrue(Modifier.isStatic(assertDeleteWithFabut.getModifiers()));
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

        // For regular field 'name': nameIs, nameIsNull, nameIsNotNull, nameIgnored
        assertTrue(methodNames.contains("nameIs"), "Should have nameIs method");
        assertTrue(methodNames.contains("nameIsNull"), "Should have nameIsNull method");
        assertTrue(methodNames.contains("nameIsNotNull"), "Should have nameIsNotNull method");
        assertTrue(methodNames.contains("nameIgnored"), "Should have nameIgnored method");

        // For Optional field 'description': additional isEmpty, isNotEmpty, hasValue
        assertTrue(methodNames.contains("descriptionIs"), "Should have descriptionIs method");
        assertTrue(methodNames.contains("descriptionIsEmpty"), "Should have descriptionIsEmpty method");
        assertTrue(methodNames.contains("descriptionIsNotEmpty"), "Should have descriptionIsNotEmpty method");
        assertTrue(methodNames.contains("descriptionHasValue"), "Should have descriptionHasValue method");
    }

    @Test
    public void testIgnoredFieldsNotGenerated() {
        // 'version' is in @Assertable(ignoredFields = {"version"})
        // So no versionIs, versionIsNull, etc. methods should exist
        Set<String> methodNames = Arrays.stream(AssertableEntityAssert.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        assertFalse(methodNames.contains("versionIs"), "version field should not have generated methods");
        assertFalse(methodNames.contains("versionIsNull"), "version field should not have generated methods");
        assertFalse(methodNames.contains("versionIsNotNull"), "version field should not have generated methods");
    }

    @Test
    public void testFieldMethodsReturnBuilder() throws NoSuchMethodException {
        // All field methods should return the builder for fluent chaining
        Method nameIs = AssertableEntityAssert.class.getMethod("nameIs", String.class);
        assertEquals(AssertableEntityAssert.class, nameIs.getReturnType());

        Method idIsNotNull = AssertableEntityAssert.class.getMethod("idIsNotNull");
        assertEquals(AssertableEntityAssert.class, idIsNotNull.getReturnType());

        Method descriptionIsEmpty = AssertableEntityAssert.class.getMethod("descriptionIsEmpty");
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
        AssertableEntityAssert.assertCreate(this, entity)
                .idIs(1L)
                .nameIs("test")
                .countIs(42)
                .descriptionHasValue("desc")
                .scoreHasValue(100)
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
        assertTrue(methodNames.contains("idIs"));
        assertTrue(methodNames.contains("nameIs"));
        assertTrue(methodNames.contains("countIs"));
        assertTrue(methodNames.contains("descriptionIs"));
        assertTrue(methodNames.contains("scoreIs"));
        assertFalse(methodNames.contains("versionIs"));
    }

    @Test
    public void testProcessorDetectsOptionalFields() {
        Set<String> methodNames = Arrays.stream(AssertableEntityAssert.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        // Optional fields (description, score) should have IsEmpty/IsNotEmpty/HasValue
        assertTrue(methodNames.contains("descriptionIsEmpty"));
        assertTrue(methodNames.contains("descriptionIsNotEmpty"));
        assertTrue(methodNames.contains("descriptionHasValue"));
        assertTrue(methodNames.contains("scoreIsEmpty"));
        assertTrue(methodNames.contains("scoreIsNotEmpty"));
        assertTrue(methodNames.contains("scoreHasValue"));

        // Non-Optional fields (id, name, count) should NOT have these methods
        assertFalse(methodNames.contains("idIsEmpty"));
        assertFalse(methodNames.contains("nameIsEmpty"));
        assertFalse(methodNames.contains("countIsEmpty"));
    }

    @Test
    public void testProcessorGeneratesCorrectParameterTypes() throws NoSuchMethodException {
        // id is Long
        Method idIs = AssertableEntityAssert.class.getMethod("idIs", Long.class);
        assertNotNull(idIs);

        // name is String
        Method nameIs = AssertableEntityAssert.class.getMethod("nameIs", String.class);
        assertNotNull(nameIs);

        // count is Integer
        Method countIs = AssertableEntityAssert.class.getMethod("countIs", Integer.class);
        assertNotNull(countIs);

        // description is Optional<String>, so descriptionIs takes Optional
        Method descriptionIs = AssertableEntityAssert.class.getMethod("descriptionIs", Optional.class);
        assertNotNull(descriptionIs);

        // descriptionHasValue takes the inner type (String)
        Method descriptionHasValue = AssertableEntityAssert.class.getMethod("descriptionHasValue", String.class);
        assertNotNull(descriptionHasValue);

        // scoreHasValue takes Integer (inner type of Optional<Integer>)
        Method scoreHasValue = AssertableEntityAssert.class.getMethod("scoreHasValue", Integer.class);
        assertNotNull(scoreHasValue);
    }
}
