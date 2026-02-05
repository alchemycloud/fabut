package cloud.alchemy.fabut;

import cloud.alchemy.fabut.enums.AssertionContext;
import cloud.alchemy.fabut.enums.EntityChangeType;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

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
    private static final String ARROW = " ";
    private static final String DASH = "-";
    private static final String NEW_LINE = "\n";
    
    // State variables
    private boolean success = true;

    // Using ArrayList - tests are single-threaded, no need for thread-safe collections
    private final List<FabutReport> subReports = new ArrayList<>();
    private final List<FabutToString> messages = new ArrayList<>();
    private final List<ReportCode> codes = new ArrayList<>();

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
        if (!success) return false;
        for (FabutReport subReport : subReports) {
            if (!subReport.isSuccess()) return false;
        }
        return true;
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
        // getMessage(0) now includes entity changes at all levels
        return getMessage(0);
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

        // Add regular messages using for loop
        boolean firstMessage = true;
        for (FabutToString msg : messages) {
            String text = msg.fabutToString();
            if (!text.isEmpty()) {
                if (!firstMessage) {
                    sb.append(spacer);
                }
                sb.append(text);
                firstMessage = false;
            }
        }

        // Add entity changes (DELETED, CREATED, UPDATED) for this report
        String entityChangesText = getEntityChangesMessage();
        if (!entityChangesText.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(NEW_LINE);
            }
            sb.append(entityChangesText);
        }

        // Add code messages if present
        if (!codes.isEmpty()) {
            sb.append("\nCODE:");
            for (ReportCode code : codes) {
                sb.append(code.code());
            }
        }

        // Add failed subreport messages
        boolean hasMessages = sb.length() > 0;
        boolean firstSubreport = true;
        for (FabutReport report : subReports) {
            if (!report.isSuccess()) {
                String text = report.getMessage(depth + 1);
                if (!text.isEmpty()) {
                    if (firstSubreport && hasMessages) {
                        sb.append(NEW_LINE);
                    } else if (!firstSubreport) {
                        sb.append(NEW_LINE);
                    }
                    sb.append(text);
                    firstSubreport = false;
                }
            }
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

    void listDifferentSizeComment(final String propertyName, final int expectedSize, final int actualSize) {
        addLazyComment(
            () -> "LIST SIZE MISMATCH: " + propertyName + " expected size: " + expectedSize + ", but was: " + actualSize,
            CommentType.FAIL
        );
    }

    void noPropertyForField(final Object fieldOwner, final String fieldName, final Object field) {
        addLazyComment(
            () -> "UNASSERTED FIELD: " + fieldOwner.getClass().getSimpleName() + "." + fieldName + " = " + formatFieldValue(field)
                + "\n    Fix: add value(\"" + fieldName + "\", ...) or ignored(\"" + fieldName + "\")",
            CommentType.FAIL
        );
    }

    void notNullProperty(final String fieldName) {
        addComment(fieldName + ": expected not null, but was null", CommentType.FAIL);
    }

    void nullProperty(final String fieldName, final Object actual) {
        addLazyComment(
            () -> fieldName + ": expected null, but was: " + formatFieldValue(actual),
            CommentType.FAIL
        );
    }

    void notEmptyProperty(final String fieldName, final Object actual) {
        addLazyComment(
            () -> fieldName + ": expected non-empty Optional, but was: " + formatFieldValue(actual),
            CommentType.FAIL
        );
    }

    void emptyProperty(final String fieldName, final Object actual) {
        addLazyComment(
            () -> fieldName + ": expected empty Optional, but was: " + formatFieldValue(actual),
            CommentType.FAIL
        );
    }

    void reportIgnoreProperty(final String fieldName) {
        addComment(fieldName + ": is ignored field", CommentType.IGNORED);
    }

    void checkByReference(final String fieldName, final Object object) {
        addComment("Property:  " + fieldName + " of class:  " + object.getClass().getSimpleName() + " has wrong reference.", CommentType.FAIL);
    }

    void ignoredType(final Class<?> clazz) {
        addComment("Type  " + clazz.getSimpleName() + " is ignored type.", CommentType.IGNORED);
    }

    void assertingListElement(final String listName, final int index) {
        addComment("Asserting object at index [" + index + "] of list " + listName + ".", CommentType.COLLECTION);
    }

    void noEntityInSnapshot(final Object entity) {
        addLazyComment(
            () -> "NOT IN SNAPSHOT: " + entity
                + "\n    Entity was not present when takeSnapshot() was called."
                + "\n    Fix: call takeSnapshot() after creating this entity, or use assertObject() for new entities",
            CommentType.FAIL
        );
    }

    void entityInSnapshot(final Object entity) {
        addLazyComment(
            () -> "ALREADY IN SNAPSHOT: " + entity
                + "\n    This entity existed before takeSnapshot(). Use assertEntityWithSnapshot() instead of assertObject()",
            CommentType.FAIL
        );
    }

    void entityNotAssertedInAfterState(final Object entity) {
        addLazyComment(
            () -> "UNASSERTED ENTITY: " + entity
                + " was created after takeSnapshot() but not asserted."
                + "\n    Fix: add assertObject(...) or ignoreEntity(...)",
            CommentType.FAIL
        );
    }

    void uncallableMethod(final Method method, final Object actual) {
        addComment("There is no method: " + method.getName() + " in actual object class: " + actual.getClass() +
                   " (expected object class was: " + method.getDeclaringClass().getSimpleName() + ").", CommentType.FAIL);
    }

    void notNecessaryAssert(final String propertyName, final Object actual, final Object fieldValue) {
        addLazyComment(
            () -> "UNNECESSARY ASSERT: " + actual.getClass().getSimpleName() + "." + propertyName
                + " was not modified (value: " + formatFieldValue(fieldValue) + ")"
                + "\n    Fix: remove this assertion, or verify the test action modifies this field",
            CommentType.FAIL
        );
    }

    void nullReference() {
        addComment("Object that was passed to assertObject was null, it must not be null!", CommentType.FAIL);
    }

    void assertFail(final String propertyName, final Object expected, final Object actual) {
        addComment(propertyName + ": expected: " + expected + " but was: " + actual, CommentType.FAIL);
    }

    void assertFailFormatted(final String propertyName, final Supplier<String> expectedSupplier, final Supplier<String> actualSupplier) {
        addLazyComment(
            () -> propertyName + ": expected: " + expectedSupplier.get() + " but was: " + actualSupplier.get(),
            CommentType.FAIL
        );
    }

    void idNull(final Class<?> clazz) {
        addComment("Id of " + clazz.getSimpleName() + " cannot be null", CommentType.FAIL);
    }

    void notDeletedInRepository(final Object entity) {
        addLazyComment(
            () -> "NOT DELETED: " + entity + " still exists in repository"
                + "\n    Fix: verify the test action actually deletes this entity",
            CommentType.FAIL
        );
    }

    void noCopy(final Object entity) {
        addComment("Entity: " + entity + " cannot be copied into snapshot", CommentType.FAIL);
    }

    void excessExpectedMap(final Object key) {
        addComment("No match for expected key: " + key, CommentType.FAIL);
    }

    void excessActualMap(final Object key) {
        addComment("No match for actual key: " + key, CommentType.FAIL);
    }

    void excessExpectedProperty(final String path) {
        addComment("UNKNOWN PROPERTY: \"" + path + "\" does not match any field on the object", CommentType.FAIL);
    }

    void excessExpectedProperty(final String path, final List<String> availableFields) {
        addLazyComment(
            () -> {
                StringBuilder sb = new StringBuilder("UNKNOWN PROPERTY: \"" + path + "\" does not match any field");
                String closest = findClosestMatch(path, availableFields);
                if (closest != null) {
                    sb.append("\n    Did you mean: \"").append(closest).append("\"?");
                }
                if (!availableFields.isEmpty()) {
                    sb.append("\n    Available fields: ").append(String.join(", ", availableFields));
                }
                return sb.toString();
            },
            CommentType.FAIL
        );
    }

    void assertingMapKey(final Object key) {
        addComment("Map key: " + key, CommentType.COLLECTION);
    }

    <T> void assertWithSnapshotMustHaveAtLeastOnChange(T entity) {
        addLazyComment(
            () -> "assertEntityWithSnapshot() called with 0 property assertions on: " + entity
                + "\n    Fix: specify which fields changed, or use ignoreEntity() if no changes expected",
            CommentType.FAIL
        );
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
        entityChanges.computeIfAbsent(changeType, k -> new ArrayList<>())
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

        // Summary header
        List<String> parts = new ArrayList<>();
        for (EntityChangeType changeType : EntityChangeType.values()) {
            List<EntityChange> changes = entityChanges.get(changeType);
            if (changes != null && !changes.isEmpty()) {
                parts.add(changes.size() + " " + changeType.getLabel().toLowerCase());
            }
        }
        sb.append("SNAPSHOT VIOLATION: ").append(String.join(", ", parts));
        sb.append("\n").append(separator);

        for (EntityChangeType changeType : EntityChangeType.values()) {
            List<EntityChange> changes = entityChanges.get(changeType);
            if (changes != null && !changes.isEmpty()) {
                sb.append("\n").append(changeType.getLabel()).append(":");
                for (EntityChange change : changes) {
                    sb.append("\n  ").append(change.entityPath());
                    // Show suggested fix or code for each entity
                    if (changeType == EntityChangeType.CREATED) {
                        if (change.code() != null && !change.code().isEmpty()) {
                            sb.append(change.code());
                        }
                    } else {
                        if (change.suggestedFix() != null && !change.suggestedFix().isEmpty()) {
                            sb.append("\n    -> ").append(change.suggestedFix());
                        }
                    }
                }
            }
        }

        return sb.toString();
    }

    // ==================== Helper Methods ====================

    /**
     * Formats a field value for display in error messages.
     */
    private static String formatFieldValue(final Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Optional<?> opt) {
            return opt.isEmpty() ? "Optional.empty" : "Optional[" + formatFieldValue(opt.get()) + "]";
        }
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        if (value.getClass().isEnum()) {
            return value.getClass().getSimpleName() + "." + value;
        }
        return String.valueOf(value);
    }

    /**
     * Finds the closest matching field name using edit distance.
     * Returns null if no close match found (distance > 3).
     */
    static String findClosestMatch(final String input, final List<String> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        String best = null;
        int bestDistance = Integer.MAX_VALUE;
        final String inputLower = input.toLowerCase();
        for (String candidate : candidates) {
            int distance = editDistance(inputLower, candidate.toLowerCase());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        // Only suggest if reasonably close (max 3 edits or less than half the length)
        int threshold = Math.min(3, Math.max(1, input.length() / 2));
        return bestDistance <= threshold ? best : null;
    }

    /**
     * Computes Levenshtein edit distance between two strings.
     */
    private static int editDistance(final String a, final String b) {
        final int lenA = a.length();
        final int lenB = b.length();
        int[] prev = new int[lenB + 1];
        int[] curr = new int[lenB + 1];
        for (int j = 0; j <= lenB; j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= lenA; i++) {
            curr[0] = i;
            for (int j = 1; j <= lenB; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        return prev[lenB];
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
