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

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * Radial burst effect — spawns multiple expanding particle quads radiating
 * outward from a center point. Used for altar tier-up effects, perk node
 * activation flashes, and ritual completion pulses.
 *
 * <p>Each burst contains {@code rayCount} billboarded quads that expand
 * outward at different angles, fading as they travel. The effect is
 * short-lived (typically 10-20 ticks).</p>
 */
@OnlyIn(Dist.CLIENT)
public class FXBurst extends EntityVisualFX {

    private final int rayCount;
    private final float[] rayAnglesX;
    private final float[] rayAnglesY;
    private final float expansionSpeed;

    /**
     * @param x              world X center
     * @param y              world Y center
     * @param z              world Z center
     * @param rayCount       number of radial rays (8-16 typical)
     * @param expansionSpeed how fast rays expand outward (blocks/tick)
     */
    public FXBurst(double x, double y, double z, int rayCount, float expansionSpeed) {
        super(x, y, z);
        this.rayCount = rayCount;
        this.expansionSpeed = expansionSpeed;
        this.rayAnglesX = new float[rayCount];
        this.rayAnglesY = new float[rayCount];

        // Distribute rays roughly evenly on a sphere
        for (int i = 0; i < rayCount; i++) {
            // Golden angle distribution for even spacing
            float theta = (float) (Math.acos(1 - 2.0 * (i + 0.5) / rayCount));
            float phi = (float) (Math.PI * (1 + Math.sqrt(5)) * i);
            rayAnglesX[i] = theta;
            rayAnglesY[i] = phi;
        }

        setMaxAge(15);
    }

    public FXBurst(double x, double y, double z) {
        this(x, y, z, 12, 0.15f);
    }

    @Override
    @Nonnull
    public RenderType getRenderType() {
        return RenderTypesAS.EFFECT_FX_GENERIC_PARTICLE;
    }

    @Override
    public void renderEffect(@Nonnull PoseStack poseStack,
                              @Nonnull VertexConsumer buffer,
                              float partialTick) {
        float progress = getAgeProgress();
        float expansion = (getAge() + partialTick) * expansionSpeed;
        float alpha = getAlpha() * (1.0f - progress * progress); // Quadratic fade
        float scale = getScaleX() * (0.5f + progress * 0.5f);

        if (alpha <= 0.01f) return;

        Color color = getColor();
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        float cx = (float) getInterpolatedX(partialTick);
        float cy = (float) getInterpolatedY(partialTick);
        float cz = (float) getInterpolatedZ(partialTick);

        for (int i = 0; i < rayCount; i++) {
            float theta = rayAnglesX[i];
            float phi = rayAnglesY[i];

            // Spherical to cartesian offset
            float dx = (float) (Math.sin(theta) * Math.cos(phi)) * expansion;
            float dy = (float) (Math.cos(theta)) * expansion;
            float dz = (float) (Math.sin(theta) * Math.sin(phi)) * expansion;

            float px = cx + dx;
            float py = cy + dy;
            float pz = cz + dz;

            // Each ray is a small billboarded quad
            RenderingUtils.renderFacingQuad(buffer, poseStack, px, py, pz, scale, r, g, b, alpha);
        }
    }
}
