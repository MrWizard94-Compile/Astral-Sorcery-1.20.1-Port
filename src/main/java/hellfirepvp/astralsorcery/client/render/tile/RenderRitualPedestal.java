/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.BlockEntityRitualPedestal;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * BER for the Ritual Pedestal. Renders floating/spinning crystal above pedestal
 * and ritual area indicator glow.
 */
@OnlyIn(Dist.CLIENT)
public class RenderRitualPedestal implements BlockEntityRenderer<BlockEntityRitualPedestal> {

    public RenderRitualPedestal(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityRitualPedestal pedestal, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);

        float tickCount = pedestal.getTicksExisted() + partialTick;

        // Floating bob effect
        float bob = (float) Math.sin(tickCount * 0.04) * 0.05f;
        float rotation = tickCount * 0.8f;

        // Crystal glow above pedestal
        VertexConsumer halo = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        Matrix4f matrix = poseStack.last().pose();
        float glowSize = 0.25f;
        float glowY = 1.3f + bob;
        float glowAlpha = 0.3f + 0.1f * (float) Math.sin(tickCount * 0.06);

        RenderingUtils.vertex(halo, matrix, -glowSize, glowY, -glowSize, 0.6f, 0.4f, 1.0f, glowAlpha, 0, 0);
        RenderingUtils.vertex(halo, matrix, -glowSize, glowY, glowSize, 0.6f, 0.4f, 1.0f, glowAlpha, 0, 1);
        RenderingUtils.vertex(halo, matrix, glowSize, glowY, glowSize, 0.6f, 0.4f, 1.0f, glowAlpha, 1, 1);
        RenderingUtils.vertex(halo, matrix, glowSize, glowY, -glowSize, 0.6f, 0.4f, 1.0f, glowAlpha, 1, 0);

        // Ritual area ring (horizontal)
        float ringSize = 1.5f;
        float ringAlpha = 0.08f + 0.04f * (float) Math.sin(tickCount * 0.02);
        RenderingUtils.vertex(halo, matrix, -ringSize, 0.02f, -ringSize, 0.4f, 0.3f, 0.8f, ringAlpha, 0, 0);
        RenderingUtils.vertex(halo, matrix, -ringSize, 0.02f, ringSize, 0.4f, 0.3f, 0.8f, ringAlpha, 0, 1);
        RenderingUtils.vertex(halo, matrix, ringSize, 0.02f, ringSize, 0.4f, 0.3f, 0.8f, ringAlpha, 1, 1);
        RenderingUtils.vertex(halo, matrix, ringSize, 0.02f, -ringSize, 0.4f, 0.3f, 0.8f, ringAlpha, 1, 0);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull BlockEntityRitualPedestal p) { return true; }
}
