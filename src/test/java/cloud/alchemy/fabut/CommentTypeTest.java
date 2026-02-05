package cloud.alchemy.fabut;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CommentType enum.
 */
public class CommentTypeTest {

    // ==================== Values Tests ====================

    @Test
    void values_containsAllExpectedTypes() {
        CommentType[] types = CommentType.values();

        assertEquals(4, types.length);
        assertNotNull(CommentType.valueOf("FAIL"));
        assertNotNull(CommentType.valueOf("SUCCESS"));
        assertNotNull(CommentType.valueOf("IGNORED"));
        assertNotNull(CommentType.valueOf("COLLECTION"));
    }

    // ==================== getMark() Tests ====================

    @Test
    void getMark_fail_returnsSquare() {
        assertEquals("❌", CommentType.FAIL.getMark());
    }

    @Test
    void getMark_success_returnsGreaterThan() {
        assertEquals("✅", CommentType.SUCCESS.getMark());
    }

    @Test
    void getMark_ignored_returnsSkipEmoji() {
        assertEquals("⏭", CommentType.IGNORED.getMark());
    }

    @Test
    void getMark_collection_returnsHash() {
        assertEquals("\uD83D\uDCCB", CommentType.COLLECTION.getMark());
    }

    // ==================== isFailure() Tests ====================

    @Test
    void isFailure_fail_returnsTrue() {
        assertTrue(CommentType.FAIL.isFailure());
    }

    @Test
    void isFailure_success_returnsFalse() {
        assertFalse(CommentType.SUCCESS.isFailure());
    }

    @Test
    void isFailure_ignored_returnsFalse() {
        assertFalse(CommentType.IGNORED.isFailure());
    }

    @Test
    void isFailure_collection_returnsFalse() {
        assertFalse(CommentType.COLLECTION.isFailure());
    }

    // ==================== isSuccess() Tests ====================

    @Test
    void isSuccess_success_returnsTrue() {
        assertTrue(CommentType.SUCCESS.isSuccess());
    }

    @Test
    void isSuccess_fail_returnsFalse() {
        assertFalse(CommentType.FAIL.isSuccess());
    }

    @Test
    void isSuccess_ignored_returnsFalse() {
        assertFalse(CommentType.IGNORED.isSuccess());
    }

    @Test
    void isSuccess_collection_returnsFalse() {
        assertFalse(CommentType.COLLECTION.isSuccess());
    }

    // ==================== isCollection() Tests ====================

    @Test
    void isCollection_collection_returnsTrue() {
        assertTrue(CommentType.COLLECTION.isCollection());
    }

    @Test
    void isCollection_fail_returnsFalse() {
        assertFalse(CommentType.FAIL.isCollection());
    }

    @Test
    void isCollection_ignored_returnsFalse() {
        assertFalse(CommentType.IGNORED.isCollection());
    }

    @Test
    void isCollection_success_returnsFalse() {
        assertFalse(CommentType.SUCCESS.isCollection());
    }

    // ==================== isIgnored() Tests ====================

    @Test
    void isIgnored_ignored_returnsTrue() {
        assertTrue(CommentType.IGNORED.isIgnored());
    }

    @Test
    void isIgnored_fail_returnsFalse() {
        assertFalse(CommentType.FAIL.isIgnored());
    }

    @Test
    void isIgnored_success_returnsFalse() {
        assertFalse(CommentType.SUCCESS.isIgnored());
    }

    @Test
    void isIgnored_collection_returnsFalse() {
        assertFalse(CommentType.COLLECTION.isIgnored());
    }

    // ==================== Enum Properties Tests ====================

    @Test
    void ordinal_valuesAreCorrect() {
        assertEquals(0, CommentType.FAIL.ordinal());
        assertEquals(1, CommentType.SUCCESS.ordinal());
        assertEquals(2, CommentType.IGNORED.ordinal());
        assertEquals(3, CommentType.COLLECTION.ordinal());
    }

    @Test
    void valueOf_invalidValue_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> CommentType.valueOf("INVALID"));
    }
}
