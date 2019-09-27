package cloud.alchemy.fabut;

import cloud.alchemy.fabut.assertt.FabutRepositoryAssert;
import cloud.alchemy.fabut.assertt.SnapshotAssert;
import cloud.alchemy.fabut.enums.AssertType;
import cloud.alchemy.fabut.property.*;
import cloud.alchemy.fabut.report.FabutReportBuilder;
import cloud.alchemy.fabut.util.ConversionUtil;
import cloud.alchemy.fabut.util.ReflectionUtil;
import junit.framework.AssertionFailedError;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of method for advanced asserting.
 *
 * @author Dusko Vesin
 * @author Andrej Miletic
 */
public class Fabut {

    private static FabutRepositoryAssert fabutAssert = null;
    private static AssertType assertType;
    private static boolean assertPassed;

    /**
     * Private constructor to forbid instancing this class.
     */
    protected Fabut() {

    }

    /**
     * This method needs to be called in @Before method of a test in order for {@link Fabut} to work.
     *
     * @param testInstance the test instance
     */
    public static synchronized void beforeTest(final Object testInstance) {
        assertPassed = true;
        assertType = ConversionUtil.getAssertType(testInstance);
        switch (assertType) {
            case OBJECT_ASSERT:
                fabutAssert = new FabutRepositoryAssert((IFabutTest) testInstance);
                break;
            case REPOSITORY_ASSERT:
                fabutAssert = new FabutRepositoryAssert((IFabutRepositoryTest) testInstance);
                break;
            case UNSUPPORTED_ASSERT:
                throw new IllegalStateException("This test must implement IFabutAssert or IRepositoryFabutAssert");
            default:
                throw new IllegalStateException("Unsupported assert type: " + assertType);
        }
    }

    /**
     * This method needs to be called in @After method of a test in order for {@link Fabut} to work.
     */
    public static void afterTest() {
        boolean ok = true;
        final StringBuilder sb = new StringBuilder();

        if (assertPassed) {
            final FabutReportBuilder parameterReport = new FabutReportBuilder("Parameter snapshot assert");
            if (!fabutAssert.assertParameterSnapshot(parameterReport)) {
                ok = false;
                sb.append(parameterReport.getMessage());
            }

            final FabutReportBuilder snapshotReport = new FabutReportBuilder("Repository snapshot assert");
            if (assertType == AssertType.REPOSITORY_ASSERT) {
                if (!fabutAssert.assertDbSnapshot(snapshotReport)) {
                    ok = false;
                    sb.append(snapshotReport.getMessage());
                }
            }
        }
        if (!ok) {
            throw new AssertionFailedError(sb.toString());
        }

    }

    /**
     * Creates repository snapshot so it can be asserted with after state after the test execution.
     *
     * @param parameters array of parameters for snapshot
     */
    public static void takeSnapshot(final Object... parameters) {
        checkValidInit();
        if (assertType == AssertType.UNSUPPORTED_ASSERT) {
            throw new IllegalStateException("Test must implement IRepositoryFabutAssert");
        }
        final FabutReportBuilder report = new FabutReportBuilder();
        if (!fabutAssert.takeSnapshot(report, parameters)) {
            throw new AssertionFailedError(report.getMessage());
        }
    }

    /**
     * Asserts object with expected properties.
     *
     * @param message    custom message to be added on top of the report
     * @param object     the object that needs to be asserted
     * @param properties expected properties for asserting object
     */
    public static void assertObject(final String message, final Object object, final IProperty... properties) {
        checkValidInit();

        final FabutReportBuilder report = new FabutReportBuilder(message);
        if (isRepositoryAssert() && fabutAssert.doesExistInSnapshot(object)) {
            assertPassed = false;
            report.entityInSnapshot(object);
            throw new AssertionFailedError(report.getMessage());
        }

        if (!fabutAssert.assertObjectWithProperties(report, object, fabutAssert.extractProperties(properties))) {
            assertPassed = false;
            throw new AssertionFailedError(report.getMessage());
        }
    }

    /**
     * Asserts object with expected properties.
     *
     * @param expected   the expected
     * @param properties expected properties for asserting object
     */
    public static void assertObject(final Object expected, final IProperty... properties) {
        assertObject("", expected, properties);
    }

    /**
     * Asserts entity with one saved in snapshot.
     *
     * @param entity          the entity
     * @param expectedChanges properties changed after the snapshot has been taken
     */
    public static <T> T assertEntityWithSnapshot(final T entity, final IProperty... expectedChanges) {
        checkValidInit();
        checkIfEntity(entity);
        final FabutReportBuilder report = new FabutReportBuilder();

        if (expectedChanges.length == 0) {
            report.assertWithSnapshotMustHaveAtLeastOnChange(entity);
            throw new AssertionFailedError(report.getMessage());
        }

        final SnapshotAssert assertResult = fabutAssert.assertEntityWithSnapshot(report, entity, fabutAssert.extractProperties(expectedChanges));
        if (!assertResult.getResult()) {
            assertPassed = false;
            throw new AssertionFailedError(report.getMessage());
        }
        return (T) assertResult.getEntity();
    }

