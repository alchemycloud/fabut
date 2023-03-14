package cloud.alchemy.fabut;

import cloud.alchemy.fabut.model.*;
import cloud.alchemy.fabut.model.test.Address;
import cloud.alchemy.fabut.model.test.Faculty;
import cloud.alchemy.fabut.model.test.Student;
import cloud.alchemy.fabut.model.test.Teacher;
import cloud.alchemy.fabut.property.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FabutTest extends AbstractFabutTest {

    private static final String TEST = "test";

    // mock lists
    private final List<Object> entityTierOneTypes = new ArrayList<>();
    private final List<Object> entityTierTwoTypes = new ArrayList<>();
    private final List<Object> entityWithListTypes = new ArrayList<>();
    private final List<Object> noDefaultConstructorEntities = new ArrayList<>();

    private boolean assertAfterTest;

    public FabutTest() {
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
        complexTypes.add(Student.class);
        complexTypes.add(EntityWithList.class);

        ignoredTypes.add(IgnoredType.class);
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

    private List<Object> getEntityTierOneTypes() {
        return entityTierOneTypes;
    }

    private List<Object> getEntityWithListTypes() {
        return entityWithListTypes;
    }

    private List<Object> getNoDefaultConstructorEntities() {
        return noDefaultConstructorEntities;
    }

    /** Test for ignored when varargs are passed. */
    @Test
    public void testIgnoredVarargs() {
        // setup
        final PropertyPath<?>[] properties = new PropertyPath[] {EntityTierOneType.PROPERTY, EntityTierOneType.ID};

        // method
        final MultiProperties multi = ignored(properties);

        // assert
        assertEquals(properties.length, multi.getProperties().size());

        for (int i = 0; i < properties.length; i++) {
            assertTrue(multi.getProperties().get(i) instanceof IgnoredProperty);
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

    /** Test for null when varargs are passed. */
    @Test
    public void testNulllVarargs() {
        // setup
        final PropertyPath<?>[] properties = new PropertyPath<?>[] {EntityTierOneType.PROPERTY, EntityTierOneType.ID};

        // method
        final MultiProperties multi = isNull(properties);

        // assert
        assertEquals(properties.length, multi.getProperties().size());

        for (int i = 0; i < properties.length; i++) {
            assertTrue(multi.getProperties().get(i) instanceof NullProperty);
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

    /** Test for notNull when varargs are passed. */
    @Test
    public void testNotNullVarargs() {
        // setup
        final PropertyPath<?>[] properties = new PropertyPath[] {EntityTierOneType.PROPERTY, EntityTierOneType.ID};

        // method
        final MultiProperties multi = notNull(properties);

        // assert
        assertEquals(properties.length, multi.getProperties().size());

        for (int i = 0; i < properties.length; i++) {
            assertTrue(multi.getProperties().get(i) instanceof NotNullProperty);
            assertEquals(properties[i].getPath(), multi.getProperties().get(i).getPath());
        }
    }

    /** Test for {@link Fabut#notNull(PropertyPath)}. */
    @Test
    public void testNotNull() {
        // method
        final NotNullProperty notNullProperty = notNull(EntityTierOneType.PROPERTY);

        // assert
        assertEquals(EntityTierOneType.PROPERTY.getPath(), notNullProperty.getPath());
    }

    /** Test for {@link Fabut#isNull(PropertyPath)}. */
    @Test
    public void testNulll() {
        // method
        final NullProperty nullProperty = isNull(EntityTierOneType.PROPERTY);

        // assert
        assertEquals(EntityTierOneType.PROPERTY.getPath(), nullProperty.getPath());
    }

    /** Test for {@link Fabut#ignored(PropertyPath)}. */
    @Test
    public void testIgnored() {
        // method
        final IgnoredProperty ignoreProperty = ignored(EntityTierOneType.PROPERTY);

        // assert
        assertEquals(EntityTierOneType.PROPERTY.getPath(), ignoreProperty.getPath());
    }

    /** Test for {@link Fabut#value(PropertyPath, Object)}. */
    @Test
    public void testValue() {
        // method
        final Property<String> changedProperty = value(EntityTierOneType.PROPERTY, TEST);

        // assert
        assertEquals(TEST, changedProperty.getValue());
        assertEquals(EntityTierOneType.PROPERTY.getPath(), changedProperty.getPath());
    }

    @Test
    public void testAfterTestSucess() {
        // setup

        takeSnapshot();

        // method

    }

    @Test
    public void testAfterTestFail() {
        assertThrows(
                AssertionFailedError.class,
                () -> {
                    // setup
                    takeSnapshot();
                    final EntityTierOneType entityTierOneType = new EntityTierOneType("test", 1);
                    getEntityTierOneTypes().add(entityTierOneType);

                    // method
                    assertAfterTest = false;
                    super.after();
                });
    }

    @Test
    public void testTakeSnapshotFail() {
        assertThrows(
                AssertionError.class,
                () -> {
                    // setup

                    getNoDefaultConstructorEntities().add(new NoDefaultConstructorEntity("test", 1));

                    // method
                    assertAfterTest = false;
                    takeSnapshot();
                });
    }

    @Test
    public void testTakeSnapshotSuccess() {
        // setup

        final EntityTierOneType entityTierOneType = new EntityTierOneType("test", 1);
        getEntityTierOneTypes().add(entityTierOneType);
        takeSnapshot();

        // method

    }

    @Test
    public void testAssertObjectWithComplexType() {
        assertThrows(
                AssertionError.class,
                () -> {
                    // setup

                    final TierOneType object = new TierOneType("test");

                    // method
                    assertObject(object, value(TierOneType.PROPERTY, "test"));
                });
    }

    @Test
    public void testAssertObjectWithEntityType() {
        // setup

        final EntityTierOneType entity = new EntityTierOneType();
        entity.setProperty("test");
        entity.setId(1);

        // method
        takeSnapshot();
        getEntityTierOneTypes().add(entity);
        assertObject(entity, value(EntityTierOneType.ID, 1), value(EntityTierOneType.PROPERTY, "test"));
    }

    @Test
    public void testAssertObjectWithEntityTypeWithListField() {
        // setup

        final EntityWithList entity = new EntityWithList();
        entity.setList(Collections.singletonList(new EntityTierOneType("test", 1)));
        entity.setId(1);

        // method
        takeSnapshot();
        getEntityWithListTypes().add(entity);
        assertObject(entity, value(EntityWithList.ID, 1));
    }

    @Test
    public void testAssertObjectWithEntityTypeFail() {
        assertThrows(
                AssertionFailedError.class,
                () -> {
                    // setup

                    final EntityTierOneType entity = new EntityTierOneType();
                    entity.setProperty("test");
                    entity.setId(1);

                    // method
                    takeSnapshot();
                    getEntityTierOneTypes().add(entity);
                    assertObject(entity, value(EntityTierOneType.ID, 1), value(EntityTierOneType.PROPERTY, "fail"));
                });
    }

    @Test
    public void testAssertEntityWithSnapshotSuccess() {
        // setup

        final EntityTierOneType entityTierOneType = new EntityTierOneType();
        entityTierOneType.setId(10);
        entityTierOneType.setProperty("property");
        getEntityTierOneTypes().add(entityTierOneType);
        takeSnapshot();

        // method
        ((EntityTierOneType) getEntityTierOneTypes().get(0)).setProperty("test");
        assertEntityWithSnapshot(entityTierOneType, value(EntityTierOneType.PROPERTY, "test"));
    }

    @Test
    public void testAssertEntityWithSnapshotFail() {
        assertThrows(
                AssertionError.class,
                () -> {
                    // setup

                    final EntityTierOneType entityTierOneType = new EntityTierOneType();
                    entityTierOneType.setId(10);
                    entityTierOneType.setProperty("property");
                    getEntityTierOneTypes().add(entityTierOneType);
                    takeSnapshot();

                    // method
                    ((EntityTierOneType) getEntityTierOneTypes().get(0)).setProperty("test");
                    assertEntityWithSnapshot(entityTierOneType, value(EntityTierOneType.PROPERTY, "testtest"));

                    assertAfterTest = false;
                    super.after();
                });
    }

    @Test
    public void testAssertEntityWithSnapshotNotEntity() {
        assertThrows(
                AssertionError.class,
                () -> {
                    // setup

                    takeSnapshot();

                    // method
                    assertEntityWithSnapshot(new TierOneType());
                });
    }

    @Test
    public void testAssertEntityWithSnapshotNullEntity() {
        assertThrows(
                IllegalStateException.class,
                () -> {
                    // setup

                    final EntityTierOneType entityTierOneType = new EntityTierOneType();
                    entityTierOneType.setId(10);
                    entityTierOneType.setProperty("property");
                    getEntityTierOneTypes().add(entityTierOneType);
                    takeSnapshot();

                    // method
                    assertAfterTest = false;
                    ((EntityTierOneType) getEntityTierOneTypes().get(0)).setProperty("test");
                    assertEntityWithSnapshot(null, value(EntityTierOneType.PROPERTY, "test"));
                });
    }

    @Test
    public void assertEntityAsDeletedSuccess() {
        // setup

        final EntityTierOneType entity = new EntityTierOneType(TEST, 1);
        getEntityTierOneTypes().add(entity);
        takeSnapshot();

        // method
        getEntityTierOneTypes().remove(0);
        assertEntityAsDeleted(entity);
    }

    @Test
    public void assertEntityAsDeletedFail() {
        assertThrows(
                AssertionFailedError.class,
                () -> {
                    // setup

                    final EntityTierOneType entity = new EntityTierOneType(TEST, 1);
                    getEntityTierOneTypes().add(entity);
                    takeSnapshot();

                    // method
                    assertEntityAsDeleted(entity);
                });
    }

    @Test
    public void assertEntityAsDeletedNotEntity() {
        assertThrows(
                AssertionError.class,
                () -> {
                    // setup

                    final TierOneType object = new TierOneType();
                    takeSnapshot();

                    // method
                    assertEntityAsDeleted(object);
                });
    }

    @Test
    public void testIgnoreEntitySuccess() {
        // setup

        final EntityTierOneType entity = new EntityTierOneType(TEST, 1);
        takeSnapshot();
        getEntityTierOneTypes().add(entity);

        // method
        ignoreEntity(entity);
    }

    @Test
    public void testIgnoreEntityFail() {
        assertThrows(
                AssertionError.class,
                () -> {
                    // setup

                    final EntityTierOneType entity = new EntityTierOneType(TEST, null);
                    takeSnapshot();
                    getEntityTierOneTypes().add(entity);

                    // method
                    ignoreEntity(entity);
                });
    }

    @Test
    public void testIgnoreEntityNotEntity() {
        assertThrows(
                AssertionError.class,
                () -> {
                    // setup

                    final TierOneType object = new TierOneType();
                    takeSnapshot();

                    // method
                    ignoreEntity(object);
                });
    }

    @Test
    public void testAssertObject() {
        // method
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
        takeSnapshot();

        // assert
        assertObject(
                student,
                value(new PropertyPath<>("name"), "Nikola"),
                value(new PropertyPath<>("lastName"), "Olah"),
                value(new PropertyPath<>("address.city"), "Temerin"),
                value(new PropertyPath<>("address.street"), "Novosadska"),
                value(new PropertyPath<>("address.streetNumber"), "627"),
                value(new PropertyPath<>("faculty.name"), "PMF"),
                value(new PropertyPath<>("faculty.teacher.name"), "Djura"),
                value(new PropertyPath<>("faculty.teacher.address.city"), "Kamenica"),
                value(new PropertyPath<>("faculty.teacher.address.street"), "Ljubicica"),
                value(new PropertyPath<>("faculty.teacher.student"), student),
                value(new PropertyPath<>("faculty.teacher.address.streetNumber"), "10"));
    }
}
