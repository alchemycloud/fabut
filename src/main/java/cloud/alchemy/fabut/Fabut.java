package cloud.alchemy.fabut;

import cloud.alchemy.fabut.enums.ReferenceCheckType;
import cloud.alchemy.fabut.graph.NodesList;
import cloud.alchemy.fabut.pair.SnapshotPair;
import cloud.alchemy.fabut.property.*;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static cloud.alchemy.fabut.ReflectionUtil.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public abstract class Fabut extends Assert {

    private static final String DOT = ".";

    protected final List<Class> entityTypes = new ArrayList<>();
    protected final List<Class> complexTypes = new ArrayList<>();
    protected final List<Class> ignoredTypes = new ArrayList<>();
    protected final Map<Class, List<String>> ignoredFields = new HashMap<>();

    private final Map<Class<?>, Map<Object, CopyAssert>> dbSnapshot = new HashMap<>();
    final List<SnapshotPair> parameterSnapshot = new ArrayList<>();

    protected void customAssertEquals(Object expected, Object actual) {
        Assert.assertEquals(expected, actual);
    }

    protected List<?> findAll(final Class<?> entityClass) {
        throw new IllegalStateException("Override findAll method");
    }

    protected Object findById(final Class<?> entityClass, final Object id) {
        throw new IllegalStateException("Override findById method");
    }

    @Before
    public void before() {
        parameterSnapshot.clear();

        dbSnapshot.clear();
        for (final Class<?> entityType : entityTypes) {
            getDbSnapshot().put(entityType, new HashMap<>());
        }
    }

    @After
    public void after() {
        final FabutReport report = new FabutReport("After test assert");

        final FabutReport paremeterReport = report.getSubReport("Parameter snapshot test report");
        assertParameterSnapshot(paremeterReport);

        final FabutReport snapshotReport = report.getSubReport("Repository snapshot assert");
        assertDbSnapshot(snapshotReport);

        if (!report.isSuccess()) {
            throw new AssertionFailedError(report.getMessage());
        }
    }

    // COMMANDS
    public void takeSnapshot(final Object... parameters) {
        final FabutReport report = new FabutReport("Take snapshot");
        takeSnapshott(report, parameters);

        if (!report.isSuccess()) {
            throw new AssertionFailedError(report.getMessage());
        }
    }

    public void assertObject(final String message, final Object object, final IProperty... properties) {

        final FabutReport report = new FabutReport(message);
        if (isEntityType(object.getClass()) && doesExistInSnapshot(object)) {
            report.entityInSnapshot(object);
            throw new AssertionFailedError(report.getMessage());
        }

        assertObjectWithProperties(report, object, extractProperties(properties));

        if (!report.isSuccess()) {
            throw new AssertionFailedError(report.getMessage());
        }
    }

    public void assertObject(final Object expected, final IProperty... properties) {
        assertObject("", expected, properties);
    }

    public <T> T assertEntityWithSnapshot(final T entity, final IProperty... expectedChanges) {
        checkIfEntity(entity);
        final FabutReport report = new FabutReport("Assert with snapshot");

        if (expectedChanges.length == 0) {
            report.assertWithSnapshotMustHaveAtLeastOnChange(entity);
            throw new AssertionFailedError(report.getMessage());
        }

        final Object freshEntity = assertEntityWithSnapshot(report, entity, extractProperties(expectedChanges));

        if (!report.isSuccess()) {
            throw new AssertionFailedError(report.getMessage());
        }

        return (T) freshEntity;
    }

    public void assertEntityAsDeleted(final Object entity) {
        checkIfEntity(entity);

        final FabutReport report = new FabutReport("Assert entity as deleted");
        assertEntityAsDeleted(report, entity);
        if (!report.isSuccess()) {
            throw new AssertionFailedError(report.getMessage());
        }
    }

    public void ignoreEntity(final Object entity) {
        checkIfEntity(entity);

        final FabutReport report = new FabutReport("Ignore entity");
        ignoreEntity(report, entity);
        if (!report.isSuccess()) {
            throw new AssertionFailedError(report.getMessage());
        }
    }

    public <T> Property<T> value(final PropertyPath<T> path, final T expectedValue) {
        return new Property<>(path.getPath(), expectedValue);
    }

    public IgnoredProperty ignored(final PropertyPath<?> path) {
        return new IgnoredProperty(path.getPath());
    }

    public MultiProperties ignored(final PropertyPath<?>... paths) {
        final List<ISingleProperty> properties = new ArrayList<>();

        for (final PropertyPath<?> path : paths) {
            properties.add(ignored(path));
        }

        return new MultiProperties(properties);
    }

    public NotNullProperty notNull(final PropertyPath<?> path) {
        return new NotNullProperty(path.getPath());
    }

    public MultiProperties notNull(final PropertyPath<?>... paths) {
        final List<ISingleProperty> properties = new ArrayList<>();

        for (final PropertyPath<?> path : paths) {
            properties.add(notNull(path));
        }

        return new MultiProperties(properties);
    }

    public NullProperty isNull(final PropertyPath<?> path) {
        return new NullProperty(path.getPath());
    }

    public MultiProperties isNull(final PropertyPath<?>... paths) {
        final List<ISingleProperty> properties = new ArrayList<>();

        for (final PropertyPath<?> path : paths) {
            properties.add(isNull(path));
        }

        return new MultiProperties(properties);
    }

    public NotEmptyProperty notEmpty(final PropertyPath<?> path) {
        return new NotEmptyProperty(path.getPath());
    }

    public MultiProperties notEmpty(final PropertyPath<?>... paths) {
        final List<ISingleProperty> properties = new ArrayList<>();

        for (final PropertyPath<?> path : paths) {
            properties.add(notEmpty(path));
        }

        return new MultiProperties(properties);
    }

    public EmptyProperty isEmpty(final PropertyPath<?> path) {
        return new EmptyProperty(path.getPath());
    }

    public MultiProperties isEmpty(final PropertyPath<?>... paths) {
        final List<ISingleProperty> properties = new ArrayList<>();

        for (final PropertyPath<?> path : paths) {
            properties.add(isEmpty(path));
        }

        return new MultiProperties(properties);
    }


    //TYPE METHODS
    private void checkIfEntity(final Object entity) {
        if (entity == null) {
            throw new IllegalStateException("assertEntityWithSnapshot cannot take null entity!");
        }

        if (!isEntityType(entity.getClass())) {
            throw new IllegalStateException(entity.getClass() + " is not registered as entity type");
        }
    }

    private boolean isEntityType(final Class<?> classs) {
        return isOneOfType(classs, entityTypes);
    }

    private boolean isComplexType(final Class<?> classs) {
        return isOneOfType(classs, complexTypes);
    }

    private boolean isIgnoredType(final Class<?> classs) {
        return isOneOfType(classs, ignoredTypes);
    }

    private boolean isIgnoredField(Class clazz, String fieldName) {
        return ignoredFields.getOrDefault(clazz, Collections.emptyList()).contains(fieldName);
    }

    // PROPERTIES
    private List<Method> getGetMethods(final Object object) {

        final List<Method> getMethods = new ArrayList<>();
        final List<Method> getMethodsComplexType = new ArrayList<>();
        final boolean isEntityClass = isEntityType(object.getClass());

        final Method[] allMethods = object.getClass().getMethods();
        for (final Method method : allMethods) {
            if (isGetMethod(object.getClass(), method) && !(isEntityClass && isCollectionClass(method.getReturnType()))) {

                // complex or entity type get methods inside object come last in list,
                // this is important because otherwise inner object asserts will possibly 'eat up' expected properties of parent object during asserts
                if (isComplexType(object.getClass()) || isEntityType(object.getClass())) {
                    getMethodsComplexType.add(method);
                } else {
                    getMethods.add(method);
                }
            }
        }
        getMethods.addAll(getMethodsComplexType);
        return getMethods;

    }

    List<ISingleProperty> removeParentQualification(final String parentPropertyName, final List<ISingleProperty> properties) {

        final String parentPrefix = parentPropertyName + DOT;
        for (final ISingleProperty property : properties) {
            final String path = StringUtils.removeStart(property.getPath(), parentPrefix);
            property.setPath(path);
        }
        return properties;
    }

    ISingleProperty obtainProperty(final Object field, final String propertyPath, final List<ISingleProperty> properties) {

        final ISingleProperty property = getPropertyFromList(propertyPath, properties);
        if (property != null) {
            return property;
        }
        return value(new PropertyPath(propertyPath), field);
    }

    ISingleProperty getPropertyFromList(final String propertyPath, final List<ISingleProperty> properties) {

        final Iterator<ISingleProperty> iterator = properties.iterator();
        while (iterator.hasNext()) {
            final ISingleProperty property = iterator.next();
            if (property.getPath().equalsIgnoreCase(propertyPath)) {
                iterator.remove();
                return property;
            }
        }
        return null;
    }

    ReferenceCheckType checkByReference(final FabutReport report, Object expected, Object actual, final Optional<String> propertyName) {

        if (expected == actual) {
            //report.asserted(pair, propertyName);
            return ReferenceCheckType.EQUAL_REFERENCE;
        }

        if (expected == null ^ actual == null) {
            report.assertFail(propertyName.orElse(""), expected, actual);
            return ReferenceCheckType.EXCLUSIVE_NULL;
        }
        return ReferenceCheckType.NOT_NULL_PAIR;
    }

    List<ISingleProperty> extractProperties(final IProperty... properties) {
        final ArrayList<ISingleProperty> list = new ArrayList<>();

        for (final IProperty property : properties) {
            if (property instanceof ISingleProperty) {
                list.add((ISingleProperty) property);
            } else {
                list.addAll(((IMultiProperties) property).getProperties());
            }
        }

        return list;
    }

    List<ISingleProperty> extractPropertiesWithMatchingParent(final String parent, final List<ISingleProperty> properties) {

        final List<ISingleProperty> extracts = new LinkedList<>();
        final Iterator<ISingleProperty> iterator = properties.iterator();
        while (iterator.hasNext()) {
            final ISingleProperty property = iterator.next();
            if (property.getPath().startsWith(parent + DOT) || property.getPath().equalsIgnoreCase(parent)) {
                extracts.add(property);
                iterator.remove();
            }
        }
        return extracts;
    }

    boolean hasInnerProperties(final String parent, final List<ISingleProperty> properties) {

        for (final ISingleProperty property : properties) {
            if (property.getPath().startsWith(parent + DOT)) {
                return true;
            }
        }
        return false;
    }

    // COPY
    private Object createCopyObject(final Object object, final NodesList nodes) throws CopyException {

        Object copy = nodes.getExpected(object);
        if (copy != null) {
            return copy;
        }

        copy = createEmptyCopyOf(object);
        if (copy == null) {
            throw new CopyException(object.getClass().getSimpleName());
        }
        nodes.addPair(copy, object);

        final boolean isEntityType = isEntityType(object.getClass());

        try {
            final Class<?> classObject = object.getClass();
            final Method[] methods = classObject.getMethods();
            for (final Method method : methods) {
                if (method.getName().equals(GET_ID)) {
                    Field field = getDeclaredFieldFromClassOrSupperClass(object.getClass(), "id");
                    if (field == null) {
                        throw new CopyException(object.getClass().getSimpleName());
                    }
                    field.setAccessible(true);
                    field.set(copy, field.get(object));
                }
            }
            for (final Method method : methods) {
                if (!method.getName().equals(GET_ID) && isGetMethod(object.getClass(), method) && method.getParameterAnnotations().length == 0 && !(isEntityType && isCollectionClass(method.getReturnType()))) {
                    final String declaredField = getFieldName(method);
                    Field field = getDeclaredFieldFromClassOrSupperClass(object.getClass(), declaredField);
                    if (field == null) {
                        throw new CopyException(object.getClass().getSimpleName());
                    }
                    field.setAccessible(true);
                    field.set(copy, copyProperty(field.get(object), nodes));
                }
            }
        } catch (Exception e) {
            throw new CopyException(object.getClass().getSimpleName());
        }
        return copy;
    }

    private Object createEmptyCopyOf(final Object object) {
        try {
            return object.getClass().getConstructor().newInstance();
        } catch (final Exception e) {
            return null;
        }
    }

    private Object copyProperty(final Object propertyForCopying, final NodesList nodes) throws CopyException {
        if (propertyForCopying == null) {
            // its null we shouldn't do anything
            return null;
        }

        if (isComplexType(propertyForCopying.getClass())) {
            // its complex object, we need its copy
            return createCopyObject(propertyForCopying, nodes);
        }

        if (isEntityType(propertyForCopying.getClass())) {
            // its entity object, we need its copy
            if (hasIdMethod(propertyForCopying)) {
                return createCopyObject(propertyForCopying, nodes);
            } else {
                return propertyForCopying;
            }
        }

        if (isListType(propertyForCopying.getClass())) {
            // just creating new list with same elements
            return copyList((List<?>) propertyForCopying, nodes);
        }

        if (isOptionalType(propertyForCopying.getClass())) {
            // just creating new list with same elements
            return copyOptional((Optional<?>) propertyForCopying, nodes);
        }

        if (isSetType(propertyForCopying.getClass())) {
            // just creating new set with same elements
            return copySet((Set<?>) propertyForCopying, nodes);
        }

        if (isMapType(propertyForCopying.getClass())) {
            return copyMap((Map) propertyForCopying, nodes);
        }

        // if its not list or some complex type same object will be added.
        return propertyForCopying;

    }

    private Object copyMap(final Map propertyForCopying, NodesList nodes) throws CopyException {

        final Map mapCopy = new HashMap();
        for (final Object key : propertyForCopying.keySet()) {
            mapCopy.put(key, copyProperty(propertyForCopying.get(key), nodes));
        }
        return mapCopy;
    }

    private <T> List<T> copyList(final List<T> list, NodesList nodes) throws CopyException {
        final List<T> copyList = new ArrayList<>();
        for (final T t : list) {
            copyList.add((T) copyProperty(t, nodes));
        }
        return copyList;
    }

    private <T> Optional<T> copyOptional(final Optional<T> optional, NodesList nodes) throws CopyException {

        if (optional.isPresent()) {
            return of((T) copyProperty(optional.get(), nodes));
        } else {
            return Optional.empty();
        }
    }

    private <T> Set<T> copySet(final Set<T> set, NodesList nodes) throws CopyException {

        final Set<T> copySet = new HashSet<>();
        for (final T t : set) {
            copySet.add((T) copyProperty(t, nodes));
        }
        return copySet;
    }

    // ASSERT
    void assertEntityPair(final FabutReport report, final Optional<String> propertyName, Object expected, Object actual, final List<ISingleProperty> properties, final NodesList nodesList) {

        if (propertyName.isPresent()) {
            assertEntityById(report, propertyName.get(), expected, actual);
        } else {
            assertSubfields(report, empty(), expected, actual, properties, nodesList);
        }

    }

    void assertEntityAsDeleted(final FabutReport report, final Object entity) {

        ignoreEntity(report, entity);

        final Object findById = findById(entity.getClass(), getIdValue(entity));
        final boolean isDeletedInRepository = findById == null;

        if (!isDeletedInRepository) {
            report.notDeletedInRepository(entity);
        }

    }

    void ignoreEntity(final FabutReport report, final Object entity) {
        markAsAsserted(report, entity);
    }

    Object assertEntityWithSnapshot(final FabutReport report, final Object entity, final List<ISingleProperty> properties) {

        final Object id = getIdValue(entity);
        final Class<?> entityClass = getRealClass(entity.getClass());

        final Map<Object, CopyAssert> map = dbSnapshot.get(entityClass);

        final CopyAssert copyAssert = map.get(id);
        if (copyAssert != null) {
            final Object expected = copyAssert.getEntity();
            final Object freshEntity = findById(entityClass, id);
            assertObjects(report, expected, freshEntity, properties);
            return freshEntity;
        } else {
            report.noEntityInSnapshot(entity);
            return null;
        }

    }

    void afterAssertObject(final FabutReport report, final Object object) {
        if (isEntityType(object.getClass())) {
            markAsAsserted(report, object);
        }
    }

    private void assertEntityById(final FabutReport report, final String propertyName, Object expected, Object actual) {

        final Object expectedId = ReflectionUtil.getIdValue(expected);
        final Object actualId = ReflectionUtil.getIdValue(actual);
        try {
            customAssertEquals(expectedId, actualId);
        } catch (final AssertionError e) {
            report.assertFail(propertyName, expected, actual);
        }
    }

    void assertObjectWithProperties(final FabutReport report, final Object actual, final List<ISingleProperty> expectedProperties) {

        if (actual == null) {
            report.nullReference();
            return;
        }

        final List<Method> methods = getGetMethods(actual);

        for (final Method method : methods) {

            final String fieldName = ReflectionUtil.getFieldName(method);
            final boolean ignoredField = isIgnoredField(actual.getClass(), fieldName);

            final ISingleProperty property = getPropertyFromList(fieldName, expectedProperties);
            try {
                if (property != null) {
                    assertProperty(report, fieldName, property, method.invoke(actual), expectedProperties, new NodesList());
                } else if (!ignoredField && hasInnerProperties(fieldName, expectedProperties)) {
                    assertInnerProperty(report, method.invoke(actual), expectedProperties, fieldName);
                } else if (!ignoredField) {
                    // there is no matching property for field
                    report.noPropertyForField(fieldName, method.invoke(actual));
                }
            } catch (final Exception e) {
                report.uncallableMethod(method, actual);
            }
        }

        if (!expectedProperties.isEmpty()) {
            for (ISingleProperty singleProperty : expectedProperties) {
                report.excessExpectedProperty(singleProperty.getPath());
            }
        }

        afterAssertObject(report, actual);
    }

    void assertInnerProperty(final FabutReport report, final Object actual, final List<ISingleProperty> properties, final String parent) {
        final List<ISingleProperty> extracts = extractPropertiesWithMatchingParent(parent, properties);
        removeParentQualification(parent, extracts);

        assertObjectWithProperties(report, actual, extracts);
    }

    void assertInnerObject(final FabutReport report, final Object expected, final Object actual, final List<ISingleProperty> properties, final String parent) {

        final List<ISingleProperty> extracts = extractPropertiesWithMatchingParent(parent, properties);
        removeParentQualification(parent, extracts);

        assertObjects(report, expected, actual, extracts);
    }

    void assertObjects(final FabutReport report, final Object expected, final Object actual, final List<ISingleProperty> expectedChangedProperties) {

        assertPair(report, empty(), expected, actual, expectedChangedProperties, new NodesList());

        afterAssertObject(report, actual);
    }

    void assertPair(final FabutReport report, final Optional<String> propertyName, Object expected, Object actual, final List<ISingleProperty> properties, final NodesList nodesList) {

        final ReferenceCheckType referenceCheck = checkByReference(report, expected, actual, propertyName);

        if (referenceCheck == ReferenceCheckType.EQUAL_REFERENCE) {
            return;
        }
        if (referenceCheck == ReferenceCheckType.EXCLUSIVE_NULL) {
            return;
        }

        // check if any of the expected/actual object is recurring in nodes list
        switch (nodesList.nodeCheck(expected, actual)) {
            case SINGLE_NODE:
                report.checkByReference(propertyName.orElse(""), actual);
                return;
            case CONTAINS_PAIR:
                return;
            case NEW_PAIR:

                nodesList.addPair(expected, actual);

                if (isIgnoredType(expected.getClass())) {
                    report.ignoredType(expected.getClass());

                } else if (isComplexType(expected.getClass())) {
                    assertSubfields(report, propertyName, expected, actual, properties, nodesList);

                } else if (isEntityType(expected.getClass())) {
                    assertEntityPair(report, propertyName, expected, actual, properties, nodesList);

                } else if (isListType(expected.getClass()) && isListType(actual.getClass())) {
                    assertList(report, propertyName, (List) expected, (List) actual, properties, nodesList);

                } else if (isMapType(expected.getClass()) && isMapType(actual.getClass())) {
                    assertMap(report, propertyName, (Map) expected, (Map) actual, properties, nodesList);

                } else if (isOptionalType(expected.getClass()) && isOptionalType(actual.getClass())) {
                    assertOptional(report, propertyName, (Optional) expected, (Optional) actual, properties, nodesList);

                } else {
                    assertPrimitives(report, propertyName, expected, actual);
                }

                break;
        }
    }

    private void assertSubfields(final FabutReport report, final Optional<String> propertyName, Object expected, Object actual, final List<ISingleProperty> properties, final NodesList nodesList) {

        final ArrayList<ISingleProperty> propertiesCopy = new ArrayList<>(properties);

        final List<Method> getMethods = getGetMethods(expected);

        for (final Method expectedMethod : getMethods) {
            final String fieldName = ReflectionUtil.getFieldName(expectedMethod);
            if (!isIgnoredField(expected.getClass(), fieldName)) {
                try {

                    final ISingleProperty property = obtainProperty(expectedMethod.invoke(expected), fieldName, properties);
                    final Method actualMethod = getGetMethod(expectedMethod.getName(), actual);
                    assertProperty(report, fieldName, property, actualMethod.invoke(actual), properties, nodesList);

                    if (propertiesCopy.contains(property)) {
                        final FabutReport optimisationReport = new FabutReport();
                        assertProperty(optimisationReport, fieldName, value(new PropertyPath(fieldName), expectedMethod.invoke(expected)), actualMethod.invoke(actual), new ArrayList<>(), new NodesList());

                        if (optimisationReport.isSuccess()) {
                            report.notNecessaryAssert(fieldName, actual);
                        }
                    }

                } catch (final Exception e) {
                    report.uncallableMethod(expectedMethod, actual);
                }
            }
        }

    }

    private void assertPrimitives(final FabutReport report, final Optional<String> propertyName, Object expected, Object actual) {
        try {
            customAssertEquals(expected, actual);
        } catch (final AssertionError e) {
            report.assertFail(propertyName.orElse(""), expected, actual);
        }
    }

    void assertProperty(final FabutReport report, final String fieldName, final ISingleProperty expected, final Object actual, final List<ISingleProperty> properties, final NodesList nodesList) {

        removeParentQualification(fieldName, properties);

        if (expected instanceof NotNullProperty) { // expected any not null value
            if (actual == null) {
                report.notNullProperty(expected.getPath());
            }
        } else if (expected instanceof NullProperty) { // expected null value
            if (actual != null) {
                report.nullProperty(expected.getPath());
            }
        } else if (expected instanceof NotEmptyProperty) { // expected any not empty value
            if (!(actual instanceof Optional && ((Optional) actual).isPresent())) {
                report.notEmptyProperty(expected.getPath());
            }
        } else if (expected instanceof EmptyProperty) {// expected empty value
            if (!(actual instanceof Optional && !((Optional) actual).isPresent())) {
                report.emptyProperty(expected.getPath());
            }
        } else if (expected instanceof IgnoredProperty) {
            report.reportIgnoreProperty(expected.getPath());

        } else if (expected instanceof Property) {
            final Object expectedValue = ((Property) expected).getValue();
            assertPair(report, of(expected.getPath()), expectedValue, actual, properties, nodesList);
        } else {
            throw new IllegalStateException();
        }

    }

    void assertList(final FabutReport report, final Optional<String> propertyName, final List expected, final List actual, final List<ISingleProperty> properties, final NodesList nodesList) {

        // check sizes
        if (expected.size() != actual.size()) {
            report.listDifferentSizeComment(propertyName.orElse(""), expected.size(), actual.size());
        } else {
            // assert every element by index

            for (int i = 0; i < actual.size(); i++) {
                report.assertingListElement(propertyName.orElse(""), i);
                assertObjects(report, expected.get(i), actual.get(i), properties);
            }
        }
    }

    void assertMap(final FabutReport report, final Optional<String> propertyName, final Map expected, final Map actual, final List<ISingleProperty> properties, final NodesList nodesList) {

        final TreeSet expectedKeys = new TreeSet(expected.keySet());
        final TreeSet actualKeys = new TreeSet(actual.keySet());
        final TreeSet expectedKeysCopy = new TreeSet(expectedKeys);

        expectedKeysCopy.retainAll(actualKeys);

        for (final Object key : expectedKeysCopy) {
            report.assertingMapKey(key);
            assertPair(report, propertyName, expected.get(key), actual.get(key), properties, nodesList);
        }
        assertExcessExpected(propertyName, report, expected, expectedKeysCopy, actualKeys);
        assertExcessActual(propertyName, report, actual, expectedKeysCopy, actualKeys);
    }

    private void assertOptional(final FabutReport report, Optional<String> propertyName, Optional expected, Optional actual, final List<ISingleProperty> properties, final NodesList nodesList) {

        if (!expected.isPresent() && !actual.isPresent()) {
            return;
        }

        if (expected.isPresent() ^ actual.isPresent()) {
            report.assertFail(propertyName.orElse(""), expected, actual);
            return;
        }

        final Object expectedValue = expected.get();
        final Object actualValue = actual.get();

        assertPair(report, propertyName, expectedValue, actualValue, properties, nodesList);
    }

    void assertExcessExpected(final Optional<String> propertyName, final FabutReport report, final Map expected, final TreeSet expectedKeys, final TreeSet actualKeys) {

        final TreeSet expectedKeysCopy = new TreeSet(expectedKeys);
        expectedKeysCopy.removeAll(actualKeys);
        if (expectedKeysCopy.size() > 0) {
            for (final Object key : expectedKeysCopy) {
                report.excessExpectedMap(key);
            }
        }
    }

    void assertExcessActual(final Optional<String> propertyName, final FabutReport report, final Map actual, final TreeSet expectedKeys, final TreeSet actualKeys) {

        final TreeSet actualKeysCopy = new TreeSet(actualKeys);
        actualKeysCopy.removeAll(expectedKeys);
        if (actualKeysCopy.size() > 0) {
            for (final Object key : actualKeysCopy) {
                report.excessActualMap(key);
            }
        }

    }

    // SNAPSHOT
    void takeSnapshott(final FabutReport report, final Object... parameters) {

        for (final Object object : parameters) {
            try {
                final SnapshotPair snapshotPair = new SnapshotPair(object, createCopyObject(object, new NodesList()));
                parameterSnapshot.add(snapshotPair);
            } catch (final CopyException e) {
                report.noCopy(object);
            }
        }

        for (final Map.Entry<Class<?>, Map<Object, CopyAssert>> entry : dbSnapshot.entrySet()) {
            final List<?> findAll = findAll(entry.getKey());

            for (final Object entity : findAll) {
                try {
                    final Object copy = createCopyObject(entity, new NodesList());
                    entry.getValue().put(ReflectionUtil.getIdValue(entity), new CopyAssert(copy));
                } catch (final CopyException e) {
                    report.noCopy(entity);
                }
            }
        }

    }

    private Map getAfterEntities(final Class<?> clazz) {
        final Map afterEntities = new TreeMap();
        final List entities = findAll(clazz);
        for (final Object entity : entities) {
            final Object id = ReflectionUtil.getIdValue(entity);
            if (id != null) {
                afterEntities.put(id, entity);
            }
        }
        return afterEntities;
    }

    private Map<Class<?>, Map<Object, CopyAssert>> getDbSnapshot() {
        return dbSnapshot;
    }

    private boolean doesExistInSnapshot(final Object entity) {
        final Object id = getIdValue(entity);
        final Class<?> entityClass = entity.getClass();

        final Map<Object, CopyAssert> map = dbSnapshot.get(entityClass);

        return map != null && map.get(id) != null;
    }

    void assertParameterSnapshot(final FabutReport report) {

        for (final SnapshotPair snapshotPair : parameterSnapshot) {
            assertObjects(report.getSubReport("Snapshot pair assert"), snapshotPair.getExpected(), snapshotPair.getActual(),
                    new LinkedList<>());
        }

    }

    void markAsAsserted(final FabutReport report, final Object entity) {
        final Class<?> actualType = entity.getClass();

        final Object id = getIdValue(entity);
        if (id == null) {
            report.idNull(actualType);
            return;
        }

        markAsserted(id, entity, actualType);
    }

    private void markAsserted(final Object id, final Object copy, final Class<?> actualType) {
        final Map<Object, CopyAssert> map = dbSnapshot.get(getRealClass(actualType));
        CopyAssert copyAssert = map.get(id);
        if (copyAssert == null) {
            copyAssert = new CopyAssert(copy);
            map.put(getIdValue(copy), copyAssert);
        }
        copyAssert.setAsserted(true);
    }

    void assertDbSnapshot(final FabutReport report) {
        // assert entities by classes
        for (final Map.Entry<Class<?>, Map<Object, CopyAssert>> snapshotEntry : dbSnapshot.entrySet()) {

            final Map<Object, Object> afterEntities = getAfterEntities(snapshotEntry.getKey());
            final TreeSet beforeIds = new TreeSet(snapshotEntry.getValue().keySet());
            final TreeSet afterIds = new TreeSet(afterEntities.keySet());

            checkNotExistingInAfterDbState(beforeIds, afterIds, snapshotEntry.getValue(), report);
            checkNewToAfterDbState(beforeIds, afterIds, afterEntities, report);
            assertDbSnapshotWithAfterState(beforeIds, afterIds, snapshotEntry.getValue(), afterEntities, report);

        }

    }

    void checkNotExistingInAfterDbState(final TreeSet beforeIds, final TreeSet afterIds, final Map<Object, CopyAssert> beforeEntities, final FabutReport report) {

        final TreeSet beforeIdsCopy = new TreeSet(beforeIds);

        // does difference between db snapshot and after db state
        beforeIdsCopy.removeAll(afterIds);
        for (final Object id : beforeIdsCopy) {
            final CopyAssert copyAssert = beforeEntities.get(id);
            if (!copyAssert.isAsserted()) {

                report.noEntityInSnapshot(copyAssert.getEntity());
            }
        }
    }

    void checkNewToAfterDbState(final TreeSet beforeIds, final TreeSet afterIds, final Map<Object, Object> afterEntities, final FabutReport report) {

        final TreeSet afterIdsCopy = new TreeSet(afterIds);

        // does difference between after db state and db snapshot
        afterIdsCopy.removeAll(beforeIds);
        for (final Object id : afterIdsCopy) {
            final Object entity = afterEntities.get(id);

            report.entityNotAssertedInAfterState(entity);
        }

    }

    void assertDbSnapshotWithAfterState(final TreeSet beforeIds, final TreeSet afterIds, final Map<Object, CopyAssert> beforeEntities, final Map<Object, Object> afterEntities, final FabutReport report) {

        final TreeSet beforeIdsCopy = new TreeSet(beforeIds);
        // does intersection between db snapshot and after db state
        beforeIdsCopy.retainAll(afterIds);

        for (final Object id : beforeIdsCopy) {
            if (!beforeEntities.get(id).isAsserted()) {
                assertObjects(report, beforeEntities.get(id).getEntity(), afterEntities.get(id),
                        new LinkedList<>());
            }
        }

    }


}