    /**
     * Assert entity as deleted. It will fail if entity can still be found in snapshot.
     *
     * @param entity the entity
     */
    public static void assertEntityAsDeleted(final Object entity) {
        checkValidInit();
        checkIfEntity(entity);

        final FabutReportBuilder report = new FabutReportBuilder();
        if (!fabutAssert.assertEntityAsDeleted(report, entity)) {
            assertPassed = false;
            throw new AssertionFailedError(report.getMessage());
        }
    }

    /**
     * Ignores the entity.
     *
     * @param entity the entity
     */
    public static void ignoreEntity(final Object entity) {
        checkValidInit();
        checkIfEntity(entity);

        final FabutReportBuilder report = new FabutReportBuilder();
        if (!fabutAssert.ignoreEntity(report, entity)) {
            assertPassed = false;
            throw new AssertionFailedError(report.getMessage());
        }
    }

    /**
     * Checks if specified object is entity.
     *
     * @param entity the entity
     */
    private static void checkIfEntity(final Object entity) {
        if (!isRepositoryAssert()) {
            throw new IllegalStateException("Test class must implement IRepositoryFabutAssert");
        }

        if (entity == null) {
            throw new NullPointerException("assertEntityWithSnapshot cannot take null entity!");
        }

        if (!ReflectionUtil.isEntityType(entity, fabutAssert.getTypes())) {
            throw new IllegalStateException(entity.getClass() + " is not registered as entity type");
        }
    }

    private static boolean isRepositoryAssert() {
        return assertType == AssertType.REPOSITORY_ASSERT;
    }

    private static void checkValidInit() {
        if (fabutAssert == null) {
            throw new IllegalStateException("Fabut.beforeTest must be called before the test");
        }
    }

    /**
     * Create {@link Property} with provided parameters.
     *
     * @param path          property path.
     * @param expectedValue expected values
     * @param <T>           generic type
     * @return created object.
     */
    public static <T> Property<T> value(final PropertyPath<T> path, final T expectedValue) {
        return new Property<>(path.getPath(), expectedValue);
    }

    /**
     * Create {@link IgnoredProperty} with provided parameter.
     *
     * @param path property path.
     * @return created object.
     */
    public static IgnoredProperty ignored(final PropertyPath<?> path) {
        return new IgnoredProperty(path.getPath());
    }

    /**
     * Create {@link IgnoredProperty} with provided parameters.
     *
     * @param paths property path.
     * @return created objects.
     */
    public static MultiProperties ignored(final PropertyPath<?>... paths) {
        final List<ISingleProperty> properties = new ArrayList<>();

        for (final PropertyPath<?> path : paths) {
            properties.add(ignored(path));
        }

        return new MultiProperties(properties);
    }

    /**
     * Create {@link NotNullProperty} with provided parameter.
     *
     * @param path property path.
     * @return created object.
     */
    public static NotNullProperty notNull(final PropertyPath<?> path) {
        return new NotNullProperty(path.getPath());
    }

    /**
     * Create {@link NotNullProperty} with provided parameters.
     *
     * @param paths property paths.
     * @return created objects.
     */
    public static MultiProperties notNull(final PropertyPath<?>... paths) {
        final List<ISingleProperty> properties = new ArrayList<>();

        for (final PropertyPath<?> path : paths) {
            properties.add(notNull(path));
        }

        return new MultiProperties(properties);
    }

    /**
     * Create {@link NullProperty} with provided parameter.
     *
     * @param path property path.
     * @return created object.
     */
    public static NullProperty isNull(final PropertyPath<?> path) {
        return new NullProperty(path.getPath());
    }

    /**
     * Create {@link NullProperty} with provided parameters.
     *
     * @param paths property paths.
     * @return created objects.
     */
    public static MultiProperties isNull(final PropertyPath<?>... paths) {
        final List<ISingleProperty> properties = new ArrayList<>();

        for (final PropertyPath<?> path : paths) {
            properties.add(isNull(path));
        }

        return new MultiProperties(properties);
    }

    /**
     * Create {@link NotEmptyProperty} with provided parameter.
     *
     * @param path property path.
     * @return created object.
     */
    public static NotEmptyProperty notEmpty(final PropertyPath<?> path) {
        return new NotEmptyProperty(path.getPath());
    }

    /**
     * Create {@link NotEmptyProperty} with provided parameters.
     *
     * @param paths property paths.
     * @return created objects.
     */
    public static MultiProperties notEmpty(final PropertyPath<?>... paths) {
        final List<ISingleProperty> properties = new ArrayList<>();

        for (final PropertyPath<?> path : paths) {
            properties.add(notEmpty(path));
        }

        return new MultiProperties(properties);
    }

    /**
     * Create {@link EmptyProperty} with provided parameter.
     *
     * @param path property path.
     * @return created object.
     */
    public static EmptyProperty isEmpty(final PropertyPath<?> path) {
        return new EmptyProperty(path.getPath());
    }

    /**
     * Create {@link EmptyProperty} with provided parameters.
     *
     * @param paths property paths.
     * @return created objects.
     */
    public static MultiProperties isEmpty(final PropertyPath<?>... paths) {
        final List<ISingleProperty> properties = new ArrayList<>();

        for (final PropertyPath<?> path : paths) {
            properties.add(isEmpty(path));
        }

        return new MultiProperties(properties);
    }
}

