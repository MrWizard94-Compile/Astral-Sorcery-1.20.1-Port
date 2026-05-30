package hellfirepvp.astralsorcery.client.screen.helper;

/** Axis-aligned 2D bounding box for scissor / hit-test regions on the journal screen. */
public final class ScreenRenderBoundingBox {

    private final double lx, ly, hx, hy;

    public ScreenRenderBoundingBox(double lx, double ly, double hx, double hy) {
        this.lx = lx;
        this.ly = ly;
        this.hx = hx;
        this.hy = hy;
    }

    public boolean isInBox(double x, double y) {
        return x >= lx && x <= hx && y >= ly && y <= hy;
    }

    public double getLowerX() { return lx; }
    public double getLowerY() { return ly; }
    public double getWidth()  { return hx - lx; }
    public double getHeight() { return hy - ly; }
    public double getMidX()   { return getWidth()  / 2; }
    public double getMidY()   { return getHeight() / 2; }

    @Override
    public String toString() {
        return "ScreenRenderBoundingBox{" + lx + "," + ly + " → " + hx + "," + hy + "}";
    }
}
