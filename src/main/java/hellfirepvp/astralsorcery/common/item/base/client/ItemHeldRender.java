/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.item.base.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Implemented by items that render a custom world-space effect when held.
 * Called once per frame (first matching equipment slot wins) via
 * {@link hellfirepvp.astralsorcery.client.event.ItemHeldEffectRenderer}.
 *
 * 1.16 → 1.20: MatrixStack → PoseStack.
 */
public interface ItemHeldRender {

    @OnlyIn(Dist.CLIENT)
    boolean renderInHand(ItemStack stack, PoseStack poseStack, float partialTick);
}
