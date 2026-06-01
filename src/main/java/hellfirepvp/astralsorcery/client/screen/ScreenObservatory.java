/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktDiscoverConstellation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Observatory constellation discovery screen.
 * A full-screen sky view allowing the player to identify constellations
 * visible in the current night sky.
 *
 * <p>More sensitive than the hand telescope — shows more constellations
 * across a full-screen view. Closes on right-click or Escape.</p>
 *
 * <p>1.16 → 1.20: Simplified from the complex TileConstellationDiscoveryScreen
 * system to the GuiGraphics-based Screen pattern used by ScreenTelescope.</p>
 */
@OnlyIn(Dist.CLIENT)
@SuppressWarnings("null")
public class ScreenObservatory extends Screen {

    private static final int FRAME_SIZE = 16;
    private static final int STAR_COUNT = 280;
    private static final int CONSTELLATION_SIZE = 90;

    private final List<ConstellationEntry> visibleConstellations = new ArrayList<>();
    private final List<StarEntry> backgroundStars = new ArrayList<>();
    private final Random seedRand = new Random();

    private int selectedIndex = -1;

    public ScreenObservatory() {
        super(Component.translatable("screen.astralsorcery.observatory"));
    }

    @Override
    protected void init() {
        super.init();
        buildConstellations();
        buildStars();
    }

    private void buildConstellations() {
        visibleConstellations.clear();
        @Nullable Level level = minecraft != null ? minecraft.level : null;
        if (level == null || !DayTimeHelper.isNight(level)) return;

        Set<IConstellation> visible = CelestialHandler.getVisibleConstellations(level);
        int total = visible.size();
        if (total == 0) return;

        int idx = 0;
        for (IConstellation cst : visible) {
            double angle = 2.0 * Math.PI * idx / total;
            float radiusX = width  * 0.30f;
            float radiusY = height * 0.28f;
            int cx = (int) (width  / 2 + Math.cos(angle) * radiusX);
            int cy = (int) (height / 2 + Math.sin(angle) * radiusY);
            visibleConstellations.add(new ConstellationEntry(cst, cx, cy));
            idx++;
        }
    }

    private void buildStars() {
        backgroundStars.clear();
        for (int i = 0; i < STAR_COUNT; i++) {
            backgroundStars.add(new StarEntry(
                    FRAME_SIZE + seedRand.nextFloat() * (width  - FRAME_SIZE * 2),
                    FRAME_SIZE + seedRand.nextFloat() * (height - FRAME_SIZE * 2),
                    2 + seedRand.nextFloat() * 2f,
                    i % 40
            ));
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Night sky gradient
        graphics.fillGradient(0, 0, width, height, 0xFF050510, 0xFF0A0A20);

        @Nullable Level level = minecraft != null ? minecraft.level : null;
        boolean isNight = level != null && DayTimeHelper.isNight(level);

        if (isNight) {
            int tick = (int) ClientScheduler.getClientTick();

            // Background stars
            for (StarEntry star : backgroundStars) {
                float flicker = RenderingConstellationUtils.getTwinkleBrightness(
                        star.flickerOffset, tick, partialTick);
                int alpha = (int) (flicker * 180);
                int color = (alpha << 24) | 0x99BBDD;
                graphics.fill((int) star.x, (int) star.y,
                        (int) (star.x + star.size), (int) (star.y + star.size), color);
            }

            // Update hover state
            selectedIndex = -1;
            for (int i = 0; i < visibleConstellations.size(); i++) {
                ConstellationEntry e = visibleConstellations.get(i);
                double dist = Math.sqrt(Math.pow(mouseX - e.cx, 2) + Math.pow(mouseY - e.cy, 2));
                if (dist < CONSTELLATION_SIZE / 2.0) {
                    selectedIndex = i;
                    break;
                }
            }

            // Constellations
            var progress = PlayerProgressManager.getClientProgress();
            for (int i = 0; i < visibleConstellations.size(); i++) {
                ConstellationEntry entry = visibleConstellations.get(i);
                boolean hovered = (i == selectedIndex);
                int sz = hovered ? (int) (CONSTELLATION_SIZE * 1.3f) : CONSTELLATION_SIZE;
                float brightness = hovered ? 1.0f : 0.65f;

                RenderingConstellationUtils.renderConstellationLarge(
                        graphics, entry.constellation,
                        entry.cx - sz / 2, entry.cy - sz / 2,
                        sz, sz, brightness);

                if (hovered) {
                    boolean known = progress.hasDiscovered(entry.constellation.getRegistryName());
                    String label = known
                            ? entry.constellation.getConstellationName().getString()
                            : "???";
                    graphics.drawCenteredString(font, label,
                            entry.cx, entry.cy + sz / 2 + 5, 0xFFEEFF);
                }
            }
        }

        // Vignette border
        drawFrame(graphics);

        // Status hint
        String msg;
        if (!isNight) {
            msg = "Come back at night to use the observatory";
        } else if (visibleConstellations.isEmpty()) {
            msg = "No constellations visible tonight";
        } else {
            msg = "Click a constellation to identify it  |  Right-click to exit";
        }
        graphics.drawCenteredString(font, msg, width / 2, height - FRAME_SIZE - 12, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawFrame(@Nonnull GuiGraphics graphics) {
        int c = 0xCC000000;
        graphics.fill(0, 0, FRAME_SIZE, height, c);
        graphics.fill(width - FRAME_SIZE, 0, width, height, c);
        graphics.fill(FRAME_SIZE, 0, width - FRAME_SIZE, FRAME_SIZE, c);
        graphics.fill(FRAME_SIZE, height - FRAME_SIZE, width - FRAME_SIZE, height, c);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && selectedIndex >= 0) {
            ConstellationEntry entry = visibleConstellations.get(selectedIndex);
            PacketChannel.sendToServer(
                    new PktDiscoverConstellation(entry.constellation.getRegistryName()));
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(
                        Component.translatable("astralsorcery.observatory.observing",
                                entry.constellation.getConstellationName()),
                        true);
            }
            return true;
        }
        if (button == 1) {
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        super.onClose();
        if (minecraft != null && minecraft.player != null
                && minecraft.player.isPassenger()) {
            minecraft.player.stopRiding();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record ConstellationEntry(@Nonnull IConstellation constellation, int cx, int cy) {}
    private record StarEntry(float x, float y, float size, int flickerOffset) {}
}
