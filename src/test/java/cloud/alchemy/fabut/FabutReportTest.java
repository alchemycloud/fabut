package cloud.alchemy.fabut;

import cloud.alchemy.fabut.enums.AssertionContext;
import cloud.alchemy.fabut.enums.EntityChangeType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FabutReport class.
 */
class FabutReportTest {

    // ==================== Constructor Tests ====================

    @Test
    void defaultConstructor_createsSuccessfulReport() {
        FabutReport report = new FabutReport();
        assertTrue(report.isSuccess());
    }

    @Test
    void constructorWithMessage_createsSuccessfulReport() {
        FabutReport report = new FabutReport(() -> "Test message");
        assertTrue(report.isSuccess());
        assertEquals(1, report.getMessageCount());
    }

    @Test
    void constructorWithNullMessage_createsEmptyReport() {
        FabutReport report = new FabutReport(null);
        assertTrue(report.isSuccess());
        assertEquals(0, report.getMessageCount());
    }

    // ==================== isSuccess() Tests ====================

    @Test
    void isSuccess_afterFailure_returnsFalse() {
        FabutReport report = new FabutReport();
        report.assertFail("field", "expected", "actual");
        assertFalse(report.isSuccess());
    }

    @Test
    void isSuccess_withFailedSubreport_returnsFalse() {
        FabutReport parent = new FabutReport();
        FabutReport child = parent.getSubReport(() -> "child");
        child.assertFail("field", "expected", "actual");

        assertFalse(parent.isSuccess());
    }

    // ==================== getSubReport() Tests ====================

    @Test
    void getSubReport_createsNewSubreport() {
        FabutReport report = new FabutReport();
        FabutReport subReport = report.getSubReport(() -> "Sub message");

        assertNotNull(subReport);
        assertEquals(1, report.getSubreportCount());
    }

    @Test
    void getSubReport_nullMessage_throwsException() {
        FabutReport report = new FabutReport();
        assertThrows(NullPointerException.class, () -> report.getSubReport(null));
    }

    // ==================== Comment Methods Tests ====================

    @Test
    void listDifferentSizeComment_marksFailure() {
        FabutReport report = new FabutReport();
        report.listDifferentSizeComment("myList", 5, 3);

        assertFalse(report.isSuccess());
        assertEquals("❌ LIST SIZE MISMATCH: myList expected size: 5, but was: 3", report.getMessage());
    }

    @Test
    void noPropertyForField_marksFailure() {
        FabutReport report = new FabutReport();
        report.noPropertyForField(new TestEntity(), "fieldName", "fieldValue");

        assertFalse(report.isSuccess());
        assertEquals("""
                ❌ UNASSERTED FIELD: TestEntity.fieldName = "fieldValue"
                    Fix: add value("fieldName", ...) or ignored("fieldName")""", report.getMessage());
    }

    @Test
    void notNullProperty_marksFailure() {
        FabutReport report = new FabutReport();
        report.notNullProperty("myField");

        assertFalse(report.isSuccess());
        assertEquals("❌ myField: expected not null, but was null", report.getMessage());
    }

    @Test
    void nullProperty_marksFailure() {
        FabutReport report = new FabutReport();
        report.nullProperty("myField", "actualValue");

        assertFalse(report.isSuccess());
        assertEquals("""
                ❌ myField: expected null, but was: "actualValue\"""", report.getMessage());
    }

    @Test
    void notEmptyProperty_marksFailure() {
        FabutReport report = new FabutReport();
        report.notEmptyProperty("myField", null);

        assertFalse(report.isSuccess());
        assertEquals("❌ myField: expected non-empty Optional, but was: null", report.getMessage());
    }

    @Test
    void emptyProperty_marksFailure() {
        FabutReport report = new FabutReport();
        report.emptyProperty("myField", Optional.of("value"));

        assertFalse(report.isSuccess());
        assertEquals("""
                ❌ myField: expected empty Optional, but was: Optional["value"]""", report.getMessage());
    }

