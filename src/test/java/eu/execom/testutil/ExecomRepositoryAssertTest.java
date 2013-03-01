package eu.execom.testutil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;

import eu.execom.testutil.enums.ObjectType;
import eu.execom.testutil.graph.NodesList;
import eu.execom.testutil.model.A;
import eu.execom.testutil.model.B;
import eu.execom.testutil.model.C;
import eu.execom.testutil.model.DoubleLink;
import eu.execom.testutil.model.EntityTierOneType;
import eu.execom.testutil.model.EntityTierThreeType;
import eu.execom.testutil.model.EntityTierTwoType;
import eu.execom.testutil.model.IgnoredMethodsType;
import eu.execom.testutil.model.IgnoredType;
import eu.execom.testutil.model.NoDefaultConstructorType;
import eu.execom.testutil.model.NoGetMethodsType;
import eu.execom.testutil.model.Start;
import eu.execom.testutil.model.TierFiveType;
import eu.execom.testutil.model.TierFourType;
import eu.execom.testutil.model.TierOneType;
import eu.execom.testutil.model.TierSixType;
import eu.execom.testutil.model.TierThreeType;
import eu.execom.testutil.model.TierTwoType;
import eu.execom.testutil.model.TierTwoTypeWithIgnoreProperty;
import eu.execom.testutil.model.TierTwoTypeWithListProperty;
import eu.execom.testutil.model.TierTwoTypeWithPrimitiveProperty;
import eu.execom.testutil.model.UnknownEntityType;
import eu.execom.testutil.model.UnknownType;
import eu.execom.testutil.report.AssertReportBuilder;
import eu.execom.testutil.util.ReflectionUtil;

/**
 * Tests for {@link AbstractExecomRepositoryAssert}.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 */
@SuppressWarnings("unchecked")
public class ExecomRepositoryAssertTest extends AbstractExecomRepositoryAssertTest {

    private static final String TEST = "test";
    private static final String PROPERTY = "property";

    @Before
    public void before() {
        final List<Class<?>> entityTypes = new LinkedList<Class<?>>();
        entityTypes.add(EntityTierOneType.class);
        entityTypes.add(EntityTierTwoType.class);
        setEntityTypes(entityTypes);

        final List<Class<?>> complexTypes = new LinkedList<Class<?>>();
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
        setComplexTypes(complexTypes);

        final List<Class<?>> ignoredTypes = new LinkedList<Class<?>>();
        ignoredTypes.add(IgnoredType.class);
        setIgnoredTypes(ignoredTypes);

        initDbSnapshot();
        initParametersSnapshot();

        getTypes().put(ObjectType.ENTITY_TYPE, entityTypes);
        getTypes().put(ObjectType.COMPLEX_TYPE, complexTypes);
        getTypes().put(ObjectType.IGNORED_TYPE, ignoredTypes);
    }

    /**
     * Test for assertDbState of {@link AbstractExecomRepositoryAssert} when before snapshot matches after snapshot.
     */
    @Test
    public void testAssertDbStateTrue() {
        // setup
        final List<EntityTierOneType> beforeList1 = new ArrayList<EntityTierOneType>();
        beforeList1.add(new EntityTierOneType(TEST + TEST, 1));
        setList1(beforeList1);

        final List<EntityTierTwoType> beforeList2 = new ArrayList<EntityTierTwoType>();
        beforeList2.add(new EntityTierTwoType(PROPERTY + PROPERTY + PROPERTY, 4, new EntityTierOneType(TEST + TEST
                + TEST + TEST, 4)));
        setList2(beforeList2);

        takeSnapshot();

        final List<EntityTierOneType> afterList1 = new ArrayList<EntityTierOneType>();
        afterList1.add(new EntityTierOneType(TEST + TEST, 1));
        setList1(afterList1);

        final List<EntityTierTwoType> afterist2 = new ArrayList<EntityTierTwoType>();
        afterist2.add(new EntityTierTwoType(PROPERTY + PROPERTY + PROPERTY, 4, new EntityTierOneType(TEST + TEST + TEST
                + TEST, 4)));
        setList2(afterist2);

        // method
        assertDbState();
    }

