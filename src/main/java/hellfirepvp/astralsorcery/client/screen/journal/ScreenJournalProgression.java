package hellfirepvp.astralsorcery.client.screen.journal;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.journal.progression.ScreenJournalProgressionRenderer;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.ScreenTextEntry;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * The main journal screen — shows the galaxy progression view with zoom/pan,
 * plus a search box that switches to a paginated research node list.
 *
 * <p>1.16 → 1.20: MatrixStack → GuiGraphics; GL11 scissor → RenderSystem.enableScissor;
 * font.getStringPropertyWidth → font.width; font.trimStringToWidth → font.split;
 * IReorderingProcessor → FormattedCharSequence; displayGuiScreen → setScreen.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenJournalProgression extends ScreenJournal {

    private static ScreenJournalProgression currentInstance = null;
    private boolean expectReinit = false;
    private boolean rescaleAndRefresh = true;

    private final ScreenTextEntry searchTextEntry = new ScreenTextEntry();

    private static final int SEARCH_ENTRIES_LEFT  = 15;
    private static final int SEARCH_ENTRIES_RIGHT = 14;
    private static final int SEARCH_ENTRY_WIDTH   = 170;

    private int searchPageOffset = 0;
    private Rectangle searchPrevRct, searchNextRct;
    private ResearchNode searchHoverNode = null;
    private final List<ResearchNode> searchResult = new ArrayList<>();
    private final Map<Integer, List<ResearchNode>> searchResultPageIndex = new HashMap<>();

    private static ScreenJournalProgressionRenderer progressionRenderer;

    private ScreenJournalProgression() {
        super(Component.translatable("screen.astralsorcery.tome.progression"), 10);
        this.searchTextEntry.setChangeCallback(this::onSearchTextInput);
    }

    public static ScreenJournalProgression getJournalInstance() {
        return currentInstance != null ? currentInstance : new ScreenJournalProgression();
    }

    public static ScreenJournal getOpenJournalInstance() {
        ScreenJournalPages pages = ScreenJournalPages.getClearOpenGuiInstance();
        return pages != null ? pages : getJournalInstance();
    }

    public void expectReInit()    { this.expectReinit = true; }
    public void preventRefresh()  { this.rescaleAndRefresh = false; }

    public static void resetJournal() {
        currentInstance = null;
        ScreenJournalPages.getClearOpenGuiInstance();
    }

    @Override
    protected void onBookmarkNavigate() {
        resetJournal();
    }

    @Override
    public void onClose() {
        super.onClose();
        rescaleAndRefresh = false;
    }

    @Override
    protected void init() {
        super.init();
        if (expectReinit) {
            expectReinit = false;
            return;
        }
        if (currentInstance == null || progressionRenderer == null) {
            currentInstance = this;
            progressionRenderer = new ScreenJournalProgressionRenderer(currentInstance);
            progressionRenderer.centerMouse();
        }
        progressionRenderer.updateOffset(guiLeft + 10, guiTop + 10);
        progressionRenderer.setBox(10, 10, guiWidth - 10, guiHeight - 10);

        if (rescaleAndRefresh) {
            progressionRenderer.resetZoom();
            progressionRenderer.unfocus();
            progressionRenderer.refreshSize();
            progressionRenderer.updateMouseState();
        } else {
            rescaleAndRefresh = true;
        }
    }

    private boolean inProgressView() {
        return this.searchTextEntry.getText().length() < 3;
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        super.render(graphics, mouseX, mouseY, pTicks);

        this.searchPrevRct = null;
        this.searchNextRct = null;
        this.searchHoverNode = null;

        if (inProgressView()) {
            this.searchPageOffset = 0;
            renderProgressView(graphics, mouseX, mouseY, pTicks);
        } else {
            renderSearchView(graphics, mouseX, mouseY, pTicks);
        }
    }

    private void renderSearchView(GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        drawDefault(graphics, TexturesAS.TEX_GUI_BOOK_BLANK, mouseX, mouseY);
        drawSearchResults(graphics, mouseX, mouseY);
        drawSearchBox(graphics);
        drawSearchPageNavArrows(graphics, mouseX, mouseY, pTicks);
    }

    private void renderProgressView(GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        // Scissor to the book's inner area
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        int sx = (int) Math.floor((guiLeft + 27) * guiScale);
        int sy = (int) Math.floor((guiTop  + 27) * guiScale);
        int sw = (int) Math.floor((guiWidth  - 54) * guiScale);
        int sh = (int) Math.floor((guiHeight - 54) * guiScale);
        RenderSystem.enableScissor(sx, sy, sw, sh);
        progressionRenderer.drawProgressionPart(graphics, mouseX, mouseY);
        RenderSystem.disableScissor();

        // Draw the book frame on top (clears outside)
        drawDefault(graphics, TexturesAS.TEX_GUI_BOOK_FRAME_FULL, mouseX, mouseY);
        drawSearchBox(graphics);
        progressionRenderer.drawMouseHighlight(graphics, mouseX, mouseY);
    }

    private void drawSearchResults(GuiGraphics graphics, int mouseX, int mouseY) {
        var font = Minecraft.getInstance().font;
        int lineH   = 12;
        int offsetX = this.guiLeft + 35;
        int offsetY = this.guiTop + 26;

        double t = (Math.sin(Math.toRadians((ClientScheduler.getClientTick() * 5L) % 360)) + 1.0) / 2.0;
        int alpha  = Math.round((0.45f + 0.1f * (float) t) * 255);
        int gray   = Math.round((0.7f  + 0.2f * (float) t) * 255);
        int boxColor = new Color(gray, gray, gray, alpha).getRGB();

        List<ResearchNode> left = searchResultPageIndex.getOrDefault(searchPageOffset, new ArrayList<>());
        for (ResearchNode node : left) {
            int startY = offsetY;
            List<FormattedCharSequence> lines = font.split(node.getName(), SEARCH_ENTRY_WIDTH);
            int maxW = 0;
            for (FormattedCharSequence line : lines) {
                int w = font.width(line);
                if (w > maxW) maxW = w;
                graphics.drawString(font, line, offsetX, offsetY, 0x00D0D0D0, false);
                offsetY += lineH;
            }
            if (searchHoverNode == null) {
                Rectangle rct = new Rectangle(offsetX - 2, startY - 2, maxW + 4, offsetY - startY);
                if (rct.contains(mouseX, mouseY)) {
                    graphics.fill(rct.x, rct.y, rct.x + rct.width, rct.y + rct.height, boxColor);
                    searchHoverNode = node;
                }
            }
        }

        offsetX = this.guiLeft + 225;
        offsetY = this.guiTop  + 39;
        List<ResearchNode> right = searchResultPageIndex.getOrDefault(searchPageOffset + 1, new ArrayList<>());
        for (ResearchNode node : right) {
            int startY = offsetY;
            List<FormattedCharSequence> lines = font.split(node.getName(), SEARCH_ENTRY_WIDTH);
            int maxW = 0;
            for (FormattedCharSequence line : lines) {
                int w = font.width(line);
                if (w > maxW) maxW = w;
                graphics.drawString(font, line, offsetX, offsetY, 0x00D0D0D0, false);
                offsetY += lineH;
            }
            if (searchHoverNode == null) {
                Rectangle rct = new Rectangle(offsetX - 2, startY - 2, maxW + 4, offsetY - startY);
                if (rct.contains(mouseX, mouseY)) {
                    graphics.fill(rct.x, rct.y, rct.x + rct.width, rct.y + rct.height, boxColor);
                    searchHoverNode = node;
                }
            }
        }
    }

    private void drawSearchBox(GuiGraphics graphics) {
        RenderingDrawUtils.drawTexturedRect(graphics, TexturesAS.TEX_GUI_TEXT_FIELD.getKey(),
                guiLeft + 300, guiTop + 16, 89, 15);

        String text = this.searchTextEntry.getText();
        var font = Minecraft.getInstance().font;
        int length = font.width(text);
        boolean addDots = length > 75;
        while (length > 75) {
            text = text.substring(1);
            length = font.width("..." + text);
        }
        if (addDots) text = "..." + text;
        if ((ClientScheduler.getClientTick() % 20) > 10) text += "_";

        graphics.drawString(font, text, guiLeft + 304, guiTop + 20, 0xCCCCCC, false);
    }

    private void drawSearchPageNavArrows(GuiGraphics graphics, int mouseX, int mouseY, float pTicks) {
        if (searchPageOffset > 0) {
            float w = 30, h = 15;
            searchPrevRct = new Rectangle(guiLeft + 25, guiTop + 220, (int) w, (int) h);
            boolean hov = searchPrevRct.contains(mouseX, mouseY);
            float scale = hov ? 1.1f : (float)(Math.sin(ClientScheduler.getClientTick() / 4.0) / 32.0 + 1.0);
            graphics.pose().pushPose();
            graphics.pose().translate(searchPrevRct.x + w / 2f, searchPrevRct.y + h / 2f, 0);
            graphics.pose().scale(scale, scale, 1f);
            graphics.pose().translate(-w / 2f, -h / 2f, 0);
            RenderingDrawUtils.drawTexturedRect(graphics, TexturesAS.TEX_GUI_BOOK_ARROWS.getKey(),
                    0, 0, (int) w, (int) h, hov ? 30 : 0, 15, (int) w, (int) h, 60, 30);
            graphics.pose().popPose();
        }

        int nextDoublePageIndex = (searchPageOffset * 2) + 2;
        if (searchResultPageIndex.size() >= nextDoublePageIndex + 1) {
            float w = 30, h = 15;
            searchNextRct = new Rectangle(guiLeft + 367, guiTop + 220, (int) w, (int) h);
            boolean hov = searchNextRct.contains(mouseX, mouseY);
            float scale = hov ? 1.1f : (float)(Math.sin(ClientScheduler.getClientTick() / 4.0) / 32.0 + 1.0);
            graphics.pose().pushPose();
            graphics.pose().translate(searchNextRct.x + w / 2f, searchNextRct.y + h / 2f, 0);
            graphics.pose().scale(scale, scale, 1f);
            graphics.pose().translate(-w / 2f, -h / 2f, 0);
            RenderingDrawUtils.drawTexturedRect(graphics, TexturesAS.TEX_GUI_BOOK_ARROWS.getKey(),
                    0, 0, (int) w, (int) h, hov ? 30 : 0, 0, (int) w, (int) h, 60, 30);
            graphics.pose().popPose();
        }
    }

    private void onSearchTextInput() {
        if (!inProgressView() && isCurrentlyDragging()) {
            stopDragging(-1, -1);
            progressionRenderer.applyMovedMouseOffset();
        }
        PlayerProgress prog = ResearchHelper.getClientProgress();
        searchResult.clear();
        searchResultPageIndex.clear();
        String query = searchTextEntry.getText().toLowerCase(Locale.ROOT);

        for (ResearchProgression research : ResearchProgression.values()) {
            if (!prog.hasResearch(research)) continue;
            for (ResearchNode node : research.getResearchNodes()) {
                String name = node.getName().getString().toLowerCase(Locale.ROOT);
                if (name.contains(query) && !searchResult.contains(node)) {
                    searchResult.add(node);
                }
            }
        }
        searchResult.sort(Comparator.comparing(n -> n.getName().getString()));

        var font = Minecraft.getInstance().font;
        int addedPages = 0, pageIndex = 0;
        while (addedPages < searchResult.size()) {
            List<ResearchNode> page = searchResultPageIndex.computeIfAbsent(pageIndex, k -> new ArrayList<>());
            int entriesPerPage = (pageIndex % 2 == 0) ? SEARCH_ENTRIES_LEFT : SEARCH_ENTRIES_RIGHT;
            int remaining = entriesPerPage - page.size();
            ResearchNode node = searchResult.get(addedPages);
            int lines = font.split(node.getName(), SEARCH_ENTRY_WIDTH).size();
            if (remaining < lines) { pageIndex++; continue; }
            page.add(node);
            addedPages++;
        }
        while (searchPageOffset > 0 && searchPageOffset >= searchResultPageIndex.size()) {
            searchPageOffset--;
        }
    }

    @Override
    protected void mouseDragTick(double mouseX, double mouseY, double diffX, double diffY,
                                  double offX, double offY) {
        super.mouseDragTick(mouseX, mouseY, diffX, diffY, offX, offY);
        if (inProgressView()) {
            progressionRenderer.moveMouse((float) diffX, (float) diffY);
        }
    }

    @Override
    protected void mouseDragStop(double mouseX, double mouseY, double diffX, double diffY) {
        super.mouseDragStop(mouseX, mouseY, diffX, diffY);
        if (inProgressView()) {
            progressionRenderer.applyMovedMouseOffset();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (inProgressView()) {
            if (scroll < 0) { progressionRenderer.handleZoomOut(); return true; }
            if (scroll > 0) { progressionRenderer.handleZoomIn((float) mouseX, (float) mouseY); return true; }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0) return false;
        if (handleBookmarkClick(mouseX, mouseY)) return true;
        if (inProgressView()) {
            return progressionRenderer.propagateClick((float) mouseX, (float) mouseY);
        } else {
            if (searchPrevRct != null && searchPrevRct.contains(mouseX, mouseY)) {
                searchPageOffset--;
                return true;
            }
            if (searchNextRct != null && searchNextRct.contains(mouseX, mouseY)) {
                searchPageOffset++;
                return true;
            }
            if (searchHoverNode != null) {
                searchTextEntry.setText("");
                Minecraft.getInstance().setScreen(new ScreenJournalPages(this, searchHoverNode));
                return true;
            }
        }
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
    public boolean charTyped(char charCode, int keyModifiers) {
        if (searchTextEntry.charTyped(charCode)) return true;
        return super.charTyped(charCode, keyModifiers);
    }
}
