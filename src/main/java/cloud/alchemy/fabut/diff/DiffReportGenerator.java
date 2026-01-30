package cloud.alchemy.fabut.diff;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates visual diff reports in multiple formats.
 * Supports console output with box-drawing characters and HTML with styling.
 */
public final class DiffReportGenerator {

    private DiffReportGenerator() {}

    // ==================== Console Report ====================

    /**
     * Generates a console-friendly visual diff report.
     *
     * @param diff the diff to report
     * @return formatted string with box-drawing characters
     */
    public static <T> String toConsole(Diff<T> diff) {
        StringBuilder sb = new StringBuilder();
        String title = diff.getObjectIdentifier() + " - " + getStatusLabel(diff);
        int width = Math.max(60, title.length() + 4);

        // Header
        sb.append("\n");
        sb.append("╔").append("═".repeat(width)).append("╗\n");
        sb.append("║ ").append(padRight(title, width - 1)).append("║\n");
        sb.append("╠").append("═".repeat(width)).append("╣\n");

        // Column headers
        String header = String.format(" %-15s │ %-18s │ %-18s │ %s",
                "Field", "Before", "After", "Status");
        sb.append("║").append(padRight(header, width)).append("║\n");
        sb.append("╠").append("─".repeat(width)).append("╣\n");

        // Field rows
        for (FieldChange change : diff.getAllChanges()) {
            String row = formatFieldRow(change);
            sb.append("║").append(padRight(row, width)).append("║\n");
        }

        // Footer
        sb.append("╚").append("═".repeat(width)).append("╝\n");

        // Summary
        sb.append(formatSummary(diff));

        return sb.toString();
    }

    private static String getStatusLabel(Diff<?> diff) {
        if (!diff.hasChanges()) {
            return "UNCHANGED";
        }
        return diff.changeCount() + " CHANGE(S)";
    }

    private static String formatFieldRow(FieldChange change) {
        String beforeStr = truncate(formatValue(change.beforeValue()), 18);
        String afterStr = truncate(formatValue(change.afterValue()), 18);
        String status = getStatusIcon(change);

        return String.format(" %-15s │ %-18s │ %-18s │ %s",
                truncate(change.fieldName(), 15),
                beforeStr,
                afterStr,
                status);
    }

    private static String getStatusIcon(FieldChange change) {
        return switch (change.changeType()) {
            case UNCHANGED -> "  unchanged";
            case MODIFIED -> "✗ MODIFIED";
            case SET -> "✗ SET";
            case CLEARED -> "✗ CLEARED";
        };
    }

