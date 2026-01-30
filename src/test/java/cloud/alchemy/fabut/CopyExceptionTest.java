package cloud.alchemy.fabut;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CopyException class.
 */
public class CopyExceptionTest {

    @Test
    void constructor_setsNameAndMessage() {
        CopyException ex = new CopyException("TestEntity");

        assertEquals("TestEntity", ex.getCopyFailName());
        assertTrue(ex.getMessage().contains("TestEntity"));
        assertTrue(ex.getMessage().contains("Failed to copy entity"));
    }

    @Test
    void constructor_withCause_setsNameMessageAndCause() {
        RuntimeException cause = new RuntimeException("Original error");
        CopyException ex = new CopyException("TestEntity", cause);

        assertEquals("TestEntity", ex.getCopyFailName());
        assertTrue(ex.getMessage().contains("TestEntity"));
        assertSame(cause, ex.getCause());
    }

    @Test
    void constructor_nullName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CopyException(null));
    }

    @Test
    void constructor_withCause_nullName_throwsNullPointerException() {
        RuntimeException cause = new RuntimeException("Original error");
        assertThrows(NullPointerException.class, () -> new CopyException(null, cause));
    }

    @Test
    void getCopyFailName_returnsCorrectName() {
        CopyException ex = new CopyException("MyEntity");

        assertEquals("MyEntity", ex.getCopyFailName());
    }

    @Test
    void exception_isCheckedExceptionType() {
        CopyException ex = new CopyException("Test");

        assertInstanceOf(Exception.class, ex);
        assertFalse(RuntimeException.class.isAssignableFrom(CopyException.class));
    }
}
