/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.item.base.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Implemented by items that want to draw a custom HUD overlay when equipped.
 * The overlay is rendered once per frame for the first matching equipment slot.
 * 1.16 → 1.20: MatrixStack → GuiGraphics.
 */
@OnlyIn(Dist.CLIENT)
public interface ItemOverlayRender {

    boolean renderOverlay(@Nonnull GuiGraphics graphics, @Nonnull ItemStack stack, float partialTick);
}
