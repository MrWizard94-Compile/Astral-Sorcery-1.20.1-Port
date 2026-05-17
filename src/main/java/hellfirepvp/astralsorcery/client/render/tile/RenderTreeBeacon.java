/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.BlockEntityTreeBeacon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * BER for the Tree Beacon. Renders an upward light beam when charged
 * with starlight, plus a base glow effect.
 *
 * <p>The beam height and brightness scale with the stored starlight
 * ratio. At full charge the beam reaches 32 blocks high and is
 * visible from long range.</p>
 *
 * <p>The base halo has a green-blue nature tint reflecting the
 * growth-boosting function of the beacon.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderTreeBeacon implements BlockEntityRenderer<BlockEntityTreeBeacon> {

    /** Maximum beam height in blocks at full starlight. */
    private static final float MAX_BEAM_HEIGHT = 32.0f;

    /** Number of vertical segments in the beam. */
    private static final int BEAM_SEGMENTS = 8;

    public RenderTreeBeacon(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityTreeBeacon beacon, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        float ratio = beacon.getStarlightRatio();
        if (ratio < 0.01f) return; // Nothing to render if empty

        float tickCount = beacon.getTicksExisted() + partialTick;
        boolean active = beacon.isActive();

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);

        Matrix4f matrix = poseStack.last().pose();

        // Base glow — always present when charged
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        float basePulse = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.05);
        float baseSize = 0.35f + basePulse * 0.05f;
        float baseAlpha = (0.15f + basePulse * 0.1f) * ratio;

        // Nature-tinted halo (green-blue)
        RenderingUtils.vertex(halo, matrix, -baseSize, 0.9f, -baseSize, 0.3f, 0.8f, 0.5f, baseAlpha, 0, 0);
        RenderingUtils.vertex(halo, matrix, -baseSize, 0.9f, baseSize, 0.3f, 0.8f, 0.5f, baseAlpha, 0, 1);
        RenderingUtils.vertex(halo, matrix, baseSize, 0.9f, baseSize, 0.3f, 0.8f, 0.5f, baseAlpha, 1, 1);
        RenderingUtils.vertex(halo, matrix, baseSize, 0.9f, -baseSize, 0.3f, 0.8f, 0.5f, baseAlpha, 1, 0);

        // Upward light beam — only when active with meaningful charge
        if (active && ratio > 0.05f) {
            VertexConsumer beam = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_LIGHTBEAM);

            float beamHeight = MAX_BEAM_HEIGHT * ratio;
            float segmentHeight = beamHeight / BEAM_SEGMENTS;
            float beamWidth = 0.1f;

            for (int i = 0; i < BEAM_SEGMENTS; i++) {
                float segFraction = i / (float) BEAM_SEGMENTS;
                float baseY = 1.0f + segFraction * beamHeight;

                // Narrow and fade toward top
                float segWidth = beamWidth * (1.0f - segFraction * 0.5f);
                float segAlpha = (0.3f + 0.15f * (float) Math.sin(tickCount * 0.06 + i * 0.8))
                        * ratio * (1.0f - segFraction * 0.7f);

                // Subtle wobble
                float wobbleX = 0.01f * (float) Math.sin(tickCount * 0.04 + i * 1.1);
                float wobbleZ = 0.01f * (float) Math.cos(tickCount * 0.035 + i * 1.3);

                // Color shifts from green-white at base to blue-white at top
                float r = 0.4f + 0.4f * segFraction;
                float g = 0.8f - 0.2f * segFraction;
                float b = 0.6f + 0.4f * segFraction;

                // Two crossing quads for volumetric appearance
                RenderingUtils.vertex(beam, matrix, wobbleX - segWidth, baseY, wobbleZ, r, g, b, segAlpha, 0, 0);
                RenderingUtils.vertex(beam, matrix, wobbleX + segWidth, baseY, wobbleZ, r, g, b, segAlpha, 1, 0);
                RenderingUtils.vertex(beam, matrix, wobbleX + segWidth, baseY + segmentHeight, wobbleZ, r, g, b, segAlpha, 1, 1);
                RenderingUtils.vertex(beam, matrix, wobbleX - segWidth, baseY + segmentHeight, wobbleZ, r, g, b, segAlpha, 0, 1);

                RenderingUtils.vertex(beam, matrix, wobbleX, baseY, wobbleZ - segWidth, r, g, b, segAlpha, 0, 0);
                RenderingUtils.vertex(beam, matrix, wobbleX, baseY, wobbleZ + segWidth, r, g, b, segAlpha, 1, 0);
                RenderingUtils.vertex(beam, matrix, wobbleX, baseY + segmentHeight, wobbleZ + segWidth, r, g, b, segAlpha, 1, 1);
                RenderingUtils.vertex(beam, matrix, wobbleX, baseY + segmentHeight, wobbleZ - segWidth, r, g, b, segAlpha, 0, 1);
            }

            // Beam apex glow
            float topY = 1.0f + beamHeight;
            float topSize = 0.15f + 0.05f * (float) Math.sin(tickCount * 0.12);
            float topAlpha = 0.12f * ratio;
            RenderingUtils.vertex(halo, matrix, -topSize, topY, -topSize, 0.7f, 0.9f, 1.0f, topAlpha, 0, 0);
            RenderingUtils.vertex(halo, matrix, -topSize, topY, topSize, 0.7f, 0.9f, 1.0f, topAlpha, 0, 1);
            RenderingUtils.vertex(halo, matrix, topSize, topY, topSize, 0.7f, 0.9f, 1.0f, topAlpha, 1, 1);
            RenderingUtils.vertex(halo, matrix, topSize, topY, -topSize, 0.7f, 0.9f, 1.0f, topAlpha, 1, 0);
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull BlockEntityTreeBeacon p) {
        return true;
    }

    @Override
    public int getViewDistance() {
        // Visible from far away like a beacon
        return 256;
    }
}
