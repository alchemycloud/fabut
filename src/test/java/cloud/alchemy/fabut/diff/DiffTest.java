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
