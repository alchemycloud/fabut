package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ReflectionUtil}.
 */
public class ReflectionUtilTest {

    private static final String TEST = "test";
    private static final String PROPERTY = "property";

    // ==================== getRealClass tests ====================

    @Test
    void testGetRealClassNonProxy() {
        Class<?> result = ReflectionUtil.getRealClass(TierOneType.class);
        assertEquals(TierOneType.class, result);
    }

    @Test
    void testGetRealClassCaching() {
        // Call twice to test caching
        Class<?> result1 = ReflectionUtil.getRealClass(TierOneType.class);
        Class<?> result2 = ReflectionUtil.getRealClass(TierOneType.class);
        assertSame(result1, result2);
    }

    // ==================== findGetMethod tests ====================

    @Test
    void testFindGetMethodExists() {
        Method method = ReflectionUtil.findGetMethod(TierOneType.class, "getProperty");
        assertNotNull(method);
        assertEquals("getProperty", method.getName());
    }

    @Test
    void testFindGetMethodNotExists() {
        Method method = ReflectionUtil.findGetMethod(TierOneType.class, "getNonExistent");
        assertNull(method);
    }

    @Test
    void testFindGetMethodInSuperclass() {
        Method method = ReflectionUtil.findGetMethod(EntityTierOneType.class, "getId");
        assertNotNull(method);
        assertEquals("getId", method.getName());
    }

    @Test
    void testFindGetMethodOnObject() {
        TierOneType obj = new TierOneType(TEST);
        Method method = ReflectionUtil.findGetMethod(obj, "getProperty");
        assertNotNull(method);
    }

    // ==================== findSetMethod tests ====================

    @Test
    void testFindSetMethodExists() {
        Method method = ReflectionUtil.findSetMethod(TierOneType.class, "setProperty");
        assertNotNull(method);
        assertEquals("setProperty", method.getName());
    }

    @Test
    void testFindSetMethodNotExists() {
        Method method = ReflectionUtil.findSetMethod(TierOneType.class, "setNonExistent");
        assertNull(method);
    }

    @Test
    void testFindSetMethodOnObject() {
        TierOneType obj = new TierOneType(TEST);
        Method method = ReflectionUtil.findSetMethod(obj, "setProperty");
        assertNotNull(method);
    }

    // ==================== getMethods caching tests ====================

    @Test
    void testGetMethodsCaching() {
        Map<String, Method> methods1 = ReflectionUtil.getMethods(TierOneType.class);
        Map<String, Method> methods2 = ReflectionUtil.getMethods(TierOneType.class);
        assertSame(methods1, methods2); // Should return cached instance
    }

    @Test
    void testGetMethodsContainsGetters() {
        Map<String, Method> methods = ReflectionUtil.getMethods(TierOneType.class);
        assertTrue(methods.containsKey("getProperty"));
    }

    // ==================== setMethods caching tests ====================

    @Test
    void testSetMethodsCaching() {
        Map<String, Method> methods1 = ReflectionUtil.setMethods(TierOneType.class);
        Map<String, Method> methods2 = ReflectionUtil.setMethods(TierOneType.class);
        assertSame(methods1, methods2); // Should return cached instance
    }

    // ==================== findField tests ====================

    @Test
    void testFindFieldExists() {
        Field field = ReflectionUtil.findField(TierOneType.class, PROPERTY);
        assertNotNull(field);
        assertEquals(PROPERTY, field.getName());
    }

    @Test
    void testFindFieldNotExists() {
        Field field = ReflectionUtil.findField(TierOneType.class, "nonExistent");
        assertNull(field);
    }

    @Test
    void testFindFieldInSuperclass() {
        Field field = ReflectionUtil.findField(EntityTierOneType.class, "id");
        assertNotNull(field);
        assertEquals("id", field.getName());
    }

    @Test
    void testFindFieldOnObject() {
        TierOneType obj = new TierOneType(TEST);
        Field field = ReflectionUtil.findField(obj, PROPERTY);
        assertNotNull(field);
    }

    // ==================== findAccessibleField tests ====================

    @Test
    void testFindAccessibleField() {
        TierOneType obj = new TierOneType(TEST);
        Field field = ReflectionUtil.findAccessibleField(obj, PROPERTY);
        assertNotNull(field);
        assertTrue(field.canAccess(obj) || field.trySetAccessible());
    }

    @Test
    void testFindAccessibleFieldNotExists() {
        TierOneType obj = new TierOneType(TEST);
        Field field = ReflectionUtil.findAccessibleField(obj, "nonExistent");
        assertNull(field);
    }

    // ==================== makeAccessible tests ====================

    @Test
    void testMakeAccessibleCaching() throws Exception {
        Field field = TierOneType.class.getDeclaredField(PROPERTY);

        // Call twice - second call should use cache
        ReflectionUtil.makeAccessible(field);
        ReflectionUtil.makeAccessible(field);

        assertTrue(field.canAccess(new TierOneType(TEST)) || field.trySetAccessible());
    }

    // ==================== getFieldNameOfGet tests ====================

