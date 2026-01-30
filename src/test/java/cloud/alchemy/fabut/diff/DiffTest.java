package cloud.alchemy.fabut.diff;

import cloud.alchemy.fabut.model.AssertableEntity;
import cloud.alchemy.fabut.model.AssertableEntityDiff;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for compile-time Diff generation and visual reports.
 */
class DiffTest {

    @Test
    void compareDetectsNoChanges() {
        // Given
        AssertableEntity before = createEntity(1L, "Test", 10, "Desc", 5);
        AssertableEntity after = createEntity(1L, "Test", 10, "Desc", 5);

        // When
        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);

        // Then
        assertFalse(diff.hasChanges());
        assertEquals(0, diff.changeCount());
        assertEquals(5, diff.getAllChanges().size());
        assertTrue(diff.getChangedFields().isEmpty());
    }

    @Test
    void compareDetectsModifiedField() {
        // Given
        AssertableEntity before = createEntity(1L, "Before", 10, "Desc", 5);
        AssertableEntity after = createEntity(1L, "After", 10, "Desc", 5);

        // When
        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);

        // Then
        assertTrue(diff.hasChanges());
        assertEquals(1, diff.changeCount());
        assertTrue(diff.isFieldChanged("name"));
        assertFalse(diff.isFieldChanged("id"));

        FieldChange nameChange = diff.getChange("name").orElseThrow();
        assertEquals(FieldChange.ChangeType.MODIFIED, nameChange.changeType());
        assertEquals("Before", nameChange.beforeValue());
        assertEquals("After", nameChange.afterValue());
    }

    @Test
    void compareDetectsSetField() {
        // Given
        AssertableEntity before = createEntity(1L, null, 10, "Desc", 5);
        AssertableEntity after = createEntity(1L, "New Name", 10, "Desc", 5);

        // When
        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);

        // Then
        assertTrue(diff.hasChanges());
        FieldChange nameChange = diff.getChange("name").orElseThrow();
        assertEquals(FieldChange.ChangeType.SET, nameChange.changeType());
    }

    @Test
    void compareDetectsClearedField() {
        // Given
        AssertableEntity before = createEntity(1L, "Has Name", 10, "Desc", 5);
        AssertableEntity after = createEntity(1L, null, 10, "Desc", 5);

        // When
        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);

        // Then
        assertTrue(diff.hasChanges());
        FieldChange nameChange = diff.getChange("name").orElseThrow();
        assertEquals(FieldChange.ChangeType.CLEARED, nameChange.changeType());
    }

    @Test
    void compareDetectsMultipleChanges() {
        // Given
        AssertableEntity before = createEntity(1L, "Before", 10, "Old Desc", 5);
        AssertableEntity after = createEntity(1L, "After", 20, "New Desc", 99);

        // When
        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);

        // Then
        assertTrue(diff.hasChanges());
        assertEquals(4, diff.changeCount());
        assertTrue(diff.isFieldChanged("name"));
        assertTrue(diff.isFieldChanged("count"));
        assertTrue(diff.isFieldChanged("description"));
        assertTrue(diff.isFieldChanged("score"));
        assertFalse(diff.isFieldChanged("id"));
    }

    @Test
    void consoleReportShowsAllFields() {
        // Given
        AssertableEntity before = createEntity(1L, "Before", 10, "Desc", 5);
        AssertableEntity after = createEntity(1L, "After", 20, "Desc", 5);

        // When
        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);
        String report = diff.toConsoleReport();

        // Then
        System.out.println(report); // Visual inspection
        assertTrue(report.contains("name"));
        assertTrue(report.contains("Before"));
        assertTrue(report.contains("After"));
        assertTrue(report.contains("MODIFIED"));
        assertTrue(report.contains("unchanged"));
    }

    @Test
    void htmlReportGeneratesValidHtml() throws IOException {
        // Given
        AssertableEntity before = createEntity(1L, "Before", 10, "Old", 5);
        AssertableEntity after = createEntity(1L, "After", 20, "New", 99);

        // When
        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);
        String html = diff.toHtmlReport();

        // Then
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("AssertableEntity"));
        assertTrue(html.contains("Before"));
        assertTrue(html.contains("After"));
        assertTrue(html.contains("MODIFIED"));

        // Optionally write to file for visual inspection
        Path reportPath = Path.of("target/diff-report.html");
        Files.writeString(reportPath, html);
        System.out.println("HTML report written to: " + reportPath.toAbsolutePath());
    }

    @Test
    void summaryShowsChangedFields() {
        // Given
        AssertableEntity before = createEntity(1L, "Before", 10, "Desc", 5);
        AssertableEntity after = createEntity(1L, "After", 10, "Desc", 5);

        // When
        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);
        String summary = diff.toSummary();

        // Then
        System.out.println(summary);
        assertTrue(summary.contains("1 change"));
        assertTrue(summary.contains("name"));
    }

    @Test
    void customIdentifierInReport() {
        // Given
        AssertableEntity before = createEntity(1L, "Before", 10, "Desc", 5);
        AssertableEntity after = createEntity(1L, "After", 10, "Desc", 5);

        // When
        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after, "Order #42 (Customer: John)");
        String report = diff.toConsoleReport();

        // Then
        System.out.println(report);
        assertTrue(report.contains("Order #42 (Customer: John)"));
    }

    // ==================== Compact Report Tests ====================

    @Test
    void compactReportShowsOnlyChanges() {
        AssertableEntity before = createEntity(1L, "Before", 10, "Desc", 5);
        AssertableEntity after = createEntity(1L, "After", 20, "Desc", 5);

        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);
        String compact = diff.toCompactReport();

        System.out.println("Compact: " + compact);
        assertTrue(compact.contains("name:"));
        assertTrue(compact.contains("\"Before\" → \"After\""));
        assertTrue(compact.contains("count:"));
        assertFalse(compact.contains("description")); // unchanged, should not appear
        assertFalse(compact.contains("score")); // unchanged, should not appear
    }

    @Test
    void changesOnlyReportFormat() {
        AssertableEntity before = createEntity(1L, "Before", 10, null, 5);
        AssertableEntity after = createEntity(1L, "After", 10, "New Desc", null);

        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);
        String report = diff.toChangesOnly();

        System.out.println("Changes only:\n" + report);
        assertTrue(report.contains("name:"));
        assertTrue(report.contains("description:")); // SET
        assertTrue(report.contains("score:")); // CLEARED
        assertTrue(report.contains("→"));
    }

    @Test
    void assertionCodeGeneration() {
        AssertableEntity before = createEntity(1L, "Before", 10, "Desc", 5);
        AssertableEntity after = createEntity(1L, "After", 20, "Desc", 5);

        Diff<AssertableEntity> diff = AssertableEntityDiff.compare(before, after);
        String code = diff.toAssertionCode();

        System.out.println("Assertion code:\n" + code);
        assertTrue(code.contains("assertEntityWithSnapshot("));
        assertTrue(code.contains("value(\"name\""));
        assertTrue(code.contains("value(\"count\""));
    }

    // ==================== Batch Report Tests ====================

    @Test
    void batchReportWithMultipleEntities() {
        BatchDiffReport batch = new BatchDiffReport();

        // Add multiple entity diffs
        batch.add(AssertableEntityDiff.compare(
                createEntity(1L, "Before1", 10, "Desc", 5),
                createEntity(1L, "After1", 10, "Desc", 5)));
        batch.add(AssertableEntityDiff.compare(
                createEntity(2L, "Before2", 20, "Desc", 10),
                createEntity(2L, "After2", 30, "Desc", 10)));
        batch.add(AssertableEntityDiff.compare(
                createEntity(3L, "Same", 30, "Desc", 15),
                createEntity(3L, "Same", 30, "Desc", 15))); // No changes

        assertEquals(2, batch.changedEntityCount());
        assertTrue(batch.hasChanges());

        String report = batch.toCompactReport();
        System.out.println("Batch compact report:\n" + report);
        assertTrue(report.contains("2 entities changed"));
    }

    @Test
    void batchReportAssertionCode() {
        BatchDiffReport batch = new BatchDiffReport();

        batch.add(AssertableEntityDiff.compare(
                createEntity(1L, "Before", 10, "Desc", 5),
                createEntity(1L, "After", 20, "Desc", 5)));
        batch.add(AssertableEntityDiff.compare(
                createEntity(2L, "Old", 100, "Desc", 50),
                createEntity(2L, "New", 100, "Desc", 50)));

        String code = batch.toAssertionCode();
        System.out.println("Batch assertion code:\n" + code);
        assertTrue(code.contains("assertEntityWithSnapshot"));
        assertTrue(code.contains("// === 2 entities need assertions ==="));
    }

    @Test
    void batchReportOneLiner() {
        BatchDiffReport batch = new BatchDiffReport();

        batch.add(AssertableEntityDiff.compare(
                createEntity(1L, "A", 10, "D", 5),
                createEntity(1L, "B", 10, "D", 5)));
        batch.add(AssertableEntityDiff.compare(
                createEntity(2L, "X", 20, "D", 10),
                createEntity(2L, "Y", 30, "D", 10)));

        String oneLiner = batch.toOneLiner();
        System.out.println("One-liner: " + oneLiner);
        assertTrue(oneLiner.startsWith("2 changes:"));
    }

    @Test
    void batchReportNoChanges() {
        BatchDiffReport batch = new BatchDiffReport();

        batch.add(AssertableEntityDiff.compare(
                createEntity(1L, "Same", 10, "Desc", 5),
                createEntity(1L, "Same", 10, "Desc", 5)));

        assertFalse(batch.hasChanges());
        assertEquals(0, batch.changedEntityCount());
        assertTrue(batch.toCompactReport().contains("No changes"));
    }

    // Helper method
    private AssertableEntity createEntity(Long id, String name, int count, String desc, Integer score) {
        AssertableEntity entity = new AssertableEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setCount(count);
        entity.setDescription(Optional.ofNullable(desc));
        entity.setScore(Optional.ofNullable(score));
        return entity;
    }
}
