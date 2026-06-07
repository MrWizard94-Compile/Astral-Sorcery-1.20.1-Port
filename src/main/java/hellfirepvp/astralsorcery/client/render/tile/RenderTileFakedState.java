/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityFakedState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * BER for blocks that display a ghost (faked) block state — TranslucentBlock and
 * VanishingBlock. Renders the stored {@link BlockEntityFakedState#getFakedState()} at
 * 50% alpha tinted with {@link BlockEntityFakedState#getOverlayColor()}.
 *
 * <p>Uses a VertexConsumer wrapper that intercepts the color channel and applies
 * the overlay tint, then routes all vertices through {@link RenderType#translucent()}.
 * This avoids the ObserverLib dependency from 1.16.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderTileFakedState implements BlockEntityRenderer<BlockEntityFakedState> {

    public RenderTileFakedState(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityFakedState tile, float partialTick,
                       @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        BlockState fakedState = tile.getFakedState();
        if (fakedState.is(Blocks.AIR)) return;

        Color overlay = tile.getOverlayColor();
        float tintR = overlay.getRed()   / 255f;
        float tintG = overlay.getGreen() / 255f;
        float tintB = overlay.getBlue()  / 255f;
        float alpha = 0.5f;

        VertexConsumer translucentConsumer = bufferSource.getBuffer(RenderType.translucent());
        TintedVertexConsumer tinted = new TintedVertexConsumer(translucentConsumer, tintR, tintG, tintB, alpha);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                fakedState, poseStack,
                (renderType) -> tinted,   // MultiBufferSource lambda: always route to our tinted consumer
                packedLight, packedOverlay,
                net.minecraftforge.client.model.data.ModelData.EMPTY, RenderType.translucent());
    }

    // -------------------------------------------------------------------------
    // Color-override VertexConsumer wrapper
    // -------------------------------------------------------------------------

    private static final class TintedVertexConsumer implements VertexConsumer {

        private final VertexConsumer delegate;
        private final float tintR, tintG, tintB, alpha;

        TintedVertexConsumer(@Nonnull VertexConsumer delegate,
                             float tintR, float tintG, float tintB, float alpha) {
            this.delegate = delegate;
            this.tintR = tintR;
            this.tintG = tintG;
            this.tintB = tintB;
            this.alpha = alpha;
        }

        // --- builder delegates ---

        @Override @Nonnull
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z); return this;
        }

        @Override @Nonnull
        public VertexConsumer color(int r, int g, int b, int a) {
            delegate.color(
                    (int) (r * tintR),
                    (int) (g * tintG),
                    (int) (b * tintB),
                    (int) (a * alpha));
            return this;
        }

        @Override @Nonnull
        public VertexConsumer uv(float u, float v) {
            delegate.uv(u, v); return this;
        }

        @Override @Nonnull
        public VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v); return this;
        }

        @Override @Nonnull
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v); return this;
        }

        @Override @Nonnull
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z); return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            delegate.defaultColor(
                    (int) (r * tintR), (int) (g * tintG),
                    (int) (b * tintB), (int) (a * alpha));
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }
}
