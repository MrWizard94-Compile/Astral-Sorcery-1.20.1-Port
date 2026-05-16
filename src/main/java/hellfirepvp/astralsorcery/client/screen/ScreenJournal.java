/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.constellation.Constellation;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Astral Sorcery Journal — the player's knowledge book and
 * progression tracker. Accessed via the Resonating Wand or keybind.
 *
 * <p>The journal has multiple pages/tabs:
 * <ul>
 *   <li><b>Discovery</b> — discovered constellations, lore entries</li>
 *   <li><b>Constellation</b> — detailed constellation info and effects</li>
 *   <li><b>Perks</b> — shortcut to perk tree screen</li>
 *   <li><b>Knowledge</b> — recipes and progression tips</li>
 * </ul></p>
 *
 * <p>Page rendering uses a book-style dual-page layout with navigation
 * buttons for forward/backward, tab buttons at the top for sections.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenJournal extends ScreenBaseAS {

    // =========================================================================
    // Textures
    // =========================================================================

    private static final ResourceLocation TEX_JOURNAL_BACKGROUND =
            AstralSorcery.key("textures/gui/journal/background.png");
    private static final ResourceLocation TEX_TAB_DISCOVERY =
            AstralSorcery.key("textures/gui/journal/tab_discovery.png");
    private static final ResourceLocation TEX_TAB_CONSTELLATION =
            AstralSorcery.key("textures/gui/journal/tab_constellation.png");
    private static final ResourceLocation TEX_TAB_KNOWLEDGE =
            AstralSorcery.key("textures/gui/journal/tab_knowledge.png");

    private static final int JOURNAL_WIDTH = 378;
    private static final int JOURNAL_HEIGHT = 256;

    // =========================================================================
    // State
    // =========================================================================

    /** Current journal tab. */
    @Nonnull
    private JournalTab currentTab = JournalTab.DISCOVERY;

    /** Current page within the tab. */
    private int currentPage = 0;

    /** Discovered constellations for the player (populated on open). */
    @Nonnull
    private List<IConstellation> discoveredConstellations = new ArrayList<>();

    /** Currently selected constellation for detail view. */
    @Nullable
    private IConstellation selectedConstellation = null;

    // =========================================================================
    // Construction
    // =========================================================================

    public ScreenJournal() {
        super(Component.translatable("screen.astralsorcery.journal"),
                TEX_JOURNAL_BACKGROUND, JOURNAL_WIDTH, JOURNAL_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        loadDiscoveredConstellations();
        currentPage = 0;
        selectedConstellation = null;

        // Navigation buttons
        int navY = guiTop + bgHeight - 20;
        this.addRenderableWidget(Button.builder(Component.literal("<"), btn -> prevPage())
                .bounds(guiLeft + 20, navY, 20, 16)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), btn -> nextPage())
                .bounds(guiLeft + bgWidth - 40, navY, 20, 16)
                .build());

        // Tab buttons
        int tabY = guiTop - 18;
        int tabX = guiLeft + 30;
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.astralsorcery.journal.tab.discovery"),
                btn -> switchTab(JournalTab.DISCOVERY))
                .bounds(tabX, tabY, 60, 16)
                .build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.astralsorcery.journal.tab.constellations"),
                btn -> switchTab(JournalTab.CONSTELLATIONS))
                .bounds(tabX + 65, tabY, 80, 16)
                .build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.astralsorcery.journal.tab.knowledge"),
                btn -> switchTab(JournalTab.KNOWLEDGE))
                .bounds(tabX + 150, tabY, 60, 16)
                .build());
    }

    // =========================================================================
    // Rendering
    // =========================================================================

    @Override
    protected void renderContent(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        switch (currentTab) {
            case DISCOVERY -> renderDiscoveryTab(graphics, mouseX, mouseY, partialTick);
            case CONSTELLATIONS -> renderConstellationsTab(graphics, mouseX, mouseY, partialTick);
            case KNOWLEDGE -> renderKnowledgeTab(graphics, mouseX, mouseY, partialTick);
        }
    }

    /**
     * Discovery tab: shows discovered constellations as a grid of icons.
     */
    private void renderDiscoveryTab(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int startX = guiLeft + 30;
        int startY = guiTop + 30;
        int columns = 4;
        int cellSize = 40;

        Component title = Component.translatable("screen.astralsorcery.journal.discovery.title");
        graphics.drawString(this.font, title, guiLeft + 20, guiTop + 12, 0xCCDDFF);

        int startIdx = currentPage * 8; // 8 per page (4x2)
        int endIdx = Math.min(startIdx + 8, discoveredConstellations.size());

        for (int i = startIdx; i < endIdx; i++) {
            IConstellation c = discoveredConstellations.get(i);
            int idx = i - startIdx;
            int col = idx % columns;
            int row = idx / columns;
            int x = startX + col * (cellSize + 10);
            int y = startY + row * (cellSize + 10);

            // Draw constellation icon (simplified star pattern)
            RenderingConstellationUtils.renderConstellationSmall(
                    graphics, c, x, y, cellSize, cellSize, 1.0f);

            // Name below
            Component name = Component.translatable(
                    "constellation.astralsorcery." + c.getSimpleName());
            graphics.drawCenteredString(this.font, name, x + cellSize / 2, y + cellSize + 2, 0xAABBFF);
        }

        if (discoveredConstellations.isEmpty()) {
            Component empty = Component.translatable("screen.astralsorcery.journal.discovery.empty");
            graphics.drawCenteredString(this.font, empty, guiLeft + bgWidth / 2, guiTop + bgHeight / 2, 0x888888);
        }
    }

    /**
     * Constellations tab: shows detailed info about a selected constellation.
     */
    private void renderConstellationsTab(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (selectedConstellation != null) {
            renderConstellationDetail(graphics, selectedConstellation, partialTick);
        } else {
            // Show constellation list (all known)
            renderConstellationList(graphics, mouseX, mouseY);
        }
    }

    /**
     * Render the full constellation detail page with star map, name, and description.
     */
    private void renderConstellationDetail(@Nonnull GuiGraphics graphics,
                                            @Nonnull IConstellation constellation,
                                            float partialTick) {
        int centerX = guiLeft + bgWidth / 2;
        int topY = guiTop + 30;

        // Name
        Component name = Component.translatable(
                "constellation.astralsorcery." + constellation.getSimpleName());
        graphics.drawCenteredString(this.font, name, centerX, topY, 0xFFEECC);

        // Large constellation rendering
        int starMapSize = 120;
        int mapX = centerX - starMapSize / 2;
        int mapY = topY + 20;
        RenderingConstellationUtils.renderConstellationLarge(
                graphics, constellation, mapX, mapY, starMapSize, starMapSize, 1.0f);

        // Type indicator
        String typeKey;
        if (constellation instanceof Constellation.Major) typeKey = "major";
        else if (constellation instanceof Constellation.Weak) typeKey = "weak";
        else typeKey = "minor";

        Component type = Component.translatable("screen.astralsorcery.journal.constellation.type." + typeKey);
        graphics.drawCenteredString(this.font, type, centerX, mapY + starMapSize + 10, 0xAABBCC);
    }

    /**
     * Render constellation list (selectable grid).
     */
    private void renderConstellationList(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        Component title = Component.translatable("screen.astralsorcery.journal.constellations.title");
        graphics.drawString(this.font, title, guiLeft + 20, guiTop + 12, 0xCCDDFF);

        int startX = guiLeft + 25;
        int startY = guiTop + 30;
        int cellSize = 36;
        int columns = 5;
        int startIdx = currentPage * 10;
        int endIdx = Math.min(startIdx + 10, discoveredConstellations.size());

        for (int i = startIdx; i < endIdx; i++) {
            IConstellation c = discoveredConstellations.get(i);
            int idx = i - startIdx;
            int col = idx % columns;
            int row = idx / columns;
            int x = startX + col * (cellSize + 8);
            int y = startY + row * (cellSize + 16);

            RenderingConstellationUtils.renderConstellationSmall(
                    graphics, c, x, y, cellSize, cellSize, 0.8f);

            Component name = Component.translatable(
                    "constellation.astralsorcery." + c.getSimpleName());
            graphics.drawCenteredString(this.font, name, x + cellSize / 2, y + cellSize + 2, 0x999999);
        }
    }

    /**
     * Knowledge tab: progression hints and recipe references.
     */
    private void renderKnowledgeTab(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Component title = Component.translatable("screen.astralsorcery.journal.knowledge.title");
        graphics.drawString(this.font, title, guiLeft + 20, guiTop + 12, 0xCCDDFF);

        // Placeholder content — will be populated with actual knowledge entries
        int textY = guiTop + 35;
        graphics.drawString(this.font,
                Component.translatable("screen.astralsorcery.journal.knowledge.hint1"),
                guiLeft + 25, textY, 0xAABBCC);
        graphics.drawString(this.font,
                Component.translatable("screen.astralsorcery.journal.knowledge.hint2"),
                guiLeft + 25, textY + 14, 0xAABBCC);
        graphics.drawString(this.font,
                Component.translatable("screen.astralsorcery.journal.knowledge.hint3"),
                guiLeft + 25, textY + 28, 0xAABBCC);
    }

    // =========================================================================
    // Navigation
    // =========================================================================

    private void switchTab(@Nonnull JournalTab tab) {
        this.currentTab = tab;
        this.currentPage = 0;
        this.selectedConstellation = null;
    }

    private void nextPage() {
        int maxPages = getMaxPages();
        if (currentPage < maxPages - 1) {
            currentPage++;
        }
    }

    private void prevPage() {
        if (currentPage > 0) {
            currentPage--;
        }
    }

    private int getMaxPages() {
        int itemsPerPage = switch (currentTab) {
            case DISCOVERY -> 8;
            case CONSTELLATIONS -> 10;
            case KNOWLEDGE -> 1;
        };
        int total = switch (currentTab) {
            case DISCOVERY, CONSTELLATIONS -> discoveredConstellations.size();
            case KNOWLEDGE -> 1;
        };
        return Math.max(1, (total + itemsPerPage - 1) / itemsPerPage);
    }

    // =========================================================================
    // Mouse interaction
    // =========================================================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && currentTab == JournalTab.CONSTELLATIONS && selectedConstellation == null) {
            // Check if clicking on a constellation in the list
            int startX = guiLeft + 25;
            int startY = guiTop + 30;
            int cellSize = 36;
            int columns = 5;
            int startIdx = currentPage * 10;
            int endIdx = Math.min(startIdx + 10, discoveredConstellations.size());

            for (int i = startIdx; i < endIdx; i++) {
                int idx = i - startIdx;
                int col = idx % columns;
                int row = idx / columns;
                int x = startX + col * (cellSize + 8);
                int y = startY + row * (cellSize + 16);

                if (mouseX >= x && mouseX <= x + cellSize && mouseY >= y && mouseY <= y + cellSize) {
                    selectedConstellation = discoveredConstellations.get(i);
                    return true;
                }
            }
        } else if (button == 1 && selectedConstellation != null) {
            // Right-click to go back to list
            selectedConstellation = null;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // =========================================================================
    // Data loading
    // =========================================================================

    /**
     * Load the player's discovered constellations from capability data.
     */
    private void loadDiscoveredConstellations() {
        discoveredConstellations.clear();
        // TODO: Read from player capability data
        // For now, show all registered constellations as discovered
        discoveredConstellations.addAll(ConstellationRegistry.getAllConstellations());
    }

    // =========================================================================
    // Tab enum
    // =========================================================================

    private enum JournalTab {
        DISCOVERY,
        CONSTELLATIONS,
        KNOWLEDGE
    }
}
