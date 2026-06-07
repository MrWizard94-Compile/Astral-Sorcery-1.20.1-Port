/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * Rotating cube particle effect. Used for crystal-related particles,
 * collector crystal ambient effects, and perk gem visuals.
 *
 * <p>Renders 6 faces of a cube with per-face color variation,
 * rotating around Y axis based on age. Supports scaling and fading
 * like all EntityVisualFX subtypes.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FXCube extends EntityVisualFX {

    private final float rotationSpeedDeg;
    private final float initialRotation;

    /**
     * @param x                world X
     * @param y                world Y
     * @param z                world Z
     * @param rotationSpeedDeg degrees per tick rotation
     */
    public FXCube(double x, double y, double z, float rotationSpeedDeg) {
        super(x, y, z);
        this.rotationSpeedDeg = rotationSpeedDeg;
        this.initialRotation = (float) (Math.random() * 360);
    }

    public FXCube(double x, double y, double z) {
        this(x, y, z, 3.0f);
    }

    @Override
    @Nonnull
    public RenderType getRenderType() {
        return RenderTypesAS.EFFECT_FX_CRYSTAL;
    }

    @Override
    public void renderEffect(@Nonnull PoseStack poseStack,
                              @Nonnull VertexConsumer buffer,
                              float partialTick) {
        float x = (float) getInterpolatedX(partialTick);
        float y = (float) getInterpolatedY(partialTick);
        float z = (float) getInterpolatedZ(partialTick);

        float scale = getScaleX();
        float alpha = getAlpha();
        Color color = getColor();
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        if (alpha <= 0.01f) return;

        float half = scale * 0.5f;
        float rotation = initialRotation + (getAge() + partialTick) * rotationSpeedDeg;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));

        Matrix4f matrix = poseStack.last().pose();

        // Render 6 faces with slight color variation per face for depth perception
        // +Y face (top)
        float rT = Math.min(1.0f, r * 1.2f);
        float gT = Math.min(1.0f, g * 1.2f);
        float bT = Math.min(1.0f, b * 1.2f);
        RenderingUtils.vertex(buffer, matrix, -half, half, -half, rT, gT, bT, alpha, 0, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, -half, half, half, rT, gT, bT, alpha, 0, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, half, half, half, rT, gT, bT, alpha, 1, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, half, half, -half, rT, gT, bT, alpha, 1, 0, RenderingUtils.FULL_BRIGHT_LIGHT);

        // -Y face (bottom)
        float rB = r * 0.7f;
        float gB = g * 0.7f;
        float bB = b * 0.7f;
        RenderingUtils.vertex(buffer, matrix, -half, -half, half, rB, gB, bB, alpha, 0, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, -half, -half, -half, rB, gB, bB, alpha, 0, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, half, -half, -half, rB, gB, bB, alpha, 1, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, half, -half, half, rB, gB, bB, alpha, 1, 0, RenderingUtils.FULL_BRIGHT_LIGHT);

        // +Z face (south)
        float rS = r * 0.9f;
        float gS = g * 0.9f;
        float bS = b * 0.9f;
        RenderingUtils.vertex(buffer, matrix, -half, -half, half, rS, gS, bS, alpha, 0, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, half, -half, half, rS, gS, bS, alpha, 1, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, half, half, half, rS, gS, bS, alpha, 1, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, -half, half, half, rS, gS, bS, alpha, 0, 1, RenderingUtils.FULL_BRIGHT_LIGHT);

        // -Z face (north)
        RenderingUtils.vertex(buffer, matrix, half, -half, -half, rS, gS, bS, alpha, 0, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, -half, -half, -half, rS, gS, bS, alpha, 1, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, -half, half, -half, rS, gS, bS, alpha, 1, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, half, half, -half, rS, gS, bS, alpha, 0, 1, RenderingUtils.FULL_BRIGHT_LIGHT);

        // +X face (east)
        float rE = r * 0.8f;
        float gE = g * 0.8f;
        float bE = b * 0.8f;
        RenderingUtils.vertex(buffer, matrix, half, -half, -half, rE, gE, bE, alpha, 0, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, half, half, -half, rE, gE, bE, alpha, 0, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, half, half, half, rE, gE, bE, alpha, 1, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, half, -half, half, rE, gE, bE, alpha, 1, 0, RenderingUtils.FULL_BRIGHT_LIGHT);

        // -X face (west)
        RenderingUtils.vertex(buffer, matrix, -half, -half, half, rE, gE, bE, alpha, 0, 0, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, -half, half, half, rE, gE, bE, alpha, 0, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, -half, half, -half, rE, gE, bE, alpha, 1, 1, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, -half, -half, -half, rE, gE, bE, alpha, 1, 0, RenderingUtils.FULL_BRIGHT_LIGHT);

        poseStack.popPose();
    }
}
