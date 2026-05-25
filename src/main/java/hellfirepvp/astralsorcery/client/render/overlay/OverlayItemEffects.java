/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.overlay;

import hellfirepvp.astralsorcery.common.item.base.client.ItemOverlayRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import javax.annotation.Nonnull;

/**
 * Iterates all equipment slots and calls {@link ItemOverlayRender#renderOverlay}
 * on the first item that implements it. Only one overlay renders per frame
 * (first match wins, in EquipmentSlot order).
 *
 * 1.16 → 1.20: RenderGameOverlayEvent → IGuiOverlay,
 * EquipmentSlotType → EquipmentSlot, getItemStackFromSlot → getItemBySlot.
 */
@OnlyIn(Dist.CLIENT)
public class OverlayItemEffects implements IGuiOverlay {

    public static final OverlayItemEffects INSTANCE = new OverlayItemEffects();

    private OverlayItemEffects() {}

    @Override
    public void render(@Nonnull ForgeGui gui, @Nonnull GuiGraphics graphics,
                        float partialTick, int screenWidth, int screenHeight) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();
            if (item instanceof ItemOverlayRender renderer) {
                if (renderer.renderOverlay(graphics, stack, partialTick)) {
                    return;
                }
            }
        }
    }
}
