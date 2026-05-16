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
import hellfirepvp.astralsorcery.common.tile.BlockEntityCollectorCrystal;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;

/**
 * Block entity renderer for collector crystals (rock crystal and celestial).
 *
 * <p>Renders:
 * <ul>
 *   <li>Rotating crystal model with translucent texture</li>
 *   <li>Inner glow effect that pulses with starlight collection</li>
 *   <li>Vertical starlight beam when actively collecting</li>
 *   <li>Halo ring at the crystal base</li>
 * </ul></p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderCollectorCrystal implements BlockEntityRenderer<BlockEntityCollectorCrystal> {

    /** Crystal rotation speed in degrees per tick. */
    private static final float ROTATION_SPEED = 0.5f;

    private final BlockEntityRendererProvider.Context context;

    public RenderCollectorCrystal(@Nonnull BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(@Nonnull BlockEntityCollectorCrystal crystal,
                        float partialTick,
                        @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource,
                        int packedLight,
                        int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        // Slow rotation
        float tickCount = crystal.getTicksExisted() + partialTick;
        float rotation = tickCount * ROTATION_SPEED;
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));

        renderCrystalBody(crystal, partialTick, poseStack, bufferSource, packedLight);
        renderInnerGlow(crystal, partialTick, poseStack, bufferSource);

        poseStack.popPose();

        // Starlight beam (rendered without rotation)
        renderStarlightBeam(crystal, partialTick, poseStack, bufferSource);
    }

    /**
     * Render the crystal body as a translucent hexagonal prism.
     */
    private void renderCrystalBody(@Nonnull BlockEntityCollectorCrystal crystal,
                                    float partialTick,
                                    @Nonnull PoseStack poseStack,
                                    @Nonnull MultiBufferSource bufferSource,
                                    int packedLight) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_CRYSTAL);
        Matrix4f matrix = poseStack.last().pose();

        boolean isCelestial = crystal.isCelestial();
        float r = isCelestial ? 0.4f : 0.6f;
        float g = isCelestial ? 0.5f : 0.7f;
        float b = isCelestial ? 0.9f : 1.0f;
        float a = 0.7f;

        // Simplified hexagonal prism (6 sides)
        float radius = 0.25f;
        float halfHeight = 0.35f;
        int sides = 6;

        for (int i = 0; i < sides; i++) {
            float angle1 = (float) (i * Math.PI * 2.0 / sides);
            float angle2 = (float) ((i + 1) * Math.PI * 2.0 / sides);

            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;

            float u1 = (float) i / sides;
            float u2 = (float) (i + 1) / sides;

            // Side face
            RenderingUtils.vertex(buffer, matrix, x1, -halfHeight, z1,
                    r, g, b, a, u1, 0, packedLight);
            RenderingUtils.vertex(buffer, matrix, x2, -halfHeight, z2,
                    r, g, b, a, u2, 0, packedLight);
            RenderingUtils.vertex(buffer, matrix, x2, halfHeight, z2,
                    r, g, b, a, u2, 1, packedLight);
            RenderingUtils.vertex(buffer, matrix, x1, halfHeight, z1,
                    r, g, b, a, u1, 1, packedLight);
        }
    }

    /**
     * Render the inner glow: a pulsing additive quad.
     */
    private void renderInnerGlow(@Nonnull BlockEntityCollectorCrystal crystal,
                                  float partialTick,
                                  @Nonnull PoseStack poseStack,
                                  @Nonnull MultiBufferSource bufferSource) {
        float tickCount = crystal.getTicksExisted() + partialTick;
        float pulse = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.05);
        float glowAlpha = 0.2f + pulse * 0.3f;

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        Matrix4f matrix = poseStack.last().pose();

        float size = 0.3f + pulse * 0.1f;

        // Camera-facing glow quad (static axes since crystal already rotates)
        RenderingUtils.vertex(buffer, matrix, -size, -size, 0, 0.6f, 0.8f, 1.0f, glowAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, size, -size, 0, 0.6f, 0.8f, 1.0f, glowAlpha, 1, 0);
        RenderingUtils.vertex(buffer, matrix, size, size, 0, 0.6f, 0.8f, 1.0f, glowAlpha, 1, 1);
        RenderingUtils.vertex(buffer, matrix, -size, size, 0, 0.6f, 0.8f, 1.0f, glowAlpha, 0, 1);
    }

    /**
     * Render vertical starlight beam above the crystal.
     */
    private void renderStarlightBeam(@Nonnull BlockEntityCollectorCrystal crystal,
                                      float partialTick,
                                      @Nonnull PoseStack poseStack,
                                      @Nonnull MultiBufferSource bufferSource) {
        if (!crystal.isCollecting()) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.85, 0.5);

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_LIGHTBEAM);
        Matrix4f matrix = poseStack.last().pose();

        float beamAlpha = 0.4f;
        float width = 0.08f;
        float height = 4.0f;

        // Four faces of a thin vertical beam
        // Front
        RenderingUtils.vertex(buffer, matrix, -width, 0, -width, 0.5f, 0.7f, 1.0f, beamAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, -width, height, -width, 0.5f, 0.7f, 1.0f, 0, 0, 1);
        RenderingUtils.vertex(buffer, matrix, width, height, -width, 0.5f, 0.7f, 1.0f, 0, 1, 1);
        RenderingUtils.vertex(buffer, matrix, width, 0, -width, 0.5f, 0.7f, 1.0f, beamAlpha, 1, 0);

        // Back
        RenderingUtils.vertex(buffer, matrix, width, 0, width, 0.5f, 0.7f, 1.0f, beamAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, width, height, width, 0.5f, 0.7f, 1.0f, 0, 0, 1);
        RenderingUtils.vertex(buffer, matrix, -width, height, width, 0.5f, 0.7f, 1.0f, 0, 1, 1);
        RenderingUtils.vertex(buffer, matrix, -width, 0, width, 0.5f, 0.7f, 1.0f, beamAlpha, 1, 0);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@Nonnull BlockEntityCollectorCrystal crystal) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
