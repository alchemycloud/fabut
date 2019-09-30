package cloud.alchemy.fabut;

import cloud.alchemy.fabut.property.*;
import cloud.alchemy.fabut2.property.*;
import junit.framework.AssertionFailedError;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;


import static cloud.alchemy.fabut.ReflectionUtil.*;

public abstract class FabutTests {

    private static final String EMPTY_STRING = "";
    private static final String DOT = ".";

    private List<Class> entityTypes;
    private List<Class> complexTypes;
    private List<Class> ignoredTypes;
    private Map<Class, List<String>> ignoredFields;

    private Map<Class<?>, Map<Object, CopyAssert>> dbSnapshot = new HashMap<>();
    private List<SnapshotPair> parameterSnapshot = new ArrayList<>();

    protected void customAssertEquals(Object expected, Object actual) {
        Assert.assertEquals(expected, actual);
    }

    @Before
    public void fabutBeforeTest() {
        parameterSnapshot.clear();
        ;

        dbSnapshot.clear();
        for (final Class<?> entityType : entityTypes) {
            getDbSnapshot().put(entityType, new HashMap<>());
        }
    }

    @After
    public void fabutAfterTest() {
        final FabutReport report = new FabutReport("After test assert");

        final FabutReport paremeterReport = report.getSubReport("Parameter snapshot test report");
        assertParameterSnapshot(paremeterReport);

        final FabutReport snapshotReport = report.getSubReport("Repository snapshot assert");
        assertDbSnapshot(snapshotReport);

        if (!report.isSuccess()) {
            throw new AssertionFailedError(report.getMessage());
        }
    }

    public void takeSnapshot(final Object... parameters) {
        final FabutReport report = new FabutReport("Take snapshot");
        takeSnapshot(report, parameters);

        if (!report.isSuccess()) {
            throw new AssertionFailedError(report.getMessage());
        }
    }