    /**
     * Test for assertDbState of {@link AbstractExecomRepositoryAssert} when before snapshot doesn't match after
     * snapshot.
     */
    @Test(expected = AssertionError.class)
    // TODO(nolah) test is probably bad now that test util isnt extended anymore
    public void testAssertDbStateFalse() {
        // setup

        final List<EntityTierOneType> beforeList1 = new ArrayList<EntityTierOneType>();
        beforeList1.add(new EntityTierOneType(TEST, 1));
        setList1(beforeList1);

        final List<EntityTierTwoType> beforeList2 = new ArrayList<EntityTierTwoType>();
        beforeList2.add(new EntityTierTwoType(PROPERTY, 4, new EntityTierOneType(TEST, 7)));
        setList2(beforeList2);

        takeSnapshot();

        final List<EntityTierOneType> afterList1 = new ArrayList<EntityTierOneType>();
        afterList1.add(new EntityTierOneType(TEST + TEST, 1));
        setList1(afterList1);

        final List<EntityTierTwoType> afterList2 = new ArrayList<EntityTierTwoType>();
        afterList2.add(new EntityTierTwoType(PROPERTY + PROPERTY, 4, new EntityTierOneType(TEST + TEST, 7)));
        setList2(afterList2);

        // method
        assertDbState();
    }

    /**
     * Test for assertByEntity of {@link AbstractExecomRepositoryAssert} when before entities match after entities.
     */
    @Test
    public void testAssertByEntityTrue() {
        // setup
        initDbSnapshot();
        final Map<Integer, CopyAssert<EntityTierOneType>> beforeEntities = new HashMap<Integer, CopyAssert<EntityTierOneType>>();
        beforeEntities.put(1, new CopyAssert<EntityTierOneType>(new EntityTierOneType(TEST, 1)));
        final List<EntityTierOneType> afterEntities = new ArrayList<EntityTierOneType>();
        afterEntities.add(new EntityTierOneType(TEST, 1));

        // method
        final boolean assertValue = assertByEntity(beforeEntities, afterEntities, new AssertReportBuilder());

        // assert
        assertTrue(assertValue);

    }

    /**
     * Test for assertByEntity of {@link AbstractExecomRepositoryAssert} when before entities don't match after
     * entities.
     */
    @Test
    public void testAssertByEntityFalse() {
        // setup
        initDbSnapshot();
        final Map<Integer, CopyAssert<EntityTierOneType>> beforeEntities = new HashMap<Integer, CopyAssert<EntityTierOneType>>();
        beforeEntities.put(1, new CopyAssert<EntityTierOneType>(new EntityTierOneType(TEST, 1)));
        final List<EntityTierOneType> afterEntities = new ArrayList<EntityTierOneType>();
        afterEntities.add(new EntityTierOneType(TEST + TEST, 1));

        // method
        final boolean assertValue = assertByEntity(beforeEntities, afterEntities, new AssertReportBuilder());

        // assert
        assertFalse(assertValue);

    }

    /**
     * Test for validateEntry of {@link AbstractExecomRepositoryAssert} when specified entry is already asserted.
     */
    @Test
    public void testValidateEntrylreadyAsserted() {
        // setup
        final List<Integer> ids = new ArrayList<Integer>();
        final Map<Integer, CopyAssert<EntityTierOneType>> beforeEntities = new HashMap<Integer, CopyAssert<EntityTierOneType>>();
        final CopyAssert<EntityTierOneType> copyAssert = new CopyAssert<EntityTierOneType>(new EntityTierOneType(TEST,
                1));
        copyAssert.setAsserted(true);
        beforeEntities.put(1, copyAssert);
        final List<EntityTierOneType> afterEntities = new ArrayList<EntityTierOneType>();

        boolean assertValue = false;
        for (final Entry<Integer, CopyAssert<EntityTierOneType>> beforeEntry : beforeEntities.entrySet()) {
            // method
            assertValue = validateEntry(ids, beforeEntry, afterEntities, new AssertReportBuilder());
        }

        // assert
        assertTrue(assertValue);

    }

    /**
     * Test for AssertClasess of {@link AbstractExecomRepositoryAssert} when specified element has no match in after
     * entities.
     */
    @Test
    public void testAssertClasessNoAfterElem() {
        // setup
        final List<Integer> ids = new ArrayList<Integer>();
        final Map<Integer, CopyAssert<EntityTierOneType>> beforeEntities = new HashMap<Integer, CopyAssert<EntityTierOneType>>();
        final CopyAssert<EntityTierOneType> copyAssert = new CopyAssert<EntityTierOneType>(new EntityTierOneType(TEST,
                1));
        beforeEntities.put(1, copyAssert);
        final List<EntityTierOneType> afterEntities = new ArrayList<EntityTierOneType>();

        boolean assertValue = false;
        for (final Entry<Integer, CopyAssert<EntityTierOneType>> beforeEntry : beforeEntities.entrySet()) {
            // method
            assertValue = validateEntry(ids, beforeEntry, afterEntities, new AssertReportBuilder());
        }

        // assert
        assertFalse(assertValue);
    }

