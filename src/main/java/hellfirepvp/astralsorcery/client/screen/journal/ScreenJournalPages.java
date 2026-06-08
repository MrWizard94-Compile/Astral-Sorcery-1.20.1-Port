package hellfirepvp.astralsorcery.client.screen.journal;

import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.NavigationArrowScreen;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import hellfirepvp.astralsorcery.common.data.journal.RenderablePage;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows the pages of a single {@link ResearchNode} in the journal's book layout.
 * Supports navigation arrows for multi-page entries.
 *
 * <p>1.16 → 1.20: MatrixStack → GuiGraphics; displayGuiScreen → setScreen;
 * font.getStringPropertyWidth → font.width; closeScreen → onClose.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenJournalPages extends ScreenJournal implements NavigationArrowScreen {

    private static ScreenJournalPages openGuiInstance;
    private static boolean saveSite = true;

    @Nullable private final ScreenJournalProgression origin;
    @Nullable private final Screen previous;

    private final ResearchNode researchNode;
    private final List<RenderablePage> pages;

    private boolean informPreviousClose = true;
    private int currentPageOffset = 0;

    private Rectangle rectBack, rectNext, rectPrev;

    public ScreenJournalPages(@Nullable ScreenJournalProgression origin, ResearchNode node) {
        super(node.getName(), NO_BOOKMARK);
        this.researchNode = node;
        this.origin = origin;
        this.previous = null;
        this.pages = buildPages(node);
    }

    public ScreenJournalPages(@Nullable Screen previous, ResearchNode node, int exactPage) {
        super(node.getName(), NO_BOOKMARK);
        this.researchNode = node;
        this.origin = null;
        this.previous = previous;
        this.currentPageOffset = exactPage / 2;
        this.pages = buildPages(node);
    }

    private static List<RenderablePage> buildPages(ResearchNode node) {
        List<JournalPage> rawPages = node.getPages();
        List<RenderablePage> rendered = new ArrayList<>(rawPages.size());
        for (int i = 0; i < rawPages.size(); i++) {
            rendered.add(rawPages.get(i).buildRenderPage(node, i));
        }
        return rendered;
    }

    @Nullable
    public static ScreenJournalPages getClearOpenGuiInstance() {
        ScreenJournalPages gui = openGuiInstance;
        openGuiInstance = null;
        return gui;
    }

    public int getCurrentPageOffset()  { return currentPageOffset; }
    public ResearchNode getResearchNode() { return researchNode; }

    @Override
    protected void init() {
        super.init();
        ScreenJournalProgression localOrigin = origin;
        if (localOrigin != null) {
            localOrigin.preventRefresh();
            localOrigin.width  = width;
            localOrigin.height = height;
            localOrigin.init();
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        super.render(graphics, mouseX, mouseY, pTicks);

        if (origin != null) {
            drawDefault(graphics, TexturesAS.TEX_GUI_BOOK_BLANK, mouseX, mouseY);
        } else {
            drawWHRect(graphics, TexturesAS.TEX_GUI_BOOK_BLANK);
        }

        int pageYOffset = 20;

        // Headline on first spread
        if (this.currentPageOffset == 0) {
            Component title = this.getTitle();
            int titleWidth = Minecraft.getInstance().font.width(title);
            graphics.pose().pushPose();
            graphics.pose().translate(guiLeft + 117 - titleWidth * 1.3f / 2f, guiTop + 22, 0);
            graphics.pose().scale(1.3f, 1.3f, 1f);
            graphics.drawString(Minecraft.getInstance().font, title, 0, 0, 0x00DDDDDD, false);
            graphics.pose().popPose();

            // Underline
            RenderingDrawUtils.drawTexturedRect(graphics, TexturesAS.TEX_GUI_BOOK_UNDERLINE.getKey(),
                    guiLeft + 30, guiTop + 35, 175, 6);
            pageYOffset += 30;
        }

        // Left page
        int index = currentPageOffset * 2;
        if (index < pages.size()) {
            pages.get(index).render(graphics, guiLeft + 30, guiTop + pageYOffset, pTicks, mouseX, mouseY);
        }
        // Right page
        int rightIndex = index + 1;
        if (rightIndex < pages.size()) {
            pages.get(rightIndex).render(graphics, guiLeft + 220, guiTop + 20, pTicks, mouseX, mouseY);
        }

        // Navigation arrows
        this.rectBack = null;
        this.rectPrev = null;
        this.rectNext = null;
        this.rectBack = drawArrow(graphics, guiLeft + 197, guiTop + 230, NavigationArrowScreen.Type.LEFT, mouseX, mouseY, pTicks);
        if (index > 0) {
            this.rectPrev = drawArrow(graphics, guiLeft + 25, guiTop + 220, NavigationArrowScreen.Type.LEFT, mouseX, mouseY, pTicks);
        }
        if (pages.size() >= index + 3) {
            this.rectNext = drawArrow(graphics, guiLeft + 367, guiTop + 220, NavigationArrowScreen.Type.RIGHT, mouseX, mouseY, pTicks);
        }

        // Post-render (tooltips, overlays)
        if (index < pages.size()) {
            pages.get(index).postRender(graphics, guiLeft + 30, guiTop + pageYOffset, pTicks, mouseX, mouseY);
        }
        if (rightIndex < pages.size()) {
            pages.get(rightIndex).postRender(graphics, guiLeft + 220, guiTop + 20, pTicks, mouseX, mouseY);
        }
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double mouseX, double mouseY) {
        if (origin != null) {
            origin.expectReInit();
            saveSite = false;
        } else {
            informPreviousClose = false;
        }
        return true;
    }

    @Override
    public void onClose() {
        if (origin != null) {
            if (saveSite) {
                openGuiInstance = this;
                ScreenJournalProgression.getJournalInstance().preventRefresh();
                Minecraft.getInstance().setScreen(null);
            } else {
                saveSite = true;
                openGuiInstance = null;
                Minecraft.getInstance().setScreen(origin);
            }
        } else {
            if (previous != null && informPreviousClose) {
                previous.onClose();
            }
            Minecraft.getInstance().setScreen(previous);
        }
    }

    @Override
    protected void mouseDragTick(double mouseX, double mouseY, double mouseDiffX, double mouseDiffY,
                                  double mouseOffsetX, double mouseOffsetY) {
        int idx = currentPageOffset * 2;
        if (idx < pages.size() && pages.get(idx).propagateMouseDrag(mouseOffsetX, mouseOffsetY)) return;
        if (idx + 1 < pages.size()) pages.get(idx + 1).propagateMouseDrag(mouseOffsetX, mouseOffsetY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 1) return true;
        if (button != 0) return false;

        if (origin != null && handleBookmarkClick(mouseX, mouseY)) {
            saveSite = false;
            return true;
        }

        if (rectBack != null && rectBack.contains(mouseX, mouseY)) {
            if (origin != null) {
                origin.expectReInit();
                saveSite = false;
            } else {
                informPreviousClose = false;
            }
            this.onClose();
            return true;
        }
        if (rectPrev != null && rectPrev.contains(mouseX, mouseY)) {
            this.currentPageOffset--;
            return true;
        }
        if (rectNext != null && rectNext.contains(mouseX, mouseY)) {
            this.currentPageOffset++;
            return true;
        }

        int idx = currentPageOffset * 2;
        if (idx < pages.size() && pages.get(idx).propagateMouseClick(mouseX, mouseY)) return true;
        if (idx + 1 < pages.size() && pages.get(idx + 1).propagateMouseClick(mouseX, mouseY)) return true;
        return false;
    }
}
