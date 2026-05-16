/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.BlockEntityChalice;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * BER for the Chalice. Renders fluid surface inside the chalice bowl with a shimmer effect.
 */
@OnlyIn(Dist.CLIENT)
public class RenderChalice implements BlockEntityRenderer<BlockEntityChalice> {

    public RenderChalice(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityChalice chalice, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        int fluidAmount = chalice.getFluidAmount();
        if (fluidAmount <= 0) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);

        float tickCount = chalice.getTicksExisted() + partialTick;
        float fillRatio = Math.min(1.0f, fluidAmount / 4000.0f);
        float surfaceY = 0.55f + fillRatio * 0.3f;
        float wave = 0.005f * (float) Math.sin(tickCount * 0.05);

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_CRYSTAL);
        Matrix4f matrix = poseStack.last().pose();
        float size = 0.3f;
        float r = 0.3f, g = 0.4f, b = 0.85f, a = 0.65f;

        RenderingUtils.vertex(buffer, matrix, -size, surfaceY + wave, -size, r, g, b, a, 0, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, -size, surfaceY + wave, size, r, g, b, a, 0, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, size, surfaceY + wave, size, r, g, b, a, 1, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, size, surfaceY + wave, -size, r, g, b, a, 1, 0, RenderingUtils.FULL_BRIGHT_LIGHT);

        poseStack.popPose();
    }
}
