package cloud.alchemy.fabut;

import cloud.alchemy.fabut.enums.AssertionContext;
import cloud.alchemy.fabut.enums.EntityChangeType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

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
        String message = report.getMessage();
        assertTrue(message.contains("myList"));
        assertTrue(message.contains("5"));
        assertTrue(message.contains("3"));
    }

    @Test
    void noPropertyForField_marksFailure() {
        FabutReport report = new FabutReport();
        report.noPropertyForField(new TestEntity(), "fieldName", "fieldValue");

        assertFalse(report.isSuccess());
        String message = report.getMessage();
        assertTrue(message.contains("fieldName"));
        assertTrue(message.contains("TestEntity"));
    }

    @Test
    void notNullProperty_marksFailure() {
        FabutReport report = new FabutReport();
        report.notNullProperty("myField");

        assertFalse(report.isSuccess());
        assertTrue(report.getMessage().contains("myField"));
    }

    @Test
    void nullProperty_marksFailure() {
        FabutReport report = new FabutReport();
        report.nullProperty("myField");

        assertFalse(report.isSuccess());
        assertTrue(report.getMessage().contains("myField"));
    }

    @Test
    void notEmptyProperty_marksFailure() {
        FabutReport report = new FabutReport();
        report.notEmptyProperty("myField");

        assertFalse(report.isSuccess());
        assertTrue(report.getMessage().contains("myField"));
    }

    @Test
    void emptyProperty_marksFailure() {
        FabutReport report = new FabutReport();
        report.emptyProperty("myField");

        assertFalse(report.isSuccess());
        assertTrue(report.getMessage().contains("myField"));
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
        assertTrue(report.getMessage().contains("wrong reference"));
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
    }

    @Test
    void entityInSnapshot_marksFailure() {
        FabutReport report = new FabutReport();
        report.entityInSnapshot(new TestEntity());

        assertFalse(report.isSuccess());
    }

    @Test
    void entityNotAssertedInAfterState_marksFailure() {
        FabutReport report = new FabutReport();
        report.entityNotAssertedInAfterState(new TestEntity());

        assertFalse(report.isSuccess());
    }

    @Test
    void uncallableMethod_marksFailure() throws NoSuchMethodException {
        FabutReport report = new FabutReport();
        Method method = String.class.getMethod("toString");
        report.uncallableMethod(method, new TestEntity());

        assertFalse(report.isSuccess());
    }

    @Test
    void notNecessaryAssert_marksFailure() {
        FabutReport report = new FabutReport();
        report.notNecessaryAssert("field", "value");

        assertFalse(report.isSuccess());
    }

    @Test
    void nullReference_marksFailure() {
        FabutReport report = new FabutReport();
        report.nullReference();

        assertFalse(report.isSuccess());
    }

    @Test
    void assertFail_marksFailure() {
        FabutReport report = new FabutReport();
        report.assertFail("field", "expected", "actual");

        assertFalse(report.isSuccess());
        String message = report.getMessage();
        assertTrue(message.contains("field"));
        assertTrue(message.contains("expected"));
        assertTrue(message.contains("actual"));
    }

    @Test
    void assertFailFormatted_marksFailure() {
        FabutReport report = new FabutReport();
        report.assertFailFormatted("field", () -> "formatted_expected", () -> "formatted_actual");

        assertFalse(report.isSuccess());
        String message = report.getMessage();
        assertTrue(message.contains("formatted_expected"));
        assertTrue(message.contains("formatted_actual"));
    }

    @Test
    void idNull_marksFailure() {
        FabutReport report = new FabutReport();
        report.idNull(TestEntity.class);

        assertFalse(report.isSuccess());
        assertTrue(report.getMessage().contains("TestEntity"));
    }

    @Test
    void notDeletedInRepository_marksFailure() {
        FabutReport report = new FabutReport();
        report.notDeletedInRepository(new TestEntity());

        assertFalse(report.isSuccess());
    }

    @Test
    void noCopy_marksFailure() {
        FabutReport report = new FabutReport();
        report.noCopy(new TestEntity());

        assertFalse(report.isSuccess());
    }

    @Test
    void excessExpectedMap_marksFailure() {
        FabutReport report = new FabutReport();
        report.excessExpectedMap("key1");

        assertFalse(report.isSuccess());
    }

    @Test
    void excessActualMap_marksFailure() {
        FabutReport report = new FabutReport();
        report.excessActualMap("key2");

        assertFalse(report.isSuccess());
    }

    @Test
    void excessExpectedProperty_marksFailure() {
        FabutReport report = new FabutReport();
        report.excessExpectedProperty("some.path");

        assertFalse(report.isSuccess());
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
    }

    // ==================== Code Methods Tests ====================

    @Test
    void addCode_addsCodeToReport() {
        FabutReport report = new FabutReport();
        report.addCode(() -> "generated code");
        report.markAsFailed();

        String message = report.getMessage();
        assertTrue(message.contains("CODE"));
        assertTrue(message.contains("generated code"));
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
        report.recordEntityChange(EntityChangeType.CREATED, "Entity#1", TestEntity.class, "details", "fix", "generated code");

        String changes = report.getEntityChangesMessage();
        assertTrue(changes.contains("generated code"));
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
    void getEntityChangesMessage_withChanges_includesChangeType() {
        FabutReport report = new FabutReport();
        report.recordEntityChange(EntityChangeType.DELETED, "Entity#1", TestEntity.class, "deleted", "remove assertion");

        String message = report.getEntityChangesMessage();
        assertTrue(message.contains("Entity#1"));
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

        String message = parent.getMessage();
        assertTrue(message.contains("Child"));
        assertTrue(message.contains("field"));
    }

    // Helper class
    static class TestEntity {
        @Override
        public String toString() {
            return "TestEntity";
        }
    }
}
