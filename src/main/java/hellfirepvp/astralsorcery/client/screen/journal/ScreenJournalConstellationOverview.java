package hellfirepvp.astralsorcery.client.screen.journal;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.NavigationArrowScreen;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Journal screen showing all seen constellations in a grid,
 * navigable by page arrows. Clicking a constellation opens its detail view.
 *
 * <p>1.16 → 1.20: MatrixStack → GuiGraphics; displayGuiScreen → setScreen;
 * MathHelper → Mth; GL11 vertex drawing → RenderingUtils.drawColorTex;
 * getSeenConstellations → getDiscoveredConstellations.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenJournalConstellationOverview extends ScreenJournal implements NavigationArrowScreen {

    private static final int CONSTELLATIONS_PER_PAGE = 4;
    private static final int CST_WIDTH  = 80;
    private static final int CST_HEIGHT = 110;

    private static final Map<Integer, Point> OFFSET_MAP = new HashMap<>();
    static {
        OFFSET_MAP.put(0, new Point(45, 55));
        OFFSET_MAP.put(1, new Point(125, 105));
        OFFSET_MAP.put(2, new Point(200, 45));
        OFFSET_MAP.put(3, new Point(280, 110));
    }

    private final List<IConstellation> constellations;
    private final int pageId;

    private final Map<Rectangle, IConstellation> rectCRenderMap = new HashMap<>();
    private Rectangle rectPrev, rectNext;

    private ScreenJournalConstellationOverview(int pageId, List<IConstellation> constellations) {
        super(Component.translatable("screen.astralsorcery.tome.constellations"), 20);
        this.constellations = constellations;
        this.pageId = pageId;
    }

    public static ScreenJournal getConstellationScreen() {
        PlayerProgress client = ResearchHelper.getClientProgress();
        List<IConstellation> visible = client.getDiscoveredConstellations()
                .stream()
                .map(ConstellationRegistry::getConstellation)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new ScreenJournalConstellationOverview(0, visible);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        super.render(graphics, mouseX, mouseY, pTicks);
        drawConstellationBackground(graphics);
        drawDefault(graphics, TexturesAS.TEX_GUI_BOOK_FRAME_FULL, mouseX, mouseY);
        drawNavArrows(graphics, pTicks, mouseX, mouseY);
        drawConstellations(graphics, pTicks, mouseX, mouseY);
    }

    private void drawConstellationBackground(GuiGraphics graphics) {
        Matrix4f matrix = graphics.pose().last().pose();

        // Solid black base
        RenderSystem.setShaderTexture(0, TexturesAS.TEX_BLACK.getKey());
        RenderingUtils.drawColorTex(matrix,
                guiLeft + 15, guiTop + 10,
                guiWidth - 30, guiHeight - 20,
                1f, 1f, 1f, 1f);

        // Constellation background overlay
        RenderSystem.setShaderTexture(0, TexturesAS.TEX_GUI_BACKGROUND_CONSTELLATIONS.getKey());
        Blending.DEFAULT.apply();
        RenderingUtils.drawColorTex(matrix,
                guiLeft + 15, guiTop + 10,
                guiWidth - 30, guiHeight - 20,
                0.8f, 0.8f, 1f, 0.7f);
        RenderSystem.disableBlend();
    }

    private void drawConstellations(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        rectCRenderMap.clear();
        int start = pageId * CONSTELLATIONS_PER_PAGE;
        int end   = Math.min(start + CONSTELLATIONS_PER_PAGE, constellations.size());
        List<IConstellation> page = constellations.subList(start, end);

        for (int i = 0; i < page.size(); i++) {
            IConstellation c = page.get(i);
            Point p = OFFSET_MAP.get(i);
            if (p == null) continue;
            Rectangle r = drawConstellation(graphics, c,
                    guiLeft + p.x, guiTop + p.y, partial, mouseX, mouseY);
            rectCRenderMap.put(r, c);
        }
    }

    private Rectangle drawConstellation(GuiGraphics graphics, IConstellation cst,
                                         int offsetX, int offsetY,
                                         float partial, int mouseX, int mouseY) {
        Rectangle rect = new Rectangle(offsetX, offsetY, CST_WIDTH, CST_HEIGHT);
        boolean hovered = rect.contains(mouseX, mouseY);

        graphics.pose().pushPose();
        graphics.pose().translate(offsetX + CST_WIDTH / 2f, offsetY + CST_WIDTH / 2f, 0);
        if (hovered) graphics.pose().scale(1.1f, 1.1f, 1f);
        graphics.pose().translate(-CST_WIDTH / 2f, -CST_WIDTH / 2f, 0);

        long tick = ClientScheduler.getClientTick();
        float brightness = 0.5f + 0.5f * RenderingConstellationUtils.getTwinkleBrightness(0, (int) tick, partial);
        RenderingConstellationUtils.renderConstellationSmall(graphics, cst,
                0, 0, 95, 95, brightness);

        Component name = cst.getConstellationName();
        int nameWidth = Minecraft.getInstance().font.width(name);
        graphics.drawString(Minecraft.getInstance().font, name,
                CST_WIDTH / 2 - nameWidth / 2, 90, 0xBBDDDDDD, false);

        graphics.pose().popPose();
        return rect;
    }

    private void drawNavArrows(GuiGraphics graphics, float pTicks, int mouseX, int mouseY) {
        rectPrev = null;
        rectNext = null;

        int cIndex = pageId * CONSTELLATIONS_PER_PAGE;
        if (cIndex > 0) {
            rectPrev = drawArrow(graphics, guiLeft + 15, guiTop + 127,
                    Type.LEFT, mouseX, mouseY, pTicks);
        }
        if (constellations.size() >= cIndex + CONSTELLATIONS_PER_PAGE + 1) {
            rectNext = drawArrow(graphics, guiLeft + 367, guiTop + 127,
                    Type.RIGHT, mouseX, mouseY, pTicks);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;
        if (handleBookmarkClick(mouseX, mouseY)) return true;

        for (Map.Entry<Rectangle, IConstellation> entry : rectCRenderMap.entrySet()) {
            if (entry.getKey().contains(mouseX, mouseY)) {
                Minecraft.getInstance().setScreen(
                        new ScreenJournalConstellationDetail(this, entry.getValue()));
                return true;
            }
        }
        if (rectPrev != null && rectPrev.contains(mouseX, mouseY)) {
            Minecraft.getInstance().setScreen(
                    new ScreenJournalConstellationOverview(pageId - 1, constellations));
            return true;
        }
        if (rectNext != null && rectNext.contains(mouseX, mouseY)) {
            Minecraft.getInstance().setScreen(
                    new ScreenJournalConstellationOverview(pageId + 1, constellations));
            return true;
        }
        return false;
    }
}
