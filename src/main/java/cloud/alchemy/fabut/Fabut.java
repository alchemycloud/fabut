package cloud.alchemy.fabut;

import cloud.alchemy.fabut.enums.ReferenceCheckType;
import cloud.alchemy.fabut.graph.NodesList;
import cloud.alchemy.fabut.pair.SnapshotPair;
import cloud.alchemy.fabut.property.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.opentest4j.AssertionFailedError;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static cloud.alchemy.fabut.ReflectionUtil.*;
import static java.util.Optional.of;

public abstract class Fabut extends Assertions {

    private static final String DOT = ".";

    protected final List<Class<?>> entityTypes = new ArrayList<>();
    protected final List<Class<?>> complexTypes = new ArrayList<>();
    protected final List<Class<?>> ignoredTypes = new ArrayList<>();
    protected final Map<Class<?>, List<String>> ignoredFields = new HashMap<>();

    private final Map<Class<?>, Map<Object, CopyAssert>> dbSnapshot = new HashMap<>();
    final List<SnapshotPair> parameterSnapshot = new ArrayList<>();

    protected void customAssertEquals(Object expected, Object actual) {
        assertEquals(expected, actual);
    }

    protected List<?> findAll(final Class<?> entityClass) {
        throw new IllegalStateException("Override findAll method");
    }

    protected Object findById(final Class<?> entityClass, final Object id) {
        throw new IllegalStateException("Override findById method");
    }

    @BeforeEach
    public void before() {
        parameterSnapshot.clear();

        dbSnapshot.clear();
        for (final Class<?> entityType : entityTypes) {
            getDbSnapshot().put(entityType, new HashMap<>());
        }
    }

    @AfterEach
    public void after() {
        final FabutReport report = new FabutReport(() -> "After test assert");

        final FabutReport paremeterReport = report.getSubReport(() -> "Parameter snapshot test report");
        assertParameterSnapshot(paremeterReport);

        final FabutReport snapshotReport = report.getSubReport(() -> "Repository snapshot assert");
        assertDbSnapshot(snapshotReport);

        if (!report.isSuccess()) {
            throw new AssertionFailedError(report.getMessage());
        }
    }

    // COMMANDS
    public void takeSnapshot(final Object... parameters) {
        final FabutReport report = new FabutReport(() -> "Take snapshot");
        takeSnapshott(report, parameters);

        if (!report.isSuccess()) {
            throw new AssertionFailedError(report.getMessage());
        }
    }

    public void assertObject(final String message, final Object object, final IProperty... properties) {

        final FabutReport report = new FabutReport(() -> message + ": " + object);
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
        final FabutReport report = new FabutReport(() -> "Assert with snapshot: " + entity);

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

        final FabutReport report = new FabutReport(() -> "Assert entity as deleted: " + entity);
        assertEntityAsDeleted(report, entity);
        if (!report.isSuccess()) {
            throw new AssertionFailedError(report.getMessage());
        }
    }

