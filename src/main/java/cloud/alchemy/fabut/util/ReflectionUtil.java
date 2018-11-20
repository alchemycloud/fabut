package cloud.alchemy.fabut.util;

import cloud.alchemy.fabut.enums.AssertableType;
import cloud.alchemy.fabut.exception.CopyException;
import cloud.alchemy.fabut.graph.NodesList;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.*;
import java.util.*;

/**
 * Util class for reflection logic needed by testutil.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @author Andrej Miletic
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class ReflectionUtil {

    /**
     * The Constant GET_METHOD_PREFIX.
     */
    private static final String GET_METHOD_PREFIX = "get";

    /**
     * The Constant IS_METHOD_PREFIX.
     */
    private static final String IS_METHOD_PREFIX = "is";

    /**
     * The Constant GET_ID.
     */
    private static final String GET_ID = "getId";

    /**
     * The Constant SET_METHOD_PREFIX.
     */
    protected static final String SET_METHOD_PREFIX = "set";

    /**
     * Instantiates a new reflection util.
     */
    private ReflectionUtil() {
        super();
    }

    /**
     * Check if specified method of class Object is get method. Primitive boolean type fields have "is" for prefix of
     * their get method and all other types have "get" prefix for their get method so this method checks if field name
     * gotten from method name has a matched field name in the class X. Methods with prefix "is" have to have underlying
     * field of primitive boolean class.
     *
     * @param classs class that method belongs to
     * @param method thats checking
     * @return <code>true</code> if method is "real" get method, <code>false</code> otherwise
     */
    public static boolean isGetMethod(final Class<?> classs, final Method method, Map<Class<?>, List<String>> ignoredFields) {
        try {
            if (isIgnoredField(ignoredFields, classs, getFieldName(method))) {
                return false;
            }
            if (method.getName().startsWith(IS_METHOD_PREFIX)) {
                // if field type is primitive boolean
                return classs.getDeclaredField(getFieldName(method)).getType() == boolean.class;
            }
            return method.getName().startsWith(GET_METHOD_PREFIX) && findField(classs, getFieldName(method)) != null;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Get field name from specified get method. It differs get methods for regular objects and primitive boolean fields
     * by their get method prefix. ("is" is the prefix for primitive boolean get method)
     *
     * @param method that is checked
     * @return field name represented by specified get method
     */
    public static String getFieldName(final Method method) {
        String fieldName;
        if (method.getName().startsWith(IS_METHOD_PREFIX)) {
            fieldName = StringUtils.removeStart(method.getName(), IS_METHOD_PREFIX);
        } else {
            fieldName = StringUtils.removeStart(method.getName(), GET_METHOD_PREFIX);
        }
        return StringUtils.uncapitalize(fieldName);
    }

    /**
     * Searches trough property class inheritance tree for field with specified name. Starting from property class
     * method recursively climbs higher in the inheritance tree until it finds field with specified name or reached
     * object in which case returns null.
     *
     * @param fieldClass class of the field.
     * @param fieldName  name of the field
     * @return {@link Field} with specified name, otherwise <code>null</code>
     */
    public static Field findField(final Class<?> fieldClass, final String fieldName) {
        if (fieldClass == null) {
            return null;
        }
        try {
            return fieldClass.getDeclaredField(fieldName);
        } catch (final Exception e) {
            return findField(fieldClass.getSuperclass(), fieldName);
        }
    }

    /**
     * Determines if specified object is instance of {@link List}.
     *
     * @param object unidentified object
     * @return <code>true</code> if specified object is instance of {@link List} , <code>false</code> otherwise
     */
    public static boolean isListType(final Object object) {
        return object instanceof List;
    }

    public static boolean isCollectionClass(final Class classs) {
        return classs.isAssignableFrom(List.class) || classs.isAssignableFrom(Set.class) || classs.isAssignableFrom(Map.class);
    }

    /**
     * Determines if specified object is instance of {@link Set}.
     *
     * @param object unidentified object
     * @return <code>true</code> if specified object is instance of {@link Set} , <code>false</code> otherwise
     */
    public static boolean isSetType(final Object object) {
        return object instanceof Set;
    }

    /**
     * Determines if specified object is instance of {@link Map}.
     *
     * @param object the object that is checked
     * @return <code>true</code> if it is a map, <code>false</code> otherwise
     */
    public static boolean isMapType(final Object object) {
        return object instanceof Map;
    }

    /**
     * Check if specified class is contained in entity types.
     *
     * @param object that's checked
     * @param types map of all types by their Assertable type
     * @return <code>true</code> if specified class is contained in entity types, <code>false</code> otherwise.
     */
    public static boolean isEntityType(final Class<?> object, final Map<AssertableType, List<Class<?>>> types) {

        if (types.get(AssertableType.ENTITY_TYPE) != null) {

            final boolean isEntity = types.get(AssertableType.ENTITY_TYPE).contains(object);

            // necessary tweak for hibernate beans witch in some cases are fetched as proxy objects
            final boolean isSuperClassEntity = types.get(AssertableType.ENTITY_TYPE).contains(object.getSuperclass());
            return isEntity || isSuperClassEntity;
        }
        return false;
    }

    /**
     * Check if specified class is contained in complex types.
     *
     * @param classs       that's checking
     * @param types map of all types by their Assertable type
     * @return <code>true</code> if specified class is contained in complex types, <code>false</code> otherwise.
     */
    public static boolean isComplexType(final Class<?> classs, final Map<AssertableType, List<Class<?>>> types) {
        return types.get(AssertableType.COMPLEX_TYPE).contains(classs);
    }

    /**
     * Check if specified class is contained in ignored types.
     *
     * @param classs       thats checked
     * @param types map of all types by their Assertable type
     * @return <code>true</code> if specified class is contained in ignored types, <code>false</code> otherwise.
     */
    public static boolean isIgnoredType(final Class<?> classs, final Map<AssertableType, List<Class<?>>> types) {
        return types.get(AssertableType.IGNORED_TYPE).contains(classs);
    }

    /**
     * Check if specified fieldName is ignored for the specified class
     *
     * @param ignoredFields all ignored fields by classes
     * @param clazz class for which we check if field is ignored
     * @param fieldName field name for which we are checking ig its ignored
     * @return <code>true</code> if field is ignored, otherwise <code>false</code>
     */
    public static boolean isIgnoredField(Map<Class<?>, List<String>> ignoredFields, Class clazz, String fieldName) {
        return ignoredFields.getOrDefault(clazz, Collections.emptyList()).contains(fieldName);
    }

    /**
     * Checks if object is ignored type.
     *
     * @param firstObject  that is checked
     * @param secondObject that is checked
     * @param types map of all types by their Assertable type
     * @return <code>true</code> if type of expected or actual is ignored type, <code>false</code> otherwise.
     */
    public static boolean isIgnoredType(final Object firstObject, final Object secondObject,
                                        final Map<AssertableType, List<Class<?>>> types) {

        if (secondObject != null) {
            return isIgnoredType(secondObject.getClass(), types);
        }

        if (firstObject != null) {
            return isIgnoredType(firstObject.getClass(), types);
        }

        return false;
    }

    /**
     * Gets entity's id value.
     * @param entity - entity from which id is taken
     * @return {@link Number} if specified entity id field and matching get method, <code>null</code> otherwise.
     */
    public static Object getIdValue(final Object entity) {
        try {
            final Method method = entity.getClass().getMethod(GET_ID);
            return method.invoke(entity);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Extracts all "real" get methods for object of class Object in a list and returns them. "Real" get methods are
     * those methods who have matching property in the class with the name equal to get method's name uncapitalized and
     * without "get" prefix.
     *
     * @param object       instance of class X
     * @param types map of all types by their Assertable type
     * @return {@link List} of real "get" methods of class X
     */
    public static List<Method> getGetMethods(final Object object, final Map<AssertableType, List<Class<?>>> types, final Map<Class<?>, List<String>> ignoredFields) {

        final List<Method> getMethods = new ArrayList<>();
        final List<Method> getMethodsComplexType = new ArrayList<>();
        final boolean isEntityClass = isEntityType(object.getClass(), types);

        final Method[] allMethods = object.getClass().getMethods();
        for (final Method method : allMethods) {
            if (isGetMethod(object.getClass(), method, ignoredFields) && !(isEntityClass && isCollectionClass(method.getReturnType()))
                    && !isIgnoredField(ignoredFields, object.getClass(), getFieldName(method))) {

                final Class methodReturnType = method.getReturnType();
                final Class clazz;

                // TODO write test with List of list of something, and Optional of optional
                if (isCollectionClass(methodReturnType) || methodReturnType.isAssignableFrom(Optional.class)) {
                    clazz = getGenericType(method.getGenericReturnType());
                } else {
                    clazz = methodReturnType;
                }

                // complex or entity type get methods inside object come last in list,
                // this is important because otherwise inner object asserts will possibly 'eat up' expected properties of parent object during asserts
                if (isComplexType(clazz, types) || isEntityType(clazz, types)) {
                    getMethodsComplexType.add(method);
                } else {
                    getMethods.add(method);
                }
            }
        }
        getMethods.addAll(getMethodsComplexType);
        return getMethods;

    }

    /**
     *
     * @param genericType Type of generic container
     * @return Class that is contained in List or Optional
     */
    public static Class<?> getGenericType(Type genericType) {

        if (genericType instanceof Class) {
            Class clazz = (Class) genericType;
            if (!isCollectionClass(clazz))
                return clazz;

            return Object.class;
        }

        if (genericType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) genericType;

            if (parameterizedType.getActualTypeArguments()[0] instanceof Class && !isCollectionClass((Class)parameterizedType.getActualTypeArguments()[0])) {
                return (Class)(parameterizedType).getActualTypeArguments()[0];
            }

            if ((parameterizedType).getRawType() instanceof Class && !isCollectionClass((Class)parameterizedType.getRawType())) {
                return (Class) (parameterizedType).getRawType();
            }

            return getGenericType(parameterizedType.getActualTypeArguments()[0]);
        }

        if (genericType instanceof WildcardType) {
            final WildcardType wildcardType = (WildcardType) genericType;
            return getGenericType(wildcardType.getUpperBounds()[0]);
        }

        if (genericType instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) genericType;
            return getGenericType(typeVariable.getBounds()[0]);
        }

        if (genericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) genericType;
            return getGenericType(genericArrayType.getGenericComponentType());
        }

        return Object.class;
    }

    /**
     * Gets the object get method named.
     *
     * @param methodName the method name
     * @param object     the object
     * @return the object get method named
     * @throws Exception if it can't get the method
     */
    public static Method getGetMethod(final String methodName, final Object object) throws Exception {
        return object.getClass().getMethod(methodName);
    }

    /**
     * Gets the object type.
     *
     * @param expected the expected
     * @param actual   the actual
     * @param types    the types
     * @return the object type
     */
    public static AssertableType getObjectType(final Object expected, final Object actual,
                                               final Map<AssertableType, List<Class<?>>> types) {
        if (expected == null && actual == null) {
            return AssertableType.PRIMITIVE_TYPE;
        }

        final Class<?> typeClass = actual != null ? actual.getClass() : expected.getClass();
        if (List.class.isAssignableFrom(typeClass)) {
            return AssertableType.LIST_TYPE;
        } else if (Map.class.isAssignableFrom(typeClass)) {
            return AssertableType.MAP_TYPE;
        } else if (Optional.class.isAssignableFrom(typeClass)) {
            return AssertableType.OPTIONAL_TYPE;
        } else if (types.get(AssertableType.COMPLEX_TYPE).contains(typeClass)) {
            return AssertableType.COMPLEX_TYPE;
        } else if (types.get(AssertableType.ENTITY_TYPE).contains(typeClass)) {
            return AssertableType.ENTITY_TYPE;
        } else if (types.get(AssertableType.IGNORED_TYPE).contains(typeClass)) {
            return AssertableType.IGNORED_TYPE;
        } else {
            return AssertableType.PRIMITIVE_TYPE;
        }

    }

    /**
     * Creates a copy of specified object by creating instance with reflection and fills it using get and set method of
     * a class.
     *
     * @param object object for copying
     * @param nodes  list of objects that had been copied
     * @param types  the types
     * @return copied entity
     * @throws CopyException the copy exception
     */
    public static Object createCopyObject(final Object object, final NodesList nodes,
                                          final Map<AssertableType, List<Class<?>>> types, final Map<Class<?>, List<String>> ignoredFields) throws CopyException {

        Object copy = nodes.getExpected(object);
        if (copy != null) {
            return copy;
        }

        copy = createEmptyCopyOf(object);
        if (copy == null) {
            throw new CopyException(object.getClass().getSimpleName());
        }
        nodes.addPair(copy, object);

        final boolean isEntityType = isEntityType(object.getClass(), types);

        final Class<?> classObject = object.getClass();
        for (final Method method : classObject.getMethods()) {

            if (isGetMethod(object.getClass(), method, ignoredFields) && method.getParameterAnnotations().length == 0 && !(isEntityType && isCollectionClass(method.getReturnType()))) {
                final String propertyName = getFieldName(method);
                final Object propertyForCopying = getPropertyForCopying(object, method);
                final Object copiedProperty = copyProperty(propertyForCopying, nodes, types, ignoredFields);
                if (!invokeSetMethod(method, classObject, propertyName, copy, copiedProperty)) {
                    throw new CopyException(classObject.getSimpleName());
                }
            }
        }
        return copy;
    }

    /**
     * Creates empty copy of object using reflection to call default constructor.
     *
     * @param object object for copying
     * @return copied empty instance of specified object or <code>null</code> if default constructor can not be called
     */
    public static Object createEmptyCopyOf(final Object object) {
        try {
            return object.getClass().getConstructor().newInstance();
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Gets property for copying using reflection.
     *
     * @param object property's parent
     * @param method get method for property
     * @return property
     */
    public static Object getPropertyForCopying(final Object object, final Method method) {
        try {
            return method.invoke(object);
        } catch (final Exception e) {
            throw new AssertionFailedError(e.getMessage());
        }
    }

    /**
     * Copies property.
     *
     * @param propertyForCopying property for copying
     * @param nodes list of objects that had been copied
     * @param types map of all types by their Assertable type
     * @return copied property
     * @throws  CopyException if it can't make a copy of property
     */
    public static Object copyProperty(final Object propertyForCopying, final NodesList nodes,
                                      final Map<AssertableType, List<Class<?>>> types, final Map<Class<?>, List<String>> ignoredFields) throws CopyException {
        if (propertyForCopying == null) {
            // its null we shouldn't do anything
            return null;
        }

        if (isComplexType(propertyForCopying.getClass(), types)) {
            // its complex object, we need its copy
            return createCopyObject(propertyForCopying, nodes, types, ignoredFields);
        }

        if (isListType(propertyForCopying)) {
            // just creating new list with same elements
            return copyList((List<?>) propertyForCopying, types, ignoredFields);
        }

        if (isSetType(propertyForCopying)) {
            // just creating new set with same elements
            return copySet((Set<?>) propertyForCopying, types, ignoredFields);
        }

        if (isMapType(propertyForCopying)) {
            return copyMap((Map) propertyForCopying, types, ignoredFields);
        }

        // if its not list or some complex type same object will be added.
        return propertyForCopying;

    }

    /**
     * Creates a copy of specified map.
     *
     * @param propertyForCopying property that is to be copied
     * @param types map of all types by their Assertable type
     * @return copy of the map
     * @throws CopyException if can't make a copy
     */
    public static Object copyMap(final Map propertyForCopying, final Map<AssertableType, List<Class<?>>> types, final Map<Class<?>, List<String>> ignoredFields)
            throws CopyException {
        final Map mapCopy = new HashMap();
        for (final Object key : propertyForCopying.keySet()) {
            mapCopy.put(key, copyProperty(propertyForCopying.get(key), new NodesList(), types, ignoredFields));
        }
        return mapCopy;
    }

    /**
     * Creates a copy of specified list.
     *
     * @param <T>  type objects in the list
     * @param list list for copying
     * @param types map of all types by their Assertable type
     * @return copied list
     * @throws CopyException the copy exception
     */
    public static <T> List<T> copyList(final List<T> list, final Map<AssertableType, List<Class<?>>> types, Map<Class<?>, List<String>> ignoredFields)
            throws CopyException {
        final List<T> copyList = new ArrayList<T>();
        for (final T t : list) {
            copyList.add((T) copyProperty(t, new NodesList(), types, ignoredFields));
        }
        return copyList;
    }

    /**
     * Creates a copy of specified set.
     *
     * @param <T> type objects in the set
     * @param set set for copying
     * @param types map of all types by their Assertable type
     * @return copied list
     * @throws CopyException the copy exception
     */
    public static <T> Set<T> copySet(final Set<T> set, final Map<AssertableType, List<Class<?>>> types, Map<Class<?>, List<String>> ignoredFields)
            throws CopyException {
        final Set<T> copySet = new HashSet<T>();
        for (final T t : set) {
            copySet.add((T) copyProperty(t, new NodesList(), types, ignoredFields));
        }
        return copySet;
    }

    /**
     * Create copy of specified object and return its copy.
     *
     * @param object object for copying
     * @param types  the types
     * @return copied object
     * @throws CopyException the copy exception
     */
    public static Object createCopy(final Object object, final Map<AssertableType, List<Class<?>>> types, Map<Class<?>, List<String>> ignoredFields)
            throws CopyException {
        if (object == null) {
            return null;
        }

        if (isListType(object)) {
            final List<?> list = (List<?>) object;
            return copyList(list, types, ignoredFields);
        }

        if (isSetType(object)) {
            final Set<?> set = (Set<?>) object;
            return copySet(set, types, ignoredFields);
        }

        return createCopyObject(object, new NodesList(), types, ignoredFields);

    }

    /**
     * Invokes specified set method via reflection to set property to object.
     *
     * @param <T>            object type
     * @param method         get method for property
     * @param classObject    parent class for property
     * @param propertyName   property name
     * @param object         copied parent object
     * @param copiedProperty copied property
     * @return <code>true</code> if set method exists and it's successfully invoked, otherwise <code>false</code>.
     */
    public static <T> boolean invokeSetMethod(final Method method, final Class<?> classObject,
                                              final String propertyName, final T object, final Object copiedProperty) {
        Method setMethod = null;

        try {
            setMethod = classObject.getMethod(SET_METHOD_PREFIX + StringUtils.capitalize(propertyName),
                    method.getReturnType());
            setMethod.invoke(object, copiedProperty);
            return true;
        } catch (final NoSuchMethodException e) {
            if (isSetType(copiedProperty)) {
                try {
                    ((Set) method.invoke(object)).addAll((Set) copiedProperty);
                    return true;
                } catch (Exception e1) {
                    return false;
                }
            } else {
                return false;
            }
        } catch (final Exception e) {
            return false;
        }
    }

}
