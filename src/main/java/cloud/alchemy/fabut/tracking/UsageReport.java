package cloud.alchemy.fabut.tracking;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a human-readable usage report from tracked objects.
 * Groups objects by class and shows field-level access statistics.
 */
public class UsageReport {

    private final List<TrackedObject> trackedObjects;

    public UsageReport(List<TrackedObject> trackedObjects) {
        this.trackedObjects = Objects.requireNonNull(trackedObjects);
    }

    public boolean hasTrackedObjects() {
        return !trackedObjects.isEmpty();
    }

    public boolean hasUnderusedObjects() {
        return trackedObjects.stream().anyMatch(t -> !t.isFullyUsed());
    }

    public List<TrackedObject> getUnderusedObjects() {
        return trackedObjects.stream()
                .filter(t -> !t.isFullyUsed())
                .toList();
    }

    /**
     * Returns a summary grouped by class: class name, instance count, average usage.
     */
    public Map<String, ClassUsageSummary> getSummaryByClass() {
        var grouped = trackedObjects.stream()
                .collect(Collectors.groupingBy(t -> t.getObjectClass().getSimpleName()));

        var result = new LinkedHashMap<String, ClassUsageSummary>();
        for (var entry : grouped.entrySet()) {
            String className = entry.getKey();
            List<TrackedObject> objects = entry.getValue();
            int count = objects.size();
            double avgUsage = objects.stream()
                    .mapToDouble(TrackedObject::getUsagePercentage)
                    .average()
                    .orElse(0.0);

            // Find commonly unused fields (unused in majority of instances)
            Map<String, Integer> unusedCounts = new LinkedHashMap<>();
            for (TrackedObject obj : objects) {
                for (String field : obj.getUnusedFields()) {
                    unusedCounts.merge(field, 1, Integer::sum);
                }
            }
            int threshold = Math.max(1, count / 2);
            List<String> commonUnused = unusedCounts.entrySet().stream()
                    .filter(e -> e.getValue() >= threshold)
                    .map(Map.Entry::getKey)
                    .sorted()
                    .toList();

            result.put(className, new ClassUsageSummary(className, count, avgUsage, commonUnused));
        }
        return result;
    }

    /**
     * Generates the formatted report string.
     */
    public String generate() {
        if (trackedObjects.isEmpty()) {
            return "USAGE REPORT: No tracked objects.";
        }

        var sb = new StringBuilder();
        sb.append("USAGE REPORT:\n");

        var summaries = getSummaryByClass();
        for (var summary : summaries.values()) {
            sb.append("  ").append(summary.className())
                    .append(": ").append(summary.instanceCount())
                    .append(summary.instanceCount() == 1 ? " instance" : " instances")
                    .append(" fetched\n");

            if (summary.averageUsagePercent() >= 100.0) {
                sb.append("    Accessed: all fields ✓\n");
            } else {
                sb.append(String.format("    Avg usage: %.0f%%\n", summary.averageUsagePercent()));
                if (!summary.commonUnusedFields().isEmpty()) {
                    sb.append("    Commonly unused: ")
                            .append(String.join(", ", summary.commonUnusedFields()))
                            .append("\n");
                }
            }
        }

        // Detail section for never-accessed objects
        var neverAccessed = trackedObjects.stream()
                .filter(TrackedObject::isNeverAccessed)
                .toList();
        if (!neverAccessed.isEmpty()) {
            sb.append("  WARNING: ").append(neverAccessed.size())
                    .append(neverAccessed.size() == 1 ? " object" : " objects")
                    .append(" fetched but never accessed\n");
        }

        return sb.toString().stripTrailing();
    }

    /**
     * Returns classes whose average usage is below the given threshold.
     *
     * @param threshold percentage threshold (0-100)
     * @return list of summaries that violate the threshold
     */
    public List<ClassUsageSummary> getViolations(double threshold) {
        if (threshold <= 0) {
            return List.of();
        }
        return getSummaryByClass().values().stream()
                .filter(s -> s.averageUsagePercent() < threshold)
                .toList();
    }

    /**
     * Summary of usage for all instances of one class.
     */
    public record ClassUsageSummary(
            String className,
            int instanceCount,
            double averageUsagePercent,
            List<String> commonUnusedFields
    ) {}
}
