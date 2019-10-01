package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FabutRepositoryAssertTest extends Fabut {

    private static final String TEST = "test";
    private static final String PROPERTY = "property";

    // mock lists
    private List<Object> entityTierOneTypes = new ArrayList<>();
    private List<Object> entityTierTwoTypes = new ArrayList<>();
    private List<Object> entityWithListTypes = new ArrayList<>();
    private List<Object> noDefaultConstructorEntities = new ArrayList<>();

    public FabutRepositoryAssertTest() {
        super();
        entityTypes.add(EntityTierOneType.class);
        entityTypes.add(EntityTierTwoType.class);
        entityTypes.add(EntityWithList.class);
        entityTypes.add(NoDefaultConstructorEntity.class);

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
        complexTypes.add(TierTwoTypeWithMap.class);

        ignoredTypes.add(IgnoredType.class);
    }

    @Override
    public List<Object> findAll(final Class<?> entityClass) {
        if (entityClass == EntityTierOneType.class) {
            return entityTierOneTypes;
        }
        if (entityClass == EntityTierTwoType.class) {
            return entityTierTwoTypes;
        }
        if (entityClass == EntityWithList.class) {
            return entityWithListTypes;
        }
        if (entityClass == NoDefaultConstructorEntity.class) {
            return noDefaultConstructorEntities;
        }
        return null;
    }

    @Override
    public Object findById(final Class<?> entityClass, final Object id) {
        if (entityClass == EntityTierOneType.class) {
            for (final Object entity : entityTierOneTypes) {
                if (((EntityTierOneType) entity).getId().equals(id)) {
                    return entity;
                }
            }
        }
        if (entityClass == EntityWithList.class) {
            for (final Object entity : entityWithListTypes) {
                if (((EntityWithList) entity).getId().equals(id)) {
                    return entity;
                }
            }
        }
        if (entityClass == NoDefaultConstructorEntity.class) {
            for (final Object entity : noDefaultConstructorEntities) {
                if (((NoDefaultConstructorEntity) entity).getId().equals(id)) {
                    return entity;
                }
            }
        }
        return null;
    }

    private void setEntityTierOneTypes(final List<Object> list1) {
        entityTierOneTypes = list1;
    }

    private void setEntityTierTwoTypes(final List<Object> list2) {
        entityTierTwoTypes = list2;
    }

    @Test
    public void testAssertDbStateTrue() {
        // setup
        final List<Object> beforeList1 = new ArrayList<Object>();
        beforeList1.add(new EntityTierOneType(TEST + TEST, 1));
        setEntityTierOneTypes(beforeList1);

        final List<Object> beforeList2 = new ArrayList<Object>();
        beforeList2.add(new EntityTierTwoType(PROPERTY + PROPERTY + PROPERTY, 4, new EntityTierOneType(TEST + TEST
                + TEST + TEST, 4)));
        setEntityTierTwoTypes(beforeList2);

        takeSnapshot(new FabutReport());

        final List<Object> afterList1 = new ArrayList<Object>();
        afterList1.add(new EntityTierOneType(TEST + TEST, 1));
        setEntityTierOneTypes(afterList1);

        final List<Object> afterist2 = new ArrayList<Object>();
        afterist2.add(new EntityTierTwoType(PROPERTY + PROPERTY + PROPERTY, 4, new EntityTierOneType(TEST + TEST + TEST
                + TEST, 4)));
        setEntityTierTwoTypes(afterist2);

        // method
        final FabutReport report = new FabutReport();
        assertDbSnapshot(report);

        // assert
        assertTrue(report.isSuccess());
    }

    @Test
    public void testAssertDbStateFalse() {
        // setup

        final List<Object> beforeList1 = new ArrayList<Object>();
        beforeList1.add(new EntityTierOneType(TEST, 1));
        setEntityTierOneTypes(beforeList1);

        final List<Object> beforeList2 = new ArrayList<Object>();
        beforeList2.add(new EntityTierTwoType(PROPERTY, 4, new EntityTierOneType(TEST, 7)));
        setEntityTierTwoTypes(beforeList2);

        takeSnapshot(new FabutReport());

        final List<Object> afterList1 = new ArrayList<Object>();
        afterList1.add(new EntityTierOneType(TEST + TEST, 1));
        setEntityTierOneTypes(afterList1);

        final List<Object> afterList2 = new ArrayList<Object>();
        afterList2.add(new EntityTierTwoType(PROPERTY + PROPERTY, 4, new EntityTierOneType(TEST + TEST, 7)));
        setEntityTierTwoTypes(afterList2);

        // method
        final FabutReport report = new FabutReport();
        assertDbSnapshot(report);

        // assert
        assertFalse(report.isSuccess());
    }

    @Test
    public void testAssertEntityAsDeletedEntity() {
        // setup
        final List<Object> list1 = new ArrayList<Object>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshot(new FabutReport());

        final List<Object> list2 = new ArrayList<Object>();
        list2.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list2);

        // method
        final FabutReport assertEntityAsDeleted = new FabutReport();
        assertEntityAsDeleted(assertEntityAsDeleted, actual);
        final FabutReport report = new FabutReport();
        assertDbSnapshot(report);


        // assert
        assertTrue(assertEntityAsDeleted.isSuccess());
        assertTrue(report.isSuccess());

    }

    @Test
    public void testIgnoreEntityEntity() {
        // setup
        final List<Object> list1 = new ArrayList<Object>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshot(new FabutReport());

        final List<Object> list2 = new ArrayList<Object>();
        list2.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list2);

        // method
        final FabutReport ignoreEntityReport = new FabutReport();
        ignoreEntity(ignoreEntityReport, actual);
        final FabutReport report = new FabutReport();
        assertDbSnapshot(report);


        // assert
        assertTrue(ignoreEntityReport.isSuccess());
        assertTrue(report.isSuccess());
    }

    @Test
    public void testAfterAssertEntityParentEntityNotProperty() {
        // setup
        final List<Object> list1 = new ArrayList<Object>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshot(new FabutReport());

        final List<Object> list2 = new ArrayList<Object>();
        list2.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list2);

        // method
        final FabutReport assertReport = new FabutReport();
        afterAssertEntity(assertReport, actual, false);
        final FabutReport report = new FabutReport();
        assertDbSnapshot(report);


        // assert
        assertTrue(assertReport.isSuccess());
        assertTrue(report.isSuccess());
    }

