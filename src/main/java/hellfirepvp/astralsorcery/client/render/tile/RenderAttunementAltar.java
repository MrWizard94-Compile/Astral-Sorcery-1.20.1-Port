package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.model.builtin.ModelAttunementAltar;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAttunementAltar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * BER for the Attunement Altar.
 * Renders the altar base geometry and 8 orbiting crystal cubes.
 */
@OnlyIn(Dist.CLIENT)
public class RenderAttunementAltar implements BlockEntityRenderer<BlockEntityAttunementAltar> {

    private static final float SPIN_DURATION = 30f;

    private final ModelAttunementAltar model;

    public RenderAttunementAltar(@Nonnull BlockEntityRendererProvider.Context context) {
        this.model = new ModelAttunementAltar(context.bakeLayer(ModelAttunementAltar.LAYER));
    }

    @Override
    public void render(@Nonnull BlockEntityAttunementAltar altar, float partialTick,
                        @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                        int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entitySolid(ModelAttunementAltar.TEXTURE));
        model.renderBase(poseStack, buffer, packedLight, packedOverlay);

        double animTick = (ClientScheduler.getClientTick() + partialTick) / 4.0;

        for (int i = 1; i < 9; i++) {
            float increment = (SPIN_DURATION / 8f) * i;
            double frame = (animTick + increment) % SPIN_DURATION;
            float normalized = (float) (frame / SPIN_DURATION * 2f * Math.PI);

            float xOffset = Mth.cos(normalized);
            float zOffset = Mth.sin(normalized);

            model.renderHovering(poseStack, buffer, packedLight, packedOverlay, xOffset, zOffset, 1f);
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull BlockEntityAttunementAltar altar) { return true; }

    @Override
    public int getViewDistance() { return 128; }
}
