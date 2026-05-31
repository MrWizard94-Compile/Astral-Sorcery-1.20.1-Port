package hellfirepvp.astralsorcery.client.screen.journal;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.helper.ScalingPoint;
import hellfirepvp.astralsorcery.client.screen.helper.ScreenRenderBoundingBox;
import hellfirepvp.astralsorcery.client.screen.helper.SizeHandler;
import hellfirepvp.astralsorcery.client.screen.journal.perk.PerkTreeSizeHandler;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.ScreenTextEntry;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Perk tree journal screen — shows the perk constellation map with pan/zoom
 * in the journal book frame with bookmark tabs.
 *
 * <p>This is a simplified port of 1.16's ScreenJournalPerkTree. Complex features
 * like gem sockets, seal dragging, and batch sprite rendering will be added
 * incrementally as their dependencies are ported.</p>
 *
 * <p>1.16 → 1.20: MatrixStack → GuiGraphics; GL11 scissor → RenderSystem.enableScissor.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenJournalPerkTree extends ScreenJournal {

    public boolean expectReinit = false;

    private SizeHandler sizeHandler;
    private ScreenRenderBoundingBox guiBox;
    private ScalingPoint mousePosition;
    private ScalingPoint previousMousePosition;

    private final Map<AbstractPerk, Rectangle.Float> thisFramePerks = new HashMap<>();
    private final ScreenTextEntry searchTextEntry = new ScreenTextEntry();

    private int guiOffsetX, guiOffsetY;

    public ScreenJournalPerkTree() {
        super(Component.translatable("screen.astralsorcery.tome.perks"), 30);
        this.closeWithInventoryKey = false;
        this.searchTextEntry.setChangeCallback(this::updateSearchHighlight);
    }

    @Override
    protected void init() {
        super.init();
        if (expectReinit) {
            expectReinit = false;
            return;
        }

        this.guiOffsetX = guiLeft + 10;
        this.guiOffsetY = guiTop + 10;
        this.guiBox = new ScreenRenderBoundingBox(10, 10, guiWidth - 10, guiHeight - 10);

        if (sizeHandler == null) {
            sizeHandler = new PerkTreeSizeHandler();
            sizeHandler.setScaleSpeed(0.04f);
            sizeHandler.setMaxScale(1f);
            sizeHandler.setMinScale(0.1f);
            sizeHandler.updateSize();
            mousePosition = ScalingPoint.createPoint(0, 0, sizeHandler.getScalingFactor(), false);
        }

        // Center on attuned root perk or middle of tree
        PlayerProgress progress = ResearchHelper.getClientProgress();
        boolean shifted = false;
        if (progress.getAttunedConstellation() != null) {
            ResourceLocation attunedKey = progress.getAttunedConstellation();
            for (AbstractPerk perk : PerkTree.getAllPerks()) {
                if (perk instanceof hellfirepvp.astralsorcery.common.perk.node.RootPerk root) {
                    ResourceLocation rootCst = root.getRequiredConstellation();
                    if (attunedKey.equals(rootCst)) {
                        Point.Float shift = sizeHandler.evRelativePos(new Point.Float(root.getX(), root.getY()));
                        moveMouse(Mth.floor(shift.x), Mth.floor(shift.y));
                        shifted = true;
                        break;
                    }
                }
            }
        }
        if (!shifted) {
            moveMouse(Mth.floor(sizeHandler.getTotalWidth() / 2f), Mth.floor(sizeHandler.getTotalHeight() / 2f));
        }
        applyMovedMouseOffset();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        super.render(graphics, mouseX, mouseY, pTicks);

        thisFramePerks.clear();

        // Scissor to inner book area.
        // GuiGraphics.enableScissor takes two corners in GUI space and handles y-flip internally.
        graphics.enableScissor(guiLeft + 39, guiTop + 44, guiLeft + guiWidth - 37, guiTop + guiHeight - 27);

        drawBackground(graphics);
        drawPerkNodes(graphics, pTicks);

        graphics.disableScissor();

        drawDefault(graphics, TexturesAS.TEX_GUI_BOOK_FRAME_FULL, mouseX, mouseY);
        drawSearchBox(graphics);

        drawPerkTooltip(graphics, mouseX, mouseY);
    }

    private void drawBackground(GuiGraphics graphics) {
        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.setShaderTexture(0, TexturesAS.TEX_GUI_BACKGROUND_PERKS.getKey());
        RenderingUtils.drawColorTex(matrix,
                guiLeft - 10, guiTop - 10, guiWidth + 20, guiHeight + 20,
                0.5f, 0.5f, 0.5f, 1f);
    }

    private void drawPerkNodes(GuiGraphics graphics, float pTicks) {
        PlayerProgress progress = ResearchHelper.getClientProgress();

        for (AbstractPerk perk : PerkTree.getAllPerks()) {
            if (!perk.isEnabled()) continue;

            Point.Float offset = sizeHandler.scalePointToGui(this, mousePosition,
                    new Point.Float(perk.getX(), perk.getY()));
            float sz = sizeHandler.getZoomedWHNode();
            float x = offset.x - sz / 2f;
            float y = offset.y - sz / 2f;

            if (!guiBox.isInBox(x - guiLeft, y - guiTop)) continue;

            boolean allocated = progress.hasPerkAllocated(perk.getKey());

            float r, g, b;
            if (allocated) {
                r = 0.6f; g = 0.8f; b = 1.0f;
            } else {
                r = 0.4f; g = 0.4f; b = 0.5f;
            }

            RenderingDrawUtils.drawRect(graphics, (int) x, (int) y, (int) sz, (int) sz,
                    new Color(r, g, b, 0.8f));
            thisFramePerks.put(perk, new Rectangle.Float(x, y, sz, sz));
        }
    }

    private void drawSearchBox(GuiGraphics graphics) {
        RenderingDrawUtils.drawTexturedRect(graphics, TexturesAS.TEX_GUI_TEXT_FIELD.getKey(),
                guiLeft + 300, guiTop + 16, 89, 15);
        String text = searchTextEntry.getText();
        if ((hellfirepvp.astralsorcery.client.ClientScheduler.getClientTick() % 20) > 10) text += "_";
        graphics.drawString(Minecraft.getInstance().font, text, guiLeft + 304, guiTop + 20, 0xCCCCCC, false);
    }

    @SuppressWarnings("null")
    private void drawPerkTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (Map.Entry<AbstractPerk, Rectangle.Float> entry : thisFramePerks.entrySet()) {
            if (entry.getValue().contains(mouseX, mouseY)) {
                AbstractPerk perk = entry.getKey();
                List<Component> lines = new ArrayList<>();
                lines.add(perk.getName());
                lines.addAll(perk.getTooltipDescription());
                graphics.renderComponentTooltip(Minecraft.getInstance().font, lines, mouseX, mouseY);
                break;
            }
        }
    }

    private void updateSearchHighlight() {
        // Search highlight placeholder — full implementation deferred
    }

    private void moveMouse(float x, float y) {
        if (previousMousePosition != null) {
            mousePosition.updateScaledPos(
                    sizeHandler.clampX(previousMousePosition.getScaledPosX() + x),
                    sizeHandler.clampY(previousMousePosition.getScaledPosY() + y),
                    sizeHandler.getScalingFactor());
        } else {
            mousePosition.updateScaledPos(
                    sizeHandler.clampX(x), sizeHandler.clampY(y),
                    sizeHandler.getScalingFactor());
        }
    }

    private void applyMovedMouseOffset() {
        previousMousePosition = ScalingPoint.createPoint(
                mousePosition.getScaledPosX(), mousePosition.getScaledPosY(),
                sizeHandler.getScalingFactor(), true);
    }

    @Override
    protected void mouseDragTick(double mouseX, double mouseY, double diffX, double diffY,
                                  double offX, double offY) {
        moveMouse((float) diffX, (float) diffY);
    }

    @Override
    protected void mouseDragStop(double mouseX, double mouseY, double diffX, double diffY) {
        applyMovedMouseOffset();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (scroll < 0) { sizeHandler.handleZoomOut(); rescaleMouse(); return true; }
        if (scroll > 0) { sizeHandler.handleZoomIn();  rescaleMouse(); return true; }
        return false;
    }

    private void rescaleMouse() {
        mousePosition.rescale(sizeHandler.getScalingFactor());
        if (previousMousePosition != null) previousMousePosition.rescale(sizeHandler.getScalingFactor());
        moveMouse(0, 0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0 && handleBookmarkClick(mouseX, mouseY)) return true;
        return false;
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double mouseX, double mouseY) {
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (searchTextEntry.keyTyped(key)) return true;
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char ch, int modifiers) {
        if (searchTextEntry.charTyped(ch)) return true;
        return super.charTyped(ch, modifiers);
    }
}
