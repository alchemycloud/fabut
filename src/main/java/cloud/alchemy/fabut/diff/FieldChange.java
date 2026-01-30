package cloud.alchemy.fabut.diff;

import java.util.Objects;

/**
 * Represents a change in a single field between two object states.
 * Immutable record capturing before/after values and change type.
 *
 * @param fieldName the name of the changed field
 * @param beforeValue the value before the change (null if field was added)
 * @param afterValue the value after the change (null if field was removed)
 * @param changeType the type of change detected
 */
public record FieldChange(
        String fieldName,
        Object beforeValue,
        Object afterValue,
        ChangeType changeType
) {

    /**
     * Types of changes that can occur to a field.
     */
    public enum ChangeType {
        /** Value was modified from one value to another */
        MODIFIED,
        /** Value changed from null to non-null */
        SET,
        /** Value changed from non-null to null */
        CLEARED,
        /** Value remained the same */
        UNCHANGED
    }

    /**
     * Creates a FieldChange by comparing two values.
     *
     * @param fieldName the field name
     * @param before the before value
     * @param after the after value
     * @return the FieldChange representing this comparison
     */
    public static FieldChange compare(String fieldName, Object before, Object after) {
        if (Objects.equals(before, after)) {
            return new FieldChange(fieldName, before, after, ChangeType.UNCHANGED);
        } else if (before == null) {
            return new FieldChange(fieldName, null, after, ChangeType.SET);
        } else if (after == null) {
            return new FieldChange(fieldName, before, null, ChangeType.CLEARED);
        } else {
            return new FieldChange(fieldName, before, after, ChangeType.MODIFIED);
        }
    }

    /**
     * Returns true if this field was changed (not UNCHANGED).
     */
    public boolean isChanged() {
        return changeType != ChangeType.UNCHANGED;
    }

    /**
     * Returns a human-readable description of this change.
     */
    public String describe() {
        return switch (changeType) {
            case UNCHANGED -> fieldName + ": unchanged (" + formatValue(beforeValue) + ")";
            case SET -> fieldName + ": null -> " + formatValue(afterValue);
            case CLEARED -> fieldName + ": " + formatValue(beforeValue) + " -> null";
            case MODIFIED -> fieldName + ": " + formatValue(beforeValue) + " -> " + formatValue(afterValue);
        };
    }

    private String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + value + "\"";
        return String.valueOf(value);
    }
}
