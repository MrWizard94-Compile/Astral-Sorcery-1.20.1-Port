/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.container.ContainerAltarDiscovery;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Screen for the Discovery-tier altar (Luminous Crafting Table).
 * Displays a 3x3 crafting grid plus a starlight meter and output slot.
 *
 * <p>1.16 → 1.20: Uses GuiGraphics for rendering, extends the
 * ScreenContainerBaseAS framework.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ScreenAltarDiscovery extends ScreenContainerBaseAS<ContainerAltarDiscovery> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AstralSorcery.MODID, "textures/gui/container_altar_discovery.png");

    private static final int BG_WIDTH = 176;
    private static final int BG_HEIGHT = 166;

    // Starlight meter position (relative to leftPos/topPos)
    private static final int STARLIGHT_X = 8;
    private static final int STARLIGHT_Y = 18;
    private static final int STARLIGHT_WIDTH = 10;
    private static final int STARLIGHT_HEIGHT = 50;

    public ScreenAltarDiscovery(@Nonnull ContainerAltarDiscovery menu,
                                 @Nonnull Inventory playerInventory,
                                 @Nonnull Component title) {
        super(menu, playerInventory, title, TEXTURE, BG_WIDTH, BG_HEIGHT);
    }

    @Override
    protected void renderContainerBg(@Nonnull GuiGraphics graphics,
                                      float partialTick,
                                      int mouseX, int mouseY) {
        // Render starlight level meter
        float starlightRatio = getStarlightFillRatio();
        if (starlightRatio > 0) {
            int filledHeight = (int) (STARLIGHT_HEIGHT * starlightRatio);
            int yOffset = STARLIGHT_HEIGHT - filledHeight;

            // Blue gradient fill from bottom to current level
            RenderingDrawUtils.drawGradientRect(graphics,
                    this.leftPos + STARLIGHT_X,
                    this.topPos + STARLIGHT_Y + yOffset,
                    STARLIGHT_WIDTH,
                    filledHeight,
                    0x40_4080FF,  // top color (lighter blue)
                    0x80_2040FF); // bottom color (deeper blue)
        }
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        // Title centered above crafting grid
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                0xCCCCFF, false);
        // Player inventory label
        graphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    /**
     * Get the current starlight fill ratio from the container's altar data.
     *
     * @return fill ratio [0, 1]
     */
    private float getStarlightFillRatio() {
        float stored = this.menu.getStarlightStored();
        float capacity = this.menu.getStarlightCapacity();
        if (capacity <= 0) return 0;
        return Math.min(1.0f, stored / capacity);
    }
}