    private static <T> String formatSummary(Diff<T> diff) {
        StringBuilder sb = new StringBuilder();
        if (diff.hasChanges()) {
            sb.append("\n Changed fields: ");
            List<FieldChange> changed = diff.getChangedFields();
            for (int i = 0; i < changed.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(changed.get(i).fieldName());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ==================== HTML Report ====================

    /**
     * Generates an HTML diff report with styling.
     *
     * @param diff the diff to report
     * @return HTML string
     */
    public static <T> String toHtml(Diff<T> diff) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Diff Report - %s</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        margin: 20px;
                        background: #f5f5f5;
                    }
                    .diff-container {
                        background: white;
                        border-radius: 8px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                        overflow: hidden;
                        max-width: 900px;
                    }
                    .diff-header {
                        background: %s;
                        color: white;
                        padding: 16px 20px;
                        font-size: 18px;
                        font-weight: 600;
                    }
                    .diff-header .subtitle {
                        font-size: 14px;
                        font-weight: normal;
                        opacity: 0.9;
                        margin-top: 4px;
                    }
                    table {
                        width: 100%%;
                        border-collapse: collapse;
                    }
                    th {
                        background: #f8f9fa;
                        padding: 12px 16px;
                        text-align: left;
                        font-weight: 600;
                        border-bottom: 2px solid #e9ecef;
                    }
                    td {
                        padding: 12px 16px;
                        border-bottom: 1px solid #e9ecef;
                        font-family: 'Monaco', 'Menlo', monospace;
                        font-size: 13px;
                    }
                    tr:hover {
                        background: #f8f9fa;
                    }
                    .field-name {
                        font-weight: 600;
                        color: #495057;
                    }
                    .value-before {
                        color: #dc3545;
                        background: #fff5f5;
                        padding: 2px 6px;
                        border-radius: 3px;
                    }
                    .value-after {
                        color: #28a745;
                        background: #f0fff4;
                        padding: 2px 6px;
                        border-radius: 3px;
                    }
                    .value-unchanged {
                        color: #6c757d;
                    }
                    .status {
                        font-weight: 600;
                        padding: 4px 8px;
                        border-radius: 4px;
                        font-size: 11px;
                        text-transform: uppercase;
                    }
                    .status-unchanged {
                        background: #e9ecef;
                        color: #6c757d;
                    }
                    .status-modified {
                        background: #fff3cd;
                        color: #856404;
                    }
                    .status-set {
                        background: #d4edda;
                        color: #155724;
                    }
                    .status-cleared {
                        background: #f8d7da;
                        color: #721c24;
                    }
                    .summary {
                        padding: 16px 20px;
                        background: #f8f9fa;
                        border-top: 1px solid #e9ecef;
                        font-size: 14px;
                        color: #495057;
                    }
                    .timestamp {
                        color: #6c757d;
                        font-size: 12px;
                        margin-top: 8px;
                    }
                </style>
            </head>
            <body>
                <div class="diff-container">
                    <div class="diff-header">
                        %s
                        <div class="subtitle">%s</div>
                    </div>
                    <table>
                        <thead>
                            <tr>
                                <th>Field</th>
                                <th>Before</th>
                                <th>After</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
            """.formatted(
                escapeHtml(diff.getObjectIdentifier()),
                diff.hasChanges() ? "#e67e22" : "#27ae60",
                escapeHtml(diff.getObjectIdentifier()),
                diff.hasChanges() ? diff.changeCount() + " field(s) changed" : "No changes detected"
        ));

        // Table rows
        for (FieldChange change : diff.getAllChanges()) {
            sb.append(formatHtmlRow(change));
        }

        // Footer
        sb.append("""
                        </tbody>
                    </table>
                    <div class="summary">
                        <strong>Type:</strong> %s<br>
                        <strong>Total fields:</strong> %d |
                        <strong>Changed:</strong> %d |
                        <strong>Unchanged:</strong> %d
                        <div class="timestamp">Generated: %s</div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                diff.getType().getSimpleName(),
                diff.getAllChanges().size(),
                diff.changeCount(),
                diff.getUnchangedFields().size(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));

        return sb.toString();
    }

    private static String formatHtmlRow(FieldChange change) {
        String statusClass = switch (change.changeType()) {
            case UNCHANGED -> "status-unchanged";
            case MODIFIED -> "status-modified";
            case SET -> "status-set";
            case CLEARED -> "status-cleared";
        };

        String beforeClass = change.isChanged() ? "value-before" : "value-unchanged";
        String afterClass = change.isChanged() ? "value-after" : "value-unchanged";

        return """
                <tr>
                    <td class="field-name">%s</td>
                    <td><span class="%s">%s</span></td>
                    <td><span class="%s">%s</span></td>
                    <td><span class="status %s">%s</span></td>
                </tr>
            """.formatted(
                escapeHtml(change.fieldName()),
                beforeClass,
                escapeHtml(formatValue(change.beforeValue())),
                afterClass,
                escapeHtml(formatValue(change.afterValue())),
                statusClass,
                change.changeType().name()
        );
    }

    // ==================== Utilities ====================

    private static String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + value + "\"";
        return String.valueOf(value);
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "null";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    private static String padRight(String s, int length) {
        if (s.length() >= length) return s;
        return s + " ".repeat(length - s.length());
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
