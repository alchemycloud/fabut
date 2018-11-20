package cloud.alchemy.fabut;

import cloud.alchemy.fabut.enums.AssertableType;
import cloud.alchemy.fabut.enums.ReferenceCheckType;
import cloud.alchemy.fabut.graph.NodesList;
import cloud.alchemy.fabut.model.*;
import cloud.alchemy.fabut.pair.AssertPair;
import cloud.alchemy.fabut.pair.SnapshotPair;
import cloud.alchemy.fabut.property.*;
import cloud.alchemy.fabut.report.FabutReportBuilder;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Tests methods from {@link FabutObjectAssert}.
 *
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @author Andrej Miletic
 */
@SuppressWarnings("rawtypes")
public class FabutObjectAssertTest extends AbstractFabutObjectAssertTest {
    private static final String EMPTY_STRING = "";
    private static final String TEST = "test";


    /**
     * Test for getFabutObjectAssert().assertObject if it ignores types added to ignore list.
     */
    @Test
    public void testAssertObjectIgnoreType() {
        // setup
        final IgnoredType ignoredType = new IgnoredType();
        final List<ISingleProperty> properties = new LinkedList<>();

        // method
        final boolean assertResult = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                ignoredType, properties);

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test if getFabutObjectAssert().assertObject only recognizes getMethods for getting properties.
     */
    @Test
    public void testAssertObjectNoGetMethodsType() {
        // setup
        final NoGetMethodsType noGetMethodsType = new NoGetMethodsType(TEST);
        final Property<String> jokerProperty = Fabut.value(NoGetMethodsType.PROPERTY, TEST + TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(jokerProperty);

        // method
        final boolean assertResult = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                noGetMethodsType, properties);

        // assert
        assertFalse(assertResult);
    }

    /**
     * Test if getFabutObjectAssert().assertObject throws {@link AssertionError} when there is no property associated to
     * a {@link TierOneType}.
     */
    @Test
    public void testAssertObjectNoProperty() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();

