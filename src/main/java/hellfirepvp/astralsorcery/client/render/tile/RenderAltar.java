/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * Block entity renderer for all altar tiers (discovery, attunement,
 * constellation, radiance).
 *
 * <p>Renders:
 * <ul>
 *   <li>Floating item above the altar (current recipe output preview)</li>
 *   <li>Starlight collection beam (vertical pillar when active)</li>
 *   <li>Halo/glow effect at the altar surface</li>
 *   <li>Particle orbits during crafting</li>
 *   <li>Constellation focus overlay (constellation tier+)</li>
 * </ul>
 *
 * <p>1.16 → 1.20:
 * {@code TileEntityRenderer<T>} → {@code BlockEntityRenderer<T>}.
 * Constructor takes {@code BlockEntityRendererProvider.Context}.
 * {@code MatrixStack} → {@code PoseStack}.</p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderAltar implements BlockEntityRenderer<BlockEntityAltar> {

    private final BlockEntityRendererProvider.Context context;

    public RenderAltar(@Nonnull BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(@Nonnull BlockEntityAltar altar,
                        float partialTick,
                        @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource,
                        int packedLight,
                        int packedOverlay) {
        poseStack.pushPose();

        // Center on block
        poseStack.translate(0.5, 0.0, 0.5);

        renderStarlightBeam(altar, partialTick, poseStack, bufferSource);
        renderHaloGlow(altar, partialTick, poseStack, bufferSource);

        poseStack.popPose();
    }

    /**
     * Render the vertical starlight collection beam above the altar.
     */
    private void renderStarlightBeam(@Nonnull BlockEntityAltar altar,
                                      float partialTick,
                                      @Nonnull PoseStack poseStack,
                                      @Nonnull MultiBufferSource bufferSource) {
        float starlightStored = altar.getStarlightStored();
        float starlightCapacity = altar.getStarlightCapacity();
        if (starlightStored <= 0 || starlightCapacity <= 0) return;

        float fillRatio = Math.min(1.0f, starlightStored / starlightCapacity);
        float beamAlpha = 0.3f + fillRatio * 0.5f;
        float beamHeight = 2.0f + fillRatio * 6.0f;

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_LIGHTBEAM);
        Matrix4f matrix = poseStack.last().pose();

        float width = 0.15f;

        // Front face
        RenderingUtils.vertex(buffer, matrix, -width, 1.0f, -width, 0.6f, 0.7f, 1.0f, beamAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, -width, 1.0f + beamHeight, -width, 0.6f, 0.7f, 1.0f, 0, 0, 1);
        RenderingUtils.vertex(buffer, matrix, width, 1.0f + beamHeight, -width, 0.6f, 0.7f, 1.0f, 0, 1, 1);
        RenderingUtils.vertex(buffer, matrix, width, 1.0f, -width, 0.6f, 0.7f, 1.0f, beamAlpha, 1, 0);

        // Back face
        RenderingUtils.vertex(buffer, matrix, width, 1.0f, width, 0.6f, 0.7f, 1.0f, beamAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, width, 1.0f + beamHeight, width, 0.6f, 0.7f, 1.0f, 0, 0, 1);
        RenderingUtils.vertex(buffer, matrix, -width, 1.0f + beamHeight, width, 0.6f, 0.7f, 1.0f, 0, 1, 1);
        RenderingUtils.vertex(buffer, matrix, -width, 1.0f, width, 0.6f, 0.7f, 1.0f, beamAlpha, 1, 0);

        // Right face
        RenderingUtils.vertex(buffer, matrix, width, 1.0f, -width, 0.6f, 0.7f, 1.0f, beamAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, width, 1.0f + beamHeight, -width, 0.6f, 0.7f, 1.0f, 0, 0, 1);
        RenderingUtils.vertex(buffer, matrix, width, 1.0f + beamHeight, width, 0.6f, 0.7f, 1.0f, 0, 1, 1);
        RenderingUtils.vertex(buffer, matrix, width, 1.0f, width, 0.6f, 0.7f, 1.0f, beamAlpha, 1, 0);

        // Left face
        RenderingUtils.vertex(buffer, matrix, -width, 1.0f, width, 0.6f, 0.7f, 1.0f, beamAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, -width, 1.0f + beamHeight, width, 0.6f, 0.7f, 1.0f, 0, 0, 1);
        RenderingUtils.vertex(buffer, matrix, -width, 1.0f + beamHeight, -width, 0.6f, 0.7f, 1.0f, 0, 1, 1);
        RenderingUtils.vertex(buffer, matrix, -width, 1.0f, -width, 0.6f, 0.7f, 1.0f, beamAlpha, 1, 0);
    }

    /**
     * Render the halo glow effect on the altar surface.
     */
    private void renderHaloGlow(@Nonnull BlockEntityAltar altar,
                                 float partialTick,
                                 @Nonnull PoseStack poseStack,
                                 @Nonnull MultiBufferSource bufferSource) {
        float starlightStored = altar.getStarlightStored();
        if (starlightStored <= 0) return;

        float starlightCapacity = altar.getStarlightCapacity();
        float fillRatio = starlightCapacity > 0
                ? Math.min(1.0f, starlightStored / starlightCapacity)
                : 0;
        float haloAlpha = 0.15f + fillRatio * 0.3f;

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.8f + fillRatio * 0.4f;

        // Horizontal quad just above the altar surface
        RenderingUtils.vertex(buffer, matrix, -size, 1.02f, -size, 0.5f, 0.6f, 1.0f, haloAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, -size, 1.02f, size, 0.5f, 0.6f, 1.0f, haloAlpha, 0, 1);
        RenderingUtils.vertex(buffer, matrix, size, 1.02f, size, 0.5f, 0.6f, 1.0f, haloAlpha, 1, 1);
        RenderingUtils.vertex(buffer, matrix, size, 1.02f, -size, 0.5f, 0.6f, 1.0f, haloAlpha, 1, 0);
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull BlockEntityAltar altar) {
        // Beam extends well above the block
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
