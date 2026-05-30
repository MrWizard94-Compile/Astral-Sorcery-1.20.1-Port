package hellfirepvp.astralsorcery.client.screen.helper;

import hellfirepvp.astralsorcery.client.screen.base.WidthHeightScreen;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * Manages zoom level and coordinate mapping for the journal's galaxy/cluster views.
 * Subclasses override {@link #buildRequiredRectangle()} to define the node bounding box.
 *
 * <p>1.16 → 1.20: MathHelper → Mth; WidthHeightScreen package path updated.</p>
 */
public abstract class SizeHandler {

    private static final int W_H_NODE = 18;

    private float widthHeightNodes  = W_H_NODE;
    private float spaceBetweenNodes = W_H_NODE;

    private float shiftX, shiftY;
    private float leftOffset, topOffset;
    private float totalWidth, totalHeight;

    private float scalingFactor = 1f;
    private float maxScale  = 10f;
    private float minScale  = 1f;
    private float scaleSpeed = 0.2f;

    public void setMaxScale(float v)              { this.maxScale = v; }
    public void setMinScale(float v)              { this.minScale = v; }
    public void setScaleSpeed(float v)            { this.scaleSpeed = v; }
    public void setWidthHeightNodes(float v)      { this.widthHeightNodes = v; }
    public void setSpaceBetweenNodes(float v)     { this.spaceBetweenNodes = v; }

    public void updateSize() {
        resetZoom();
        float leftMost = 0, rightMost = 0, upperMost = 0, lowerMost = 0;
        float[] req = buildRequiredRectangle();
        if (req != null) {
            leftMost  = req[0];
            rightMost = req[1];
            upperMost = req[2];
            lowerMost = req[3];
        }
        shiftX = (leftMost + rightMost) / 2f;
        shiftY = (lowerMost + upperMost) / 2f;
        float w = rightMost - leftMost;
        float h = lowerMost - upperMost;
        leftOffset = leftMost - shiftX;
        topOffset  = upperMost - shiftY;
        totalWidth  = w * widthHeightNodes + Math.max(w - 1, 0) * spaceBetweenNodes;
        totalHeight = h * widthHeightNodes + Math.max(h - 1, 0) * spaceBetweenNodes;
    }

    @Nullable
    public abstract float[] buildRequiredRectangle();

    public float getTotalWidth()  { return totalWidth  * scalingFactor; }
    public float getTotalHeight() { return totalHeight * scalingFactor; }
    public Point.Float getRelativeCenter() { return new Point.Float(getTotalWidth() / 2f, getTotalHeight() / 2f); }
    public float getScalingFactor()    { return scalingFactor; }
    public float getZoomedWHNode()     { return widthHeightNodes  * scalingFactor; }
    public float getZoomedSpaceBetweenNodes() { return spaceBetweenNodes * scalingFactor; }
    public float scaleAccordingly(float v) { return v * scalingFactor; }

    public void handleZoomIn()  { if (scalingFactor < maxScale) scalingFactor = Math.min(maxScale, scalingFactor + scaleSpeed); }
    public void handleZoomOut() { if (scalingFactor > minScale) scalingFactor = Math.max(minScale, scalingFactor - scaleSpeed); }
    public void forceScaleTo(float scale) { this.scalingFactor = scale; }
    public void resetZoom() { this.scalingFactor = 1f; }

    public float clampX(float v) { return Mth.clamp(v, 0, getTotalWidth()); }
    public float clampY(float v) { return Mth.clamp(v, 0, getTotalHeight()); }

    public float evRelativePosX(float relX) {
        float shifted = relX - shiftX;
        float leftShift = shifted - leftOffset;
        return leftShift * (getZoomedWHNode() + getZoomedSpaceBetweenNodes()) + 0.5f * getZoomedWHNode();
    }

    public float evRelativePosY(float relY) {
        float shifted = relY - shiftY;
        float topShift = shifted - topOffset;
        return topShift * (getZoomedWHNode() + getZoomedSpaceBetweenNodes()) + 0.5f * getZoomedWHNode();
    }

    public Point.Float evRelativePos(Point.Float p) { return new Point.Float(evRelativePosX(p.x), evRelativePosY(p.y)); }
    public Point.Float evRelativePos(Point p)       { return new Point.Float(evRelativePosX(p.x), evRelativePosY(p.y)); }

    public float scaledDistanceX(float fromX, float toX) { return evRelativePosX(toX) - evRelativePosX(fromX); }
    public float scaledDistanceY(float fromY, float toY) { return evRelativePosY(toY) - evRelativePosY(fromY); }

    public Point.Float scalePointToGui(WidthHeightScreen screen, ScalingPoint offset, Point.Float point) {
        Point.Float shifted = evRelativePos(point);
        float fx = shifted.x - offset.getScaledPosX() + screen.getGuiLeft() + screen.getGuiWidth()  / 2f;
        float fy = shifted.y - offset.getScaledPosY() + screen.getGuiTop()  + screen.getGuiHeight() / 2f;
        return new Point.Float(fx, fy);
    }
}
