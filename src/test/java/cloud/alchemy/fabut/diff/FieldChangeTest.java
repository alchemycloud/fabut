package cloud.alchemy.fabut.diff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FieldChange class covering all change types and methods.
 */
public class FieldChangeTest {

    // ==================== compare() Tests ====================

    @Test
    void compare_equalValues_returnsUnchanged() {
        FieldChange change = FieldChange.compare("field", "value", "value");

        assertEquals("field", change.fieldName());
        assertEquals("value", change.beforeValue());
        assertEquals("value", change.afterValue());
        assertEquals(FieldChange.ChangeType.UNCHANGED, change.changeType());
    }

    @Test
    void compare_bothNull_returnsUnchanged() {
        FieldChange change = FieldChange.compare("field", null, null);

        assertEquals(FieldChange.ChangeType.UNCHANGED, change.changeType());
        assertNull(change.beforeValue());
        assertNull(change.afterValue());
    }

    @Test
    void compare_nullToValue_returnsSet() {
        FieldChange change = FieldChange.compare("field", null, "newValue");

        assertEquals(FieldChange.ChangeType.SET, change.changeType());
        assertNull(change.beforeValue());
        assertEquals("newValue", change.afterValue());
    }

    @Test
    void compare_valueToNull_returnsCleared() {
        FieldChange change = FieldChange.compare("field", "oldValue", null);

        assertEquals(FieldChange.ChangeType.CLEARED, change.changeType());
        assertEquals("oldValue", change.beforeValue());
        assertNull(change.afterValue());
    }

    @Test
    void compare_differentValues_returnsModified() {
        FieldChange change = FieldChange.compare("field", "old", "new");

        assertEquals(FieldChange.ChangeType.MODIFIED, change.changeType());
        assertEquals("old", change.beforeValue());
        assertEquals("new", change.afterValue());
    }

    @Test
    void compare_equalIntegers_returnsUnchanged() {
        FieldChange change = FieldChange.compare("count", 42, 42);

        assertEquals(FieldChange.ChangeType.UNCHANGED, change.changeType());
    }

    @Test
    void compare_differentIntegers_returnsModified() {
        FieldChange change = FieldChange.compare("count", 10, 20);

        assertEquals(FieldChange.ChangeType.MODIFIED, change.changeType());
        assertEquals(10, change.beforeValue());
        assertEquals(20, change.afterValue());
    }

    // ==================== isChanged() Tests ====================

    @Test
    void isChanged_unchanged_returnsFalse() {
        FieldChange change = FieldChange.compare("field", "same", "same");

        assertFalse(change.isChanged());
    }

    @Test
    void isChanged_modified_returnsTrue() {
        FieldChange change = FieldChange.compare("field", "old", "new");

        assertTrue(change.isChanged());
    }

    @Test
    void isChanged_set_returnsTrue() {
        FieldChange change = FieldChange.compare("field", null, "value");

        assertTrue(change.isChanged());
    }

    @Test
    void isChanged_cleared_returnsTrue() {
        FieldChange change = FieldChange.compare("field", "value", null);

        assertTrue(change.isChanged());
    }

    // ==================== describe() Tests ====================

    @Test
    void describe_unchanged_containsUnchanged() {
        FieldChange change = FieldChange.compare("name", "John", "John");

        String description = change.describe();
        assertTrue(description.contains("name"));
        assertTrue(description.contains("unchanged"));
        assertTrue(description.contains("\"John\""));
    }

    @Test
    void describe_modified_showsBeforeAndAfter() {
        FieldChange change = FieldChange.compare("name", "John", "Jane");

        String description = change.describe();
        assertTrue(description.contains("name"));
        assertTrue(description.contains("\"John\""));
        assertTrue(description.contains("\"Jane\""));
        assertTrue(description.contains("->"));
    }

    @Test
    void describe_set_showsNullToValue() {
        FieldChange change = FieldChange.compare("name", null, "John");

        String description = change.describe();
        assertTrue(description.contains("null"));
        assertTrue(description.contains("\"John\""));
    }

    @Test
    void describe_cleared_showsValueToNull() {
        FieldChange change = FieldChange.compare("name", "John", null);

        String description = change.describe();
        assertTrue(description.contains("\"John\""));
        assertTrue(description.contains("null"));
    }

    @Test
    void describe_integerValue_notQuoted() {
        FieldChange change = FieldChange.compare("count", 10, 20);

        String description = change.describe();
        assertTrue(description.contains("10"));
        assertTrue(description.contains("20"));
        assertFalse(description.contains("\"10\""));
    }

    @Test
    void describe_nullValueUnchanged_showsNull() {
        FieldChange change = FieldChange.compare("field", null, null);

        String description = change.describe();
        assertTrue(description.contains("null"));
        assertTrue(description.contains("unchanged"));
    }

    // ==================== Record accessor Tests ====================

    @Test
    void recordAccessors_returnCorrectValues() {
        FieldChange change = new FieldChange("myField", "before", "after", FieldChange.ChangeType.MODIFIED);

        assertEquals("myField", change.fieldName());
        assertEquals("before", change.beforeValue());
        assertEquals("after", change.afterValue());
        assertEquals(FieldChange.ChangeType.MODIFIED, change.changeType());
    }

    // ==================== ChangeType enum Tests ====================

    @Test
    void changeType_allValuesExist() {
        FieldChange.ChangeType[] types = FieldChange.ChangeType.values();

        assertEquals(4, types.length);
        assertNotNull(FieldChange.ChangeType.valueOf("MODIFIED"));
        assertNotNull(FieldChange.ChangeType.valueOf("SET"));
        assertNotNull(FieldChange.ChangeType.valueOf("CLEARED"));
        assertNotNull(FieldChange.ChangeType.valueOf("UNCHANGED"));
    }
}
