package cloud.alchemy.fabut.ui;

/**
 * Bounding box of a group of nearby differing pixels (image pixel coordinates).
 */
public record DiffCluster(int x, int y, int width, int height, int pixels) {

    int centerX() {
        return x + width / 2;
    }

    int centerY() {
        return y + height / 2;
    }

    @Override
    public String toString() {
        return "x=" + x + " y=" + y + " w=" + width + " h=" + height + " (" + pixels + " px)";
    }
}
