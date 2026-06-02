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
import hellfirepvp.astralsorcery.client.render.CrystalModelRenderer;
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
import java.awt.Color;

/**
 * Block entity renderer for collector crystals (rock crystal and celestial).
 *
 * <p>Renders:
 * <ul>
 *   <li>Rotating crystal model via {@link CrystalModelRenderer} (proper tapered tips)</li>
 *   <li>Inner glow pulsing in the crystal's constellation color</li>
 *   <li>Vertical starlight beam when actively collecting</li>
 * </ul></p>
 */
@OnlyIn(Dist.CLIENT)
public class RenderCollectorCrystal implements BlockEntityRenderer<BlockEntityCollectorCrystal> {

    private static final float ROTATION_SPEED = 0.5f;

    public RenderCollectorCrystal(@Nonnull BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@Nonnull BlockEntityCollectorCrystal crystal,
                        float partialTick,
                        @Nonnull PoseStack poseStack,
                        @Nonnull MultiBufferSource bufferSource,
                        int packedLight,
                        int packedOverlay) {
        float tickCount = crystal.getTicksExisted() + partialTick;
        float rotation = tickCount * ROTATION_SPEED;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        // Crystal body via shared model renderer (has proper tapered tips)
        if (crystal.isCelestial()) {
            CrystalModelRenderer.renderCelestialCrystal(poseStack, bufferSource, rotation, packedLight);
        } else {
            CrystalModelRenderer.renderCollectorCrystal(poseStack, bufferSource, rotation, packedLight);
        }

        renderInnerGlow(crystal, tickCount, poseStack, bufferSource);

        poseStack.popPose();

        renderStarlightBeam(crystal, poseStack, bufferSource);
    }

    /** Pulsing glow in the crystal's constellation color (or default blue-white). */
    private void renderInnerGlow(@Nonnull BlockEntityCollectorCrystal crystal,
                                  float tickCount,
                                  @Nonnull PoseStack poseStack,
                                  @Nonnull MultiBufferSource bufferSource) {
        float pulse = 0.5f + 0.5f * (float) Math.sin(tickCount * 0.05);
        float glowAlpha = 0.2f + pulse * 0.3f;
        float size = 0.3f + pulse * 0.1f;

        int rgb = crystal.getConstellationColor();
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8)  & 0xFF) / 255f;
        float b = (rgb         & 0xFF) / 255f;

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_HALO);
        Matrix4f matrix = poseStack.last().pose();

        RenderingUtils.vertex(buffer, matrix, -size, -size, 0, r, g, b, glowAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix,  size, -size, 0, r, g, b, glowAlpha, 1, 0);
        RenderingUtils.vertex(buffer, matrix,  size,  size, 0, r, g, b, glowAlpha, 1, 1);
        RenderingUtils.vertex(buffer, matrix, -size,  size, 0, r, g, b, glowAlpha, 0, 1);
    }

    /** Thin vertical beam skyward, visible only while the crystal is collecting. */
    private void renderStarlightBeam(@Nonnull BlockEntityCollectorCrystal crystal,
                                      @Nonnull PoseStack poseStack,
                                      @Nonnull MultiBufferSource bufferSource) {
        if (!crystal.isCollecting()) return;

        int rgb = crystal.getConstellationColor();
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8)  & 0xFF) / 255f;
        float b = (rgb         & 0xFF) / 255f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.85, 0.5);

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypesAS.EFFECT_FX_LIGHTBEAM);
        Matrix4f matrix = poseStack.last().pose();

        float beamAlpha = 0.4f;
        float width = 0.08f;
        float height = 4.0f;

        // Front face
        RenderingUtils.vertex(buffer, matrix, -width, 0,      -width, r, g, b, beamAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix, -width, height, -width, r, g, b, 0,         0, 1);
        RenderingUtils.vertex(buffer, matrix,  width, height, -width, r, g, b, 0,         1, 1);
        RenderingUtils.vertex(buffer, matrix,  width, 0,      -width, r, g, b, beamAlpha, 1, 0);
        // Back face
        RenderingUtils.vertex(buffer, matrix,  width, 0,       width, r, g, b, beamAlpha, 0, 0);
        RenderingUtils.vertex(buffer, matrix,  width, height,  width, r, g, b, 0,         0, 1);
        RenderingUtils.vertex(buffer, matrix, -width, height,  width, r, g, b, 0,         1, 1);
        RenderingUtils.vertex(buffer, matrix, -width, 0,       width, r, g, b, beamAlpha, 1, 0);

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
