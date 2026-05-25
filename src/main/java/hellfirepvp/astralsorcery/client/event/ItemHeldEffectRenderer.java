/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.item.base.client.ItemHeldRender;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Iterates all equipment slots and calls {@link ItemHeldRender#renderInHand}
 * on the first item that implements it (first match wins).
 *
 * 1.16 → 1.20: RenderWorldLastEvent → RenderLevelStageEvent (AFTER_PARTICLES),
 * MatrixStack → PoseStack, getItemStackFromSlot → getItemBySlot,
 * EquipmentSlotType → EquipmentSlot.
 */
@OnlyIn(Dist.CLIENT)
public class ItemHeldEffectRenderer {

    public static final ItemHeldEffectRenderer INSTANCE = new ItemHeldEffectRenderer();

    private ItemHeldEffectRenderer() {}

    @SubscribeEvent
    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof ItemHeldRender renderer) {
                if (renderer.renderInHand(stack, poseStack, partialTick)) {
                    return;
                }
            }
        }
    }
}
