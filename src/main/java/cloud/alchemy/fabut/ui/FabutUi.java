package cloud.alchemy.fabut.ui;

import cloud.alchemy.fabut.Fabut;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.ReducedMotion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Base class for pixel-perfect UI tests on top of Playwright. Inherits all of {@link Fabut}, so a single test can
 * assert both the screen and the database snapshot.
 *
 * <pre>{@code
 * class CheckoutUiTest extends FabutUi {
 *
 *     public CheckoutUiTest() {
 *         screenSettings.ignore("[data-testid=clock]");   // ignored on every screen
 *     }
 *
 *     @Override
 *     protected String baseUrl() { return "http://localhost:8080"; }
 *
 *     @Test
 *     void checkout() {
 *         page.navigate("/cart");
 *         screen("cart").verify();                         // step 1
 *
 *         page.click("text=Checkout");
 *         screen("payment")
 *             .ignore(".promo-banner")
 *             .tolerate(".map", 0.05)
 *             .verify();                                   // step 2
 *
 *         screen(page.locator(".order-summary"), "summary").maxDiffPixels(4).verify();
 *     }
 * }
 * }</pre>
 *
 * <p>Baselines live in {@code src/test/fabut-ui/<TestClass>/<testMethod>/<name>-<browser>-<os>.png}. Missing
 * baselines are recorded on local runs; on CI ({@code CI} env set) they fail. Mismatches are collected and reported
 * together after the test (set {@link #failFast} to fail immediately). Accept intended changes with
 * {@code -Dfabut.ui.update=all}.</p>
 */
public abstract class FabutUi extends Fabut {

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Browser>> BROWSERS = ThreadLocal.withInitial(HashMap::new);
    private static final ConcurrentLinkedQueue<Playwright> ALL = new ConcurrentLinkedQueue<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Playwright p : ALL) {
                try {
                    p.close();
                } catch (RuntimeException ignored) {
                    // JVM is shutting down
                }
            }
        }, "fabut-ui-playwright-shutdown"));
    }

    /** Snapshot configuration; adjust in the test constructor. */
    protected final ScreenSettings screenSettings = ScreenSettings.defaults();

    /** {@code false} (default): collect all screen mismatches and fail after the test. {@code true}: fail on the first. */
    protected boolean failFast = false;

    /** Page for the current test (new isolated browser context per test). */
    protected Page page;

    /** Browser context for the current test. */
    protected BrowserContext context;

    private ScreenSnapshotter snapshotter;
    private String group;
    private int autoIndex;
    private final Set<String> usedNames = new HashSet<>();
    private final List<ScreenResult> results = new ArrayList<>();

    // ---- overridable configuration ----

    /** Browser engine, default Chromium. */
    protected BrowserType browserType(Playwright playwright) {
        return playwright.chromium();
    }

    /** Launch options, default headless ({@code -Dfabut.ui.headed=true} to watch). */
    protected BrowserType.LaunchOptions launchOptions() {
        return new BrowserType.LaunchOptions().setHeadless(!Boolean.getBoolean("fabut.ui.headed"));
    }

    /**
     * Context options tuned for deterministic rendering: fixed viewport, scale factor, locale, timezone,
     * light color scheme and reduced motion.
     */
    protected Browser.NewContextOptions contextOptions() {
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setViewportSize(1280, 720)
                .setDeviceScaleFactor(1)
                .setLocale("en-US")
                .setTimezoneId("UTC")
                .setColorScheme(ColorScheme.LIGHT)
                .setReducedMotion(ReducedMotion.REDUCE);
        String baseUrl = baseUrl();
        if (baseUrl != null) options.setBaseURL(baseUrl);
        return options;
    }

    /** Base URL for relative {@code page.navigate(..)} calls; {@code null} = none. */
    protected String baseUrl() {
        return null;
    }

    // ---- lifecycle ----

    @BeforeEach
    public void beforeUi(TestInfo testInfo) {
        String className = testInfo.getTestClass().map(Class::getSimpleName).orElse(getClass().getSimpleName());
        String methodName = testInfo.getTestMethod().map(m -> m.getName()).orElse("test");
        group = className + "/" + methodName;
        autoIndex = 0;
        usedNames.clear();
        results.clear();

        Browser browser = browser();
        screenSettings.browserName(browser.browserType().name());
        snapshotter = new ScreenSnapshotter(screenSettings);
        context = browser.newContext(contextOptions());
        page = context.newPage();
    }

    @AfterEach
    public void afterUi() {
        try {
            // in fail-fast mode failures were already thrown from verify()
            List<ScreenResult> failed = failFast ? List.of() : results.stream().filter(r -> !r.passed()).toList();
            for (ScreenResult r : results) {
                if (r.status() == ScreenResult.Status.NEW || r.status() == ScreenResult.Status.UPDATED) {
                    System.out.println(r.message());
                }
            }
            if (!failed.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append(failed.size()).append(failed.size() == 1 ? " screen" : " screens")
                        .append(" did not match (").append(results.size()).append(" checked)\n");
                for (ScreenResult r : failed) {
                    sb.append('\n').append(r.message()).append('\n');
                }
                throw new AssertionFailedError(sb.toString().stripTrailing());
            }
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (RuntimeException ignored) {
                    // browser may already be gone
                }
            }
            context = null;
            page = null;
        }
    }

    // ---- assertions ----

    /** Assert the current viewport (or full page) against the baseline {@code name}. Call {@code .verify()}. */
    protected ScreenAssert screen(String name) {
        return screen(null, name);
    }

    /** Assert the next auto-numbered step ({@code step-01}, {@code step-02}, ...). Call {@code .verify()}. */
    protected ScreenAssert screen() {
        return screen(null, String.format("step-%02d", ++autoIndex));
    }

    /** Assert a single element against the baseline {@code name}. Call {@code .verify()}. */
    protected ScreenAssert screen(Locator element, String name) {
        if (page == null) {
            throw new IllegalStateException("No page available. screen() can only be used inside a test method.");
        }
        String key = ScreenSnapshotter.sanitize(name);
        if (!usedNames.add(key)) {
            throw new IllegalArgumentException("Screen name '" + name + "' is used twice in " + group
                    + ". Every step needs a unique name.");
        }
        ScreenAssert[] holder = new ScreenAssert[1];
        ScreenAssert screenAssert = snapshotter.screen(page, element, group, name, result -> {
            markVerified(holder[0]);
            results.add(result);
            if (failFast && !result.passed()) {
                throw new AssertionFailedError(result.message());
            }
        });
        holder[0] = screenAssert;
        registerPendingVerification(screenAssert);
        return screenAssert;
    }

    /** Results of all screen assertions verified so far in this test. */
    protected List<ScreenResult> screenResults() {
        return List.copyOf(results);
    }

    private Browser browser() {
        Playwright playwright = PLAYWRIGHT.get();
        if (playwright == null) {
            playwright = Playwright.create();
            PLAYWRIGHT.set(playwright);
            ALL.add(playwright);
        }
        BrowserType type = browserType(playwright);
        BrowserType.LaunchOptions launch = launchOptions();
        String key = type.name() + ":" + launch.headless + ":" + launch.channel;
        Map<String, Browser> browsers = BROWSERS.get();
        Browser browser = browsers.get(key);
        if (browser == null || !browser.isConnected()) {
            browser = type.launch(launch);
            browsers.put(key, browser);
        }
        return browser;
    }
}
