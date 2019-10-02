package cloud.alchemy.fabut;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionUtil {

    private static final String GET_METHOD_PREFIX = "get";
    private static final String IS_METHOD_PREFIX = "is";
    static final String GET_ID = "getId";

    ReflectionUtil() {
        super();
    }

    static boolean isGetMethod(final Class<?> classs, final Method method) {
        try {
            if (method.getName().startsWith(IS_METHOD_PREFIX)) {
                // if field type is primitive boolean
                return classs.getDeclaredField(getFieldName(method)).getType() == boolean.class;
            }
            return method.getName().startsWith(GET_METHOD_PREFIX) && findField(classs, getFieldName(method)) != null;
        } catch (final Exception e) {
            return false;
        }
    }


    static Method getGetMethod(final String methodName, final Object object) throws Exception {
        return object.getClass().getMethod(methodName);
    }

    static String getFieldName(final Method method) {
        String fieldName;
        if (method.getName().startsWith(IS_METHOD_PREFIX)) {
            fieldName = StringUtils.removeStart(method.getName(), IS_METHOD_PREFIX);
        } else {
            fieldName = StringUtils.removeStart(method.getName(), GET_METHOD_PREFIX);
        }
        return StringUtils.uncapitalize(fieldName);
    }

    private static Field findField(final Class<?> fieldClass, final String fieldName) {
        if (fieldClass == null) {
            return null;
        }
        try {
            return fieldClass.getDeclaredField(fieldName);
        } catch (final Exception e) {
            return findField(fieldClass.getSuperclass(), fieldName);
        }
    }

    public static Field getDeclaredFieldFromClassOrSupperClass(Class<?> clazz, String declaredField) {
        Class<?> tmpClass = clazz;
        Field field;
        do {
            try {
                field = tmpClass.getDeclaredField(declaredField);
                return field;
            } catch (NoSuchFieldException e) {
                tmpClass = tmpClass.getSuperclass();
            }
        } while (tmpClass != null);

        return null;
    }


    public static boolean hasIdMethod(final Object entity) {
        try {
            entity.getClass().getMethod(GET_ID);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    public static Object getIdValue(final Object entity) {
        try {
            final Method method = entity.getClass().getMethod(GET_ID);
            return method.invoke(entity);
        } catch (final Exception e) {
            return null;
        }
    }

    static boolean isListType(final Class classs) {
        return List.class.isAssignableFrom(classs);
    }

    static boolean isSetType(final Class classs) {
        return Set.class.isAssignableFrom(classs);
    }

    static boolean isMapType(final Class classs) {
        return Map.class.isAssignableFrom(classs);
    }

    static boolean isCollectionClass(final Class classs) {
        return isListType(classs) || isSetType(classs) || isMapType(classs);
    }

    static boolean isOptionalType(final Class classs) {
        return Optional.class.isAssignableFrom(classs);
    }

    static boolean isOneOfType(final Class classs, List<Class> classes) {
        return classes.stream().anyMatch(classs::isAssignableFrom);
    }




}
