package eu.execom.testutil.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;

import eu.execom.testutil.model.BooleanFieldType;
import eu.execom.testutil.model.EntityTierOneType;
import eu.execom.testutil.model.NoGetMethodsType;
import eu.execom.testutil.model.TierOneType;
import eu.execom.testutil.model.UnknownType;

/**
 * Tests for {@link ReflectionUtil}.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
public class ReflectionUtilTest extends Assert {

    private static final String TEST = "test";
    private static final String PROPERTY = "property";
    private static final String GET_PROPERTY = "getProperty";
    private static final String IS_PROPERTY = "isProperty";
    private static final String IS_FIELD = "isField";
    private static final String IS_NOT_BOOLEAN_PROPERTY = "isNotBooleanProperty";

    /**
     * Test for isGetMethod of {@link ReflectionUtil} when method does not starts with "get" prefix and there is
     * matching field in the class.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testIsGetMethodNoGetPrefix() throws SecurityException, NoSuchMethodException {
        // setup
        final Method method = NoGetMethodsType.class.getMethod("property");
        // method
        final boolean isGetMethod = ReflectionUtil.isGetMethod(new NoGetMethodsType(TEST).getClass(), method);
        // assert
        assertFalse(isGetMethod);
    }

    /**
     * Test for isGetMethod of {@link ReflectionUtil} when specified method is has no matching field in the class.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testIsGetMethodFakeGetMethod() throws SecurityException, NoSuchMethodException {
        // setup
        final Method method = NoGetMethodsType.class.getMethod("getString");
        // method
        final boolean isGetMethod = ReflectionUtil.isGetMethod(new NoGetMethodsType(TEST).getClass(), method);
        // assert
        assertFalse(isGetMethod);
    }

    /**
     * Test for isGetMethod of {@link ReflectionUtil} when method is real get method.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testIsGetMethodGetPrefix() throws SecurityException, NoSuchMethodException {
        // setup
        final Method method = TierOneType.class.getMethod(GET_PROPERTY);
        // method
        final boolean isGetMetod = ReflectionUtil.isGetMethod(new TierOneType(TEST).getClass(), method);
        // assert
        assertTrue(isGetMetod);
    }

    /**
     * Test for isGetMethod of {@link ReflectionUtil} when method has no "get" or "is" prefix and has matching field in
     * class.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testIsGetMethodNoPrefix() throws SecurityException, NoSuchMethodException {
        // setup
        final Method method = NoGetMethodsType.class.getMethod(PROPERTY);
        // method
        final boolean isGetMethod = ReflectionUtil.isGetMethod(new NoGetMethodsType(TEST).getClass(), method);
        // assert
        assertFalse(isGetMethod);
    }

    /**
     * Test for isGetMethod of {@link ReflectionUtil} when get methods for primitive boolean type has no matching
     * boolean field in class.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testIsGetMethodFakeIsMethod() throws SecurityException, NoSuchMethodException {
        // setup
        final Method method = NoGetMethodsType.class.getMethod(IS_NOT_BOOLEAN_PROPERTY);

        // method
        final boolean isGetMethod = ReflectionUtil.isGetMethod(new NoGetMethodsType(TEST).getClass(), method);

        // assert
        assertFalse(isGetMethod);
    }

    /**
     * Test for isGetMethod of {@link ReflectionUtil} when there is no field for specified method so it throws
     * {@link NoSuchFieldException}.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testIsGetMethodFakeMethodNoField() throws SecurityException, NoSuchMethodException {
        // setup
        final Method method = NoGetMethodsType.class.getMethod(IS_FIELD);

        // method
        final boolean assertValue = ReflectionUtil.isGetMethod(new NoGetMethodsType(TEST).getClass(), method);

        // assert
        assertFalse(assertValue);
    }

    /**
     * Test for isGetMethod of {@link ReflectionUtil} when get methods is of boolean primitive type.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testIsGetMethodIsMethod() throws SecurityException, NoSuchMethodException {
        // setup
        final Method method = BooleanFieldType.class.getMethod(IS_PROPERTY);

        // method
        final boolean isGetMethod = ReflectionUtil.isGetMethod(new BooleanFieldType(true).getClass(), method);

        // assert
        assertTrue(isGetMethod);
    }

    /**
     * Test for getFieldName of {@link ReflectionUtil} when method starts with "get" prefix and there is matching field
     * in class.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testGetFieldNameGetPrefix() throws SecurityException, NoSuchMethodException {
        // setup
        final Method method = TierOneType.class.getMethod(GET_PROPERTY);
        // method
        final String fieldName = ReflectionUtil.getFieldName(method);
        // assert
        assertEquals(PROPERTY, fieldName);
    }

    /**
     * Test for getFieldName of {@link ReflectionUtil} when get method is of boolean primitive type.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    public void testGetFieldNameIsPrefix() throws SecurityException, NoSuchMethodException {
        // setup
        final Method method = BooleanFieldType.class.getMethod(IS_PROPERTY);
        // method
        final String fieldName = ReflectionUtil.getFieldName(method);
        // assert
        assertEquals(PROPERTY, fieldName);
    }

    /**
     * Test for findFieldInInheritance of {@link ReflectionUtil} when null is specified as a class.
     */
    @Test
    public void testFindFieldInInheritanceNullClass() {
        // method
        final Field field = ReflectionUtil.findField(null, TEST);

        // assert
        assertNull(field);
    }

    /**
     * Test for findFieldInInheritance of {@link ReflectionUtil} when subclass and name of field from subclass is
     * specified.
     */
    @Test
    public void testFindFieldInInheritanceSubclass() {
        // method
        final Field field = ReflectionUtil.findField(EntityTierOneType.class, EntityTierOneType.PROPERTY);

        // assert
        assertNotNull(field);
        assertEquals(EntityTierOneType.PROPERTY, field.getName());
    }

    /**
     * Test for findFieldInInheritance of {@link ReflectionUtil} when subclass and name of field from superclass is
     * specified.
     */
    @Test
    public void testFindFieldInInheritanceSuperclass() {
        // method
        final Field field = ReflectionUtil.findField(EntityTierOneType.class, EntityTierOneType.ID);

        // assert
        assertNotNull(field);
        assertEquals(EntityTierOneType.ID, field.getName());
    }

    /**
     * Test for isListType of {@link ReflectionUtil} when specified object is instance of {@link Collection} interface.
     */
    @Test
    public void testIsListTypeTrue() {
        // assert
        assertTrue(ReflectionUtil.isListType(new ArrayList<String>()));
    }

    /**
     * Test for isListType of {@link ReflectionUtil} when specified object is not instance of {@link Collection}
     * interface.
     */
    @Test
    public void testIsListTypeFalse() {
        // assert
        assertFalse(ReflectionUtil.isListType(TEST));
    }

    // TODO fix these tests

    // /**
    // * Test foe isEntityType of {@link ReflectionUtil} when specified class is entity type.
    // */
    // @Test
    // public void testIsEntityTypeTrue() {
    // // setup
    // final List<Class<?>> entityTypes = new ArrayList<Class<?>>();
    // entityTypes.add(EntityTierOneType.class);
    //
    // // assert
    // assertTrue(ReflectionUtil.isEntityType(new EntityTierOneType().getClass(), entityTypes));
    // }
    //
    // /**
    // * Test foe isEntityType of {@link ReflectionUtil} when specified class is not entity type.
    // */
    // @Test
    // public void testIsEntityTypeFalse() {
    // // assert
    // assertFalse(ReflectionUtil.isEntityType(new TierOneType().getClass(), new ArrayList<Class<?>>()));
    // }
    //
    // /**
    // * Test for isComplexType of {@link ReflectionUtil} when specified class is complex type.
    // */
    // @Test
    // public void testIsComplexTypeTrue() {
    // // setup
    // final List<Class<?>> complexTypes = new ArrayList<Class<?>>();
    // complexTypes.add(TierOneType.class);
    //
    // // assert
    // assertTrue(ReflectionUtil.isComplexType(new TierOneType().getClass(), complexTypes));
    // }
    //
    // /**
    // * Test for isComplexType of {@link ReflectionUtil} when specified class is not complex type.
    // */
    // @Test
    // public void testIsComplexTypeFalse() {
    // // assert
    // assertFalse(ReflectionUtil.isComplexType(new Object().getClass(), new ArrayList<Class<?>>()));
    // }
    //
    // /**
    // * Test for isIgnoredType of {@link ReflectionUtil} when specified class is ignored type.
    // */
    // @Test
    // public void testIsIgnoredTypeTrue() {
    // // setup
    // final List<Class<?>> ignoredTypes = new ArrayList<Class<?>>();
    // ignoredTypes.add(IgnoredType.class);
    //
    // // assert
    // assertTrue(ReflectionUtil.isIgnoredType(new IgnoredType().getClass(), ignoredTypes));
    // }
    //
    // /**
    // * Test for isIgnoredType of {@link ReflectionUtil} when specified class is not ignored type.
    // */
    // @Test
    // public void testIsIgnoredTypeFalse() {
    // // assert
    // assertFalse(ReflectionUtil.isIgnoredType(new Object().getClass(), new ArrayList<Class<?>>()));
    //
    // }
    //
    // /**
    // * Test for isIgnoredType of {@link ReflectionUtil} when expected and actual are null.
    // */
    // @Test
    // public void testIsIgnoredTypeExpectedActualNull() {
    // // assert
    // assertFalse(ReflectionUtil.isIgnoredType(null, null, new ArrayList<Class<?>>()));
    // }
    //
    // /**
    // * Test of isIgnoredType of {@link ReflectionUtil} when expected is not null and is ignored type.
    // */
    // @Test
    // public void testIsIgnoredTypeExpectedNotNull() {
    // // setup
    // final List<Class<?>> ignoredTypes = new ArrayList<Class<?>>();
    // ignoredTypes.add(IgnoredType.class);
    //
    // // assert
    // assertTrue(ReflectionUtil.isIgnoredType(new IgnoredType().getClass(), ignoredTypes));
    //
    // }
    //
    // /**
    // * Test of isIgnoredType of {@link ReflectionUtil} when actual is not null and is ignored type.
    // */
    // @Test
    // public void testIsIgnoredTypeActualNotNull() {
    // // setup
    // final List<Class<?>> ignoredTypes = new ArrayList<Class<?>>();
    // ignoredTypes.add(IgnoredType.class);
    //
    // // assert
    // assertTrue(ReflectionUtil.isIgnoredType(null, new IgnoredType(), ignoredTypes));
    // }

    /**
     * Test for getIdValue of {@link ReflectionUtil} when specified type is entity.
     */
    @Test
    public void testGetIdValueEntity() {
        // setup
        final EntityTierOneType entityTierOneType = new EntityTierOneType(TEST, 1);

        // method
        final Integer id = (Integer) ReflectionUtil.getIdValue(entityTierOneType);

        // assert
        assertEquals(1, id.intValue());
    }

    /**
     * Test for getIdValue of {@link ReflectionUtil} when specified type is unknown type.
     */
    @Test
    public void testGetIdValueUknownType() {
        // setup
        final UnknownType unknownType = new UnknownType();
        // method
        final Integer id = (Integer) ReflectionUtil.getIdValue(unknownType);

        // assert
        assertNull(id);
    }

    // TODO fix these tests

    // /**
    // * Test for getObjectGetMethods when has one real get method.
    // */
    // @Test
    // public void testGetObjectGetMethodsTierOneType() {
    // // method
    // final List<Method> methods = ReflectionUtil.getObjectGetMethods(new TierOneType(TEST),
    // new ArrayList<Class<?>>(), new ArrayList<Class<?>>());
    // final Method method = methods.get(0);
    // // assert
    // assertEquals(1, methods.size());
    // assertEquals("getProperty", method.getName());
    // }
    //
    // /**
    // * Test for getObjectGetMethods when class has no real get methods.
    // */
    // @Test
    // public void testGetObjectGetMethodsNoGetMethodsType() {
    // // method
    // final List<Method> methods = ReflectionUtil.getObjectGetMethods(new NoGetMethodsType(TEST),
    // new ArrayList<Class<?>>(), new ArrayList<Class<?>>());
    // // assert
    // assertEquals(0, methods.size());
    // }
    //
    // /**
    // * Test for getObjectGetMethods if it impose ordering of get methods for complex or entity types come last in
    // list.
    // *
    // * @throws Exception
    // */
    // @Test
    // public void testGetObjectGetMethodsCheckOrdering() throws Exception {
    // // setup
    // final Method getSubProperty = EntityTierTwoType.class.getMethod("getSubProperty");
    // final List<Class<?>> entityTypes = new ArrayList<Class<?>>();
    // entityTypes.add(EntityTierTwoType.class);
    // entityTypes.add(EntityTierOneType.class);
    //
    // // method
    // final List<Method> methods = ReflectionUtil.getObjectGetMethods(new EntityTierTwoType(TEST, 10,
    // new EntityTierOneType(TEST, 5)), new ArrayList<Class<?>>(), entityTypes);
    //
    // // assert
    // assertEquals(getSubProperty.getName(), methods.get(2).getName());
    // }

    @Test
    public void testGetObjectGetMethodNamed() throws Exception {
        // setup
        final String methodName = "getProperty";
        final TierOneType tierOneType = new TierOneType(PROPERTY);
        final Method expectedGetMethod = tierOneType.getClass().getMethod(methodName);

        // method
        final Method actualGetMethod = ReflectionUtil.getGetMethod(methodName, tierOneType);

        // assert
        assertEquals(expectedGetMethod.getName(), actualGetMethod.getName());
        assertEquals(expectedGetMethod.getParameterTypes().length, actualGetMethod.getParameterTypes().length);
    }

    @Test(expected = NoSuchMethodException.class)
    public void testGetObjectGetMethodNamedNoGetMethod() throws Exception {
        // setup
        final String methodName = "getProperty";
        final NoGetMethodsType noGetMethodsType = new NoGetMethodsType();

        // method
        ReflectionUtil.getGetMethod(methodName, noGetMethodsType);
    }

}
