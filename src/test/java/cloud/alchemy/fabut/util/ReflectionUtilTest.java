package cloud.alchemy.fabut.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import cloud.alchemy.fabut.enums.AssertableType;
import cloud.alchemy.fabut.exception.CopyException;
import cloud.alchemy.fabut.graph.NodesList;
import org.junit.Assert;
import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;

import cloud.alchemy.fabut.model.A;
import cloud.alchemy.fabut.model.B;
import cloud.alchemy.fabut.model.BooleanFieldType;
import cloud.alchemy.fabut.model.C;
import cloud.alchemy.fabut.model.EntityTierOneType;
import cloud.alchemy.fabut.model.EntityTierTwoType;
import cloud.alchemy.fabut.model.NoGetMethodsType;
import cloud.alchemy.fabut.model.TierOneType;
import cloud.alchemy.fabut.model.TierTwoType;
import cloud.alchemy.fabut.model.UnknownType;

/**
 * Tests for {@link ReflectionUtil}.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @author Andrej Miletic
 */
@SuppressWarnings("unchecked")
public class ReflectionUtilTest extends Assert {

    private static final String TEST = "test";
    private static final String PROPERTY = "property";
    private static final String GET_PROPERTY = "getProperty";
    private static final String IS_PROPERTY = "isProperty";
    private static final String IS_FIELD = "isField";
    private static final String IS_NOT_BOOLEAN_PROPERTY = "isNotBooleanProperty";

    Map<AssertableType, List<Class<?>>> types;
    Map<Class<?>, List<String>> ignoredProperties;

    /**
     * Test for isGetMethod of {@link ReflectionUtil} when method does not starts with "get" prefix and there is
     * matching field in the class.
     * 
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Before
    public void setup() {
        types = new EnumMap<>(AssertableType.class);
        final List<Class<?>> complexTypes = new LinkedList<>();
        complexTypes.add(TierOneType.class);
        complexTypes.add(TierTwoType.class);
        complexTypes.add(A.class);
        complexTypes.add(B.class);
        complexTypes.add(C.class);
        types.put(AssertableType.COMPLEX_TYPE, complexTypes);

        final List<Class<?>> entityTypes = new LinkedList<>();
        complexTypes.add(EntityTierOneType.class);
        complexTypes.add(EntityTierTwoType.class);
        types.put(AssertableType.ENTITY_TYPE, entityTypes);

        ignoredProperties = new HashMap<>();

    }

    @Test
    public void testIsGetMethodNoGetPrefix() throws SecurityException, NoSuchMethodException {
        // setup
        final Method method = NoGetMethodsType.class.getMethod("property");
        // method
        final boolean isGetMethod = ReflectionUtil.isGetMethod(new NoGetMethodsType(TEST).getClass(), method, ignoredProperties);
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
        final boolean isGetMethod = ReflectionUtil.isGetMethod(new NoGetMethodsType(TEST).getClass(), method, ignoredProperties);
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
        final boolean isGetMetod = ReflectionUtil.isGetMethod(new TierOneType(TEST).getClass(), method, ignoredProperties);
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
        final boolean isGetMethod = ReflectionUtil.isGetMethod(new NoGetMethodsType(TEST).getClass(), method, ignoredProperties);
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
        final boolean isGetMethod = ReflectionUtil.isGetMethod(new NoGetMethodsType(TEST).getClass(), method, ignoredProperties);

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
        final boolean assertValue = ReflectionUtil.isGetMethod(new NoGetMethodsType(TEST).getClass(), method, ignoredProperties);

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
        final boolean isGetMethod = ReflectionUtil.isGetMethod(new BooleanFieldType(true).getClass(), method, ignoredProperties);

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
        final Field field = ReflectionUtil.findField(EntityTierOneType.class, EntityTierOneType.PROPERTY.getPath());

        // assert
        assertNotNull(field);
        assertEquals(EntityTierOneType.PROPERTY.getPath(), field.getName());
    }

    /**
     * Test for findFieldInInheritance of {@link ReflectionUtil} when subclass and name of field from superclass is
     * specified.
     */
    @Test
    public void testFindFieldInInheritanceSuperclass() {
        // method
        final Field field = ReflectionUtil.findField(EntityTierOneType.class, EntityTierOneType.ID.getPath());

        // assert
        assertNotNull(field);
        assertEquals(EntityTierOneType.ID.getPath(), field.getName());
    }

    /**
     * Test for isListType of {@link ReflectionUtil} when specified object is instance of {@link List} interface.
     */
    @Test
    public void testIsListTypeTrue() {
        // assert
        assertTrue(ReflectionUtil.isListType(new ArrayList<String>()));
    }

    /**
     * Test for isListType of {@link ReflectionUtil} when specified object is not instance of {@link List}
     * interface.
     */
    @Test
    public void testIsListTypeFalse() {
        // assert
        assertFalse(ReflectionUtil.isListType(TEST));
    }