    @Test
    void reportIgnoreProperty_staysSuccessful() {
        FabutReport report = new FabutReport();
        report.reportIgnoreProperty("ignoredField");

        assertTrue(report.isSuccess());
    }

    @Test
    void checkByReference_marksFailure() {
        FabutReport report = new FabutReport();
        report.checkByReference("field", new TestEntity());

        assertFalse(report.isSuccess());
        assertEquals("❌ Property:  field of class:  TestEntity has wrong reference.", report.getMessage());
    }

    @Test
    void ignoredType_staysSuccessful() {
        FabutReport report = new FabutReport();
        report.ignoredType(String.class);

        assertTrue(report.isSuccess());
    }

    @Test
    void assertingListElement_staysSuccessful() {
        FabutReport report = new FabutReport();
        report.assertingListElement("myList", 0);

        assertTrue(report.isSuccess());
    }

    @Test
    void noEntityInSnapshot_marksFailure() {
        FabutReport report = new FabutReport();
        report.noEntityInSnapshot(new TestEntity());

        assertFalse(report.isSuccess());
        assertEquals("""
                ❌ NOT IN SNAPSHOT: TestEntity
                    Entity was not present when takeSnapshot() was called.
                    Fix: call takeSnapshot() after creating this entity, or use assertObject() for new entities""",
                report.getMessage());
    }

    @Test
    void entityInSnapshot_marksFailure() {
        FabutReport report = new FabutReport();
        report.entityInSnapshot(new TestEntity());

        assertFalse(report.isSuccess());
        assertEquals("""
                ❌ ALREADY IN SNAPSHOT: TestEntity
                    This entity existed before takeSnapshot(). Use assertEntityWithSnapshot() instead of assertObject()""",
                report.getMessage());
    }

    @Test
    void entityNotAssertedInAfterState_marksFailure() {
        FabutReport report = new FabutReport();
        report.entityNotAssertedInAfterState(new TestEntity());

        assertFalse(report.isSuccess());
        assertEquals("""
                ❌ UNASSERTED ENTITY: TestEntity was created after takeSnapshot() but not asserted.
                    Fix: add assertObject(...) or ignoreEntity(...)""", report.getMessage());
    }

    @Test
    void uncallableMethod_marksFailure() throws NoSuchMethodException {
        FabutReport report = new FabutReport();
        Method method = String.class.getMethod("toString");
        report.uncallableMethod(method, new TestEntity());

        assertFalse(report.isSuccess());
        assertEquals("❌ There is no method: toString in actual object class: class cloud.alchemy.fabut.FabutReportTest$TestEntity (expected object class was: String).",
                report.getMessage());
    }

    @Test
    void notNecessaryAssert_marksFailure() {
        FabutReport report = new FabutReport();
        report.notNecessaryAssert("field", "ownerObject", "fieldValue");

        assertFalse(report.isSuccess());
        assertEquals("""
                ❌ UNNECESSARY ASSERT: String.field was not modified (value: "fieldValue")
                    Fix: remove this assertion, or verify the test action modifies this field""",
                report.getMessage());
    }

    @Test
    void nullReference_marksFailure() {
        FabutReport report = new FabutReport();
        report.nullReference();

        assertFalse(report.isSuccess());
        assertEquals("❌ Object that was passed to assertObject was null, it must not be null!", report.getMessage());
    }

    @Test
    void assertFail_marksFailure() {
        FabutReport report = new FabutReport();
        report.assertFail("field", "expected", "actual");

        assertFalse(report.isSuccess());
        assertEquals("❌ field: expected: expected but was: actual", report.getMessage());
    }

    @Test
    void assertFailFormatted_marksFailure() {
        FabutReport report = new FabutReport();
        report.assertFailFormatted("field", () -> "formatted_expected", () -> "formatted_actual");

        assertFalse(report.isSuccess());
        assertEquals("❌ field: expected: formatted_expected but was: formatted_actual", report.getMessage());
    }