        // method
        final boolean ok = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(), tierOneType,
                properties);

        // assert
        assertFalse(ok);
    }

    /**
     * Test if getFabutObjectAssert().assertObject throws {@link AssertionError} when type has null value property and
     * we assert it with {@link NotNullProperty} with a {@link TierOneType}.
     */
    @Test
    public void testAssertObjectNotNullPropertyActuallyNull() {
        // setup
        final TierOneType tierOneType = new TierOneType(null);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.notNull(TierOneType.PROPERTY));

        // method
        final boolean ok = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(), tierOneType,
                properties);

        // assert
        assertFalse(ok);
    }

    /**
     * Test for getFabutObjectAssert().assertObject with {@link NotNullProperty} with a {@link TierOneType}.
     */
    @Test
    public void testAssertObjectNotNullProperty() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.notNull(TierOneType.PROPERTY));

        // method
        final boolean assertResult = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                tierOneType, properties);

        // assert
        assertTrue(assertResult);

    }

    /**
     * Test if getFabutObjectAssert().assertObject throws {@link AssertionError} when type has not null value property
     * and we assert it with {@link NullProperty} with a {@link TierOneType}.
     */
    @Test
    public void testAssertObjectNullPropertyActuallyNotNull() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.isNull(TierOneType.PROPERTY));

        // method
        final boolean ok = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(), tierOneType,
                properties);

        // assert
        assertFalse(ok);
    }

    /**
     * Test for getFabutObjectAssert().assertObject with {@link NullProperty} with a {@link TierOneType}.
     */
    @Test
    public void testAssertObjectNullProperty() {
        // setup
        final TierOneType tierOneType = new TierOneType(null);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.isNull(TierOneType.PROPERTY));

        // method
        final boolean assertResult = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                tierOneType, properties);

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for getFabutObjectAssert().assertObject with {@link IgnoredProperty} with a {@link TierOneType}.
     */
    @Test
    public void testAssertObjectIgnoreProperty() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.ignored(TierOneType.PROPERTY));

        // method
        final boolean assertResult = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                tierOneType, properties);

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for asssertObject with {@link Property} when expected value is null and actual value is null with a
     * {@link TierOneType}.
     */
    @Test
    public void testAssertObjectChangedPropertyExpectedNullActualNull() {
        // setup
        final TierOneType tierOneType = new TierOneType(null);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(TierOneType.PROPERTY, null));

        // method
        final boolean assertResult = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                tierOneType, properties);

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test if getFabutObjectAssert().assertObject throws {@link AssertionError} when expected value is not null and
     * actual value is null and we assert it with {@link Property} with a {@link TierOneType}.
     */
    @Test
    public void testAssertObjectChangedPropertyActualNull() {
        // setup
        final TierOneType tierOneType = new TierOneType(null);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(TierOneType.PROPERTY, TEST));

        // method
        final boolean ok = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(), tierOneType,
                properties);

        // assert
        assertFalse(ok);
    }

    /**
     * Test if getFabutObjectAssert().assertObject throws {@link AssertionError} when expected value is null and actual
     * value is not null and we assert it with {@link Property} with a {@link TierOneType}.
     */
    @Test
    public void testAssertObjectChangedPropertyExpectedNull() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(TierOneType.PROPERTY, null));

        // method
        final boolean ok = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(), tierOneType,
                properties);

        // assert
        assertFalse(ok);
    }

    /**
     * Test for getFabutObjectAssert().assertObject with {@link Property} when expected value is equal to actual value
     * with a {@link TierOneType}.
     */
    @Test
    public void testAssertObjectChangedPropertyEqual() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(TierOneType.PROPERTY, TEST));

        // method
        final boolean assertResult = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                tierOneType, properties);

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for getFabutObjectAssert().assertObject with {@link Property} when expected value is not equal to actual
     * value with a {@link TierOneType}.
     */
    @Test
    public void testAssertObjectChangedPropertyNotEqual() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(TierOneType.PROPERTY, TEST + TEST));

        // method
        final boolean ok = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(), tierOneType,
                properties);

        // assert
        assertFalse(ok);
    }

    /**
     * Test for getFabutObjectAssert().assertObject when ignored type is property of complex object.
     */
    @Test
    public void testAssertObjectChangedPropertyWithIgnoredType() {
        // setup
        final TierTwoTypeWithIgnoreProperty tierTwoTypeWithIgnoreProperty = new TierTwoTypeWithIgnoreProperty(
                new IgnoredType());
        final Property<IgnoredType> jokerProperty = Fabut.value(TierTwoTypeWithIgnoreProperty.IGNORED_TYPE,
                new IgnoredType());
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(jokerProperty);

        // method
        final boolean assertResult = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                tierTwoTypeWithIgnoreProperty, properties);

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test if getFabutObjectAssert().assertObject throws {@link AssertionError} when size of actual list is not equal
     * to expected list with a {@link TierTwoTypeWithListProperty}.
     */
    @Test
    public void testAssertObjectChangedPropertyWithListNotEqualSize() {
        // setup
        final TierTwoTypeWithListProperty tierTwoTypeWithListProperty = new TierTwoTypeWithListProperty(
                new ArrayList<>());
        final List<String> jokerList = new ArrayList<>();
        jokerList.add(TEST);

        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(TierTwoTypeWithListProperty.PROPERTY, jokerList));

        // method
        final boolean ok = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                tierTwoTypeWithListProperty, properties);

        // assert
        assertFalse(ok);
    }

    /**
     * Test for getFabutObjectAssert().assertObject with {@link Property} when all actual list members are primitive
     * types and are equal to expected list members with a {@link TierTwoTypeWithListProperty}.
     */
    @Test
    public void testAssertObjectChangedPropertyWithListAllPropertiesEqual() {
        // setup
        final List<String> actualList = new ArrayList<>();
        actualList.add(TEST);

        final List<String> expectedList = new ArrayList<>();
        expectedList.add(TEST);

        final TierTwoTypeWithListProperty tierTwoTypeWithListProperty = new TierTwoTypeWithListProperty(actualList);

        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(TierTwoTypeWithListProperty.PROPERTY, expectedList));

        // method
        final boolean ok = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                tierTwoTypeWithListProperty, properties);

        // assert
        assertTrue(ok);
    }

    /**
     * Test for getFabutObjectAssert().assertObject with {@link Property} when actual list members are primitive types
     * and are not equal to expected list members with a {@link TierTwoTypeWithListProperty}.
     */
    @Test
    public void testAssertObjectChangedPropertyWithListAllPropertiesNotEqual() {
        // setup
        final List<String> actualList = new ArrayList<>();
        actualList.add(TEST);

        final List<String> expectedList = new ArrayList<>();
        expectedList.add(TEST + TEST);

        final TierTwoTypeWithListProperty tierTwoTypeWithListProperty = new TierTwoTypeWithListProperty(actualList);

        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(TierTwoTypeWithListProperty.PROPERTY, expectedList));

        // method
        final boolean ok = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                tierTwoTypeWithListProperty, properties);

        // assert
        assertFalse(ok);
    }

    /**
     * Test for getFabutObjectAssert().assertObjects for two {@link TierTwoType} objects with equal values.
     */
    @Test
    public void testAssertObjectsTierTwoObjectsWithEqualValues() {
        // setup
        final TierTwoType actual = new TierTwoType(new TierOneType(TEST));
        final TierTwoType expected = new TierTwoType(new TierOneType(TEST));

        // method
        getFabutObjectAssert().assertObjects(new FabutReportBuilder(), expected, actual,
                new LinkedList<>());
    }

    /**
     * Test for getFabutObjectAssert().assertObjects for two {@link TierTwoType} objects with equal values.
     */
    @Test
    public void testAssertObjectsTierTwoObjectsWithNotEqualValues() {
        // setup
        final TierTwoType actual = new TierTwoType(new TierOneType(TEST));
        final TierTwoType expected = new TierTwoType(new TierOneType(TEST + TEST));

        // method
        final boolean ok = getFabutObjectAssert().assertObjects(new FabutReportBuilder(), expected, actual,
                new LinkedList<>());

        // assert
        assertFalse(ok);
    }

    /**
     * Test for getFabutObjectAssert().assertObjects for two {@link List}s of {@link TierOneType} with equal values.
     */
    @Test
    public void testAssertObjectsListOfTierOneObjectsWithEqualValues() {
        // setup
        final List<TierOneType> actual = new ArrayList<>();
        actual.add(new TierOneType(TEST));
        actual.add(new TierOneType(TEST + TEST));
        actual.add(new TierOneType(TEST + TEST + TEST));

        final List<TierOneType> expected = new ArrayList<>();
        expected.add(new TierOneType(TEST));
        expected.add(new TierOneType(TEST + TEST));
        expected.add(new TierOneType(TEST + TEST + TEST));

        // method
        final boolean assertResult = getFabutObjectAssert().assertObjects(new FabutReportBuilder(), expected, actual,
                new LinkedList<>());

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for getFabutObjectAssert().assertObjects for two {@link List}s of {@link TierOneType} with unequal values.
     */
    @Test
    public void testAssertObjectsListOfTierOneObjectsWithNotEqualValues() {
        // setup
        final List<TierOneType> actual = new ArrayList<>();
        actual.add(new TierOneType(TEST));
        actual.add(new TierOneType(TEST + TEST));
        actual.add(new TierOneType(TEST + TEST + TEST));

        final List<TierOneType> expected = new ArrayList<>();
        expected.add(new TierOneType(TEST + TEST));
        expected.add(new TierOneType(TEST + TEST + TEST));
        expected.add(new TierOneType(TEST + TEST + TEST + TEST));

        // method
        final boolean ok = getFabutObjectAssert().assertObjects(new FabutReportBuilder(), expected, actual,
                new LinkedList<>());

        // assert
        assertFalse(ok);
    }

    /**
     * Test for getFabutObjectAssert().assertObjects with two {@link TierTwoTypeWithPrimitiveProperty} with
     * {@link IgnoredProperty}.
     */
    @Test
    public void testAssertObjectsTierTwoTypeWithPrimitivePropertyWithIgnoreProperty() {
        // setup
        final TierTwoTypeWithPrimitiveProperty actual = new TierTwoTypeWithPrimitiveProperty(new TierOneType(TEST),
                TEST);
        final TierTwoTypeWithPrimitiveProperty expected = new TierTwoTypeWithPrimitiveProperty(new TierOneType(TEST
                + TEST), TEST);

        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.ignored(TierTwoType.PROPERTY.chain(TierOneType.PROPERTY)));

        // method
        final boolean assertResult = getFabutObjectAssert().assertObjects(new FabutReportBuilder(), expected, actual,
                properties);

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for getFabutObjectAssert().assertObjects with expected and actual same instance.
     */
    @Test
    public void testAssertObjectsSameInstances() {
        // setup
        final TierOneType tierOneType = new TierOneType();

        // method
        getFabutObjectAssert().assertObjects(new FabutReportBuilder(), tierOneType, tierOneType,
                new LinkedList<>());
    }

    /**
     * Test for getFabutObjectAssert().assertObjects with two {@link TierSixType} when complex object depth is six.
     */
    @Test
    public void testAssertObjectsTierSixTypeDepthSix() {
        // setup
        final TierSixType actual = new TierSixType(new TierFiveType(new TierFourType(new TierThreeType(new TierTwoType(
                new TierOneType(TEST))))));
        final TierSixType expected = new TierSixType(new TierFiveType(new TierFourType(new TierThreeType(
                new TierTwoType(new TierOneType(TEST + TEST))))));

        // method
        final boolean ok = getFabutObjectAssert().assertObjects(new FabutReportBuilder(), expected, actual,
                new LinkedList<>());

        // assert
        assertFalse(ok);
    }

    /**
     * Test for getFabutObjectAssert().assertObjects with {@link TierOneType} when varargs of actual is called.
     */
    @Test
    public void testAssertObjectsVarargsExpected() {
        // setup
        final List<TierOneType> expected = new ArrayList<>();
        expected.add(new TierOneType(TEST));
        final TierOneType actual = new TierOneType(TEST);

        // method
        final boolean assertResult = getFabutObjectAssert().assertObjects(new FabutReportBuilder(), expected, actual,
                new LinkedList<>());

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for getFabutObjectAssert().assertObjects with {@link TierOneType} when varargs of properties is called.
     */
    @Test
    public void testAssertObjectVarargsProperties() {
        // setup
        final TierOneType actual = new TierOneType(TEST);

        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(TierOneType.PROPERTY, TEST));

        // method
        final boolean assertResult = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                actual, properties);

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for {@link FabutObjectAssert#assertPair(String, FabutReportBuilder, AssertPair, List, NodesList)} when object graph is trivial.
     */
    @Test
    public void testAssertPairrTrivialGraphEqual() {
        // setup
        final A actual = new A(null);
        final A expected = new A(null);
        final NodesList nodesList = new NodesList();

        // method
        final boolean assertValue = getFabutObjectAssert().assertPair(EMPTY_STRING, new FabutReportBuilder(),
                new AssertPair(expected, actual, AssertableType.COMPLEX_TYPE), new ArrayList<>(),
                nodesList);

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for {@link FabutObjectAssert#assertPair(String, FabutReportBuilder, AssertPair, List,
     * NodesList)} when actual and expected object nodes are contained in nodes list.
     */
    @Test
    public void testAssertPairNodePairInList() {
        // setup
        final Object actual = new Object();
        final Object expected = new Object();
        final NodesList nodesList = new NodesList();
        nodesList.addPair(expected, actual);

        // method
        final boolean assertValue = getFabutObjectAssert().assertPair(EMPTY_STRING, new FabutReportBuilder(),
                new AssertPair(expected, actual, AssertableType.PRIMITIVE_TYPE), new ArrayList<>(),
                nodesList);

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for {@link FabutObjectAssert#assertPair(String, FabutReportBuilder, AssertPair, List,
     * NodesList)} when asserting objects are cyclic graphs.
     */
    @Test
    public void testAssertPairCyclicGraphEqual() {
        // setup
        final NodesList nodesList = new NodesList();

        final A actual = new A(null);
        actual.setB(new B(new C(actual)));

        final A expected = new A(null);
        expected.setB(new B(new C(expected)));

        // method
        final boolean assertValue = getFabutObjectAssert().assertPair(EMPTY_STRING, new FabutReportBuilder(),
                new AssertPair(expected, actual, AssertableType.COMPLEX_TYPE), new ArrayList<>(),
                nodesList);

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for {@link FabutObjectAssert#assertSubfields(FabutReportBuilder, AssertPair, List, NodesList, String)}
     * when getting reference to field via {@link Method}'s method invoke and it trows exception.
     */
    @Test
    public void testAssertSubfieldsExceptionInReflectionCalls() {
        // method
        final boolean t = getFabutObjectAssert().assertSubfields(new FabutReportBuilder(),
                new AssertPair(new TierOneType(TEST), new UnknownType(), AssertableType.COMPLEX_TYPE),
                new LinkedList<>(), new NodesList(), EMPTY_STRING);

        // assert
        assertFalse(t);
    }

    /**
     * Test for {@link FabutObjectAssert#assertSubfields(FabutReportBuilder, AssertPair, List, NodesList, String)} when object pair can be asseted.
     */
    @Test
    public void testAssertSubfieldsAsserted() {
        // setup
        final TierOneType expected = new TierOneType(TEST);
        final TierOneType actual = new TierOneType(TEST);
        final NodesList nodeList = new NodesList();
        final AssertPair pair = new AssertPair(expected, actual, AssertableType.COMPLEX_TYPE);
        nodeList.addPair(pair);

        // method
        final boolean assertResult = getFabutObjectAssert().assertSubfields(new FabutReportBuilder(), pair,
                new LinkedList<>(), nodeList, EMPTY_STRING);

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for {@link FabutObjectAssert#assertSubfields(FabutReportBuilder, AssertPair, List, NodesList, String)} when asserting objects fail.
     */
    @Test
    public void testAssertSubfieldsAssertFails() {
        // setup
        final TierOneType expected = new TierOneType(TEST);
        final TierOneType actual = new TierOneType(TEST + TEST);
        final NodesList nodeList = new NodesList();
        final AssertPair pair = new AssertPair(expected, actual, AssertableType.COMPLEX_TYPE);
        nodeList.addPair(pair);

        // method
        final boolean assertResult = getFabutObjectAssert().assertSubfields(new FabutReportBuilder(), pair,
                new LinkedList<>(), nodeList, EMPTY_STRING);

        // assert
        assertFalse(assertResult);
    }

    /**
     * Test for {@link FabutObjectAssert#assertSubfields(FabutReportBuilder, AssertPair, List, NodesList, String)} when asserting pair of objects with no get methods.
     */
    @Test
    public void testAssertSubfieldsNoGetMethodsType() {
        // setup
        final NoGetMethodsType expected = new NoGetMethodsType(TEST);
        final NoGetMethodsType actual = new NoGetMethodsType(TEST);
        final NodesList nodeList = new NodesList();
        final AssertPair pair = new AssertPair(expected, actual, AssertableType.COMPLEX_TYPE);
        nodeList.addPair(pair);

        // method
        final boolean assertResult = getFabutObjectAssert().assertSubfields(new FabutReportBuilder(), pair,
                new LinkedList<>(), nodeList, EMPTY_STRING);

        // assert
        assertTrue(assertResult);

    }

    /**
     * Test for assertChangedProperty of {@link FabutObjectAssert} when actual and expected values are nulls.
     */
    @Test
    public void testAssertChangedPropertyBothNulls() {
        // method
        final boolean assertValue = getFabutObjectAssert().assertPair(EMPTY_STRING, new FabutReportBuilder(),
                new AssertPair(null, null, AssertableType.PRIMITIVE_TYPE), new ArrayList<>(),
                new NodesList());

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for assertChangedProperty of {@link FabutObjectAssert} when actual is instance of complex class.
     */
    @Test
    public void testAssertChangedPropertyComplexType() {
        // setup
        final TierOneType actual = new TierOneType(TEST);
        final TierOneType expected = new TierOneType(TEST);

        // method
        final boolean assertValue = getFabutObjectAssert().assertPair(EMPTY_STRING, new FabutReportBuilder(),
                new AssertPair(expected, actual, AssertableType.COMPLEX_TYPE, false), new ArrayList<>(),
                new NodesList());

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for assertChangedProperty of {@link FabutObjectAssert} when actual is ignored type.
     */
    @Test
    public void testAssertChangedPropertyIgnoredType() {
        // setup
        final IgnoredType actual = new IgnoredType();
        final IgnoredType expected = new IgnoredType();

        // method
        final boolean assertValue = getFabutObjectAssert().assertPair(EMPTY_STRING, new FabutReportBuilder(),
                new AssertPair(expected, actual, AssertableType.IGNORED_TYPE, true), new ArrayList<>(),
                new NodesList());

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for assertChangedProperty of {@link FabutObjectAssert} when actual is type unknown to test util and its
     * considered to be primitive.Actual is not equal to expected.
     */
    @Test
    public void testAssertChangedPropertyPrimitiveTypeTrue() {
        // setup
        final String actual = TEST;
        final String expected = TEST;

        // method
        final boolean assertValue = getFabutObjectAssert().assertPair(EMPTY_STRING, new FabutReportBuilder(),
                new AssertPair(expected, actual, AssertableType.PRIMITIVE_TYPE, true),
                new ArrayList<>(), new NodesList());

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for assertChangedProperty of {@link FabutObjectAssert} when actual is type unknown to test util and its
     * considered to be primitive. Actual is not equal to expected.
     */
    @Test
    public void testAssertChangedPropertyPrimitiveTypeFalse() {
        // setup
        final String actual = TEST;
        final String expected = TEST + TEST;

        // method
        final boolean assertValue = getFabutObjectAssert().assertPair(EMPTY_STRING, new FabutReportBuilder(),
                new AssertPair(expected, actual, AssertableType.PRIMITIVE_TYPE, true),
                new ArrayList<>(), new NodesList());

        // assert
        assertFalse(assertValue);
    }

    /**
     * Test for assertChangedProperty of {@link FabutObjectAssert} when actual list and expected list are equal.
     */
    @Test
    public void testAssertChangedPropertyListTypeEqual() {
        // setup
        final List<String> actual = new ArrayList<>();
        actual.add(TEST);
        actual.add(TEST);
        final List<String> expected = new ArrayList<>();
        expected.add(TEST);
        expected.add(TEST);

        // method
        final boolean assertValue = getFabutObjectAssert().assertPair(EMPTY_STRING, new FabutReportBuilder(),
                new AssertPair(expected, actual, AssertableType.LIST_TYPE, true), new LinkedList<>(),
                new NodesList());

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for assertProperties of {@link FabutObjectAssert} when expected property is {@link NotNullProperty} and
     * actual is not null.
     */
    @Test
    public void testAssertPropertiesNotNullPropertyTrue() {
        // method
        final boolean assertValue = getFabutObjectAssert().assertProperty(EMPTY_STRING, new FabutReportBuilder(),
                Fabut.notNull(TierOneType.PROPERTY), new TierOneType(TEST), "", new ArrayList<>(), null,
                true);

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for assertProperties of {@link FabutObjectAssert} when expected property is {@link NotNullProperty} and
     * actual is null.
     */
    @Test
    public void testAssertPropertiesNotNullPropertyFalse() {
        // method
        final boolean assertValue = getFabutObjectAssert().assertProperty(EMPTY_STRING, new FabutReportBuilder(),
                Fabut.notNull(TierOneType.PROPERTY), null, "", new ArrayList<>(), null, true);

        // assert
        assertFalse(assertValue);
    }

    /**
     * Test for assertProperties of {@link FabutObjectAssert} when expected property is {@link NullProperty} and actual
     * is null.
     */
    @Test
    public void testAssertPropertiesNullPropertyTrue() {
        // method
        final boolean assertValue = getFabutObjectAssert().assertProperty(EMPTY_STRING, new FabutReportBuilder(),
                Fabut.isNull(TierOneType.PROPERTY), null, "", new ArrayList<>(), null, true);

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for assertProperties of {@link FabutObjectAssert} when expected property is {@link NullProperty} and actual
     * is not null.
     */
    @Test
    public void testAssertPropertiesNullPropertyFalse() {
        // method
        final boolean assertValue = getFabutObjectAssert().assertProperty(EMPTY_STRING, new FabutReportBuilder(),
                Fabut.isNull(TierOneType.PROPERTY), new TierOneType(TEST), "", new ArrayList<>(), null,
                true);

        // assert
        assertFalse(assertValue);
    }

    /**
     * Test for assertProperties of {@link FabutObjectAssert} when expected property is {@link IgnoredProperty}.
     */
    @Test
    public void testAssertPropertiesIgnoreProperty() {
        // method
        final boolean assertValue = getFabutObjectAssert().assertProperty(EMPTY_STRING, new FabutReportBuilder(),
                Fabut.ignored(TierOneType.PROPERTY), new TierOneType(TEST), "", new ArrayList<>(), null,
                true);

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for assertList of {@link FabutObjectAssert} when actual list has different size than expected list.
     */
    @Test
    public void testAssertListNotEqualSize() {
        // setup
        final List<String> actual = new ArrayList<>();
        actual.add(TEST);
        actual.add(TEST);
        final List<String> expected = new ArrayList<>();
        expected.add(TEST);

        // method
        final boolean assertValue = getFabutObjectAssert().assertList(EMPTY_STRING, new FabutReportBuilder(), expected,
                actual, new ArrayList<>(), new NodesList(), false);

        // assert
        assertFalse(assertValue);
    }

    /**
     * Test for assertList of {@link FabutObjectAssert} when actual list is equal to expected list.
     */
    @Test
    public void testAssertListEqual() {
        // setup
        final List<String> actual = new ArrayList<>();
        actual.add(TEST);
        actual.add(TEST);
        final List<String> expected = new ArrayList<>();
        expected.add(TEST);
        expected.add(TEST);

        // method
        final boolean assertValue = getFabutObjectAssert().assertList(EMPTY_STRING, new FabutReportBuilder(), expected,
                actual, new ArrayList<>(), new NodesList(), false);

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for assertList of {@link FabutObjectAssert} when actual list is equal to expected list.
     */
    @Test
    public void testAssertListNotEqual() {
        // setup
        final List<String> actual = new ArrayList<>();
        actual.add(TEST);
        actual.add(TEST + TEST);
        final List<String> expected = new ArrayList<>();
        expected.add(TEST);
        expected.add(TEST);

        // method
        final boolean assertValue = getFabutObjectAssert().assertList(EMPTY_STRING, new FabutReportBuilder(), expected,
                actual, new ArrayList<>(), new NodesList(), false);

        // assert
        assertFalse(assertValue);
    }

    /**
     * Test for preAssertObjectWithProperties of {@link FabutObjectAssert} when object properties match specified
     * properties.
     */
    @Test
    public void testPreAssertObjectWithPropertiesEqual() {
        // setup
        final List<ISingleProperty> properties = new ArrayList<>();
        properties.add(Fabut.value(TierOneType.PROPERTY, "ninja"));

        // method
        getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(), new TierOneType("ninja"),
                properties);

    }

    /**
     * Test for preAssertObjectWithProperties of {@link FabutObjectAssert} when object properties don't match specified
     * properties.
     */
    @Test
    public void testPreAssertObjectWithPropertiesNotEqual() {
        // setup
        final List<ISingleProperty> properties = new ArrayList<>();
        properties.add(Fabut.value(TierOneType.PROPERTY, TEST));

        // method
        final boolean assertValue = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                new TierOneType(TEST + TEST), properties);

        // assert
        assertFalse(assertValue);
    }

    /**
     * Test for preAssertObjectWithProperties of {@link FabutObjectAssert} when there is no property for field and field
     * value is null.
     */
    @Test
    public void testPreAssertObjectWithPropertiesMethodReturnsNull() {
        // setup
        final List<ISingleProperty> properties = new ArrayList<>();

        // method
        final boolean assertValue = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                new TierOneType(null), properties);

        // assert
        assertFalse(assertValue);
    }

    /**
     * Test for preAssertObjectWithProperties of {@link FabutObjectAssert} when specified property's path doesn't match
     * object's field path.
     */
    @Test
    public void testPreAssertObjectWithPropertiesBadProperties() {
        // setup
        final List<ISingleProperty> properties = new ArrayList<>();
        properties.add(Fabut.value(new PropertyPath<>(TEST), TEST));

        // method
        final boolean assertValue = getFabutObjectAssert().assertObjectWithProperties(new FabutReportBuilder(),
                new TierOneType(TEST), properties);

        // assertTrue
        assertFalse(assertValue);
    }

    /**
     * Test for removeParentQualificationForProperties of {@link FabutObjectAssert}.
     */
    @Test
    public void testRemoveParentQualificationForProperty() {
        // setup
        final List<ISingleProperty> properties = new ArrayList<>();

        properties.add(Fabut.notNull(new PropertyPath("parent.id")));
        properties.add(Fabut.notNull(new PropertyPath("parent.name")));
        properties.add(Fabut.notNull(new PropertyPath("parent.lastname")));

        // method
        final List<ISingleProperty> unqualifiedProperties = getFabutObjectAssert().removeParentQualification("parent",
                properties);

        // assert
        assertEquals("id", unqualifiedProperties.get(0).getPath());
        assertEquals("name", unqualifiedProperties.get(1).getPath());
        assertEquals("lastname", unqualifiedProperties.get(2).getPath());
    }

    /**
     * Test for generateNewProperty of {@link FabutObjectAssert} when property for that field already exists.
     */
    @Test
    public void testGeneratePropertyFromListOfExcluded() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new ArrayList<>();
        properties.add(Fabut.notNull(TierOneType.PROPERTY));
        final int numProperties = properties.size();

        // method
        final ISingleProperty property = getFabutObjectAssert().obtainProperty(tierOneType, TierOneType.PROPERTY.getPath(),
                properties);

        // assert
        assertEquals(TierOneType.PROPERTY.getPath(), property.getPath());
        assertEquals(numProperties - 1, properties.size());
    }

    /**
     * Test for generateNewProperty of {@link FabutObjectAssert} when specified field needs to be wrapped inside new
     * ChangedProperty.
     */
    @Test
    public void testGeneratePropertyCreateNew() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new ArrayList<>();
        properties.add(Fabut.notNull(new PropertyPath<>(TEST)));
        final int numProperties = properties.size();

        // method
        final Property property = (Property) getFabutObjectAssert().obtainProperty(tierOneType, TierOneType.PROPERTY.getPath(),
                properties);

        // assert
        assertEquals(TierOneType.PROPERTY.getPath(), property.getPath());
        assertEquals(tierOneType, property.getValue());
        assertEquals(numProperties, properties.size());

    }

    /**
     * Test for popProperty of {@link FabutObjectAssert} when specified property's path matches specified path.
     */
    @Test
    public void testPopPropertyEqualPath() {
        // setup
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.isNull(new PropertyPath(TEST)));

        // method
        final ISingleProperty property = getFabutObjectAssert().getPropertyFromList(TEST, properties);

        // assert
        assertEquals(TEST, property.getPath());
        assertEquals(0, properties.size());
    }

    /**
     * Test for popProperty of {@link FabutObjectAssert} when there are no properties in the list.
     */
    @Test
    public void testPopPropertyNoProperties() {
        // setup
        final List<ISingleProperty> properties = new LinkedList<>();

        // method
        final ISingleProperty property = getFabutObjectAssert().getPropertyFromList(TEST, properties);

        // assert
        assertNull(property);
    }

    /**
     * Test for popProperty of {@link FabutObjectAssert} when specified property's path doesn't match specified path.
     */
    @Test
    public void testPopPropertyNotEqualPath() {
        // setup
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.isNull(new PropertyPath(TEST)));

        // method
        final ISingleProperty property = getFabutObjectAssert().getPropertyFromList(TEST + TEST, properties);

        // assert
        assertNull(property);
    }

    /**
     * Test for checkForNull of {@link FabutObjectAssert} with two null objects.
     */
    @Test
    public void testCheckForNullsTrueNull() {
        // method
        final ReferenceCheckType assertValue = getFabutObjectAssert().checkByReference(new FabutReportBuilder(),
                new AssertPair(null, null, AssertableType.COMPLEX_TYPE), TEST);

        // assert
        assertEquals(ReferenceCheckType.EQUAL_REFERENCE, assertValue);
    }

    /**
     * Test for checkForNulls of {@link FabutObjectAssert} with two same references.
     */
    @Test
    public void testCheckForNullsTrueNotNull() {
        // setup
        final TierOneType expected = new TierOneType();

        // method
        final ReferenceCheckType assertValue = getFabutObjectAssert().checkByReference(new FabutReportBuilder(),
                new AssertPair(expected, expected, AssertableType.COMPLEX_TYPE), TEST);

        // assert
        assertEquals(ReferenceCheckType.EQUAL_REFERENCE, assertValue);
    }

    /**
     * Test for checkForNulls of {@link FabutObjectAssert} with one object null and one not null, both cases.
     */
    @Test
    public void testCheckForNullsFalse() {
        // method
        final ReferenceCheckType assertValue1 = getFabutObjectAssert().checkByReference(new FabutReportBuilder(),
                new AssertPair(new Object(), null, AssertableType.COMPLEX_TYPE), TEST);
        final ReferenceCheckType assertValue2 = getFabutObjectAssert().checkByReference(new FabutReportBuilder(),
                new AssertPair(null, new Object(), AssertableType.COMPLEX_TYPE), TEST);

        // assert
        assertEquals(ReferenceCheckType.EXCLUSIVE_NULL, assertValue1);
        assertEquals(ReferenceCheckType.EXCLUSIVE_NULL, assertValue2);
    }

    /**
     * Test for checkForNulls of {@link FabutObjectAssert} with both objects not null.
     */
    @Test
    public void testCheckForNullsNull() {
        // method
        final ReferenceCheckType assertValue = getFabutObjectAssert().checkByReference(new FabutReportBuilder(),
                new AssertPair(new Object(), new Object(), AssertableType.COMPLEX_TYPE), TEST);

        // assert
        assertEquals(ReferenceCheckType.NOT_NULL_PAIR, assertValue);
    }

    /**
     * Test for extractProperties when parameters are of types {@link ISingleProperty} and {@link IMultiProperties}.
     */
    @Test
    public void testExtractPropertiesMixed() {
        // method
        final List<ISingleProperty> properties = getFabutObjectAssert().extractProperties(
                Fabut.value(EntityTierOneType.PROPERTY, ""),
                Fabut.notNull(EntityTierOneType.ID, EntityTierOneType.PROPERTY));

        // assert
        assertEquals(3, properties.size());
        assertEquals(EntityTierOneType.PROPERTY.getPath(), properties.get(0).getPath());
        assertEquals(EntityTierOneType.ID.getPath(), properties.get(1).getPath());
        assertEquals(EntityTierOneType.PROPERTY.getPath(), properties.get(2).getPath());
    }

    /**
     * Test for extractProperties when all passed parameters are of type {@link ISingleProperty}.
     */
    @Test
    public void testExtractPropertiesAllISingleProperty() {
        // setup
        final IProperty[] propArray = new ISingleProperty[]{Fabut.value(EntityTierOneType.PROPERTY, ""),
                Fabut.value(EntityTierOneType.ID, 0)};

        // method
        final List<ISingleProperty> properties = getFabutObjectAssert().extractProperties(propArray);

        // assert
        assertEquals(propArray.length, properties.size());
        for (int i = 0; i < propArray.length; i++) {
            assertEquals(((ISingleProperty) propArray[i]).getPath(), properties.get(i).getPath());
        }
    }

    /**
     * Test for extractProperties when all passed parameters are of type {@link IMultiProperties}.
     */
    @Test
    public void testExtractPropertiesAllIMultiProperty() {
        // setup
        final IMultiProperties notNullMultiProp = Fabut.notNull(EntityTierOneType.PROPERTY, EntityTierOneType.ID);
        final IMultiProperties ignoredMultiProp = Fabut.ignored(EntityTierOneType.PROPERTY, EntityTierOneType.ID);
        final IProperty[] multiPropArray = new IMultiProperties[]{notNullMultiProp, ignoredMultiProp};

        // method
        final List<ISingleProperty> properties = getFabutObjectAssert().extractProperties(multiPropArray);

        // assert
        assertEquals(notNullMultiProp.getProperties().size() + ignoredMultiProp.getProperties().size(),
                properties.size());
        assertEquals(notNullMultiProp.getProperties().get(0).getPath(), properties.get(0).getPath());
        assertEquals(notNullMultiProp.getProperties().get(1).getPath(), properties.get(1).getPath());
        assertEquals(ignoredMultiProp.getProperties().get(0).getPath(), properties.get(2).getPath());
        assertEquals(ignoredMultiProp.getProperties().get(1).getPath(), properties.get(3).getPath());
    }

    /**
     * Test for getFabutObjectAssert().assertObjects with parameters of type {@link IMultiProperties}.
     */
    @Test
    public void testAssertObjectsMultiProperty() {
        // setup
        final TierTwoTypeWithPrimitiveProperty actual = new TierTwoTypeWithPrimitiveProperty(new TierOneType(TEST),
                TEST);
        final TierTwoTypeWithPrimitiveProperty expected = new TierTwoTypeWithPrimitiveProperty(new TierOneType(TEST
                + TEST), TEST + TEST);

        // method
        getFabutObjectAssert().assertObjects(
                new FabutReportBuilder(),
                expected,
                actual,
                getFabutObjectAssert().extractProperties(
                        Fabut.ignored(TierTwoType.PROPERTY.chain(TierOneType.PROPERTY).chain(TierTwoTypeWithPrimitiveProperty.PROPERTY2))));
    }

    /**
     * Test for {@link FabutObjectAssert#takeSnapshot(FabutReportBuilder, Object...)}.
     */
    @Test
    public void testTakeSnapshot() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);

        // method
        getFabutObjectAssert().takeSnapshot(new FabutReportBuilder(), tierOneType);

        // assert
        final List<SnapshotPair> parameterSnapshot = getFabutObjectAssert().getParameterSnapshot();
        assertEquals(parameterSnapshot.size(), 1);

        final List<ISingleProperty> properties = new LinkedList<>();

        getFabutObjectAssert().assertObjects(new FabutReportBuilder(), new TierOneType(TEST),
                parameterSnapshot.get(0).getExpected(), properties);
        getFabutObjectAssert().assertObjects(new FabutReportBuilder(), new TierOneType(TEST),
                parameterSnapshot.get(0).getActual(), properties);

        assertTrue(getFabutObjectAssert().assertParameterSnapshot(new FabutReportBuilder()));
    }

    @Test
    public void testExtractPropertiesWithMatchingParent() {
        // setup
        final String parent = "parent";
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(new PropertyPath<>("parent.name"), "name"));
        properties.add(Fabut.value(new PropertyPath<>("parents"), "parents"));
        properties.add(Fabut.value(new PropertyPath<>("parent.lastName"), "lastName"));
        properties.add(Fabut.value(new PropertyPath<>("parent.address.city"), "city"));

        // method
        final List<ISingleProperty> extracted = getFabutObjectAssert().extractPropertiesWithMatchingParent(parent,
                properties);

        // assert
        assertEquals(1, properties.size());
        assertEquals("parents", properties.get(0).getPath());
        assertEquals(3, extracted.size());
        assertEquals("parent.name", extracted.get(0).getPath());
        assertEquals("parent.lastName", extracted.get(1).getPath());
        assertEquals("parent.address.city", extracted.get(2).getPath());
    }

    @Test
    public void testHasInnerPropertyTrue() {
        // setup
        final String parent = "parent";
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(new PropertyPath<>("parent.name"), "name"));

        // method
        final boolean hasInnerProperties = getFabutObjectAssert().hasInnerProperties(parent, properties);

        // assert
        assertTrue(hasInnerProperties);
    }

    @Test
    public void testHasInnerPropertyFalse() {
        // setup
        final String parent = "parent";
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(new PropertyPath<>("parents"), "name"));

        // method
        final boolean hasInnerProperties = getFabutObjectAssert().hasInnerProperties(parent, properties);

        // assert
        assertFalse(hasInnerProperties);
    }

    @Test
    public void testAssertInnerProperty() {
        // setup
        final TierOneType actual = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(new PropertyPath<>("property.property"), TEST));
        final FabutReportBuilder report = new FabutReportBuilder();
        // assert
        assertTrue(getFabutObjectAssert().assertInnerProperty(report, actual, properties, "property"));

    }

    @Test
    public void testAssertInnerObject() {
        // setup
        final TierOneType actual = new TierOneType(TEST);
        final TierOneType expected = new TierOneType(TEST + TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(Fabut.value(new PropertyPath<>("property.property"), TEST + TEST));
        final FabutReportBuilder report = new FabutReportBuilder();
        // assert
        assertTrue(getFabutObjectAssert().assertInnerObject(report, actual, expected, properties, "property"));
    }

    @Test
    public void testAssertMapTrue() {
        // setup
        final Map<String, TierOneType> expected = new HashMap<>();
        expected.put("first", new TierOneType(TEST));
        expected.put("second", new TierOneType(TEST));

        final Map<String, TierOneType> actual = new HashMap<>();
        actual.put("first", new TierOneType(TEST));
        actual.put("second", new TierOneType(TEST));

        // method
        final boolean assertResult = getFabutObjectAssert().assertMap("", new FabutReportBuilder(), expected, actual,
                new ArrayList<>(), new NodesList(), false);

        // assert
        assertTrue(assertResult);
    }

    @Test
    public void testAssertMapFalseSameSize() {
        // setup
        final Map<String, TierOneType> expected = new HashMap<>();
        expected.put("first", new TierOneType(TEST));
        expected.put("second", new TierOneType(TEST));

        final Map<String, TierOneType> actual = new HashMap<>();
        actual.put("first", new TierOneType(TEST + TEST));
        actual.put("second", new TierOneType(TEST + TEST));

        // method
        final boolean assertResult = getFabutObjectAssert().assertMap("", new FabutReportBuilder(), expected, actual,
                new ArrayList<>(), new NodesList(), false);

        // assert
        assertFalse(assertResult);
    }

    @Test
    public void testAssertMapFalseDifferentKeySet() {
        // setup
        final Map<String, TierOneType> expected = new HashMap<>();
        expected.put("first", new TierOneType(TEST));
        expected.put("third", new TierOneType(TEST));

        final Map<String, TierOneType> actual = new HashMap<>();
        actual.put("first", new TierOneType(TEST));
        actual.put("second", new TierOneType(TEST));

        // method
        final boolean assertResult = getFabutObjectAssert().assertMap("", new FabutReportBuilder(), expected, actual,
                new ArrayList<>(), new NodesList(), false);

        // assert
        assertFalse(assertResult);
    }

    @Test
    public void testAssertExcessExpectedNoExcess() {
        // setup
        final Map<String, TierOneType> expected = new HashMap<>();
        expected.put("first", new TierOneType());
        final TreeSet<String> expectedKeys = new TreeSet<>();
        expectedKeys.add("first");
        final TreeSet<String> actualKeys = new TreeSet<>();
        actualKeys.add("first");

        // method
        final boolean assertResult = getFabutObjectAssert().assertExcessExpected("", new FabutReportBuilder(),
                expected, expectedKeys, actualKeys);

        // assert
        assertTrue(assertResult);
    }

    @Test
    public void testAssertExcessExpectedExcess() {
        // setup
        final Map<String, TierOneType> expected = new HashMap<>();
        expected.put("first", new TierOneType());
        final TreeSet<String> expectedKeys = new TreeSet<>();
        expectedKeys.add("first");
        final TreeSet<String> actualKeys = new TreeSet<>();
        final FabutReportBuilder report = new FabutReportBuilder();
        // method
        final boolean assertResult = getFabutObjectAssert().assertExcessExpected("", report, expected, expectedKeys,
                actualKeys);

        // assert
        assertFalse(assertResult);
    }

    @Test
    public void testAssertExcessActualNoExcess() {
        // setup
        final Map<String, TierOneType> actual = new HashMap<>();
        actual.put("first", new TierOneType());
        final TreeSet<String> expectedKeys = new TreeSet<>();
        expectedKeys.add("first");
        final TreeSet<String> actualKeys = new TreeSet<>();
        actualKeys.add("first");

        // method
        final boolean assertResult = getFabutObjectAssert().assertExcessActual("", new FabutReportBuilder(), actual,
                expectedKeys, actualKeys);

        // assert
        assertTrue(assertResult);
    }

    @Test
    public void testAssertExcessActualExcess() {
        // setup
        final Map<String, TierOneType> actual = new HashMap<>();
        actual.put("first", new TierOneType());
        final TreeSet<String> expectedKeys = new TreeSet<>();
        final TreeSet<String> actualKeys = new TreeSet<>();
        actualKeys.add("first");
        final FabutReportBuilder report = new FabutReportBuilder();
        // method
        final boolean assertResult = getFabutObjectAssert().assertExcessActual("", report, actual, expectedKeys,
                actualKeys);

        // assert
        assertFalse(assertResult);
    }
}
