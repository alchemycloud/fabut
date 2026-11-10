package cloud.alchemy.fabut.ui;

/**
 * Rectangular area of a screenshot (image pixel coordinates) that is either ignored completely
 * or compared with its own deviation budget.
 *
 * @param label        human readable origin (selector, "area", ...) used in reports
 * @param x            left edge in image pixels
 * @param y            top edge in image pixels
 * @param width        width in image pixels
 * @param height       height in image pixels
 * @param ignore       {@code true} = pixels inside are never compared
 * @param maxDiffRatio for tolerance regions: allowed fraction (0..1) of differing pixels inside the region
 */
public record Region(String label, int x, int y, int width, int height, boolean ignore, double maxDiffRatio) {

    public static Region ignore(String label, int x, int y, int width, int height) {
        return new Region(label, x, y, width, height, true, 1.0);
    }

    public static Region tolerate(String label, int x, int y, int width, int height, double maxDiffRatio) {
        if (maxDiffRatio < 0 || maxDiffRatio > 1) {
            throw new IllegalArgumentException("maxDiffRatio must be between 0 and 1, was " + maxDiffRatio);
        }
        return new Region(label, x, y, width, height, false, maxDiffRatio);
    }

    boolean contains(int px, int py) {
        return px >= x && py >= y && px < x + width && py < y + height;
    }

    /** Area clipped to an image of the given size. */
    int clippedArea(int imageWidth, int imageHeight) {
        int x0 = Math.max(0, x), y0 = Math.max(0, y);
        int x1 = Math.min(imageWidth, x + width), y1 = Math.min(imageHeight, y + height);
        return Math.max(0, x1 - x0) * Math.max(0, y1 - y0);
    }

    @Override
    public String toString() {
        return (ignore ? "ignore " : "tolerate ") + label + " [x=" + x + " y=" + y + " w=" + width + " h=" + height + "]"
                + (ignore ? "" : String.format(" max %.2f%%", maxDiffRatio * 100));
    }
}
