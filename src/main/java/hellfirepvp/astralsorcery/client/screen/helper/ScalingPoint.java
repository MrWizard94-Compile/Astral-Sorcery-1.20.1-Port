package hellfirepvp.astralsorcery.client.screen.helper;

/**
 * A 2D point that tracks both raw and scaled coordinates.
 * Used by the journal progression renderer for pan/zoom offset tracking.
 */
public class ScalingPoint {

    private float posX, posY;
    private float scaledX, scaledY;

    private ScalingPoint() {}

    public static ScalingPoint createPoint(float posX, float posY, float scale, boolean arePositionsScaled) {
        ScalingPoint sp = new ScalingPoint();
        if (arePositionsScaled) {
            sp.updateScaledPos(posX, posY, scale);
        } else {
            sp.updatePos(posX, posY, scale);
        }
        return sp;
    }

    public void updatePos(float posX, float posY, float scale) {
        this.posX = posX;
        this.posY = posY;
        this.scaledX = scale * posX;
        this.scaledY = scale * posY;
    }

    public void updateScaledPos(float scaledX, float scaledY, float scale) {
        this.scaledX = scaledX;
        this.scaledY = scaledY;
        this.posX = scale > 0 ? scaledX / scale : 0;
        this.posY = scale > 0 ? scaledY / scale : 0;
    }

    public void rescale(float newScale) {
        this.scaledX = this.posX * newScale;
        this.scaledY = this.posY * newScale;
    }

    public float getPosX()       { return posX; }
    public float getPosY()       { return posY; }
    public float getScaledPosX() { return scaledX; }
    public float getScaledPosY() { return scaledY; }
}
