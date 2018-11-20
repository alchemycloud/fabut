package cloud.alchemy.fabut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import cloud.alchemy.fabut.graph.NodesList;
import cloud.alchemy.fabut.pair.AssertPair;
import cloud.alchemy.fabut.property.CopyAssert;
import cloud.alchemy.fabut.property.ISingleProperty;
import cloud.alchemy.fabut.report.FabutReportBuilder;
import cloud.alchemy.fabut.util.ConversionUtil;
import org.junit.Test;

import cloud.alchemy.fabut.model.EntityTierOneType;
import cloud.alchemy.fabut.model.EntityTierThreeType;
import cloud.alchemy.fabut.model.EntityTierTwoType;
import cloud.alchemy.fabut.model.TierOneType;
import cloud.alchemy.fabut.model.UnknownEntityType;

/**
 * Tests for {@link FabutRepositoryAssert}.
 * 
 * @author Dusko Vesin
 * @author Nikola Olah
 * @author Bojan Babic
 * @author Nikola Trkulja
 * @author Andrej Miletic
 */
public class FabutRepositoryAssertTest extends AbstractFabutRepositoryAssertTest {

    private static final String TEST = "test";
    private static final String PROPERTY = "property";

    /**
     * Test for getFabutRepositoryAssert().assertDbState of {@link FabutRepositoryAssert} when before snapshot matches
     * after snapshot.
     */
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

        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());

        final List<Object> afterList1 = new ArrayList<Object>();
        afterList1.add(new EntityTierOneType(TEST + TEST, 1));
        setEntityTierOneTypes(afterList1);

        final List<Object> afterist2 = new ArrayList<Object>();
        afterist2.add(new EntityTierTwoType(PROPERTY + PROPERTY + PROPERTY, 4, new EntityTierOneType(TEST + TEST + TEST
                + TEST, 4)));
        setEntityTierTwoTypes(afterist2);

        // method
        final boolean assertResult = getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder());

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for getFabutRepositoryAssert().assertDbState of {@link FabutRepositoryAssert} when before snapshot doesn't
     * match after snapshot.
     */
    @Test
    public void testAssertDbStateFalse() {
        // setup

        final List<Object> beforeList1 = new ArrayList<Object>();
        beforeList1.add(new EntityTierOneType(TEST, 1));
        setEntityTierOneTypes(beforeList1);

        final List<Object> beforeList2 = new ArrayList<Object>();
        beforeList2.add(new EntityTierTwoType(PROPERTY, 4, new EntityTierOneType(TEST, 7)));
        setEntityTierTwoTypes(beforeList2);

        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());

        final List<Object> afterList1 = new ArrayList<Object>();
        afterList1.add(new EntityTierOneType(TEST + TEST, 1));
        setEntityTierOneTypes(afterList1);

        final List<Object> afterList2 = new ArrayList<Object>();
        afterList2.add(new EntityTierTwoType(PROPERTY + PROPERTY, 4, new EntityTierOneType(TEST + TEST, 7)));
        setEntityTierTwoTypes(afterList2);

        // method
        final boolean assertResult = getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder());

        // assert
        assertFalse(assertResult);
    }

    /**
     * Test for markEntityAsDeleted of {@link FabutRepositoryAssert} when entity is marked as deleted.
     */
    @Test
    public void testAssertEntityAsDeletedEntity() {
        // setup
        final List<Object> list1 = new ArrayList<Object>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());

        final List<Object> list2 = new ArrayList<Object>();
        list2.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list2);

        // method
        final boolean assertEnitityAsDeleted = getFabutRepositoryAssert().assertEntityAsDeleted(
                new FabutReportBuilder(), actual);
        final boolean assertDbState = getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder());

        // assert
        assertTrue(assertEnitityAsDeleted);
        assertTrue(assertDbState);
    }

    /**
     * Test for ignoreEntity of {@link FabutRepositoryAssert} when specified entity is ignored.
     */
    @Test
    public void testIgnoreEntityEntity() {
        // setup
        final List<Object> list1 = new ArrayList<Object>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());

        final List<Object> list2 = new ArrayList<Object>();
        list2.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list2);

        // method
        final boolean ignoreEntity = getFabutRepositoryAssert().ignoreEntity(new FabutReportBuilder(), actual);
        final boolean assertDbState = getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder());

        // assert
        assertTrue(ignoreEntity);
        assertTrue(assertDbState);
    }

    /**
     * Test for afterAssertEntity of {@link FabutRepositoryAssert} when specified object is entity and it is not
     * property.
     */
    @Test
    public void testAfterAssertEntityParentEntityNotProperty() {
        // setup
        final List<Object> list1 = new ArrayList<Object>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());

        final List<Object> list2 = new ArrayList<Object>();
        list2.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list2);

        // method
        final boolean afterAssertEntity = getFabutRepositoryAssert().afterAssertEntity(new FabutReportBuilder(),
                actual, false);
        final boolean assertDbState = getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder());

        // assert
        assertTrue(afterAssertEntity);
        assertTrue(assertDbState);
    }

    /**
     * Test for afterAssertEntity of {@link FabutRepositoryAssert} when specified object is entity and it is property of
     * another entity.
     */
    @Test(expected = AssertionError.class)
    public void testAfterAssertEntityIsProperty() {
        // setup
        final List<Object> list1 = new ArrayList<Object>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());

        final List<Object> list2 = new ArrayList<Object>();
        list2.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list2);

        // method
        getFabutRepositoryAssert().afterAssertEntity(new FabutReportBuilder(), actual, true);
        final boolean assertResult = getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder());

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for afterAssertEntity of {@link FabutRepositoryAssert} when specified object is entity and it is property of
     * another entity.
     */
    @Test
    public void testAfterAssertEntityNotEntity() {
        // setup
        final List<Object> list1 = new ArrayList<Object>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final TierOneType actual = new TierOneType();
        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());

        final List<Object> list2 = new ArrayList<Object>();
        list2.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list2);

        // method
        getFabutRepositoryAssert().afterAssertEntity(new FabutReportBuilder(), actual, false);
        final boolean assertResult = getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder());

        // assert
        assertFalse(assertResult);
    }

    /**
     * Test for afterAssertEntity of {@link FabutRepositoryAssert} when specified object is entity and it is not
     * property of another entity but its id is <code>null</code>.
     */
    @Test
    public void testAfterAssertEntityWithoutID() {
        // setup
        final List<Object> list1 = new ArrayList<Object>();
        list1.add(new EntityTierOneType(TEST, 1));
        list1.add(new EntityTierOneType(TEST, 2));
        setEntityTierOneTypes(list1);

        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());

        // method
        final boolean afterAssertEntity = getFabutRepositoryAssert().afterAssertEntity(new FabutReportBuilder(),
                actual, false);
        final boolean assertDbState = getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder());

        // assert
        assertTrue(afterAssertEntity);
        assertTrue(assertDbState);
    }

    /**
     * Test for markAsAsserted of {@link FabutRepositoryAssert} when specified type is not type supported.
     */
    @Test
    public void testMarkAssertedNotTypeSupportedTrue() {
        // setup
        final List<Object> list = new ArrayList<>();
        setEntityTierTwoTypes(list);

        // method
        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());
        list.add(new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)));
        final boolean assertValue = getFabutRepositoryAssert().markAsAsserted(new FabutReportBuilder(),
                new EntityTierThreeType(TEST, 1, new EntityTierOneType(PROPERTY, 10)), EntityTierThreeType.class);

        // assert
        assertTrue(getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder()));
        assertTrue(assertValue);
    }

    /**
     * Test for markAsAsserted of {@link FabutRepositoryAssert} when specified type and its superclass are not
     * supported.
     */
    @Test
    public void testMarkAssertedNotTyrpeSupportedFalse() {
        // method
        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());
        final boolean assertValue = getFabutRepositoryAssert().markAsAsserted(new FabutReportBuilder(),
                new UnknownEntityType(4), UnknownEntityType.class);

        // assert
        assertTrue(getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder()));
        assertFalse(assertValue);
    }

    /**
     * Test for markAsAsserted of {@link FabutRepositoryAssert} when there is no {@link CopyAssert} in db snapshot.
     */
    @Test
    public void testMarkAssertedCopyAssertNull() {
        // setup
        final List<Object> list = new ArrayList<Object>();
        setEntityTierTwoTypes(list);

        // method
        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());
        list.add(new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)));
        final boolean assertValue = getFabutRepositoryAssert().markAsAsserted(new FabutReportBuilder(),
                new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)), EntityTierTwoType.class);

        // assert
        assertTrue(getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder()));
        assertTrue(assertValue);
    }

    /**
     * Test for markAsAsserted of {@link FabutRepositoryAssert} when {@link CopyAssert} exists in db snapshot.
     */
    @Test
    public void testMarkAssertedCopyAssertNotNull() {
        // setup
        final EntityTierTwoType entity = new EntityTierThreeType(TEST, 1, new EntityTierOneType(PROPERTY, 10));
        final List<Object> list = new ArrayList<Object>();
        list.add(entity);
        setEntityTierTwoTypes(list);

        // method
        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());
        entity.setProperty(TEST + TEST);
        final boolean assertValue = getFabutRepositoryAssert().markAsAsserted(new FabutReportBuilder(),
                new EntityTierTwoType(TEST, 1, new EntityTierOneType(PROPERTY, 10)), EntityTierTwoType.class);

        // assert
        assertTrue(getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder()));
        assertTrue(assertValue);
    }

    /**
     * Test for {@link FabutRepositoryAssert#checkNotExistingInAfterDbState(TreeSet, TreeSet, Map, FabutReportBuilder)}
     * when there are more entities in after snapshot but are asserted.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testCheckNotExistingInAfterDbStateTrue() {
        // setup
        final TreeSet beforeIds = new TreeSet();
        beforeIds.add(new Integer(1));
        beforeIds.add(new Integer(2));
        beforeIds.add(new Integer(3));

        final TreeSet afterIds = new TreeSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, CopyAssert> beforeEntities = new HashMap<Object, CopyAssert>();

        final CopyAssert copyAssert1 = new CopyAssert(new EntityTierOneType());
        beforeEntities.put(new Integer(1), copyAssert1);

        final CopyAssert copyAssert2 = new CopyAssert(new EntityTierOneType());
        copyAssert2.setAsserted(true);
        beforeEntities.put(new Integer(2), copyAssert2);

        final CopyAssert copyAssert3 = new CopyAssert(new EntityTierOneType());
        beforeEntities.put(new Integer(3), copyAssert3);

        // method
        final boolean assertResult = getFabutRepositoryAssert().checkNotExistingInAfterDbState(beforeIds, afterIds,
                beforeEntities, new FabutReportBuilder());

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for {@link FabutRepositoryAssert#checkNotExistingInAfterDbState(TreeSet, TreeSet, Map, FabutReportBuilder)}
     * when one of the elements in before snapshot isn't asserted.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testCheckNotExistingInAfterDbStateFalse() {
        // setup
        final TreeSet beforeIds = new TreeSet();
        beforeIds.add(new Integer(1));
        beforeIds.add(new Integer(2));
        beforeIds.add(new Integer(3));

        final TreeSet afterIds = new TreeSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, CopyAssert> beforeEntities = new HashMap<Object, CopyAssert>();

        final CopyAssert copyAssert1 = new CopyAssert(new EntityTierOneType());
        beforeEntities.put(new Integer(1), copyAssert1);

        final CopyAssert copyAssert2 = new CopyAssert(new EntityTierOneType());
        beforeEntities.put(new Integer(2), copyAssert2);

        final CopyAssert copyAssert3 = new CopyAssert(new EntityTierOneType());
        beforeEntities.put(new Integer(3), copyAssert3);

        // method
        final boolean assertResult = getFabutRepositoryAssert().checkNotExistingInAfterDbState(beforeIds, afterIds,
                beforeEntities, new FabutReportBuilder());

        // assert
        assertFalse(assertResult);
    }

    /**
     * Test for {@link FabutRepositoryAssert#checkNewToAfterDbState(TreeSet, TreeSet, Map, FabutReportBuilder)} when
     * there new element in after snapshot.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testCheckAddedToAfterDbStateFalse() {
        // setup
        final TreeSet beforeIds = new TreeSet();
        beforeIds.add(new Integer(1));
        beforeIds.add(new Integer(2));

        final TreeSet afterIds = new TreeSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, Object> afterEntities = new HashMap<Object, Object>();
        afterEntities.put(new Integer(1), new EntityTierOneType());
        afterEntities.put(new Integer(3), new EntityTierOneType());

        // method
        final boolean assertResult = getFabutRepositoryAssert().checkNewToAfterDbState(beforeIds, afterIds,
                afterEntities, new FabutReportBuilder());

        // assert
        assertFalse(assertResult);
    }

    /**
     * Test for {@link FabutRepositoryAssert#checkNewToAfterDbState(TreeSet, TreeSet, Map, FabutReportBuilder)} when
     * there new element in after snapshot.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testCheckAddedToAfterDbStateTrue() {
        // setup
        final TreeSet beforeIds = new TreeSet();
        beforeIds.add(new Integer(1));
        beforeIds.add(new Integer(3));

        final TreeSet afterIds = new TreeSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, Object> afterEntities = new HashMap<Object, Object>();
        afterEntities.put(new Integer(1), new EntityTierOneType());
        afterEntities.put(new Integer(3), new EntityTierOneType());

        // method
        final boolean assertResult = getFabutRepositoryAssert().checkNewToAfterDbState(beforeIds, afterIds,
                afterEntities, new FabutReportBuilder());

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for {@link FabutRepositoryAssert#getFabutRepositoryAssert().assertDbSnapshotWithAfterState(TreeSet, TreeSet,
     * Map, Map, FabutReportBuilder)} when snapshots don't match.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testAssertDbSnapshotWithAfterStateTrue() {
        // setup
        final TreeSet beforeIds = new TreeSet();
        beforeIds.add(new Integer(1));
        beforeIds.add(new Integer(3));

        final TreeSet afterIds = new TreeSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, Object> afterEntities = new HashMap<Object, Object>();
        afterEntities.put(new Integer(1), new EntityTierOneType());
        afterEntities.put(new Integer(3), new EntityTierOneType(TEST, new Integer(3)));

        final Map<Object, CopyAssert> beforeEntities = new HashMap<Object, CopyAssert>();

        final CopyAssert copyAssert1 = new CopyAssert(new EntityTierOneType(TEST, new Integer(1)));
        copyAssert1.setAsserted(true);
        beforeEntities.put(new Integer(1), copyAssert1);

        final CopyAssert copyAssert2 = new CopyAssert(new EntityTierOneType());
        copyAssert2.setAsserted(true);
        beforeEntities.put(new Integer(2), copyAssert2);

        final CopyAssert copyAssert3 = new CopyAssert(new EntityTierOneType(TEST, new Integer(3)));
        beforeEntities.put(new Integer(3), copyAssert3);

        // method
        final boolean assertResult = getFabutRepositoryAssert().assertDbSnapshotWithAfterState(beforeIds, afterIds,
                beforeEntities, afterEntities, new FabutReportBuilder());

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for {@link FabutRepositoryAssert#getFabutRepositoryAssert().assertDbSnapshotWithAfterState(TreeSet, TreeSet,
     * Map, Map, FabutReportBuilder)} when db snapshot matches after db state.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testAssertDbSnapshotWithAfterStateFalse() {
        // setup
        final TreeSet beforeIds = new TreeSet();
        beforeIds.add(new Integer(1));
        beforeIds.add(new Integer(3));

        final TreeSet afterIds = new TreeSet();
        afterIds.add(1);
        afterIds.add(3);

        final Map<Object, Object> afterEntities = new HashMap<Object, Object>();
        afterEntities.put(new Integer(1), new EntityTierOneType());
        afterEntities.put(new Integer(3), new EntityTierOneType(TEST + TEST, new Integer(3)));

        final Map<Object, CopyAssert> beforeEntities = new HashMap<Object, CopyAssert>();

        final CopyAssert copyAssert1 = new CopyAssert(new EntityTierOneType(TEST, new Integer(1)));
        copyAssert1.setAsserted(true);
        beforeEntities.put(new Integer(1), copyAssert1);

        final CopyAssert copyAssert2 = new CopyAssert(new EntityTierOneType());
        copyAssert2.setAsserted(true);
        beforeEntities.put(new Integer(2), copyAssert2);

        final CopyAssert copyAssert3 = new CopyAssert(new EntityTierOneType(TEST, new Integer(3)));
        beforeEntities.put(new Integer(3), copyAssert3);

        // method
        final boolean assertResult = getFabutRepositoryAssert().assertDbSnapshotWithAfterState(beforeIds, afterIds,
                beforeEntities, afterEntities, new FabutReportBuilder());

        // assert
        assertFalse(assertResult);
    }

    /**
     * Test for {@link FabutRepositoryAssert#assertPair(String, FabutReportBuilder, AssertPair, List, NodesList)} when
     * entities are not properties and can be asserted.
     */
    @Test
    public void testAssertPairNotPropertyAsserted() {
        // setup
        final AssertPair entityPair = ConversionUtil.createAssertPair(new EntityTierOneType(TEST, new Integer(1)),
                new EntityTierOneType(TEST, new Integer(1)), getFabutRepositoryAssert().getTypes());

        // method
        final boolean assertResult = getFabutRepositoryAssert().assertPair("", new FabutReportBuilder(), entityPair,
                new LinkedList<ISingleProperty>(), new NodesList());

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for {@link FabutRepositoryAssert#assertPair(String, FabutReportBuilder, AssertPair, List, NodesList)} when
     * entities are not properties and assert fails.
     */
    @Test
    public void testAssertPairNotPropertyAssertFail() {
        // setup
        final AssertPair entityPair = ConversionUtil.createAssertPair(
                new EntityTierOneType(TEST + TEST, new Integer(1)), new EntityTierOneType(TEST, new Integer(1)),
                getFabutRepositoryAssert().getTypes());

        // method
        final boolean assertResult = getFabutRepositoryAssert().assertPair("", new FabutReportBuilder(), entityPair,
                new LinkedList<>(), new NodesList());

        // assert
        assertFalse(assertResult);
    }

    /**
     * Test for {@link FabutRepositoryAssert#assertPair(String, FabutReportBuilder, AssertPair, List, NodesList)} when
     * entities are properties and can be asserted.
     */
    @Test
    public void testAssertPairPropertyAsserted() {
        // setup
        final AssertPair entityPair = ConversionUtil.createAssertPair(new EntityTierOneType(TEST, new Integer(1)),
                new EntityTierOneType(TEST, new Integer(1)), getFabutRepositoryAssert().getTypes());
        entityPair.setProperty(true);

        // method
        final boolean assertResult = getFabutRepositoryAssert().assertPair("", new FabutReportBuilder(), entityPair,
                new LinkedList<ISingleProperty>(), new NodesList());

        // assert
        assertTrue(assertResult);
    }

    /**
     * Test for {@link FabutRepositoryAssert#assertPair(String, FabutReportBuilder, AssertPair, List, NodesList)} when
     * entities are properties and assert fails.
     */
    @Test
    public void testAssertPairPropertyAssertFail() {
        // setup
        final AssertPair entityPair = ConversionUtil.createAssertPair(
                new EntityTierOneType(TEST + TEST, new Integer(1)), new EntityTierOneType(TEST, new Integer(2)),
                getFabutRepositoryAssert().getTypes());
        entityPair.setProperty(true);

        // method
        final boolean assertResult = getFabutRepositoryAssert().assertPair("", new FabutReportBuilder(), entityPair,
                new LinkedList<ISingleProperty>(), new NodesList());

        // assert
        assertFalse(assertResult);
    }

    /**
     * Test for {@link FabutRepositoryAssert#assertEntityWithSnapshot(FabutReportBuilder, Object, List)} when specified
     * entity can be asserted.
     */
    @Test
    public void testAssertEntityWithSnapshotTrue() {
        // setup
        final List<Object> list1 = new ArrayList<Object>();
        list1.add(new EntityTierOneType(TEST, 1));
        setEntityTierOneTypes(list1);
        final EntityTierOneType entity = new EntityTierOneType(TEST + TEST, new Integer(1));
        final List<ISingleProperty> properties = new LinkedList<ISingleProperty>();
        properties.add(Fabut.value(EntityTierOneType.PROPERTY, TEST + TEST));

        // method
        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());
        final boolean assertEntityWithSnapshot = getFabutRepositoryAssert().assertEntityWithSnapshot(
                new FabutReportBuilder(), entity, properties);

        // assert
        assertTrue(assertEntityWithSnapshot);
        assertTrue(getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder()));

    }

    /**
     * Test for {@link FabutRepositoryAssert#assertEntityWithSnapshot(FabutReportBuilder, Object, List)} when specified
     * entity cannot be asserted.
     */
    @Test
    public void testAssertEntityWithSnapshotFalse() {
        // setup
        final List<Object> list1 = new ArrayList<Object>();
        setEntityTierOneTypes(list1);
        final EntityTierOneType entity = new EntityTierOneType(TEST + TEST, new Integer(1));
        final List<ISingleProperty> properties = new LinkedList<ISingleProperty>();
        properties.add(Fabut.value(EntityTierOneType.PROPERTY, TEST + TEST));

        // method
        getFabutRepositoryAssert().takeSnapshot(new FabutReportBuilder());
        final boolean assertEntityWithSnapshot = getFabutRepositoryAssert().assertEntityWithSnapshot(
                new FabutReportBuilder(), entity, properties);

        // assert
        assertFalse(assertEntityWithSnapshot);
        assertTrue(getFabutRepositoryAssert().assertDbSnapshot(new FabutReportBuilder()));

    }
}
