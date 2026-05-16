/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import javax.annotation.Nonnull;

/**
 * HUD overlay rendering the player's current starlight charge level.
 * Displayed as a small crescent/star gauge in the top-left area
 * when the player is attuned and has starlight exposure.
 *
 * <p>1.16 → 1.20: Forge overlay system completely changed.
 * Old: {@code RenderGameOverlayEvent} + manual rendering.
 * New: {@code IGuiOverlay} registered via {@code RegisterGuiOverlaysEvent}.</p>
 */
@OnlyIn(Dist.CLIENT)
public class OverlayStarlightGauge implements IGuiOverlay {

    public static final OverlayStarlightGauge INSTANCE = new OverlayStarlightGauge();

    private static final ResourceLocation TEX_GAUGE_BG =
            AstralSorcery.key("textures/gui/overlay/starlight_gauge_bg.png");
    private static final ResourceLocation TEX_GAUGE_FILL =
            AstralSorcery.key("textures/gui/overlay/starlight_gauge_fill.png");

    /** Gauge dimensions in screen pixels. */
    private static final int GAUGE_WIDTH = 22;
    private static final int GAUGE_HEIGHT = 64;

    /** Screen position offset from top-left. */
    private static final int OFFSET_X = 4;
    private static final int OFFSET_Y = 4;

    private OverlayStarlightGauge() {}

    @Override
    public void render(@Nonnull ForgeGui gui, @Nonnull GuiGraphics graphics,
                        float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // TODO: Check if player is attuned (has starlight capability)
        if (!shouldRender()) return;

        float starlightFill = getStarlightFillLevel();
        if (starlightFill <= 0.0f) return;

        int x = OFFSET_X;
        int y = OFFSET_Y;

        // Background
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.7f);
        graphics.blit(TEX_GAUGE_BG, x, y, 0, 0, GAUGE_WIDTH, GAUGE_HEIGHT, GAUGE_WIDTH, GAUGE_HEIGHT);

        // Fill (bottom to top)
        int fillHeight = (int) (GAUGE_HEIGHT * starlightFill);
        int fillY = y + GAUGE_HEIGHT - fillHeight;
        int fillVOffset = GAUGE_HEIGHT - fillHeight;

        RenderSystem.setShaderColor(0.6f, 0.8f, 1.0f, 0.9f);
        graphics.blit(TEX_GAUGE_FILL, x, fillY, 0, fillVOffset,
                GAUGE_WIDTH, fillHeight, GAUGE_WIDTH, GAUGE_HEIGHT);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Whether to show the starlight gauge.
     * Only shown when the player is attuned.
     */
    private boolean shouldRender() {
        // TODO: Check player attunement capability
        // Placeholder: always show
        return true;
    }

    /**
     * Get the current starlight fill level [0, 1].
     * Reads from the player's starlight capability.
     */
    private float getStarlightFillLevel() {
        // TODO: Read from player starlight capability
        // Placeholder: simulate based on time of day (nighttime = high)
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return 0.0f;

        long dayTime = mc.level.getDayTime() % 24000;
        // Night is ~13000-23000
        if (dayTime >= 13000 && dayTime <= 23000) {
            // Calculate brightness based on how deep into night
            float nightProgress = (dayTime - 13000f) / 10000f;
            if (nightProgress <= 0.5f) {
                return nightProgress * 2.0f; // Ramp up
            } else {
                return (1.0f - nightProgress) * 2.0f; // Ramp down
            }
        }
        return 0.05f; // Minimal during day
    }
}