    /**
     * Test for isSetType of {@link ReflectionUtil} when specified object is instance of {@link Set} interface.
     */
    @Test
    public void testIsSetTypeTrue() {
        // assert
        assertTrue(ReflectionUtil.isSetType(new HashSet<String>()));
    }

    /**
     * Test for isSetType of {@link ReflectionUtil} when specified object is not instance of {@link Set}
     * interface.
     */
    @Test
    public void testIsSetTypeFalse() {
        // assert
        assertFalse(ReflectionUtil.isSetType(TEST));
    }


    @Test
    public void testIsMapTypeTrue() {
        // assert
        assertTrue(ReflectionUtil.isMapType(new HashMap<String, String>()));
    }

    @Test
    public void testIsMapTypeFalse() {
        // assert
        assertFalse(ReflectionUtil.isMapType(TEST));
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

    /**
     * Test for createEmptyCopyOf of {@link FabutRepositoryAssert} when specified object has default constructor.
     */
    @Test
    public void testCreateEmptyCopyOfHasDefaultConstructor() {
        // method
        final TierOneType assertObject = (TierOneType) ReflectionUtil.createEmptyCopyOf(new TierOneType());

        // assert
        assertNotNull(assertObject);
        assertEquals(TierOneType.class, assertObject.getClass());
    }

    /**
     * Test for createEmptyCopyOf of {@link FabutRepositoryAssert} when specified object has no default constructor.
     */
    @Test(expected = AssertionError.class)
    public void testCreateEmptyCopyOfNoDefaultConstructor() {
        // method
        final NoGetMethodsType assertObject = (NoGetMethodsType) ReflectionUtil.createEmptyCopyOf(new NoGetMethodsType(
                TEST));

        // assert
        assertNull(assertObject);
    }

    /**
     * Test for copyList of {@link FabutRepositoryAssert} if it copies list.
     */
    @Test
    public void testCopyList() throws CopyException {
        // setup
        final List<String> list = new LinkedList<>();
        final Map<AssertableType, List<Class<?>>> types = new EnumMap<AssertableType, List<Class<?>>>(
                AssertableType.class);
        types.put(AssertableType.COMPLEX_TYPE, new LinkedList<Class<?>>());
        list.add(TEST);

        // method
        final List<String> assertList = ReflectionUtil.copyList(list, types, ignoredProperties);

        assertNotNull(assertList);
        assertEquals(1, assertList.size());
        assertEquals(TEST, assertList.get(0));
    }

    /**
     * Test for createCopy of {@link FabutRepositoryAssert} when specified object is null.
     */
    @Test
    public void testCreateCopyNull() throws CopyException {
        // method
        final Object object = ReflectionUtil.createCopy(null, new EnumMap<AssertableType, List<Class<?>>>(
                AssertableType.class), ignoredProperties);

        // assert
        assertNull(object);
    }

    /**
     * Test for createCopy of {@link FabutRepositoryAssert} when specified object is list.
     */
    @Test
    public void testCreateCopyList() throws CopyException {
        // setup
        final List<String> list = new LinkedList<>();
        final Map<AssertableType, List<Class<?>>> types = new EnumMap<AssertableType, List<Class<?>>>(
                AssertableType.class);
        types.put(AssertableType.COMPLEX_TYPE, new LinkedList<Class<?>>());
        list.add(TEST);

        // method
        final List<String> assertList = (List<String>) ReflectionUtil.createCopy(list, types, ignoredProperties);

        assertNotNull(assertList);
        assertEquals(1, assertList.size());
        assertEquals(TEST, assertList.get(0));
    }

    /**
     * Test for ivokeSetMethod of {@link FabutRepositoryAssert} when specified object for copying has set method with
     * specified name and can be invoked.
     */
    @Test
    public void testIvokeSetMethodSuccess() {
        // setup
        final Method method = ReflectionUtil.getGetMethods(new TierOneType(), types, ignoredProperties).get(0);
        final Class<?> classObject = TierOneType.class;
        final String propertyName = PROPERTY;
        final TierOneType copy = new TierOneType();
        final Object copiedProperty = PROPERTY;

        // method
        ReflectionUtil.invokeSetMethod(method, classObject, propertyName, copy, copiedProperty);

        // assert
        assertEquals(PROPERTY, copy.getProperty());
    }

    /**
     * Test for ivokeSetMethod of {@link FabutRepositoryAssert} when specified object for copying has set method with
     * specified name and can't be invoked as object has no set methods.
     */
    @Test
    public void testIvokeSetMethodNull() {
        // setup
        final Method method = ReflectionUtil.getGetMethods(new TierTwoType(new TierOneType()), types, ignoredProperties).get(0);
        final Class<?> classObject = TierTwoType.class;
        final String propertyName = PROPERTY;
        final TierTwoType copy = new TierTwoType(new TierOneType());
        final Object copiedProperty = PROPERTY;

        // method
        final boolean ok = ReflectionUtil.invokeSetMethod(method, classObject, propertyName, copy, copiedProperty);

        // assert
        assertFalse(ok);
    }

    /**
     * Test for copyProperty of {@link FabutRepositoryAssert} when specified object for copying is null;
     */
    @Test
    public void testCopyPropertyNull() throws CopyException {
        // method
        final Object copy = ReflectionUtil.copyProperty(null, null, types, ignoredProperties);

        // assert
        assertNull(copy);
    }

    /**
     * Test for copyProperty of {@link FabutRepositoryAssert} when specified object is complex object with property of
     * complex type.
     */
    @Test
    public void testCopyPropertyComplexType() throws CopyException {
        // method
        final EntityTierTwoType copy = (EntityTierTwoType) ReflectionUtil.copyProperty(new EntityTierTwoType(TEST, 1,
                new EntityTierOneType(PROPERTY, 2)), new NodesList(), types, ignoredProperties);

        // assert
        assertNotNull(copy);
        assertEquals(TEST, copy.getProperty());
        assertEquals(1, copy.getId());
        assertNotNull(copy.getSubProperty());
        assertEquals(PROPERTY, copy.getSubProperty().getProperty());
        assertEquals(new Integer(2), copy.getSubProperty().getId());
    }

    /**
     * Test for copyProperty of {@link FabutRepositoryAssert} when specified object is {@link List}.
     */
    @Test
    public void testCopyPropertyList() throws CopyException {
        // setup
        final List<String> list = new ArrayList<String>();
        list.add(TEST);

        // method
        final List<String> copy = (List<String>) ReflectionUtil.copyProperty(list, null, types, ignoredProperties);

        // assert
        assertNotNull(copy);
        assertEquals(1, copy.size());
        assertEquals(TEST, copy.get(0));

    }

    /**
     * Test for copyProperty of {@link FabutRepositoryAssert} when specified object is of unknown type.
     */
    @Test
    public void testCopyPropertyUnkownType() throws CopyException {
        // setup
        final UnknownType unknownType = new UnknownType();

        // method
        final UnknownType copy = (UnknownType) ReflectionUtil.copyProperty(unknownType, null, types, ignoredProperties);

        // assert
        assertEquals(unknownType, copy);

    }

    /**
     * Test for getPropertyForCopying of {@link FabutRepositoryAssert} when specified method can be invoked.
     */
    @Test
    public void testGetPropertyForCopyingCanInvoke() {
        // setup
        final Method method = ReflectionUtil.getGetMethods(new EntityTierOneType(), types, ignoredProperties).get(1);

        // method
        final String property = (String) ReflectionUtil.getPropertyForCopying(new EntityTierOneType(TEST, 1), method);

        // assert
        assertEquals(TEST, property);

    }

    /**
     * Test for getPropertyForCopying of {@link FabutRepositoryAssert} when specified method can not be invoked.
     */
    @Test(expected = AssertionFailedError.class)
    public void testGetPropertyForCopyingCantInvoke() {
        // setup
        final Method method = ReflectionUtil.getGetMethods(new EntityTierOneType(), types, ignoredProperties).get(1);

        // method
        final String property = (String) ReflectionUtil.getPropertyForCopying(null, method);

        // assert
        assertNull(property);
    }

    /**
     * Test for createCopy if it properly handles cyclic object references.
     */
    @Test
    public void testCreateCopyCyclic() throws CopyException {
        // setup
        final A a = new A();
        a.setProperty(PROPERTY);
        a.setB(new B(new C(a)));

        // method
        final A aCopy = (A) ReflectionUtil.createCopy(a, types, ignoredProperties);

        // assert
        assertEquals(aCopy, aCopy.getB().getC().getA());
        assertEquals(a.getProperty(), aCopy.getProperty());
        assertEquals(a.getB().getC().getA().getProperty(), aCopy.getB().getC().getA().getProperty());

    }

    /**
     * Test for {@link ReflectionUtil#copyMap(Map, Map)}.
     * 
     * @throws CopyException
     */
    @Test
    public void testCopyMap() throws CopyException {
        // setup
        final Map<String, TierOneType> map = new HashMap<>();
        map.put("first", new TierOneType(TEST));
        map.put("second", new TierOneType(TEST + TEST));

        // method
        final Map<String, TierOneType> result = (Map<String, TierOneType>) ReflectionUtil.copyMap(map, types, ignoredProperties);

        // assert
        assertEquals(2, result.size());
        assertEquals(TEST, result.get("first").getProperty());
        assertEquals(TEST + TEST, result.get("second").getProperty());
    }
}
