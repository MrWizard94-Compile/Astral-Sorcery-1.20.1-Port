/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.BlockEntityTelescope;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * BER for the Telescope. Renders lens glow effect at night.
 */
@OnlyIn(Dist.CLIENT)
public class RenderTelescope implements BlockEntityRenderer<BlockEntityTelescope> {

    public RenderTelescope(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityTelescope telescope, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.2, 0.5);

        float tickCount = telescope.getTicksExisted() + partialTick;
        float pulse = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.06);
        float glowAlpha = 0.1f + pulse * 0.15f;
        float size = 0.12f + pulse * 0.03f;

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        Matrix4f matrix = poseStack.last().pose();

        RenderingUtils.vertex(buffer, matrix, -size, -size, 0, 0.7f, 0.8f, 1.0f, glowAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, size, -size, 0, 0.7f, 0.8f, 1.0f, glowAlpha, 1, 0);
        RenderingUtils.vertex(buffer, matrix, size, size, 0, 0.7f, 0.8f, 1.0f, glowAlpha, 1, 1);
        RenderingUtils.vertex(buffer, matrix, -size, size, 0, 0.7f, 0.8f, 1.0f, glowAlpha, 0, 1);

        poseStack.popPose();
    }
}
