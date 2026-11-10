package cloud.alchemy.fabut.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fluent screen assertion. Configure what to ignore and how much deviation is accepted, then call {@link #verify()}.
 *
 * <pre>{@code
 * screen("checkout")
 *     .ignore("[data-testid=clock]")          // masked + excluded from comparison
 *     .tolerate(".chart", 0.02)               // up to 2% of the chart may differ
 *     .maxDiffPixels(10)                      // everything else: at most 10 px
 *     .verify();
 * }</pre>
 */
public final class ScreenAssert {

    /** Ignore or tolerance target given as selector, locator or fixed area. */
    record Target(Locator locator, String label, int x, int y, int width, int height, boolean ignore, double maxDiffRatio) {
        boolean isArea() {
            return locator == null;
        }
    }

    private final ScreenSnapshotter snapshotter;
    private final Page page;
    private final Locator element;
    private final String group;
    private final String name;
    private final Consumer<ScreenResult> onResult;

    private final List<Target> targets = new ArrayList<>();
    private Boolean fullPage;
    private Double colorThreshold;
    private Integer maxDiffPixels;
    private Double maxDiffRatio;
    private Boolean ignoreAntialiasing;
    private String style;
    private boolean verified;

    ScreenAssert(ScreenSnapshotter snapshotter, Page page, Locator element, String group, String name,
                 Consumer<ScreenResult> onResult) {
        this.snapshotter = snapshotter;
        this.page = page;
        this.element = element;
        this.group = group;
        this.name = name;
        this.onResult = onResult;
        for (String selector : snapshotter.settings().ignoredSelectors()) {
            ignore(selector);
        }
    }

    /** Mask all elements matching the selector and exclude them from comparison (clocks, avatars, random ids). */
    public ScreenAssert ignore(String selector) {
        return ignore(page.locator(selector), selector);
    }

    /** Mask all elements matching the locator and exclude them from comparison. */
    public ScreenAssert ignore(Locator locator) {
        return ignore(locator, locator.toString());
    }

    private ScreenAssert ignore(Locator locator, String label) {
        targets.add(new Target(locator, label, 0, 0, 0, 0, true, 1.0));
        return this;
    }

    /** Exclude a fixed area (screenshot pixel coordinates) from comparison. */
    public ScreenAssert ignoreArea(int x, int y, int width, int height) {
        targets.add(new Target(null, "area", x, y, width, height, true, 1.0));
        return this;
    }

    /**
     * Allow up to {@code maxDiffRatio} (0..1) of the pixels of matching elements to differ.
     * Differences there don't count against the screen-wide budget.
     */
    public ScreenAssert tolerate(String selector, double maxDiffRatio) {
        return tolerate(page.locator(selector), selector, maxDiffRatio);
    }

    /** Same as {@link #tolerate(String, double)} for a locator. */
    public ScreenAssert tolerate(Locator locator, double maxDiffRatio) {
        return tolerate(locator, locator.toString(), maxDiffRatio);
    }

    private ScreenAssert tolerate(Locator locator, String label, double maxDiffRatio) {
        Region.tolerate(label, 0, 0, 0, 0, maxDiffRatio); // validates ratio
        targets.add(new Target(locator, label, 0, 0, 0, 0, false, maxDiffRatio));
        return this;
    }

    /** Allow up to {@code maxDiffRatio} (0..1) of a fixed area (screenshot pixel coordinates) to differ. */
    public ScreenAssert tolerateArea(int x, int y, int width, int height, double maxDiffRatio) {
        Region.tolerate("area", x, y, width, height, maxDiffRatio);
        targets.add(new Target(null, "area", x, y, width, height, false, maxDiffRatio));
        return this;
    }

    /** Capture the whole scrollable page instead of the viewport. */
    public ScreenAssert fullPage() {
        this.fullPage = true;
        return this;
    }

    /** Per-pixel color sensitivity 0..1: 0 = exact, 0.1 = default, 0.3 = lenient. */
    public ScreenAssert colorThreshold(double threshold) {
        this.colorThreshold = threshold;
        return this;
    }

    /** Max number of differing pixels outside tolerance regions. */
    public ScreenAssert maxDiffPixels(int pixels) {
        this.maxDiffPixels = pixels;
        return this;
    }

    /** Max fraction (0..1) of differing pixels outside tolerance regions. */
    public ScreenAssert maxDiffRatio(double ratio) {
        this.maxDiffRatio = ratio;
        return this;
    }

    /** Count anti-aliasing differences too (default: they are ignored). */
    public ScreenAssert strictAntialiasing() {
        this.ignoreAntialiasing = false;
        return this;
    }

    /** CSS injected while taking this screenshot. */
    public ScreenAssert style(String css) {
        this.style = css;
        return this;
    }

    /** Take the screenshot, compare it with the baseline and report the result. */
    public ScreenResult verify() {
        if (verified) {
            throw new IllegalStateException("verify() already called for screen " + group + "/" + name);
        }
        verified = true;
        ScreenResult result = snapshotter.check(this);
        if (onResult != null) onResult.accept(result);
        return result;
    }

    // ---- accessors for ScreenSnapshotter ----

    Page page() { return page; }
    Locator element() { return element; }
    String group() { return group; }
    String name() { return name; }
    List<Target> targets() { return targets; }

    boolean effectiveFullPage() {
        return fullPage != null ? fullPage : snapshotter.settings().fullPage();
    }

    String effectiveStyle() {
        String global = snapshotter.settings().style();
        if (global == null) return style;
        return style == null ? global : global + "\n" + style;
    }

    CompareOptions compareOptions(List<Region> regions) {
        ScreenSettings s = snapshotter.settings();
        return new CompareOptions(
                colorThreshold != null ? colorThreshold : s.colorThreshold(),
                maxDiffPixels != null ? maxDiffPixels : s.maxDiffPixels(),
                maxDiffRatio != null ? maxDiffRatio : s.maxDiffRatio(),
                ignoreAntialiasing != null ? ignoreAntialiasing : s.ignoreAntialiasing(),
                regions);
    }
}
