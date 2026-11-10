package cloud.alchemy.fabut.ui;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Result of comparing two images with {@link ImageComparator}.
 *
 * @param expectedWidth      baseline width
 * @param expectedHeight     baseline height
 * @param actualWidth        actual width
 * @param actualHeight       actual height
 * @param diffPixels         differing pixels outside tolerance regions (counted against the global budget)
 * @param comparedPixels     pixels that were compared (total minus ignored)
 * @param antialiasedPixels  differing pixels classified as anti-aliasing (not counted when ignored)
 * @param ignoredPixels      pixels inside ignore regions
 * @param regionResults      per tolerance region outcome
 * @param clusters           groups of differing pixels, largest first
 * @param diffImage          visualisation: red = diff, orange = diff inside tolerance region,
 *                           yellow = anti-aliasing, blue tint = ignored
 * @param passed             whether all budgets hold
 * @param failureReason      why it failed, {@code null} when passed
 */
public record ImageDiff(int expectedWidth, int expectedHeight, int actualWidth, int actualHeight,
                        int diffPixels, int comparedPixels, int antialiasedPixels, int ignoredPixels,
                        List<RegionResult> regionResults, List<DiffCluster> clusters,
                        BufferedImage diffImage, boolean passed, String failureReason) {

    /** Outcome for a single tolerance region. */
    public record RegionResult(Region region, int area, int diffPixels, boolean passed) {
        public double diffRatio() {
            return area == 0 ? 0 : (double) diffPixels / area;
        }
    }

    public boolean sizeMismatch() {
        return expectedWidth != actualWidth || expectedHeight != actualHeight;
    }

    public double diffRatio() {
        return comparedPixels == 0 ? 0 : (double) diffPixels / comparedPixels;
    }
}
