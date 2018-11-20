package cloud.alchemy.fabut;

import cloud.alchemy.fabut.enums.AssertableType;
import cloud.alchemy.fabut.enums.NodeCheckType;
import cloud.alchemy.fabut.enums.ReferenceCheckType;
import cloud.alchemy.fabut.exception.CopyException;
import cloud.alchemy.fabut.graph.NodesList;
import cloud.alchemy.fabut.pair.AssertPair;
import cloud.alchemy.fabut.pair.SnapshotPair;
import cloud.alchemy.fabut.property.*;
import cloud.alchemy.fabut.report.FabutReportBuilder;
import cloud.alchemy.fabut.util.ConversionUtil;
import cloud.alchemy.fabut.util.ReflectionUtil;
import org.junit.Assert;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.Optional.empty;

/**
 * Tool for smart asserting two objecting, or asserting object with list of custom properties. Object asserting is done
 * by asserting all the fields inside the given object, if the field is primitive the tool will do user specified assert
 * for primitives, if not, tool will perform smart assert on that field.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @author Andrej Miletic
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class FabutObjectAssert extends Assert {

    private static final String EMPTY_STRING = "";
    private static final String DOT = ".";
    protected static final boolean ASSERTED = true;
    protected static final boolean ASSERT_FAIL = false;

    /**
     * Types supported by Fabut.
     */
    private Map<AssertableType, List<Class<?>>> types;

    /**
     *  Ignored properties
     */
    private Map<Class<?>, List<String>> ignoredFields;

    /**
     * The parameter snapshot.
     */
    private final List<SnapshotPair> parameterSnapshot;

    /**
     * Instance of the JUnit test that is currently running.
     */
    private Object testInstance;

    private final IFabutTest fabutTest;

    /**
     * Instantiates a new fabut object assert.
     */
    public FabutObjectAssert(final IFabutTest fabutTest) {
        super();
        this.fabutTest = fabutTest;
        ignoredFields = fabutTest.getIgnoredFields();
        types = new EnumMap<>(AssertableType.class);
        parameterSnapshot = new ArrayList<>();
        types.put(AssertableType.COMPLEX_TYPE, fabutTest.getComplexTypes());
        types.put(AssertableType.IGNORED_TYPE, fabutTest.getIgnoredTypes());
        types.put(AssertableType.ENTITY_TYPE, new LinkedList<>());
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
    protected boolean assertObjectWithProperties(final FabutReportBuilder report, final Object actual,
                                                 final List<ISingleProperty> expectedProperties) {

        if (actual == null) {
            report.nullReference();
            return ASSERT_FAIL;
        }

        final List<Method> methods = ReflectionUtil.getGetMethods(actual, types, fabutTest.getIgnoredFields());
        boolean result = ASSERTED;
        for (final Method method : methods) {

            final String fieldName = ReflectionUtil.getFieldName(method);

            final ISingleProperty property = getPropertyFromList(fieldName, expectedProperties);
            try {
                if (property != null) {
                    result &= assertProperty(fieldName, report, property, method.invoke(actual), EMPTY_STRING,
                            expectedProperties, new NodesList(), true);
                } else if (hasInnerProperties(fieldName, expectedProperties)) {
                    result &= assertInnerProperty(report, method.invoke(actual), expectedProperties, fieldName);
                } else {
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
            afterAssertObject(actual, false);
        }

        return result;
    }

    boolean assertInnerProperty(final FabutReportBuilder report, final Object actual,
                                final List<ISingleProperty> properties, final String parent) {
        final List<ISingleProperty> extracts = extractPropertiesWithMatchingParent(parent, properties);
        removeParentQualification(parent, extracts);
        report.increaseDepth(parent);
        final boolean t = assertObjectWithProperties(report, actual, extracts);
        report.decreaseDepth();
        return t;
    }

    boolean assertInnerObject(final FabutReportBuilder report, final Object expected, final Object actual,
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
    protected boolean assertObjects(final FabutReportBuilder report, final Object expected, final Object actual,
                                    final List<ISingleProperty> expectedChangedProperties) {

        final AssertPair assertPair = ConversionUtil.createAssertPair(expected, actual, types);
        final boolean assertResult = assertPair(EMPTY_STRING, report, assertPair, expectedChangedProperties,
                new NodesList());
        if (assertResult) {
            afterAssertObject(actual, false);
        }
        return assertResult;
    }

    /**
     * Makes snapshot of specified parameters.
     *
     * @param parameters array of parameters
     */
    protected boolean takeSnapshot(final FabutReportBuilder report, final Object... parameters) {
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
    boolean assertPair(final String propertyName, final FabutReportBuilder report, final AssertPair pair,
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
                report.ignoredType(pair.getExpected().getClass());
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
                return assertOptional(report, (Optional) pair.getExpected(), (Optional) pair.getActual(), properties);
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
    protected boolean assertEntityPair(final FabutReportBuilder report, final String propertyName,
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
    boolean assertSubfields(final FabutReportBuilder report, final AssertPair pair,
                            final List<ISingleProperty> properties, final NodesList nodesList, final String propertyName) {

        report.increaseDepth(propertyName);

        boolean t = ASSERTED;
        final List<Method> getMethods = ReflectionUtil.getGetMethods(pair.getExpected(), types, ignoredFields);

        for (final Method expectedMethod : getMethods) {
            try {
                final String fieldName = ReflectionUtil.getFieldName(expectedMethod);

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
    boolean assertPrimitives(final FabutReportBuilder report, final String propertyName, final AssertPair pair) {
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
     * Asserts two primitive types, if assert fails method must throw {@link AssertionError}.
     *
     * @param expected expected object
     * @param actual   actual object
     */
    protected void customAssertEquals(final Object expected, final Object actual) {
        fabutTest.customAssertEquals(expected, actual);
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
    boolean assertProperty(final String propertyName, final FabutReportBuilder report, final ISingleProperty expected,
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
            if (!ok){
                report.nullProperty(propertyName);
            }
            return ok;
        }

        // expected any not empty value
        if (expected instanceof NotEmptyProperty) {
            final boolean ok = actual instanceof Optional && ((Optional)actual).isPresent() ? ASSERTED : ASSERT_FAIL;
            if (!ok) {
                report.notEmptyProperty(propertyName);
            }
            return ok;
        }

        // expected empty value
        if (expected instanceof EmptyProperty) {
            final boolean ok = actual instanceof Optional && !((Optional)actual).isPresent() ? ASSERTED : ASSERT_FAIL;
            if (!ok){
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
            final AssertPair assertPair = ConversionUtil.createAssertPair(expectedValue, actual, types, isProperty);
            return assertPair(propertyName, report, assertPair, properties, nodesList);
        }

        throw new IllegalStateException();
    }

    /**
     * Handles list asserting. It traverses trough the list by list index start from 0 and going up to list size and
     * asserts every two elements on matching index. Lists cannot be asserted if their sizes are different.
     *
     * @param propertyName name of current property
     * @param report       assert report builder
     * @param expected     expected list
     * @param actual       actual list
     * @param properties   list of excluded properties
     * @param nodesList    list of object that had been asserted
     * @param isProperty   is it parent object or its member
     * @return - <code>true</code> if every element from expected list with index <em>i</em> is asserted with element
     * from actual list with index <em>i</em>, <code>false</code> otherwise.
     */
    boolean assertList(final String propertyName, final FabutReportBuilder report, final List expected,
                       final List actual, final List<ISingleProperty> properties, final NodesList nodesList,
                       final boolean isProperty) {

        // check sizes
        if (expected.size() != actual.size()) {
            report.listDifferentSizeComment(propertyName, expected.size(), actual.size());
            return ASSERT_FAIL;
        }

        report.increaseDepth(propertyName);

        // assert every element by index
        boolean assertResult = ASSERTED;
        for (int i = 0; i < actual.size(); i++) {
            report.assertingListElement(propertyName, i);
            assertResult &= assertObjects(report, expected.get(i), actual.get(i), properties);
        }
        report.decreaseDepth();
        return assertResult;
    }

    /**
     * Asserts two maps.
     *
     * @param propertyName
     * @param report
     * @param expected
     * @param actual
     * @param properties
     * @param nodesList
     * @param isProperty
     * @return
     */
    boolean assertMap(final String propertyName, final FabutReportBuilder report, final Map expected, final Map actual,
                      final List<ISingleProperty> properties, final NodesList nodesList, final boolean isProperty) {
        // TODO add better reporting when asserting map objects, similar to list
        final TreeSet expectedKeys = new TreeSet(expected.keySet());
        final TreeSet actualKeys = new TreeSet(actual.keySet());
        final TreeSet expectedKeysCopy = new TreeSet(expectedKeys);
        report.increaseDepth(propertyName);
        expectedKeysCopy.retainAll(actualKeys);
        boolean ok = true;
        for (final Object key : expectedKeysCopy) {
            final AssertPair assertPair = ConversionUtil.createAssertPair(expected.get(key), actual.get(key), types);
            report.assertingMapKey(key);
            ok &= assertPair(EMPTY_STRING, report, assertPair, properties, nodesList);
        }
        ok &= assertExcessExpected(propertyName, report, expected, expectedKeysCopy, actualKeys);
        ok &= assertExcessActual(propertyName, report, actual, expectedKeysCopy, actualKeys);
        report.decreaseDepth();
        return ok;
    }

    boolean assertOptional(final FabutReportBuilder report, final Optional expected, final Optional actual,
                           final List<ISingleProperty> properties) {

        if (!expected.isPresent() && !actual.isPresent()) {
            return ASSERTED;
        }
        if (expected.isPresent() ^ actual.isPresent()) {
            return ASSERT_FAIL;
        }

        return assertObjects(report, expected.get(), actual.get(), properties);
    }

    /**
     * Asserts excess, if any, from expected map.
     *
     * @param propertyName
     * @param report
     * @param expected
     * @param expectedKeys
     * @param actualKeys
     * @return
     */
    boolean assertExcessExpected(final String propertyName, final FabutReportBuilder report, final Map expected,
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

    /**
     * Asserts excess, if any, from actual map.
     *
     * @param propertyName
     * @param report
     * @param actual
     * @param expectedKeys
     * @param actualKeys
     * @return
     */
    boolean assertExcessActual(final String propertyName, final FabutReportBuilder report, final Map actual,
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

    /**
     * Cuts off parent property name from start of property path.
     *
     * @param parentPropertyName parent name
     * @param properties         list of excluded properties
     * @return List of properties without specified parent property name
     */
    List<ISingleProperty> removeParentQualification(final String parentPropertyName,
                                                    final List<ISingleProperty> properties) {

        final String parentPrefix = parentPropertyName + DOT;
        for (final ISingleProperty property : properties) {
            final String path = StringUtils.removeStart(property.getPath(), parentPrefix);
            property.setPath(path);
        }
        return properties;
    }

    /**
     * Obtains property by following rules: if there is {@link ISingleProperty} in the list of properties matching path
     * with fieldName, it removes it from the list and returns it. Otherwise, it makes new {@link Property} with
     * fieldName as path and value of field.
     *
     * @param field        expected value for {@link Property}
     * @param propertyPath path for property
     * @param properties   list of excluded properties
     * @return generated property
     */
    ISingleProperty obtainProperty(final Object field, final String propertyPath, final List<ISingleProperty> properties) {
        final ISingleProperty property = getPropertyFromList(propertyPath, properties);
        if (property != null) {
            return property;
        }
        return Fabut.value(new PropertyPath(propertyPath), field);
    }

    /**
     * Searches for property with the specified path in the list of properties, removes it from the list and returns it.
     *
     * @param propertyPath property path
     * @param properties   list of properties
     * @return {@link ISingleProperty} if there is property with same path as specified in list of properties,
     * <code>null</code> otherwise
     */
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

    /**
     * For two specified objects checks references and returns appropriate value.
     *
     * @param report       builder
     * @param pair         the pair
     * @param propertyName name of the property
     * @return {@link ReferenceCheckType#EQUAL_REFERENCE} is expected and actual have same reference, if and only if one
     * of them is null return {@link ReferenceCheckType#EXCLUSIVE_NULL}
     */
    ReferenceCheckType checkByReference(final FabutReportBuilder report, final AssertPair pair,
                                        final String propertyName) {
        if (pair.getExpected() == pair.getActual()) {
            //report.asserted(pair, propertyName);
            return ReferenceCheckType.EQUAL_REFERENCE;
        }

        if (pair.getExpected() == null ^ pair.getActual() == null) {
            report.assertFail(pair, propertyName);
            return ReferenceCheckType.EXCLUSIVE_NULL;
        }
        return ReferenceCheckType.NOT_NULL_PAIR;
    }

    /**
     * Check is object of entity type and if it is mark it as asserted entity, in other case do nothing.
     *
     * @param object        the object
     * @param isSubproperty is object subproperty
     * @return true, if successful
     */
    boolean afterAssertObject(final Object object, final boolean isSubproperty) {
        return ASSERT_FAIL;
    }

    /**
     * Extract properties and merge them into an array.
     *
     * @param properties array/arrays of properties
     * @return the list
     */
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

    /**
     * Asserts current parameters states with snapshot previously taken.
     *
     * @param report the report
     * @return true, if successful
     */
    boolean assertParameterSnapshot(final FabutReportBuilder report) {

        boolean ok = true;
        for (final SnapshotPair snapshotPair : parameterSnapshot) {
            ok &= assertObjects(report, snapshotPair.getExpected(), snapshotPair.getActual(),
                    new LinkedList<ISingleProperty>());
        }

        initParametersSnapshot();

        return ok;
    }

    /**
     * Extracts properties from specified list that have same parent as specified one.
     *
     * @param parent
     * @param properties
     * @return
     */
    List<ISingleProperty> extractPropertiesWithMatchingParent(final String parent,
                                                              final List<ISingleProperty> properties) {
        final List<ISingleProperty> extracts = new LinkedList<ISingleProperty>();
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

    /**
     * Determines if list of properties has inner properties in it.
     *
     * @param parent
     * @param properties
     * @return
     */
    boolean hasInnerProperties(final String parent, final List<ISingleProperty> properties) {
        for (final ISingleProperty property : properties) {
            if (property.getPath().startsWith(parent + DOT)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initialize parameters snapshot.
     */
    void initParametersSnapshot() {
        parameterSnapshot.clear();
    }

    /**
     * Gets the types.
     *
     * @return the types
     */
    public Map<AssertableType, List<Class<?>>> getTypes() {
        return types;
    }

    public Map<Class<?>, List<String>> getIgnoredFields() {
        return ignoredFields;
    }

    /**
     * Sets the types.
     *
     * @param types the types
     */
    public void setTypes(final Map<AssertableType, List<Class<?>>> types) {
        this.types = types;
    }

    /**
     * Gets the complex types.
     *
     * @return the complex types
     */
    public List<Class<?>> getComplexTypes() {
        return types.get(AssertableType.COMPLEX_TYPE);
    }

    /**
     * Gets the entity types.
     *
     * @return the entity types
     */
    public List<Class<?>> getEntityTypes() {
        return types.get(AssertableType.ENTITY_TYPE);
    }

    /**
     * Gets the ignored types.
     *
     * @return the ignored types
     */
    public List<Class<?>> getIgnoredTypes() {
        return types.get(AssertableType.IGNORED_TYPE);
    }

    /**
     * Gets the parameter snapshot.
     *
     * @return the parameter snapshot
     */
    List<SnapshotPair> getParameterSnapshot() {
        return parameterSnapshot;
    }

    /**
     * Gets the instance of the JUnit test that is currently running.
     *
     * @return the instance of the JUnit test that is currently running
     */
    public Object getTestInstance() {
        return testInstance;
    }

    /**
     * Sets the instance of the JUnit test that is currently running.
     *
     * @param testInstance the new instance of the JUnit test that is currently running
     */
    public void setTestInstance(final Object testInstance) {
        this.testInstance = testInstance;
    }

    /**
     * Init list of complex types.
     *
     * @param complexTypes list of complex types
     */
    public void setComplexTypes(final List<Class<?>> complexTypes) {
        types.put(AssertableType.COMPLEX_TYPE, complexTypes);
    }

    /**
     * Init list of ignored types.
     *
     * @param ignoredTypes list of ignored types
     */
    public void setIgnoredTypes(final List<Class<?>> ignoredTypes) {
        types.put(AssertableType.IGNORED_TYPE, ignoredTypes);
    }

}
