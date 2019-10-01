package cloud.alchemy.fabut;

import cloud.alchemy.fabut.enums.ReferenceCheckType;
import cloud.alchemy.fabut.graph.NodesList;
import cloud.alchemy.fabut.model.*;
import cloud.alchemy.fabut.model.test.Address;
import cloud.alchemy.fabut.model.test.Faculty;
import cloud.alchemy.fabut.model.test.Student;
import cloud.alchemy.fabut.model.test.Teacher;
import cloud.alchemy.fabut.property.*;
import org.junit.Test;

import java.util.*;

public class FabutObjectAssertTest extends Fabut {
    private static final String EMPTY_STRING = "";
    private static final String TEST = "test";

    public FabutObjectAssertTest() {
        super();
        complexTypes.add(A.class);
        complexTypes.add(B.class);
        complexTypes.add(C.class);
        complexTypes.add(TierOneType.class);
        complexTypes.add(TierTwoType.class);
        complexTypes.add(TierThreeType.class);
        complexTypes.add(TierFourType.class);
        complexTypes.add(TierFiveType.class);
        complexTypes.add(TierSixType.class);
        complexTypes.add(NoGetMethodsType.class);
        complexTypes.add(IgnoredMethodsType.class);
        complexTypes.add(TierTwoTypeWithIgnoreProperty.class);
        complexTypes.add(TierTwoTypeWithListProperty.class);
        complexTypes.add(TierTwoTypeWithPrimitiveProperty.class);
        complexTypes.add(DoubleLink.class);
        complexTypes.add(Start.class);
        complexTypes.add(Address.class);
        complexTypes.add(Faculty.class);
        complexTypes.add(Student.class);
        complexTypes.add(Teacher.class);

        ignoredTypes.add(IgnoredType.class);
    }

