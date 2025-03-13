package cloud.alchemy.fabut;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class that provides helper methods for reflection operations.
 * This class handles method finding, field access, and type checking operations.
 * <p>
 * The class caches reflection results to improve performance for repeated operations.
 * </p>
 */
public class ReflectionUtil {
    private static final Logger LOGGER = Logger.getLogger(ReflectionUtil.class.getName());

    // Method name constants
    private static final String GET_METHOD_PREFIX = "get";
    private static final String IS_METHOD_PREFIX = "is";
    static final String SET_METHOD_PREFIX = "set";
    static final String GET_ID = "getId";
    static final String SET_ID = "setId";
    static final String ID = "id";

    // Thread-safe caches for reflection operations
    private static final Map<Class<?>, Map<String, Method>> classGetMethods = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Method>> classSetMethods = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Field>> classFields = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ReflectionUtil() {
        // Utility class should not be instantiated
    }

    /**
     * Returns the real class of an object, handling proxy classes.
     *
     * @param clazz The class to evaluate
     * @return The real class (superclass if proxy)
     */
    static Class<?> getRealClass(final Class<?> clazz) {
        if (clazz.getName().contains("Proxy")) {
            return clazz.getSuperclass();
        }
        return clazz;
    }

    /**
     * Finds a getter method on the given object.
     *
     * @param object The object to find the getter method on
     * @param methodName The name of the method to find
     * @return The Method object, or null if not found
     */
    static Method findGetMethod(final Object object, final String methodName) {
        return findGetMethod(object.getClass(), methodName);
    }

    /**
     * Finds a getter method on the given class.
     *
     * @param methodClass The class to find the getter method on
     * @param methodName The name of the method to find
     * @return The Method object, or null if not found
     */
    static Method findGetMethod(final Class<?> methodClass, final String methodName) {
        var methodMap = getMethods(methodClass);
        Method method = methodMap.get(methodName);
        if (method != null) return method;

        if (methodClass.getSuperclass() == null) return null;

        return findGetMethod(methodClass.getSuperclass(), methodName);
    }

    /**
     * Gets all getter methods for a class, using caching for performance.
     *
     * @param methodClass The class to get methods for
     * @return Map of method names to Method objects
     */
    static Map<String, Method> getMethods(Class<?> methodClass) {
        return classGetMethods.computeIfAbsent(methodClass, clazz -> {
            var methodMap = new ConcurrentHashMap<String, Method>();
            
            // For classes with many methods, use parallel processing
            Arrays.stream(clazz.getMethods())
                  .parallel()
                  .filter(method -> isGetMethod(clazz, method))
                  .forEach(method -> methodMap.put(method.getName(), method));
                  
            return methodMap;
        });
    }

    /**
     * Finds a setter method on the given object.
     *
     * @param object The object to find the setter method on
     * @param methodName The name of the method to find
     * @return The Method object, or null if not found
     */
    static Method findSetMethod(final Object object, final String methodName) {
        return findSetMethod(object.getClass(), methodName);
    }

    /**
     * Finds a setter method on the given class.
     *
     * @param methodClass The class to find the setter method on
     * @param methodName The name of the method to find
     * @return The Method object, or null if not found
     */
    static Method findSetMethod(final Class<?> methodClass, final String methodName) {
        var methodMap = setMethods(methodClass);
        Method method = methodMap.get(methodName);
        if (method != null) return method;

        if (methodClass.getSuperclass() == null) return null;

        return findSetMethod(methodClass.getSuperclass(), methodName);
    }

    /**
     * Gets all setter methods for a class, using caching for performance.
     *
     * @param methodClass The class to get methods for
     * @return Map of method names to Method objects
     */
    static Map<String, Method> setMethods(Class<?> methodClass) {
        return classSetMethods.computeIfAbsent(methodClass, clazz -> {
            var methodMap = new ConcurrentHashMap<String, Method>();
            
            // Use parallel processing for classes with many methods
            Arrays.stream(clazz.getMethods())
                  .parallel()
                  .filter(method -> isSetMethod(clazz, method))
                  .forEach(method -> methodMap.put(method.getName(), method));
                  
            return methodMap;
        });
    }

    /**
     * Extracts the field name from a getter method.
     *
     * @param method The getter method
     * @return The field name corresponding to the getter
     */
    static String getFieldNameOfGet(final Method method) {
        String fieldName;
        if (method.getName().startsWith(IS_METHOD_PREFIX)) {
            fieldName = StringUtils.removeStart(method.getName(), IS_METHOD_PREFIX);
        } else {
            fieldName = StringUtils.removeStart(method.getName(), GET_METHOD_PREFIX);
        }
        return StringUtils.uncapitalize(fieldName);
    }

