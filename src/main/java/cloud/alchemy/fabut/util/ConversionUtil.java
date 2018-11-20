package cloud.alchemy.fabut.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cloud.alchemy.fabut.IFabutRepositoryTest;
import cloud.alchemy.fabut.IFabutTest;
import cloud.alchemy.fabut.enums.AssertType;
import cloud.alchemy.fabut.enums.AssertableType;
import cloud.alchemy.fabut.pair.AssertPair;

/**
 * Util class for conversions needed by testutil.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public final class ConversionUtil {

    /**
     * Private Conversion util constructor.
     */
    private ConversionUtil() {
        super();
    }

    /**
     * Creates a {@link LinkedList} from specified varargs.
     * 
     * @param excludes
     *            varargs parameters
     * @return list of objects of class X
     * 
     * @param <X>
     *            generic type
     */
    @SafeVarargs
    public static <X> List<X> createListFromArray(final X... excludes) {
        final List<X> list = new LinkedList<>();
        list.addAll(Arrays.asList(excludes));

        return list;
    }

    /**
     * Creates the assert pair.
     * 
     * @param expected
     *            the expected
     * @param actual
     *            the actual
     * @param types
     *            the types
     * @return the assert pair
     */
    public static AssertPair createAssertPair(final Object expected, final Object actual,
            final Map<AssertableType, List<Class<?>>> types) {
        final AssertableType objectType = ReflectionUtil.getObjectType(expected, actual, types);
        return new AssertPair(expected, actual, objectType);
    }

    /**
     * Creates the assert pair.
     * 
     * @param expected
     *            the expected
     * @param actual
     *            the actual
     * @param types
     *            the types
     * @param isProperty
     *            the is property
     * @return the assert pair
     */
    public static AssertPair createAssertPair(final Object expected, final Object actual,
            final Map<AssertableType, List<Class<?>>> types, final boolean isProperty) {
        final AssertPair assertPair = createAssertPair(expected, actual, types);
        assertPair.setProperty(isProperty);
        return assertPair;
    }

    /**
     * Gets the assert type based on which of the fabut interfaces does test instance implements.
     * 
     * @param testInstance
     *            the test instance
     * @return the assert type
     */
    public static AssertType getAssertType(final Object testInstance) {
        if (testInstance instanceof IFabutRepositoryTest) {
            return AssertType.REPOSITORY_ASSERT;
        } else if (testInstance instanceof IFabutTest) {
            return AssertType.OBJECT_ASSERT;
        } else {
            return AssertType.UNSUPPORTED_ASSERT;
        }
    }

}