    @Test
    public void testAssertObjectIgnoreType() {
        // setup
        final IgnoredType ignoredType = new IgnoredType();
        final List<ISingleProperty> properties = new LinkedList<>();

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, ignoredType, properties);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectNoGetMethodsType() {
        // setup
        final NoGetMethodsType noGetMethodsType = new NoGetMethodsType(TEST);
        final Property<String> jokerProperty = value(NoGetMethodsType.PROPERTY, TEST + TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(jokerProperty);

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, noGetMethodsType, properties);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectNoProperty() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, tierOneType, properties);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectNotNullPropertyActuallyNull() {
        // setup
        final TierOneType tierOneType = new TierOneType(null);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(notNull(TierOneType.PROPERTY));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, tierOneType,
                properties);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectNotNullProperty() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(notNull(TierOneType.PROPERTY));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report,
                tierOneType, properties);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());

    }

    @Test
    public void testAssertObjectNullPropertyActuallyNotNull() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(isNull(TierOneType.PROPERTY));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, tierOneType, properties);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectNullProperty() {
        // setup
        final TierOneType tierOneType = new TierOneType(null);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(isNull(TierOneType.PROPERTY));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report,
                tierOneType, properties);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectIgnoreProperty() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(ignored(TierOneType.PROPERTY));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, tierOneType, properties);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectChangedPropertyExpectedNullActualNull() {
        // setup
        final TierOneType tierOneType = new TierOneType(null);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(value(TierOneType.PROPERTY, null));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, tierOneType, properties);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectChangedPropertyActualNull() {
        // setup
        final TierOneType tierOneType = new TierOneType(null);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(value(TierOneType.PROPERTY, TEST));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, tierOneType, properties);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectChangedPropertyExpectedNull() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(value(TierOneType.PROPERTY, null));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, tierOneType,
                properties);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectChangedPropertyEqual() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(value(TierOneType.PROPERTY, TEST));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, tierOneType, properties);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectChangedPropertyNotEqual() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(value(TierOneType.PROPERTY, TEST + TEST));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, tierOneType, properties);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectChangedPropertyWithIgnoredType() {
        // setup
        final TierTwoTypeWithIgnoreProperty tierTwoTypeWithIgnoreProperty = new TierTwoTypeWithIgnoreProperty(new IgnoredType());

        final Property<IgnoredType> jokerProperty = value(TierTwoTypeWithIgnoreProperty.IGNORED_TYPE, new IgnoredType());

        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(jokerProperty);

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, tierTwoTypeWithIgnoreProperty, properties);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    /**
     * Test if assertObject throws {@link AssertionError} when size of actual list is not equal
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
        properties.add(value(TierTwoTypeWithListProperty.PROPERTY, jokerList));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report,
                tierTwoTypeWithListProperty, properties);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    /**
     * Test for assertObject with {@link Property} when all actual list members are primitive
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
        properties.add(value(TierTwoTypeWithListProperty.PROPERTY, expectedList));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report,
                tierTwoTypeWithListProperty, properties);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    /**
     * Test for assertObject with {@link Property} when actual list members are primitive types
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
        properties.add(value(TierTwoTypeWithListProperty.PROPERTY, expectedList));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report,
                tierTwoTypeWithListProperty, properties);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    /**
     * Test for assertObjects for two {@link TierTwoType} objects with equal values.
     */
    @Test
    public void testAssertObjectsTierTwoObjectsWithEqualValues() {
        // setup
        final TierTwoType actual = new TierTwoType(new TierOneType(TEST));
        final TierTwoType expected = new TierTwoType(new TierOneType(TEST));

        // method
        final FabutReport report = new FabutReport();
        assertObjects(report, expected, actual,
                new LinkedList<>());
    }

    /**
     * Test for assertObjects for two {@link TierTwoType} objects with equal values.
     */
    @Test
    public void testAssertObjectsTierTwoObjectsWithNotEqualValues() {
        // setup
        final TierTwoType actual = new TierTwoType(new TierOneType(TEST));
        final TierTwoType expected = new TierTwoType(new TierOneType(TEST + TEST));

        // method
        final FabutReport report = new FabutReport();
        assertObjects(report, expected, actual, new LinkedList<>());

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    /**
     * Test for assertObjects for two {@link List}s of {@link TierOneType} with equal values.
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
        final FabutReport report = new FabutReport();
        assertObjects(report, expected, actual,
                new LinkedList<>());

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    /**
     * Test for assertObjects for two {@link List}s of {@link TierOneType} with unequal values.
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
        final FabutReport report = new FabutReport();
        assertObjects(report, expected, actual,
                new LinkedList<>());

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertObjectsTierTwoTypeWithPrimitivePropertyWithIgnoreProperty() {
        // setup
        final TierTwoTypeWithPrimitiveProperty actual = new TierTwoTypeWithPrimitiveProperty(new TierOneType(TEST), TEST);
        final TierTwoTypeWithPrimitiveProperty expected = new TierTwoTypeWithPrimitiveProperty(new TierOneType(TEST + TEST), TEST);

        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(ignored(TierTwoType.PROPERTY.chain(TierOneType.PROPERTY)));

        // method
        final FabutReport report = new FabutReport();
        assertObjects(report, expected, actual, properties);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }


    @Test
    public void testAssertObjectsSameInstances() {
        // setup
        final TierOneType tierOneType = new TierOneType();

        // method
        final FabutReport report = new FabutReport();
        assertObjects(report, tierOneType, tierOneType,
                new LinkedList<>());
    }


    @Test
    public void testAssertObjectsTierSixTypeDepthSix() {
        // setup
        final TierSixType actual = new TierSixType(new TierFiveType(new TierFourType(new TierThreeType(new TierTwoType(
                new TierOneType(TEST))))));
        final TierSixType expected = new TierSixType(new TierFiveType(new TierFourType(new TierThreeType(
                new TierTwoType(new TierOneType(TEST + TEST))))));

        // method
        final FabutReport report = new FabutReport();
        assertObjects(report, expected, actual,
                new LinkedList<>());

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }


    @Test
    public void testAssertObjectsVarargsExpected() {
        // setup
        final List<TierOneType> expected = new ArrayList<>();
        expected.add(new TierOneType(TEST));
        final TierOneType actual = new TierOneType(TEST);

        // method
        final FabutReport report = new FabutReport();
        assertObjects(report, expected, actual,
                new LinkedList<>());

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    /**
     * Test for assertObjects with {@link TierOneType} when varargs of properties is called.
     */
    @Test
    public void testAssertObjectVarargsProperties() {
        // setup
        final TierOneType actual = new TierOneType(TEST);

        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(value(TierOneType.PROPERTY, TEST));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report,
                actual, properties);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }


    @Test
    public void testAssertPairrTrivialGraphEqual() {
        // setup
        final A actual = new A(null);
        final A expected = new A(null);
        final NodesList nodesList = new NodesList();

        // method
        final FabutReport report = new FabutReport();
        assertPair(EMPTY_STRING, report, expected, actual, new ArrayList<>(), nodesList);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }


    @Test
    public void testAssertPairNodePairInList() {
        // setup
        final Object actual = new Object();
        final Object expected = new Object();
        final NodesList nodesList = new NodesList();
        nodesList.addPair(expected, actual);

        // method
        final FabutReport report = new FabutReport();
        assertPair(EMPTY_STRING, report, expected, actual, new ArrayList<>(), nodesList);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertPairCyclicGraphEqual() {
        // setup
        final NodesList nodesList = new NodesList();

        final A actual = new A(null);
        actual.setB(new B(new C(actual)));

        final A expected = new A(null);
        expected.setB(new B(new C(expected)));

        // method
        final FabutReport report = new FabutReport();
        assertPair(EMPTY_STRING, report, expected, actual, new ArrayList<>(), nodesList);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }


    @Test
    public void testAssertChangedPropertyBothNulls() {
        // method
        final FabutReport report = new FabutReport();
        assertPair(EMPTY_STRING, report, null, null, new ArrayList<>(), new NodesList());

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }


    @Test
    public void testAssertChangedPropertyComplexType() {
        // setup
        final TierOneType actual = new TierOneType(TEST);
        final TierOneType expected = new TierOneType(TEST);

        // method
        final FabutReport report = new FabutReport();
        assertPair(EMPTY_STRING, report, expected, actual, new ArrayList<>(), new NodesList());

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertChangedPropertyIgnoredType() {
        // setup
        final IgnoredType actual = new IgnoredType();
        final IgnoredType expected = new IgnoredType();

        // method
        final FabutReport report = new FabutReport();
        assertPair(EMPTY_STRING, report, expected, actual, new ArrayList<>(), new NodesList());

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertChangedPropertyPrimitiveTypeTrue() {
        // setup

        // method
        final FabutReport report = new FabutReport();
        assertPair(EMPTY_STRING, report,
                TEST, TEST,
                new ArrayList<>(), new NodesList());

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertChangedPropertyPrimitiveTypeFalse() {
        // setup
        final String expected = TEST + TEST;

        // method
        final FabutReport report = new FabutReport();
        assertPair(EMPTY_STRING, report,
                expected, TEST,
                new ArrayList<>(), new NodesList());

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

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
        final FabutReport report = new FabutReport();
        assertPair(EMPTY_STRING, report,
                expected, actual, new LinkedList<>(),
                new NodesList());

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertPropertiesNotNullPropertyTrue() {
        // method
        final FabutReport report = new FabutReport();
        assertProperty(EMPTY_STRING, report, notNull(TierOneType.PROPERTY), new TierOneType(TEST), "", new ArrayList<>(), null);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertPropertiesNotNullPropertyFalse() {
        // method
        final FabutReport report = new FabutReport();
        assertProperty(EMPTY_STRING, report,
                notNull(TierOneType.PROPERTY), null, "", new ArrayList<>(), null);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertPropertiesNullPropertyTrue() {
        // method
        final FabutReport report = new FabutReport();
        assertProperty(EMPTY_STRING, report,
                isNull(TierOneType.PROPERTY), null, "", new ArrayList<>(), null);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertPropertiesNullPropertyFalse() {
        // method
        final FabutReport report = new FabutReport();
        assertProperty(EMPTY_STRING, report,
                isNull(TierOneType.PROPERTY), new TierOneType(TEST), "", new ArrayList<>(), null);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertPropertiesIgnoreProperty() {
        // method
        final FabutReport report = new FabutReport();
        assertProperty(EMPTY_STRING, report,
                ignored(TierOneType.PROPERTY), new TierOneType(TEST), "", new ArrayList<>(), null);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertListNotEqualSize() {
        // setup
        final List<String> actual = new ArrayList<>();
        actual.add(TEST);
        actual.add(TEST);
        final List<String> expected = new ArrayList<>();
        expected.add(TEST);

        // method
        final FabutReport report = new FabutReport();
        assertList(EMPTY_STRING, report, expected, actual, new ArrayList<>(), new NodesList());

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

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
        final FabutReport report = new FabutReport();
        assertList(EMPTY_STRING, report, expected, actual, new ArrayList<>(), new NodesList());

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

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
        final FabutReport report = new FabutReport();
        assertList(EMPTY_STRING, report, expected, actual, new ArrayList<>(), new NodesList());

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testPreAssertObjectWithPropertiesEqual() {
        // setup
        final List<ISingleProperty> properties = new ArrayList<>();
        properties.add(value(TierOneType.PROPERTY, "ninja"));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, new TierOneType("ninja"),
                properties);

    }

    @Test
    public void testPreAssertObjectWithPropertiesNotEqual() {
        // setup
        final List<ISingleProperty> properties = new ArrayList<>();
        properties.add(value(TierOneType.PROPERTY, TEST));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report,
                new TierOneType(TEST + TEST), properties);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testPreAssertObjectWithPropertiesMethodReturnsNull() {
        // setup
        final List<ISingleProperty> properties = new ArrayList<>();

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report,
                new TierOneType(null), properties);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testPreAssertObjectWithPropertiesBadProperties() {
        // setup
        final List<ISingleProperty> properties = new ArrayList<>();
        properties.add(value(new PropertyPath<>(TEST), TEST));

        // method
        final FabutReport report = new FabutReport();
        assertObjectWithProperties(report, new TierOneType(TEST), properties);

        // assertTrue
        assertFalse(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testRemoveParentQualificationForProperty() {
        // setup
        final List<ISingleProperty> properties = new ArrayList<>();

        properties.add(notNull(new PropertyPath("parent.id")));
        properties.add(notNull(new PropertyPath("parent.name")));
        properties.add(notNull(new PropertyPath("parent.lastname")));

        // method
        final List<ISingleProperty> unqualifiedProperties = removeParentQualification("parent", properties);

        // assert
        assertEquals("id", unqualifiedProperties.get(0).getPath());
        assertEquals("name", unqualifiedProperties.get(1).getPath());
        assertEquals("lastname", unqualifiedProperties.get(2).getPath());
    }

    @Test
    public void testGeneratePropertyFromListOfExcluded() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new ArrayList<>();
        properties.add(notNull(TierOneType.PROPERTY));
        final int numProperties = properties.size();

        // method
        final ISingleProperty property = obtainProperty(tierOneType, TierOneType.PROPERTY.getPath(),
                properties);

        // assert
        assertEquals(TierOneType.PROPERTY.getPath(), property.getPath());
        assertEquals(numProperties - 1, properties.size());
    }

    @Test
    public void testGeneratePropertyCreateNew() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);
        final List<ISingleProperty> properties = new ArrayList<>();
        properties.add(notNull(new PropertyPath<>(TEST)));
        final int numProperties = properties.size();

        // method
        final Property property = (Property) obtainProperty(tierOneType, TierOneType.PROPERTY.getPath(),
                properties);

        // assert
        assertEquals(TierOneType.PROPERTY.getPath(), property.getPath());
        assertEquals(tierOneType, property.getValue());
        assertEquals(numProperties, properties.size());

    }


    @Test
    public void testPopPropertyEqualPath() {
        // setup
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(isNull(new PropertyPath(TEST)));

        // method
        final ISingleProperty property = getPropertyFromList(TEST, properties);

        // assert
        assertEquals(TEST, property.getPath());
        assertEquals(0, properties.size());
    }

    @Test
    public void testPopPropertyNoProperties() {
        // setup
        final List<ISingleProperty> properties = new LinkedList<>();

        // method
        final ISingleProperty property = getPropertyFromList(TEST, properties);

        // assert
        assertNull(property);
    }

    @Test
    public void testPopPropertyNotEqualPath() {
        // setup
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(isNull(new PropertyPath(TEST)));

        // method
        final ISingleProperty property = getPropertyFromList(TEST + TEST, properties);

        // assert
        assertNull(property);
    }

    @Test
    public void testCheckForNullsTrueNull() {
        // method
        final ReferenceCheckType report = checkByReference(new FabutReport(), null, null, TEST);

        // assert
        assertEquals(ReferenceCheckType.EQUAL_REFERENCE, report);
    }

    @Test
    public void testCheckForNullsTrueNotNull() {
        // setup
        final TierOneType expected = new TierOneType();

        // method
        final ReferenceCheckType re = checkByReference(new FabutReport(), expected, expected, TEST);

        // assert
        assertEquals(ReferenceCheckType.EQUAL_REFERENCE, re);
    }

    @Test
    public void testCheckForNullsFalse() {
        // method
        final ReferenceCheckType report1 = checkByReference(new FabutReport(), new Object(), null, TEST);
        final ReferenceCheckType report2 = checkByReference(new FabutReport(), null, new Object(), TEST);

        // assert
        assertEquals(ReferenceCheckType.EXCLUSIVE_NULL, report1);
        assertEquals(ReferenceCheckType.EXCLUSIVE_NULL, report2);
    }

    @Test
    public void testCheckForNullsNull() {
        // method

        final ReferenceCheckType referenceCheckType = checkByReference(new FabutReport(), new Object(), new Object(), TEST);

        // assert
        assertEquals(ReferenceCheckType.NOT_NULL_PAIR, referenceCheckType);
    }

    /**
     * Test for extractProperties when parameters are of types {@link ISingleProperty} and {@link IMultiProperties}.
     */
    @Test
    public void testExtractPropertiesMixed() {
        // method
        final List<ISingleProperty> properties = extractProperties(
                value(EntityTierOneType.PROPERTY, ""),
                notNull(EntityTierOneType.ID, EntityTierOneType.PROPERTY));

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
        final IProperty[] propArray = new ISingleProperty[]{value(EntityTierOneType.PROPERTY, ""),
                value(EntityTierOneType.ID, 0)};

        // method
        final List<ISingleProperty> properties = extractProperties(propArray);

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
        final IMultiProperties notNullMultiProp = notNull(EntityTierOneType.PROPERTY, EntityTierOneType.ID);
        final IMultiProperties ignoredMultiProp = ignored(EntityTierOneType.PROPERTY, EntityTierOneType.ID);
        final IProperty[] multiPropArray = new IMultiProperties[]{notNullMultiProp, ignoredMultiProp};

        // method
        final List<ISingleProperty> properties = extractProperties(multiPropArray);

        // assert
        assertEquals(notNullMultiProp.getProperties().size() + ignoredMultiProp.getProperties().size(),
                properties.size());
        assertEquals(notNullMultiProp.getProperties().get(0).getPath(), properties.get(0).getPath());
        assertEquals(notNullMultiProp.getProperties().get(1).getPath(), properties.get(1).getPath());
        assertEquals(ignoredMultiProp.getProperties().get(0).getPath(), properties.get(2).getPath());
        assertEquals(ignoredMultiProp.getProperties().get(1).getPath(), properties.get(3).getPath());
    }

    @Test
    public void testAssertObjectsMultiProperty() {
        // setup
        final TierTwoTypeWithPrimitiveProperty actual = new TierTwoTypeWithPrimitiveProperty(new TierOneType(TEST),
                TEST);
        final TierTwoTypeWithPrimitiveProperty expected = new TierTwoTypeWithPrimitiveProperty(new TierOneType(TEST
                + TEST), TEST + TEST);

        // method
        assertObjects(new FabutReport(), expected, actual, extractProperties(ignored(TierTwoType.PROPERTY.chain(TierOneType.PROPERTY).chain(TierTwoTypeWithPrimitiveProperty.PROPERTY2))));
    }

    @Test
    public void testTakeSnapshot() {
        // setup
        final TierOneType tierOneType = new TierOneType(TEST);

        // method
        takeSnapshot(new FabutReport(), tierOneType);

        // assert
        assertEquals(parameterSnapshot.size(), 1);

        final List<ISingleProperty> properties = new LinkedList<>();


        assertObjects(new FabutReport(), new TierOneType(TEST),
                parameterSnapshot.get(0).getExpected(), properties);

        assertObjects(new FabutReport(), new TierOneType(TEST),
                parameterSnapshot.get(0).getActual(), properties);

        final FabutReport report = new FabutReport();
        assertParameterSnapshot(report);
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testExtractPropertiesWithMatchingParent() {
        // setup
        final String parent = "parent";
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(value(new PropertyPath<>("parent.name"), "name"));
        properties.add(value(new PropertyPath<>("parents"), "parents"));
        properties.add(value(new PropertyPath<>("parent.lastName"), "lastName"));
        properties.add(value(new PropertyPath<>("parent.address.city"), "city"));

        // method
        final List<ISingleProperty> extracted = extractPropertiesWithMatchingParent(parent, properties);

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
        properties.add(value(new PropertyPath<>("parent.name"), "name"));

        // method
        final boolean hasInnerProperties = hasInnerProperties(parent, properties);

        // assert
        assertTrue(hasInnerProperties);
    }

    @Test
    public void testHasInnerPropertyFalse() {
        // setup
        final String parent = "parent";
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(value(new PropertyPath<>("parents"), "name"));

        // method
        final boolean hasInnerProperties = hasInnerProperties(parent, properties);

        // assert
        assertFalse(hasInnerProperties);
    }

    @Test
    public void testAssertInnerProperty() {
        // setup
        final TierOneType actual = new TierOneType(TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(value(new PropertyPath<>("property.property"), TEST));
        final FabutReport report = new FabutReport();
        // assert
        assertInnerProperty(report, actual, properties, "property");
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertInnerObject() {
        // setup
        final TierOneType actual = new TierOneType(TEST);
        final TierOneType expected = new TierOneType(TEST + TEST);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(value(new PropertyPath<>("property.property"), TEST + TEST));

        // assert
        final FabutReport report = new FabutReport();
        assertInnerObject(report, actual, expected, properties, "property");

        assertTrue(report.getMessage(), report.isSuccess());
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
        final FabutReport report = new FabutReport();
        assertMap("", report, expected, actual, new ArrayList<>(), new NodesList());

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
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
        final FabutReport report = new FabutReport();
        assertMap("", report, expected, actual, new ArrayList<>(), new NodesList());

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
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
        final FabutReport report = new FabutReport();
        assertMap("", report, expected, actual, new ArrayList<>(), new NodesList());

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
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
        final FabutReport report = new FabutReport();
        assertExcessExpected("", report, expected, expectedKeys, actualKeys);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertExcessExpectedExcess() {
        // setup
        final Map<String, TierOneType> expected = new HashMap<>();
        expected.put("first", new TierOneType());
        final TreeSet<String> expectedKeys = new TreeSet<>();
        expectedKeys.add("first");
        final TreeSet<String> actualKeys = new TreeSet<>();
        final FabutReport report = new FabutReport();
        // method
        assertExcessExpected("", report, expected, expectedKeys, actualKeys);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
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
        final FabutReport report = new FabutReport();
        assertExcessActual("", report, actual, expectedKeys, actualKeys);

        // assert
        assertTrue(report.getMessage(), report.isSuccess());
    }

    @Test
    public void testAssertExcessActualExcess() {
        // setup
        final Map<String, TierOneType> actual = new HashMap<>();
        actual.put("first", new TierOneType());
        final TreeSet<String> expectedKeys = new TreeSet<>();
        final TreeSet<String> actualKeys = new TreeSet<>();
        actualKeys.add("first");
        final FabutReport report = new FabutReport();
        // method
        assertExcessActual("", report, actual, expectedKeys, actualKeys);

        // assert
        assertFalse(report.getMessage(), report.isSuccess());
    }
}
