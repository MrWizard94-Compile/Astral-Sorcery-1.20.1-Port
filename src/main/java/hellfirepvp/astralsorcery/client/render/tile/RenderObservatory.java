package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.client.model.builtin.ModelObservatory;
import hellfirepvp.astralsorcery.common.tile.BlockEntityObservatory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * BER for the Observatory. Renders the full telescope geometry using the ported
 * 1.16 Java model. Yaw/pitch tracking will be wired once the tile entity
 * exposes interpolated rotation state.
 */
@OnlyIn(Dist.CLIENT)
public class RenderObservatory implements BlockEntityRenderer<BlockEntityObservatory> {

    private final ModelObservatory model;

    public RenderObservatory(@Nonnull BlockEntityRendererProvider.Context context) {
        this.model = new ModelObservatory(context.bakeLayer(ModelObservatory.LAYER));
    }

    @Override
    public void render(@Nonnull BlockEntityObservatory observatory, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entitySolid(ModelObservatory.TEXTURE));
        model.render(poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull BlockEntityObservatory p) {
        return true;
    }
}
