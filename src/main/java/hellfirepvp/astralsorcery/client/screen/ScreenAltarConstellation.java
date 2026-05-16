/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.container.ContainerAltarConstellation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Screen for the Constellation-tier altar (Celestial Altar).
 * Displays a 5x5 crafting grid (21 active slots) with relay slots,
 * starlight meter, and constellation focus indicator.
 */
@OnlyIn(Dist.CLIENT)
public class ScreenAltarConstellation extends ScreenContainerBaseAS<ContainerAltarConstellation> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AstralSorcery.MODID, "textures/gui/altar_constellation.png");

    private static final int BG_WIDTH = 176;
    private static final int BG_HEIGHT = 166;

    private static final int STARLIGHT_X = 8;
    private static final int STARLIGHT_Y = 10;
    private static final int STARLIGHT_WIDTH = 10;
    private static final int STARLIGHT_HEIGHT = 62;

    // Constellation focus indicator
    private static final int FOCUS_X = 152;
    private static final int FOCUS_Y = 10;
    private static final int FOCUS_SIZE = 16;

    public ScreenAltarConstellation(@Nonnull ContainerAltarConstellation menu,
                                     @Nonnull Inventory playerInventory,
                                     @Nonnull Component title) {
        super(menu, playerInventory, title, TEXTURE, BG_WIDTH, BG_HEIGHT);
    }

    @Override
    protected void renderContainerBg(@Nonnull GuiGraphics graphics,
                                      float partialTick,
                                      int mouseX, int mouseY) {
        // Starlight meter
        float starlightRatio = getStarlightFillRatio();
        if (starlightRatio > 0) {
            int filledHeight = (int) (STARLIGHT_HEIGHT * starlightRatio);
            int yOffset = STARLIGHT_HEIGHT - filledHeight;
            RenderingDrawUtils.drawGradientRect(graphics,
                    this.leftPos + STARLIGHT_X,
                    this.topPos + STARLIGHT_Y + yOffset,
                    STARLIGHT_WIDTH,
                    filledHeight,
                    0x60_6080FF,
                    0xA0_4060FF);
        }

        // Constellation focus glow (when a constellation is focused)
        // Render a subtle pulsing indicator in the top-right area
        float tick = (System.currentTimeMillis() % 2000) / 2000.0f;
        float pulse = 0.5f + 0.5f * (float) Math.sin(tick * Math.PI * 2);
        int focusAlpha = (int) (40 + pulse * 30);
        int focusColor = (focusAlpha << 24) | 0x8060FF;
        RenderingDrawUtils.drawRect(graphics,
                this.leftPos + FOCUS_X, this.topPos + FOCUS_Y,
                FOCUS_SIZE, FOCUS_SIZE, focusColor);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                0xCCCCFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    private float getStarlightFillRatio() {
        float stored = this.menu.getStarlightStored();
        float capacity = this.menu.getStarlightCapacity();
        if (capacity <= 0) return 0;
        return Math.min(1.0f, stored / capacity);
    }
}