//    @Test(expected = AssertionError.class)
//    public void testAfterAssertEntityIsProperty() {
//        // setup
//        final List<Object> list1 = new ArrayList<Object>();
//        list1.add(new EntityTierOneType(TEST, 1));
//        list1.add(new EntityTierOneType(TEST, 2));
//        setEntityTierOneTypes(list1);
//
//        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
//        takeSnapshot(new FabutReport());
//
//        final List<Object> list2 = new ArrayList<Object>();
//        list2.add(new EntityTierOneType(TEST, 2));
//        setEntityTierOneTypes(list2);
//
//        // method
//        afterAssertEntity(new FabutReport(), actual, true);
//        final FabutReport report = new FabutReport();
//        assertDbSnapshot(report);
//        ;
//
//        // assert
//        assertTrue(assertResult);
//    }
//
//    @Test
//    public void testAfterAssertEntityNotEntity() {
//        // setup
//        final List<Object> list1 = new ArrayList<Object>();
//        list1.add(new EntityTierOneType(TEST, 1));
//        list1.add(new EntityTierOneType(TEST, 2));
//        setEntityTierOneTypes(list1);
//
//        final TierOneType actual = new TierOneType();
//        takeSnapshot(new FabutReport());
//
//        final List<Object> list2 = new ArrayList<Object>();
//        list2.add(new EntityTierOneType(TEST, 2));
//        setEntityTierOneTypes(list2);
//
//        // method
//        afterAssertEntity(new FabutReport(), actual, false);
//        final FabutReport report = new FabutReport();
//        assertDbSnapshot(report);
//        ;
//
//        // assert
//        assertFalse(assertResult);
//    }
//
//    @Test
//    public void testAfterAssertEntityWithoutID() {
//        // setup
//        final List<Object> list1 = new ArrayList<Object>();
//        list1.add(new EntityTierOneType(TEST, 1));
//        list1.add(new EntityTierOneType(TEST, 2));
//        setEntityTierOneTypes(list1);
//
//        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
//        takeSnapshot(new FabutReport());
//
//        // method
//        final boolean afterAssertEntity = afterAssertEntity(new FabutReport(),
//                actual, false);
//        final boolean assertDbState = final FabutReport report = new FabutReport();
//        assertDbSnapshot(report);
//        ;
//
//        // assert
//        assertTrue(afterAssertEntity);
//        assertTrue(assertDbState);
//    }
//
//    @Test
//    public void testMarkAssertedNotTypeSupportedTrue() {
//        // setup
//        final List<Object> list = new ArrayList<>();
//        setEntityTierTwoTypes(list);
//
//        // method
//        takeSnapshot(new FabutReport());
//        list.add(new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)));
//        final boolean assertValue = markAsAsserted(new FabutReport(),
//                new EntityTierThreeType(TEST, 1, new EntityTierOneType(PROPERTY, 10)), EntityTierThreeType.class);
//
//        // assert
//        assertTrue( final FabutReport report = new FabutReport();
//        assertDbSnapshot(report););
//        assertTrue(assertValue);
//    }
//
//    @Test
//    public void testMarkAssertedNotTyrpeSupportedFalse() {
//        // method
//        takeSnapshot(new FabutReport());
//        final boolean assertValue = markAsAsserted(new FabutReport(),
//                new UnknownEntityType(4), UnknownEntityType.class);
//
//        // assert
//        assertTrue( final FabutReport report = new FabutReport();
//        assertDbSnapshot(report););
//        assertFalse(assertValue);
//    }
//
//    @Test
//    public void testMarkAssertedCopyAssertNull() {
//        // setup
//        final List<Object> list = new ArrayList<Object>();
//        setEntityTierTwoTypes(list);
//
//        // method
//        takeSnapshot(new FabutReport());
//        list.add(new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)));
//        final boolean assertValue = markAsAsserted(new FabutReport(),
//                new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)), EntityTierTwoType.class);
//
//        // assert
//        assertTrue( final FabutReport report = new FabutReport();
//        assertDbSnapshot(report););
//        assertTrue(assertValue);
//    }
//
//    @Test
//    public void testMarkAssertedCopyAssertNotNull() {
//        // setup
//        final EntityTierTwoType entity = new EntityTierThreeType(TEST, 1, new EntityTierOneType(PROPERTY, 10));
//        final List<Object> list = new ArrayList<Object>();
//        list.add(entity);
//        setEntityTierTwoTypes(list);
//
//        // method
//        takeSnapshot(new FabutReport());
//        entity.setProperty(TEST + TEST);
//        final boolean assertValue = markAsAsserted(new FabutReport(),
//                new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)), EntityTierTwoType.class);
//
//        // assert
//        assertTrue( final FabutReport report = new FabutReport();
//        assertDbSnapshot(report););
//        assertTrue(assertValue);
//    }
//
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    @Test
//    public void testCheckNotExistingInAfterDbStateTrue() {
//        // setup
//        final TreeSet beforeIds = new TreeSet();
//        beforeIds.add(1);
//        beforeIds.add(2);
//        beforeIds.add(3);
//
//        final TreeSet afterIds = new TreeSet();
//        afterIds.add(1);
//        afterIds.add(3);
//
//        final Map<Object, CopyAssert> beforeEntities = new HashMap<Object, CopyAssert>();
//
//        final CopyAssert copyAssert1 = new CopyAssert(new EntityTierOneType());
//        beforeEntities.put(1, copyAssert1);
//
//        final CopyAssert copyAssert2 = new CopyAssert(new EntityTierOneType());
//        copyAssert2.setAsserted(true);
//        beforeEntities.put(2, copyAssert2);
//
//        final CopyAssert copyAssert3 = new CopyAssert(new EntityTierOneType());
//        beforeEntities.put(3, copyAssert3);
//
//        // method
//        checkNotExistingInAfterDbState(beforeIds, afterIds,
//                beforeEntities, new FabutReport());
//
//        // assert
//        assertTrue(assertResult);
//    }
//
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    @Test
//    public void testCheckNotExistingInAfterDbStateFalse() {
//        // setup
//        final TreeSet beforeIds = new TreeSet();
//        beforeIds.add(1);
//        beforeIds.add(2);
//        beforeIds.add(3);
//
//        final TreeSet afterIds = new TreeSet();
//        afterIds.add(1);
//        afterIds.add(3);
//
//        final Map<Object, CopyAssert> beforeEntities = new HashMap<Object, CopyAssert>();
//
//        final CopyAssert copyAssert1 = new CopyAssert(new EntityTierOneType());
//        beforeEntities.put(1, copyAssert1);
//
//        final CopyAssert copyAssert2 = new CopyAssert(new EntityTierOneType());
//        beforeEntities.put(2, copyAssert2);
//
//        final CopyAssert copyAssert3 = new CopyAssert(new EntityTierOneType());
//        beforeEntities.put(3, copyAssert3);
//
//        // method
//        checkNotExistingInAfterDbState(beforeIds, afterIds,
//                beforeEntities, new FabutReport());
//
//        // assert
//        assertFalse(assertResult);
//    }
//
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    @Test
//    public void testCheckAddedToAfterDbStateFalse() {
//        // setup
//        final TreeSet beforeIds = new TreeSet();
//        beforeIds.add(1);
//        beforeIds.add(2);
//
//        final TreeSet afterIds = new TreeSet();
//        afterIds.add(1);
//        afterIds.add(3);
//
//        final Map<Object, Object> afterEntities = new HashMap<Object, Object>();
//        afterEntities.put(1, new EntityTierOneType());
//        afterEntities.put(3, new EntityTierOneType());
//
//        // method
//        checkNewToAfterDbState(beforeIds, afterIds,
//                afterEntities, new FabutReport());
//
//        // assert
//        assertFalse(assertResult);
//    }
//
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    @Test
//    public void testCheckAddedToAfterDbStateTrue() {
//        // setup
//        final TreeSet beforeIds = new TreeSet();
//        beforeIds.add(1);
//        beforeIds.add(3);
//
//        final TreeSet afterIds = new TreeSet();
//        afterIds.add(1);
//        afterIds.add(3);
//
//        final Map<Object, Object> afterEntities = new HashMap<Object, Object>();
//        afterEntities.put(1, new EntityTierOneType());
//        afterEntities.put(3, new EntityTierOneType());
//
//        // method
//        checkNewToAfterDbState(beforeIds, afterIds,
//                afterEntities, new FabutReport());
//
//        // assert
//        assertTrue(assertResult);
//    }
//
//
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    @Test
//    public void testAssertDbSnapshotWithAfterStateTrue() {
//        // setup
//        final TreeSet beforeIds = new TreeSet();
//        beforeIds.add(1);
//        beforeIds.add(3);
//
//        final TreeSet afterIds = new TreeSet();
//        afterIds.add(1);
//        afterIds.add(3);
//
//        final Map<Object, Object> afterEntities = new HashMap<Object, Object>();
//        afterEntities.put(1, new EntityTierOneType());
//        afterEntities.put(3, new EntityTierOneType(TEST, 3));
//
//        final Map<Object, CopyAssert> beforeEntities = new HashMap<Object, CopyAssert>();
//
//        final CopyAssert copyAssert1 = new CopyAssert(new EntityTierOneType(TEST, 1));
//        copyAssert1.setAsserted(true);
//        beforeEntities.put(1, copyAssert1);
//
//        final CopyAssert copyAssert2 = new CopyAssert(new EntityTierOneType());
//        copyAssert2.setAsserted(true);
//        beforeEntities.put(2, copyAssert2);
//
//        final CopyAssert copyAssert3 = new CopyAssert(new EntityTierOneType(TEST, 3));
//        beforeEntities.put(3, copyAssert3);
//
//        // method
//        assertDbSnapshotWithAfterState(beforeIds, afterIds,
//                beforeEntities, afterEntities, new FabutReport());
//
//        // assert
//        assertTrue(assertResult);
//    }
//
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    @Test
//    public void testAssertDbSnapshotWithAfterStateFalse() {
//        // setup
//        final TreeSet beforeIds = new TreeSet();
//        beforeIds.add(1);
//        beforeIds.add(3);
//
//        final TreeSet afterIds = new TreeSet();
//        afterIds.add(1);
//        afterIds.add(3);
//
//        final Map<Object, Object> afterEntities = new HashMap<Object, Object>();
//        afterEntities.put(1, new EntityTierOneType());
//        afterEntities.put(3, new EntityTierOneType(TEST + TEST, 3));
//
//        final Map<Object, CopyAssert> beforeEntities = new HashMap<Object, CopyAssert>();
//
//        final CopyAssert copyAssert1 = new CopyAssert(new EntityTierOneType(TEST, 1));
//        copyAssert1.setAsserted(true);
//        beforeEntities.put(1, copyAssert1);
//
//        final CopyAssert copyAssert2 = new CopyAssert(new EntityTierOneType());
//        copyAssert2.setAsserted(true);
//        beforeEntities.put(2, copyAssert2);
//
//        final CopyAssert copyAssert3 = new CopyAssert(new EntityTierOneType(TEST, 3));
//        beforeEntities.put(3, copyAssert3);
//
//        // method
//        assertDbSnapshotWithAfterState(beforeIds, afterIds,
//                beforeEntities, afterEntities, new FabutReport());
//
//        // assert
//        assertFalse(assertResult);
//    }
//
//    @Test
//    public void testAssertPairNotPropertyAsserted() {
//        // setup
//        final AssertPair entityPair = ConversionUtil.createAssertPair(new EntityTierOneType(TEST, 1),
//                new EntityTierOneType(TEST, 1), getTypes());
//
//        // method
//        assertPair("", new FabutReport(), entityPair,
//                new LinkedList<ISingleProperty>(), new NodesList());
//
//        // assert
//        assertTrue(assertResult);
//    }
//
//    @Test
//    public void testAssertPairNotPropertyAssertFail() {
//        // setup
//        final AssertPair entityPair = ConversionUtil.createAssertPair(
//                new EntityTierOneType(TEST + TEST, 1), new EntityTierOneType(TEST, 1),
//                getTypes());
//
//        // method
//        assertPair("", new FabutReport(), entityPair,
//                new LinkedList<>(), new NodesList());
//
//        // assert
//        assertFalse(assertResult);
//    }
//
//    @Test
//    public void testAssertPairPropertyAsserted() {
//        // setup
//        final AssertPair entityPair = ConversionUtil.createAssertPair(new EntityTierOneType(TEST, 1),
//                new EntityTierOneType(TEST, 1), getTypes());
//        entityPair.setProperty(true);
//
//        // method
//        assertPair("", new FabutReport(), entityPair,
//                new LinkedList<ISingleProperty>(), new NodesList());
//
//        // assert
//        assertTrue(assertResult);
//    }
//
//    @Test
//    public void testAssertPairPropertyAssertFail() {
//        // setup
//        final AssertPair entityPair = ConversionUtil.createAssertPair(
//                new EntityTierOneType(TEST + TEST, 1), new EntityTierOneType(TEST, 2),
//                getTypes());
//        entityPair.setProperty(true);
//
//        // method
//        assertPair("", new FabutReport(), entityPair, new LinkedList<ISingleProperty>(), new NodesList());
//
//        // assert
//        assertFalse(assertResult);
//    }
//
//    @Test
//    public void testAssertEntityWithSnapshotTrue() {
//        // setup
//        final List<Object> list1 = new ArrayList<Object>();
//        list1.add(new EntityTierOneType(TEST, 1));
//        setEntityTierOneTypes(list1);
//        final EntityTierOneType entity = new EntityTierOneType(TEST + TEST, 1);
//        final List<ISingleProperty> properties = new LinkedList<ISingleProperty>();
//        properties.add(Fabut.value(EntityTierOneType.PROPERTY, TEST + TEST));
//
//        // method
//        takeSnapshot(new FabutReport());
//        final boolean assertEntityWithSnapshot = assertEntityWithSnapshot(
//                new FabutReport(), entity, properties);
//
//        // assert
//        assertTrue(assertEntityWithSnapshot);
//        assertTrue( final FabutReport report = new FabutReport();
//        assertDbSnapshot(report););
//
//    }
//
//    @Test
//    public void testAssertEntityWithSnapshotFalse() {
//        // setup
//        final List<Object> list1 = new ArrayList<Object>();
//        setEntityTierOneTypes(list1);
//        final EntityTierOneType entity = new EntityTierOneType(TEST + TEST, 1);
//        final List<ISingleProperty> properties = new LinkedList<ISingleProperty>();
//        properties.add(Fabut.value(EntityTierOneType.PROPERTY, TEST + TEST));
//
//        // method
//        takeSnapshot(new FabutReport());
//        final boolean assertEntityWithSnapshot = assertEntityWithSnapshot(
//                new FabutReport(), entity, properties);
//
//        // assert
//        assertFalse(assertEntityWithSnapshot);
//        assertTrue( final FabutReport report = new FabutReport();
//        assertDbSnapshot(report););
//
//    }
}