    public void ignoreEntity(final Object entity) {
        checkIfEntity(entity);

        final FabutReport report = new FabutReport(() -> "Ignore entity");
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

    // TYPE METHODS
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

    private boolean isIgnoredField(Class<?> clazz, String fieldName) {
        return ignoredFields.getOrDefault(clazz, Collections.emptyList()).contains(fieldName);
    }

    // PROPERTIES
    private List<Method> getGetMethods(final Object object) {

        final List<Method> getMethods = new ArrayList<>();
        final List<Method> getMethodsComplexType = new ArrayList<>();
        final boolean isEntityClass = isEntityType(object.getClass());

        final Collection<Method> allGetMethods = ReflectionUtil.getMethods(object.getClass()).values();

        for (final Method method : allGetMethods) {
            if (!(isEntityClass && isCollectionClass(method.getReturnType()))) {

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
        return value(new PropertyPath<>(propertyPath), field);
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

    ReferenceCheckType checkByReference(final FabutReport report, Object expected, Object actual, final List<ObjectMethod> parents) {

        if (expected == actual) {
            // report.asserted(pair, propertyName);
            return ReferenceCheckType.EQUAL_REFERENCE;
        }

        if (expected == null ^ actual == null) {
            final List<String> propertyNames = parents.stream().map(ObjectMethod::getProperty).collect(Collectors.toList());
            final String propertyName = propertyNames.get(propertyNames.size() - 1);
            report.assertFail(propertyName, expected, actual);
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
    protected Object createCopyObject(final Object object, final NodesList nodes) throws CopyException {

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
            final Collection<Method> allGetMethods = getMethods(classObject).values();
            for (final Method method : allGetMethods) {
                if (method.getName().equals(GET_ID)) {
                    Field field = findField(object.getClass(), "id");
                    if (field == null) {
                        throw new CopyException(object.getClass().getSimpleName());
                    }
                    field.setAccessible(true);
                    field.set(copy, field.get(object));
                }
            }
            for (final Method method : allGetMethods) {
                if (!method.getName().equals(GET_ID)
                        && method.getParameterAnnotations().length == 0
                        && !(isEntityType && isCollectionClass(method.getReturnType()))) {
                    final String declaredField = getFieldName(method);
                    Field field = findField(object, declaredField);
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

    protected Object createEmptyCopyOf(final Object object) {
        try {
            return object.getClass().getConstructor().newInstance();
        } catch (final Exception e) {
            return null;
        }
    }

    protected Object copyProperty(final Object propertyForCopying, final NodesList nodes) throws CopyException {
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
            return copyMap((Map<?, ?>) propertyForCopying, nodes);
        }

        // if its not list or some complex type same object will be added.
        return propertyForCopying;
    }

    private Object copyMap(final Map<?, ?> propertyForCopying, NodesList nodes) throws CopyException {

        final Map<Object, Object> mapCopy = new HashMap<>();
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
    private void assertEntityPair(
            final FabutReport report,
            final List<ObjectMethod> parents,
            Object expected,
            Object actual,
            final List<ISingleProperty> properties,
            final NodesList nodesList) {

        if (!parents.isEmpty()) {
            final List<String> propertyNames = parents.stream().map(ObjectMethod::getProperty).collect(Collectors.toList());
            final String propertyName = propertyNames.get(propertyNames.size() - 1);
            assertEntityById(report, propertyName, expected, actual);
        } else {
            assertSubfields(report, Collections.emptyList(), expected, actual, properties, nodesList);
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
                    assertProperty(report, new ArrayList<>(), fieldName, property, method.invoke(actual), expectedProperties, new NodesList());
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

        assertPair(report, Collections.emptyList(), expected, actual, expectedChangedProperties, new NodesList());

        afterAssertObject(report, actual);
    }

    void assertPair(
            final FabutReport report,
            final List<ObjectMethod> parents,
            Object expected,
            Object actual,
            final List<ISingleProperty> properties,
            final NodesList nodesList) {

        final ReferenceCheckType referenceCheck = checkByReference(report, expected, actual, parents);

        if (referenceCheck == ReferenceCheckType.EQUAL_REFERENCE) {
            return;
        }
        if (referenceCheck == ReferenceCheckType.EXCLUSIVE_NULL) {
            return;
        }

        final String propertyName = getLastPropertyName(parents);
        // check if any of the expected/actual object is recurring in nodes list
        switch (nodesList.nodeCheck(expected, actual)) {
            case SINGLE_NODE:
                report.checkByReference(propertyName, actual);
                return;
            case CONTAINS_PAIR:
                return;
            case NEW_PAIR:
                nodesList.addPair(expected, actual);

                if (isIgnoredType(expected.getClass())) {
                    report.ignoredType(expected.getClass());

                } else if (isComplexType(expected.getClass())) {
                    assertSubfields(report, parents, expected, actual, properties, nodesList);

                } else if (isEntityType(expected.getClass())) {
                    assertEntityPair(report, parents, expected, actual, properties, nodesList);

                } else if (isListType(expected.getClass()) && isListType(actual.getClass())) {
                    assertList(report, parents, (List<?>) expected, (List<?>) actual, properties, nodesList);

                } else if (isMapType(expected.getClass()) && isMapType(actual.getClass())) {
                    assertMap(report, parents, (Map<?, ?>) expected, (Map<?, ?>) actual, properties, nodesList);

                } else if (isOptionalType(expected.getClass()) && isOptionalType(actual.getClass())) {
                    assertOptional(report, parents, (Optional<?>) expected, (Optional<?>) actual, properties, nodesList);

                } else {
                    assertPrimitives(report, parents, expected, actual);
                }

                break;
        }
    }

    private String splitCamelCase(String s, String separator) {
        return s.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[^A-Za-z])", separator);
    }

    private String upperUnderscored(String s) {
        return splitCamelCase(s, "_").toUpperCase();
    }

    private void assertSubfields(
            final FabutReport report,
            final List<ObjectMethod> parents,
            Object expected,
            Object actual,
            final List<ISingleProperty> properties,
            final NodesList nodesList) {

        final ArrayList<ISingleProperty> propertiesCopy = new ArrayList<>(properties);

        final List<Method> getMethods = getGetMethods(expected);

        if (parents.isEmpty()) {
            report.addCode("\nassertObject(object");
        }

        final String className = getRealClass(actual.getClass()).getSimpleName();

        StringBuilder chainPrefix = new StringBuilder();
        StringBuilder chainPostfix = new StringBuilder();

        final List<ObjectMethod> reversedParents = new ArrayList<>(parents);
        Collections.reverse(reversedParents);

        for (ObjectMethod parent : reversedParents) {
            final String parentClass = getRealClass(parent.getParrent().getClass()).getSimpleName();
            chainPrefix.insert(0, parentClass + "." + upperUnderscored(parent.getProperty()) + ".chain(");
            chainPostfix.append(")");
        }

        for (final Method expectedMethod : getMethods) {
            final String fieldName = ReflectionUtil.getFieldName(expectedMethod);
            if (!isIgnoredField(expected.getClass(), fieldName)) {
                try {

                    final Object invoke = expectedMethod.invoke(expected);
                    final String propertyPath = chainPrefix + className + "." + upperUnderscored(fieldName) + chainPostfix;

                    if (invoke == null) {
                        report.addCode(",\nisNull(" + propertyPath + ")");
                    } else if (invoke.getClass().isAssignableFrom(String.class)) {
                        report.addCode(",\nvalue(" + propertyPath + ", " + "\"" + invoke + "\"" + ")");
                    } else if (invoke.getClass().isEnum()) {
                        report.addCode(",\nvalue(" + propertyPath + ", " + invoke.getClass().getSimpleName() + "." + invoke + ")");
                    } else if (invoke.getClass().isAssignableFrom(Optional.class) && !((Optional<?>) invoke).isPresent()) {
                        report.addCode(",\nisEmpty(" + propertyPath + ")");
                    } else {
                        report.addCode(",\nvalue(" + propertyPath + ", " + invoke + ")");
                    }

                    final ISingleProperty property = obtainProperty(invoke, fieldName, properties);
                    final Method actualMethod = findMethod(actual, expectedMethod.getName());
                    assertProperty(report, parents, fieldName, property, actualMethod.invoke(actual), properties, nodesList);

                    if (propertiesCopy.contains(property)) {
                        final FabutReport optimisationReport = new FabutReport();
                        assertProperty(
                                optimisationReport,
                                parents,
                                fieldName,
                                value(new PropertyPath<>(fieldName), invoke),
                                actualMethod.invoke(actual),
                                new ArrayList<>(),
                                new NodesList());

                        if (optimisationReport.isSuccess()) {
                            report.notNecessaryAssert(fieldName, actual);
                        }
                    }

                } catch (final IllegalAccessException | InvocationTargetException e) {
                    report.uncallableMethod(expectedMethod, actual);
                }
            }
        }

        if (parents.isEmpty()) {
            report.addCode(");");
        }
    }

    private void assertPrimitives(final FabutReport report, final List<ObjectMethod> parents, Object expected, Object actual) {
        try {
            customAssertEquals(expected, actual);
        } catch (final AssertionError e) {
            final String propertyName = getLastPropertyName(parents);
            report.assertFail(propertyName, expected, actual);
        }
    }

    private String getLastPropertyName(List<ObjectMethod> parents) {
        final List<String> propertyNames = parents.stream().map(ObjectMethod::getProperty).collect(Collectors.toList());
        final String propertyName;
        if (propertyNames.isEmpty()) {
            propertyName = "";
        } else {
            propertyName = propertyNames.get(propertyNames.size() - 1);
        }
        return propertyName;
    }

    void assertProperty(
            final FabutReport report,
            List<ObjectMethod> parents,
            final String fieldName,
            final ISingleProperty expected,
            final Object actual,
            final List<ISingleProperty> properties,
            final NodesList nodesList) {

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
            if (!(actual instanceof Optional && ((Optional<?>) actual).isPresent())) {
                report.notEmptyProperty(expected.getPath());
            }
        } else if (expected instanceof EmptyProperty) { // expected empty value
            if (!(actual instanceof Optional && !((Optional<?>) actual).isPresent())) {
                report.emptyProperty(expected.getPath());
            }
        } else if (expected instanceof IgnoredProperty) {
            report.reportIgnoreProperty(expected.getPath());

        } else if (expected instanceof Property) {
            final Object expectedValue = ((Property<?>) expected).getValue();
            final ArrayList<ObjectMethod> parentsExtended = new ArrayList<>(parents);
            parentsExtended.add(new ObjectMethod(actual, expected.getPath()));
            assertPair(report, parentsExtended, expectedValue, actual, properties, nodesList);
        } else {
            throw new IllegalStateException();
        }
    }

    void assertList(
            final FabutReport report,
            final List<ObjectMethod> parents,
            final List<?> expected,
            final List<?> actual,
            final List<ISingleProperty> properties,
            final NodesList nodesList) {

        final String propertyName = getLastPropertyName(parents);

        // check sizes
        if (expected.size() != actual.size()) {
            report.listDifferentSizeComment(propertyName, expected.size(), actual.size());
        } else {
            // assert every element by index

            for (int i = 0; i < actual.size(); i++) {
                report.assertingListElement(propertyName, i);
                assertObjects(report, expected.get(i), actual.get(i), properties);
            }
        }
    }

    void assertMap(
            final FabutReport report,
            final List<ObjectMethod> parents,
            final Map<?, ?> expected,
            final Map<?, ?> actual,
            final List<ISingleProperty> properties,
            final NodesList nodesList) {

        final Set<?> expectedKeys = new TreeSet<>(expected.keySet());
        final Set<?> actualKeys = new TreeSet<>(actual.keySet());
        final Set<?> expectedKeysCopy = new TreeSet<>(expectedKeys);

        expectedKeysCopy.retainAll(actualKeys);

        for (final Object key : expectedKeysCopy) {
            report.assertingMapKey(key);
            assertPair(report, parents, expected.get(key), actual.get(key), properties, nodesList);
        }
        assertExcessExpected(parents, report, expected, expectedKeysCopy, actualKeys);
        assertExcessActual(parents, report, actual, expectedKeysCopy, actualKeys);
    }

    private void assertOptional(
            final FabutReport report,
            List<ObjectMethod> parents,
            Optional<?> expected,
            Optional<?> actual,
            final List<ISingleProperty> properties,
            final NodesList nodesList) {

        if (!expected.isPresent() && !actual.isPresent()) {
            return;
        }

        if (expected.isPresent() ^ actual.isPresent()) {

            final List<String> propertyNames = parents.stream().map(ObjectMethod::getProperty).collect(Collectors.toList());
            final String propertyName = propertyNames.get(propertyNames.size() - 1);

            report.assertFail(propertyName, expected, actual);
            return;
        }

        final Object expectedValue = expected.get();
        final Object actualValue = actual.get();

        assertPair(report, parents, expectedValue, actualValue, properties, nodesList);
    }

    void assertExcessExpected(
            final List<ObjectMethod> parents, final FabutReport report, final Map<?, ?> expected, final Set<?> expectedKeys, final Set<?> actualKeys) {

        final Set<?> expectedKeysCopy = new TreeSet<>(expectedKeys);
        expectedKeysCopy.removeAll(actualKeys);
        if (expectedKeysCopy.size() > 0) {
            for (final Object key : expectedKeysCopy) {
                report.excessExpectedMap(key);
            }
        }
    }

    void assertExcessActual(
            final List<ObjectMethod> parents, final FabutReport report, final Map<?, ?> actual, final Set<?> expectedKeys, final Set<?> actualKeys) {

        final Set<?> actualKeysCopy = new TreeSet<>(actualKeys);
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

    private Map<Object, Object> getAfterEntities(final Class<?> clazz) {
        final Map<Object, Object> afterEntities = new TreeMap<>();
        final List<?> entities = findAll(clazz);
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
            assertObjects(report.getSubReport(() -> "Snapshot pair assert"), snapshotPair.getExpected(), snapshotPair.getActual(), new LinkedList<>());
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
            final Set<?> beforeIds = new TreeSet<>(snapshotEntry.getValue().keySet());
            final Set<?> afterIds = new TreeSet<>(afterEntities.keySet());

            checkNotExistingInAfterDbState(beforeIds, afterIds, snapshotEntry.getValue(), report);
            checkNewToAfterDbState(beforeIds, afterIds, afterEntities, report);
            assertDbSnapshotWithAfterState(beforeIds, afterIds, snapshotEntry.getValue(), afterEntities, report);
        }
    }

    void checkNotExistingInAfterDbState(final Set<?> beforeIds, final Set<?> afterIds, final Map<Object, CopyAssert> beforeEntities, final FabutReport report) {

        final Set<?> beforeIdsCopy = new TreeSet<>(beforeIds);

        // does difference between db snapshot and after db state
        beforeIdsCopy.removeAll(afterIds);
        for (final Object id : beforeIdsCopy) {
            final CopyAssert copyAssert = beforeEntities.get(id);
            if (!copyAssert.isAsserted()) {

                report.noEntityInSnapshot(copyAssert.getEntity());
            }
        }
    }

    void checkNewToAfterDbState(final Set<?> beforeIds, final Set<?> afterIds, final Map<Object, Object> afterEntities, final FabutReport report) {

        final Set<?> afterIdsCopy = new TreeSet<>(afterIds);

        // does difference between after db state and db snapshot
        afterIdsCopy.removeAll(beforeIds);
        for (final Object id : afterIdsCopy) {
            final Object entity = afterEntities.get(id);

            report.entityNotAssertedInAfterState(entity);
        }
    }

    void assertDbSnapshotWithAfterState(
            final Set<?> beforeIds,
            final Set<?> afterIds,
            final Map<Object, CopyAssert> beforeEntities,
            final Map<Object, Object> afterEntities,
            final FabutReport report) {

        final Set<?> beforeIdsCopy = new TreeSet<>(beforeIds);
        // does intersection between db snapshot and after db state
        beforeIdsCopy.retainAll(afterIds);

        for (final Object id : beforeIdsCopy) {
            if (!beforeEntities.get(id).isAsserted()) {
                Object beforeEntity = beforeEntities.get(id).getEntity();
                Object afterEntity = afterEntities.get(id);
                assertObjects(report.getSubReport(() -> "Asserting object: " + beforeEntity), beforeEntity, afterEntity, new LinkedList<>());
            }
        }
    }
}

class ObjectMethod {

    private final Object parrent;
    private final String property;

    public ObjectMethod(Object parrent, String property) {
        this.parrent = parrent;
        this.property = property;
    }

    public Object getParrent() {
        return parrent;
    }

    public String getProperty() {
        return property;
    }
}
