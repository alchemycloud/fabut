package cloud.alchemy.fabut.ui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Global configuration for screen snapshots. Every value can be overridden per assertion on {@link ScreenAssert}.
 *
 * <p>System properties: {@code fabut.ui.update}, {@code fabut.ui.baselineDir}, {@code fabut.ui.outputDir}.</p>
 */
public final class ScreenSettings {

    private Path baselineDir = Path.of(System.getProperty("fabut.ui.baselineDir", "src/test/fabut-ui"));
    private Path outputDir = Path.of(System.getProperty("fabut.ui.outputDir", "target/fabut-ui"));
    private UpdateMode updateMode = UpdateMode.fromEnvironment();
    private String platformSuffix; // null = <browser>-<os>
    private String browserName = "chromium";
    private double colorThreshold = CompareOptions.DEFAULT_COLOR_THRESHOLD;
    private int maxDiffPixels = -1;
    private double maxDiffRatio = -1;
    private boolean ignoreAntialiasing = true;
    private int stabilityAttempts = 5;
    private boolean captureAria = true;
    private boolean fullPage = false;
    private String style;
    private final List<String> ignoredSelectors = new ArrayList<>();

    public static ScreenSettings defaults() {
        return new ScreenSettings();
    }

    /** Directory with committed baseline images. Default {@code src/test/fabut-ui}. */
    public ScreenSettings baselineDir(Path dir) {
        this.baselineDir = dir;
        return this;
    }

    /** Directory for actual/diff/compare images and JSON reports. Default {@code target/fabut-ui}. */
    public ScreenSettings outputDir(Path dir) {
        this.outputDir = dir;
        return this;
    }

    /** Browser name used in the default platform suffix (set automatically by {@link FabutUi}). */
    public ScreenSettings browserName(String browserName) {
        this.browserName = browserName;
        return this;
    }

    public ScreenSettings updateMode(UpdateMode mode) {
        this.updateMode = mode;
        return this;
    }

    /**
     * Suffix appended to baseline file names, default {@code <browser>-<os>} (e.g. {@code chromium-linux}),
     * because font rendering differs between platforms. Set to {@code ""} to share baselines.
     */
    public ScreenSettings platformSuffix(String suffix) {
        this.platformSuffix = suffix;
        return this;
    }

    /** Default per-pixel color sensitivity 0..1 (0 = exact). Default 0.1. */
    public ScreenSettings colorThreshold(double threshold) {
        this.colorThreshold = threshold;
        return this;
    }

    /** Default number of differing pixels allowed. Default: not set (0 allowed). */
    public ScreenSettings maxDiffPixels(int pixels) {
        this.maxDiffPixels = pixels;
        return this;
    }

    /** Default fraction (0..1) of differing pixels allowed. Default: not set (0 allowed). */
    public ScreenSettings maxDiffRatio(double ratio) {
        this.maxDiffRatio = ratio;
        return this;
    }

    /** Whether anti-aliasing pixels are ignored. Default {@code true}. */
    public ScreenSettings ignoreAntialiasing(boolean ignore) {
        this.ignoreAntialiasing = ignore;
        return this;
    }

    /** Max screenshots taken until two consecutive ones are identical. 1 disables the stability check. */
    public ScreenSettings stabilityAttempts(int attempts) {
        this.stabilityAttempts = Math.max(1, attempts);
        return this;
    }

    /** Store an ARIA (accessibility tree) snapshot next to each baseline and show its diff on failure. */
    public ScreenSettings captureAria(boolean capture) {
        this.captureAria = capture;
        return this;
    }

    /** Capture the full scrollable page by default instead of the viewport. */
    public ScreenSettings fullPage(boolean fullPage) {
        this.fullPage = fullPage;
        return this;
    }

    /** CSS injected while taking screenshots (e.g. hide scrollbars, freeze dynamic content). */
    public ScreenSettings style(String css) {
        this.style = css;
        return this;
    }

    /** Selectors masked and ignored in every screenshot (clocks, avatars, ads ...). */
    public ScreenSettings ignore(String... selectors) {
        this.ignoredSelectors.addAll(List.of(selectors));
        return this;
    }

    public Path baselineDir() { return baselineDir; }
    public Path outputDir() { return outputDir; }
    public UpdateMode updateMode() { return updateMode; }
    public String platformSuffix() { return platformSuffix != null ? platformSuffix : browserName + "-" + osName(); }
    public double colorThreshold() { return colorThreshold; }
    public int maxDiffPixels() { return maxDiffPixels; }
    public double maxDiffRatio() { return maxDiffRatio; }
    public boolean ignoreAntialiasing() { return ignoreAntialiasing; }
    public int stabilityAttempts() { return stabilityAttempts; }
    public boolean captureAria() { return captureAria; }
    public boolean fullPage() { return fullPage; }
    public String style() { return style; }
    public List<String> ignoredSelectors() { return List.copyOf(ignoredSelectors); }

    public static String osName() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("mac") || os.contains("darwin")) return "darwin";
        if (os.contains("win")) return "win32";
        return "linux";
    }
}
