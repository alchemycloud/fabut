package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.*;
import cloud.alchemy.fabut.model.test.Address;
import cloud.alchemy.fabut.model.test.Faculty;
import cloud.alchemy.fabut.model.test.Student;
import cloud.alchemy.fabut.model.test.Teacher;
import cloud.alchemy.fabut.property.*;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.util.*;

public class FabutTest extends AbstractFabutRepositoryAssertTest {

    private static final String TEST = "test";

    /**
     * Test for ignored when varargs are passed.
     */
    @Test
    public void testIgnoredVarargs() {
        // setup
        final PropertyPath[] properties = new PropertyPath[]{EntityTierOneType.PROPERTY, EntityTierOneType.ID};

        // method
        final MultiProperties multi = Fabut.ignored(properties);

        // assert
        assertEquals(properties.length, multi.getProperties().size());

        for (int i = 0; i < properties.length; i++) {
            assertTrue(multi.getProperties().get(i) instanceof IgnoredProperty);
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

    /**
     * Test for null when varargs are passed.
     */
    @Test
    public void testNulllVarargs() {
        // setup
        final PropertyPath[] properties = new PropertyPath[]{EntityTierOneType.PROPERTY, EntityTierOneType.ID};

        // method
        final MultiProperties multi = Fabut.isNull(properties);

        // assert
        assertEquals(properties.length, multi.getProperties().size());

        for (int i = 0; i < properties.length; i++) {
            assertTrue(multi.getProperties().get(i) instanceof NullProperty);
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

    /**
     * Test for notNull when varargs are passed.
     */
    @Test
    public void testNotNullVarargs() {
        // setup
        final PropertyPath[] properties = new PropertyPath[]{EntityTierOneType.PROPERTY, EntityTierOneType.ID};

        // method
        final MultiProperties multi = Fabut.notNull(properties);

        // assert
        assertEquals(properties.length, multi.getProperties().size());

        for (int i = 0; i < properties.length; i++) {
            assertTrue(multi.getProperties().get(i) instanceof NotNullProperty);
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

    /**
     * Test for {@link Fabut#notNull(PropertyPath)}.
     */
    @Test
    public void testNotNull() {
        // method
        final NotNullProperty notNullProperty = Fabut.notNull(EntityTierOneType.PROPERTY);

        // assert
        assertEquals(EntityTierOneType.PROPERTY.getPath(), notNullProperty.getPath());
    }

    /**
     * Test for {@link Fabut#isNull(PropertyPath)}.
     */
    @Test
    public void testNulll() {
        // method
        final NullProperty nullProperty = Fabut.isNull(EntityTierOneType.PROPERTY);

        // assert
        assertEquals(EntityTierOneType.PROPERTY.getPath(), nullProperty.getPath());
    }

    /**
     * Test for {@link Fabut#ignored(PropertyPath)}.
     */
    @Test
    public void testIgnored() {
        // method
        final IgnoredProperty ignoreProperty = Fabut.ignored(EntityTierOneType.PROPERTY);

        // assert
        assertEquals(EntityTierOneType.PROPERTY.getPath(), ignoreProperty.getPath());
    }

    /**
     * Test for {@link Fabut#value(PropertyPath, Object)}.
     */
    @Test
    public void testValue() {
        // method
        final Property<String> changedProperty = Fabut.value(EntityTierOneType.PROPERTY, TEST);

        // assert
        assertEquals(TEST, changedProperty.getValue());
        assertEquals(EntityTierOneType.PROPERTY.getPath(), changedProperty.getPath());
    }

    /**
     * Test for {@link Fabut#beforeTest(Object)} if it throws {@link IllegalStateException} if specified test instance
     * doesn't implement {@link IFabutTest} or {@link IFabutRepositoryTest}.
     */
    @Test(expected = IllegalStateException.class)
    public void testBeforeTest() {
        // method
        Fabut.beforeTest(new Object());
    }

    /**
     * Test for {@link Fabut#afterTest()} when snapshot matches after state.
     */
    @Test
    public void testAfterTestSucess() {
        // setup
        Fabut.beforeTest(this);
        Fabut.takeSnapshot();

        // method
        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#afterTest()} when snapshot doesn't match after state.
     */
    @Test(expected = AssertionFailedError.class)
    public void testAfterTestFail() {
        // setup
        Fabut.beforeTest(this);
        Fabut.takeSnapshot();
        final EntityTierOneType entityTierOneType = new EntityTierOneType("test", 1);
        getEntityTierOneTypes().add(entityTierOneType);

        // method
        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#takeSnapshot(Object...)} when there are entites in repository that cannot be copied.
     */
    @Test(expected = AssertionFailedError.class)
    public void testTakeSnapshotFail() {
        // setup
        Fabut.beforeTest(this);
        getNoDefaultConstructorEntities().add(new NoDefaultConstructorEntity("test", 1));

        // method
        Fabut.takeSnapshot();
    }

    /**
     * Test for {@link Fabut#takeSnapshot(Object...)} when repository can be copied.
     */
    @Test
    public void testTakeSnapshotSuccess() {
        // setup
        Fabut.beforeTest(this);
        final EntityTierOneType entityTierOneType = new EntityTierOneType("test", 1);
        getEntityTierOneTypes().add(entityTierOneType);
        Fabut.takeSnapshot();

        // method
        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertObject(Object, IProperty...)} when object is complex type
     * and can be asserted.
     */
    @Test
    public void testAssertObjectWithComplexType() {
        // setup
        Fabut.beforeTest(this);
        final TierOneType object = new TierOneType("test");

        // method
        Fabut.assertObject(object, Fabut.value(TierOneType.PROPERTY, "test"));

    }

    /**
     * Test for {@link Fabut#assertObject(Object, IProperty...)} when object is entity type and
     * can be asserted.
     */
    @Test
    public void testAssertObjectWithEntityType() {
        // setup
        Fabut.beforeTest(this);
        final EntityTierOneType entity = new EntityTierOneType();
        entity.setProperty("test");
        entity.setId(1);

        // method
        Fabut.takeSnapshot();
        getEntityTierOneTypes().add(entity);
        Fabut.assertObject(entity, Fabut.value(EntityTierOneType.ID, 1),
                Fabut.value(EntityTierOneType.PROPERTY, "test"));

        Fabut.afterTest();

    }

    /**
     * Test for {@link Fabut#assertObject(Object, IProperty...)} when object is entity type with list field and
     * can be asserted.
     */
    @Test
    public void testAssertObjectWithEntityTypeWithListField() {
        // setup
        Fabut.beforeTest(this);
        final EntityWithList entity = new EntityWithList();
        entity.setList(Arrays.asList(new EntityTierOneType("test", 1)));
        entity.setId(1);

        // method
        Fabut.takeSnapshot();
        getEntityWithListTypes().add(entity);
        Fabut.assertObject(entity, Fabut.value(EntityWithList.ID, 1));

        Fabut.afterTest();

    }

    /**
     * Test for {@link Fabut#assertObject(Object, IProperty...)} when object is entity and
     * cannot be asserted.
     */
    @Test(expected = AssertionFailedError.class)
    public void testAssertObjectWithEntityTypeFail() {
        // setup
        Fabut.beforeTest(this);
        final EntityTierOneType entity = new EntityTierOneType();
        entity.setProperty("test");
        entity.setId(1);

        // method
        Fabut.takeSnapshot();
        getEntityTierOneTypes().add(entity);
        Fabut.assertObject(entity, Fabut.value(EntityTierOneType.ID, 1),
                Fabut.value(EntityTierOneType.PROPERTY, "fail"));

    }

    /**
     * Test for {@link Fabut#assertObjects(Object, Object, IProperty...)} when specified
     * objects are complex types and can be asserted.
     */
    @Test
    public void testAssertObjectsComplexSuccess() {
        // setup
        Fabut.beforeTest(this);
        final TierOneType expected = new TierOneType(TEST);
        final TierOneType actual = new TierOneType(TEST);

        // method
        Fabut.assertObjects(expected, actual);
        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertObjects(Object, Object, IProperty...)} when specified
     * objects are complex types and assert fails.
     */
    @Test(expected = AssertionFailedError.class)
    public void testAssertObjectsComplexFail() {
        // setup
        Fabut.beforeTest(this);
        final TierOneType expected = new TierOneType(TEST);
        final TierOneType actual = new TierOneType(TEST + TEST);

        // method
        Fabut.assertObjects(expected, actual);
        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertObjects(Object, Object, IProperty...)} when specified
     * objects are complex types and property difference is used for assert.
     */
    @Test
    public void testAssertObjectsComplexWithPropertyDifference() {
        // setup
        Fabut.beforeTest(this);
        final TierOneType expected = new TierOneType(TEST);
        final TierOneType actual = new TierOneType(TEST + TEST);

        // method
        Fabut.assertObjects(expected, actual, Fabut.value(TierOneType.PROPERTY, TEST + TEST));
        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertList(List, Object...)} when specified objects are lists and they can be asserted.
     */
    @Test
    public void testAssertObjectsListSuccess() {
        // setup
        Fabut.beforeTest(this);

        final List<Object> expected = new LinkedList<Object>();
        expected.add(new TierOneType(TEST));
        expected.add(new TierOneType(TEST + TEST));

        // method
        Fabut.assertList(expected, new TierOneType(TEST), new TierOneType(TEST + TEST));
        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertList(List, Object...)} when specified objects are lists and they cannot be asserted.
     */
    @Test(expected = AssertionFailedError.class)
    public void testAssertObjectsListFail() {
        // setup
        Fabut.beforeTest(this);

        final List<Object> expected = new LinkedList<Object>();
        expected.add(new TierOneType(TEST));
        expected.add(new TierOneType(TEST + TEST));

        // method
        Fabut.assertList(expected, new TierOneType(TEST), new TierOneType(TEST));
        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertObjects(Object, Object, IProperty...)} when specified
     * objects are entities and can be asserted.
     */
    @Test
    public void testAssertObjectsEntitySuccess() {
        // setup
        Fabut.beforeTest(this);

        final EntityTierOneType expected = new EntityTierOneType(TEST, 1);
        final EntityTierOneType actual = new EntityTierOneType(TEST, 1);

        // method
        Fabut.takeSnapshot();
        getEntityTierOneTypes().add(actual);
        Fabut.assertObjects(expected, actual);

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertObjects(Object, Object, IProperty...)} when specified
     * objects are entities and cannot be asserted.
     */
    @Test(expected = AssertionFailedError.class)
    public void testAssertObjectsEntityFail() {
        // setup
        Fabut.beforeTest(this);

        final EntityTierOneType expected = new EntityTierOneType(TEST, 1);
        final EntityTierOneType actual = new EntityTierOneType(TEST + TEST, 1);

        // method
        Fabut.takeSnapshot();
        getEntityTierOneTypes().add(actual);
        Fabut.assertObjects(expected, actual);

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertEntityWithSnapshot(Object, IProperty...)} when entity can be
     * asserted with one in snapshot.
     */
    @Test
    public void testAssertEntityWithSnapshotSuccess() {
        // setup
        Fabut.beforeTest(this);
        final EntityTierOneType entityTierOneType = new EntityTierOneType();
        entityTierOneType.setId(10);
        entityTierOneType.setProperty("property");
        getEntityTierOneTypes().add(entityTierOneType);
        Fabut.takeSnapshot();

        // method
        ((EntityTierOneType) getEntityTierOneTypes().get(0)).setProperty("test");
        Fabut.assertEntityWithSnapshot(entityTierOneType, Fabut.value(EntityTierOneType.PROPERTY, "test"));

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertEntityWithSnapshot(Object, IProperty...)} when entity cannot
     * be asserted with one in snapshot.
     */
    @Test(expected = AssertionFailedError.class)
    public void testAssertEntityWithSnapshotFail() {
        // setup
        Fabut.beforeTest(this);
        final EntityTierOneType entityTierOneType = new EntityTierOneType();
        entityTierOneType.setId(10);
        entityTierOneType.setProperty("property");
        getEntityTierOneTypes().add(entityTierOneType);
        Fabut.takeSnapshot();

        // method
        ((EntityTierOneType) getEntityTierOneTypes().get(0)).setProperty("test");
        Fabut.assertEntityWithSnapshot(entityTierOneType, Fabut.value(EntityTierOneType.PROPERTY, "testtest"));

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertEntityWithSnapshot(Object, IProperty...)} when specified
     * object is not an entity.
     */
    @Test(expected = IllegalStateException.class)
    public void testAssertEntityWithSnapshotNotEntity() {
        // setup
        Fabut.beforeTest(this);
        Fabut.takeSnapshot();

        // method
        Fabut.assertEntityWithSnapshot(new TierOneType());
    }

    @Test(expected = NullPointerException.class)
    public void testAssertEntityWithSnapshotNullEntity() {
        // setup
        Fabut.beforeTest(this);
        final EntityTierOneType entityTierOneType = new EntityTierOneType();
        entityTierOneType.setId(10);
        entityTierOneType.setProperty("property");
        getEntityTierOneTypes().add(entityTierOneType);
        Fabut.takeSnapshot();

        // method
        ((EntityTierOneType) getEntityTierOneTypes().get(0)).setProperty("test");
        Fabut.assertEntityWithSnapshot(null, Fabut.value(EntityTierOneType.PROPERTY, "test"));

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#markAsserted(Object)} when entity can be marked as asserted.
     */
    @Test
    public void testMarkAssertedSuccess() {
        // setup
        Fabut.beforeTest(this);
        final EntityTierOneType entity = new EntityTierOneType(TEST, 1);
        Fabut.takeSnapshot();
        getEntityTierOneTypes().add(entity);

        // method
        Fabut.markAsserted(entity);

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#markAsserted(Object)} when entity cannot be marked as asserted.
     */
    @Test(expected = AssertionFailedError.class)
    public void testMarkAssertedFail() {
        // setup
        Fabut.beforeTest(this);
        final EntityTierOneType entity = new EntityTierOneType(TEST, null);
        Fabut.takeSnapshot();
        getEntityTierOneTypes().add(entity);

        // method
        Fabut.markAsserted(entity);

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#markAsserted(Object)} when object is not entity.
     */
    @Test(expected = IllegalStateException.class)
    public void testMarkAssertedNotEntity() {
        // setup
        Fabut.beforeTest(this);
        final TierOneType object = new TierOneType();
        Fabut.takeSnapshot();

        // method
        Fabut.markAsserted(object);

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertEntityAsDeleted(Object)} when specified entity is successfully asserted as deleted.
     */
    @Test
    public void assertEntityAsDeletedSuccess() {
        // setup
        Fabut.beforeTest(this);
        final EntityTierOneType entity = new EntityTierOneType(TEST, 1);
        getEntityTierOneTypes().add(entity);
        Fabut.takeSnapshot();

        // method
        getEntityTierOneTypes().remove(0);
        Fabut.assertEntityAsDeleted(entity);

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertEntityAsDeleted(Object)} when specified entity is not deleted in repository.
     */
    @Test(expected = AssertionFailedError.class)
    public void assertEntityAsDeletedFail() {
        // setup
        Fabut.beforeTest(this);
        final EntityTierOneType entity = new EntityTierOneType(TEST, 1);
        getEntityTierOneTypes().add(entity);
        Fabut.takeSnapshot();

        // method
        Fabut.assertEntityAsDeleted(entity);

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#assertEntityAsDeleted(Object)} when specified object is not entity.
     */
    @Test(expected = IllegalStateException.class)
    public void assertEntityAsDeletedNotEntity() {
        // setup
        Fabut.beforeTest(this);
        final TierOneType object = new TierOneType();
        Fabut.takeSnapshot();

        // method
        Fabut.assertEntityAsDeleted(object);

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#ignoreEntity(Object)} when entity can ignored.
     */
    @Test
    public void testIgnoreEntitySuccess() {
        // setup
        Fabut.beforeTest(this);
        final EntityTierOneType entity = new EntityTierOneType(TEST, 1);
        Fabut.takeSnapshot();
        getEntityTierOneTypes().add(entity);

        // method
        Fabut.ignoreEntity(entity);

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#ignoreEntity(Object)} when entity cannot be ignored.
     */
    @Test(expected = AssertionFailedError.class)
    public void testIgnoreEntityFail() {
        // setup
        Fabut.beforeTest(this);
        final EntityTierOneType entity = new EntityTierOneType(TEST, null);
        Fabut.takeSnapshot();
        getEntityTierOneTypes().add(entity);

        // method
        Fabut.ignoreEntity(entity);

        Fabut.afterTest();
    }

    /**
     * Test for {@link Fabut#ignoreEntity(Object)} when object is not entity.
     */
    @Test(expected = IllegalStateException.class)
    public void testIgnoreEntityNotEntity() {
        // setup
        Fabut.beforeTest(this);
        final TierOneType object = new TierOneType();
        Fabut.takeSnapshot();

        // method
        Fabut.ignoreEntity(object);

        Fabut.afterTest();
    }

    /**
     * Integration test for {@link Fabut#assertObject(Object, IProperty...)} when inner
     * properties are used for asserting.
     */
    @Test
    public void testAssertObject() {
        // method
        Fabut.beforeTest(this);
        final Student student = new Student();
        student.setName("Nikola");
        student.setLastName("Olah");
        final Address address1 = new Address();
        address1.setCity("Temerin");
        address1.setStreet("Novosadska");
        address1.setStreetNumber("627");
        student.setAddress(address1);
        final Faculty faculty = new Faculty();
        faculty.setName("PMF");
        student.setFaculty(faculty);
        final Teacher teacher = new Teacher();
        teacher.setName("Djura");
        faculty.setTeacher(teacher);
        final Address address2 = new Address();
        address2.setCity("Kamenica");
        address2.setStreet("Ljubicica");
        address2.setStreetNumber("10");
        teacher.setAddress(address2);
        teacher.setStudent(student);
        Fabut.takeSnapshot();

        // assert
        Fabut.assertObject(student,
                Fabut.value(new PropertyPath<>("name"), "Nikola"),
                Fabut.value(new PropertyPath<>("lastName"), "Olah"),
                Fabut.value(new PropertyPath<>("address.city"), "Temerin"),
                Fabut.value(new PropertyPath<>("address.street"), "Novosadska"),
                Fabut.value(new PropertyPath<>("address.streetNumber"), "627"),
                Fabut.value(new PropertyPath<>("faculty.name"), "PMF"),
                Fabut.value(new PropertyPath<>("faculty.teacher.name"), "Djura"),
                Fabut.value(new PropertyPath<>("faculty.teacher.address.city"), "Kamenica"),
                Fabut.value(new PropertyPath<>("faculty.teacher.address.street"), "Ljubicica"),
                Fabut.value(new PropertyPath<Student>("faculty.teacher.student"), student),
                Fabut.value(new PropertyPath<>("faculty.teacher.address.streetNumber"), "10"));
        Fabut.afterTest();
    }

    @Test
    public void testAssertObjectMaps() {
        // setup
        Fabut.beforeTest(this);
        final Map<String, TierOneType> expected = new HashMap<>();
        expected.put("first", new TierOneType(TEST));
        expected.put("second", new TierOneType(TEST));

        final Map<String, TierOneType> actual = new HashMap<>();
        actual.put("first", new TierOneType(TEST));
        actual.put("second", new TierOneType(TEST));
        Fabut.takeSnapshot();

        // method
        Fabut.assertObjects(expected, actual);

        Fabut.afterTest();
    }

    @Test(expected = AssertionFailedError.class)
    public void testAssertObjectMapsFail() {
        // setup
        Fabut.beforeTest(this);
        final Map<String, TierOneType> expected = new HashMap<>();
        expected.put("first", new TierOneType(TEST));
        expected.put("second", new TierOneType(TEST));

        final Map<String, TierOneType> actual = new HashMap<>();
        actual.put("first", new TierOneType(TEST + TEST));
        actual.put("second", new TierOneType(TEST));
        Fabut.takeSnapshot();

        // method
        Fabut.assertObjects(expected, actual);

        Fabut.afterTest();
    }

    @Test(expected = AssertionFailedError.class)
    public void testAssertObjectWithInnerMapFail() {
        // setup
        Fabut.beforeTest(this);
        final TierTwoTypeWithMap expected = new TierTwoTypeWithMap();
        expected.setProperty(TEST);
        expected.setMap(new HashMap<>());
        expected.getMap().put(1, new TierOneType("a"));
        expected.getMap().put(2, new TierOneType("b"));

        final TierTwoTypeWithMap actual = new TierTwoTypeWithMap();
        actual.setProperty(TEST);
        actual.setMap(new HashMap<>());
        actual.getMap().put(1, new TierOneType("c"));
        actual.getMap().put(2, new TierOneType("d"));

        Fabut.takeSnapshot();

        // method
        Fabut.assertObjects(expected, actual);

        Fabut.afterTest();
    }

    @Test(expected = AssertionFailedError.class)
    public void testAssertObjectWithInnerList() {
        // setup
        Fabut.beforeTest(this);
        final TierTwoTypeWithListProperty expected = new TierTwoTypeWithListProperty(new LinkedList<>());
        expected.getProperty().add(TEST);
        final TierTwoTypeWithListProperty actual = new TierTwoTypeWithListProperty(new LinkedList<>());
        actual.getProperty().add(TEST + TEST);

        Fabut.takeSnapshot();

        // method
        Fabut.assertObjects(expected, actual);
        Fabut.afterTest();
    }
}
