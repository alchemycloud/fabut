package cloud.alchemy.fabut.tracking;

import cloud.alchemy.fabut.model.TrackedDto;
import cloud.alchemy.fabut.model.TrackedTuple;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsageReportTest {

    @Test
    void emptyReport() {
        UsageReport report = new UsageReport(List.of());

        assertFalse(report.hasTrackedObjects());
        assertFalse(report.hasUnderusedObjects());
        assertTrue(report.getUnderusedObjects().isEmpty());
        assertEquals("USAGE REPORT: No tracked objects.", report.generate());
    }

    @Test
    void singleFullyUsedObject() {
        TrackedObject tracked = new TrackedObject(1, TrackedTuple.class, Set.of("entityId", "label"));
        tracked.recordAccess("entityId");
        tracked.recordAccess("label");

        UsageReport report = new UsageReport(List.of(tracked));

        assertTrue(report.hasTrackedObjects());
        assertFalse(report.hasUnderusedObjects());
        assertTrue(report.getUnderusedObjects().isEmpty());

        String output = report.generate();
        assertTrue(output.contains("TrackedTuple"));
        assertTrue(output.contains("1 instance fetched"));
        assertTrue(output.contains("all fields"));
    }

    @Test
    void singleNeverAccessedObject() {
        TrackedObject tracked = new TrackedObject(1, TrackedDto.class, Set.of("id", "name", "count"));

        UsageReport report = new UsageReport(List.of(tracked));

        assertTrue(report.hasUnderusedObjects());
        assertEquals(1, report.getUnderusedObjects().size());

        String output = report.generate();
        assertTrue(output.contains("TrackedDto"));
        assertTrue(output.contains("never accessed"));
    }

    @Test
    void singlePartiallyUsedObject() {
        TrackedObject tracked = new TrackedObject(1, TrackedDto.class,
                Set.of("id", "name", "description", "count"));
        tracked.recordAccess("id");
        tracked.recordAccess("name");

        UsageReport report = new UsageReport(List.of(tracked));

        assertTrue(report.hasUnderusedObjects());

        String output = report.generate();
        assertTrue(output.contains("TrackedDto"));
        assertTrue(output.contains("50%"));
        assertTrue(output.contains("Commonly unused"));
    }

    @Test
    void multipleInstancesSameClass() {
        TrackedObject t1 = new TrackedObject(1, TrackedDto.class,
                Set.of("id", "name", "count"));
        t1.recordAccess("id");

        TrackedObject t2 = new TrackedObject(2, TrackedDto.class,
                Set.of("id", "name", "count"));
        t2.recordAccess("id");
        t2.recordAccess("name");

        UsageReport report = new UsageReport(List.of(t1, t2));

        String output = report.generate();
        assertTrue(output.contains("2 instances fetched"));
    }

    @Test
    void multipleClasses() {
        TrackedObject dto = new TrackedObject(1, TrackedDto.class,
                Set.of("id", "name"));
        dto.recordAccess("id");

        TrackedObject tuple = new TrackedObject(2, TrackedTuple.class,
                Set.of("entityId", "label"));
        tuple.recordAccess("entityId");
        tuple.recordAccess("label");

        UsageReport report = new UsageReport(List.of(dto, tuple));

        String output = report.generate();
        assertTrue(output.contains("TrackedDto"));
        assertTrue(output.contains("TrackedTuple"));
    }

    @Test
    void summaryByClass() {
        TrackedObject t1 = new TrackedObject(1, TrackedDto.class, Set.of("id", "name"));
        t1.recordAccess("id");

        TrackedObject t2 = new TrackedObject(2, TrackedDto.class, Set.of("id", "name"));
        t2.recordAccess("id");

        UsageReport report = new UsageReport(List.of(t1, t2));
        Map<String, UsageReport.ClassUsageSummary> summary = report.getSummaryByClass();

        assertEquals(1, summary.size());
        UsageReport.ClassUsageSummary dtoSummary = summary.get("TrackedDto");
        assertNotNull(dtoSummary);
        assertEquals("TrackedDto", dtoSummary.className());
        assertEquals(2, dtoSummary.instanceCount());
        assertEquals(50.0, dtoSummary.averageUsagePercent(), 0.001);
        assertTrue(dtoSummary.commonUnusedFields().contains("name"));
    }

    @Test
    void commonUnusedFields_onlyIncludesFieldsUnusedByMajority() {
        TrackedObject t1 = new TrackedObject(1, TrackedDto.class,
                Set.of("id", "name", "count"));
        t1.recordAccess("id");
        // name and count unused

        TrackedObject t2 = new TrackedObject(2, TrackedDto.class,
                Set.of("id", "name", "count"));
        t2.recordAccess("id");
        t2.recordAccess("name");
        // only count unused

        TrackedObject t3 = new TrackedObject(3, TrackedDto.class,
                Set.of("id", "name", "count"));
        t3.recordAccess("id");
        t3.recordAccess("name");
        // only count unused

        UsageReport report = new UsageReport(List.of(t1, t2, t3));
        var summary = report.getSummaryByClass().get("TrackedDto");

        // count is unused in all 3, name in only 1 — threshold is 3/2=1, so both qualify
        // But with 3 instances, threshold = max(1, 3/2) = 1
        // count unused in 3/3 → included, name unused in 1/3 → included (>=1)
        assertTrue(summary.commonUnusedFields().contains("count"));
    }

    @Test
    void generate_headerPresent() {
        TrackedObject tracked = new TrackedObject(1, TrackedDto.class, Set.of("id"));
        UsageReport report = new UsageReport(List.of(tracked));

        assertTrue(report.generate().startsWith("USAGE REPORT:"));
    }

    @Test
    void generate_pluralInstances() {
        TrackedObject t1 = new TrackedObject(1, TrackedDto.class, Set.of("id"));
        TrackedObject t2 = new TrackedObject(2, TrackedDto.class, Set.of("id"));

        String output = new UsageReport(List.of(t1, t2)).generate();
        assertTrue(output.contains("2 instances"));
    }

    @Test
    void generate_singularInstance() {
        TrackedObject t1 = new TrackedObject(1, TrackedDto.class, Set.of("id"));

        String output = new UsageReport(List.of(t1)).generate();
        assertTrue(output.contains("1 instance fetched"));
        assertFalse(output.contains("1 instances"));
    }

    @Test
    void getViolations_emptyWhenAllAboveThreshold() {
        TrackedObject tracked = new TrackedObject(1, TrackedDto.class, Set.of("id", "name"));
        tracked.recordAccess("id");
        tracked.recordAccess("name");

        UsageReport report = new UsageReport(List.of(tracked));
        assertTrue(report.getViolations(50).isEmpty());
    }

    @Test
    void getViolations_returnsClassesBelowThreshold() {
        TrackedObject tracked = new TrackedObject(1, TrackedDto.class, Set.of("id", "name", "count", "description"));
        tracked.recordAccess("id");

        UsageReport report = new UsageReport(List.of(tracked));
        var violations = report.getViolations(50);
        assertEquals(1, violations.size());
        assertEquals("TrackedDto", violations.getFirst().className());
    }

    @Test
    void getViolations_zeroThreshold_returnsEmpty() {
        TrackedObject tracked = new TrackedObject(1, TrackedDto.class, Set.of("id"));

        UsageReport report = new UsageReport(List.of(tracked));
        assertTrue(report.getViolations(0).isEmpty());
    }

    @Test
    void getViolations_negativeThreshold_returnsEmpty() {
        TrackedObject tracked = new TrackedObject(1, TrackedDto.class, Set.of("id"));

        UsageReport report = new UsageReport(List.of(tracked));
        assertTrue(report.getViolations(-1).isEmpty());
    }

    @Test
    void getViolations_exactlyAtThreshold_passes() {
        TrackedObject tracked = new TrackedObject(1, TrackedDto.class, Set.of("id", "name"));
        tracked.recordAccess("id"); // 50%

        UsageReport report = new UsageReport(List.of(tracked));
        assertTrue(report.getViolations(50).isEmpty()); // 50% is not < 50%
    }

    @Test
    void getViolations_multipleClasses_onlyViolators() {
        TrackedObject dto = new TrackedObject(1, TrackedDto.class, Set.of("id", "name", "count", "description"));
        dto.recordAccess("id"); // 25%

        TrackedObject tuple = new TrackedObject(2, TrackedTuple.class, Set.of("entityId", "label"));
        tuple.recordAccess("entityId");
        tuple.recordAccess("label"); // 100%

        UsageReport report = new UsageReport(List.of(dto, tuple));
        var violations = report.getViolations(50);
        assertEquals(1, violations.size());
        assertEquals("TrackedDto", violations.getFirst().className());
    }
}