    /**
     * Test for validateEntry of {@link AbstractExecomRepositoryAssert} when specified element has no valid match in
     * before entities.
     */
    @Test
    public void testValidateEntryNoValidCopy() {
        // setup
        final List<Integer> ids = new ArrayList<Integer>();
        final Map<Integer, CopyAssert<EntityTierOneType>> beforeEntities = new HashMap<Integer, CopyAssert<EntityTierOneType>>();
        final CopyAssert<EntityTierOneType> copyAssert = new CopyAssert<EntityTierOneType>(null);
        beforeEntities.put(1, copyAssert);
        final List<EntityTierOneType> afterEntities = new ArrayList<EntityTierOneType>();

        boolean assertValue = false;
        for (final Entry<Integer, CopyAssert<EntityTierOneType>> beforeEntry : beforeEntities.entrySet()) {
            // method
            assertValue = validateEntry(ids, beforeEntry, afterEntities, new AssertReportBuilder());
        }

        // assert
        assertFalse(assertValue);
    }

    /**
     * Test for assertEntities of {@link AbstractExecomRepositoryAssert} when two entities are equal.
     */
    @Test
    public void testAssertEntitiesTrue() {
        // setup
        final EntityTierOneType beforeEntity = new EntityTierOneType(TEST, 1);
        final EntityTierOneType afterEntity = new EntityTierOneType(TEST, 1);

        // method
        final boolean assertValue = assertEntities(beforeEntity, afterEntity, new AssertReportBuilder());

        // assert
        assertTrue(assertValue);
    }

    /**
     * Test for assertEntities of {@link AbstractExecomRepositoryAssert} when two entities are nor equal.
     */
    @Test
    public void testAssertEntitiesFalse() {
        // setup
        final EntityTierOneType beforeEntity = new EntityTierOneType(TEST, new Integer(1));
        final EntityTierOneType afterEntity = new EntityTierOneType(TEST + TEST, new Integer(1));

        // method
        final boolean assertValue = assertEntities(beforeEntity, afterEntity, new AssertReportBuilder());

        // assert
        assertFalse(assertValue);
    }

    /**
     * Test for popEntityFromList of {@link AbstractExecomRepositoryAssert} when there is entity in list with specified
     * id.
     */
    @Test
    public void testPopEntityFromListNotNull() {
        // setup
        final Integer id = new Integer(1);
        final List<EntityTierOneType> afterEntities = new ArrayList<EntityTierOneType>();
        afterEntities.add(new EntityTierOneType(TEST, id));

        // method
        final EntityTierOneType afterEntity = popEntityFromList(id, afterEntities);

        // assert
        assertNotNull(afterEntity);
        assertEquals(0, afterEntities.size());

    }

    /**
     * Test for popEntityFromList of {@link AbstractExecomRepositoryAssert} when there is no entity in list with
     * specified id.
     */
    @Test
    public void testPopEntityFromListNull() {
        // setup
        final Integer id = new Integer(1);
        final List<EntityTierOneType> afterEntities = new ArrayList<EntityTierOneType>();
        afterEntities.add(new EntityTierOneType(TEST, id));

        // method
        final EntityTierOneType afterEntity = popEntityFromList(id + id, afterEntities);

        // assert
        assertNull(afterEntity);
        assertEquals(1, afterEntities.size());
    }

    /**
     * Test for removeAfterEntities of {@link AbstractExecomRepositoryAssert} when there is id for entity specified in
     * ids list.
     */
    @Test
    public void testRemoveAfterEntitiesContainsId() {
        // setup
        final List<Integer> ids = new ArrayList<Integer>();
        ids.add(1);

        final List<EntityTierOneType> afterEntities = new ArrayList<EntityTierOneType>();
        afterEntities.add(new EntityTierOneType(TEST, 1));
        afterEntities.add(new EntityTierOneType(TEST, 2));
        afterEntities.add(new EntityTierOneType(TEST, 3));

        // method
        removeAfterEntitites(ids, afterEntities);

        // assert
        assertEquals(2, afterEntities.size());
        assertEquals(new Integer(2), afterEntities.get(0).getId());
        assertEquals(new Integer(3), afterEntities.get(1).getId());
    }

