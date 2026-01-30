package cloud.alchemy.fabut.property;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for property type classes: EmptyProperty, NotEmptyProperty,
 * IgnoredProperty, NullProperty, NotNullProperty, and Property.
 */
public class PropertyTypesTest {

    // ==================== EmptyProperty Tests ====================

    @Test
    void emptyProperty_constructor_setsPath() {
        EmptyProperty prop = new EmptyProperty("field.name");
        assertEquals("field.name", prop.getPath());
    }

    @Test
    void emptyProperty_getCopy_returnsNewInstance() {
        EmptyProperty original = new EmptyProperty("test.path");
        ISingleProperty copy = original.getCopy();

        assertNotSame(original, copy);
        assertInstanceOf(EmptyProperty.class, copy);
        assertEquals(original.getPath(), copy.getPath());
    }

    // ==================== NotEmptyProperty Tests ====================

    @Test
    void notEmptyProperty_constructor_setsPath() {
        NotEmptyProperty prop = new NotEmptyProperty("field.name");
        assertEquals("field.name", prop.getPath());
    }

    @Test
    void notEmptyProperty_getCopy_returnsNewInstance() {
        NotEmptyProperty original = new NotEmptyProperty("test.path");
        ISingleProperty copy = original.getCopy();

        assertNotSame(original, copy);
        assertInstanceOf(NotEmptyProperty.class, copy);
        assertEquals(original.getPath(), copy.getPath());
    }

    // ==================== IgnoredProperty Tests ====================

    @Test
    void ignoredProperty_constructor_setsPath() {
        IgnoredProperty prop = new IgnoredProperty("ignored.field");
        assertEquals("ignored.field", prop.getPath());
    }

    @Test
    void ignoredProperty_getCopy_returnsNewInstance() {
        IgnoredProperty original = new IgnoredProperty("test.path");
        ISingleProperty copy = original.getCopy();

        assertNotSame(original, copy);
        assertInstanceOf(IgnoredProperty.class, copy);
        assertEquals(original.getPath(), copy.getPath());
    }

    // ==================== NullProperty Tests ====================

    @Test
    void nullProperty_constructor_setsPath() {
        NullProperty prop = new NullProperty("null.field");
        assertEquals("null.field", prop.getPath());
    }

    @Test
    void nullProperty_getCopy_returnsNewInstance() {
        NullProperty original = new NullProperty("test.path");
        ISingleProperty copy = original.getCopy();

        assertNotSame(original, copy);
        assertInstanceOf(NullProperty.class, copy);
        assertEquals(original.getPath(), copy.getPath());
    }

    // ==================== NotNullProperty Tests ====================

    @Test
    void notNullProperty_constructor_setsPath() {
        NotNullProperty prop = new NotNullProperty("notNull.field");
        assertEquals("notNull.field", prop.getPath());
    }

    @Test
    void notNullProperty_getCopy_returnsNewInstance() {
        NotNullProperty original = new NotNullProperty("test.path");
        ISingleProperty copy = original.getCopy();

        assertNotSame(original, copy);
        assertInstanceOf(NotNullProperty.class, copy);
        assertEquals(original.getPath(), copy.getPath());
    }

    // ==================== Property Tests ====================

    @Test
    void property_constructor_setsPathAndValue() {
        Property<String> prop = new Property<>("name", "testValue");
        assertEquals("name", prop.getPath());
        assertEquals("testValue", prop.getValue());
    }

    @Test
    void property_constructor_allowsNullValue() {
        Property<String> prop = new Property<>("name", null);
        assertEquals("name", prop.getPath());
        assertNull(prop.getValue());
    }

    @Test
    void property_getCopy_returnsNewInstanceWithSameValue() {
        Property<Integer> original = new Property<>("count", 42);
        ISingleProperty copy = original.getCopy();

        assertNotSame(original, copy);
        assertInstanceOf(Property.class, copy);
        assertEquals(original.getPath(), copy.getPath());
        assertEquals(original.getValue(), ((Property<?>) copy).getValue());
    }

    @Test
    void property_getDescription_includesPathAndValue() {
        Property<String> prop = new Property<>("field", "value");
        String description = prop.getDescription();

        assertTrue(description.contains("Property"));
        assertTrue(description.contains("field"));
        assertTrue(description.contains("value"));
    }

    @Test
    void property_getDescription_handlesNullValue() {
        Property<String> prop = new Property<>("field", null);
        String description = prop.getDescription();

        assertTrue(description.contains("null"));
    }

    @Test
    void property_equals_samePathAndValue_returnsTrue() {
        Property<String> prop1 = new Property<>("path", "value");
        Property<String> prop2 = new Property<>("path", "value");

        assertEquals(prop1, prop2);
    }

    @Test
    void property_equals_differentPath_returnsFalse() {
        Property<String> prop1 = new Property<>("path1", "value");
        Property<String> prop2 = new Property<>("path2", "value");

        assertNotEquals(prop1, prop2);
    }

    @Test
    void property_equals_differentValue_returnsFalse() {
        Property<String> prop1 = new Property<>("path", "value1");
        Property<String> prop2 = new Property<>("path", "value2");

        assertNotEquals(prop1, prop2);
    }

    @Test
    void property_equals_nullValue_handledCorrectly() {
        Property<String> prop1 = new Property<>("path", null);
        Property<String> prop2 = new Property<>("path", null);

        assertEquals(prop1, prop2);
    }

    @Test
    void property_equals_oneNullValue_returnsFalse() {
        Property<String> prop1 = new Property<>("path", "value");
        Property<String> prop2 = new Property<>("path", null);

        assertNotEquals(prop1, prop2);
    }

    @Test
    void property_equals_differentType_returnsFalse() {
        Property<String> prop = new Property<>("path", "value");

        assertNotEquals(prop, "not a property");
        assertNotEquals(prop, null);
    }

    @Test
    void property_hashCode_sameForEqualProperties() {
        Property<String> prop1 = new Property<>("path", "value");
        Property<String> prop2 = new Property<>("path", "value");

        assertEquals(prop1.hashCode(), prop2.hashCode());
    }

    @Test
    void property_hashCode_differentForDifferentProperties() {
        Property<String> prop1 = new Property<>("path1", "value1");
        Property<String> prop2 = new Property<>("path2", "value2");

        assertNotEquals(prop1.hashCode(), prop2.hashCode());
    }

    // ==================== Cross-type Tests ====================

    @Test
    void samePropertyTypes_samePath_areEqual() {
        EmptyProperty empty1 = new EmptyProperty("path");
        EmptyProperty empty2 = new EmptyProperty("path");

        assertEquals(empty1, empty2);
    }

    @Test
    void samePropertyTypes_differentPath_notEqual() {
        EmptyProperty empty1 = new EmptyProperty("path1");
        EmptyProperty empty2 = new EmptyProperty("path2");

        assertNotEquals(empty1, empty2);
    }

    @Test
    void propertyTypes_areAllSingleProperties() {
        assertTrue(new EmptyProperty("path") instanceof ISingleProperty);
        assertTrue(new NotEmptyProperty("path") instanceof ISingleProperty);
        assertTrue(new NullProperty("path") instanceof ISingleProperty);
        assertTrue(new NotNullProperty("path") instanceof ISingleProperty);
        assertTrue(new IgnoredProperty("path") instanceof ISingleProperty);
        assertTrue(new Property<>("path", "value") instanceof ISingleProperty);
    }
}
