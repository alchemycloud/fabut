package cloud.alchemy.fabut.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for enum types to ensure coverage and validate enum values.
 */
@SuppressWarnings("deprecation")
public class EnumTypesTest {

    // ==================== AssertType Tests ====================

    @Test
    void assertType_hasAllExpectedValues() {
        AssertType[] values = AssertType.values();

        assertEquals(3, values.length);
        assertNotNull(AssertType.valueOf("REPOSITORY_ASSERT"));
        assertNotNull(AssertType.valueOf("OBJECT_ASSERT"));
        assertNotNull(AssertType.valueOf("UNSUPPORTED_ASSERT"));
    }

    @Test
    void assertType_repositoryAssert_exists() {
        AssertType type = AssertType.REPOSITORY_ASSERT;
        assertEquals("REPOSITORY_ASSERT", type.name());
        assertEquals(0, type.ordinal());
    }

    @Test
    void assertType_objectAssert_exists() {
        AssertType type = AssertType.OBJECT_ASSERT;
        assertEquals("OBJECT_ASSERT", type.name());
        assertEquals(1, type.ordinal());
    }

    @Test
    void assertType_unsupportedAssert_exists() {
        AssertType type = AssertType.UNSUPPORTED_ASSERT;
        assertEquals("UNSUPPORTED_ASSERT", type.name());
        assertEquals(2, type.ordinal());
    }

    // ==================== AssertableType Tests ====================

    @Test
    void assertableType_hasAllExpectedValues() {
        AssertableType[] values = AssertableType.values();

        assertEquals(7, values.length);
        assertNotNull(AssertableType.valueOf("COMPLEX_TYPE"));
        assertNotNull(AssertableType.valueOf("IGNORED_TYPE"));
        assertNotNull(AssertableType.valueOf("ENTITY_TYPE"));
        assertNotNull(AssertableType.valueOf("PRIMITIVE_TYPE"));
        assertNotNull(AssertableType.valueOf("LIST_TYPE"));
        assertNotNull(AssertableType.valueOf("MAP_TYPE"));
        assertNotNull(AssertableType.valueOf("OPTIONAL_TYPE"));
    }

    @Test
    void assertableType_complexType_exists() {
        AssertableType type = AssertableType.COMPLEX_TYPE;
        assertEquals("COMPLEX_TYPE", type.name());
        assertEquals(0, type.ordinal());
    }

    @Test
    void assertableType_ignoredType_exists() {
        AssertableType type = AssertableType.IGNORED_TYPE;
        assertEquals("IGNORED_TYPE", type.name());
        assertEquals(1, type.ordinal());
    }

    @Test
    void assertableType_entityType_exists() {
        AssertableType type = AssertableType.ENTITY_TYPE;
        assertEquals("ENTITY_TYPE", type.name());
        assertEquals(2, type.ordinal());
    }

    @Test
    void assertableType_primitiveType_exists() {
        AssertableType type = AssertableType.PRIMITIVE_TYPE;
        assertEquals("PRIMITIVE_TYPE", type.name());
        assertEquals(3, type.ordinal());
    }

    @Test
    void assertableType_listType_exists() {
        AssertableType type = AssertableType.LIST_TYPE;
        assertEquals("LIST_TYPE", type.name());
        assertEquals(4, type.ordinal());
    }

    @Test
    void assertableType_mapType_exists() {
        AssertableType type = AssertableType.MAP_TYPE;
        assertEquals("MAP_TYPE", type.name());
        assertEquals(5, type.ordinal());
    }

    @Test
    void assertableType_optionalType_exists() {
        AssertableType type = AssertableType.OPTIONAL_TYPE;
        assertEquals("OPTIONAL_TYPE", type.name());
        assertEquals(6, type.ordinal());
    }

    // ==================== Enum valueOf Exception Tests ====================

    @Test
    void assertType_invalidValue_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> AssertType.valueOf("INVALID"));
    }

    @Test
    void assertableType_invalidValue_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> AssertableType.valueOf("INVALID"));
    }
}
