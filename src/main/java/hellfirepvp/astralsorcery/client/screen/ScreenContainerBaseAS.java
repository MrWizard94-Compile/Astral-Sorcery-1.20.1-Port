/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Base class for Astral Sorcery container/menu screens (altar crafting,
 * infuser, etc.). Extends Minecraft's container screen system.
 *
 * <p>1.16 → 1.20:
 * {@code ContainerScreen<T>} → {@code AbstractContainerScreen<T>}.
 * {@code PlayerInventory} → {@code Inventory}.
 * {@code render(MatrixStack, ...)} → {@code render(GuiGraphics, ...)}.</p>
 *
 * @param <T> the menu type
 */
@OnlyIn(Dist.CLIENT)
public abstract class ScreenContainerBaseAS<T extends AbstractContainerMenu>
        extends AbstractContainerScreen<T> {

    @Nonnull
    protected final ResourceLocation backgroundTexture;

    protected ScreenContainerBaseAS(@Nonnull T menu,
                                     @Nonnull Inventory playerInventory,
                                     @Nonnull Component title,
                                     @Nonnull ResourceLocation backgroundTexture,
                                     int bgWidth, int bgHeight) {
        super(menu, playerInventory, title);
        this.backgroundTexture = backgroundTexture;
        this.imageWidth = bgWidth;
        this.imageHeight = bgHeight;
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTick,
                             int mouseX, int mouseY) {
        RenderingDrawUtils.drawTexturedRect(graphics, backgroundTexture,
                this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        renderContainerBg(graphics, partialTick, mouseX, mouseY);
    }

    /**
     * Subclasses implement to render additional background elements
     * (starlight meter, recipe preview, etc.).
     */
    protected abstract void renderContainerBg(@Nonnull GuiGraphics graphics,
                                                float partialTick,
                                                int mouseX, int mouseY);

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    /**
     * Test if the mouse is within a rectangular region relative to leftPos/topPos.
     */
    protected boolean isMouseInRegion(int mouseX, int mouseY,
                                       int x, int y, int width, int height) {
        int absX = this.leftPos + x;
        int absY = this.topPos + y;
        return mouseX >= absX && mouseX < absX + width
                && mouseY >= absY && mouseY < absY + height;
    }
}
