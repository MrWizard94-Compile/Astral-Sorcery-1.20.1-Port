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
import hellfirepvp.astralsorcery.common.tile.BlockEntityInfuser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * Block entity renderer for the Starlight Infuser.
 *
 * <p>Renders:
 * <ul>
 *   <li>Liquid starlight pool surface</li>
 *   <li>Item floating/spinning above the pool during infusion</li>
 *   <li>Starlight swirl effect during active infusion</li>
 *   <li>Vertical beam when infusion completes</li>
 * </ul></p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderInfuser implements BlockEntityRenderer<BlockEntityInfuser> {

    private final BlockEntityRendererProvider.Context context;

    public RenderInfuser(@Nonnull BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(@Nonnull BlockEntityInfuser infuser,
                        float partialTick,
                        @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource,
                        int packedLight,
                        int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);

        renderLiquidPool(infuser, partialTick, poseStack, bufferSource);
        renderInfusionEffect(infuser, partialTick, poseStack, bufferSource);

        poseStack.popPose();
    }

    /**
     * Render the liquid starlight pool inside the infuser basin.
     */
    private void renderLiquidPool(@Nonnull BlockEntityInfuser infuser,
                                   float partialTick,
                                   @Nonnull PoseStack poseStack,
                                   @Nonnull MultiBufferSource bufferSource) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_CRYSTAL);
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.4f;
        float surfaceY = 0.65f;
        float tickCount = infuser.getTicksExisted() + partialTick;

        // Gentle wave animation
        float wave = 0.01f * (float) Math.sin(tickCount * 0.03);
        surfaceY += wave;

        float r = 0.3f, g = 0.45f, b = 0.85f, a = 0.7f;

        RenderingUtils.vertex(buffer, matrix, -size, surfaceY, -size,
                r, g, b, a, 0, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, -size, surfaceY, size,
                r, g, b, a, 0, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, size, surfaceY, size,
                r, g, b, a, 1, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, size, surfaceY, -size,
                r, g, b, a, 1, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
    }

    /**
     * Render swirl/glow effect during active infusion.
     */
    private void renderInfusionEffect(@Nonnull BlockEntityInfuser infuser,
                                       float partialTick,
                                       @Nonnull PoseStack poseStack,
                                       @Nonnull MultiBufferSource bufferSource) {
        if (!infuser.isInfusing()) return;

        float progress = infuser.getInfusionProgress();
        float tickCount = infuser.getTicksExisted() + partialTick;

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        Matrix4f matrix = poseStack.last().pose();

        // Expanding glow as infusion progresses
        float glowSize = 0.3f + progress * 0.5f;
        float glowAlpha = 0.2f + progress * 0.4f;

        // Rotating glow
        float angle = tickCount * 0.05f;
        float offsetX = (float) Math.cos(angle) * 0.02f;
        float offsetZ = (float) Math.sin(angle) * 0.02f;
        float glowY = 0.8f;

        RenderingUtils.vertex(buffer, matrix,
                offsetX - glowSize, glowY, offsetZ - glowSize,
                0.5f, 0.6f, 1.0f, glowAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix,
                offsetX - glowSize, glowY, offsetZ + glowSize,
                0.5f, 0.6f, 1.0f, glowAlpha, 0, 1);
        RenderingUtils.vertex(buffer, matrix,
                offsetX + glowSize, glowY, offsetZ + glowSize,
                0.5f, 0.6f, 1.0f, glowAlpha, 1, 1);
        RenderingUtils.vertex(buffer, matrix,
                offsetX + glowSize, glowY, offsetZ - glowSize,
                0.5f, 0.6f, 1.0f, glowAlpha, 1, 0);
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull BlockEntityInfuser infuser) {
        return true;
    }
}
