package cloud.alchemy.fabut;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

interface FabutToString {
    String fabutToString();
}

class FabutReport {

    private static final String ARROW = ">";
    private static final String DASH = "-";
    private static final String NEW_LINE = "\n";
    private boolean success = true;
    private final List<FabutReport> subReports = new ArrayList<>();
    private final List<FabutToString> messages = new ArrayList<>();
    private final List<ReportCode> codes = new ArrayList<>();

    FabutReport() {}

    FabutReport(final FabutToString message) {
        this();
        messages.add(message);
    }

    boolean isSuccess() {
        return success && subReports.stream().allMatch(FabutReport::isSuccess);
    }

    FabutReport getSubReport(final FabutToString subReport) {
        FabutReport newSubReport = new FabutReport(subReport);
        subReports.add(newSubReport);
        return newSubReport;
    }

    String getMessage() {
        return getMessage(0);
    }

    private String getMessage(Integer depth) {
        final String spacer = NEW_LINE + StringUtils.repeat(DASH, depth * 2);

        String message = messages.stream().map(FabutToString::fabutToString).filter(a -> !a.isEmpty()).collect(Collectors.joining(spacer));
        if (!codes.isEmpty()) {
            message = message + "\nCODE:" + String.join("", codes.stream().map(ReportCode::code).toList());
        }

        final String subMessages =
                subReports.stream()
                        .filter(a -> !a.isSuccess())
                        .map(a -> a.getMessage(depth + 1))
                        .filter(a -> !a.isEmpty())
                        .collect(Collectors.joining(NEW_LINE));

        if (subMessages.isEmpty()) {
            return message;
        } else {
            return message + NEW_LINE + subMessages;
        }
    }

    /**
     * Add new comment to specified depth.
     *
     * @param comment to be added
     * @param type type of comment
     */
    private void addComment(final String comment, final CommentType type) {
        messages.add(() -> type.getMark() + ARROW + comment);

        if (type == CommentType.FAIL) {
            success = false;
        }
    }

    void listDifferentSizeComment(final String propertyName, final int expectedSize, final int actualSize) {
        final String comment = String.format("Expected size for list: %s is: %d, but was: %d", propertyName, expectedSize, actualSize);
        addComment(comment, CommentType.FAIL);
    }

    void noPropertyForField(final Object fieldOwner, final String fieldName, final Object field) {
        final String comment = String.format("There was no property for field:  %s of class:  %s, with value: %s", fieldName, fieldOwner.getClass(), field);
        addComment(comment, CommentType.FAIL);
    }

    void notNullProperty(final String fieldName) {
        final String comment = String.format("%s: expected not null property, but field was null", fieldName);
        addComment(comment, CommentType.FAIL);
    }

    void nullProperty(final String fieldName) {
        final String comment = String.format("%s: expected null property, but field was not null", fieldName);
        addComment(comment, CommentType.FAIL);
    }

    void notEmptyProperty(final String fieldName) {
        final String comment = String.format("%s: expected not empty property, but field was empty", fieldName);
        addComment(comment, CommentType.FAIL);
    }

    void emptyProperty(final String fieldName) {
        final String comment = String.format("%s: expected empty property, but field was not empty", fieldName);
        addComment(comment, CommentType.FAIL);
    }

    void reportIgnoreProperty(final String fieldName) {
        final String comment = String.format("%s: is ignored field", fieldName);
        addComment(comment, CommentType.SUCCESS);
    }

    void checkByReference(final String fieldName, final Object object) {
        final String comment = String.format("Property:  %s of class:  %s has wrong reference.", fieldName, object.getClass().getSimpleName());
        addComment(comment, CommentType.FAIL);
    }

    void ignoredType(final Class<?> clazz) {
        final String comment = String.format("Type  %s is ignored type.", clazz.getSimpleName());
        addComment(comment, CommentType.SUCCESS);
    }

    void assertingListElement(final String listName, final int index) {
        final String comment = String.format("Asserting object at index [%d] of list %s.", index, listName);
        addComment(comment, CommentType.COLLECTION);
    }

    void noEntityInSnapshot(final Object entity) {
        final String comment = String.format("Entity %s doesn't exist in DB any more but is not asserted in test.", entity);
        addComment(comment, CommentType.FAIL);
    }

    void entityInSnapshot(final Object entity) {
        final String comment = String.format("Entity %s exist in DB, user assertWithSnapshot instead..", entity);
        addComment(comment, CommentType.FAIL);
    }

    void entityNotAssertedInAfterState(final Object entity) {
        final String comment = String.format("Entity %s is created in system after last snapshot but hasn't been asserted in test.", entity);
        addComment(comment, CommentType.FAIL);
    }

    void uncallableMethod(final Method method, final Object actual) {
        final String comment =
                String.format(
                        "There is no method: %s in actual object class: %s (expected object class was: %s).",
                        method.getName(), actual.getClass(), method.getDeclaringClass().getSimpleName());
        addComment(comment, CommentType.FAIL);
    }

    void notNecessaryAssert(final String propertyName, final Object actual) {
        final String comment = String.format("Property: %s is same in expected and actual object, no need for assert", propertyName);
        addComment(comment, CommentType.FAIL);
    }

    void nullReference() {
        final String comment = "Object that was passed to assertObject was null, it must not be null!";
        addComment(comment, CommentType.FAIL);
    }

    void assertFail(final String propertyName, final Object expected, final Object actual) {
        final String commentComment = String.format("%s: expected: %s", propertyName, expected);
        addComment(commentComment, CommentType.FAIL);
        final String butWasComment = String.format("%s: but was: %s", propertyName, actual);
        addComment(butWasComment, CommentType.FAIL);
    }

    void idNull(final Class<?> clazz) {
        final String comment = String.format("Id of %s cannot be null", clazz.getSimpleName());
        addComment(comment, CommentType.FAIL);
    }

    void notDeletedInRepository(final Object entity) {
        final String comment = String.format("Entity: %s was not deleted in repository", entity);
        addComment(comment, CommentType.FAIL);
    }

    void noCopy(final Object entity) {
        final String comment = String.format("Entity: %s cannot be copied into snapshot", entity);
        addComment(comment, CommentType.FAIL);
    }

    void excessExpectedMap(final Object key) {
        final String comment = String.format("No match for expected key: %s", key);
        addComment(comment, CommentType.FAIL);
    }

    void excessActualMap(final Object key) {
        final String comment = String.format("No match for actual key: %s", key);
        addComment(comment, CommentType.FAIL);
    }

    void excessExpectedProperty(final String path) {
        final String comment = String.format("Excess property: %s", path);
        addComment(comment, CommentType.FAIL);
    }

    void assertingMapKey(final Object key) {
        final String comment = String.format("Map key: %s", key);
        addComment(comment, CommentType.COLLECTION);
    }

    <T> void assertWithSnapshotMustHaveAtLeastOnChange(T entity) {
        final String comment = String.format("Assert entity with snapshot must be called with at least one property: %s", entity);
        addComment(comment, CommentType.FAIL);
    }

    void addCode(ReportCode code) {
        codes.add(code);
    }
}

interface ReportCode {
    String code();
}
