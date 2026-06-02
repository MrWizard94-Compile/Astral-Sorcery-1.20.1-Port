/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktDiscoverConstellation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Telescope constellation discovery screen.
 * Displays the currently visible constellations inside the telescope viewport
 * with a proper starfield background and actual constellation star patterns.
 *
 * <p>1.16 → 1.20: Screen rendering via GuiGraphics.
 * Constellation rendering via RenderingConstellationUtils (MultiBufferSource path).
 * Background stars drawn with CONSTELLATION_STAR render type (additive blend + star texture).</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenTelescope extends ScreenBaseAS {

    private static final ResourceLocation TEXTURE_BG =
            AstralSorcery.key("textures/gui/telescope_frame.png");

    private static final int GUI_WIDTH = 280;
    private static final int GUI_HEIGHT = 280;

    /** Size of each constellation's render area in pixels. */
    private static final int CST_BOX = 64;

    /** Number of background star dots. */
    private static final int BG_STAR_COUNT = 120;

    @Nonnull
    private final List<ConstellationEntry> visibleConstellations = new ArrayList<>();

    private int selectedIndex = -1;

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
        if (!DayTimeHelper.isNight(minecraft.level)) return;

        Set<IConstellation> visible = CelestialHandler.getVisibleConstellations(minecraft.level);
        int total = visible.size();
        int idx = 0;
        for (IConstellation constellation : visible) {
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
        if (minecraft == null || minecraft.level == null) return;

        boolean isNight = DayTimeHelper.isNight(minecraft.level);

        // Sky gradient inside the telescope viewport (5px inset from frame edges)
        graphics.fillGradient(
                guiLeft + 5, guiTop + 5,
                guiLeft + bgWidth - 5, guiTop + bgHeight - 5,
                0xFF06061A, 0xFF0E0E28);

        if (!isNight) {
            graphics.drawCenteredString(font, "Come back at night to observe the sky",
                    width / 2, guiTop + bgHeight / 2, 0xAAAAAA);
            return;
        }

        int tick = (int) ClientScheduler.getClientTick();
        int innerX = guiLeft + 8;
        int innerY = guiTop + 8;
        int innerW = bgWidth - 16;
        int innerH = bgHeight - 16;

        // Background stars — textured quads with additive blending
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance()
                .renderBuffers().bufferSource();
        PoseStack poseStack = graphics.pose();
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer starBuf = bufferSource.getBuffer(RenderTypesAS.CONSTELLATION_STAR);

        Random starRand = new Random(98765L);
        for (int i = 0; i < BG_STAR_COUNT; i++) {
            float sx = innerX + starRand.nextFloat() * innerW;
            float sy = innerY + starRand.nextFloat() * innerH;
            float sz = 2.0f + starRand.nextFloat() * 2.5f;
            float br = RenderingConstellationUtils.getTwinkleBrightness(i, tick, partialTick) * 0.75f;
            RenderingConstellationUtils.renderStar(starBuf, matrix, sx, sy, sz,
                    0.6f, 0.75f, 1.0f, br);
        }
        bufferSource.endBatch();

        // Hover detection
        selectedIndex = -1;
        for (int i = 0; i < visibleConstellations.size(); i++) {
            ConstellationEntry e = visibleConstellations.get(i);
            int cx = guiLeft + e.guiX;
            int cy = guiTop + e.guiY;
            double dist = Math.sqrt(Math.pow(mouseX - cx, 2) + Math.pow(mouseY - cy, 2));
            if (dist < CST_BOX / 2.0) {
                selectedIndex = i;
                break;
            }
        }

        // Draw each visible constellation as an actual star pattern
        for (int i = 0; i < visibleConstellations.size(); i++) {
            ConstellationEntry entry = visibleConstellations.get(i);
            boolean hovered = (i == selectedIndex);
            int cx = guiLeft + entry.guiX;
            int cy = guiTop + entry.guiY;
            int sz = hovered ? (int) (CST_BOX * 1.25f) : CST_BOX;
            float brightness = hovered ? 1.0f : 0.6f;

            RenderingConstellationUtils.renderConstellationSmall(
                    graphics, entry.constellation,
                    cx - sz / 2, cy - sz / 2,
                    sz, sz, brightness);

            if (hovered) {
                graphics.drawCenteredString(font,
                        entry.constellation.getConstellationName(),
                        cx, cy + sz / 2 + 4, 0xFFFFDD);
            }
        }

        // Instructions below frame
        if (!visibleConstellations.isEmpty()) {
            graphics.drawCenteredString(font,
                    "Click a constellation to identify it",
                    width / 2, guiTop + bgHeight + 5, 0xCCCCCC);
        } else {
            graphics.drawCenteredString(font,
                    "No constellations visible tonight",
                    width / 2, guiTop + bgHeight + 5, 0xAAAAAA);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && selectedIndex >= 0) {
            attemptDiscovery(visibleConstellations.get(selectedIndex).constellation);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void attemptDiscovery(@Nonnull IConstellation constellation) {
        ResourceLocation key = constellation.getRegistryName();
        PacketChannel.sendToServer(new PktDiscoverConstellation(key));

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

    private record ConstellationEntry(@Nonnull IConstellation constellation, int guiX, int guiY) {}
}
