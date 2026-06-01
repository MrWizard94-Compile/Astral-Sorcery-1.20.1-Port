/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.overlay;

import hellfirepvp.astralsorcery.client.ClientStarlightCache;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import javax.annotation.Nonnull;

/**
 * HUD overlay rendering the player's current starlight charge level as a
 * simple color-fill gauge in the top-left corner when the player is attuned.
 */
@OnlyIn(Dist.CLIENT)
public class OverlayStarlightGauge implements IGuiOverlay {

    public static final OverlayStarlightGauge INSTANCE = new OverlayStarlightGauge();

    private static final int GAUGE_WIDTH  = 6;
    private static final int GAUGE_HEIGHT = 48;
    private static final int OFFSET_X     = 4;
    private static final int OFFSET_Y     = 4;

    // ARGB colors
    private static final int COLOR_BG   = 0x55223344; // dim blue-black background
    private static final int COLOR_FILL = 0xCC99CCFF; // light blue fill

    private OverlayStarlightGauge() {}

    @Override
    public void render(@Nonnull ForgeGui gui, @Nonnull GuiGraphics graphics,
                        float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!shouldRender()) return;

        float fill = getStarlightFillLevel();
        if (fill <= 0.0f) return;

        int x = OFFSET_X;
        int y = OFFSET_Y;

        // Background track
        graphics.fill(x, y, x + GAUGE_WIDTH, y + GAUGE_HEIGHT, COLOR_BG);

        // Fill bar (bottom to top)
        int fillHeight = (int) (GAUGE_HEIGHT * fill);
        int fillY = y + GAUGE_HEIGHT - fillHeight;
        graphics.fill(x, fillY, x + GAUGE_WIDTH, y + GAUGE_HEIGHT, COLOR_FILL);
    }

    private boolean shouldRender() {
        return PlayerProgressManager.getClientProgress().getAttunedConstellation() != null;
    }

    private float getStarlightFillLevel() {
        return ClientStarlightCache.getFillFraction();
    }
}
