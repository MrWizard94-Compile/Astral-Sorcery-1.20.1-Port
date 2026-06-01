/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.container.ContainerAltarAttunement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Screen for the Attunement-tier altar (Starlight Crafting Altar).
 * Displays a 5x5 crafting grid (13 active slots) plus starlight meter.
 */
@OnlyIn(Dist.CLIENT)
public class ScreenAltarAttunement extends ScreenContainerBaseAS<ContainerAltarAttunement> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AstralSorcery.MODID, "textures/gui/container_altar_attunement.png");

    private static final int BG_WIDTH = 176;
    private static final int BG_HEIGHT = 166;

    private static final int STARLIGHT_X = 8;
    private static final int STARLIGHT_Y = 13;
    private static final int STARLIGHT_WIDTH = 10;
    private static final int STARLIGHT_HEIGHT = 58;

    public ScreenAltarAttunement(@Nonnull ContainerAltarAttunement menu,
                                  @Nonnull Inventory playerInventory,
                                  @Nonnull Component title) {
        super(menu, playerInventory, title, TEXTURE, BG_WIDTH, BG_HEIGHT);
    }

    @Override
    protected void renderContainerBg(@Nonnull GuiGraphics graphics,
                                      float partialTick,
                                      int mouseX, int mouseY) {
        float starlightRatio = getStarlightFillRatio();
        if (starlightRatio > 0) {
            int filledHeight = (int) (STARLIGHT_HEIGHT * starlightRatio);
            int yOffset = STARLIGHT_HEIGHT - filledHeight;
            RenderingDrawUtils.drawGradientRect(graphics,
                    this.leftPos + STARLIGHT_X,
                    this.topPos + STARLIGHT_Y + yOffset,
                    STARLIGHT_WIDTH,
                    filledHeight,
                    0x50_5090FF,
                    0x90_3050FF);
        }
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