    @Test
    void idNull_marksFailure() {
        FabutReport report = new FabutReport();
        report.idNull(TestEntity.class);

        assertFalse(report.isSuccess());
        assertEquals("❌ Id of TestEntity cannot be null", report.getMessage());
    }

    @Test
    void notDeletedInRepository_marksFailure() {
        FabutReport report = new FabutReport();
        report.notDeletedInRepository(new TestEntity());

        assertFalse(report.isSuccess());
        assertEquals("""
                ❌ NOT DELETED: TestEntity still exists in repository
                    Fix: verify the test action actually deletes this entity""",
                report.getMessage());
    }

    @Test
    void noCopy_marksFailure() {
        FabutReport report = new FabutReport();
        report.noCopy(new TestEntity());

        assertFalse(report.isSuccess());
        assertEquals("❌ Entity: TestEntity cannot be copied into snapshot", report.getMessage());
    }

    @Test
    void excessExpectedMap_marksFailure() {
        FabutReport report = new FabutReport();
        report.excessExpectedMap("key1");

        assertFalse(report.isSuccess());
        assertEquals("❌ No match for expected key: key1", report.getMessage());
    }

    @Test
    void excessActualMap_marksFailure() {
        FabutReport report = new FabutReport();
        report.excessActualMap("key2");

        assertFalse(report.isSuccess());
        assertEquals("❌ No match for actual key: key2", report.getMessage());
    }

    @Test
    void excessExpectedProperty_marksFailure() {
        FabutReport report = new FabutReport();
        report.excessExpectedProperty("some.path");

        assertFalse(report.isSuccess());
        assertEquals("""
                ❌ UNKNOWN PROPERTY: "some.path" does not match any field on the object""",
                report.getMessage());
    }

    @Test
    void excessExpectedProperty_withAvailableFields_suggestsClosestMatch() {
        FabutReport report = new FabutReport();
        report.excessExpectedProperty("valuDecimal", List.of("valueDecimal", "valueText", "priority"));

        assertFalse(report.isSuccess());
        assertEquals("""
                ❌ UNKNOWN PROPERTY: "valuDecimal" does not match any field
                    Did you mean: "valueDecimal"?
                    Available fields: valueDecimal, valueText, priority""",
                report.getMessage());
    }

    @Test
    void excessExpectedProperty_withAvailableFields_noCloseMatch() {
        FabutReport report = new FabutReport();
        report.excessExpectedProperty("xyz", List.of("valueDecimal", "valueText", "priority"));

        assertFalse(report.isSuccess());
        assertEquals("""
                ❌ UNKNOWN PROPERTY: "xyz" does not match any field
                    Available fields: valueDecimal, valueText, priority""",
                report.getMessage());
    }

    @Test
    void findClosestMatch_exactMatch_returnsIt() {
        assertEquals("name", FabutReport.findClosestMatch("name", List.of("name", "age")));
    }

    @Test
    void findClosestMatch_typo_suggestsCorrection() {
        assertEquals("valueDecimal", FabutReport.findClosestMatch("valuDecimal", List.of("valueDecimal", "valueText")));
    }

    @Test
    void findClosestMatch_tooFar_returnsNull() {
        assertNull(FabutReport.findClosestMatch("xyz", List.of("valueDecimal", "valueText")));
    }

    @Test
    void findClosestMatch_emptyList_returnsNull() {
        assertNull(FabutReport.findClosestMatch("name", List.of()));
    }

    @Test
    void assertingMapKey_staysSuccessful() {
        FabutReport report = new FabutReport();
        report.assertingMapKey("mapKey");

        assertTrue(report.isSuccess());
    }

