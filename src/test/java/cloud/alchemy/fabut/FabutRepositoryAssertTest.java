package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.*;
import cloud.alchemy.fabut.property.CopyAssert;
import cloud.alchemy.fabut.property.ISingleProperty;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.util.*;

public class FabutRepositoryAssertTest extends Fabut {

    private static final String TEST = "test";
    private static final String PROPERTY = "property";

    // mock lists
    private List<Object> entityTierOneTypes = new ArrayList<>();
    private List<Object> entityTierTwoTypes = new ArrayList<>();
    private List<Object> entityWithListTypes = new ArrayList<>();
    private List<Object> noDefaultConstructorEntities = new ArrayList<>();
    private boolean assertAfterTest = true;

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
        if (entityClass == EntityTierTwoType.class) {
            for (final Object entity : entityTierTwoTypes) {
                if (((EntityTierTwoType) entity).getId() == id) {
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

    @Override
    public void fabutBeforeTest() {
        super.fabutBeforeTest();
        assertAfterTest = true;
    }

    @Override
    public void fabutAfterTest() {
        if (assertAfterTest) {
            super.fabutAfterTest();
        }
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
        final List<Object> beforeList1 = new ArrayList<>();
        beforeList1.add(new EntityTierOneType(TEST + TEST, 1));
        setEntityTierOneTypes(beforeList1);

        final List<Object> beforeList2 = new ArrayList<>();
        beforeList2.add(new EntityTierTwoType(PROPERTY + PROPERTY + PROPERTY, 4, new EntityTierOneType(TEST + TEST + TEST + TEST, 4)));
        setEntityTierTwoTypes(beforeList2);

        takeSnapshot();

        final List<Object> afterList1 = new ArrayList<>();
        afterList1.add(new EntityTierOneType(TEST + TEST, 1));
        setEntityTierOneTypes(afterList1);

        final List<Object> afterist2 = new ArrayList<>();
        afterist2.add(new EntityTierTwoType(PROPERTY + PROPERTY + PROPERTY, 4, new EntityTierOneType(TEST + TEST + TEST + TEST, 4)));
        setEntityTierTwoTypes(afterist2);
    }

    @Test
    public void testAssertDbStateFalse() {
        // setup

        final List<Object> beforeList1 = new ArrayList<>();
        beforeList1.add(new EntityTierOneType(TEST, 1));
        setEntityTierOneTypes(beforeList1);

        final List<Object> beforeList2 = new ArrayList<>();
        beforeList2.add(new EntityTierTwoType(PROPERTY, 4, new EntityTierOneType(TEST, 7)));
        setEntityTierTwoTypes(beforeList2);

        takeSnapshott(new FabutReport());

        final List<Object> afterList1 = new ArrayList<>();
        afterList1.add(new EntityTierOneType(TEST + TEST, 1));
        setEntityTierOneTypes(afterList1);

        final List<Object> afterList2 = new ArrayList<>();
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
        final List<Object> list1 = new ArrayList<>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshott(new FabutReport());

        final List<Object> list2 = new ArrayList<>();
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
        final List<Object> list1 = new ArrayList<>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshott(new FabutReport());

        final List<Object> list2 = new ArrayList<>();
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
        final List<Object> list1 = new ArrayList<>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshott(new FabutReport());

        final List<Object> list2 = new ArrayList<>();
        list2.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list2);

        // method
        final FabutReport assertReport = new FabutReport();
        afterAssertObject(assertReport, actual);
        final FabutReport report = new FabutReport();
        assertDbSnapshot(report);


        // assert
        assertTrue(assertReport.isSuccess());
        assertTrue(report.isSuccess());
    }

    @Test(expected = AssertionError.class)
    public void testAfterAssertEntityIsProperty() {
        // setup
        final List<Object> list1 = new ArrayList<>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshot(new FabutReport());

        final List<Object> list2 = new ArrayList<>();
        list2.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list2);

        // method
        assertAfterTest = false;
        super.fabutAfterTest();
    }

    @Test(expected = AssertionFailedError.class)
    public void testAfterAssertEntityNotEntity() {
        // setup
        final List<Object> list1 = new ArrayList<>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final TierOneType actual = new TierOneType();
        takeSnapshot(new FabutReport());

        final List<Object> list2 = new ArrayList<>();
        list2.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list2);

        // method
        assertAfterTest = false;
        super.fabutAfterTest();
    }

