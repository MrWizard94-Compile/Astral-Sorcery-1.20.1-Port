/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.BlockEntityRelay;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * BER for the Spectral Relay. Renders orbiting particle effect and ambient glow.
 */
@OnlyIn(Dist.CLIENT)
public class RenderRelay implements BlockEntityRenderer<BlockEntityRelay> {

    public RenderRelay(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityRelay relay, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.8, 0.5);

        float tickCount = relay.getTicksExisted() + partialTick;

        // Orbiting glow particles (4 quads in circular orbit)
        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE);
        Matrix4f matrix = poseStack.last().pose();
        float orbitRadius = 0.3f;
        float particleSize = 0.06f;
        float orbitSpeed = 0.04f;

        for (int i = 0; i < 4; i++) {
            float angle = tickCount * orbitSpeed + i * (float) Math.PI * 0.5f;
            float px = (float) Math.cos(angle) * orbitRadius;
            float pz = (float) Math.sin(angle) * orbitRadius;
            float py = 0.1f * (float) Math.sin(tickCount * 0.06f + i);
            float a = 0.4f + 0.2f * (float) Math.sin(tickCount * 0.08f + i * 1.5f);

            RenderingUtils.vertex(buffer, matrix, px - particleSize, py - particleSize, pz, 0.5f, 0.6f, 1.0f, a, 0, 0);
            RenderingUtils.vertex(buffer, matrix, px + particleSize, py - particleSize, pz, 0.5f, 0.6f, 1.0f, a, 1, 0);
            RenderingUtils.vertex(buffer, matrix, px + particleSize, py + particleSize, pz, 0.5f, 0.6f, 1.0f, a, 1, 1);
            RenderingUtils.vertex(buffer, matrix, px - particleSize, py + particleSize, pz, 0.5f, 0.6f, 1.0f, a, 0, 1);
        }

        // Central glow
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        float pulse = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.05);
        float size = 0.15f + pulse * 0.05f;
        float haloA = 0.15f + pulse * 0.1f;

        RenderingUtils.vertex(halo, matrix, -size, -size, 0, 0.5f, 0.6f, 1.0f, haloA, 0, 0);
        RenderingUtils.vertex(halo, matrix, size, -size, 0, 0.5f, 0.6f, 1.0f, haloA, 1, 0);
        RenderingUtils.vertex(halo, matrix, size, size, 0, 0.5f, 0.6f, 1.0f, haloA, 1, 1);
        RenderingUtils.vertex(halo, matrix, -size, size, 0, 0.5f, 0.6f, 1.0f, haloA, 0, 1);

        poseStack.popPose();
    }
}