    @Test
    void assertWithSnapshotMustHaveAtLeastOnChange_marksFailure() {
        FabutReport report = new FabutReport();
        report.assertWithSnapshotMustHaveAtLeastOnChange(new TestEntity());

        assertFalse(report.isSuccess());
        assertEquals("""
                ❌ assertEntityWithSnapshot() called with 0 property assertions on: TestEntity
                    Fix: specify which fields changed, or use ignoreEntity() if no changes expected""",
                report.getMessage());
    }

    // ==================== Code Methods Tests ====================

    @Test
    void addCode_addsCodeToReport() {
        FabutReport report = new FabutReport();
        report.addCode(() -> "generated code");
        report.markAsFailed();

        assertEquals("\nCODE:generated code", report.getMessage());
    }

    @Test
    void addCode_nullCode_doesNothing() {
        FabutReport report = new FabutReport();
        report.addCode(null);

        assertTrue(report.isSuccess());
    }

    // ==================== Assertion Context Tests ====================

    @Test
    void getAssertionContext_defaultIsNewObject() {
        FabutReport report = new FabutReport();
        assertEquals(AssertionContext.NEW_OBJECT, report.getAssertionContext());
    }

    @Test
    void setAssertionContext_updatesContext() {
        FabutReport report = new FabutReport();
        report.setAssertionContext(AssertionContext.ENTITY_WITH_SNAPSHOT);

        assertEquals(AssertionContext.ENTITY_WITH_SNAPSHOT, report.getAssertionContext());
    }

    // ==================== Entity Changes Tests ====================

    @Test
    void recordEntityChange_marksFailure() {
        FabutReport report = new FabutReport();
        report.recordEntityChange(EntityChangeType.CREATED, "Entity#1", TestEntity.class, "details", "fix");

        assertFalse(report.isSuccess());
        assertTrue(report.hasEntityChanges());
    }

    @Test
    void recordEntityChange_withCode_includesCode() {
        FabutReport report = new FabutReport();
        report.recordEntityChange(EntityChangeType.CREATED, "Entity#1", TestEntity.class, "details", "fix", "\nsome code");

        assertEquals("""
                SNAPSHOT VIOLATION: 1 created
                ============================================================
                CREATED:
                  Entity#1
                some code""", report.getEntityChangesMessage());
    }

    @Test
    void hasEntityChanges_noChanges_returnsFalse() {
        FabutReport report = new FabutReport();
        assertFalse(report.hasEntityChanges());
    }

    @Test
    void getEntityChangesMessage_noChanges_returnsEmpty() {
        FabutReport report = new FabutReport();
        assertEquals("", report.getEntityChangesMessage());
    }

    @Test
    void getEntityChangesMessage_withDeletedChange() {
        FabutReport report = new FabutReport();
        report.recordEntityChange(EntityChangeType.DELETED, "Entity#1", TestEntity.class, "deleted", "assertEntityAsDeleted(entity);");

        assertEquals("""
                SNAPSHOT VIOLATION: 1 deleted
                ============================================================
                DELETED:
                  Entity#1
                    -> assertEntityAsDeleted(entity);""", report.getEntityChangesMessage());
    }

    // ==================== markAsFailed() Tests ====================

    @Test
    void markAsFailed_changesSuccess() {
        FabutReport report = new FabutReport();
        assertTrue(report.isSuccess());

        report.markAsFailed();
        assertFalse(report.isSuccess());
    }

    // ==================== getMessage() Tests ====================

    @Test
    void getMessage_emptyReport_returnsEmptyString() {
        FabutReport report = new FabutReport();
        assertEquals("", report.getMessage());
    }

    @Test
    void getMessage_withNestedSubreports_formatsCorrectly() {
        FabutReport parent = new FabutReport();
        FabutReport child = parent.getSubReport(() -> "Child");
        child.assertFail("field", "exp", "act");

        assertEquals("""
                Child
                --❌ field: expected: exp but was: act""", parent.getMessage());
    }

    // Helper class
    static class TestEntity {
        @Override
        public String toString() {
            return "TestEntity";
        }
    }
}
