/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.BlockEntityObservatory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * BER for the Observatory. Renders a lens glow effect and a subtle
 * rotating starfield overlay when the observatory is in use.
 *
 * <p>When not in use, renders a dim idle glow at the eyepiece.
 * When in use, the glow brightens and a constellation overlay
 * appears above the block.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderObservatory implements BlockEntityRenderer<BlockEntityObservatory> {

    public RenderObservatory(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityObservatory observatory, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        float tickCount = observatory.getTicksExisted() + partialTick;
        boolean inUse = observatory.isInUse();

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);

        // Eyepiece lens glow
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        Matrix4f matrix = poseStack.last().pose();

        float lensAlpha = inUse
                ? 0.4f + 0.15f * (float) Math.sin(tickCount * 0.08)
                : 0.1f + 0.05f * (float) Math.sin(tickCount * 0.04);
        float lensSize = inUse ? 0.18f : 0.12f;

        // Lens glow at top of telescope body
        float lensY = 1.1f;
        RenderingUtils.vertex(halo, matrix, -lensSize, lensY, -lensSize, 0.4f, 0.6f, 1.0f, lensAlpha, 0, 0);
        RenderingUtils.vertex(halo, matrix, -lensSize, lensY, lensSize, 0.4f, 0.6f, 1.0f, lensAlpha, 0, 1);
        RenderingUtils.vertex(halo, matrix, lensSize, lensY, lensSize, 0.4f, 0.6f, 1.0f, lensAlpha, 1, 1);
        RenderingUtils.vertex(halo, matrix, lensSize, lensY, -lensSize, 0.4f, 0.6f, 1.0f, lensAlpha, 1, 0);

        if (inUse) {
            // Active starfield overlay above observatory
            poseStack.pushPose();
            poseStack.translate(0, 2.0, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(tickCount * 0.3f));

            VertexConsumer stars = bufferSource.getBuffer(RenderTypesAS.CONSTELLATION_STAR);
            Matrix4f starMatrix = poseStack.last().pose();

            // Four decorative star points around the lens
            float starSize = 0.04f;
            float starAlpha = 0.5f + 0.2f * (float) Math.sin(tickCount * 0.1);
            float[][] starOffsets = {
                    { 0.3f, 0, 0 }, { -0.3f, 0, 0 },
                    { 0, 0, 0.3f }, { 0, 0, -0.3f },
                    { 0.2f, 0.1f, 0.2f }, { -0.2f, -0.1f, -0.2f },
                    { 0.15f, 0.15f, -0.15f }, { -0.15f, -0.15f, 0.15f }
            };

            for (float[] offset : starOffsets) {
                float sx = offset[0];
                float sy = offset[1];
                float sz = offset[2];
                RenderingUtils.vertex(stars, starMatrix, sx - starSize, sy - starSize, sz, 0.8f, 0.85f, 1.0f, starAlpha, 0, 0);
                RenderingUtils.vertex(stars, starMatrix, sx - starSize, sy + starSize, sz, 0.8f, 0.85f, 1.0f, starAlpha, 0, 1);
                RenderingUtils.vertex(stars, starMatrix, sx + starSize, sy + starSize, sz, 0.8f, 0.85f, 1.0f, starAlpha, 1, 1);
                RenderingUtils.vertex(stars, starMatrix, sx + starSize, sy - starSize, sz, 0.8f, 0.85f, 1.0f, starAlpha, 1, 0);
            }

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull BlockEntityObservatory p) {
        return true;
    }
}
