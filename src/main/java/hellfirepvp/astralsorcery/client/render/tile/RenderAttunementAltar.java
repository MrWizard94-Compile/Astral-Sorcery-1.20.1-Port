/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAttunementAltar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * BER for the Attunement Altar. Renders constellation overlay during attunement,
 * pulsing halo, and vertical energy column.
 */
@OnlyIn(Dist.CLIENT)
public class RenderAttunementAltar implements BlockEntityRenderer<BlockEntityAttunementAltar> {

    public RenderAttunementAltar(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityAttunementAltar altar, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);

        float tickCount = altar.getTicksExisted() + partialTick;
        float pulse = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.04);

        // Halo on surface
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        Matrix4f matrix = poseStack.last().pose();
        float haloSize = 0.6f + pulse * 0.2f;
        float haloAlpha = 0.15f + pulse * 0.15f;

        RenderingUtils.vertex(halo, matrix, -haloSize, 1.02f, -haloSize, 0.4f, 0.3f, 0.8f, haloAlpha, 0, 0);
        RenderingUtils.vertex(halo, matrix, -haloSize, 1.02f, haloSize, 0.4f, 0.3f, 0.8f, haloAlpha, 0, 1);
        RenderingUtils.vertex(halo, matrix, haloSize, 1.02f, haloSize, 0.4f, 0.3f, 0.8f, haloAlpha, 1, 1);
        RenderingUtils.vertex(halo, matrix, haloSize, 1.02f, -haloSize, 0.4f, 0.3f, 0.8f, haloAlpha, 1, 0);

        // Vertical energy column
        VertexConsumer beam = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_LIGHTBEAM);
        float beamAlpha = 0.2f + pulse * 0.2f;
        float w = 0.1f;
        float h = 3.0f + pulse * 1.5f;

        RenderingUtils.vertex(beam, matrix, -w, 1.0f, 0, 0.5f, 0.3f, 0.9f, beamAlpha, 0, 0);
        RenderingUtils.vertex(beam, matrix, -w, 1.0f + h, 0, 0.5f, 0.3f, 0.9f, 0, 0, 1);
        RenderingUtils.vertex(beam, matrix, w, 1.0f + h, 0, 0.5f, 0.3f, 0.9f, 0, 1, 1);
        RenderingUtils.vertex(beam, matrix, w, 1.0f, 0, 0.5f, 0.3f, 0.9f, beamAlpha, 1, 0);

        RenderingUtils.vertex(beam, matrix, 0, 1.0f, -w, 0.5f, 0.3f, 0.9f, beamAlpha, 0, 0);
        RenderingUtils.vertex(beam, matrix, 0, 1.0f + h, -w, 0.5f, 0.3f, 0.9f, 0, 0, 1);
        RenderingUtils.vertex(beam, matrix, 0, 1.0f + h, w, 0.5f, 0.3f, 0.9f, 0, 1, 1);
        RenderingUtils.vertex(beam, matrix, 0, 1.0f, w, 0.5f, 0.3f, 0.9f, beamAlpha, 1, 0);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull BlockEntityAttunementAltar altar) { return true; }

    @Override
    public int getViewDistance() { return 128; }
}
