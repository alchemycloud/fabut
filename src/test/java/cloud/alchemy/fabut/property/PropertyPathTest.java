package cloud.alchemy.fabut.property;

import cloud.alchemy.fabut.Fabut;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PropertyPath} backward compatibility.
 * Verifies that the v4 PropertyPath-based API still works.
 */
public class PropertyPathTest extends Fabut {

    // PropertyPath constants (simulating v4 model classes)
    private static final PropertyPath<String> NAME = new PropertyPath<>("name");
    private static final PropertyPath<Integer> ID = new PropertyPath<>("id");
    private static final PropertyPath<String> ADDRESS = new PropertyPath<>("address");

    @Test
    public void testPropertyPathGetPath() {
        assertEquals("name", NAME.getPath());
        assertEquals("id", ID.getPath());
    }

    @Test
    public void testPropertyPathChain() {
        PropertyPath<String> chained = NAME.chain(ADDRESS);
        assertEquals("name.address", chained.getPath());
    }

    @Test
    public void testPropertyPathAppend() {
        PropertyPath<String> appended = NAME.append("street");
        assertEquals("name.street", appended.getPath());
    }

    @Test
    public void testPropertyPathEquals() {
        PropertyPath<String> name1 = new PropertyPath<>("name");
        PropertyPath<String> name2 = new PropertyPath<>("name");
        PropertyPath<String> other = new PropertyPath<>("other");

        assertEquals(name1, name2);
        assertNotEquals(name1, other);
    }

    @Test
    public void testPropertyPathHashCode() {
        PropertyPath<String> name1 = new PropertyPath<>("name");
        PropertyPath<String> name2 = new PropertyPath<>("name");

        assertEquals(name1.hashCode(), name2.hashCode());
    }

    @Test
    public void testPropertyPathToString() {
        assertEquals("name", NAME.toString());
    }

    // ==================== Fabut API with PropertyPath ====================

    @Test
    public void testIgnoredWithPropertyPath() {
        PropertyPath<?>[] properties = new PropertyPath[] {NAME, ID};

        MultiProperties multi = ignored(properties);

        assertEquals(properties.length, multi.getProperties().size());
        for (int i = 0; i < properties.length; i++) {
            assertInstanceOf(IgnoredProperty.class, multi.getProperties().get(i));
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

    @Test
    public void testIsNullWithPropertyPath() {
        PropertyPath<?>[] properties = new PropertyPath[] {NAME, ID};

        MultiProperties multi = isNull(properties);

        assertEquals(properties.length, multi.getProperties().size());
        for (int i = 0; i < properties.length; i++) {
            assertInstanceOf(NullProperty.class, multi.getProperties().get(i));
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

    @Test
    public void testNotNullWithPropertyPath() {
        PropertyPath<?>[] properties = new PropertyPath[] {NAME, ID};

        MultiProperties multi = notNull(properties);

        assertEquals(properties.length, multi.getProperties().size());
        for (int i = 0; i < properties.length; i++) {
            assertInstanceOf(NotNullProperty.class, multi.getProperties().get(i));
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

    @Test
    public void testValueWithPropertyPath() {
        Property<String> property = value(NAME, "test");

        assertEquals("name", property.getPath());
        assertEquals("test", property.getValue());
    }

    @Test
    public void testSingleIgnoredWithPropertyPath() {
        IgnoredProperty property = ignored(NAME);

        assertEquals("name", property.getPath());
    }

    @Test
    public void testSingleNotNullWithPropertyPath() {
        NotNullProperty property = notNull(NAME);

        assertEquals("name", property.getPath());
    }

    @Test
    public void testSingleIsNullWithPropertyPath() {
        NullProperty property = isNull(NAME);

        assertEquals("name", property.getPath());
    }

    @Test
    public void testNotEmptyWithPropertyPath() {
        NotEmptyProperty property = notEmpty(NAME);

        assertEquals("name", property.getPath());
    }

    @Test
    public void testIsEmptyWithPropertyPath() {
        EmptyProperty property = isEmpty(NAME);

        assertEquals("name", property.getPath());
    }

    @Test
    public void testNotEmptyVarargsWithPropertyPath() {
        PropertyPath<?>[] properties = new PropertyPath[] {NAME, ID};

        MultiProperties multi = notEmpty(properties);

        assertEquals(properties.length, multi.getProperties().size());
        for (int i = 0; i < properties.length; i++) {
            assertInstanceOf(NotEmptyProperty.class, multi.getProperties().get(i));
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

    @Test
    public void testIsEmptyVarargsWithPropertyPath() {
        PropertyPath<?>[] properties = new PropertyPath[] {NAME, ID};

        MultiProperties multi = isEmpty(properties);

        assertEquals(properties.length, multi.getProperties().size());
        for (int i = 0; i < properties.length; i++) {
            assertInstanceOf(EmptyProperty.class, multi.getProperties().get(i));
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }
}