    /**
     * Test for removeAfterEntities of {@link AbstractExecomRepositoryAssert} when there isnt id for entity specified in
     * ids list.
     */
    @Test
    public void testRemoveAfterEntitiesDoesntContainId() {
        // setup
        final List<Integer> ids = new ArrayList<Integer>();
        ids.add(150);

        final List<EntityTierOneType> afterEntities = new ArrayList<EntityTierOneType>();
        afterEntities.add(new EntityTierOneType(TEST, 1));
        afterEntities.add(new EntityTierOneType(TEST, 2));
        afterEntities.add(new EntityTierOneType(TEST, 3));

        // method
        removeAfterEntitites(ids, afterEntities);

        // assert
        assertEquals(3, afterEntities.size());
        assertEquals(new Integer(1), afterEntities.get(0).getId());
        assertEquals(new Integer(2), afterEntities.get(1).getId());
        assertEquals(new Integer(3), afterEntities.get(2).getId());
    }

    /**
     * Test for markEntityAsDeleted of {@link AbstractExecomRepositoryAssert} when entity is marked as deleted.
     */
    @Test
    public void testAssertEntityAsDeletedEntity() {
        // setup
        final List<EntityTierOneType> list1 = new ArrayList<EntityTierOneType>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setList1(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshot();

        final List<EntityTierOneType> list2 = new ArrayList<EntityTierOneType>();
        list2.add(new EntityTierOneType(TEST, 2));
        setList1(list2);

        // method
        assertEntityAsDeleted(actual);
        assertDbState();
    }

    /**
     * Test for markEntityAsDeleted of {@link AbstractExecomRepositoryAssert} when specified object is not entity.
     */
    @Test
    public void testAssertAsDeletedNotEntity() {
        // method
        takeSnapshot();
        assertEntityAsDeleted(new TierOneType());
    }

    /**
     * Test for ignoreEntity of {@link AbstractExecomRepositoryAssert} when specified entity is ignored.
     */
    @Test
    public void testIgnoreEntityEntity() {
        // setup
        final List<EntityTierOneType> list1 = new ArrayList<EntityTierOneType>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setList1(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshot();

        final List<EntityTierOneType> list2 = new ArrayList<EntityTierOneType>();
        list2.add(new EntityTierOneType(TEST, 2));
        setList1(list2);

        // method
        ignoreEntity(actual);
        assertDbState();
    }

    /**
     * Test for ignoreEntity of {@link AbstractExecomRepositoryAssert} when specified object is not entity.
     */
    @Test
    public void testIngoreEntityNotEntity() {
        // method
        takeSnapshot();
        ignoreEntity(new TierOneType());
    }

    /**
     * Test for afterAssertEntity of {@link AbstractExecomRepositoryAssert} when specified object is entity and it is
     * not property.
     */
    @Test
    public void testAfterAssertEntityParentEntityNotProperty() {
        // setup
        final List<EntityTierOneType> list1 = new ArrayList<EntityTierOneType>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setList1(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshot();

        final List<EntityTierOneType> list2 = new ArrayList<EntityTierOneType>();
        list2.add(new EntityTierOneType(TEST, 2));
        setList1(list2);

        // method
        afterAssertEntity(actual, false);
        assertDbState();
    }

    /**
     * Test for afterAssertEntity of {@link AbstractExecomRepositoryAssert} when specified object is entity and it is
     * property of another entity.
     */
    @Test(expected = AssertionError.class)
    public void testAfterAssertEntityIsProperty() {
        // setup
        final List<EntityTierOneType> list1 = new ArrayList<EntityTierOneType>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setList1(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshot();

        final List<EntityTierOneType> list2 = new ArrayList<EntityTierOneType>();
        list2.add(new EntityTierOneType(TEST, 2));
        setList1(list2);

        // method
        afterAssertEntity(actual, true);
        assertDbState();
    }

    /**
     * Test for afterAssertEntity of {@link AbstractExecomRepositoryAssert} when specified object is entity and it is
     * property of another entity.
     */
    @Test(expected = AssertionError.class)
    public void testAfterAssertEntityNotEntity() {
        // setup
        final List<EntityTierOneType> list1 = new ArrayList<EntityTierOneType>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setList1(list1);

        final TierOneType actual = new TierOneType();
        takeSnapshot();

        final List<EntityTierOneType> list2 = new ArrayList<EntityTierOneType>();
        list2.add(new EntityTierOneType(TEST, 2));
        setList1(list2);

        // method
        afterAssertEntity(actual, false);
        assertDbState();
    }

    /**
     * Test for afterAssertEntity of {@link AbstractExecomRepositoryAssert} when specified object is entity and it is
     * not property of another entity but its id is <code>null</code>.
     */
    @Test
    public void testAfterAssertEntityWithoutID() {
        // setup
        final List<EntityTierOneType> list1 = new ArrayList<EntityTierOneType>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setList1(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshot();

        // method
        afterAssertEntity(actual, false);
        assertDbState();
    }

    /**
     * Test for createEmptyCopyOf of {@link AbstractExecomRepositoryAssert} when specified object has default
     * constructor.
     */
    @Test
    public void testCreateEmptyCopyOfHasDefaultConstructor() {
        // method
        final TierOneType assertObject = createEmptyCopyOf(new TierOneType());

        // assert
        assertNotNull(assertObject);
        assertEquals(TierOneType.class, assertObject.getClass());
    }

    /**
     * Test for createEmptyCopyOf of {@link AbstractExecomRepositoryAssert} when specified object has no default
     * constructor.
     */
    @Test(expected = AssertionFailedError.class)
    public void testCreateEmptyCopyOfNoDefaultConstructor() {
        // method
        final NoGetMethodsType assertObject = createEmptyCopyOf(new NoGetMethodsType(TEST));

        // assert
        assertNull(assertObject);
    }

    /**
     * Test for copyList of {@link AbstractExecomRepositoryAssert} if it copies list.
     */
    @Test
    public void testCopyList() {
        // setup
        final List<String> list = new LinkedList<String>();
        list.add(TEST);

        // method
        final List<String> assertList = copyList(list);

        assertNotNull(assertList);
        assertEquals(1, assertList.size());
        assertEquals(TEST, assertList.get(0));
    }

    /**
     * Test for createCopy of {@link AbstractExecomRepositoryAssert} when specified object is null.
     */
    @Test
    public void testCreateCopyNull() {
        // method
        final Object object = createCopy(null);

        // assert
        assertNull(object);
    }

    /**
     * Test for createCopy of {@link AbstractExecomRepositoryAssert} when specified object is list.
     */
    @Test
    public void testCreateCopyList() {
        // setup
        final List<String> list = new LinkedList<String>();
        list.add(TEST);

        // method
        final List<String> assertList = (List<String>) createCopy(list);

        assertNotNull(assertList);
        assertEquals(1, assertList.size());
        assertEquals(TEST, assertList.get(0));
    }

    /**
     * Test for ivokeSetMethod of {@link AbstractExecomRepositoryAssert} when specified object for copying has set
     * method with specified name and can be invoked.
     */
    @Test
    public void testIvokeSetMethodSuccess() {
        // setup
        final Method method = ReflectionUtil.getGetMethods(new TierOneType(), types).get(0);
        final Class<?> classObject = TierOneType.class;
        final String propertyName = PROPERTY;
        final TierOneType copy = new TierOneType();
        final Object copiedProperty = PROPERTY;

        // method
        invokeSetMethod(method, classObject, propertyName, copy, copiedProperty);

        // assert
        assertEquals(PROPERTY, copy.getProperty());
    }

    /**
     * Test for ivokeSetMethod of {@link AbstractExecomRepositoryAssert} when specified object for copying has set
     * method with specified name and can't be invoked as object has no set methods.
     */
    @Test(expected = AssertionFailedError.class)
    public void testIvokeSetMethodNull() {
        // setup
        final Method method = ReflectionUtil.getGetMethods(new TierTwoType(new TierOneType()), types).get(0);
        final Class<?> classObject = TierTwoType.class;
        final String propertyName = PROPERTY;
        final TierTwoType copy = new TierTwoType(new TierOneType());
        final Object copiedProperty = PROPERTY;

        // method
        invokeSetMethod(method, classObject, propertyName, copy, copiedProperty);
    }

    /**
     * Test for copyProperty of {@link AbstractExecomRepositoryAssert} when specified object for copying is null;
     */
    @Test
    public void testCopyPropertyNull() {
        // method
        final Object copy = copyProperty(null, null);

        // assert
        assertNull(copy);
    }

    /**
     * Test for copyProperty of {@link AbstractExecomRepositoryAssert} when specified object is complex object with
     * property of complex type.
     */
    @Test
    public void testCopyPropertyComplexType() {
        // method
        final EntityTierTwoType copy = (EntityTierTwoType) copyProperty(new EntityTierTwoType(TEST, 1,
                new EntityTierOneType(PROPERTY, 2)), new NodesList());

        // assert
        assertNotNull(copy);
        assertEquals(TEST, copy.getProperty());
        assertEquals(1, copy.getId());
        assertNotNull(copy.getSubProperty());
        assertEquals(PROPERTY, copy.getSubProperty().getProperty());
        assertEquals(new Integer(2), copy.getSubProperty().getId());
    }

    /**
     * Test for copyProperty of {@link AbstractExecomRepositoryAssert} when specified object is {@link List}.
     */
    @Test
    public void testCopyPropertyList() {
        // setup
        final List<String> list = new ArrayList<String>();
        list.add(TEST);

        // method
        final List<String> copy = (List<String>) copyProperty(list, null);

        // assert
        assertNotNull(copy);
        assertEquals(1, copy.size());
        assertEquals(TEST, copy.get(0));

    }

    /**
     * Test for copyProperty of {@link AbstractExecomRepositoryAssert} when specified object is of unknown type.
     */
    @Test
    public void testCopyPropertyUnkownType() {
        // setup
        final UnknownType unknownType = new UnknownType();

        // method
        final UnknownType copy = (UnknownType) copyProperty(unknownType, null);

        // assert
        assertEquals(unknownType, copy);

    }

    /**
     * Test for getPropertyForCopying of {@link AbstractExecomRepositoryAssert} when specified method can be invoked.
     */
    @Test
    public void testGetPropertyForCopyingCanInvoke() {
        // setup
        final Method method = ReflectionUtil.getGetMethods(new EntityTierOneType(), types).get(1);

        // method
        final String property = (String) getPropertyForCopying(new EntityTierOneType(TEST, 1), method);

        // assert
        assertEquals(TEST, property);

    }

    /**
     * Test for getPropertyForCopying of {@link AbstractExecomRepositoryAssert} when specified method can not be
     * invoked.
     */
    @Test(expected = AssertionFailedError.class)
    public void testGetPropertyForCopyingCantInvoke() {
        // setup
        final Method method = ReflectionUtil.getGetMethods(new EntityTierOneType(), types).get(1);

        // method
        final String property = (String) getPropertyForCopying(null, method);

        // assert
        assertNull(property);
    }

    /**
     * Test for markAsAsserted of {@link AbstractExecomRepositoryAssert} when specified type is not type supported.
     */
    @Test
    public void testMarkAssertedNotTypeSupportedTrue() {
        // setup
        final List<EntityTierTwoType> list = new ArrayList<EntityTierTwoType>();
        setList2(list);

        // method
        takeSnapshot();
        list.add(new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)));
        final boolean assertValue = markAsAsserted(
                new EntityTierThreeType(TEST, 1, new EntityTierOneType(PROPERTY, 10)), EntityTierThreeType.class);

        // assert
        assertDbState();
        assertTrue(assertValue);
    }

    /**
     * Test for markAsAsserted of {@link AbstractExecomRepositoryAssert} when specified type and its superclass are not
     * supported.
     */
    @Test
    public void testMarkAssertedNotTyrpeSupportedFalse() {
        // method
        takeSnapshot();
        final boolean assertValue = markAsAsserted(new UnknownEntityType(4), UnknownEntityType.class);

        // assert
        assertDbState();
        assertFalse(assertValue);
    }

    /**
     * Test for markAsAsserted of {@link AbstractExecomRepositoryAssert} when there is no {@link CopyAssert} in db
     * snapshot.
     */
    @Test
    public void testMarkAssertedCopyAssertNull() {
        // setup
        final List<EntityTierTwoType> list = new ArrayList<EntityTierTwoType>();
        setList2(list);

        // method
        takeSnapshot();
        list.add(new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)));
        final boolean assertValue = markAsAsserted(new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)),
                EntityTierTwoType.class);

