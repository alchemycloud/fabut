package cloud.alchemy.fabut.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Outcome of a single screen assertion, with everything an agent (or human) needs to understand and fix a failure.
 *
 * @param group           baseline sub-directory (e.g. {@code LoginUiTest/login})
 * @param name            screen name within the group
 * @param status          outcome
 * @param baseline        baseline image path
 * @param actual          actual image path (written for anything but {@link Status#MATCH})
 * @param diffImage       diff visualisation path, {@code null} if no comparison happened
 * @param compareImage    side-by-side expected | actual | diff path, {@code null} if no comparison happened
 * @param report          JSON report path
 * @param diff            comparison result, {@code null} for NEW / MISSING
 * @param clusterElements per {@link ImageDiff#clusters()} entry, the DOM element found at its center (may be null)
 * @param ariaDiff        changed lines of the ARIA snapshot ({@code -} expected, {@code +} actual)
 * @param stable          whether two consecutive screenshots were identical
 */
public record ScreenResult(String group, String name, Status status, Path baseline, Path actual, Path diffImage,
                           Path compareImage, Path report, ImageDiff diff, List<String> clusterElements,
                           List<String> ariaDiff, boolean stable) {

    private static final int MAX_LISTED_CLUSTERS = 8;
    private static final int MAX_ARIA_LINES = 30;

    public enum Status {
        /** Matches the baseline within the configured tolerance. */
        MATCH,
        /** No baseline existed; it was recorded. */
        NEW,
        /** Did not match; the baseline was overwritten because of {@code -Dfabut.ui.update=all}. */
        UPDATED,
        /** Did not match the baseline. */
        MISMATCH,
        /** No baseline exists and recording is disabled ({@code -Dfabut.ui.update=none} / CI). */
        MISSING
    }

    public boolean passed() {
        return status == Status.MATCH || status == Status.NEW || status == Status.UPDATED;
    }

    /** Multi-line, agent-friendly description of the result. */
    public String message() {
        StringBuilder sb = new StringBuilder();
        switch (status) {
            case MATCH -> sb.append("SCREEN MATCH: ").append(id());
            case NEW -> sb.append("SCREEN NEW BASELINE: ").append(id()).append("\n  recorded: ").append(baseline);
            case MISSING -> sb.append("SCREEN MISSING BASELINE: ").append(id())
                    .append("\n  expected: ").append(baseline).append(" (does not exist)")
                    .append("\n  actual:   ").append(actual)
                    .append("\n  record with: -D").append(UpdateMode.PROPERTY).append("=missing");
            case UPDATED, MISMATCH -> {
                sb.append(status == Status.UPDATED ? "SCREEN UPDATED: " : "SCREEN MISMATCH: ").append(id());
                sb.append("\n  ").append(status == Status.UPDATED ? "was: " : "").append(diff.failureReason());
                appendClusters(sb);
                appendRegions(sb);
                sb.append("\n  expected: ").append(baseline);
                sb.append("\n  actual:   ").append(actual);
                sb.append("\n  diff:     ").append(diffImage).append("  (red = changed, orange = changed in tolerated area, yellow = anti-aliasing, blue = ignored)");
                sb.append("\n  compare:  ").append(compareImage).append("  (expected | actual | diff)");
                appendAria(sb);
                if (status == Status.MISMATCH) {
                    sb.append("\n  if the change is intended, accept it with: -D").append(UpdateMode.PROPERTY).append("=all");
                    sb.append("\n  otherwise ignore dynamic parts with .ignore(\"selector\") or allow deviation with .maxDiffRatio(..)/.tolerate(..)");
                }
            }
        }
        if (!stable) {
            sb.append("\n  WARNING: page was still changing between screenshots (animations, loading, timers?)");
        }
        return sb.toString();
    }

    private String id() {
        return group + "/" + name;
    }

    private void appendClusters(StringBuilder sb) {
        if (diff.clusters().isEmpty()) return;
        sb.append("\n  changed areas (screenshot px):");
        for (int i = 0; i < Math.min(MAX_LISTED_CLUSTERS, diff.clusters().size()); i++) {
            sb.append("\n    #").append(i + 1).append(' ').append(diff.clusters().get(i));
            String element = i < clusterElements.size() ? clusterElements.get(i) : null;
            if (element != null) sb.append("  -> ").append(element);
        }
        if (diff.clusters().size() > MAX_LISTED_CLUSTERS) {
            sb.append("\n    ... ").append(diff.clusters().size() - MAX_LISTED_CLUSTERS).append(" more");
        }
    }

    private void appendRegions(StringBuilder sb) {
        for (ImageDiff.RegionResult r : diff.regionResults()) {
            sb.append(String.format(Locale.ROOT, "%n  tolerated %s: %d px (%.3f%%) of allowed %.3f%% %s",
                    r.region().label(), r.diffPixels(), r.diffRatio() * 100, r.region().maxDiffRatio() * 100,
                    r.passed() ? "ok" : "EXCEEDED"));
        }
    }

    private void appendAria(StringBuilder sb) {
        if (ariaDiff == null || ariaDiff.isEmpty()) return;
        sb.append("\n  accessibility tree changes:");
        for (int i = 0; i < Math.min(MAX_ARIA_LINES, ariaDiff.size()); i++) {
            sb.append("\n    ").append(ariaDiff.get(i));
        }
        if (ariaDiff.size() > MAX_ARIA_LINES) {
            sb.append("\n    ... ").append(ariaDiff.size() - MAX_ARIA_LINES).append(" more lines");
        }
    }

    /** Machine readable report (written next to the actual image). */
    public String toJson() {
        StringBuilder sb = new StringBuilder("{\n");
        field(sb, "group", group).append(",\n");
        field(sb, "name", name).append(",\n");
        field(sb, "status", status.name()).append(",\n");
        sb.append("  \"passed\": ").append(passed()).append(",\n");
        sb.append("  \"stable\": ").append(stable).append(",\n");
        field(sb, "baseline", str(baseline)).append(",\n");
        field(sb, "actual", str(actual)).append(",\n");
        field(sb, "diffImage", str(diffImage)).append(",\n");
        field(sb, "compareImage", str(compareImage));
        if (diff != null) {
            sb.append(",\n");
            sb.append("  \"expectedSize\": [").append(diff.expectedWidth()).append(", ").append(diff.expectedHeight()).append("],\n");
            sb.append("  \"actualSize\": [").append(diff.actualWidth()).append(", ").append(diff.actualHeight()).append("],\n");
            sb.append("  \"diffPixels\": ").append(diff.diffPixels()).append(",\n");
            sb.append("  \"diffRatio\": ").append(String.format(Locale.ROOT, "%.6f", diff.diffRatio())).append(",\n");
            sb.append("  \"antialiasedPixels\": ").append(diff.antialiasedPixels()).append(",\n");
            sb.append("  \"ignoredPixels\": ").append(diff.ignoredPixels()).append(",\n");
            field(sb, "failureReason", diff.failureReason()).append(",\n");
            sb.append("  \"clusters\": [");
            for (int i = 0; i < diff.clusters().size(); i++) {
                DiffCluster c = diff.clusters().get(i);
                sb.append(i == 0 ? "\n" : ",\n").append("    {\"x\": ").append(c.x()).append(", \"y\": ").append(c.y())
                        .append(", \"width\": ").append(c.width()).append(", \"height\": ").append(c.height())
                        .append(", \"pixels\": ").append(c.pixels()).append(", \"element\": ")
                        .append(quote(i < clusterElements.size() ? clusterElements.get(i) : null)).append('}');
            }
            sb.append(diff.clusters().isEmpty() ? "],\n" : "\n  ],\n");
            sb.append("  \"regions\": [");
            for (int i = 0; i < diff.regionResults().size(); i++) {
                ImageDiff.RegionResult r = diff.regionResults().get(i);
                sb.append(i == 0 ? "\n" : ",\n").append("    {\"label\": ").append(quote(r.region().label()))
                        .append(", \"diffPixels\": ").append(r.diffPixels()).append(", \"area\": ").append(r.area())
                        .append(", \"maxDiffRatio\": ").append(r.region().maxDiffRatio())
                        .append(", \"passed\": ").append(r.passed()).append('}');
            }
            sb.append(diff.regionResults().isEmpty() ? "]" : "\n  ]");
        }
        if (ariaDiff != null && !ariaDiff.isEmpty()) {
            sb.append(",\n  \"ariaDiff\": [");
            for (int i = 0; i < ariaDiff.size(); i++) {
                sb.append(i == 0 ? "\n    " : ",\n    ").append(quote(ariaDiff.get(i)));
            }
            sb.append("\n  ]");
        }
        return sb.append("\n}\n").toString();
    }

    private static String str(Path p) {
        return p == null ? null : p.toString();
    }

    private static StringBuilder field(StringBuilder sb, String key, String value) {
        return sb.append("  \"").append(key).append("\": ").append(quote(value));
    }

    static String quote(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }
}