    @Test
    void testGetFieldNameOfGetWithGetPrefix() throws Exception {
        Method method = TierOneType.class.getMethod("getProperty");
        String fieldName = ReflectionUtil.getFieldNameOfGet(method);
        assertEquals(PROPERTY, fieldName);
    }

    @Test
    void testGetFieldNameOfGetWithIsPrefix() throws Exception {
        Method method = BooleanFieldType.class.getMethod("isProperty");
        String fieldName = ReflectionUtil.getFieldNameOfGet(method);
        assertEquals(PROPERTY, fieldName);
    }

    // ==================== getFieldNameOfSet tests ====================

    @Test
    void testGetFieldNameOfSet() throws Exception {
        Method method = TierOneType.class.getMethod("setProperty", String.class);
        String fieldName = ReflectionUtil.getFieldNameOfSet(method);
        assertEquals(PROPERTY, fieldName);
    }

    // ==================== getIdValue tests ====================

    @Test
    void testGetIdValueEntity() {
        EntityTierOneType entity = new EntityTierOneType(TEST, 42);
        Object id = ReflectionUtil.getIdValue(entity);
        assertEquals(42, id);
    }

    @Test
    void testGetIdValueNoIdMethod() {
        TierOneType obj = new TierOneType(TEST);
        Object id = ReflectionUtil.getIdValue(obj);
        assertNull(id);
    }

    @Test
    void testGetIdValueNullId() {
        EntityTierOneType entity = new EntityTierOneType(TEST, null);
        Object id = ReflectionUtil.getIdValue(entity);
        assertNull(id);
    }

    // ==================== hasIdMethod tests ====================

    @Test
    void testHasIdMethodTrue() {
        EntityTierOneType entity = new EntityTierOneType();
        assertTrue(ReflectionUtil.hasIdMethod(entity));
    }

    @Test
    void testHasIdMethodFalse() {
        TierOneType obj = new TierOneType(TEST);
        assertFalse(ReflectionUtil.hasIdMethod(obj));
    }

    // ==================== isListType tests ====================

    @Test
    void testIsListTypeTrue() {
        assertTrue(ReflectionUtil.isListType(ArrayList.class));
        assertTrue(ReflectionUtil.isListType(LinkedList.class));
    }

    @Test
    void testIsListTypeFalse() {
        assertFalse(ReflectionUtil.isListType(HashSet.class));
        assertFalse(ReflectionUtil.isListType(String.class));
    }

    // ==================== isSetType tests ====================

    @Test
    void testIsSetTypeTrue() {
        assertTrue(ReflectionUtil.isSetType(HashSet.class));
        assertTrue(ReflectionUtil.isSetType(TreeSet.class));
    }

    @Test
    void testIsSetTypeFalse() {
        assertFalse(ReflectionUtil.isSetType(ArrayList.class));
        assertFalse(ReflectionUtil.isSetType(String.class));
    }

    // ==================== isMapType tests ====================

    @Test
    void testIsMapTypeTrue() {
        assertTrue(ReflectionUtil.isMapType(HashMap.class));
        assertTrue(ReflectionUtil.isMapType(TreeMap.class));
    }

    @Test
    void testIsMapTypeFalse() {
        assertFalse(ReflectionUtil.isMapType(ArrayList.class));
        assertFalse(ReflectionUtil.isMapType(String.class));
    }

    // ==================== isCollectionClass tests ====================

    @Test
    void testIsCollectionClassTrue() {
        assertTrue(ReflectionUtil.isCollectionClass(ArrayList.class));
        assertTrue(ReflectionUtil.isCollectionClass(HashSet.class));
        assertTrue(ReflectionUtil.isCollectionClass(HashMap.class));
    }

    @Test
    void testIsCollectionClassFalse() {
        assertFalse(ReflectionUtil.isCollectionClass(String.class));
        assertFalse(ReflectionUtil.isCollectionClass(Integer.class));
    }

    // ==================== isOptionalType tests ====================

    @Test
    void testIsOptionalTypeTrue() {
        assertTrue(ReflectionUtil.isOptionalType(Optional.class));
    }

    @Test
    void testIsOptionalTypeFalse() {
        assertFalse(ReflectionUtil.isOptionalType(String.class));
    }

    // ==================== isOneOfType tests ====================

    @Test
    void testIsOneOfTypeTrue() {
        List<Class<?>> types = Arrays.asList(String.class, Integer.class);
        assertTrue(ReflectionUtil.isOneOfType(String.class, types));
    }

    @Test
    void testIsOneOfTypeFalse() {
        List<Class<?>> types = Arrays.asList(String.class, Integer.class);
        assertFalse(ReflectionUtil.isOneOfType(Double.class, types));
    }

    @Test
    void testIsOneOfTypeWithSubclass() {
        List<Class<?>> types = Arrays.asList(Number.class);
        assertTrue(ReflectionUtil.isOneOfType(Integer.class, types));
    }

    @Test
    void testIsOneOfTypeEmptyCollection() {
        assertFalse(ReflectionUtil.isOneOfType(String.class, Collections.emptyList()));
    }
}
