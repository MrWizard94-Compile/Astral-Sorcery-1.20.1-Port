/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.BlockEntityPrism;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * BER for the Prism. Renders prismatic crystal body with rainbow-tint glow when transmitting.
 */
@OnlyIn(Dist.CLIENT)
public class RenderPrism implements BlockEntityRenderer<BlockEntityPrism> {

    public RenderPrism(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityPrism prism, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float tickCount = prism.getTicksExisted() + partialTick;
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(tickCount * 0.3f));

        VertexConsumer crystal = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_CRYSTAL);
        Matrix4f matrix = poseStack.last().pose();

        // Triangular prism shape — 3 faces
        float radius = 0.18f;
        float halfH = 0.25f;
        for (int i = 0; i < 3; i++) {
            float angle1 = (float) (i * Math.PI * 2.0 / 3);
            float angle2 = (float) ((i + 1) * Math.PI * 2.0 / 3);
            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;
            // Prismatic color shift per face
            float hue = (i / 3.0f + tickCount * 0.001f) % 1.0f;
            float r = 0.5f + 0.5f * (float) Math.sin(hue * Math.PI * 2);
            float g = 0.5f + 0.5f * (float) Math.sin((hue + 0.33f) * Math.PI * 2);
            float b = 0.5f + 0.5f * (float) Math.sin((hue + 0.66f) * Math.PI * 2);

            RenderingUtils.vertex(crystal, matrix, x1, -halfH, z1, r, g, b, 0.6f, 0, 0, packedLight);
            RenderingUtils.vertex(crystal, matrix, x2, -halfH, z2, r, g, b, 0.6f, 1, 0, packedLight);
            RenderingUtils.vertex(crystal, matrix, x2, halfH, z2, r, g, b, 0.6f, 1, 1, packedLight);
            RenderingUtils.vertex(crystal, matrix, x1, halfH, z1, r, g, b, 0.6f, 0, 1, packedLight);
        }

        if (prism.isTransmitting()) {
            VertexConsumer glow = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
            float pulse = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.08);
            float size = 0.2f + pulse * 0.1f;
            float a = 0.2f + pulse * 0.15f;
            RenderingUtils.vertex(glow, matrix, -size, -size, 0, 0.8f, 0.6f, 1.0f, a, 0, 0);
            RenderingUtils.vertex(glow, matrix, size, -size, 0, 0.8f, 0.6f, 1.0f, a, 1, 0);
            RenderingUtils.vertex(glow, matrix, size, size, 0, 0.8f, 0.6f, 1.0f, a, 1, 1);
            RenderingUtils.vertex(glow, matrix, -size, size, 0, 0.8f, 0.6f, 1.0f, a, 0, 1);
        }

        poseStack.popPose();
    }
}
