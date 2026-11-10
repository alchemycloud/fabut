package cloud.alchemy.fabut.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.ScreenshotAnimations;
import com.microsoft.playwright.options.ScreenshotCaret;
import com.microsoft.playwright.options.ScreenshotScale;
import org.opentest4j.AssertionFailedError;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Takes stable Playwright screenshots, compares them with stored baselines and writes failure artifacts.
 *
 * <p>Usable without {@link FabutUi}:</p>
 * <pre>{@code
 * ScreenSnapshotter screens = new ScreenSnapshotter(ScreenSettings.defaults());
 * screens.screen(page, "checkout", "summary").ignore("#clock").verify();   // throws on mismatch
 * }</pre>
 */
public final class ScreenSnapshotter {

    /** Mask color painted over ignored elements (identical in baseline and actual). */
    static final String MASK_COLOR = "#FF00FF";

    private static final String DESCRIBE_ELEMENTS_JS = """
            (points) => {
              const sx = window.scrollX, sy = window.scrollY;
              const rects = [];
              for (const e of document.querySelectorAll('body *')) {
                const r = e.getBoundingClientRect();
                if (r.width > 0 && r.height > 0) rects.push([e, r]);
              }
              const describe = (e) => {
                let d = e.tagName.toLowerCase();
                if (e.id) d += '#' + e.id;
                const testId = e.getAttribute('data-testid');
                if (testId) d += '[data-testid="' + testId + '"]';
                const cls = typeof e.className === 'string' ? e.className.trim().split(/\\s+/).filter(Boolean).slice(0, 3) : [];
                if (cls.length) d += '.' + cls.join('.');
                const text = (e.innerText || e.getAttribute('aria-label') || e.getAttribute('alt') || e.getAttribute('placeholder') || '')
                  .trim().replace(/\\s+/g, ' ');
                if (text) d += ' "' + (text.length > 50 ? text.slice(0, 50) + '…' : text) + '"';
                return d;
              };
              return points.map(([x, y]) => {
                let best = null, bestArea = Infinity;
                for (const [e, r] of rects) {
                  const l = r.left + sx, t = r.top + sy;
                  if (x >= l && x <= l + r.width && y >= t && y <= t + r.height) {
                    const a = r.width * r.height;
                    if (a <= bestArea) { bestArea = a; best = e; }
                  }
                }
                return best ? describe(best) : null;
              });
            }
            """;

    private final ScreenSettings settings;

    public ScreenSnapshotter(ScreenSettings settings) {
        this.settings = settings;
    }

    public ScreenSettings settings() {
        return settings;
    }

    /** Screen assertion of the page viewport (or full page); {@link ScreenAssert#verify()} throws on failure. */
    public ScreenAssert screen(Page page, String group, String name) {
        return new ScreenAssert(this, page, null, group, name, ScreenSnapshotter::throwIfFailed);
    }

    /** Screen assertion of a single element; {@link ScreenAssert#verify()} throws on failure. */
    public ScreenAssert screen(Locator element, String group, String name) {
        return new ScreenAssert(this, element.page(), element, group, name, ScreenSnapshotter::throwIfFailed);
    }

    /** Builder with a custom result handler (used by {@link FabutUi} for soft assertions). */
    ScreenAssert screen(Page page, Locator element, String group, String name, Consumer<ScreenResult> onResult) {
        return new ScreenAssert(this, page, element, group, name, onResult);
    }

    static void throwIfFailed(ScreenResult result) {
        if (!result.passed()) {
            throw new AssertionFailedError(result.message());
        }
    }

    // ---- core ----

