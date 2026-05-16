/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.BlockEntityGateway;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * BER for the Celestial Gateway. Renders a portal swirl effect and ambient glow ring.
 */
@OnlyIn(Dist.CLIENT)
public class RenderGateway implements BlockEntityRenderer<BlockEntityGateway> {

    private static final int SWIRL_SEGMENTS = 8;

    public RenderGateway(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityGateway gateway, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.1, 0.5);

        float tickCount = gateway.getTicksExisted() + partialTick;

        // Portal swirl — rotating particle segments in a flat disc
        VertexConsumer swirl = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE);
        Matrix4f matrix = poseStack.last().pose();
        float swirlRadius = 0.6f;
        float particleSize = 0.08f;
        float swirlSpeed = 0.05f;

        for (int i = 0; i < SWIRL_SEGMENTS; i++) {
            float baseAngle = tickCount * swirlSpeed + i * ((float) Math.PI * 2.0f / SWIRL_SEGMENTS);
            // Spiral inward then outward
            float radialOscillation = swirlRadius + 0.15f * (float) Math.sin(tickCount * 0.03f + i * 0.8f);
            float px = (float) Math.cos(baseAngle) * radialOscillation;
            float pz = (float) Math.sin(baseAngle) * radialOscillation;
            float py = 0.02f * (float) Math.sin(tickCount * 0.07f + i);
            float a = 0.35f + 0.2f * (float) Math.sin(tickCount * 0.06f + i * 1.2f);

            // Purple-blue portal color
            float r = 0.4f + 0.1f * (float) Math.sin(tickCount * 0.04f + i);
            float g = 0.2f + 0.1f * (float) Math.cos(tickCount * 0.05f + i * 0.7f);
            float b = 0.9f;

            RenderingUtils.vertex(swirl, matrix, px - particleSize, py - particleSize, pz, r, g, b, a, 0, 0);
            RenderingUtils.vertex(swirl, matrix, px + particleSize, py - particleSize, pz, r, g, b, a, 1, 0);
            RenderingUtils.vertex(swirl, matrix, px + particleSize, py + particleSize, pz, r, g, b, a, 1, 1);
            RenderingUtils.vertex(swirl, matrix, px - particleSize, py + particleSize, pz, r, g, b, a, 0, 1);
        }

        // Ambient glow ring (flat horizontal halo)
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        float pulse = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.04);
        float ringSize = 0.75f + pulse * 0.05f;
        float ringAlpha = 0.12f + pulse * 0.06f;

        RenderingUtils.vertex(halo, matrix, -ringSize, 0, -ringSize, 0.4f, 0.2f, 0.9f, ringAlpha, 0, 0);
        RenderingUtils.vertex(halo, matrix, -ringSize, 0, ringSize, 0.4f, 0.2f, 0.9f, ringAlpha, 0, 1);
        RenderingUtils.vertex(halo, matrix, ringSize, 0, ringSize, 0.4f, 0.2f, 0.9f, ringAlpha, 1, 1);
        RenderingUtils.vertex(halo, matrix, ringSize, 0, -ringSize, 0.4f, 0.2f, 0.9f, ringAlpha, 1, 0);

        // Central bright spot
        float coreSize = 0.2f + pulse * 0.08f;
        float coreAlpha = 0.2f + pulse * 0.1f;

        RenderingUtils.vertex(halo, matrix, -coreSize, 0.01f, -coreSize, 0.6f, 0.4f, 1.0f, coreAlpha, 0, 0);
        RenderingUtils.vertex(halo, matrix, -coreSize, 0.01f, coreSize, 0.6f, 0.4f, 1.0f, coreAlpha, 0, 1);
        RenderingUtils.vertex(halo, matrix, coreSize, 0.01f, coreSize, 0.6f, 0.4f, 1.0f, coreAlpha, 1, 1);
        RenderingUtils.vertex(halo, matrix, coreSize, 0.01f, -coreSize, 0.6f, 0.4f, 1.0f, coreAlpha, 1, 0);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull BlockEntityGateway p) { return true; }
}
