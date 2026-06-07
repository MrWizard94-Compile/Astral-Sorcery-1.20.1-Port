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
import hellfirepvp.astralsorcery.common.tile.BlockEntityWell;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * Block entity renderer for the Lightwell.
 *
 * <p>Renders:
 * <ul>
 *   <li>Fluid level inside the well basin</li>
 *   <li>Starlight sparkle particles rising from the catalyst</li>
 *   <li>Glow effect on the catalyst item</li>
 * </ul></p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderWell implements BlockEntityRenderer<BlockEntityWell> {

    public RenderWell(@Nonnull BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@Nonnull BlockEntityWell well,
                        float partialTick,
                        @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource,
                        int packedLight,
                        int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);

        renderFluidLevel(well, partialTick, poseStack, bufferSource);
        renderCatalystGlow(well, partialTick, poseStack, bufferSource);

        poseStack.popPose();
    }

    /**
     * Render the liquid starlight level inside the well.
     */
    private void renderFluidLevel(@Nonnull BlockEntityWell well,
                                   float partialTick,
                                   @Nonnull PoseStack poseStack,
                                   @Nonnull MultiBufferSource bufferSource) {
        float fluidLevel = well.getFluidFillRatio();
        if (fluidLevel <= 0.01f) return;

        // Fluid surface height: from 0.25 (empty bottom) to 0.85 (full)
        float surfaceY = 0.25f + fluidLevel * 0.6f;

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_CRYSTAL);
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.35f; // well interior radius
        float r = 0.4f, g = 0.5f, b = 0.9f;
        float a = 0.6f + fluidLevel * 0.2f;

        // Fluid surface quad
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
     * Render glow effect around the catalyst item.
     */
    private void renderCatalystGlow(@Nonnull BlockEntityWell well,
                                     float partialTick,
                                     @Nonnull PoseStack poseStack,
                                     @Nonnull MultiBufferSource bufferSource) {
        if (!well.hasCatalyst()) return;

        float tickCount = well.getTicksExisted() + partialTick;
        float pulse = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.08);
        float glowAlpha = 0.15f + pulse * 0.2f;

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.2f + pulse * 0.05f;
        float y = 0.9f; // just above the well edge

        // Horizontal glow quad
        RenderingUtils.vertex(buffer, matrix, -size, y, -size, 0.5f, 0.6f, 1.0f, glowAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, -size, y, size, 0.5f, 0.6f, 1.0f, glowAlpha, 0, 1);
        RenderingUtils.vertex(buffer, matrix, size, y, size, 0.5f, 0.6f, 1.0f, glowAlpha, 1, 1);
        RenderingUtils.vertex(buffer, matrix, size, y, -size, 0.5f, 0.6f, 1.0f, glowAlpha, 1, 0);
    }
}
