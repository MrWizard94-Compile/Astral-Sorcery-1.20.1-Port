/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.container.ContainerAltarRadiance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Screen for the Radiance-tier altar (Iridescent Altar).
 * The highest tier. Displays the full 5x5 grid (25 active slots)
 * with relay slots, trait constellation focus, and dual starlight meters.
 */
@OnlyIn(Dist.CLIENT)
public class ScreenAltarRadiance extends ScreenContainerBaseAS<ContainerAltarRadiance> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AstralSorcery.MODID, "textures/gui/container_altar_radiance.png");

    private static final int BG_WIDTH = 176;
    private static final int BG_HEIGHT = 166;

    // Primary starlight meter (left side)
    private static final int STARLIGHT_X = 5;
    private static final int STARLIGHT_Y = 8;
    private static final int STARLIGHT_WIDTH = 8;
    private static final int STARLIGHT_HEIGHT = 65;

    // Secondary meter (right side — trait constellation energy)
    private static final int TRAIT_X = 163;
    private static final int TRAIT_Y = 8;
    private static final int TRAIT_WIDTH = 8;
    private static final int TRAIT_HEIGHT = 65;

    public ScreenAltarRadiance(@Nonnull ContainerAltarRadiance menu,
                                @Nonnull Inventory playerInventory,
                                @Nonnull Component title) {
        super(menu, playerInventory, title, TEXTURE, BG_WIDTH, BG_HEIGHT);
    }

    @Override
    protected void renderContainerBg(@Nonnull GuiGraphics graphics,
                                      float partialTick,
                                      int mouseX, int mouseY) {
        // Primary starlight meter
        float starlightRatio = getStarlightFillRatio();
        if (starlightRatio > 0) {
            int filledHeight = (int) (STARLIGHT_HEIGHT * starlightRatio);
            int yOffset = STARLIGHT_HEIGHT - filledHeight;
            RenderingDrawUtils.drawGradientRect(graphics,
                    this.leftPos + STARLIGHT_X,
                    this.topPos + STARLIGHT_Y + yOffset,
                    STARLIGHT_WIDTH,
                    filledHeight,
                    0x70_7090FF,
                    0xB0_5070FF);
        }

        // Trait constellation energy meter (mirrors primary on right side)
        // For now renders at same ratio — will be separate when trait data is synced
        if (starlightRatio > 0) {
            int filledHeight = (int) (TRAIT_HEIGHT * starlightRatio * 0.8f);
            int yOffset = TRAIT_HEIGHT - filledHeight;
            RenderingDrawUtils.drawGradientRect(graphics,
                    this.leftPos + TRAIT_X,
                    this.topPos + TRAIT_Y + yOffset,
                    TRAIT_WIDTH,
                    filledHeight,
                    0x60_FF90FF,
                    0xA0_C060FF);
        }

        // Radiance shimmer overlay — subtle animated effect
        float tick = (System.currentTimeMillis() % 3000) / 3000.0f;
        float shimmer = 0.5f + 0.5f * (float) Math.sin(tick * Math.PI * 2);
        int shimmerAlpha = (int) (10 + shimmer * 15);
        int shimmerColor = (shimmerAlpha << 24) | 0xFFFFFF;
        RenderingDrawUtils.drawRect(graphics,
                this.leftPos + 20, this.topPos + 5,
                BG_WIDTH - 40, BG_HEIGHT - 80, shimmerColor);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                0xEECCFF, false);
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
