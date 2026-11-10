package cloud.alchemy.fabut.ui;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImageComparatorTest {

    private static BufferedImage image(int w, int h, Color color) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return img;
    }

    private static BufferedImage copy(BufferedImage src) {
        BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        img.getGraphics().drawImage(src, 0, 0, null);
        return img;
    }

    private static void fill(BufferedImage img, int x, int y, int w, int h, Color color) {
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(x, y, w, h);
        g.dispose();
    }

    private static CompareOptions options(int maxDiffPixels, double maxDiffRatio, Region... regions) {
        return new CompareOptions(0.1, maxDiffPixels, maxDiffRatio, true, List.of(regions));
    }

    @Test
    void identicalImagesPass() {
        BufferedImage a = image(100, 50, Color.WHITE);
        ImageDiff diff = ImageComparator.compare(a, copy(a), CompareOptions.pixelPerfect());

        assertTrue(diff.passed());
        assertEquals(0, diff.diffPixels());
        assertEquals(5000, diff.comparedPixels());
        assertTrue(diff.clusters().isEmpty());
        assertNull(diff.failureReason());
    }

    @Test
    void changedBlockFailsAndIsClustered() {
        BufferedImage expected = image(200, 100, Color.WHITE);
        BufferedImage actual = copy(expected);
        fill(actual, 20, 30, 10, 5, Color.BLACK);

        ImageDiff diff = ImageComparator.compare(expected, actual, CompareOptions.pixelPerfect());

        assertFalse(diff.passed());
        assertEquals(50, diff.diffPixels());
        assertEquals(1, diff.clusters().size());
        assertEquals(new DiffCluster(20, 30, 10, 5, 50), diff.clusters().getFirst());
        assertTrue(diff.failureReason().contains("50 px"));
        assertEquals(0xFFFF0000, diff.diffImage().getRGB(25, 32));
    }

    @Test
    void distantChangesProduceSeparateClustersLargestFirst() {
        BufferedImage expected = image(400, 200, Color.WHITE);
        BufferedImage actual = copy(expected);
        fill(actual, 5, 5, 4, 4, Color.BLACK);
        fill(actual, 300, 150, 10, 10, Color.BLACK);

        ImageDiff diff = ImageComparator.compare(expected, actual, CompareOptions.pixelPerfect());

        assertEquals(2, diff.clusters().size());
        assertEquals(new DiffCluster(300, 150, 10, 10, 100), diff.clusters().get(0));
        assertEquals(new DiffCluster(5, 5, 4, 4, 16), diff.clusters().get(1));
    }

    @Test
    void maxDiffPixelsAllowsSmallDeviation() {
        BufferedImage expected = image(100, 100, Color.WHITE);
        BufferedImage actual = copy(expected);
        fill(actual, 0, 0, 3, 3, Color.BLACK);

        assertTrue(ImageComparator.compare(expected, actual, options(9, -1)).passed());
        assertFalse(ImageComparator.compare(expected, actual, options(8, -1)).passed());
    }

    @Test
    void maxDiffRatioAllowsSmallDeviation() {
        BufferedImage expected = image(100, 100, Color.WHITE);
        BufferedImage actual = copy(expected);
        fill(actual, 0, 0, 10, 10, Color.BLACK); // 1%

        assertTrue(ImageComparator.compare(expected, actual, options(-1, 0.01)).passed());
        assertFalse(ImageComparator.compare(expected, actual, options(-1, 0.009)).passed());
    }

    @Test
    void bothBudgetsMustHold() {
        BufferedImage expected = image(100, 100, Color.WHITE);
        BufferedImage actual = copy(expected);
        fill(actual, 0, 0, 10, 10, Color.BLACK);

        assertFalse(ImageComparator.compare(expected, actual, options(50, 0.5)).passed());
    }

    @Test
    void ignoreRegionExcludesPixels() {
        BufferedImage expected = image(100, 100, Color.WHITE);
        BufferedImage actual = copy(expected);
        fill(actual, 10, 10, 20, 20, Color.RED);

        ImageDiff diff = ImageComparator.compare(expected, actual,
                options(-1, -1, Region.ignore("#clock", 10, 10, 20, 20)));

        assertTrue(diff.passed());
        assertEquals(400, diff.ignoredPixels());
        assertEquals(9600, diff.comparedPixels());
    }

    @Test
    void toleranceRegionHasOwnBudget() {
        BufferedImage expected = image(100, 100, Color.WHITE);
        BufferedImage actual = copy(expected);
        fill(actual, 0, 0, 5, 2, Color.BLACK); // 10 px inside a 10x10 region = 10%

        ImageDiff ok = ImageComparator.compare(expected, actual,
                options(-1, -1, Region.tolerate(".chart", 0, 0, 10, 10, 0.10)));
        assertTrue(ok.passed());
        assertEquals(0, ok.diffPixels());
        assertEquals(10, ok.regionResults().getFirst().diffPixels());

        ImageDiff exceeded = ImageComparator.compare(expected, actual,
                options(-1, -1, Region.tolerate(".chart", 0, 0, 10, 10, 0.05)));
        assertFalse(exceeded.passed());
        assertFalse(exceeded.regionResults().getFirst().passed());
        assertTrue(exceeded.failureReason().contains(".chart"));
    }

    @Test
    void diffsOutsideToleranceRegionStillCount() {
        BufferedImage expected = image(100, 100, Color.WHITE);
        BufferedImage actual = copy(expected);
        fill(actual, 50, 50, 2, 2, Color.BLACK);

        ImageDiff diff = ImageComparator.compare(expected, actual,
                options(-1, -1, Region.tolerate(".chart", 0, 0, 10, 10, 1.0)));
        assertFalse(diff.passed());
        assertEquals(4, diff.diffPixels());
    }

    @Test
    void colorThresholdToleratesInvisibleNoise() {
        BufferedImage expected = image(50, 50, new Color(200, 200, 200));
        BufferedImage actual = image(50, 50, new Color(202, 201, 200));

        assertTrue(ImageComparator.compare(expected, actual, CompareOptions.pixelPerfect()).passed());
        CompareOptions exact = new CompareOptions(0, -1, -1, true, List.of());
        assertFalse(ImageComparator.compare(expected, actual, exact).passed());
    }

    @Test
    void sizeMismatchFails() {
        ImageDiff diff = ImageComparator.compare(image(100, 100, Color.WHITE), image(100, 120, Color.WHITE),
                options(-1, 1.0));

        assertFalse(diff.passed());
        assertTrue(diff.sizeMismatch());
        assertEquals(2000, diff.diffPixels());
        assertTrue(diff.failureReason().startsWith("size differs: expected 100x100, actual 100x120"));
    }

    @Test
    void antialiasedEdgeIsIgnoredByDefault() {
        // Two-tone image with a hard vertical edge; actual shifts the edge pixel to an intermediate shade.
        BufferedImage expected = image(20, 20, Color.WHITE);
        fill(expected, 10, 0, 10, 20, Color.BLACK);
        BufferedImage actual = copy(expected);
        fill(actual, 10, 8, 1, 1, new Color(128, 128, 128));

        ImageDiff lenient = ImageComparator.compare(expected, actual, CompareOptions.pixelPerfect());
        assertTrue(lenient.passed(), lenient.failureReason());
        assertEquals(1, lenient.antialiasedPixels());

        CompareOptions strict = new CompareOptions(0.1, -1, -1, false, List.of());
        assertFalse(ImageComparator.compare(expected, actual, strict).passed());
    }

    @Test
    void invalidOptionsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CompareOptions(1.5, -1, -1, true, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new CompareOptions(0.1, -1, 2, true, List.of()));
        assertThrows(IllegalArgumentException.class, () -> Region.tolerate("x", 0, 0, 1, 1, -0.1));
    }

    @Test
    void textDiffShowsChangedLines() {
        List<String> diff = TextDiff.diff("- heading \"Login\"\n- button \"Log in\"", "- heading \"Login\"\n- button \"Sign in\"");
        assertEquals(List.of("- - button \"Log in\"", "+ - button \"Sign in\""), diff);
    }
}
