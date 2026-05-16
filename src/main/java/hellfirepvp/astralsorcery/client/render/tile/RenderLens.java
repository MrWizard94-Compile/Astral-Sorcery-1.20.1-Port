/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.BlockEntityLens;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * Block entity renderer for the Lens (starlight transmission node).
 *
 * <p>Renders a small translucent crystal lens with a glow effect
 * when the lens is actively transmitting starlight.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderLens implements BlockEntityRenderer<BlockEntityLens> {

    private final BlockEntityRendererProvider.Context context;

    public RenderLens(@Nonnull BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(@Nonnull BlockEntityLens lens,
                        float partialTick,
                        @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource,
                        int packedLight,
                        int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        renderLensBody(lens, partialTick, poseStack, bufferSource, packedLight);

        if (lens.isTransmitting()) {
            renderActiveGlow(lens, partialTick, poseStack, bufferSource);
        }

        poseStack.popPose();
    }

    private void renderLensBody(@Nonnull BlockEntityLens lens,
                                 float partialTick,
                                 @Nonnull PoseStack poseStack,
                                 @Nonnull MultiBufferSource bufferSource,
                                 int packedLight) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_CRYSTAL);
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.15f;
        float r = 0.7f, g = 0.8f, b = 1.0f, a = 0.6f;

        // Diamond shape (two pyramids)
        // Top quad
        RenderingUtils.vertex(buffer, matrix, -size, 0, -size, r, g, b, a, 0, 0, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);
        RenderingUtils.vertex(buffer, matrix, size, 0, -size, r, g, b, a, 1, 0, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);

        // Bottom quad
        RenderingUtils.vertex(buffer, matrix, -size, 0, size, r, g, b, a, 0, 1, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, -size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);
        RenderingUtils.vertex(buffer, matrix, size, 0, size, r, g, b, a, 1, 1, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, -size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);
    }

    private void renderActiveGlow(@Nonnull BlockEntityLens lens,
                                   float partialTick,
                                   @Nonnull PoseStack poseStack,
                                   @Nonnull MultiBufferSource bufferSource) {
        float tickCount = lens.getTicksExisted() + partialTick;
        float pulse = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.1);

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.15f + pulse * 0.08f;
        float glowAlpha = 0.2f + pulse * 0.15f;

        RenderingUtils.vertex(buffer, matrix, -size, -size, 0, 0.6f, 0.8f, 1.0f, glowAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, size, -size, 0, 0.6f, 0.8f, 1.0f, glowAlpha, 1, 0);
        RenderingUtils.vertex(buffer, matrix, size, size, 0, 0.6f, 0.8f, 1.0f, glowAlpha, 1, 1);
        RenderingUtils.vertex(buffer, matrix, -size, size, 0, 0.6f, 0.8f, 1.0f, glowAlpha, 0, 1);
    }
}
