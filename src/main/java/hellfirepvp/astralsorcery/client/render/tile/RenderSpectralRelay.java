/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.common.tile.BlockEntitySpectralRelay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * BER for the Spectral Relay. Renders the inserted Glass Lens as a floating,
 * slowly rotating item above the relay block.
 */
@OnlyIn(Dist.CLIENT)
public class RenderSpectralRelay implements BlockEntityRenderer<BlockEntitySpectralRelay> {

    public RenderSpectralRelay(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntitySpectralRelay relay, float partialTick,
                       @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        ItemStack stack = relay.getInventory().getStackInSlot(0);
        if (stack.isEmpty()) return;

        float ticksExisted = relay.getTicksExisted() + partialTick;
        float bob   = (float) Math.sin(ticksExisted * 0.07) * 0.05f;
        float yRot  = (ticksExisted * 2.5f) % 360f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.55 + bob, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.scale(0.5f, 0.5f, 0.5f);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack, ItemDisplayContext.GROUND,
                packedLight, packedOverlay,
                poseStack, bufferSource, relay.getLevel(), 0);

        poseStack.popPose();
    }
}