    ScreenResult check(ScreenAssert spec) {
        final String fileName = sanitize(spec.name());
        final String suffix = settings.platformSuffix() == null || settings.platformSuffix().isBlank()
                ? "" : "-" + settings.platformSuffix();
        final Path baselineDir = settings.baselineDir().resolve(spec.group());
        final Path outDir = settings.outputDir().resolve(spec.group());
        final Path baseline = baselineDir.resolve(fileName + suffix + ".png");
        final Path ariaBaseline = baselineDir.resolve(fileName + ".aria.yml");
        final Path actualPath = outDir.resolve(fileName + ".actual.png");
        final Path diffPath = outDir.resolve(fileName + ".diff.png");
        final Path comparePath = outDir.resolve(fileName + ".compare.png");
        final Path reportPath = outDir.resolve(fileName + ".json");

        deleteQuietly(actualPath, diffPath, comparePath, reportPath);

        final Capture capture = capture(spec);
        final String aria = settings.captureAria() ? ariaSnapshot(spec) : null;
        final BufferedImage actual = read(capture.png);

        ScreenResult result;
        if (!Files.exists(baseline)) {
            if (settings.updateMode() == UpdateMode.NONE) {
                write(actualPath, capture.png);
                result = new ScreenResult(spec.group(), spec.name(), ScreenResult.Status.MISSING, baseline, actualPath,
                        null, null, reportPath, null, List.of(), List.of(), capture.stable);
            } else {
                write(baseline, capture.png);
                writeAria(ariaBaseline, aria);
                result = new ScreenResult(spec.group(), spec.name(), ScreenResult.Status.NEW, baseline, null,
                        null, null, reportPath, null, List.of(), List.of(), capture.stable);
            }
        } else {
            final BufferedImage expected = readFile(baseline);
            final ImageDiff diff = ImageComparator.compare(expected, actual, spec.compareOptions(capture.regions));
            if (diff.passed()) {
                if (aria != null && !Files.exists(ariaBaseline) && settings.updateMode() != UpdateMode.NONE) {
                    writeAria(ariaBaseline, aria);
                }
                result = new ScreenResult(spec.group(), spec.name(), ScreenResult.Status.MATCH, baseline, null,
                        null, null, reportPath, diff, List.of(), List.of(), capture.stable);
            } else {
                final List<String> elements = describeClusters(spec.page(), diff.clusters(), capture);
                final List<String> ariaDiff = ariaDiff(ariaBaseline, aria);
                write(actualPath, capture.png);
                writeImage(diffPath, diff.diffImage());
                writeImage(comparePath, sideBySide(expected, actual, diff));
                ScreenResult.Status status;
                if (settings.updateMode() == UpdateMode.ALL) {
                    write(baseline, capture.png);
                    writeAria(ariaBaseline, aria);
                    status = ScreenResult.Status.UPDATED;
                } else {
                    status = ScreenResult.Status.MISMATCH;
                }
                result = new ScreenResult(spec.group(), spec.name(), status, baseline, actualPath, diffPath,
                        comparePath, reportPath, diff, elements, ariaDiff, capture.stable);
            }
        }
        if (result.status() != ScreenResult.Status.MATCH) {
            write(reportPath, result.toJson().getBytes(StandardCharsets.UTF_8));
        }
        return result;
    }

    // ---- capture ----

    /** Screenshot bytes plus resolved regions in image coordinates. */
    record Capture(byte[] png, List<Region> regions, boolean stable, double originX, double originY) {
    }

    private Capture capture(ScreenAssert spec) {
        final Page page = spec.page();
        final Locator element = spec.element();
        final List<Locator> masks = spec.targets().stream()
                .filter(t -> t.ignore() && !t.isArea())
                .map(ScreenAssert.Target::locator)
                .toList();

        byte[] previous = null, current = null;
        boolean stable = settings.stabilityAttempts() <= 1;
        for (int attempt = 0; attempt < settings.stabilityAttempts(); attempt++) {
            current = element != null ? shootElement(element, masks, spec) : shootPage(page, masks, spec);
            if (previous != null && Arrays.equals(previous, current)) {
                stable = true;
                break;
            }
            previous = current;
        }

        // Image origin in document coordinates, used to map element boxes into the screenshot.
        double[] scroll = scroll(page);
        double originX, originY;
        if (element != null) {
            BoundingBox box = element.boundingBox();
            originX = (box == null ? 0 : box.x) + scroll[0];
            originY = (box == null ? 0 : box.y) + scroll[1];
        } else if (spec.effectiveFullPage()) {
            originX = 0;
            originY = 0;
        } else {
            originX = scroll[0];
            originY = scroll[1];
        }

        List<Region> regions = new ArrayList<>();
        for (ScreenAssert.Target t : spec.targets()) {
            if (t.isArea()) {
                regions.add(t.ignore()
                        ? Region.ignore(t.label(), t.x(), t.y(), t.width(), t.height())
                        : Region.tolerate(t.label(), t.x(), t.y(), t.width(), t.height(), t.maxDiffRatio()));
                continue;
            }
            for (Locator match : t.locator().all()) {
                BoundingBox b;
                try {
                    b = match.boundingBox();
                } catch (PlaywrightException e) {
                    b = null;
                }
                if (b == null) continue;
                // round outwards and pad 1px so anti-aliased edges are covered
                int x = (int) Math.floor(b.x + scroll[0] - originX) - 1;
                int y = (int) Math.floor(b.y + scroll[1] - originY) - 1;
                int w = (int) Math.ceil(b.x + scroll[0] - originX + b.width) + 1 - x;
                int h = (int) Math.ceil(b.y + scroll[1] - originY + b.height) + 1 - y;
                regions.add(t.ignore()
                        ? Region.ignore(t.label(), x, y, w, h)
                        : Region.tolerate(t.label(), x, y, w, h, t.maxDiffRatio()));
            }
        }
        return new Capture(current, List.copyOf(regions), stable, originX, originY);
    }

    private byte[] shootPage(Page page, List<Locator> masks, ScreenAssert spec) {
        Page.ScreenshotOptions o = new Page.ScreenshotOptions()
                .setFullPage(spec.effectiveFullPage())
                .setAnimations(ScreenshotAnimations.DISABLED)
                .setCaret(ScreenshotCaret.HIDE)
                .setScale(ScreenshotScale.CSS)
                .setMask(masks)
                .setMaskColor(MASK_COLOR);
        if (spec.effectiveStyle() != null) o.setStyle(spec.effectiveStyle());
        return page.screenshot(o);
    }

