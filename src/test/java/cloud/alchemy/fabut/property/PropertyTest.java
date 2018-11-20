package cloud.alchemy.fabut.property;

import cloud.alchemy.fabut.model.EntityTierOneType;
import org.junit.Assert;
import org.junit.Test;

import cloud.alchemy.fabut.Fabut;

/**
 * Tests for {@link AbstractSingleProperty}.
 */
public class PropertyTest extends Assert {

    /**
     * Test for ignored when varargs are passed.
     */
    @Test
    public void testIgnored() {
        // setup
        final PropertyPath[] properties = new PropertyPath[] {EntityTierOneType.PROPERTY, EntityTierOneType.ID};

        // method
        final MultiProperties multi = Fabut.ignored(properties);

        // assert
        assertEquals(properties.length, multi.getProperties().size());

        for (int i = 0; i < properties.length; i++) {
            assertTrue(multi.getProperties().get(i) instanceof IgnoredProperty);
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

    /**
     * Test for nulll when varargs are passed.
     */
    @Test
    public void testNulll() {
        // setup
        final PropertyPath[] properties = new PropertyPath[] {EntityTierOneType.PROPERTY, EntityTierOneType.ID};

        // method
        final MultiProperties multi = Fabut.isNull(properties);

        // assert
        assertEquals(properties.length, multi.getProperties().size());

        for (int i = 0; i < properties.length; i++) {
            assertTrue(multi.getProperties().get(i) instanceof NullProperty);
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

    /**
     * Test for notNull when varargs are passed.
     */
    @Test
    public void testNotNull() {
        // setup
        final PropertyPath[] properties = new PropertyPath[] {EntityTierOneType.PROPERTY, EntityTierOneType.ID};

        // method
        final MultiProperties multi = Fabut.notNull(properties);

        // assert
        assertEquals(properties.length, multi.getProperties().size());

        for (int i = 0; i < properties.length; i++) {
            assertTrue(multi.getProperties().get(i) instanceof NotNullProperty);
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

}
