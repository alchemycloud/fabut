package cloud.alchemy.fabut;

public class AbstractFabutTest extends Fabut {

    protected void assertFabutReportSuccess(FabutReport report) {
        assertTrue(report.getMessage(), report.isSuccess());
    }

    protected void assertFabutReportFailure(FabutReport report, String message) {
        assertFalse(report.getMessage(), report.isSuccess());
        assertEquals(message, report.getMessage());
    }
}
