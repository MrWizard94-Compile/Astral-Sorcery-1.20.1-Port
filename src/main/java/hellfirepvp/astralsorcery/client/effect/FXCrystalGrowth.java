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
 * Crystal growth particle effect — a slowly expanding, rotating crystalline
 * shard that represents rock crystal or celestial crystal growth.
 *
 * <p>Used for:
 * <ul>
 *   <li>Crystal growth tick effects on crystal clusters</li>
 *   <li>Collector crystal charging visuals</li>
 *   <li>Rock crystal ore discovery indicators</li>
 *   <li>Crystal tool enhancement sparkles</li>
 * </ul>
 *
 * <p>Renders as a small diamond-shaped quad (rotated 45 degrees) that
 * slowly grows in size while rotating, then fades. Gives the appearance
 * of a tiny crystal shard materializing from starlight.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FXCrystalGrowth extends EntityVisualFX {

    private final float rotationSpeed;
    private final float initialRotation;
    private float targetScale;

    /**
     * @param x world X
     * @param y world Y
     * @param z world Z
     */
    public FXCrystalGrowth(double x, double y, double z) {
        super(x, y, z);
        this.rotationSpeed = 2.0f + (float) (Math.random() * 3.0);
        this.initialRotation = (float) (Math.random() * 360.0);
        this.targetScale = 0.1f;
        setMaxAge(40 + (int) (Math.random() * 20));
    }

    /**
     * Sets the target scale the crystal grows toward over its lifetime.
     *
     * @param scale target scale in blocks
     * @return this for chaining
     */
    @Nonnull
    public FXCrystalGrowth setTargetScale(float scale) {
        this.targetScale = scale;
        return this;
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
        float progress = getAgeProgress();
        float alpha = getAlpha();

        // Grow phase: first 60% of lifetime, crystal expands
        // Fade phase: last 40%, crystal fades
        float scaleFactor;
        float effectiveAlpha;
        if (progress < 0.6f) {
            // Growth: ease-out curve
            float growProgress = progress / 0.6f;
            scaleFactor = growProgress * (2.0f - growProgress); // ease-out quadratic
            effectiveAlpha = alpha;
        } else {
            // Fade: linear alpha decrease
            float fadeProgress = (progress - 0.6f) / 0.4f;
            scaleFactor = 1.0f;
            effectiveAlpha = alpha * (1.0f - fadeProgress);
        }

        if (effectiveAlpha <= 0.01f) return;

        float size = targetScale * scaleFactor * getScaleX();
        float rotation = initialRotation + (getAge() + partialTick) * rotationSpeed;

        float x = (float) getInterpolatedX(partialTick);
        float y = (float) getInterpolatedY(partialTick);
        float z = (float) getInterpolatedZ(partialTick);

        Color color = getColor();
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));

        Matrix4f matrix = poseStack.last().pose();

        // Diamond shape: rotated square (4 vertices at NSEW)
        float half = size * 0.5f;
        float height = size * 0.7f; // Slightly elongated vertically

        // Front face
        RenderingUtils.vertex(buffer, matrix, 0, -height, -half, r, g, b, effectiveAlpha, 0.5f, 1.0f, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, half, 0, 0, r, g, b, effectiveAlpha, 1.0f, 0.5f, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, 0, height, half, r, g, b, effectiveAlpha, 0.5f, 0.0f, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, -half, 0, 0, r, g, b, effectiveAlpha, 0.0f, 0.5f, RenderingUtils.FULL_BRIGHT_LIGHT);

        // Back face (reversed winding)
        RenderingUtils.vertex(buffer, matrix, 0, -height, half, r, g, b, effectiveAlpha, 0.5f, 1.0f, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, -half, 0, 0, r, g, b, effectiveAlpha, 0.0f, 0.5f, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, 0, height, -half, r, g, b, effectiveAlpha, 0.5f, 0.0f, RenderingUtils.FULL_BRIGHT_LIGHT);
        RenderingUtils.vertex(buffer, matrix, half, 0, 0, r, g, b, effectiveAlpha, 1.0f, 0.5f, RenderingUtils.FULL_BRIGHT_LIGHT);

        poseStack.popPose();
    }
}