        // assert
        assertDbState();
        assertTrue(assertValue);
    }

    /**
     * Test for markAsAsserted of {@link AbstractExecomRepositoryAssert} when {@link CopyAssert} exists in db snapshot.
     */
    @Test
    public void testMarkAssertedCopyAssertNotNull() {
        // setup
        final EntityTierTwoType entity = new EntityTierThreeType(TEST, 1, new EntityTierOneType(PROPERTY, 10));
        final List<EntityTierTwoType> list = new ArrayList<EntityTierTwoType>();
        list.add(entity);
        setList2(list);

        // method
        takeSnapshot();
        entity.setProperty(TEST + TEST);
        final boolean assertValue = markAsAsserted(new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)),
                EntityTierTwoType.class);

        // assert
        assertDbState();
        assertTrue(assertValue);
    }

    // TODO(nolah) fix this
    // /**
    // * Test for takeSnapshot of {@link AbstractExecomRepositoryAssert} if it initializes snapshot properly.
    // */
    // @Test
    // public void testTakeSnapshot() {
    // // setup
    // final EntityTierTwoType entity = new EntityTierThreeType(TEST, 1, new EntityTierOneType(PROPERTY, 10));
    // final List<EntityTierTwoType> list = new ArrayList<EntityTierTwoType>();
    // list.add(entity);
    // setList2(list);
    //
    // // method
    // takeSnapshot();
    // final Map<Class<?>, Map<Integer, CopyAssert<Type>>> dbSnapshot = getDbSnapshot();
    //
    // // assert
    // assertEquals(2, dbSnapshot.size());
    // for (final Entry<Class<?>, Map<Integer, CopyAssert<Type>>> classEntry : dbSnapshot.entrySet()) {
    // if (classEntry.getKey() == EntityTierOneType.class) {
    // assertEquals(0, classEntry.getValue().size());
    // }
    //
    // if (classEntry.getKey() == EntityTierTwoType.class) {
    // assertEquals(1, classEntry.getValue().size());
    // final Object object = classEntry.getValue().get(1);
    // final CopyAssert<EntityTierTwoType> copyAssert = (CopyAssert<EntityTierTwoType>) object;
    // final EntityTierTwoType assertEntity = copyAssert.getEntity();
    // assertEquals(1, assertEntity.getId());
    // assertEquals(TEST, assertEntity.getProperty());
    // assertEquals(PROPERTY, assertEntity.getSubProperty().getProperty());
    // assertEquals(new Integer(10), assertEntity.getSubProperty().getId());
    // }
    // }
    // }

    // TODO this should be reworked when takeSnapshot functionality is changed

    // /**
    // * Test for assertEntityWithSnapshot from {@link AbstractExecomRepositoryAssert} when id is null.
    // */
    // @Test(expected = AssertionError.class)
    // public void testAssertEntityWithSnapshotNullId() {
    // // setup
    // final EntityTierOneType entityTierOneType = new EntityTierOneType(TEST, null);
    //
    // // method
    // takeSnapshot();
    // assertEntityWithSnapshot(entityTierOneType);
    // }
    //
    // /**
    // * Test for assertEntityWithSnapshot from {@link AbstractExecomRepositoryAssert} when entity type isn't supported.
    // */
    // @Test(expected = AssertionError.class)
    // public void testAssertEntityWithSnapshotTypeNotSupported() {
    // // setup
    // final TierOneType expected = new TierOneType();
    //
    // // method
    // takeSnapshot();
    // assertEntityWithSnapshot(expected);
    // }
    //
    // /**
    // * Test for assertEntityWithSnapshot from {@link AbstractExecomRepositoryAssert} when entity doesn't exist in
    // * snapshot.
    // */
    // @Test(expected = AssertionError.class)
    // public void testAssertEntityWithSnapshotNoEntityInSnapshot() {
    // // setup
    // setList1(new ArrayList<EntityTierOneType>());
    //
    // // method
    // takeSnapshot();
    // assertEntityWithSnapshot(new EntityTierOneType(TEST, 1));
    // }
    //
    // /**
    // * Test for assertEntityWithSnapshot from {@link AbstractExecomRepositoryAssert} when entity exists in snapshot
    // and
    // * is asserted with new changed properties.
    // */
    // @Test
    // public void testAssertEntityWithSnapshotEntityAsserted() {
    // // setup
    // final List<EntityTierOneType> list = new ArrayList<EntityTierOneType>();
    // final EntityTierOneType expected = new EntityTierOneType(TEST, 1);
    // list.add(expected);
    // setList1(list);
    //
    // // method
    // takeSnapshot();
    // expected.setProperty(TEST + TEST);
    // assertEntityWithSnapshot(expected, PropertyFactory.value(EntityTierOneType.PROPERTY, TEST + TEST));
    // }

    /**
     * Test for assertParametersState of {@link AbstractExecomRepositoryAssert} when before snapshot matches after
     * parameters state.
     */
    @Test
    public void testAssertParametersTrue() {
        // setup
        final EntityTierTwoType entity2 = new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10));
        final EntityTierThreeType entity3 = new EntityTierThreeType(TEST + TEST, 5, new EntityTierOneType(PROPERTY
                + PROPERTY, 15));

        takeSnapshot(entity2, entity3);

        entity2.setId(new Integer(1));
        entity2.setProperty(TEST);
        entity2.getSubProperty().setId(new Integer(10));
        entity3.setSubProperty(new EntityTierOneType(PROPERTY + PROPERTY, 15));

        // method
        assertParameters();
    }

    /**
     * Test for assertParametersState of {@link AbstractExecomRepositoryAssert} when before snapshot doesn't match after
     * parameters state.
     */
    @Test(expected = AssertionError.class)
    public void testAssertParametersFalse() {
        // setup
        final EntityTierTwoType entity2 = new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10));
        final EntityTierThreeType entity3 = new EntityTierThreeType(TEST + TEST, 5, new EntityTierOneType(PROPERTY
                + PROPERTY, 15));

        initDbSnapshot();
        takeSnapshot(entity2, entity3);

        entity2.setId(new Integer(2));
        entity2.getSubProperty().setId(new Integer(15));
        entity3.setProperty(TEST);
        entity3.getSubProperty().setId(1);

        // method
        assertParameters();
    }

    /**
     * Test for {@link AbstractExecomRepositoryAssert#takeSnapshot(Object...)} when specified object has no default
     * constructor.
     */
    @Test(expected = AssertionError.class)
    public void testAssertParametersNullCopy() {
        // setup
        final NoDefaultConstructorType noDefaultConstructorType = new NoDefaultConstructorType(TEST);

        // method
        takeSnapshot(noDefaultConstructorType);
        assertParameters();
    }

    /**
     * Test for takeSnapshot with varargs if it initializes snapshot properly.
     */
    @Test
    public void testTakeSnapshotWithVarargs() {
        // setup
        final EntityTierTwoType entity2 = new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10));
        final EntityTierThreeType entity3 = new EntityTierThreeType(TEST + TEST, 5, new EntityTierOneType(PROPERTY
                + PROPERTY, 15));

        // method
        takeSnapshot(entity2, entity3);
        final List<Object> parameters = getParameters();
        final List<Object> parametersSnapshot = getParametersSnapshot();

        // assert
        assertEquals(parameters.size(), parametersSnapshot.size());
        assertTrue(parametersSnapshot.get(0) instanceof EntityTierTwoType);
        assertTrue(parametersSnapshot.get(1) instanceof EntityTierThreeType);

        final EntityTierTwoType assertEntity2 = (EntityTierTwoType) parametersSnapshot.get(0);
        assertEquals(1, assertEntity2.getId());
        assertEquals(TEST, assertEntity2.getProperty());
        assertEquals(new Integer(10), assertEntity2.getSubProperty().getId());
        assertEquals(PROPERTY, assertEntity2.getSubProperty().getProperty());

        final EntityTierThreeType assertEntity3 = (EntityTierThreeType) parametersSnapshot.get(1);
        assertEquals(5, assertEntity3.getId());
        assertEquals(TEST + TEST, assertEntity3.getProperty());
        assertEquals(new Integer(15), assertEntity3.getSubProperty().getId());
        assertEquals(PROPERTY + PROPERTY, assertEntity3.getSubProperty().getProperty());
    }

    /**
     * Test for createCopy if it properly handles cyclic object references.
     */
    @Test
    public void testCreateCopyCyclic() {
        // setup
        final A a = new A();
        a.setProperty(PROPERTY);
        a.setB(new B(new C(a)));

        // method
        final A aCopy = (A) createCopy(a);

        // assert
        assertEquals(aCopy, aCopy.getB().getC().getA());
        assertEquals(a.getProperty(), aCopy.getProperty());
        assertEquals(a.getB().getC().getA().getProperty(), aCopy.getB().getC().getA().getProperty());
    }

}
