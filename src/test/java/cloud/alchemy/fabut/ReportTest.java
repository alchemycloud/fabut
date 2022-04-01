// package cloud.alchemy.fabut;
//
// import cloud.alchemy.fabut.model.EntityTierOneType;
// import cloud.alchemy.fabut.model.NoDefaultConstructorEntity;
// import cloud.alchemy.fabut.model.TierOneType;
// import cloud.alchemy.fabut.model.test.Address;
// import cloud.alchemy.fabut.model.test.Faculty;
// import cloud.alchemy.fabut.model.test.Student;
// import cloud.alchemy.fabut.model.test.Teacher;
// import cloud.alchemy.fabut.property.IProperty;
// import cloud.alchemy.fabut.property.PropertyPath;
// import junit.framework.AssertionFailedError;
// import org.junit.After;
// import org.junit.Before;
// import org.junit.Test;
//
//
// public class ReportTest extends AbstractFabutRepositoryAssertTest {
//    private static final String TEST = "test";
//
//    @Before
//    public void before() {
//        System.out.println("Start test!!");
//    }
//
//    @After
//    public void after() {
//        System.out.println("After test!!");
//    }
//
//    /**
//     * Test for {@link Fabut#beforeTest(Object)} if it throws {@link IllegalStateException} if specified test instance
//     * doesn't implement {@link IFabutTest} or {@link IFabutRepositoryTest}.
//     */
//    @Test
//    public void testBeforeTest() {
//        // method
//        try {
//            Fabut.beforeTest(new Object());
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//    }
//
//    /**
//     * Test for {@link Fabut#afterTest()} when snapshot matches after state.
//     */
//    @Test
//    public void testAfterTestSucess() {
//        // setup
//        Fabut.beforeTest(this);
//        Fabut.takeSnapshot();
//
//        // method
//        Fabut.afterTest();
//    }
//
//    /**
//     * Test for {@link Fabut#afterTest()} when snapshot doesn't match after state.
//     */
//    @Test
//    public void testAfterTestFail() {
//        // setup
//        Fabut.beforeTest(this);
//        Fabut.takeSnapshot();
//        final EntityTierOneType entityTierOneType = new EntityTierOneType("test", 1);
//        getEntityTierOneTypes().add(entityTierOneType);
//
//        // method
//        try {
//            Fabut.afterTest();
//
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//    }
//
//    /**
//     * Test for {@link Fabut#takeSnapshot(Object...)} when there are entites in repository that cannot be copied.
//     */
//    @Test
//    public void testTakeSnapshotFail() {
//        // setup
//        Fabut.beforeTest(this);
//        getNoDefaultConstructorEntities().add(new NoDefaultConstructorEntity("test", 1));
//
//        // method
//        try {
//            Fabut.takeSnapshot();
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//    }
//
//    /**
//     * Test for {@link Fabut#takeSnapshot(Object...)} when repository can be copied.
//     */
//    @Test
//    public void testTakeSnapshotSuccess() {
//        // setup
//        Fabut.beforeTest(this);
//        final EntityTierOneType entityTierOneType = new EntityTierOneType("test1", 1);
//        getEntityTierOneTypes().add(entityTierOneType);
//        Fabut.takeSnapshot();
//
//        // method
//        try {
//            Fabut.afterTest();
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//    }
//
//    /**
//     * Test for {@link Fabut#assertObject(Object, IProperty...)} when object is complex type
//     * and can be asserted.
//     */
//    @Test
//    public void testAssertObjectWithComplexType() {
//        // setup
//        Fabut.beforeTest(this);
//        final TierOneType object = new TierOneType("test");
//
//        // method
//        try {
//            Fabut.assertObject(object, Fabut.value(TierOneType.PROPERTY, "test1"));
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//
//    }
//
//    /**
//     * Test for {@link Fabut#assertObject(Object, IProperty...)} when object is entity type and
//     * can be asserted.
//     */
//    @Test
//    public void testAssertObjectWithEntityType() {
//        // setup
//        Fabut.beforeTest(this);
//        final EntityTierOneType entity = new EntityTierOneType();
//        entity.setProperty("test");
//        entity.setId(1);
//
//        // method
//        Fabut.takeSnapshot();
//        getEntityTierOneTypes().add(entity);
//        try {
//            Fabut.assertObject(entity, Fabut.value(EntityTierOneType.ID, 2),
//                    Fabut.value(EntityTierOneType.PROPERTY, "test"));
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//
//        Fabut.afterTest();
//
//    }
//
//    /**
//     * Test for {@link Fabut#assertObject(Object, IProperty...)} when object is entity and
//     * cannot be asserted.
//     */
//    @Test
//    public void testAssertObjectWithEntityTypeFail() {
//        // setup
//        Fabut.beforeTest(this);
//        final EntityTierOneType entity = new EntityTierOneType();
//        entity.setProperty("test");
//        entity.setId(1);
//
//        // method
//        Fabut.takeSnapshot();
//        getEntityTierOneTypes().add(entity);
//        try {
//            Fabut.assertObject(entity, Fabut.value(EntityTierOneType.ID, 1),
//                    Fabut.value(EntityTierOneType.PROPERTY, "fail"));
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//
//    }
//
//    /**
//     * Test for {@link Fabut#assertEntityWithSnapshot(Object, IProperty...)} when entity can be
//     * asserted with one in snapshot.
//     */
//    @Test
//    public void testAssertEntityWithSnapshotSuccess() {
//        // setup
//        Fabut.beforeTest(this);
//        final EntityTierOneType entityTierOneType = new EntityTierOneType();
//        entityTierOneType.setId(10);
//        entityTierOneType.setProperty("property");
//        getEntityTierOneTypes().add(entityTierOneType);
//        Fabut.takeSnapshot();
//
//        // method
//        ((EntityTierOneType) getEntityTierOneTypes().get(0)).setProperty("test");
//        try {
//            Fabut.assertEntityWithSnapshot(entityTierOneType, Fabut.value(EntityTierOneType.PROPERTY, "test1"));
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//
//        Fabut.afterTest();
//    }
//
//    /**
//     * Test for {@link Fabut#assertEntityWithSnapshot(Object, IProperty...)} when entity cannot
//     * be asserted with one in snapshot.
//     */
//    @Test
//    public void testAssertEntityWithSnapshotFail() {
//        // setup
//        Fabut.beforeTest(this);
//        final EntityTierOneType entityTierOneType = new EntityTierOneType();
//        entityTierOneType.setId(10);
//        entityTierOneType.setProperty("property");
//        getEntityTierOneTypes().add(entityTierOneType);
//        Fabut.takeSnapshot();
//
//        // method
//        ((EntityTierOneType) getEntityTierOneTypes().get(0)).setProperty("test");
//        try {
//            Fabut.assertEntityWithSnapshot(entityTierOneType, Fabut.value(EntityTierOneType.PROPERTY, "testtest"));
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//
//        Fabut.afterTest();
//    }
//
//    /**
//     * Test for {@link Fabut#assertEntityWithSnapshot(Object, IProperty...)} when specified
//     * object is not an entity.
//     */
//    @Test
//    public void testAssertEntityWithSnapshotNotEntity() {
//        // setup
//        Fabut.beforeTest(this);
//        Fabut.takeSnapshot();
//
//        // method
//        try {
//            Fabut.assertEntityWithSnapshot(new TierOneType());
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//    }
//
//    @Test(expected = AssertionFailedError.class)
//    public void testAssertEntityWithSnapshotNullEntity() {
//        // setup
//        Fabut.beforeTest(this);
//        final EntityTierOneType entityTierOneType = new EntityTierOneType();
//        entityTierOneType.setId(10);
//        entityTierOneType.setProperty("property");
//        getEntityTierOneTypes().add(entityTierOneType);
//        Fabut.takeSnapshot();
//
//        // method
//        ((EntityTierOneType) getEntityTierOneTypes().get(0)).setProperty("test");
//        try {
//            Fabut.assertEntityWithSnapshot(null, Fabut.value(EntityTierOneType.PROPERTY, "test"));
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//
//        Fabut.afterTest();
//    }
//
//    /**
//     * Test for {@link Fabut#assertEntityAsDeleted(Object)} when specified entity is successfully asserted as deleted.
//     */
//    @Test
//    public void assertEntityAsDeletedSuccess() {
//        // setup
//        Fabut.beforeTest(this);
//        final EntityTierOneType entity = new EntityTierOneType(TEST, 1);
//        getEntityTierOneTypes().add(entity);
//        Fabut.takeSnapshot();
//
//        // method
//        // getEntityTierOneTypes().remove(0);
//        try {
//            Fabut.assertEntityAsDeleted(entity);
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//
//        Fabut.afterTest();
//    }
//
//    /**
//     * Test for {@link Fabut#assertEntityAsDeleted(Object)} when specified entity is not deleted in repository.
//     */
//    @Test
//    public void assertEntityAsDeletedFail() {
//        // setup
//        Fabut.beforeTest(this);
//        final EntityTierOneType entity = new EntityTierOneType(TEST, 1);
//        getEntityTierOneTypes().add(entity);
//        Fabut.takeSnapshot();
//
//        // method
//        try {
//            Fabut.assertEntityAsDeleted(entity);
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//
//        Fabut.afterTest();
//    }
//
//    /**
//     * Test for {@link Fabut#assertEntityAsDeleted(Object)} when specified object is not entity.
//     */
//    @Test
//    public void assertEntityAsDeletedNotEntity() {
//        // setup
//        Fabut.beforeTest(this);
//        final TierOneType object = new TierOneType();
//        Fabut.takeSnapshot();
//
//        // method
//        try {
//            Fabut.assertEntityAsDeleted(object);
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//
//        Fabut.afterTest();
//    }
//
//    /**
//     * Test for {@link Fabut#ignoreEntity(Object)} when entity can ignored.
//     */
//    @Test
//    public void testIgnoreEntitySuccess() {
//        // setup
//        Fabut.beforeTest(this);
//        final EntityTierOneType entity = new EntityTierOneType(TEST, 1);
//        Fabut.takeSnapshot();
//        // getEntityTierOneTypes().add(entity);
//
//        // method
//        try {
//            Fabut.ignoreEntity(entity);
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//
//        Fabut.afterTest();
//    }
//
//    /**
//     * Test for {@link Fabut#ignoreEntity(Object)} when entity cannot be ignored.
//     */
//    @Test
//    public void testIgnoreEntityFail() {
//        // setup
//        Fabut.beforeTest(this);
//        final EntityTierOneType entity = new EntityTierOneType(TEST, null);
//        Fabut.takeSnapshot();
//        getEntityTierOneTypes().add(entity);
//
//        // method
//        try {
//            Fabut.ignoreEntity(entity);
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//
//        Fabut.afterTest();
//    }
//
//    /**
//     * Test for {@link Fabut#ignoreEntity(Object)} when object is not entity.
//     */
//    @Test
//    public void testIgnoreEntityNotEntity() {
//        // setup
//        Fabut.beforeTest(this);
//        final TierOneType object = new TierOneType();
//        Fabut.takeSnapshot();
//
//        // method
//        try {
//            Fabut.ignoreEntity(object);
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//
//        Fabut.afterTest();
//    }
//
//    /**
//     * Integration test for {@link Fabut#assertObject(Object, IProperty...)} when inner
//     * properties are used for asserting.
//     */
//    @Test
//    public void testAssertObject() {
//        // method
//        Fabut.beforeTest(this);
//        final Student student = new Student();
//        student.setName("Nikola");
//        student.setLastName("Olah");
//        final Address address1 = new Address();
//        address1.setCity("Temerin");
//        address1.setStreet("Novosadska");
//        address1.setStreetNumber("627");
//        student.setAddress(address1);
//        final Faculty faculty = new Faculty();
//        faculty.setName("PMF");
//        student.setFaculty(faculty);
//        final Teacher teacher = new Teacher();
//        teacher.setName("Djura");
//        faculty.setTeacher(teacher);
//        final Address address2 = new Address();
//        address2.setCity("Kamenica");
//        address2.setStreet("Ljubicica");
//        address2.setStreetNumber("10");
//        teacher.setAddress(address2);
//        teacher.setStudent(student);
//        Fabut.takeSnapshot();
//
//        // assert
//        try {
//            Fabut.assertObject(student,
//                    Fabut.value(new PropertyPath<>("name"), "Nikola"),
//                    Fabut.value(new PropertyPath<>("lastName"), "Olah"),
//                    Fabut.value(new PropertyPath<>("address.city"), "Temerin1"),
//                    Fabut.value(new PropertyPath<>("address.street"), "Novosadska"),
//                    Fabut.value(new PropertyPath<>("address.streetNumber"), "627"),
//                    Fabut.value(new PropertyPath<>("faculty.name"), "PMF"),
//                    Fabut.value(new PropertyPath<>("faculty.teacher.name"), "Djura"),
//                    Fabut.value(new PropertyPath<>("faculty.teacher.address.city"), "Kamenica"),
//                    Fabut.value(new PropertyPath<>("faculty.teacher.address.street"), "Ljubicica"),
//                    Fabut.value(new PropertyPath<>("faculty.teacher.student"), student),
//                    Fabut.value(new PropertyPath<>("faculty.teacher.address.streetNumber"), "10"));
//        } catch (final Throwable e) {
//            System.out.println(e.getMessage());
//        }
//        Fabut.afterTest();
//    }
//
// }
