package cloud.alchemy.fabut.ui;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pure-Java perceptual image comparison (no browser required).
 *
 * <p>Per-pixel color distance is measured in YIQ space (same metric as pixelmatch), so
 * {@code colorThreshold} has the same meaning as Playwright's {@code threshold}. Pixels that only
 * differ because of anti-aliasing (font smoothing, sub-pixel edges) can be ignored. Differences are
 * grouped into {@link DiffCluster}s so failures can point at concrete areas of the screen.</p>
 */
public final class ImageComparator {

    /** Maximum possible YIQ delta between two colors. */
    private static final double MAX_YIQ_DELTA = 35215.0;
    /** Cluster grid cell size in pixels; diffs within ~2 cells are merged into one cluster. */
    private static final int CELL = 16;
    private static final int MAX_CLUSTERS = 20;

    private static final int RED = 0xFFFF0000;
    private static final int ORANGE = 0xFFFF9900;
    private static final int YELLOW = 0xFFFFFF00;

    private ImageComparator() {
    }

    public static ImageDiff compare(BufferedImage expected, BufferedImage actual, CompareOptions options) {
        final int ew = expected.getWidth(), eh = expected.getHeight();
        final int aw = actual.getWidth(), ah = actual.getHeight();
        final int w = Math.max(ew, aw), h = Math.max(eh, ah);

        final int[] e = pixels(expected, w, h);
        final int[] a = pixels(actual, w, h);
        final int[] out = new int[w * h];

        final List<Region> regions = options.regions();
        final boolean[] ignored = ignoreMask(regions, w, h);
        final List<Region> tolerance = regions.stream().filter(r -> !r.ignore()).toList();
        final int[] toleranceDiff = new int[tolerance.size()];

        final double maxDelta = MAX_YIQ_DELTA * options.colorThreshold() * options.colorThreshold();
        final ClusterGrid grid = new ClusterGrid(w, h);

        int diff = 0, aa = 0, ignoredCount = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int i = y * w + x;
                if (ignored != null && ignored[i]) {
                    ignoredCount++;
                    out[i] = ignoredPixel(a[i]);
                    continue;
                }
                final boolean inBoth = x < ew && y < eh && x < aw && y < ah;
                boolean isDiff;
                if (!inBoth) {
                    isDiff = true;
                } else if (e[i] == a[i]) {
                    isDiff = false;
                } else {
                    isDiff = Math.abs(colorDelta(e[i], a[i], false)) > maxDelta;
                    if (isDiff && options.ignoreAntialiasing()
                            && (antialiased(e, x, y, w, h, a) || antialiased(a, x, y, w, h, e))) {
                        aa++;
                        out[i] = YELLOW;
                        continue;
                    }
                }
                if (!isDiff) {
                    out[i] = grayPixel(inBoth ? a[i] : 0);
                    continue;
                }
                grid.add(x, y);
                int t = toleranceIndex(tolerance, x, y);
                if (t >= 0) {
                    toleranceDiff[t]++;
                    out[i] = ORANGE;
                } else {
                    diff++;
                    out[i] = RED;
                }
            }
        }

        final List<ImageDiff.RegionResult> regionResults = new ArrayList<>();
        final List<String> failures = new ArrayList<>();
        for (int t = 0; t < tolerance.size(); t++) {
            Region r = tolerance.get(t);
            int area = r.clippedArea(w, h);
            boolean ok = toleranceDiff[t] <= Math.floor(r.maxDiffRatio() * area);
            regionResults.add(new ImageDiff.RegionResult(r, area, toleranceDiff[t], ok));
            if (!ok) {
                failures.add(String.format("tolerance region %s: %d px (%.3f%%) differ, allowed %.3f%%",
                        r.label(), toleranceDiff[t], area == 0 ? 0 : 100.0 * toleranceDiff[t] / area, r.maxDiffRatio() * 100));
            }
        }

        final int compared = w * h - ignoredCount;
        if (ew != aw || eh != ah) {
            failures.addFirst("size differs: expected " + ew + "x" + eh + ", actual " + aw + "x" + ah);
        }
        if (!withinBudget(diff, compared, options)) {
            failures.add(String.format("%d px (%.3f%%) differ, allowed %s", diff,
                    compared == 0 ? 0 : 100.0 * diff / compared, options.budgetDescription()));
        }

        BufferedImage diffImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        diffImage.setRGB(0, 0, w, h, out, 0, w);

        return new ImageDiff(ew, eh, aw, ah, diff, compared, aa, ignoredCount, List.copyOf(regionResults),
                grid.clusters(), diffImage, failures.isEmpty(), failures.isEmpty() ? null : String.join("; ", failures));
    }

    private static boolean withinBudget(int diff, int compared, CompareOptions o) {
        if (o.maxDiffPixels() < 0 && o.maxDiffRatio() < 0) {
            return diff == 0;
        }
        if (o.maxDiffPixels() >= 0 && diff > o.maxDiffPixels()) {
            return false;
        }
        return !(o.maxDiffRatio() >= 0 && diff > Math.floor(o.maxDiffRatio() * compared));
    }

    private static int toleranceIndex(List<Region> tolerance, int x, int y) {
        for (int t = 0; t < tolerance.size(); t++) {
            if (tolerance.get(t).contains(x, y)) return t;
        }
        return -1;
    }

    private static boolean[] ignoreMask(List<Region> regions, int w, int h) {
        boolean[] mask = null;
        for (Region r : regions) {
            if (!r.ignore()) continue;
            if (mask == null) mask = new boolean[w * h];
            int x0 = Math.max(0, r.x()), y0 = Math.max(0, r.y());
            int x1 = Math.min(w, r.x() + r.width()), y1 = Math.min(h, r.y() + r.height());
            for (int y = y0; y < y1; y++) {
                for (int x = x0; x < x1; x++) {
                    mask[y * w + x] = true;
                }
            }
        }
        return mask;
    }

    /** ARGB pixels padded with transparent black to {@code w x h}. */
    private static int[] pixels(BufferedImage img, int w, int h) {
        int iw = img.getWidth(), ih = img.getHeight();
        int[] px = new int[w * h];
        int[] row = img.getRGB(0, 0, iw, ih, null, 0, iw);
        for (int y = 0; y < ih; y++) {
            System.arraycopy(row, y * iw, px, y * w, iw);
        }
        return px;
    }

    // ---- color math (pixelmatch algorithm) ----

    private static double blend(int c, double alpha) {
        return 255 + (c - 255) * alpha;
    }

    private static double rgb2y(double r, double g, double b) {
        return r * 0.29889531 + g * 0.58662247 + b * 0.11448223;
    }

    private static double rgb2i(double r, double g, double b) {
        return r * 0.59597799 - g * 0.27417610 - b * 0.32180189;
    }

    private static double rgb2q(double r, double g, double b) {
        return r * 0.21147017 - g * 0.52261711 + b * 0.31114694;
    }

    /** Signed perceptual delta between two ARGB colors; with {@code yOnly} returns brightness delta only. */
    static double colorDelta(int c1, int c2, boolean yOnly) {
        if (c1 == c2) return 0;
        double a1 = ((c1 >>> 24) & 0xFF) / 255.0, a2 = ((c2 >>> 24) & 0xFF) / 255.0;
        double r1 = blend((c1 >> 16) & 0xFF, a1), g1 = blend((c1 >> 8) & 0xFF, a1), b1 = blend(c1 & 0xFF, a1);
        double r2 = blend((c2 >> 16) & 0xFF, a2), g2 = blend((c2 >> 8) & 0xFF, a2), b2 = blend(c2 & 0xFF, a2);
        double y1 = rgb2y(r1, g1, b1), y2 = rgb2y(r2, g2, b2);
        double y = y1 - y2;
        if (yOnly) return y;
        double i = rgb2i(r1, g1, b1) - rgb2i(r2, g2, b2);
        double q = rgb2q(r1, g1, b1) - rgb2q(r2, g2, b2);
        double delta = 0.5053 * y * y + 0.299 * i * i + 0.1957 * q * q;
        return y1 > y2 ? -delta : delta;
    }

    /** Whether the pixel at (x1,y1) is likely anti-aliasing (pixelmatch heuristic). */
    private static boolean antialiased(int[] img, int x1, int y1, int w, int h, int[] img2) {
        int x0 = Math.max(x1 - 1, 0), y0 = Math.max(y1 - 1, 0);
        int x2 = Math.min(x1 + 1, w - 1), y2 = Math.min(y1 + 1, h - 1);
        int pos = y1 * w + x1;
        int zeroes = (x1 == x0 || x1 == x2 || y1 == y0 || y1 == y2) ? 1 : 0;
        double min = 0, max = 0;
        int minX = 0, minY = 0, maxX = 0, maxY = 0;
        for (int x = x0; x <= x2; x++) {
            for (int y = y0; y <= y2; y++) {
                if (x == x1 && y == y1) continue;
                double delta = colorDelta(img[pos], img[y * w + x], true);
                if (delta == 0) {
                    zeroes++;
                    if (zeroes > 2) return false;
                } else if (delta < min) {
                    min = delta;
                    minX = x;
                    minY = y;
                } else if (delta > max) {
                    max = delta;
                    maxX = x;
                    maxY = y;
                }
            }
        }
        if (min == 0 || max == 0) return false;
        return (hasManySiblings(img, minX, minY, w, h) && hasManySiblings(img2, minX, minY, w, h))
                || (hasManySiblings(img, maxX, maxY, w, h) && hasManySiblings(img2, maxX, maxY, w, h));
    }

    private static boolean hasManySiblings(int[] img, int x1, int y1, int w, int h) {
        int x0 = Math.max(x1 - 1, 0), y0 = Math.max(y1 - 1, 0);
        int x2 = Math.min(x1 + 1, w - 1), y2 = Math.min(y1 + 1, h - 1);
        int pos = y1 * w + x1;
        int zeroes = (x1 == x0 || x1 == x2 || y1 == y0 || y1 == y2) ? 1 : 0;
        for (int x = x0; x <= x2; x++) {
            for (int y = y0; y <= y2; y++) {
                if (x == x1 && y == y1) continue;
                if (img[pos] == img[y * w + x]) zeroes++;
                if (zeroes > 2) return true;
            }
        }
        return false;
    }

    private static int grayPixel(int c) {
        double alpha = ((c >>> 24) & 0xFF) / 255.0;
        double yv = rgb2y(blend((c >> 16) & 0xFF, alpha), blend((c >> 8) & 0xFF, alpha), blend(c & 0xFF, alpha));
        int v = (int) Math.round(255 + (yv - 255) * 0.1);
        return 0xFF000000 | (v << 16) | (v << 8) | v;
    }

    private static int ignoredPixel(int c) {
        int g = grayPixel(c) & 0xFF;
        int r = (int) (g * 0.75), gg = (int) (g * 0.85);
        return 0xFF000000 | (r << 16) | (gg << 8) | 255;
    }

    // ---- clustering ----

    /** Groups diff pixels on a coarse grid and merges neighbouring cells into clusters. */
    private static final class ClusterGrid {
        private final int cols, rows;
        private final int[] count, minX, minY, maxX, maxY;

        ClusterGrid(int w, int h) {
            cols = (w + CELL - 1) / CELL;
            rows = (h + CELL - 1) / CELL;
            int n = cols * rows;
            count = new int[n];
            minX = new int[n];
            minY = new int[n];
            maxX = new int[n];
            maxY = new int[n];
        }

        void add(int x, int y) {
            int c = (y / CELL) * cols + (x / CELL);
            if (count[c] == 0) {
                minX[c] = maxX[c] = x;
                minY[c] = maxY[c] = y;
            } else {
                minX[c] = Math.min(minX[c], x);
                maxX[c] = Math.max(maxX[c], x);
                minY[c] = Math.min(minY[c], y);
                maxY[c] = Math.max(maxY[c], y);
            }
            count[c]++;
        }

        List<DiffCluster> clusters() {
            boolean[] seen = new boolean[count.length];
            List<DiffCluster> result = new ArrayList<>();
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            for (int start = 0; start < count.length; start++) {
                if (count[start] == 0 || seen[start]) continue;
                int px = 0, x0 = Integer.MAX_VALUE, y0 = Integer.MAX_VALUE, x1 = -1, y1 = -1;
                queue.add(start);
                seen[start] = true;
                while (!queue.isEmpty()) {
                    int c = queue.poll();
                    px += count[c];
                    x0 = Math.min(x0, minX[c]);
                    y0 = Math.min(y0, minY[c]);
                    x1 = Math.max(x1, maxX[c]);
                    y1 = Math.max(y1, maxY[c]);
                    int cx = c % cols, cy = c / cols;
                    for (int dy = -2; dy <= 2; dy++) {
                        for (int dx = -2; dx <= 2; dx++) {
                            int nx = cx + dx, ny = cy + dy;
                            if (nx < 0 || ny < 0 || nx >= cols || ny >= rows) continue;
                            int n = ny * cols + nx;
                            if (count[n] > 0 && !seen[n]) {
                                seen[n] = true;
                                queue.add(n);
                            }
                        }
                    }
                }
                result.add(new DiffCluster(x0, y0, x1 - x0 + 1, y1 - y0 + 1, px));
            }
            result.sort(Comparator.comparingInt(DiffCluster::pixels).reversed());
            return result.size() > MAX_CLUSTERS ? List.copyOf(result.subList(0, MAX_CLUSTERS)) : List.copyOf(result);
        }
    }
}
