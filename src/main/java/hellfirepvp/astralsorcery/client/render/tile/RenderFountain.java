/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.BlockEntityFountain;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * BER for the Fountain. Renders an upward liquid starlight stream and basin glow
 * when the structure is valid and contains fluid.
 */
@OnlyIn(Dist.CLIENT)
public class RenderFountain implements BlockEntityRenderer<BlockEntityFountain> {

    private static final int STREAM_SEGMENTS = 6;

    public RenderFountain(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityFountain fountain, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        if (!fountain.isStructureValid()) return;

        int fluidAmount = fountain.getTank().getFluidAmount();
        if (fluidAmount <= 0) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);

        float tickCount = fountain.getTicksExisted() + partialTick;
        float fillRatio = Math.min(1.0f, fluidAmount / 16000.0f);

        // Upward liquid starlight stream — series of quads rising from basin
        VertexConsumer stream = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE);
        Matrix4f matrix = poseStack.last().pose();
        float streamHeight = 1.5f + fillRatio * 1.0f;
        float streamWidth = 0.06f;

        for (int i = 0; i < STREAM_SEGMENTS; i++) {
            float segFraction = i / (float) STREAM_SEGMENTS;
            float baseY = 0.8f + segFraction * streamHeight;
            float segHeight = streamHeight / STREAM_SEGMENTS;

            // Wobble the stream slightly
            float wobbleX = 0.02f * (float) Math.sin(tickCount * 0.08f + i * 1.5f);
            float wobbleZ = 0.02f * (float) Math.cos(tickCount * 0.07f + i * 1.3f);

            // Fade out toward top
            float a = (0.5f + 0.2f * fillRatio) * (1.0f - segFraction * 0.6f);
            // Starlight blue-white color
            float r = 0.6f + 0.2f * segFraction;
            float g = 0.7f + 0.2f * segFraction;
            float b = 1.0f;

            // Two crossing quads for volumetric appearance
            RenderingUtils.vertex(stream, matrix, wobbleX - streamWidth, baseY, wobbleZ, r, g, b, a, 0, 0);
            RenderingUtils.vertex(stream, matrix, wobbleX + streamWidth, baseY, wobbleZ, r, g, b, a, 1, 0);
            RenderingUtils.vertex(stream, matrix, wobbleX + streamWidth, baseY + segHeight, wobbleZ, r, g, b, a, 1, 1);
            RenderingUtils.vertex(stream, matrix, wobbleX - streamWidth, baseY + segHeight, wobbleZ, r, g, b, a, 0, 1);

            RenderingUtils.vertex(stream, matrix, wobbleX, baseY, wobbleZ - streamWidth, r, g, b, a, 0, 0);
            RenderingUtils.vertex(stream, matrix, wobbleX, baseY, wobbleZ + streamWidth, r, g, b, a, 1, 0);
            RenderingUtils.vertex(stream, matrix, wobbleX, baseY + segHeight, wobbleZ + streamWidth, r, g, b, a, 1, 1);
            RenderingUtils.vertex(stream, matrix, wobbleX, baseY + segHeight, wobbleZ - streamWidth, r, g, b, a, 0, 1);
        }

        // Basin glow — flat horizontal halo at liquid surface
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        float pulse = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.05);
        float basinSize = 0.4f + pulse * 0.05f;
        float basinAlpha = (0.15f + pulse * 0.1f) * fillRatio;

        RenderingUtils.vertex(halo, matrix, -basinSize, 0.75f, -basinSize, 0.5f, 0.7f, 1.0f, basinAlpha, 0, 0);
        RenderingUtils.vertex(halo, matrix, -basinSize, 0.75f, basinSize, 0.5f, 0.7f, 1.0f, basinAlpha, 0, 1);
        RenderingUtils.vertex(halo, matrix, basinSize, 0.75f, basinSize, 0.5f, 0.7f, 1.0f, basinAlpha, 1, 1);
        RenderingUtils.vertex(halo, matrix, basinSize, 0.75f, -basinSize, 0.5f, 0.7f, 1.0f, basinAlpha, 1, 0);

        // Top splash effect — small glow at stream apex
        float topY = 0.8f + streamHeight;
        float splashSize = 0.12f + 0.04f * (float) Math.sin(tickCount * 0.1f);
        float splashAlpha = 0.2f * fillRatio;

        RenderingUtils.vertex(halo, matrix, -splashSize, topY, -splashSize, 0.8f, 0.9f, 1.0f, splashAlpha, 0, 0);
        RenderingUtils.vertex(halo, matrix, -splashSize, topY, splashSize, 0.8f, 0.9f, 1.0f, splashAlpha, 0, 1);
        RenderingUtils.vertex(halo, matrix, splashSize, topY, splashSize, 0.8f, 0.9f, 1.0f, splashAlpha, 1, 1);
        RenderingUtils.vertex(halo, matrix, splashSize, topY, -splashSize, 0.8f, 0.9f, 1.0f, splashAlpha, 1, 0);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull BlockEntityFountain p) { return true; }
}
