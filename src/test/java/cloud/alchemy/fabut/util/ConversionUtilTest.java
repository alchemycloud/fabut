package cloud.alchemy.fabut.util;

import cloud.alchemy.fabut.Fabut;
import cloud.alchemy.fabut.property.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link ConversionUtil}.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class ConversionUtilTest extends Assert {

    private static final PropertyPath<String> TEST = new PropertyPath<>("test");

    /**
     * Test for createListFromVaragrs of {@link ConversionUtil} when there are two properties specified in particular.
     * order.s
     */
    @Test
    public void testCreateListFromVarargsTwoProperties() {
        // setup
        final NullProperty nullProperty = Fabut.isNull(TEST);
        final NotNullProperty notNullProperty = Fabut.notNull(TEST);

        // method
        final List<AbstractSingleProperty> properties = Arrays.asList(nullProperty, notNullProperty);

        // assert
        assertEquals(2, properties.size());
        assertEquals(nullProperty, properties.get(0));
        assertEquals(notNullProperty, properties.get(1));
    }

    /**
     * Test for createListFromVaragrs of {@link ConversionUtil} when there are none properties specified.
     */
    @Test
    public void testCreateListFromVarargsNoProperties() {
        // method
        final List<ISingleProperty> properties = Collections.emptyList();

        // assert
        assertEquals(0, properties.size());
    }

}
