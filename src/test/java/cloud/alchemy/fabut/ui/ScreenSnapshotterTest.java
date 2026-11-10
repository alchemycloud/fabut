package cloud.alchemy.fabut.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.AssertionFailedError;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ScreenSnapshotterTest {

    private static Playwright playwright;
    private static Browser browser;

    private BrowserContext context;
    private Page page;

    @TempDir
    Path tmp;

    @BeforeAll
    static void launch() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void shutdown() {
        playwright.close();
    }

    @BeforeEach
    void newPage() {
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(800, 600).setDeviceScaleFactor(1));
        page = context.newPage();
    }

    @AfterEach
    void closePage() {
        context.close();
    }

    private ScreenSnapshotter snapshotter(UpdateMode mode) {
        return new ScreenSnapshotter(ScreenSettings.defaults()
                .baselineDir(tmp.resolve("baselines"))
                .outputDir(tmp.resolve("out"))
                .platformSuffix("test")
                .updateMode(mode));
    }

    private void render(String buttonText, String clock, String chartColor) {
        page.setContent("""
                <html><body style="margin:0;font-family:sans-serif;background:#fff">
                  <h1 id="title" style="margin:20px">Login</h1>
                  <div id="clock" style="position:absolute;left:600px;top:20px">%s</div>
                  <button id="submit" style="position:absolute;left:100px;top:200px;width:120px;height:40px">%s</button>
                  <div class="chart" style="position:absolute;left:400px;top:100px;width:200px;height:100px;background:#eee">
                    <div style="position:absolute;left:10px;top:10px;width:20px;height:20px;background:%s"></div>
                  </div>
                </body></html>
                """.formatted(clock, buttonText, chartColor));
    }

    @Test
    void recordsMissingBaselineThenMatches() {
        ScreenSnapshotter screens = snapshotter(UpdateMode.MISSING);
        render("Log in", "12:00", "blue");

        ScreenResult first = screens.screen(page, "Login", "start").verify();
        assertEquals(ScreenResult.Status.NEW, first.status());
        assertTrue(Files.exists(tmp.resolve("baselines/Login/start-test.png")));
        assertTrue(Files.exists(tmp.resolve("baselines/Login/start.aria.yml")));
        assertTrue(first.stable());

        ScreenResult second = screens.screen(page, "Login", "start").verify();
        assertEquals(ScreenResult.Status.MATCH, second.status());
        assertEquals(0, second.diff().diffPixels());
    }

    @Test
    void mismatchExplainsWhatChanged() {
        ScreenSnapshotter screens = snapshotter(UpdateMode.MISSING);
        render("Log in", "12:00", "blue");
        screens.screen(page, "Login", "start").verify();

        render("Sign in", "12:00", "blue");
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> screens.screen(page, "Login", "start").verify());

        String message = error.getMessage();
        assertTrue(message.startsWith("SCREEN MISMATCH: Login/start"), message);
        assertTrue(message.contains("button#submit \"Sign in\""), message);
        assertTrue(message.contains("- - button \"Log in\""), message);
        assertTrue(message.contains("+ - button \"Sign in\""), message);
        assertTrue(message.contains("-Dfabut.ui.update=all"), message);
        assertTrue(Files.exists(tmp.resolve("out/Login/start.actual.png")));
        assertTrue(Files.exists(tmp.resolve("out/Login/start.diff.png")));
        assertTrue(Files.exists(tmp.resolve("out/Login/start.compare.png")));
        assertTrue(Files.exists(tmp.resolve("out/Login/start.json")));
    }

    @Test
    void ignoredSelectorIsMaskedAndExcluded() {
        ScreenSnapshotter screens = snapshotter(UpdateMode.MISSING);
        render("Log in", "12:00:01", "blue");
        screens.screen(page, "Login", "clock").ignore("#clock").verify();

        render("Log in", "23:59:59 PM", "blue");
        ScreenResult result = screens.screen(page, "Login", "clock").ignore("#clock").verify();
        assertEquals(ScreenResult.Status.MATCH, result.status());
        assertTrue(result.diff().ignoredPixels() > 0);
    }

    @Test
    void globalIgnoredSelectorsApplyToEveryScreen() {
        ScreenSnapshotter screens = new ScreenSnapshotter(ScreenSettings.defaults()
                .baselineDir(tmp.resolve("baselines")).outputDir(tmp.resolve("out"))
                .platformSuffix("").updateMode(UpdateMode.MISSING).ignore("#clock"));
        render("Log in", "1", "blue");
        screens.screen(page, "Login", "global").verify();
        assertTrue(Files.exists(tmp.resolve("baselines/Login/global.png")));

        render("Log in", "2", "blue");
        assertEquals(ScreenResult.Status.MATCH, screens.screen(page, "Login", "global").verify().status());
    }

    @Test
    void toleratedRegionAcceptsDeviationWithinBudget() {
        ScreenSnapshotter screens = snapshotter(UpdateMode.MISSING);
        render("Log in", "12:00", "blue");
        screens.screen(page, "Login", "chart").verify();

        render("Log in", "12:00", "red"); // 400 px of a ~20000 px chart = ~2%
        ScreenResult ok = screens.screen(page, "Login", "chart").tolerate(".chart", 0.05).verify();
        assertEquals(ScreenResult.Status.MATCH, ok.status());
        assertEquals(400, ok.diff().regionResults().getFirst().diffPixels());

        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> screens.screen(page, "Login", "chart").tolerate(".chart", 0.01).verify());
        assertTrue(error.getMessage().contains("tolerated .chart"), error.getMessage());
    }

    @Test
    void maxDiffPixelsAcceptsSmallChange() {
        ScreenSnapshotter screens = snapshotter(UpdateMode.MISSING);
        render("Log in", "12:00", "blue");
        screens.screen(page, "Login", "budget").verify();

        render("Log in", "12:00", "red");
        assertEquals(ScreenResult.Status.MATCH,
                screens.screen(page, "Login", "budget").maxDiffPixels(400).verify().status());
        assertThrows(AssertionFailedError.class,
                () -> screens.screen(page, "Login", "budget").maxDiffPixels(399).verify());
    }

    @Test
    void updateAllOverwritesBaseline() {
        render("Log in", "12:00", "blue");
        snapshotter(UpdateMode.MISSING).screen(page, "Login", "update").verify();

        render("Sign in", "12:00", "blue");
        ScreenResult updated = snapshotter(UpdateMode.ALL).screen(page, "Login", "update").verify();
        assertEquals(ScreenResult.Status.UPDATED, updated.status());
        assertTrue(updated.passed());

        assertEquals(ScreenResult.Status.MATCH,
                snapshotter(UpdateMode.NONE).screen(page, "Login", "update").verify().status());
    }

    @Test
    void missingBaselineFailsWhenRecordingDisabled() {
        render("Log in", "12:00", "blue");
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> snapshotter(UpdateMode.NONE).screen(page, "Login", "none").verify());
        assertTrue(error.getMessage().startsWith("SCREEN MISSING BASELINE"), error.getMessage());
        assertFalse(Files.exists(tmp.resolve("baselines/Login/none-test.png")));
        assertTrue(Files.exists(tmp.resolve("out/Login/none.actual.png")));
    }

    @Test
    void elementScreenshotOnlyComparesElement() {
        ScreenSnapshotter screens = snapshotter(UpdateMode.MISSING);
        render("Log in", "12:00", "blue");
        screens.screen(page.locator("#submit"), "Login", "button").verify();

        render("Log in", "13:00", "red"); // changes outside the button
        assertEquals(ScreenResult.Status.MATCH,
                screens.screen(page.locator("#submit"), "Login", "button").verify().status());

        render("Sign in", "13:00", "red");
        AssertionFailedError error = assertThrows(AssertionFailedError.class,
                () -> screens.screen(page.locator("#submit"), "Login", "button").verify());
        assertTrue(error.getMessage().contains("button#submit"), error.getMessage());
    }

    @Test
    void regionsMapCorrectlyWhenScrolled() {
        ScreenSnapshotter screens = snapshotter(UpdateMode.MISSING);
        String html = """
                <html><body style="margin:0;height:3000px">
                  <div class="chart" style="position:absolute;left:100px;top:1200px;width:200px;height:100px;background:%s"></div>
                </body></html>
                """;
        page.setContent(html.formatted("blue"));
        page.evaluate("() => window.scrollTo(0, 1000)");
        screens.screen(page, "Scroll", "viewport").verify();

        page.setContent(html.formatted("red"));
        page.evaluate("() => window.scrollTo(0, 1000)");
        ScreenResult viewport = screens.screen(page, "Scroll", "viewport").tolerate(".chart", 1.0).verify();
        assertEquals(ScreenResult.Status.MATCH, viewport.status());
        assertTrue(viewport.diff().regionResults().getFirst().diffPixels() >= 20000);

        page.setContent(html.formatted("blue"));
        screens.screen(page, "Scroll", "full").fullPage().verify();
        page.setContent(html.formatted("red"));
        ScreenResult full = screens.screen(page, "Scroll", "full").fullPage().tolerate(".chart", 1.0).verify();
        assertEquals(ScreenResult.Status.MATCH, full.status());
        assertEquals(3000, full.diff().actualHeight());
    }

    @Test
    void invalidNameRejected() {
        assertThrows(IllegalArgumentException.class, () -> ScreenSnapshotter.sanitize("///"));
        assertEquals("step-1-login", ScreenSnapshotter.sanitize("step 1: login"));
    }
}
