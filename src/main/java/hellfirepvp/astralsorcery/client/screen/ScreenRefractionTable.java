package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.DrawnConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktEngraveGlass;
import hellfirepvp.astralsorcery.common.tile.BlockEntityRefractionTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Refraction table screen — lets the player select up to 3 discovered
 * constellations and drag them onto the placement grid to engrave
 * an infused glass lens.
 *
 * <p>1.16 → 1.20: MatrixStack+RenderSystem → GuiGraphics,
 * removed RenderSystem.disableAlphaTest() (no longer exists in 1.17+),
 * TileEntityScreen → plain Screen with tile reference.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenRefractionTable extends Screen {

    private static final ResourceLocation TEXTURE_EMPTY =
            AstralSorcery.key("textures/gui/refraction_table_empty.png");
    private static final ResourceLocation TEXTURE_PARCHMENT =
            AstralSorcery.key("textures/gui/refraction_table_parchment.png");

    private static final int GUI_WIDTH = 188;
    private static final int GUI_HEIGHT = 256;

    // The placement grid (relative to guiLeft/guiTop)
    private static final int GRID_X = 34;
    private static final int GRID_Y = 45;
    private static final int GRID_W = 120;
    private static final int GRID_H = 120;

    // Each constellation option icon size
    private static final int ICON_SIZE = 18;

    @Nonnull
    private final BlockEntityRefractionTable tile;
    @Nonnull
    private final BlockPos tilePos;
    @Nonnull
    private final ResourceKey<Level> dimension;

    private int guiLeft;
    private int guiTop;

    // Constellations the player has discovered (shown as options on the sides)
    @Nonnull
    private final List<IConstellation> options = new ArrayList<>();
    // Mapping from option bounding box (screen coords) to constellation
    @Nonnull
    private final Map<Rectangle, IConstellation> optionRects = new LinkedHashMap<>();

    // Constellations the player has dragged onto the grid
    @Nonnull
    private final List<DrawnConstellation> placed = new ArrayList<>();

    // Currently dragged constellation
    @Nullable
    private IConstellation dragging = null;

    public ScreenRefractionTable(@Nonnull BlockEntityRefractionTable tile) {
        super(Component.translatable("screen.astralsorcery.refraction_table"));
        this.tile = tile;
        this.tilePos = tile.getBlockPos();
        Level level = tile.getLevel();
        this.dimension = level != null
                ? level.dimension()
                : Level.OVERWORLD;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;
        refreshOptions();
    }

    private void refreshOptions() {
        options.clear();
        Set<ResourceLocation> discovered =
                PlayerProgressManager.getClientProgress().getDiscoveredConstellations();
        for (ResourceLocation key : discovered) {
            IConstellation cst = ConstellationRegistry.getConstellation(key);
            if (cst != null) options.add(cst);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        // Draw background texture
        ResourceLocation bg = tile.hasParchment() ? TEXTURE_PARCHMENT : TEXTURE_EMPTY;
        graphics.blit(bg, guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        // Draw option constellations on left and right margins
        optionRects.clear();
        buildOptionRects();
        renderOptions(graphics, mouseX, mouseY);

        // Draw placement grid border
        renderGrid(graphics);

        // Draw placed constellations on the grid
        renderPlaced(graphics);

        // Draw currently dragged constellation at cursor
        renderDragging(graphics, mouseX, mouseY);

        // Status text
        renderStatus(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void buildOptionRects() {
        int startY = guiTop + 40;
        for (int i = 0; i < Math.min(options.size(), 12); i++) {
            // Alternate left (even) and right (odd) columns
            int x = (i % 2 == 0)
                    ? guiLeft - ICON_SIZE - 4
                    : guiLeft + GUI_WIDTH + 4;
            int y = startY + (i / 2) * (ICON_SIZE + 4);
            optionRects.put(new Rectangle(x, y, ICON_SIZE, ICON_SIZE), options.get(i));
        }
    }

    private void renderOptions(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        for (Map.Entry<Rectangle, IConstellation> entry : optionRects.entrySet()) {
            Rectangle r = entry.getKey();
            IConstellation cst = entry.getValue();
            boolean hovered = r.contains(mouseX, mouseY);

            int boxColor = hovered ? 0xFF88BBFF : 0xFF446699;
            graphics.fill(r.x, r.y, r.x + r.width, r.y + r.height, boxColor);

            // Draw constellation name abbreviation (first 2 chars)
            String name = cst.getConstellationName().getString();
            String abbrev = name.length() >= 2 ? name.substring(0, 2) : name;
            graphics.drawCenteredString(font, abbrev, r.x + r.width / 2, r.y + 5, 0xFFFFFF);

            if (hovered) {
                graphics.renderTooltip(font,
                        cst.getConstellationName(),
                        mouseX, mouseY);
            }
        }
    }

    private void renderGrid(@Nonnull GuiGraphics graphics) {
        int x1 = guiLeft + GRID_X;
        int y1 = guiTop + GRID_Y;
        int x2 = x1 + GRID_W;
        int y2 = y1 + GRID_H;
        int borderColor = 0x884499AA;
        // Thin border
        graphics.fill(x1, y1, x2, y1 + 1, borderColor);
        graphics.fill(x1, y2 - 1, x2, y2, borderColor);
        graphics.fill(x1, y1, x1 + 1, y2, borderColor);
        graphics.fill(x2 - 1, y1, x2, y2, borderColor);
    }

    private void renderPlaced(@Nonnull GuiGraphics graphics) {
        int gridAbsX = guiLeft + GRID_X;
        int gridAbsY = guiTop + GRID_Y;
        int halfIcon = ICON_SIZE / 2;
        for (DrawnConstellation dc : placed) {
            int cx = gridAbsX + dc.getPoint().x;
            int cy = gridAbsY + dc.getPoint().y;
            graphics.fill(cx - halfIcon, cy - halfIcon,
                    cx + halfIcon, cy + halfIcon, 0xCC224488);
            String abbrev = dc.getConstellation().getConstellationName().getString();
            if (abbrev.length() > 2) abbrev = abbrev.substring(0, 2);
            graphics.drawCenteredString(font, abbrev, cx, cy - 4, 0xCCDDFF);
        }
    }

    private void renderDragging(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        IConstellation drag = dragging;
        if (drag == null) return;
        int halfIcon = ICON_SIZE / 2;
        graphics.fill(mouseX - halfIcon, mouseY - halfIcon,
                mouseX + halfIcon, mouseY + halfIcon, 0xCC6699CC);
        String abbrev = drag.getConstellationName().getString();
        if (abbrev.length() > 2) abbrev = abbrev.substring(0, 2);
        graphics.drawCenteredString(font, abbrev, mouseX, mouseY - 4, 0xFFFFFF);
    }

    private void renderStatus(@Nonnull GuiGraphics graphics) {
        Level level = Minecraft.getInstance().level;
        boolean isNight = level != null && DayTimeHelper.isNight(level);

        if (!tile.hasParchment()) {
            graphics.drawCenteredString(font,
                    Component.translatable("astralsorcery.refraction_table.no_parchment"),
                    guiLeft + GUI_WIDTH / 2, guiTop + GRID_Y + GRID_H + 8, 0xAAAAAA);
        } else if (!tile.hasUnengravedGlass()) {
            graphics.drawCenteredString(font,
                    Component.translatable("astralsorcery.refraction_table.already_engraved"),
                    guiLeft + GUI_WIDTH / 2, guiTop + GRID_Y + GRID_H + 8, 0xAAAAAA);
        } else if (!isNight) {
            graphics.drawCenteredString(font,
                    Component.translatable("astralsorcery.refraction_table.needs_night"),
                    guiLeft + GUI_WIDTH / 2, guiTop + GRID_Y + GRID_H + 8, 0xAAAAAA);
        } else {
            String msg = placed.size() + " / 3 constellations placed";
            graphics.drawCenteredString(font, msg,
                    guiLeft + GUI_WIDTH / 2, guiTop + GRID_Y + GRID_H + 8, 0xCCDDFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging == null
                && tile.hasParchment() && tile.hasUnengravedGlass()
                && placed.size() < 3) {
            // Try to pick an option
            for (Map.Entry<Rectangle, IConstellation> entry : optionRects.entrySet()) {
                if (entry.getKey().contains((int) mouseX, (int) mouseY)) {
                    dragging = entry.getValue();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        IConstellation drag = dragging;
        if (button == 0 && drag != null
                && tile.hasParchment() && tile.hasUnengravedGlass()
                && placed.size() < 3) {
            // Drop onto grid if inside grid bounds
            int relX = (int) mouseX - (guiLeft + GRID_X);
            int relY = (int) mouseY - (guiTop + GRID_Y);
            if (relX >= 0 && relX < GRID_W && relY >= 0 && relY < GRID_H) {
                placed.add(new DrawnConstellation(new Point(relX, relY), drag));
                if (placed.size() >= 3) {
                    sendEngravePacket();
                }
            }
            dragging = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void sendEngravePacket() {
        PacketChannel.sendToServer(new PktEngraveGlass(dimension, tilePos, new ArrayList<>(placed)));
        placed.clear();
        this.onClose();
    }

    @Override
    public void tick() {
        super.tick();
        // Close if tile is gone or too far away
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            this.onClose();
            return;
        }
        if (mc.player.distanceToSqr(
                tilePos.getX() + 0.5, tilePos.getY() + 0.5, tilePos.getZ() + 0.5) > 64.0) {
            this.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
