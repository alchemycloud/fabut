package cloud.alchemy.fabut;

import cloud.alchemy.fabut.enums.EntityChangeType;
import cloud.alchemy.fabut.model.*;
import cloud.alchemy.fabut.property.CopyAssert;
import cloud.alchemy.fabut.property.ISingleProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

public class FabutRepositoryAssertTest extends AbstractFabutTest {

    private static final String TEST = "test";
    private static final String PROPERTY = "property";

    // mock lists
    private List<Object> entityTierOneTypes = new ArrayList<>();
    private List<Object> entityTierTwoTypes = new ArrayList<>();
    private final List<Object> entityWithListTypes = new ArrayList<>();
    private final List<Object> noDefaultConstructorEntities = new ArrayList<>();
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
        complexTypes.add(TypeWithOptionalEntity.class);

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

    @BeforeEach
    @Override
    public void before() {
        super.before();
        assertAfterTest = true;
    }

    @AfterEach
    @Override
    public void after() {
        if (assertAfterTest) {
            super.after();
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

        // assert - CODE only shows changed properties
        assertFabutReportFailure(report, """
UPDATED: EntityTierOneType[id=1]
--■>property: expected: test but was: testtest
CODE:
assertEntityWithSnapshot(object,
value(EntityTierOneType.PROPERTY, "test"));
UPDATED: EntityTierTwoType[id=4]
--■>property: expected: test but was: testtest
--■>property: expected: property but was: propertyproperty
CODE:
assertEntityWithSnapshot(object,
value(EntityTierTwoType.SUB_PROPERTY, EntityTierOneType[id=7]),
value(EntityTierOneType.SUB_PROPERTY.chain(EntityTierOneType.PROPERTY), "test"),
value(EntityTierTwoType.PROPERTY, "property"));""");
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
        assertFabutReportSuccess(assertEntityAsDeleted);

        final FabutReport report = new FabutReport();
        assertDbSnapshot(report);
        assertFabutReportSuccess(report);
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
        assertFabutReportSuccess(ignoreEntityReport);
        assertFabutReportSuccess(report);
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
        assertFabutReportSuccess(assertReport);
        assertFabutReportSuccess(report);
    }

    @Test
    public void testAfterAssertEntityIsProperty() {
        assertThrows(
                AssertionError.class,
                () -> {
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
                    super.after();
                });
    }

    @Test
    public void testAfterAssertEntityNotEntity() {
        assertThrows(
                AssertionError.class,
                () -> {
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
                    super.after();
                });
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
        assertFabutReportSuccess(entityAssertReport);
    }

    @Test
    public void testMarkAssertedNotTyrpeSupportedFalse() {
        assertThrows(
                NullPointerException.class,
                () -> {
                    // method
                    takeSnapshot();
                    final FabutReport report = new FabutReport();
                    markAsAsserted(report, new UnknownEntityType(4));

                    // assert
                    assertFabutReportSuccess(report);
                });
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
        assertFabutReportSuccess(report);
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
        assertFabutReportSuccess(report);
    }

    @Test
    public void testCheckNotExistingInAfterDbStateTrue() {
        // setup
        final HashSet beforeIds = new HashSet();
        beforeIds.add(1);
        beforeIds.add(2);
        beforeIds.add(3);

        final HashSet afterIds = new HashSet();
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
        assertFabutReportSuccess(report);
    }

    @Test
    public void testCheckNotExistingInAfterDbStateFalse() {
        // setup
        final HashSet beforeIds = new HashSet();
        beforeIds.add(1);
        beforeIds.add(2);
        beforeIds.add(3);

        final HashSet afterIds = new HashSet();
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
        assertFabutReportFailure(report, """
DELETED:
  EntityTierOneType[id=null]
    -> assertEntityAsDeleted(entityTierOneType);""");
    }

    @Test
    public void testCheckAddedToAfterDbStateFalse() {
        // setup
        final HashSet beforeIds = new HashSet();
        beforeIds.add(1);
        beforeIds.add(2);

        final HashSet afterIds = new HashSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, Object> afterEntities = new HashMap<>();
        afterEntities.put(1, new EntityTierOneType());
        afterEntities.put(3, new EntityTierOneType());

        // method
        final FabutReport report = new FabutReport();
        checkNewToAfterDbState(beforeIds, afterIds, afterEntities, report);

        // assert
        assertFabutReportFailure(report, """
CREATED:
  EntityTierOneType[id=null]
CODE:
assertObject(object,
isNull(EntityTierOneType.PROPERTY),
isNull(EntityTierOneType.ID));""");
    }

    @Test
    public void testCheckAddedToAfterDbStateWithEntityReference() {
        // setup - EntityTierTwoType has a subProperty that references EntityTierOneType
        final HashSet beforeIds = new HashSet();
        beforeIds.add(1);

        final HashSet afterIds = new HashSet();
        afterIds.add(1);
        afterIds.add(2);

        final EntityTierOneType referencedEntity = new EntityTierOneType(TEST, 5);
        final Map<Object, Object> afterEntities = new HashMap<>();
        afterEntities.put(1, new EntityTierTwoType(PROPERTY, 1, null));
        afterEntities.put(2, new EntityTierTwoType(PROPERTY, 2, referencedEntity));

        // method
        final FabutReport report = new FabutReport();
        checkNewToAfterDbState(beforeIds, afterIds, afterEntities, report);

        // assert - entity reference should use entityPath format
        assertFabutReportFailure(report, """
CREATED:
  EntityTierTwoType[id=2]
CODE:
assertObject(object,
value(EntityTierTwoType.SUB_PROPERTY, EntityTierOneType[id=5]),
value(EntityTierTwoType.PROPERTY, "property"),
value(EntityTierTwoType.ID, 2));""");
    }

    @Test
    public void testCheckAddedToAfterDbStateTrue() {
        // setup
        final HashSet beforeIds = new HashSet();
        beforeIds.add(1);
        beforeIds.add(3);

        final HashSet afterIds = new HashSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, Object> afterEntities = new HashMap<>();
        afterEntities.put(1, new EntityTierOneType());
        afterEntities.put(3, new EntityTierOneType());

        // method
        final FabutReport report = new FabutReport();
        checkNewToAfterDbState(beforeIds, afterIds, afterEntities, report);

        // assert
        assertFabutReportSuccess(report);
    }

    @Test
    public void testAssertDbSnapshotWithAfterStateTrue() {
        // setup
        final HashSet beforeIds = new HashSet();
        beforeIds.add(1);
        beforeIds.add(3);

        final HashSet afterIds = new HashSet();
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
        assertFabutReportSuccess(report);
    }

    @Test
    public void testAssertDbSnapshotWithAfterStateFalse() {
        // setup
        final HashSet beforeIds = new HashSet();
        beforeIds.add(1);
        beforeIds.add(3);

        final HashSet afterIds = new HashSet();
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

        // assert - CODE only shows changed property (not ID which didn't change)
        assertFabutReportFailure(report, """
UPDATED: EntityTierOneType[id=3]
--■>property: expected: test but was: testtest
CODE:
assertEntityWithSnapshot(object,
value(EntityTierOneType.PROPERTY, "test"));""");
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
        assertFabutReportSuccess(fabutReport);
    }

    @Test
    public void testAssertEntityWithSnapshotFalse_noNeedForAssert() {
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
        properties.add(value(EntityTierOneType.ID, 1));

        final FabutReport fabutReport = new FabutReport();
        assertEntityWithSnapshot(fabutReport, entity, properties);

        // assert
        assertFabutReportFailure(
                fabutReport,
                "■>Property: id is same in expected and actual object, no need for assert\n"
                        + "CODE:\n"
                        + "assertObject(object,\n"
                        + "value(EntityTierOneType.PROPERTY, \"test\"),\n"
                        + "value(EntityTierOneType.ID, 1));");
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
        assertFabutReportFailure(report, "■>Entity EntityTierOneType{id=1, property='testtest'} doesn't exist in DB any more but is not asserted in test.");
    }

    @Test
    public void testAssertOptionalEntityFormattingExpectedEmptyActualPresent() {
        // setup
        assertAfterTest = false;
        final TypeWithOptionalEntity expected = new TypeWithOptionalEntity(Optional.empty());
        final TypeWithOptionalEntity actual = new TypeWithOptionalEntity(Optional.of(new EntityTierOneType("test", 123)));

        // method
        final FabutReport report = new FabutReport();
        assertObjects(report, expected, actual, new LinkedList<>());

        // assert
        assertFabutReportFailure(report,
                "■>optionalEntity: expected: Optional.empty but was: Optional[EntityTierOneType[id=123]]\n" +
                "CODE:\n" +
                "assertObject(object,\n" +
                "isEmpty(TypeWithOptionalEntity.OPTIONAL_ENTITY));");
    }

    @Test
    public void testAssertOptionalEntityFormattingExpectedPresentActualEmpty() {
        // setup
        assertAfterTest = false;
        final TypeWithOptionalEntity expected = new TypeWithOptionalEntity(Optional.of(new EntityTierOneType("test", 456)));
        final TypeWithOptionalEntity actual = new TypeWithOptionalEntity(Optional.empty());

        // method
        final FabutReport report = new FabutReport();
        assertObjects(report, expected, actual, new LinkedList<>());

        // assert
        assertFabutReportFailure(report,
                "■>optionalEntity: expected: Optional[EntityTierOneType[id=456]] but was: Optional.empty\n" +
                "CODE:\n" +
                "assertObject(object,\n" +
                "value(TypeWithOptionalEntity.OPTIONAL_ENTITY, Optional[EntityTierOneType{id=456, property='test'}]));");
    }

    @Test
    public void testEntityChangesInSubreportAppearInParentMessage() {
        // setup - create parent report with subreport containing entity changes
        assertAfterTest = false;
        final FabutReport parentReport = new FabutReport(() -> "Parent report");

        // Create subreport (simulates "Repository snapshot assert" structure)
        final FabutReport subReport = parentReport.getSubReport(() -> "Repository snapshot assert");

        // Record entity change in the subreport (simulates deleted entity detection)
        subReport.recordEntityChange(
                EntityChangeType.DELETED,
                "EntityTierOneType[id=123]",
                EntityTierOneType.class,
                "Entity was deleted",
                "assertEntityAsDeleted(entityTierOneType);"
        );

        // method - get message from parent report
        final String message = parentReport.getMessage();

        // assert - subreport entity changes must appear in parent message
        assertTrue(message.contains("DELETED:") && message.contains("EntityTierOneType[id=123]"),
                "Parent message should contain subreport's DELETED entity change. Actual message:\n" + message);
        assertTrue(message.contains("assertEntityAsDeleted(entityTierOneType);"),
                "Parent message should contain subreport's suggested fix. Actual message:\n" + message);
    }
}