    @Test
    public void testAfterAssertEntityWithoutID() {
        // setup
        final List<Object> list1 = new ArrayList<>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        takeSnapshot();

        // method
        final FabutReport entityAssertReport = new FabutReport();
        afterAssertObject(entityAssertReport, actual);
        assertTrue(entityAssertReport.isSuccess());
    }

    @Test(expected = NullPointerException.class)
    public void testMarkAssertedNotTyrpeSupportedFalse() {
        // method
        takeSnapshot();
        final FabutReport report = new FabutReport();
        markAsAsserted(report, new UnknownEntityType(4));

        // assert
        assertTrue(report.isSuccess());
    }

    @Test
    public void testMarkAssertedCopyAssertNull() {
        // setup
        final List<Object> list = new ArrayList<>();
        setEntityTierTwoTypes(list);

        // method
        takeSnapshot();
        list.add(new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)));

        final FabutReport report = new FabutReport();
        markAsAsserted(report, new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)));

        // assert
        assertTrue(report.isSuccess());
    }

    @Test
    public void testMarkAssertedCopyAssertNotNull() {
        // setup
        final EntityTierTwoType entity = new EntityTierThreeType(TEST, 1, new EntityTierOneType(PROPERTY, 10));
        final List<Object> list = new ArrayList<>();
        list.add(entity);
        setEntityTierTwoTypes(list);

        // method
        takeSnapshot();
        entity.setProperty(TEST + TEST);
        final FabutReport report = new FabutReport();
        markAsAsserted(report, new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)));

        // assert
        assertTrue(report.isSuccess());
    }


    @Test
    public void testCheckNotExistingInAfterDbStateTrue() {
        // setup
        final TreeSet beforeIds = new TreeSet();
        beforeIds.add(1);
        beforeIds.add(2);
        beforeIds.add(3);

        final TreeSet afterIds = new TreeSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, CopyAssert> beforeEntities = new HashMap<>();

        final CopyAssert copyAssert1 = new CopyAssert(new EntityTierOneType());
        beforeEntities.put(1, copyAssert1);

        final CopyAssert copyAssert2 = new CopyAssert(new EntityTierOneType());
        copyAssert2.setAsserted(true);
        beforeEntities.put(2, copyAssert2);

        final CopyAssert copyAssert3 = new CopyAssert(new EntityTierOneType());
        beforeEntities.put(3, copyAssert3);

        // method
        final FabutReport report = new FabutReport();
        checkNotExistingInAfterDbState(beforeIds, afterIds, beforeEntities, report);

        // assert
        assertTrue(report.isSuccess());
    }

    @Test
    public void testCheckNotExistingInAfterDbStateFalse() {
        // setup
        final TreeSet beforeIds = new TreeSet();
        beforeIds.add(1);
        beforeIds.add(2);
        beforeIds.add(3);

        final TreeSet afterIds = new TreeSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, CopyAssert> beforeEntities = new HashMap<>();

        final CopyAssert copyAssert1 = new CopyAssert(new EntityTierOneType());
        beforeEntities.put(1, copyAssert1);

        final CopyAssert copyAssert2 = new CopyAssert(new EntityTierOneType());
        beforeEntities.put(2, copyAssert2);

        final CopyAssert copyAssert3 = new CopyAssert(new EntityTierOneType());
        beforeEntities.put(3, copyAssert3);

        // method
        final FabutReport report = new FabutReport();
        checkNotExistingInAfterDbState(beforeIds, afterIds, beforeEntities, report);

        // assert
        assertFalse(report.isSuccess());
    }


    @Test
    public void testCheckAddedToAfterDbStateFalse() {
        // setup
        final TreeSet beforeIds = new TreeSet();
        beforeIds.add(1);
        beforeIds.add(2);

        final TreeSet afterIds = new TreeSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, Object> afterEntities = new HashMap<>();
        afterEntities.put(1, new EntityTierOneType());
        afterEntities.put(3, new EntityTierOneType());

        // method
        final FabutReport report = new FabutReport();
        checkNewToAfterDbState(beforeIds, afterIds, afterEntities, report);

        // assert
        assertFalse(report.isSuccess());
    }


    @Test
    public void testCheckAddedToAfterDbStateTrue() {
        // setup
        final TreeSet beforeIds = new TreeSet();
        beforeIds.add(1);
        beforeIds.add(3);

        final TreeSet afterIds = new TreeSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, Object> afterEntities = new HashMap<>();
        afterEntities.put(1, new EntityTierOneType());
        afterEntities.put(3, new EntityTierOneType());

        // method
        final FabutReport report = new FabutReport();
        checkNewToAfterDbState(beforeIds, afterIds, afterEntities, report);

        // assert
        assertTrue(report.isSuccess());
    }

    @Test
    public void testAssertDbSnapshotWithAfterStateTrue() {
        // setup
        final TreeSet beforeIds = new TreeSet();
        beforeIds.add(1);
        beforeIds.add(3);

        final TreeSet afterIds = new TreeSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, Object> afterEntities = new HashMap<>();
        afterEntities.put(1, new EntityTierOneType());
        afterEntities.put(3, new EntityTierOneType(TEST, 3));

        final Map<Object, CopyAssert> beforeEntities = new HashMap<>();

        final CopyAssert copyAssert1 = new CopyAssert(new EntityTierOneType(TEST, 1));
        copyAssert1.setAsserted(true);
        beforeEntities.put(1, copyAssert1);

        final CopyAssert copyAssert2 = new CopyAssert(new EntityTierOneType());
        copyAssert2.setAsserted(true);
        beforeEntities.put(2, copyAssert2);

        final CopyAssert copyAssert3 = new CopyAssert(new EntityTierOneType(TEST, 3));
        beforeEntities.put(3, copyAssert3);

        // method
        final FabutReport report = new FabutReport();
        assertDbSnapshotWithAfterState(beforeIds, afterIds, beforeEntities, afterEntities, report);

        // assert
        assertTrue(report.isSuccess());
    }

    @Test
    public void testAssertDbSnapshotWithAfterStateFalse() {
        // setup
        final TreeSet beforeIds = new TreeSet();
        beforeIds.add(1);
        beforeIds.add(3);

        final TreeSet afterIds = new TreeSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, Object> afterEntities = new HashMap<>();
        afterEntities.put(1, new EntityTierOneType());
        afterEntities.put(3, new EntityTierOneType(TEST + TEST, 3));

        final Map<Object, CopyAssert> beforeEntities = new HashMap<>();

        final CopyAssert copyAssert1 = new CopyAssert(new EntityTierOneType(TEST, 1));
        copyAssert1.setAsserted(true);
        beforeEntities.put(1, copyAssert1);

        final CopyAssert copyAssert2 = new CopyAssert(new EntityTierOneType());
        copyAssert2.setAsserted(true);
        beforeEntities.put(2, copyAssert2);

        final CopyAssert copyAssert3 = new CopyAssert(new EntityTierOneType(TEST, 3));
        beforeEntities.put(3, copyAssert3);

        // method
        final FabutReport report = new FabutReport();
        assertDbSnapshotWithAfterState(beforeIds, afterIds, beforeEntities, afterEntities, report);

        // assert
        assertFalse(report.isSuccess());
    }


    @Test
    public void testAssertEntityWithSnapshotTrue() {
        // setup
        final List<Object> list1 = new ArrayList<>();
        list1.add(new EntityTierOneType(TEST, 1));
        setEntityTierOneTypes(list1);

        // method
        takeSnapshot();

        final EntityTierOneType entity = new EntityTierOneType(TEST + TEST, 1);
        final List<ISingleProperty> properties = new LinkedList<>();

        final List<Object> list2 = new ArrayList<>();
        list2.add(entity);
        setEntityTierOneTypes(list2);

        properties.add(value(EntityTierOneType.PROPERTY, TEST + TEST));

        final FabutReport fabutReport = new FabutReport();
        assertEntityWithSnapshot(fabutReport, entity, properties);

        // assert
        assertTrue(fabutReport.getMessage(), fabutReport.isSuccess());
    }

    @Test
    public void testAssertEntityWithSnapshotFalse() {
        // setup
        final List<Object> list1 = new ArrayList<>();
        setEntityTierOneTypes(list1);
        final EntityTierOneType entity = new EntityTierOneType(TEST + TEST, 1);
        final List<ISingleProperty> properties = new LinkedList<>();
        properties.add(value(EntityTierOneType.PROPERTY, TEST + TEST));

        // method
        takeSnapshot();
        final FabutReport report = new FabutReport();
        assertEntityWithSnapshot(report, entity, properties);

        // assert
        assertFalse(report.isSuccess());
    }
}
