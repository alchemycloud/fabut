package cloud.alchemy.fabut;

import cloud.alchemy.fabut.enums.AssertionContext;
import cloud.alchemy.fabut.enums.EntityChangeType;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Functional interface for providing string representations in Fabut reports.
 */
@FunctionalInterface
interface FabutToString {
    /**
     * Provides a string representation for use in Fabut reports.
     *
     * @return The string representation
     */
    String fabutToString();
}

/**
 * Report class for Fabut testing framework.
 * Handles test failures, successes, and hierarchical reporting.
 */
class FabutReport {

    // Constants for report formatting
    private static final String ARROW = ">";
    private static final String DASH = "-";
    private static final String NEW_LINE = "\n";
    
    // State variables
    private boolean success = true;

    // Thread-safe collections for concurrent access
    private final List<FabutReport> subReports = new CopyOnWriteArrayList<>();
    private final List<FabutToString> messages = new CopyOnWriteArrayList<>();
    private final List<ReportCode> codes = new CopyOnWriteArrayList<>();

    // Entity change tracking grouped by change type
    private final Map<EntityChangeType, List<EntityChange>> entityChanges = new EnumMap<>(EntityChangeType.class);

    // Assertion context for code generation
    private AssertionContext assertionContext = AssertionContext.NEW_OBJECT;

    /**
     * Default constructor for empty report.
     */
    FabutReport() {}

    /**
     * Constructor with initial message.
     *
     * @param message The initial message to add to this report
     */
    FabutReport(final FabutToString message) {
        this();
        if (message != null) {
            messages.add(message);
        }
    }

    /**
     * Checks if this report and all its subreports indicate success.
     *
     * @return true if this report and all subreports are successful, false otherwise
     */
    boolean isSuccess() {
        return success && subReports.stream().allMatch(FabutReport::isSuccess);
    }

    /**
     * Creates a new subreport with the given message and adds it to this report.
     *
     * @param subReport The message for the new subreport
     * @return The newly created subreport
     */
    FabutReport getSubReport(final FabutToString subReport) {
        Objects.requireNonNull(subReport, "Subreport message cannot be null");
        FabutReport newSubReport = new FabutReport(subReport);
        subReports.add(newSubReport);
        return newSubReport;
    }

    /**
     * Gets the complete message for this report.
     *
     * @return The formatted message string
     */
    String getMessage() {
        String baseMessage = getMessage(0);
        String entityChangesMessage = getEntityChangesMessage();
        String separator = "=".repeat(60);

        // Combine entity changes with regular messages
        if (!entityChangesMessage.isEmpty() && !baseMessage.isEmpty()) {
            return entityChangesMessage + "\n" + separator + "\n" + baseMessage;
        } else if (!entityChangesMessage.isEmpty()) {
            return entityChangesMessage;
        }
        return baseMessage;
    }

    /**
     * Gets the message for this report at the specified depth.
     *
     * @param depth The depth level for indentation
     * @return The formatted message string
     */
    private String getMessage(Integer depth) {
        final String spacer = NEW_LINE + StringUtils.repeat(DASH, depth * 2);

        // Build messages from this report
        StringBuilder sb = new StringBuilder();
        
        // Add regular messages
        String messagesText = messages.stream()
                .map(FabutToString::fabutToString)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.joining(spacer));
        sb.append(messagesText);
        
        // Add code messages if present
        if (!codes.isEmpty()) {
            sb.append("\nCODE:");
            codes.stream().map(ReportCode::code).forEach(sb::append);
        }
        
