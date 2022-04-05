package cloud.alchemy.fabut;

public class AbstractFabutTest extends Fabut {

    protected void assertFabutReportSuccess(FabutReport report) {
        assertTrue(report.isSuccess(), report.getMessage());
    }

    protected void assertFabutReportFailure(FabutReport report, String message) {
        assertFalse(report.isSuccess(), report.getMessage());
        assertEquals(message, report.getMessage());
    }
}
