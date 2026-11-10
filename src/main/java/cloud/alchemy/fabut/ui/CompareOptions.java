package cloud.alchemy.fabut.ui;

import java.util.List;

/**
 * Options for {@link ImageComparator}.
 *
 * @param colorThreshold     per-pixel color sensitivity 0..1 (perceptual YIQ distance, 0 = exact match).
 *                           0.1 tolerates invisible encoding noise but catches any visible change.
 * @param maxDiffPixels      allowed number of differing pixels outside tolerance regions; negative = not set
 * @param maxDiffRatio       allowed fraction (0..1) of differing pixels outside tolerance regions; negative = not set
 * @param ignoreAntialiasing {@code true} = pixels detected as anti-aliasing (font/edge smoothing) don't count
 * @param regions            ignore and tolerance regions in image pixel coordinates
 */
public record CompareOptions(double colorThreshold, int maxDiffPixels, double maxDiffRatio,
                             boolean ignoreAntialiasing, List<Region> regions) {

    public static final double DEFAULT_COLOR_THRESHOLD = 0.1;

    public CompareOptions {
        if (colorThreshold < 0 || colorThreshold > 1) {
            throw new IllegalArgumentException("colorThreshold must be between 0 and 1, was " + colorThreshold);
        }
        if (maxDiffRatio > 1) {
            throw new IllegalArgumentException("maxDiffRatio must be between 0 and 1, was " + maxDiffRatio);
        }
        regions = regions == null ? List.of() : List.copyOf(regions);
    }

    /** Pixel-perfect defaults: no differing pixels allowed, anti-aliasing noise ignored. */
    public static CompareOptions pixelPerfect() {
        return new CompareOptions(DEFAULT_COLOR_THRESHOLD, -1, -1, true, List.of());
    }

    public CompareOptions withRegions(List<Region> newRegions) {
        return new CompareOptions(colorThreshold, maxDiffPixels, maxDiffRatio, ignoreAntialiasing, newRegions);
    }

    /** Human readable description of the global budget. */
    String budgetDescription() {
        if (maxDiffPixels < 0 && maxDiffRatio < 0) {
            return "0 px (pixel perfect)";
        }
        StringBuilder sb = new StringBuilder();
        if (maxDiffPixels >= 0) sb.append(maxDiffPixels).append(" px");
        if (maxDiffRatio >= 0) {
            if (!sb.isEmpty()) sb.append(" and ");
            sb.append(String.format("%.3f%%", maxDiffRatio * 100));
        }
        return sb.toString();
    }
}
