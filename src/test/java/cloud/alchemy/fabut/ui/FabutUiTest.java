package cloud.alchemy.fabut.ui;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.Events;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class FabutUiTest extends FabutUi {

    static final Path BASELINES = Path.of("target/fabut-ui-selftest/baselines");
    static final Path OUTPUT = Path.of("target/fabut-ui-selftest/out");

    public FabutUiTest() {
        trackUsage = false;
        screenSettings.baselineDir(BASELINES).outputDir(OUTPUT).updateMode(UpdateMode.MISSING)
                .ignore("#clock"); // dynamic on every screen
    }

    @BeforeAll
    static void clean() throws IOException {
        deleteRecursively(BASELINES.getParent());
    }

    void render(String text) {
        page.setContent("<html><body style='margin:0;font:16px sans-serif'>"
                + "<h1 id='title' style='margin:20px'>" + text + "</h1>"
                + "<span id='clock' style='position:absolute;left:500px;top:20px'>" + System.nanoTime() + "</span>"
                + "</body></html>");
    }

    @Test
    void providesIsolatedPageWithDeterministicDefaults() {
        assertNotNull(page);
        assertNotNull(context);
        assertEquals(1280, page.viewportSize().width);
        assertEquals("UTC", page.evaluate("() => Intl.DateTimeFormat().resolvedOptions().timeZone"));
        assertEquals("en-US", page.evaluate("() => navigator.language"));
    }

    @RepeatedTest(2)
    void stepsAreRecordedThenCompared(RepetitionInfo repetition) {
        render("Welcome");

        screen().verify();
        screen("titled").verify();
        screen(page.locator("#title"), "title").verify();

        ScreenResult.Status expected = repetition.getCurrentRepetition() == 1
                ? ScreenResult.Status.NEW : ScreenResult.Status.MATCH;
        List<ScreenResult> results = screenResults();
        assertEquals(3, results.size());
        results.forEach(r -> assertEquals(expected, r.status(), r.message()));
        assertEquals("FabutUiTest/stepsAreRecordedThenCompared", results.getFirst().group());
        assertEquals("step-01", results.getFirst().name());
        assertTrue(Files.exists(BASELINES.resolve("FabutUiTest/stepsAreRecordedThenCompared/title-"
                + screenSettings.platformSuffix() + ".png")));
    }

    @Test
    void duplicateScreenNameRejected() {
        render("x");
        screen("same").ignore("#clock").verify();
        assertThrows(IllegalArgumentException.class, () -> screen("same"));
    }

    @RepeatedTest(2)
    void failFastThrowsImmediately(RepetitionInfo repetition) {
        failFast = true;
        render(repetition.getCurrentRepetition() == 1 ? "Before" : "After");
        if (repetition.getCurrentRepetition() == 1) {
            screen("ff").ignore("#clock").verify();
        } else {
            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> screen("ff").ignore("#clock").verify());
            assertTrue(error.getMessage().contains("SCREEN MISMATCH"), error.getMessage());
            assertTrue(error.getMessage().contains("h1#title \"After\""), error.getMessage());
        }
    }

    // ---- lifecycle scenarios executed through the JUnit engine ----

    @Test
    void softMismatchesAreCollectedAndReportedAfterTest() {
        SoftScenario.text = "First";
        Events first = run(SoftScenario.class);
        first.assertStatistics(s -> s.succeeded(1).failed(0));

        SoftScenario.text = "Second";
        Events second = run(SoftScenario.class);
        second.assertStatistics(s -> s.succeeded(0).failed(1));
        Throwable failure = second.failed().stream().findFirst().map(this::throwable).orElseThrow();
        String message = failure.getMessage();
        assertTrue(message.startsWith("2 screens did not match (2 checked)"), message);
        assertTrue(message.contains("SCREEN MISMATCH: SoftScenario/run/one"), message);
        assertTrue(message.contains("SCREEN MISMATCH: SoftScenario/run/two"), message);
        assertTrue(SoftScenario.reachedEnd, "soft mismatches must not stop the test");
    }

    @Test
    void forgottenVerifyIsReported() {
        Events events = run(UnverifiedScenario.class);
        events.assertStatistics(s -> s.failed(1));
        String message = events.failed().stream().findFirst().map(this::throwable).orElseThrow().getMessage();
        assertTrue(message.contains("UNVERIFIED BUILDER"), message);
    }

    private Events run(Class<?> scenario) {
        return EngineTestKit.engine("junit-jupiter").selectors(selectClass(scenario)).execute().testEvents();
    }

    private Throwable throwable(Event event) {
        return event.getPayload(org.junit.platform.engine.TestExecutionResult.class)
                .flatMap(org.junit.platform.engine.TestExecutionResult::getThrowable)
                .orElseThrow();
    }

    static class SoftScenario extends FabutUi {
        static String text;
        static boolean reachedEnd;

        SoftScenario() {
            trackUsage = false;
            screenSettings.baselineDir(BASELINES).outputDir(OUTPUT).updateMode(UpdateMode.MISSING);
        }

        @Test
        void run() {
            reachedEnd = false;
            page.setContent("<h1 style='font:20px sans-serif'>" + text + "</h1>");
            screen("one").verify();
            screen("two").verify();
            reachedEnd = true;
        }
    }

    static class UnverifiedScenario extends FabutUi {
        UnverifiedScenario() {
            trackUsage = false;
            screenSettings.baselineDir(BASELINES).outputDir(OUTPUT);
        }

        @Test
        void run() {
            screen("never-verified").ignore("h1");
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
    }
}