    public void assertObject(final String message, final Object object, final IProperty... properties) {

        final FabutReport report = new FabutReport(message);
        if (isRepositoryAssert() && doesExistInSnapshot(object)) {
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


    //PRIVATE METHODS
    private void checkIfEntity(final Object entity) {
        if (entity == null) {
            throw new IllegalStateException("assertEntityWithSnapshot cannot take null entity!");
        }

        if (!isEntityType(entity.getClass())) {
            throw new IllegalStateException(entity.getClass() + " is not registered as entity type");
        }
    }

    private boolean isRepositoryAssert() {
        return !entityTypes.isEmpty();
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

    public boolean isIgnoredField(Class clazz, String fieldName) {
        return ignoredFields.getOrDefault(clazz, Collections.emptyList()).contains(fieldName);
    }

    public boolean hasIdMethod(final Object entity) {
        try {
            entity.getClass().getMethod(GET_ID);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    public Object getIdValue(final Object entity) {
        try {
            final Method method = entity.getClass().getMethod(GET_ID);
            return method.invoke(entity);
        } catch (final Exception e) {
            return null;
        }
    }

    public List<Method> getGetMethods(final Object object) {

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

    public Method getGetMethod(final String methodName, final Object object) throws Exception {
        return object.getClass().getMethod(methodName);
    }

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

    private Field getDeclaredFieldFromClassOrSupperClass(Class<?> clazz, String declaredField) {
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
            return createCopyObject(propertyForCopying.getClass(), nodes);
        }

        if (isEntityType(propertyForCopying.getClass())) {
            // its complex object, we need its copy
            return createCopyObject(propertyForCopying.getClass(), nodes);
        }

        if (isListType(propertyForCopying)) {
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
            return Optional.of((T) copyProperty(optional.get(), nodes));
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


    protected boolean assertEntityPair(final FabutReport report, final String propertyName, final AssertPair pair, final List<ISingleProperty> properties, final NodesList nodesList) {
        if (assertType == AssertType.OBJECT_ASSERT) {
            return super.assertEntityPair(report, propertyName, pair, properties, nodesList);
        }

        if (pair.isProperty()) {
            return assertEntityById(report, propertyName, pair);
        } else {
            return assertSubfields(report, pair, properties, nodesList, propertyName);
        }
    }

    private boolean assertEntityAsDeleted(final FabutReport report, final Object entity) {

        final boolean ignoreEntity = ignoreEntity(report, entity);

        final Object findById = findById(entity.getClass(), getIdValue(entity));
        final boolean isDeletedInRepository = findById == null;

        if (!isDeletedInRepository) {
            report.notDeletedInRepository(entity);
        }
        return ignoreEntity && isDeletedInRepository;
    }

    private boolean ignoreEntity(final FabutReport report, final Object entity) {
        return markAsAsserted(report, entity);
    }

    /**
     * Asserts specified entity with entity with of same class with same id in db snapshot.
     *
     * @param report
     * @param entity
     * @param properties
     * @return <code>true</code> if entity can be asserted with one in the snapshot, <code>false</code> otherwise.
     */
    private Object assertEntityWithSnapshot(final FabutReport report, final Object entity,
                                            final List<ISingleProperty> properties) {

        final Object id = getIdValue(entity);
        final Class<?> entityClass = entity.getClass();

        final Map<Object, CopyAssert> map = dbSnapshot.get(entityClass);
        final CopyAssert copyAssert = map.get(id);
        if (copyAssert != null) {
            final Object expected = copyAssert.getEntity();
            final Object freshEntity = findById(entityClass, id);
            assertObjects(report, expected, freshEntity, properties);
            return freshEntity;
        } else {
            return null;
        }

    }

    private boolean doesExistInSnapshot(final Object entity) {
        final Object id = getIdValue(entity);
        final Class<?> entityClass = entity.getClass();

        final Map<Object, CopyAssert> map = dbSnapshot.get(entityClass);

        return map != null && map.get(id) != null;
    }

    @Override
    boolean afterAssertObject(final Object object) {
        return afterAssertEntity(new FabutReport(), object, false);
    }

    final boolean afterAssertEntity(final FabutReport report, final Object entity, final boolean isProperty) {
        if (!isProperty) {
            return markAsAsserted(report, entity);
        } else {
            return ASSERTED;
        }
    }


    private List<?> findAll(final Class<?> entityClass) {
        return findAll(entityClass);
    }

    private Object findById(final Class<?> entityClass, final Object id) {
        return findById(entityClass, id);
    }

    private boolean markAsAsserted(final FabutReport report, final Object entity) {

        final Class<?> actualType = entity.getClass();

        final Object id = getIdValue(entity);
        if (id == null) {
            report.idNull(actualType);
            return ASSERT_FAIL;
        }

        return markAsserted(id, entity, actualType);
    }

    protected boolean markAsserted(final Object id, final Object copy, final Class<?> actualType) {
        final Map<Object, CopyAssert> map = dbSnapshot.get(actualType);
        final boolean isTypeSupported = map != null;
        if (isTypeSupported) {
            CopyAssert copyAssert = map.get(id);
            if (copyAssert == null) {
                copyAssert = new CopyAssert(copy);
                map.put(getIdValue(copy), copyAssert);
            }
            copyAssert.setAsserted(true);
        }

        final Class<?> superClassType = actualType.getSuperclass();
        final boolean isSuperSuperTypeSupported = (superClassType != null)
                && markAsserted(id, copy, superClassType);

        return isTypeSupported || isSuperSuperTypeSupported;
    }

    @Override
    public boolean takeSnapshot(final FabutReport report, final Object... parameters) {
        initDbSnapshot();
        isRepositoryValid = true;
        final boolean isParameterSnapshotOk = super.takeSnapshot(report, parameters);

        boolean ok = ASSERTED;
        for (final Entry<Class<?>, Map<Object, CopyAssert>> entry : dbSnapshot.entrySet()) {
            final List<?> findAll = findAll(entry.getKey());

            for (final Object entity : findAll) {
                try {
                    final Object copy = ReflectionUtil.createCopy(entity, getTypes(), getIgnoredFields());
                    entry.getValue().put(ReflectionUtil.getIdValue(entity), new CopyAssert(copy));
                } catch (final CopyException e) {
                    report.noCopy(entity);
                    ok = ASSERT_FAIL;
                }
            }
        }
        return ok && isParameterSnapshotOk;
    }

    /**
     * Asserts db snapshot with after db state.
     *
     * @param report the report
     * @return true, if successful
     */
    public boolean assertDbSnapshot(final FabutReport report) {
        boolean ok = true;
        // assert entities by classes
        for (final Map.Entry<Class<?>, Map<Object, CopyAssert>> snapshotEntry : dbSnapshot.entrySet()) {

            final Map<Object, Object> afterEntities = getAfterEntities(snapshotEntry.getKey());
            final TreeSet beforeIds = new TreeSet(snapshotEntry.getValue().keySet());
            final TreeSet afterIds = new TreeSet(afterEntities.keySet());

            ok &= checkNotExistingInAfterDbState(beforeIds, afterIds, snapshotEntry.getValue(), report);
            ok &= checkNewToAfterDbState(beforeIds, afterIds, afterEntities, report);
            ok &= assertDbSnapshotWithAfterState(beforeIds, afterIds, snapshotEntry.getValue(), afterEntities, report);

        }
        return ok;

    }

    boolean checkNotExistingInAfterDbState(final TreeSet beforeIds, final TreeSet afterIds,
                                           final Map<Object, CopyAssert> beforeEntities, final FabutReport report) {

        final TreeSet beforeIdsCopy = new TreeSet(beforeIds);
        boolean ok = ASSERTED;
        // does difference between db snapshot and after db state
        beforeIdsCopy.removeAll(afterIds);
        for (final Object id : beforeIdsCopy) {
            final CopyAssert copyAssert = beforeEntities.get(id);
            if (!copyAssert.isAsserted()) {
                ok = ASSERT_FAIL;
                report.noEntityInSnapshot(copyAssert.getEntity());
            }
        }

        return ok;
    }

    boolean checkNewToAfterDbState(final TreeSet beforeIds, final TreeSet afterIds,
                                   final Map<Object, Object> afterEntities, final FabutReport report) {

        final TreeSet afterIdsCopy = new TreeSet(afterIds);
        boolean ok = ASSERTED;
        // does difference between after db state and db snapshot
        afterIdsCopy.removeAll(beforeIds);
        for (final Object id : afterIdsCopy) {
            final Object entity = afterEntities.get(id);
            ok = ASSERT_FAIL;
            report.entityNotAssertedInAfterState(entity);
        }
        return ok;
    }

    boolean assertDbSnapshotWithAfterState(final TreeSet beforeIds, final TreeSet afterIds,
                                           final Map<Object, CopyAssert> beforeEntities, final Map<Object, Object> afterEntities,
                                           final FabutReport report) {

        final TreeSet beforeIdsCopy = new TreeSet(beforeIds);
        // does intersection between db snapshot and after db state
        beforeIdsCopy.retainAll(afterIds);
        boolean ok = ASSERTED;
        for (final Object id : beforeIdsCopy) {
            if (!beforeEntities.get(id).isAsserted()) {
                ok &= assertObjects(report, beforeEntities.get(id).getEntity(), afterEntities.get(id),
                        new LinkedList<>());
            }
        }
        return ok;
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


    /**
     * Asserts two entities by their id.
     *
     * @param report       assert report builder
     * @param propertyName name of current entity
     * @return - <code>true</code> if and only if ids of two specified objects are equal, <code>false</code> otherwise
     */
    boolean assertEntityById(final FabutReport report, final String propertyName, final AssertPair pair) {

        final Object expectedId = ReflectionUtil.getIdValue(pair.getExpected());
        final Object actualId = ReflectionUtil.getIdValue(pair.getActual());
        try {
            assertEquals(expectedId, actualId);
            //report.asserted(pair, propertyName);
            return ASSERTED;
        } catch (final AssertionError e) {
            report.assertFail(pair, propertyName);
            return ASSERT_FAIL;
        }
    }

    /**
     * Asserts object with with expected properties, every field of object must have property for it or assert will
     * fail.
     *
     * @param report             the report
     * @param actual             the actual
     * @param expectedProperties the properties
     * @return <code>true</code> if object can be asserted with list of properties, <code>false</code> otherwise.
     */
    public boolean assertObjectWithProperties(final FabutReport report, final Object actual,
                                              final List<ISingleProperty> expectedProperties) {

        if (actual == null) {
            report.nullReference();
            return ASSERT_FAIL;
        }

        final List<Method> methods = ReflectionUtil.getGetMethods(actual, types);
        boolean result = ASSERTED;
        for (final Method method : methods) {

            final String fieldName = ReflectionUtil.getFieldName(method);
            final boolean ignoredField = ReflectionUtil.isIgnoredField(ignoredFields, getRealClass(actual), fieldName);

            final ISingleProperty property = getPropertyFromList(fieldName, expectedProperties);
            try {
                if (property != null) {
                    result &= assertProperty(fieldName, report, property, method.invoke(actual), EMPTY_STRING,
                            expectedProperties, new NodesList(), true);
                } else if (!ignoredField && hasInnerProperties(fieldName, expectedProperties)) {
                    result &= assertInnerProperty(report, method.invoke(actual), expectedProperties, fieldName);
                } else if (!ignoredField) {
                    // there is no matching property for field
                    report.noPropertyForField(fieldName, method.invoke(actual));
                    result = ASSERT_FAIL;
                }
            } catch (final Exception e) {
                report.uncallableMethod(method, actual);
                result = ASSERT_FAIL;
            }


        }
        if (!expectedProperties.isEmpty()) {
            for (ISingleProperty singleProperty : expectedProperties) {
                report.excessExpectedProperty(singleProperty.getPath());
            }
            result = ASSERT_FAIL;
        }
        if (result) {
            afterAssertObject(actual);
        }

        return result;
    }

    public boolean assertInnerProperty(final FabutReport report, final Object actual,
                                       final List<ISingleProperty> properties, final String parent) {
        final List<ISingleProperty> extracts = extractPropertiesWithMatchingParent(parent, properties);
        removeParentQualification(parent, extracts);
        report.increaseDepth(parent);
        final boolean t = assertObjectWithProperties(report, actual, extracts);
        report.decreaseDepth();
        return t;
    }

    public boolean assertInnerObject(final FabutReport report, final Object expected, final Object actual,
                                     final List<ISingleProperty> properties, final String parent) {
        final List<ISingleProperty> extracts = extractPropertiesWithMatchingParent(parent, properties);
        removeParentQualification(parent, extracts);
        report.increaseDepth(parent);
        final boolean t = assertObjects(report, expected, actual, extracts);
        report.decreaseDepth();
        return t;
    }

    /**
     * Asserts two objects, if objects are primitives it will rely on custom user assert for primitives, if objects are
     * complex it will assert them by values of their fields.
     *
     * @param report                    the report
     * @param expected                  the expected
     * @param actual                    the actual
     * @param expectedChangedProperties use of this list is to remove the need for every field of actual object to match fields of expected
     *                                  object, properties in this list take priority over fields in expected object
     * @return <code>true</code> can be asserted, <code>false</code> otherwise
     */
    public boolean assertObjects(final FabutReport report, final Object expected, final Object actual,
                                 final List<ISingleProperty> expectedChangedProperties) {

        final AssertPair assertPair = ConversionUtil.createAssertPair(expected, actual, types);
        final boolean assertResult = assertPair(EMPTY_STRING, report, assertPair, expectedChangedProperties,
                new NodesList());
        if (assertResult) {
            afterAssertObject(actual);
        }
        return assertResult;
    }

    /**
     * Makes snapshot of specified parameters.
     *
     * @param parameters array of parameters
     */
    public boolean takeSnapshot(final FabutReport report, final Object... parameters) {
        initParametersSnapshot();
        boolean ok = ASSERTED;
        for (final Object object : parameters) {
            try {
                final SnapshotPair snapshotPair = new SnapshotPair(object, ReflectionUtil.createCopy(object, types, ignoredFields));
                parameterSnapshot.add(snapshotPair);
            } catch (final CopyException e) {
                report.noCopy(object);
                ok = false;
            }
        }
        return ok;
    }

    /**
     * Asserts object pair trough three phases:
     * <ul type="circle">
     * <li>Reference check, assert will only continue trough this phase if both object aren't null and aren't same
     * instance</li>
     * <li>Node check, assert will pass continue trough if object pair is new to nodes list
     * <li>Asserting by type with each type having particular method of asserting</li>
     * </ul>
     *
     * @param propertyName name of current property
     * @param report       assert report builder
     * @param pair         object pair for asserting
     * @param properties   list of expected changed properties
     * @param nodesList    list of object that had been asserted
     * @return <code>true</code> if objects can be asserted, <code>false</code> otherwise.
     */
    public boolean assertPair(final String propertyName, final FabutReport report, final AssertPair pair,
                              final List<ISingleProperty> properties, final NodesList nodesList) {

        final ReferenceCheckType referenceCheck = checkByReference(report, pair, propertyName);

        if (referenceCheck == ReferenceCheckType.EQUAL_REFERENCE) {
            return referenceCheck.isAssertResult();
        }
        if (referenceCheck == ReferenceCheckType.EXCLUSIVE_NULL) {
            return referenceCheck.isAssertResult();
        }

        // check if any of the expected/actual object is recurring in nodes list
        final NodeCheckType nodeCheckType = nodesList.nodeCheck(pair);
        if (nodeCheckType != NodeCheckType.NEW_PAIR) {
            if (nodeCheckType.getAssertValue()) {
                report.checkByReference(propertyName, pair.getActual());
            }
            return nodeCheckType.getAssertValue();
        }
        nodesList.addPair(pair);
        switch (pair.getObjectType()) {
            case IGNORED_TYPE:
                report.ignoredType(getRealClass(pair.getExpected()));
                return ASSERTED;
            case COMPLEX_TYPE:
                return assertSubfields(report, pair, properties, nodesList, propertyName);
            case ENTITY_TYPE:
                return assertEntityPair(report, propertyName, pair, properties, nodesList);
            case PRIMITIVE_TYPE:
                return assertPrimitives(report, propertyName, pair);
            case LIST_TYPE:
                return assertList(propertyName, report, (List) pair.getExpected(), (List) pair.getActual(), properties,
                        nodesList, true);
            case MAP_TYPE:
                return assertMap(propertyName, report, (Map) pair.getExpected(), (Map) pair.getActual(), properties,
                        nodesList, true);
            case OPTIONAL_TYPE:
                return assertOptional(report, pair, properties, propertyName, nodesList);
            default:
                throw new IllegalStateException("Unknown assert type: " + pair.getObjectType());
        }
    }

    /**
     * Asserts two entities.
     *
     * @param report       the report
     * @param propertyName the property name
     * @param pair         the pair
     * @param properties   the properties
     * @param nodesList    the nodes list
     * @return <code>true</code> if objects can be asserted, <code>false</code> otherwise.
     */
    protected boolean assertEntityPair(final FabutReport report, final String propertyName,
                                       final AssertPair pair, final List<ISingleProperty> properties, final NodesList nodesList) {
        throw new IllegalStateException("Entities are not supported!");
    }

    /**
     * Assert subfields of an actual object with ones from expected object, it gets the fields by invoking get methods
     * of actual/expected objects via reflection, properties passed have priority over expected object fields.
     *
     * @param report     the report
     * @param pair       the pair
     * @param properties the properties
     * @param nodesList  the nodes list
     * @return <code>true</code> if objects can be asserted, <code>false</code> otherwise.
     */
    public boolean assertSubfields(final FabutReport report, final AssertPair pair,
                                   final List<ISingleProperty> properties, final NodesList nodesList, final String propertyName) {

        report.increaseDepth(propertyName);

        boolean t = ASSERTED;
        final List<Method> getMethods = ReflectionUtil.getGetMethods(pair.getExpected(), types);

        for (final Method expectedMethod : getMethods) {
            final String fieldName = ReflectionUtil.getFieldName(expectedMethod);
            if (!ReflectionUtil.isIgnoredField(ignoredFields, getRealClass(pair.getExpected()), fieldName)) {
                try {
                    final ISingleProperty property = obtainProperty(expectedMethod.invoke(pair.getExpected()), fieldName,
                            properties);

                    final Method actualMethod = ReflectionUtil.getGetMethod(expectedMethod.getName(), pair.getActual());

                    t &= assertProperty(fieldName, report, property, actualMethod.invoke(pair.getActual()), fieldName,
                            properties, nodesList, true);

                } catch (final Exception e) {
                    report.uncallableMethod(expectedMethod, pair.getActual());
                    t = ASSERT_FAIL;
                }
            }
        }

        report.decreaseDepth();
        return t;
    }

    /**
     * Asserts two primitives using abstract method assertEqualsObjects, reports result and returns it. Primitives are
     * any class not marked as complex type, entity type or ignored type.
     *
     * @param report       assert report builder
     * @param propertyName name of the current property
     * @param pair         expected object and actual object
     * @return - <code>true</code> if and only if objects are asserted, <code>false</code> if method customAssertEquals
     * throws {@link AssertionError}.
     */
    boolean assertPrimitives(final FabutReport report, final String propertyName, final AssertPair pair) {
        try {
            fabutTest.customAssertEquals(pair.getExpected(), pair.getActual());
            //report.asserted(pair, propertyName);
            return ASSERTED;
        } catch (final AssertionError e) {
            report.assertFail(pair, propertyName);
            return ASSERT_FAIL;
        }
    }

    /**
     * Handles asserting actual object by the specified expected property. Logs the result in the report and returns it.
     *
     * @param propertyName name of the current property
     * @param report       assert report builder
     * @param expected     property containing expected information
     * @param actual       actual object
     * @param fieldName    name of the field in parent actual object
     * @param properties   list of properties that exclude fields from expected object
     * @param nodesList    list of object that had been asserted
     * @param isProperty   is actual property, important for entities
     * @return - <code>true</code> if object is asserted with expected property, <code>false</code> otherwise.
     */
    public boolean assertProperty(final String propertyName, final FabutReport report, final ISingleProperty expected,
                                  final Object actual, final String fieldName, final List<ISingleProperty> properties,
                                  final NodesList nodesList, final boolean isProperty) {

        removeParentQualification(fieldName, properties);

        // expected any not null value
        if (expected instanceof NotNullProperty) {
            final boolean ok = actual != null ? ASSERTED : ASSERT_FAIL;
            if (!ok) {
                report.notNullProperty(propertyName);
            }
            return ok;
        }

        // expected null value
        if (expected instanceof NullProperty) {
            final boolean ok = actual == null ? ASSERTED : ASSERT_FAIL;
            if (!ok) {
                report.nullProperty(propertyName);
            }
            return ok;
        }

        // expected any not empty value
        if (expected instanceof NotEmptyProperty) {
            final boolean ok = actual instanceof Optional && ((Optional) actual).isPresent() ? ASSERTED : ASSERT_FAIL;
            if (!ok) {
                report.notEmptyProperty(propertyName);
            }
            return ok;
        }

        // expected empty value
        if (expected instanceof EmptyProperty) {
            final boolean ok = actual instanceof Optional && !((Optional) actual).isPresent() ? ASSERTED : ASSERT_FAIL;
            if (!ok) {
                report.emptyProperty(propertyName);
            }
            return ok;
        }

        // any value
        if (expected instanceof IgnoredProperty) {
            report.reportIgnoreProperty(propertyName);
            return ASSERTED;
        }

        // assert by type
        if (expected instanceof Property) {
            final Object expectedValue = ((Property) expected).getValue();
            final AssertPair assertPair = createAssertPair(expectedValue, actual, types, isProperty);
            return assertPair(propertyName, report, assertPair, properties, nodesList);
        }

        throw new IllegalStateException();
    }

    private boolean assertList(final String propertyName, final FabutReport report, final List expected,
                              final List actual, final List<ISingleProperty> properties, final NodesList nodesList,
                              final boolean isProperty) {

        // check sizes
        if (expected.size() != actual.size()) {
            report.listDifferentSizeComment(propertyName, expected.size(), actual.size());
            return ASSERT_FAIL;
        }

        // assert every element by index
        boolean assertResult = ASSERTED;
        for (int i = 0; i < actual.size(); i++) {
            report.assertingListElement(propertyName, i);
            assertResult &= assertObjects(report, expected.get(i), actual.get(i), properties);
        }

        return assertResult;
    }


    private boolean assertMap(final String propertyName, final FabutReport report, final Map expected, final Map actual,
                              final List<ISingleProperty> properties, final NodesList nodesList, final boolean isProperty) {
        // TODO add better reporting when asserting map objects, similar to list
        final TreeSet expectedKeys = new TreeSet(expected.keySet());
        final TreeSet actualKeys = new TreeSet(actual.keySet());
        final TreeSet expectedKeysCopy = new TreeSet(expectedKeys);

        expectedKeysCopy.retainAll(actualKeys);
        boolean ok = true;
        for (final Object key : expectedKeysCopy) {
            final AssertPair assertPair = createAssertPair(expected.get(key), actual.get(key));
            report.assertingMapKey(key);
            ok &= assertPair(EMPTY_STRING, report, assertPair, properties, nodesList);
        }
        ok &= assertExcessExpected(propertyName, report, expected, expectedKeysCopy, actualKeys);
        ok &= assertExcessActual(propertyName, report, actual, expectedKeysCopy, actualKeys);

        return ok;
    }

    private boolean assertOptional(final FabutReport report, final AssertPair pair, final List<ISingleProperty> properties, String propertyName, final NodesList nodesList) {

        if (!((Optional) pair.getExpected()).isPresent() && !((Optional) pair.getActual()).isPresent()) {
            return ASSERTED;
        }
        if (((Optional) pair.getExpected()).isPresent() ^ ((Optional) pair.getActual()).isPresent()) {
            report.assertFail(propertyName, pair.getExpected(), pair.getActual());
            return ASSERT_FAIL;
        }

        final Object expectedValue = ((Optional) pair.getExpected()).get();
        final Object actualValue = ((Optional) pair.getActual()).get();
        final AssertPair assertPair = createAssertPair(expectedValue, actualValue, true);
        return assertPair(propertyName, report, assertPair, properties, nodesList);
    }

    private boolean assertExcessExpected(final String propertyName, final FabutReport report, final Map expected,
                                         final TreeSet expectedKeys, final TreeSet actualKeys) {
        final TreeSet expectedKeysCopy = new TreeSet(expectedKeys);
        expectedKeysCopy.removeAll(actualKeys);
        if (expectedKeysCopy.size() > 0) {
            for (final Object key : expectedKeysCopy) {
                report.excessExpectedMap(key);
            }
            return false;
        }
        return true;
    }

    private boolean assertExcessActual(final String propertyName, final FabutReport report, final Map actual,
                                       final TreeSet expectedKeys, final TreeSet actualKeys) {
        final TreeSet actualKeysCopy = new TreeSet(actualKeys);
        actualKeysCopy.removeAll(expectedKeys);
        if (actualKeysCopy.size() > 0) {
            for (final Object key : actualKeysCopy) {
                report.excessActualMap(key);
            }
            return false;
        }
        return true;
    }

    private List<ISingleProperty> removeParentQualification(final String parentPropertyName, final List<ISingleProperty> properties) {

        final String parentPrefix = parentPropertyName + DOT;
        for (final ISingleProperty property : properties) {
            final String path = StringUtils.removeStart(property.getPath(), parentPrefix);
            property.setPath(path);
        }
        return properties;
    }

    private ISingleProperty obtainProperty(final Object field, final String propertyPath, final List<ISingleProperty> properties) {

        final ISingleProperty property = getPropertyFromList(propertyPath, properties);
        if (property != null) {
            return property;
        }
        return value(new PropertyPath(propertyPath), field);
    }

    private ISingleProperty getPropertyFromList(final String propertyPath, final List<ISingleProperty> properties) {

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

    private ReferenceCheckType checkByReference(final FabutReport report, final AssertPair pair, final String propertyName) {

        if (pair.getExpected() == pair.getActual()) {
            //report.asserted(pair, propertyName);
            return ReferenceCheckType.EQUAL_REFERENCE;
        }

        if (pair.getExpected() == null ^ pair.getActual() == null) {
            report.assertFail(propertyName, pair.getExpected(), pair.getActual());
            return ReferenceCheckType.EXCLUSIVE_NULL;
        }
        return ReferenceCheckType.NOT_NULL_PAIR;
    }

    private List<ISingleProperty> extractProperties(final IProperty... properties) {
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

    private void assertParameterSnapshot(final FabutReport report) {

        for (final SnapshotPair snapshotPair : parameterSnapshot) {
            assertObjects(report.getSubReport("Snapshot pair assert"), snapshotPair.getExpected(), snapshotPair.getActual(),
                    new LinkedList<>());
        }

    }

    private List<ISingleProperty> extractPropertiesWithMatchingParent(final String parent, final List<ISingleProperty> properties) {

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

    private boolean hasInnerProperties(final String parent, final List<ISingleProperty> properties) {

        for (final ISingleProperty property : properties) {
            if (property.getPath().startsWith(parent + DOT)) {
                return true;
            }
        }
        return false;
    }

    private AssertPair createAssertPair(final Object expected, final Object actual) {

        final AssertableType objectType = ReflectionUtil.getObjectType(expected, actual);
        return new AssertPair(expected, actual, objectType);
    }

    private AssertPair createAssertPair(final Object expected, final Object actual, final boolean isProperty) {

        final AssertPair assertPair = createAssertPair(expected, actual);
        assertPair.setProperty(isProperty);
        return assertPair;
    }

}
