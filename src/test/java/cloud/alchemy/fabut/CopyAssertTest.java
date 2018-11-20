package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.EntityTierOneType;
import cloud.alchemy.fabut.property.CopyAssert;
import org.junit.Assert;

import org.junit.Test;

/**
 * Test class for {@link CopyAssert}.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * 
 */
public class CopyAssertTest extends Assert {

    /**
     * Test for {@link CopyAssert#CopyAssert(Object)}.
     */
    @Test
    public void testCopyAssert() {
        // method
        final EntityTierOneType entity = new EntityTierOneType();
        final CopyAssert copyAssert = new CopyAssert(entity);

        // assert
        assertFalse(copyAssert.isAsserted());
        assertEquals(entity, copyAssert.getEntity());
    }

    /**
     * Test for {@link CopyAssert#getEntity()}.
     */
    @Test
    public void testGetEntity() {
        // setup
        final EntityTierOneType expected = new EntityTierOneType();
        final CopyAssert copyAssert = new CopyAssert(expected);

        // method
        final EntityTierOneType actual = (EntityTierOneType) copyAssert.getEntity();

        // assert
        assertEquals(expected, actual);
    }

    /**
     * Test for {@link CopyAssert#setEntity()}.
     */
    @Test
    public void testSetEntity() {
        // setup
        final EntityTierOneType entity = new EntityTierOneType();
        final CopyAssert copyAssert = new CopyAssert(entity);
        final EntityTierOneType expected = new EntityTierOneType();

        // method
        copyAssert.setEntity(expected);

        // assert
        assertEquals(expected, copyAssert.getEntity());
    }

    /**
     * Test for {@link CopyAssert#isAsserted()}.
     */
    @Test
    public void testIsAsserted() {
        // setup
        final EntityTierOneType entity = new EntityTierOneType();
        final CopyAssert copyAssert = new CopyAssert(entity);

        // method
        final boolean isAsserted = copyAssert.isAsserted();

        // assert
        assertFalse(isAsserted);
    }

    /**
     * Test for {@link CopyAssert#setAsserted()}.
     */
    @Test
    public void testSetAsserted() {
        // setup
        final EntityTierOneType entity = new EntityTierOneType();
        final CopyAssert copyAssert = new CopyAssert(entity);

        // method
        copyAssert.setAsserted(true);

        // assert
        assertTrue(copyAssert.isAsserted());
    }

}
