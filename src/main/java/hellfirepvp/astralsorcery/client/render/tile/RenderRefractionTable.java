/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.common.tile.BlockEntityRefractionTable;
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
 * BER for the Refraction Table. The static block model renders the frame geometry.
 * This BER adds:
 *  - the inserted input item, floating above the mirror surface
 *  - the infused glass pane, hovering slightly higher when present
 */
@OnlyIn(Dist.CLIENT)
public class RenderRefractionTable implements BlockEntityRenderer<BlockEntityRefractionTable> {

    public RenderRefractionTable(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityRefractionTable tile, float partialTick,
                       @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        float ticks = tile.getTicksExisted() + partialTick;

        // Input item — render only when there is no parchment and there is an input
        ItemStack input = tile.getInputStack();
        if (!tile.hasParchment() && !input.isEmpty()) {
            float bob  = (float) Math.sin(ticks * 0.06) * 0.04f;
            float yRot = (ticks * 1.8f) % 360f;

            poseStack.pushPose();
            poseStack.translate(0.5, 1.15 + bob, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.scale(0.625f, 0.625f, 0.625f);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    input, ItemDisplayContext.GROUND,
                    packedLight, packedOverlay,
                    poseStack, bufferSource, tile.getLevel(), 0);

            poseStack.popPose();
        }

        // Glass pane — floats slightly higher, slower spin
        ItemStack glass = tile.getGlassStack();
        if (!glass.isEmpty()) {
            float bob  = (float) Math.sin(ticks * 0.05 + 1.0) * 0.03f;
            float yRot = (ticks * 1.2f) % 360f;

            poseStack.pushPose();
            poseStack.translate(0.5, 1.4 + bob, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.scale(0.5f, 0.5f, 0.5f);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    glass, ItemDisplayContext.GROUND,
                    packedLight, packedOverlay,
                    poseStack, bufferSource, tile.getLevel(), 1);

            poseStack.popPose();
        }
    }
}
