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
import hellfirepvp.astralsorcery.client.render.CrystalModelRenderer;
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
import java.awt.Color;

/**
 * Block entity renderer for the Lens (starlight transmission node).
 *
 * <p>Renders:
 * <ul>
 *   <li>Diamond-shaped lens body</li>
 *   <li>Inserted crystal floating above the lens (if one is socketed)</li>
 *   <li>Pulsing glow when actively transmitting</li>
 * </ul></p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderLens implements BlockEntityRenderer<BlockEntityLens> {

    public RenderLens(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityLens lens,
                        float partialTick,
                        @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource,
                        int packedLight,
                        int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float tickCount = lens.getTicksExisted() + partialTick;

        renderLensBody(poseStack, bufferSource, packedLight);

        if (lens.isTransmitting()) {
            renderActiveGlow(tickCount, poseStack, bufferSource);
        }

        // Show inserted crystal floating above the lens
        if (!lens.getHeldCrystal().isEmpty()) {
            renderHeldCrystal(tickCount, poseStack, bufferSource, packedLight);
        }

        poseStack.popPose();
    }

    private void renderLensBody(@Nonnull PoseStack poseStack,
                                 @Nonnull MultiBufferSource bufferSource,
                                 int packedLight) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_CRYSTAL);
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.15f;
        float r = 0.7f, g = 0.8f, b = 1.0f, a = 0.6f;

        // Diamond shape — four triangular faces as degenerate quads
        RenderingUtils.vertex(buffer, matrix, -size, 0, -size, r, g, b, a, 0, 0, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);
        RenderingUtils.vertex(buffer, matrix, size, 0, -size, r, g, b, a, 1, 0, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);

        RenderingUtils.vertex(buffer, matrix, size, 0, -size, r, g, b, a, 1, 0, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);
        RenderingUtils.vertex(buffer, matrix, size, 0, size, r, g, b, a, 1, 1, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);

        RenderingUtils.vertex(buffer, matrix, size, 0, size, r, g, b, a, 1, 1, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);
        RenderingUtils.vertex(buffer, matrix, -size, 0, size, r, g, b, a, 0, 1, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);

        RenderingUtils.vertex(buffer, matrix, -size, 0, size, r, g, b, a, 0, 1, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);
        RenderingUtils.vertex(buffer, matrix, -size, 0, -size, r, g, b, a, 0, 0, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);

        // Bottom half
        RenderingUtils.vertex(buffer, matrix, -size, 0, -size, r, g, b, a, 0, 0, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, -size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);
        RenderingUtils.vertex(buffer, matrix, size, 0, -size, r, g, b, a, 1, 0, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, -size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);

        RenderingUtils.vertex(buffer, matrix, size, 0, -size, r, g, b, a, 1, 0, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, -size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);
        RenderingUtils.vertex(buffer, matrix, size, 0, size, r, g, b, a, 1, 1, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, -size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);

        RenderingUtils.vertex(buffer, matrix, size, 0, size, r, g, b, a, 1, 1, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, -size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);
        RenderingUtils.vertex(buffer, matrix, -size, 0, size, r, g, b, a, 0, 1, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, -size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);

        RenderingUtils.vertex(buffer, matrix, -size, 0, size, r, g, b, a, 0, 1, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, -size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);
        RenderingUtils.vertex(buffer, matrix, -size, 0, -size, r, g, b, a, 0, 0, packedLight);
        RenderingUtils.vertex(buffer, matrix, 0, -size * 1.5f, 0, r, g, b, a, 0.5f, 0.5f, packedLight);
    }

    private void renderActiveGlow(float tickCount,
                                   @Nonnull PoseStack poseStack,
                                   @Nonnull MultiBufferSource bufferSource) {
        float pulse = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.1);

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.15f + pulse * 0.08f;
        float a    = 0.2f  + pulse * 0.15f;

        RenderingUtils.vertex(buffer, matrix, -size, -size, 0, 0.6f, 0.8f, 1.0f, a, 0, 0);
        RenderingUtils.vertex(buffer, matrix,  size, -size, 0, 0.6f, 0.8f, 1.0f, a, 1, 0);
        RenderingUtils.vertex(buffer, matrix,  size,  size, 0, 0.6f, 0.8f, 1.0f, a, 1, 1);
        RenderingUtils.vertex(buffer, matrix, -size,  size, 0, 0.6f, 0.8f, 1.0f, a, 0, 1);
    }

    /** Small crystal socketed above the lens body. */
    private void renderHeldCrystal(float tickCount,
                                    @Nonnull PoseStack poseStack,
                                    @Nonnull MultiBufferSource bufferSource,
                                    int packedLight) {
        float bob = (float) Math.sin(tickCount * 0.06) * 0.03f;
        poseStack.pushPose();
        poseStack.translate(0, 0.3f + bob, 0);
        CrystalModelRenderer.renderCrystal(poseStack, bufferSource,
                new Color(160, 200, 255), 0.75f,
                tickCount * 0.6f, 0.55f, packedLight);
        poseStack.popPose();
    }
}
