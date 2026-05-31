/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktDiscoverConstellation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Telescope constellation discovery screen.
 * Displays the currently visible constellations and allows the player
 * to trace their star patterns to discover them.
 *
 * <p>Visible constellations are determined by the server-side
 * {@link CelestialHandler} and the current night cycle. The player
 * clicks constellation star points in the correct pattern to identify
 * a constellation.</p>
 *
 * <p>When a constellation is identified, sends {@link PktDiscoverConstellation}
 * to the server for validation and recording.</p>
 *
 * <p>1.16 → 1.20: Screen rendering via GuiGraphics.
 * Mouse handling APIs stable.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenTelescope extends ScreenBaseAS {

    private static final ResourceLocation TEXTURE_BG =
            AstralSorcery.key("textures/gui/telescope_frame.png");

    /** Background dimensions. */
    private static final int GUI_WIDTH = 280;
    private static final int GUI_HEIGHT = 280;

    /** Constellations visible in the current sky. */
    @Nonnull
    private final List<ConstellationEntry> visibleConstellations = new ArrayList<>();

    /** Currently selected/highlighted constellation. */
    private int selectedIndex = -1;

    /** Star tracing progress (which stars have been clicked). */
    @Nonnull
    private final List<Integer> tracedStars = new ArrayList<>();

    public ScreenTelescope() {
        super(Component.translatable("astralsorcery.screen.telescope"),
                TEXTURE_BG, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        refreshVisibleConstellations();
    }

    private void refreshVisibleConstellations() {
        visibleConstellations.clear();
        if (minecraft == null || minecraft.level == null) return;

        // Check if it's nighttime
        if (!DayTimeHelper.isNight(minecraft.level)) {
            return;
        }

        // Get visible constellations from celestial handler
        Set<IConstellation> visible = CelestialHandler.getVisibleConstellations(minecraft.level);
        int total = visible.size();
        int idx = 0;
        for (IConstellation constellation : visible) {
            // Distribute constellations across the view area
            float angle = (float) (2.0 * Math.PI * idx / total);
            float radius = 80.0f;
            int cx = (int) (GUI_WIDTH / 2 + Math.cos(angle) * radius);
            int cy = (int) (GUI_HEIGHT / 2 + Math.sin(angle) * radius);
            visibleConstellations.add(new ConstellationEntry(constellation, cx, cy));
            idx++;
        }
    }

    @Override
    protected void renderContent(@Nonnull GuiGraphics graphics, int mouseX, int mouseY,
                                  float partialTick) {
        // Draw constellation indicators
        for (int i = 0; i < visibleConstellations.size(); i++) {
            ConstellationEntry entry = visibleConstellations.get(i);
            int x = guiLeft + entry.guiX;
            int y = guiTop + entry.guiY;
            boolean highlighted = (i == selectedIndex);

            // Draw constellation star pattern
            int color = highlighted ? 0xFFFFDD44 : 0xFFCCDDFF;
            int size = highlighted ? 10 : 6;
            graphics.fill(x - size / 2, y - size / 2, x + size / 2, y + size / 2, color);

            // Show name if highlighted
            if (highlighted) {
                graphics.drawCenteredString(font,
                        entry.constellation.getConstellationName(),
                        x, y - 14, 0xFFFFFF);
            }
        }

        // Instructions
        if (visibleConstellations.isEmpty()) {
            String msg = DayTimeHelper.isNight(minecraft.level)
                    ? "No constellations visible tonight"
                    : "Come back at night to observe the sky";
            graphics.drawCenteredString(font, msg, width / 2, guiTop + bgHeight + 5, 0xAAAAAA);
        } else {
            graphics.drawCenteredString(font,
                    "Click a constellation to identify it",
                    width / 2, guiTop + bgHeight + 5, 0xCCCCCC);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int guiLeft = (this.width - GUI_WIDTH) / 2;
            int guiTop = (this.height - GUI_HEIGHT) / 2;

            for (int i = 0; i < visibleConstellations.size(); i++) {
                ConstellationEntry entry = visibleConstellations.get(i);
                int x = guiLeft + entry.guiX;
                int y = guiTop + entry.guiY;
                double dist = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
                if (dist < 12) {
                    attemptDiscovery(entry.constellation);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Sends a discovery request to the server for the given constellation.
     */
    private void attemptDiscovery(@Nonnull IConstellation constellation) {
        ResourceLocation key = constellation.getRegistryName();
        PacketChannel.sendToServer(new PktDiscoverConstellation(key));

        // Optimistic UI feedback
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    Component.translatable("astralsorcery.telescope.observing",
                            constellation.getConstellationName()),
                    true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Entry representing a constellation visible in the telescope view.
     */
    private record ConstellationEntry(@Nonnull IConstellation constellation, int guiX, int guiY) {}
}
