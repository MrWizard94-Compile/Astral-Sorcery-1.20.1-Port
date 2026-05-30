package hellfirepvp.astralsorcery.client.screen.journal.progression;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.screen.helper.ScalingPoint;
import hellfirepvp.astralsorcery.client.screen.helper.ScreenRenderBoundingBox;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders the galaxy progression view inside {@link ScreenJournalProgression}.
 * Handles zoom, pan, cluster rendering, and the star-parallax background.
 *
 * <p>1.16 → 1.20: MatrixStack → GuiGraphics; ITextProperties → Component;
 * GL11.glScissor → RenderSystem.enableScissor; DefaultVertexFormats → DefaultVertexFormat;
 * Matrix4f moved to JOML; buf.pos → buf.vertex, buf.tex → buf.uv.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenJournalProgressionRenderer {

    private final GalaxySizeHandler sizeHandler;
    private final ScreenJournalProgression parentGui;

    public ScreenRenderBoundingBox realRenderBox;
    private int realCoordLowerX, realCoordLowerY;
    private int realRenderWidth, realRenderHeight;

    private final ScalingPoint mousePointScaled;
    private ScalingPoint previousMousePointScaled;

    private ResearchProgression focusedClusterZoom = null;
    private ResearchProgression focusedClusterMouse = null;
    private ScreenJournalClusterRenderer clusterRenderer = null;

    private long doubleClickLast = 0L;
    private boolean hasPrevOffset = false;

    private final Map<Rectangle, ResearchProgression> clusterRectMap = new HashMap<>();

    public ScreenJournalProgressionRenderer(ScreenJournalProgression gui) {
        this.parentGui = gui;
        this.sizeHandler = new GalaxySizeHandler();
        refreshSize();
        this.mousePointScaled = ScalingPoint.createPoint(
                sizeHandler.clampX(sizeHandler.getTotalWidth() / 2f),
                sizeHandler.clampY(sizeHandler.getTotalHeight() / 2f),
                sizeHandler.getScalingFactor(), false);
        this.moveMouse(sizeHandler.getTotalWidth() / 2f, sizeHandler.getTotalHeight() / 2f);
        applyMovedMouseOffset();
    }

    public void refreshSize() {
        this.sizeHandler.updateSize();
    }

    public void setBox(int left, int top, int right, int bottom) {
        this.realRenderBox  = new ScreenRenderBoundingBox(left, top, right, bottom);
        this.realRenderWidth  = (int) realRenderBox.getWidth();
        this.realRenderHeight = (int) realRenderBox.getHeight();
    }

    public void moveMouse(float changedX, float changedY) {
        if (sizeHandler.getScalingFactor() >= 6.1f && clusterRenderer != null) {
            clusterRenderer.moveMouse(changedX, changedY);
        } else if (hasPrevOffset) {
            mousePointScaled.updateScaledPos(
                    sizeHandler.clampX(previousMousePointScaled.getScaledPosX() + changedX),
                    sizeHandler.clampY(previousMousePointScaled.getScaledPosY() + changedY),
                    sizeHandler.getScalingFactor());
        } else {
            mousePointScaled.updateScaledPos(
                    sizeHandler.clampX(mousePointScaled.getScaledPosX()),
                    sizeHandler.clampY(mousePointScaled.getScaledPosY()),
                    sizeHandler.getScalingFactor());
        }
    }

    public void applyMovedMouseOffset() {
        if (sizeHandler.getScalingFactor() >= 6.1f && clusterRenderer != null) {
            clusterRenderer.applyMovedMouseOffset();
        } else {
            this.previousMousePointScaled = ScalingPoint.createPoint(
                    mousePointScaled.getScaledPosX(), mousePointScaled.getScaledPosY(),
                    sizeHandler.getScalingFactor(), true);
            this.hasPrevOffset = true;
        }
    }

    public void updateOffset(int guiLeft, int guiTop) {
        this.realCoordLowerX = guiLeft;
        this.realCoordLowerY = guiTop;
    }

    public void centerMouse() {
        moveMouse(parentGui.getGuiLeft() + sizeHandler.getTotalWidth() / 2f,
                  parentGui.getGuiTop()  + sizeHandler.getTotalHeight() / 2f);
    }

    public void updateMouseState() {
        moveMouse(0, 0);
    }

    public void unfocus() {
        focusedClusterZoom = null;
    }

    public void focus(@Nonnull ResearchProgression cluster) {
        this.focusedClusterZoom = cluster;
        this.clusterRenderer = new ScreenJournalClusterRenderer(cluster,
                realRenderHeight, realRenderWidth, realCoordLowerX, realCoordLowerY);
    }

    public boolean propagateClick(float mouseX, float mouseY) {
        if (clusterRenderer != null && sizeHandler.getScalingFactor() > 6) {
            if (clusterRenderer.propagateClick(parentGui, mouseX, mouseY)) return true;
        }
        if (focusedClusterMouse != null && sizeHandler.getScalingFactor() <= 6) {
            long now = System.currentTimeMillis();
            if (now - doubleClickLast < 400L) {
                int timeout = 500;
                while (focusedClusterMouse != null && sizeHandler.getScalingFactor() < 9.9f && timeout-- > 0) {
                    handleZoomIn(mouseX, mouseY);
                }
                doubleClickLast = 0L;
                return true;
            }
            doubleClickLast = now;
        }
        return false;
    }

    public void drawMouseHighlight(GuiGraphics graphics, int mouseX, int mouseY) {
        if (clusterRenderer != null && sizeHandler.getScalingFactor() > 6) {
            clusterRenderer.drawMouseHighlight(graphics, mouseX, mouseY);
        }
    }

    public void resetZoom() {
        sizeHandler.resetZoom();
        rescale(sizeHandler.getScalingFactor());
    }

    public void handleZoomOut() {
        sizeHandler.handleZoomOut();
        rescale(sizeHandler.getScalingFactor());
        if (sizeHandler.getScalingFactor() <= 4.0f) {
            unfocus();
        } else if (sizeHandler.getScalingFactor() >= 6.0f && clusterRenderer != null) {
            clusterRenderer.handleZoomOut();
        }
    }

    public void handleZoomIn(float mouseX, float mouseY) {
        float scale = sizeHandler.getScalingFactor();
        if (scale >= 4.0f) {
            if (focusedClusterZoom == null) {
                ResearchProgression prog = tryFocusCluster(mouseX, mouseY);
                if (prog != null) focus(prog);
            }
            if (focusedClusterZoom == null) return;

            if (scale < 6.1f) {
                float vDiv = (2f - (scale - 4f)) * 10f;
                JournalCluster cluster = JournalProgressionClusterMapping.getClusterMapping(focusedClusterZoom);
                float cx = sizeHandler.evRelativePosX(cluster.x)  + sizeHandler.scaledDistanceX(cluster.x, cluster.maxX) / 2f;
                float cy = sizeHandler.evRelativePosY(cluster.y) + sizeHandler.scaledDistanceY(cluster.y, cluster.maxY) / 2f;
                Vector3 center   = new Vector3(cx, cy, 0);
                Vector3 mousePos = new Vector3(mousePointScaled.getScaledPosX(), mousePointScaled.getScaledPosY(), 0);
                Vector3 dir = center.subtract(mousePos);
                if (vDiv > 0.05f) dir.divide(vDiv);

                if (!hasPrevOffset) {
                    mousePointScaled.updateScaledPos(
                            sizeHandler.clampX((float)(mousePos.getX() + dir.getX())),
                            sizeHandler.clampY((float)(mousePos.getY() + dir.getY())),
                            sizeHandler.getScalingFactor());
                } else {
                    previousMousePointScaled.updateScaledPos(
                            sizeHandler.clampX((float)(mousePos.getX() + dir.getX())),
                            sizeHandler.clampY((float)(mousePos.getY() + dir.getY())),
                            sizeHandler.getScalingFactor());
                }
                updateMouseState();
            } else if (clusterRenderer != null) {
                clusterRenderer.handleZoomIn();
            }
        }
        sizeHandler.handleZoomIn();
        mousePointScaled.rescale(sizeHandler.getScalingFactor());
        if (previousMousePointScaled != null) previousMousePointScaled.rescale(sizeHandler.getScalingFactor());
    }

    private void rescale(float newScale) {
        mousePointScaled.rescale(newScale);
        if (previousMousePointScaled != null) previousMousePointScaled.rescale(newScale);
        updateMouseState();
    }

    public void drawProgressionPart(GuiGraphics graphics, int mouseX, int mouseY) {
        drawBackground(graphics);
        drawClusters(graphics);

        focusedClusterMouse = tryFocusCluster(mouseX, mouseY);

        float scaleX = mousePointScaled.getPosX();
        float scaleY = mousePointScaled.getPosY();

        if (sizeHandler.getScalingFactor() >= 6.1f && focusedClusterZoom != null && clusterRenderer != null) {
            JournalCluster cluster = JournalProgressionClusterMapping.getClusterMapping(focusedClusterZoom);
            drawClusterBackground(graphics, cluster.clusterBackgroundTexture);
            clusterRenderer.drawClusterScreen(graphics, parentGui);
            scaleX = clusterRenderer.getMouseX();
            scaleY = clusterRenderer.getMouseY();
        }

        if (focusedClusterMouse != null) {
            JournalCluster cluster = JournalProgressionClusterMapping.getClusterMapping(focusedClusterMouse);
            float width  = sizeHandler.scaledDistanceX(cluster.x, cluster.maxX);
            float height = sizeHandler.scaledDistanceY(cluster.y, cluster.maxY);
            Point.Float offset = sizeHandler.scalePointToGui(parentGui, mousePointScaled,
                    new Point.Float(cluster.x, cluster.y));

            float scale = sizeHandler.getScalingFactor();
            float br = scale > 8.01f ? 0f : scale >= 6f ? 1f - (scale - 6f) / 2f : 1f;
            int alpha = Math.max(5, (int)(0xCC * br));
            int color = 0x5A28FF | (alpha << 24);

            String name = focusedClusterMouse.getName().getString();
            int nameWidth = Minecraft.getInstance().font.width(name);
            graphics.pose().pushPose();
            graphics.pose().translate(offset.x + width / 2f - nameWidth * 1.4f / 2f, offset.y + height / 3f, 0);
            graphics.pose().scale(1.4f, 1.4f, 1f);
            graphics.drawString(Minecraft.getInstance().font, name, 0, 0, color, true);
            graphics.pose().popPose();
        }

        drawStarParallaxLayers(graphics, scaleX, scaleY);
    }

    @Nullable
    private ResearchProgression tryFocusCluster(double mouseX, double mouseY) {
        for (Map.Entry<Rectangle, ResearchProgression> entry : clusterRectMap.entrySet()) {
            if (entry.getKey().contains(mouseX, mouseY)) return entry.getValue();
        }
        return null;
    }

    private void drawClusters(GuiGraphics graphics) {
        clusterRectMap.clear();
        if (sizeHandler.getScalingFactor() >= 8.01f) return;
        PlayerProgress progress = ResearchHelper.getClientProgress();
        for (ResearchProgression prog : progress.getResearchProgression()) {
            renderCluster(graphics, prog, JournalProgressionClusterMapping.getClusterMapping(prog));
        }
    }

    private void renderCluster(GuiGraphics graphics, ResearchProgression p, JournalCluster cluster) {
        Point.Float pCluster = sizeHandler.scalePointToGui(parentGui, mousePointScaled,
                new Point.Float(cluster.x, cluster.y));
        float width  = sizeHandler.scaledDistanceX(cluster.x, cluster.maxX);
        float height = sizeHandler.scaledDistanceY(cluster.y, cluster.maxY);

        clusterRectMap.put(new Rectangle(
                Mth.floor(pCluster.x), Mth.floor(pCluster.y),
                Mth.floor(width), Mth.floor(height)), p);

        float scale = sizeHandler.getScalingFactor();
        float br = scale > 8.01f ? 0f : scale >= 6f ? 1f - (scale - 6f) / 2f : 1f;

        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.setShaderTexture(0, cluster.cloudTexture.getKey());
        Blending.ADDITIVEDARK.apply();
        RenderingUtils.drawColorTex(matrix, pCluster.x, pCluster.y, width, height, br, br, br, br);
        RenderSystem.disableBlend();
    }

    private void drawClusterBackground(GuiGraphics graphics, AbstractRenderableTexture tex) {
        float scale = sizeHandler.getScalingFactor();
        float br = scale > 8.01f ? 0.75f : scale >= 6f ? ((scale - 6f) / 2f) * 0.75f : 0f;
        if (br <= 0f) return;

        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.setShaderTexture(0, tex.getKey());
        Blending.ADDITIVEDARK.apply();
        RenderingUtils.drawColorTex(matrix,
                realCoordLowerX, realCoordLowerY, realRenderWidth, realRenderHeight,
                br, br, br, br);
        RenderSystem.disableBlend();
    }

    private void drawBackground(GuiGraphics graphics) {
        float br = 0.35f;
        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.setShaderTexture(0, TexturesAS.TEX_GUI_BACKGROUND_DEFAULT.getKey());
        RenderingUtils.drawColorTex(matrix,
                realCoordLowerX, realCoordLowerY, realRenderWidth, realRenderHeight,
                br, br, br, 1f);
    }

    @SuppressWarnings("null")
    private void drawStarParallaxLayers(GuiGraphics graphics, float scalePosX, float scalePosY) {
        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.setShaderTexture(0, TexturesAS.TEX_GUI_STARFIELD_OVERLAY.getKey());
        Blending.OVERLAYDARK.apply();

        float ox = scalePosX / 2000f;
        float oy = scalePosY / 1000f;

        float x = parentGui.getGuiLeft(), y = parentGui.getGuiTop();
        float w = parentGui.getGuiWidth(), h = parentGui.getGuiHeight();
        float sc = sizeHandler.getScalingFactor() / 40f;

        for (float factor : new float[]{2f, 1.5f, 1f, 0.75f, 0.5f, 0.3f}) {
            float u  = 0.2f + ox + factor + sc;
            float v  = 0.2f + oy + factor + sc;
            float uL = 0.6f * factor - sc * 2f;
            float vL = 0.6f * factor - sc * 2f;
            if (uL <= 0 || vL <= 0) continue;
            RenderingUtils.drawColorTex(matrix, x, y, w, h, 0.75f, 0.75f, 0.75f, 0.7f, u, v, u + uL, v + vL);
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
