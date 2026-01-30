package cloud.alchemy.fabut.diff;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates multiple Diff results into a concise, actionable report.
 * Designed for scenarios with 20-30+ entity changes.
 *
 * <p>Usage:
 * <pre>{@code
 * BatchDiffReport report = new BatchDiffReport();
 * report.add(OrderDiff.compare(beforeOrder1, afterOrder1));
 * report.add(OrderDiff.compare(beforeOrder2, afterOrder2));
 * report.add(CustomerDiff.compare(beforeCustomer, afterCustomer));
 *
 * System.out.println(report.toCompactReport());
 * // Or for code suggestions:
 * System.out.println(report.toAssertionCode());
 * }</pre>
 */
public class BatchDiffReport {

    private final List<Diff<?>> diffs = new ArrayList<>();
    private final Map<Class<?>, List<Diff<?>>> byType = new LinkedHashMap<>();

    /**
     * Adds a diff to this batch report.
     */
    public BatchDiffReport add(Diff<?> diff) {
        if (diff != null) {
            diffs.add(diff);
            byType.computeIfAbsent(diff.getType(), k -> new ArrayList<>()).add(diff);
        }
        return this;
    }

    /**
     * Adds multiple diffs to this batch report.
     */
    public BatchDiffReport addAll(Collection<? extends Diff<?>> diffs) {
        diffs.forEach(this::add);
        return this;
    }

    /**
     * Returns true if any entity has changes.
     */
    public boolean hasChanges() {
        return diffs.stream().anyMatch(Diff::hasChanges);
    }

    /**
     * Returns the total number of entities with changes.
     */
    public int changedEntityCount() {
        return (int) diffs.stream().filter(Diff::hasChanges).count();
    }

    /**
     * Returns the total number of field changes across all entities.
     */
    public int totalFieldChanges() {
        return diffs.stream().mapToInt(Diff::changeCount).sum();
    }

    /**
     * Returns all diffs (including unchanged).
     */
    public List<Diff<?>> getAllDiffs() {
        return Collections.unmodifiableList(diffs);
    }

    /**
     * Returns only diffs with changes.
     */
    public List<Diff<?>> getChangedDiffs() {
        return diffs.stream()
                .filter(Diff::hasChanges)
                .collect(Collectors.toList());
    }

    // ==================== Report Formats ====================

    /**
     * Generates a compact summary report.
     * <pre>
     * === 23 entities changed (45 field changes) ===
     *
     * Order (15 changed):
     *   Order#1: status: "PENDING" → "SHIPPED"
     *   Order#2: status: "PENDING" → "SHIPPED", total: 99.99 → 149.99
     *   ...
     *
     * Customer (8 changed):
     *   Customer#5: email: "old@test.com" → "new@test.com"
     *   ...
     * </pre>
     */
    public String toCompactReport() {
        if (!hasChanges()) {
            return "✓ No changes detected (" + diffs.size() + " entities checked)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(changedEntityCount()).append(" entities changed (")
          .append(totalFieldChanges()).append(" field changes) ===\n");

        for (Map.Entry<Class<?>, List<Diff<?>>> entry : byType.entrySet()) {
            List<Diff<?>> changed = entry.getValue().stream()
                    .filter(Diff::hasChanges)
                    .collect(Collectors.toList());

            if (!changed.isEmpty()) {
                sb.append("\n").append(entry.getKey().getSimpleName())
                  .append(" (").append(changed.size()).append(" changed):\n");

                for (Diff<?> diff : changed) {
                    sb.append("  ").append(diff.toCompactReport()).append("\n");
                }
            }
        }

        return sb.toString().trim();
    }

    /**
     * Generates a one-line-per-entity summary.
     * <pre>
     * 23 changes: Order#1(status), Order#2(status,total), Customer#5(email), ...
     * </pre>
     */
    public String toOneLiner() {
        if (!hasChanges()) {
            return "✓ No changes";
        }

        List<String> summaries = diffs.stream()
                .filter(Diff::hasChanges)
                .map(Diff::toSummary)
                .collect(Collectors.toList());

        return changedEntityCount() + " changes: " + String.join(", ", summaries);
    }

    /**
     * Generates assertion code to fix all changes.
     * <pre>
     * // === 23 entities need assertions ===
     *
     * // Order (15 entities)
     * assertEntityWithSnapshot(order1,
     *     value("status", "SHIPPED"));
     * assertEntityWithSnapshot(order2,
     *     value("status", "SHIPPED"),
     *     value("total", 149.99));
     * ...
     * </pre>
     */
    public String toAssertionCode() {
        if (!hasChanges()) {
            return "// No changes - no assertions needed";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("// === ").append(changedEntityCount()).append(" entities need assertions ===\n");

        for (Map.Entry<Class<?>, List<Diff<?>>> entry : byType.entrySet()) {
            List<Diff<?>> changed = entry.getValue().stream()
                    .filter(Diff::hasChanges)
                    .collect(Collectors.toList());

            if (!changed.isEmpty()) {
                sb.append("\n// ").append(entry.getKey().getSimpleName())
                  .append(" (").append(changed.size()).append(" entities)\n");

                for (Diff<?> diff : changed) {
                    sb.append(diff.toAssertionCode()).append("\n");
                }
            }
        }

        return sb.toString().trim();
    }

    /**
     * Generates a summary grouped by change type (for quick scanning).
     * <pre>
     * MODIFIED (20):
     *   Order: #1, #2, #3, #4, #5 (status)
     *   Customer: #5, #10 (email)
     *
     * FIELDS SET (5):
     *   Order: #1.notes, #2.notes
     *
     * FIELDS CLEARED (3):
     *   Customer: #5.phone
     * </pre>
     */
    public String toChangeTypeSummary() {
        if (!hasChanges()) {
            return "✓ No changes";
        }

        // Group changes by type
        Map<FieldChange.ChangeType, List<String>> byChangeType = new EnumMap<>(FieldChange.ChangeType.class);

        for (Diff<?> diff : diffs) {
            for (FieldChange change : diff.getChangedFields()) {
                byChangeType.computeIfAbsent(change.changeType(), k -> new ArrayList<>())
                        .add(diff.getObjectIdentifier() + "." + change.fieldName());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== Change Summary ===\n");

        for (FieldChange.ChangeType type : FieldChange.ChangeType.values()) {
            if (type == FieldChange.ChangeType.UNCHANGED) continue;

            List<String> changes = byChangeType.get(type);
            if (changes != null && !changes.isEmpty()) {
                sb.append("\n").append(type.name()).append(" (").append(changes.size()).append("):\n");
                // Group by entity type
                sb.append("  ").append(String.join(", ", changes)).append("\n");
            }
        }

        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return toCompactReport();
    }
}