    private byte[] shootElement(Locator element, List<Locator> masks, ScreenAssert spec) {
        Locator.ScreenshotOptions o = new Locator.ScreenshotOptions()
                .setAnimations(ScreenshotAnimations.DISABLED)
                .setCaret(ScreenshotCaret.HIDE)
                .setScale(ScreenshotScale.CSS)
                .setMask(masks)
                .setMaskColor(MASK_COLOR);
        if (spec.effectiveStyle() != null) o.setStyle(spec.effectiveStyle());
        return element.screenshot(o);
    }

    private static double[] scroll(Page page) {
        Object r = page.evaluate("() => [window.scrollX, window.scrollY]");
        if (r instanceof List<?> l && l.size() == 2) {
            return new double[]{((Number) l.get(0)).doubleValue(), ((Number) l.get(1)).doubleValue()};
        }
        return new double[]{0, 0};
    }

    // ---- diagnostics ----

    private static List<String> describeClusters(Page page, List<DiffCluster> clusters, Capture capture) {
        if (clusters.isEmpty()) return List.of();
        List<List<Double>> points = clusters.stream()
                .map(c -> List.of(capture.originX + c.centerX(), capture.originY + c.centerY()))
                .toList();
        try {
            Object r = page.evaluate(DESCRIBE_ELEMENTS_JS, points);
            if (r instanceof List<?> list) {
                List<String> out = new ArrayList<>();
                for (Object o : list) out.add(o == null ? null : o.toString());
                return out;
            }
        } catch (PlaywrightException ignored) {
            // diagnostics are best effort
        }
        return List.of();
    }

    private static String ariaSnapshot(ScreenAssert spec) {
        try {
            Locator target = spec.element() != null ? spec.element() : spec.page().locator("body");
            return target.ariaSnapshot();
        } catch (PlaywrightException e) {
            return null;
        }
    }

    private static List<String> ariaDiff(Path ariaBaseline, String aria) {
        if (aria == null || !Files.exists(ariaBaseline)) return List.of();
        try {
            return TextDiff.diff(Files.readString(ariaBaseline), aria);
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * expected | actual | diff, labelled, for quick visual review by humans and vision-capable agents.
     * Wide screenshots are stacked vertically, tall ones side by side.
     */
    static BufferedImage sideBySide(BufferedImage expected, BufferedImage actual, ImageDiff diff) {
        final int gap = 12, header = 22;
        final BufferedImage[] parts = {expected, actual, diff.diffImage()};
        final String[] labels = {"EXPECTED", "ACTUAL", "DIFF"};
        final int maxW = Math.max(expected.getWidth(), actual.getWidth());
        final int maxH = Math.max(expected.getHeight(), actual.getHeight());
        final boolean vertical = maxW > maxH;
        final int cellW = maxW + gap, cellH = maxH + header + gap;
        final int width = vertical ? gap + cellW : gap + 3 * cellW;
        final int height = vertical ? 3 * cellH : cellH;
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setColor(new Color(0x2B2B2B));
            g.fillRect(0, 0, width, height);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            for (int i = 0; i < parts.length; i++) {
                int x = vertical ? gap : gap + i * cellW;
                int y = vertical ? i * cellH : 0;
                g.setColor(Color.WHITE);
                g.drawString(labels[i], x, y + 16);
                g.drawImage(parts[i], x, y + header, null);
                if (i > 0) {
                    g.setColor(Color.RED);
                    g.setStroke(new BasicStroke(2));
                    for (DiffCluster c : diff.clusters()) {
                        g.drawRect(x + c.x() - 2, y + header + c.y() - 2, c.width() + 4, c.height() + 4);
                    }
                }
            }
        } finally {
            g.dispose();
        }
        return out;
    }

    // ---- io ----

    static String sanitize(String name) {
        String s = name.replaceAll("[^A-Za-z0-9._-]+", "-").replaceAll("^-+|-+$", "");
        if (s.isEmpty()) throw new IllegalArgumentException("Invalid screen name: '" + name + "'");
        return s;
    }

    private static BufferedImage read(byte[] png) {
        try {
            return ImageIO.read(new ByteArrayInputStream(png));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static BufferedImage readFile(Path path) {
        try {
            BufferedImage img = ImageIO.read(path.toFile());
            if (img == null) throw new IOException("Not an image: " + path);
            return img;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(Path path, byte[] bytes) {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeAria(Path path, String aria) {
        if (aria != null) write(path, aria.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeImage(Path path, BufferedImage image) {
        try {
            Files.createDirectories(path.getParent());
            ImageIO.write(image, "png", path.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void deleteQuietly(Path... paths) {
        for (Path p : paths) {
            try {
                Files.deleteIfExists(p);
            } catch (IOException ignored) {
                // stale artifact, harmless
            }
        }
    }
}
