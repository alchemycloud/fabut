package cloud.alchemy.fabut;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

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

    // Thread-safe caches for reflection operations (required for parallel snapshot taking)
    private static final Map<Class<?>, Map<String, Method>> classGetMethods = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Method>> classSetMethods = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Field>> classFields = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Class<?>> realClassCache = new ConcurrentHashMap<>();
    // Cache for fields that have been made accessible
    private static final Set<Field> accessibleFields = ConcurrentHashMap.newKeySet();
    // Separate caches for getter and setter field name extraction
    private static final Map<String, String> getterFieldNameCache = new ConcurrentHashMap<>();
    private static final Map<String, String> setterFieldNameCache = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ReflectionUtil() {
        // Utility class should not be instantiated
    }

    /**
     * Returns the real class of an object, handling proxy classes.
     * Results are cached for O(1) subsequent lookups.
     *
     * @param clazz The class to evaluate
     * @return The real class (superclass if proxy)
     */
    public static Class<?> getRealClass(final Class<?> clazz) {
        return realClassCache.computeIfAbsent(clazz, c ->
            c.getName().contains("Proxy") ? c.getSuperclass() : c
        );
    }

    /**
     * Finds a getter method on the given object.
     *
     * @param object The object to find the getter method on
     * @param methodName The name of the method to find
     * @return The Method object, or null if not found
     */
   public static Method findGetMethod(final Object object, final String methodName) {
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
//                  .parallel()
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
//                  .parallel()
                  .filter(method -> isSetMethod(clazz, method))
                  .forEach(method -> methodMap.put(method.getName(), method));
                  
            return methodMap;
        });
    }

    /**
     * Extracts the field name from a getter method.
     * Results are cached for O(1) subsequent lookups.
     *
     * @param method The getter method
     * @return The field name corresponding to the getter
     */
    static String getFieldNameOfGet(final Method method) {
        return getterFieldNameCache.computeIfAbsent(method.getName(), name -> {
            String fieldName;
            if (name.startsWith(IS_METHOD_PREFIX)) {
                fieldName = name.substring(IS_METHOD_PREFIX.length());
            } else if (name.startsWith(GET_METHOD_PREFIX)) {
                fieldName = name.substring(GET_METHOD_PREFIX.length());
            } else {
                // Not a getter method, return as-is (will fail field lookup)
                fieldName = name;
            }
            return StringUtils.uncapitalize(fieldName);
        });
    }

    /**
     * Extracts the field name from a setter method.
     * Results are cached for O(1) subsequent lookups.
     *
     * @param method The setter method
     * @return The field name corresponding to the setter
     */
    static String getFieldNameOfSet(final Method method) {
        return setterFieldNameCache.computeIfAbsent(method.getName(), name -> {
            if (name.startsWith(SET_METHOD_PREFIX)) {
                return StringUtils.uncapitalize(name.substring(SET_METHOD_PREFIX.length()));
            }
            // Not a setter method, return as-is (will fail field lookup)
            return StringUtils.uncapitalize(name);
        });
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
        final Map<String, Field> fieldMap = getFields(fieldClass);

        Field field = fieldMap.get(fieldName);
        if (field != null) return field;

        if (fieldClass.getSuperclass() == null) return null;

        return findField(fieldClass.getSuperclass(), fieldName);
    }

    /**
     * Finds a field and makes it accessible, caching the accessibility state.
     *
     * @param object The object to find the field on
     * @param fieldName The name of the field to find
     * @return The accessible Field object, or null if not found
     */
    static Field findAccessibleField(final Object object, final String fieldName) {
        Field field = findField(object.getClass(), fieldName);
        if (field != null) {
            makeAccessible(field);
        }
        return field;
    }

    /**
     * Makes a field accessible, caching the state to avoid repeated calls.
     *
     * @param field The field to make accessible
     */
    static void makeAccessible(Field field) {
        if (!accessibleFields.contains(field)) {
            field.setAccessible(true);
            accessibleFields.add(field);
        }
    }

    static Map<String, Field> getFields(Class<?> fieldClass) {
        return classFields.computeIfAbsent(fieldClass, clazz -> {
            var map = new ConcurrentHashMap<String, Field>();
            for (Field field : clazz.getDeclaredFields()) {
                map.put(field.getName(), field);
            }
            return map;
        });
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
     * Checks if a class is one of the given types (exact match or subclass).
     * Only checks if clazz is assignable TO the registered types, not the reverse.
     * This prevents parent classes from matching when only child classes are registered.
     * Uses simple for loop instead of stream for better performance.
     *
     * @param clazz The class to check
     * @param classes The list of classes to check against
     * @return true if clazz is the same as or a subclass of any registered type
     */
    static boolean isOneOfType(final Class<?> clazz, Collection<Class<?>> classes) {
        for (Class<?> c : classes) {
            if (c.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
}
