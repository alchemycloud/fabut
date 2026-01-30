package cloud.alchemy.fabut.diff;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the complete diff between two object states.
 * Generated at compile-time by the annotation processor, avoiding runtime reflection.
 *
 * <p>Usage example:
 * <pre>{@code
 * Diff diff = OrderDiff.compare(beforeOrder, afterOrder);
 * if (diff.hasChanges()) {
 *     System.out.println(diff.toConsoleReport());
 * }
 * }</pre>
 *
 * @param <T> the type of object being compared
 */
public class Diff<T> {

    private final Class<T> type;
    private final String objectIdentifier;
    private final List<FieldChange> changes;
    private final Map<String, FieldChange> changeMap;

    /**
     * Creates a new Diff for the given type.
     *
     * @param type the class of the compared objects
     * @param objectIdentifier human-readable identifier (e.g., "Order#42")
     */
    public Diff(Class<T> type, String objectIdentifier) {
        this.type = type;
        this.objectIdentifier = objectIdentifier;
        this.changes = new ArrayList<>();
        this.changeMap = new LinkedHashMap<>();
    }

    /**
     * Records a field comparison result.
     *
     * @param fieldName the field name
     * @param before the before value
     * @param after the after value
     * @return this Diff for chaining
     */
    public Diff<T> field(String fieldName, Object before, Object after) {
        FieldChange change = FieldChange.compare(fieldName, before, after);
        changes.add(change);
        changeMap.put(fieldName, change);
        return this;
    }

    /**
     * Returns true if any field was changed.
     */
    public boolean hasChanges() {
        return changes.stream().anyMatch(FieldChange::isChanged);
    }

    /**
     * Returns the number of changed fields.
     */
    public int changeCount() {
        return (int) changes.stream().filter(FieldChange::isChanged).count();
    }

    /**
     * Returns all field changes (including unchanged fields).
     */
    public List<FieldChange> getAllChanges() {
        return Collections.unmodifiableList(changes);
    }

    /**
     * Returns only the fields that were actually changed.
     */
    public List<FieldChange> getChangedFields() {
        return changes.stream()
                .filter(FieldChange::isChanged)
                .collect(Collectors.toList());
    }

    /**
     * Returns only the fields that were unchanged.
     */
    public List<FieldChange> getUnchangedFields() {
        return changes.stream()
                .filter(c -> !c.isChanged())
                .collect(Collectors.toList());
    }

    /**
     * Gets the change for a specific field.
     *
     * @param fieldName the field name
     * @return Optional containing the FieldChange, or empty if field not tracked
     */
    public Optional<FieldChange> getChange(String fieldName) {
        return Optional.ofNullable(changeMap.get(fieldName));
    }

    /**
     * Checks if a specific field was changed.
     *
     * @param fieldName the field name
     * @return true if the field was changed
     */
    public boolean isFieldChanged(String fieldName) {
        return getChange(fieldName).map(FieldChange::isChanged).orElse(false);
    }

    /**
     * Returns the type of objects being compared.
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns the object identifier.
     */
    public String getObjectIdentifier() {
        return objectIdentifier;
    }

    // ==================== Report Generation ====================

    /**
     * Generates a console-friendly text report of the diff.
     */
    public String toConsoleReport() {
        return DiffReportGenerator.toConsole(this);
    }

    /**
     * Generates an HTML report of the diff.
     */
    public String toHtmlReport() {
        return DiffReportGenerator.toHtml(this);
    }

    /**
     * Generates a compact one-line summary.
     */
    public String toSummary() {
        if (!hasChanges()) {
            return objectIdentifier + ": no changes";
        }
        List<String> changedNames = getChangedFields().stream()
                .map(FieldChange::fieldName)
                .collect(Collectors.toList());
        return objectIdentifier + ": " + changeCount() + " change(s) [" +
               String.join(", ", changedNames) + "]";
    }

    @Override
    public String toString() {
        return toSummary();
    }
}