    /**
     * Extracts the field name from a setter method.
     *
     * @param method The setter method
     * @return The field name corresponding to the setter
     */
    static String getFieldNameOfSet(final Method method) {
        String fieldName = StringUtils.removeStart(method.getName(), SET_METHOD_PREFIX);
        return StringUtils.uncapitalize(fieldName);
    }

    /**
     * Finds a field on the given object.
     *
     * @param object The object to find the field on
     * @param fieldName The name of the field to find
     * @return The Field object, or null if not found
     */
    static Field findField(final Object object, final String fieldName) {
        return findField(object.getClass(), fieldName);
    }

    /**
     * Finds a field on the given class.
     *
     * @param fieldClass The class to find the field on
     * @param fieldName The name of the field to find
     * @return The Field object, or null if not found
     */
    static Field findField(final Class<?> fieldClass, final String fieldName) {
        Map<String, Field> fieldMap = classFields.computeIfAbsent(fieldClass, clazz -> {
            var map = new ConcurrentHashMap<String, Field>();
            
            // Use parallel processing for classes with many fields
            Arrays.stream(clazz.getDeclaredFields())
                  .parallel()
                  .forEach(field -> map.put(field.getName(), field));
                  
            return map;
        });
        
        Field field = fieldMap.get(fieldName);
        if (field != null) return field;

        if (fieldClass.getSuperclass() == null) return null;

        return findField(fieldClass.getSuperclass(), fieldName);
    }

    /**
     * Determines if a method is a getter method.
     *
     * @param clazz The class containing the method
     * @param method The method to check
     * @return true if the method is a getter, false otherwise
     */
    static boolean isGetMethod(final Class<?> clazz, final Method method) {
        try {
            return findField(clazz, getFieldNameOfGet(method)) != null;
        } catch (final Exception e) {
            LOGGER.log(Level.FINE, "Error determining if method is a getter: " + method.getName(), e);
            return false;
        }
    }

    /**
     * Determines if a method is a setter method.
     *
     * @param clazz The class containing the method
     * @param method The method to check
     * @return true if the method is a setter, false otherwise
     */
    static boolean isSetMethod(final Class<?> clazz, final Method method) {
        try {
            return findField(clazz, getFieldNameOfSet(method)) != null;
        } catch (final Exception e) {
            LOGGER.log(Level.FINE, "Error determining if method is a setter: " + method.getName(), e);
            return false;
        }
    }

    /**
     * Checks if an entity has an ID method.
     *
     * @param entity The entity to check
     * @return true if the entity has a getId method, false otherwise
     */
    public static boolean hasIdMethod(final Object entity) {
        return findGetMethod(entity, GET_ID) != null;
    }

    /**
     * Gets the ID value from an entity.
     *
     * @param entity The entity to get the ID from
     * @return The ID value, or null if not available or if an error occurs
     */
    public static Object getIdValue(final Object entity) {
        try {
            final Method method = findGetMethod(entity, GET_ID);
            if (method == null) {
                return null;
            }
            return method.invoke(entity);
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get ID value from entity", e);
            return null;
        }
    }

    /**
     * Checks if a class is a List type.
     *
     * @param clazz The class to check
     * @return true if the class is a List type, false otherwise
     */
    static boolean isListType(final Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    /**
     * Checks if a class is a Set type.
     *
     * @param clazz The class to check
     * @return true if the class is a Set type, false otherwise
     */
    static boolean isSetType(final Class<?> clazz) {
        return Set.class.isAssignableFrom(clazz);
    }

    /**
     * Checks if a class is a Map type.
     *
     * @param clazz The class to check
     * @return true if the class is a Map type, false otherwise
     */
    static boolean isMapType(final Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    /**
     * Checks if a class is a collection class (List, Set, or Map).
     *
     * @param clazz The class to check
     * @return true if the class is a collection class, false otherwise
     */
    static boolean isCollectionClass(final Class<?> clazz) {
        return isListType(clazz) || isSetType(clazz) || isMapType(clazz);
    }

    /**
     * Checks if a class is an Optional type.
     *
     * @param clazz The class to check
     * @return true if the class is an Optional type, false otherwise
     */
    static boolean isOptionalType(final Class<?> clazz) {
        return Optional.class.isAssignableFrom(clazz);
    }

    /**
     * Checks if a class is one of the given types.
     *
     * @param clazz The class to check
     * @param classes The list of classes to check against
     * @return true if the class is one of the given types, false otherwise
     */
    static boolean isOneOfType(final Class<?> clazz, List<Class<?>> classes) {
        return classes.stream().anyMatch(c -> c.isAssignableFrom(clazz) || clazz.isAssignableFrom(c));
    }
}