        // Add failed subreport messages
        List<String> failedSubreports = subReports.stream()
                .filter(report -> !report.isSuccess())
                .map(report -> report.getMessage(depth + 1))
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());

        if (!failedSubreports.isEmpty()) {
            if (!messagesText.isEmpty()) {
                sb.append(NEW_LINE);
            }
            sb.append(String.join(NEW_LINE, failedSubreports));
        }
        
        return sb.toString();
    }

    /**
     * Add new comment to specified depth.
     *
     * @param comment to be added
     * @param type type of comment
     */
    private void addComment(final String comment, final CommentType type) {
        Objects.requireNonNull(comment, "Comment cannot be null");
        Objects.requireNonNull(type, "Comment type cannot be null");
        
        // Cache the string value to avoid recomputation in the lambda
        final String prefix = type.getMark() + ARROW;
        messages.add(() -> prefix + comment);

        if (type == CommentType.FAIL) {
            success = false;
        }
    }
    
    /**
     * Add a comment with lazy evaluation of the message.
     * This is useful for expensive string operations that might not be needed if the report is filtered out.
     *
     * @param commentSupplier supplier for the comment string
     * @param type type of comment
     */
    private void addLazyComment(final Supplier<String> commentSupplier, final CommentType type) {
        Objects.requireNonNull(commentSupplier, "Comment supplier cannot be null");
        Objects.requireNonNull(type, "Comment type cannot be null");
        
        final String prefix = type.getMark() + ARROW;
        messages.add(() -> prefix + commentSupplier.get());

        if (type == CommentType.FAIL) {
            success = false;
        }
    }

    /**
     * Reports a list size mismatch.
     *
     * @param propertyName The name of the list property
     * @param expectedSize The expected size
     * @param actualSize The actual size
     */
    void listDifferentSizeComment(final String propertyName, final int expectedSize, final int actualSize) {
        addLazyComment(
            () -> String.format("Expected size for list: %s is: %d, but was: %d", propertyName, expectedSize, actualSize),
            CommentType.FAIL
        );
    }

    /**
     * Reports that no property was found for a field.
     *
     * @param fieldOwner The owner of the field
     * @param fieldName The name of the field
     * @param field The field value
     */
    void noPropertyForField(final Object fieldOwner, final String fieldName, final Object field) {
        addLazyComment(
            () -> String.format("There was no property for field: %s of class: %s, with value: %s", 
                                fieldName, 
                                fieldOwner.getClass().getSimpleName(), 
                                field),
            CommentType.FAIL
        );
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
        final String comment = String.format("%s: expected: %s but was: %s", propertyName, expected, actual);
        addComment(comment, CommentType.FAIL);
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

    /**
     * Adds a code to this report.
     *
     * @param code The code to add
     */
    void addCode(ReportCode code) {
        if (code != null) {
            codes.add(code);
        }
    }
    
    /**
     * Returns the number of messages in this report.
     *
     * @return The message count
     */
    int getMessageCount() {
        return messages.size();
    }
    
    /**
     * Returns the number of subreports.
     *
     * @return The subreport count
     */
    int getSubreportCount() {
        return subReports.size();
    }

    /**
     * Sets the assertion context for code generation.
     */
    void setAssertionContext(AssertionContext context) {
        this.assertionContext = context;
    }

    /**
     * Marks this report as failed (for including CODE output).
     */
    void markAsFailed() {
        this.success = false;
    }

    /**
     * Gets the assertion context for code generation.
     */
    AssertionContext getAssertionContext() {
        return assertionContext;
    }

    /**
     * Records an entity change for grouped reporting.
     *
     * @param changeType The type of change (CREATED, DELETED)
     * @param entityPath The unique path/identifier of the entity
     * @param entityClass The class of the entity
     * @param details Additional details about the change
     * @param suggestedFix The suggested code to fix this issue
     */
    void recordEntityChange(EntityChangeType changeType, String entityPath, Class<?> entityClass,
                           String details, String suggestedFix) {
        recordEntityChange(changeType, entityPath, entityClass, details, suggestedFix, null);
    }

    /**
     * Records an entity change for grouped reporting with generated code.
     */
    void recordEntityChange(EntityChangeType changeType, String entityPath, Class<?> entityClass,
                           String details, String suggestedFix, String code) {
        entityChanges.computeIfAbsent(changeType, k -> new CopyOnWriteArrayList<>())
                .add(new EntityChange(entityPath, entityClass, details, suggestedFix, code));
        success = false;
    }

    /**
     * Checks if there are any recorded entity changes.
     */
    boolean hasEntityChanges() {
        return !entityChanges.isEmpty();
    }

    /**
     * Gets the formatted entity changes section for the report.
     */
    String getEntityChangesMessage() {
        if (entityChanges.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String separator = "=".repeat(60);
        boolean firstGroup = true;

        for (EntityChangeType changeType : EntityChangeType.values()) {
            List<EntityChange> changes = entityChanges.get(changeType);
            if (changes != null && !changes.isEmpty()) {
                if (!firstGroup) {
                    sb.append("\n").append(separator).append("\n");
                }
                firstGroup = false;

                sb.append(changeType.getLabel()).append(": ");
                for (int i = 0; i < changes.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(changes.get(i).entityPath());
                }

                // For CREATED entities, show CODE; for others, show suggestedFix
                if (changeType == EntityChangeType.CREATED) {
                    for (EntityChange change : changes) {
                        if (change.code() != null && !change.code().isEmpty()) {
                            sb.append(change.code());
                        }
                    }
                } else {
                    String fix = changes.getFirst().suggestedFix();
                    if (fix != null && !fix.isEmpty()) {
                        sb.append("\n  -> ").append(fix);
                    }
                }
            }
        }

        return sb.toString();
    }
}

/**
 * Interface for providing code reports.
 */
@FunctionalInterface
interface ReportCode {
    /**
     * Provides a code string for the report.
     *
     * @return The code string
     */
    String code();
}

/**
 * Record representing an entity change with all relevant information for error reporting.
 *
 * @param entityPath The unique path/identifier of the entity
 * @param entityClass The class of the entity
 * @param details Additional details about the change
 * @param suggestedFix The suggested code to fix this issue
 * @param code The generated code for this entity (used for CREATED entities)
 */
record EntityChange(String entityPath, Class<?> entityClass, String details, String suggestedFix, String code) {}
