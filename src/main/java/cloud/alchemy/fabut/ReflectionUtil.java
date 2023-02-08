package cloud.alchemy.fabut;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionUtil {

    private static final String GET_METHOD_PREFIX = "get";
    private static final String IS_METHOD_PREFIX = "is";

    static final String SET_METHOD_PREFIX = "set";
    static final String GET_ID = "getId";

    static final String SET_ID = "setId";
    static final String ID = "id";

    private static final Map<Class<?>, Map<String, Method>> classGetMethods = new HashMap<>();
    private static final Map<Class<?>, Map<String, Method>> classSetMethods = new HashMap<>();
    private static final Map<Class<?>, Map<String, Field>> classFields = new HashMap<>();

    ReflectionUtil() {
        super();
    }

    static Class<?> getRealClass(final Class<?> classs) {
        if (classs.getName().contains("Proxy")) {
            return classs.getSuperclass();
        }
        return classs;
    }

    static Method findGetMethod(final Object object, final String methodName) {
        return findGetMethod(object.getClass(), methodName);
    }

    static Method findGetMethod(final Class<?> methodClass, final String methodName) {

        Map<String, Method> methodMap = getMethods(methodClass);
        Method method = methodMap.get(methodName);
        if (method != null) return method;

        if (methodClass.getSuperclass() == null) return null;

        return findGetMethod(methodClass.getSuperclass(), methodName);
    }

    static Map<String, Method> getMethods(Class<?> methodClass) {
        Map<String, Method> methodMap = classGetMethods.get(methodClass);
        if (methodMap == null) {
            methodMap = new HashMap<>();
            for (Method method : methodClass.getMethods()) {
                if (isGetMethod(methodClass, method)) methodMap.put(method.getName(), method);
            }
            classGetMethods.put(methodClass, methodMap);
        }
        return methodMap;
    }

    static Method findSetMethod(final Object object, final String methodName) {
        return findSetMethod(object.getClass(), methodName);
    }

    static Method findSetMethod(final Class<?> methodClass, final String methodName) {

        Map<String, Method> methodMap = setMethods(methodClass);
        Method method = methodMap.get(methodName);
        if (method != null) return method;

        if (methodClass.getSuperclass() == null) return null;

        return findSetMethod(methodClass.getSuperclass(), methodName);
    }

    static Map<String, Method> setMethods(Class<?> methodClass) {
        Map<String, Method> methodMap = classSetMethods.get(methodClass);
        if (methodMap == null) {
            methodMap = new HashMap<>();
            for (Method method : methodClass.getMethods()) {
                if (isSetMethod(methodClass, method)) methodMap.put(method.getName(), method);
            }
            classSetMethods.put(methodClass, methodMap);
        }
        return methodMap;
    }

    static String getFieldNameOfGet(final Method method) {
        String fieldName;
        if (method.getName().startsWith(IS_METHOD_PREFIX)) {
            fieldName = StringUtils.removeStart(method.getName(), IS_METHOD_PREFIX);
        } else {
            fieldName = StringUtils.removeStart(method.getName(), GET_METHOD_PREFIX);
        }
        return StringUtils.uncapitalize(fieldName);
    }

    static String getFieldNameOfSet(final Method method) {
        String fieldName = StringUtils.removeStart(method.getName(), SET_METHOD_PREFIX);
        return StringUtils.uncapitalize(fieldName);
    }

    static Field findField(final Object object, final String fieldName) {
        return findField(object.getClass(), fieldName);
    }

    static Field findField(final Class<?> fieldClass, final String fieldName) {
        Map<String, Field> fieldMap = classFields.get(fieldClass);
        if (fieldMap == null) {
            fieldMap = new HashMap<>();
            for (Field field : fieldClass.getDeclaredFields()) {
                fieldMap.put(field.getName(), field);
            }
            classFields.put(fieldClass, fieldMap);
        }
        Field field = fieldMap.get(fieldName);
        if (field != null) return field;

        if (fieldClass.getSuperclass() == null) return null;

        return findField(fieldClass.getSuperclass(), fieldName);
    }

    static boolean isGetMethod(final Class<?> classs, final Method method) {
        try {
            return findField(classs, getFieldNameOfGet(method)) != null;
        } catch (final Exception e) {
            return false;
        }
    }

    static boolean isSetMethod(final Class<?> classs, final Method method) {
        try {
            return findField(classs, getFieldNameOfSet(method)) != null;
        } catch (final Exception e) {
            return false;
        }
    }

    public static boolean hasIdMethod(final Object entity) {
        return findGetMethod(entity, GET_ID) != null;
    }

    public static Object getIdValue(final Object entity) {
        try {
            final Method method = findGetMethod(entity, GET_ID);
            return method.invoke(entity);
        } catch (final Exception e) {
            return null;
        }
    }

    static boolean isListType(final Class<?> classs) {
        return List.class.isAssignableFrom(classs);
    }

    static boolean isSetType(final Class<?> classs) {
        return Set.class.isAssignableFrom(classs);
    }

    static boolean isMapType(final Class<?> classs) {
        return Map.class.isAssignableFrom(classs);
    }

    static boolean isCollectionClass(final Class<?> classs) {
        return isListType(classs) || isSetType(classs) || isMapType(classs);
    }

    static boolean isOptionalType(final Class<?> classs) {
        return Optional.class.isAssignableFrom(classs);
    }

    static boolean isOneOfType(final Class<?> classs, List<Class<?>> classes) {
        return classes.stream().anyMatch(classs::isAssignableFrom) || classes.stream().anyMatch(a -> a.isAssignableFrom(classs));
    }
}
