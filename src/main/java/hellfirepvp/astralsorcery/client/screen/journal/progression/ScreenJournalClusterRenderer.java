package hellfirepvp.astralsorcery.client.screen.journal.progression;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.client.screen.base.WidthHeightScreen;
import hellfirepvp.astralsorcery.client.screen.helper.ScalingPoint;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPages;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the zoomed-in view of a single research cluster — draws nodes,
 * node backgrounds (item or sprite), and animated connections between them.
 *
 * <p>1.16 → 1.20: MatrixStack → GuiGraphics; IVertexBuilder → BufferBuilder via
 * RenderingUtils.drawTexQuads; ITextProperties → Component;
 * displayGuiScreen → setScreen; MathHelper → Mth;
 * RenderHelper.enableStandardItemLighting removed (no-op in 1.20 GUI).</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenJournalClusterRenderer {

    private ProgressionSizeHandler progressionSizeHandler;
    private final ResearchProgression progression;
    private ScalingPoint mousePointScaled;
    private ScalingPoint previousMousePointScaled;

    private int renderOffsetX, renderOffsetY;
    private int renderGuiHeight, renderGuiWidth;
    private boolean hasPrevOffset = false;

    private float alpha = 1f;
    private final Map<Rectangle, ResearchNode> clickableNodes = new HashMap<>();

    public ScreenJournalClusterRenderer(ResearchProgression progression,
                                        int guiHeight, int guiWidth,
                                        int guiLeft, int guiTop) {
        this.progression = progression;
        this.progressionSizeHandler = new ProgressionSizeHandler(progression);
        this.progressionSizeHandler.setMaxScale(1.2f);
        this.progressionSizeHandler.setMinScale(0.1f);
        this.progressionSizeHandler.setScaleSpeed(0.9f / 20f);
        this.progressionSizeHandler.updateSize();
        this.progressionSizeHandler.forceScaleTo(0.1f);

        this.mousePointScaled = ScalingPoint.createPoint(0, 0,
                this.progressionSizeHandler.getScalingFactor(), false);
        this.centerMouse();
        this.applyMovedMouseOffset();

        this.renderOffsetX  = guiLeft;
        this.renderOffsetY  = guiTop;
        this.renderGuiHeight = guiHeight;
        this.renderGuiWidth  = guiWidth;
    }

    public boolean propagateClick(ScreenJournalProgression parent, double mouseX, double mouseY) {
        Rectangle frame = new Rectangle(renderOffsetX, renderOffsetY, renderGuiWidth, renderGuiHeight);
        if (frame.contains(mouseX, mouseY)) {
            for (Map.Entry<Rectangle, ResearchNode> entry : clickableNodes.entrySet()) {
                if (entry.getKey().contains(mouseX, mouseY)) {
                    Minecraft.getInstance().setScreen(new ScreenJournalPages(parent, entry.getValue()));
                    return true;
                }
            }
        }
        return false;
    }

    public void drawMouseHighlight(GuiGraphics graphics, int mouseX, int mouseY) {
        Rectangle frame = new Rectangle(renderOffsetX, renderOffsetY, renderGuiWidth, renderGuiHeight);
        if (!frame.contains(mouseX, mouseY)) return;

        for (Map.Entry<Rectangle, ResearchNode> entry : clickableNodes.entrySet()) {
            if (entry.getKey().contains(mouseX, mouseY)) {
                Component name = entry.getValue().getName();
                List<Component> lines = Lists.newArrayList(name);
                graphics.renderComponentTooltip(Minecraft.getInstance().font, lines, mouseX, mouseY);
            }
        }
    }

    public void centerMouse() {
        Point.Float center = this.progressionSizeHandler.getRelativeCenter();
        this.moveMouse(center.x, center.y);
    }

    public void moveMouse(float changedX, float changedY) {
        if (hasPrevOffset) {
            mousePointScaled.updateScaledPos(
                    progressionSizeHandler.clampX(previousMousePointScaled.getScaledPosX() + changedX),
                    progressionSizeHandler.clampY(previousMousePointScaled.getScaledPosY() + changedY),
                    progressionSizeHandler.getScalingFactor());
        } else {
            mousePointScaled.updateScaledPos(
                    progressionSizeHandler.clampX(changedX),
                    progressionSizeHandler.clampY(changedY),
                    progressionSizeHandler.getScalingFactor());
        }
    }

    public void applyMovedMouseOffset() {
        this.previousMousePointScaled = ScalingPoint.createPoint(
                mousePointScaled.getScaledPosX(),
                mousePointScaled.getScaledPosY(),
                progressionSizeHandler.getScalingFactor(),
                true);
        this.hasPrevOffset = true;
    }

    public void handleZoomOut() {
        this.progressionSizeHandler.handleZoomOut();
        rescale(progressionSizeHandler.getScalingFactor());
    }

    public void handleZoomIn() {
        this.progressionSizeHandler.handleZoomIn();
        rescale(progressionSizeHandler.getScalingFactor());
    }

    public float getMouseX() { return mousePointScaled.getPosX(); }
    public float getMouseY() { return mousePointScaled.getPosY(); }

    private void rescale(float newScale) {
        this.mousePointScaled.rescale(newScale);
        if (this.previousMousePointScaled != null) {
            this.previousMousePointScaled.rescale(newScale);
        }
        moveMouse(0, 0);
    }

    public void drawClusterScreen(GuiGraphics graphics, WidthHeightScreen parentGui) {
        clickableNodes.clear();
        drawNodesAndConnections(graphics, parentGui);
    }

    private void drawNodesAndConnections(GuiGraphics graphics, WidthHeightScreen parentGui) {
        alpha = Mth.clamp((progressionSizeHandler.getScalingFactor() - 0.25f) / 0.75f, 0f, 1f);

        Map<ResearchNode, Point.Float> displayPositions = new HashMap<>();
        for (ResearchNode node : progression.getResearchNodes()) {
            if (!node.canSee(ResearchHelper.getClientProgress())) continue;

            Point.Float from = this.progressionSizeHandler.scalePointToGui(
                    parentGui, this.mousePointScaled,
                    new Point.Float(node.renderPosX, node.renderPosZ));

            for (ResearchNode target : node.getConnectionsTo()) {
                Point.Float to = this.progressionSizeHandler.scalePointToGui(
                        parentGui, this.mousePointScaled,
                        new Point.Float(target.renderPosX, target.renderPosZ));
                drawConnection(graphics, from.x, from.y, to.x, to.y);
            }
            displayPositions.put(node, from);
        }
        displayPositions.forEach((node, pos) -> renderNodeToGUI(graphics, node, pos, parentGui));
    }

    private void renderNodeToGUI(GuiGraphics graphics, ResearchNode node,
                                  Point.Float offset, WidthHeightScreen parentGui) {
        float zoomedWH = progressionSizeHandler.getZoomedWHNode();
        float offsetX  = offset.x - zoomedWH / 2f;
        float offsetY  = offset.y - zoomedWH / 2f;

        // Draw background frame
        RenderSystem.setShaderColor(alpha, alpha, alpha, alpha);
        RenderingDrawUtils.drawTexturedRect(graphics,
                node.getBackgroundTexture().resolve().getKey(),
                (int) offsetX, (int) offsetY, (int) zoomedWH, (int) zoomedWH);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (progressionSizeHandler.getScalingFactor() >= 0.7f) {
            clickableNodes.put(new Rectangle(
                    Mth.floor(offsetX), Mth.floor(offsetY),
                    Mth.floor(zoomedWH), Mth.floor(zoomedWH)), node);
        }

        switch (node.getNodeRenderType()) {
            case ITEMSTACK -> {
                graphics.pose().pushPose();
                graphics.pose().translate(offsetX + 2, offsetY + 2, 0);
                graphics.pose().scale(progressionSizeHandler.getScalingFactor() * 0.75f,
                        progressionSizeHandler.getScalingFactor() * 0.75f, 1f);
                graphics.renderItem(node.getRenderItemStack(ClientScheduler.getClientTick()), 0, 0);
                graphics.pose().popPose();
            }
            case TEXTURE_SPRITE -> {
                Color col = node.getTextureColorHint();
                float r = (col.getRed()   / 255f) * alpha;
                float g = (col.getGreen() / 255f) * alpha;
                float b = (col.getBlue()  / 255f) * alpha;
                float a = (col.getAlpha() / 255f) * alpha;

                SpriteSheetResource res = node.getSpriteTexture().resolveSprite();
                Tuple<Float, Float> uv = res.getUVOffset(ClientScheduler.getClientTick());
                float pxWH = zoomedWH / 16f;

                RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
                RenderSystem.setShaderTexture(0, res.getResource().getKey());
                Blending.DEFAULT.apply();
                Matrix4f matrix = graphics.pose().last().pose();
                RenderingUtils.drawColorTex(matrix,
                        offsetX + pxWH, offsetY + pxWH,
                        zoomedWH - 2 * pxWH, zoomedWH - 2 * pxWH,
                        r, g, b, a,
                        uv.getA(), uv.getB(),
                        uv.getA() + res.getULength(), uv.getB() + res.getVLength());
                RenderSystem.disableBlend();
            }
            default -> { }
        }
    }

    private void drawConnection(GuiGraphics graphics,
                                float originX, float originY,
                                float targetX, float targetY) {
        long clientTicks = ClientScheduler.getClientTick();
        Vector3 origin = new Vector3(originX, originY, 0);
        Vector3 line   = origin.vectorFromHereTo(targetX, targetY, 0);
        int segments   = Math.max(1, (int) Math.ceil(line.length()));
        int activeSegment = (int) (clientTicks % segments);
        Vector3 segmentIter = line.divide(segments);

        for (int i = segments; i >= 0; i--) {
            float x1 = (float) origin.getX();
            float y1 = (float) origin.getY();
            origin.add(segmentIter);
            float x2 = (float) origin.getX();
            float y2 = (float) origin.getY();

            float brightness = 0.6f + 0.4f * evaluateBrightness(i, activeSegment);
            float c = Mth.clamp(brightness * alpha, 0f, 1f);
            float a = Mth.clamp(0.4f * alpha, 0f, 1f);
            // Draw as thin quad (2 px wide)
            RenderingDrawUtils.drawLine(graphics, graphics.pose(), x1, y1, x2, y2, 2f,
                    new Color(c, c, c, a));
        }
    }

    private float evaluateBrightness(int segment, int activeSegment) {
        if (segment == activeSegment) return 1f;
        return Math.max(0f, (10f - Math.abs(activeSegment - segment)) / 10f);
    }
}
