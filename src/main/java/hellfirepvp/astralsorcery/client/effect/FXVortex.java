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

/**
 * A visual effect representing a spiraling vortex particle.
 * Used for suction effects around lightwells, infuser absorption,
 * and attunement rituals pulling in starlight.
 *
 * <p>The particle spirals inward toward a target point with
 * decreasing radius and increasing speed, fading as it reaches
 * the center. Creates a visual "funnel" or "drain" effect when
 * many are spawned together.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FXVortex extends EntityVisualFX {

    /** Target (center) of the vortex. */
    private final double targetX, targetY, targetZ;

    /** Starting distance from the target. */
    private final float startRadius;

    /** Spiral parameters. */
    private float angularSpeed; // radians per tick, increases over lifetime
    private float shrinkRate;   // radius reduction per tick
    private float currentAngle;
    private float currentRadius;

    /**
     * @param startX       initial X position (around the target)
     * @param startY       initial Y position
     * @param startZ       initial Z position
     * @param targetX      target center X
     * @param targetY      target center Y
     * @param targetZ      target center Z
     * @param angularSpeed initial angular speed (radians/tick)
     */
    public FXVortex(double startX, double startY, double startZ,
                    double targetX, double targetY, double targetZ,
                    float angularSpeed) {
        super(startX, startY, startZ);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.angularSpeed = angularSpeed;

        double dx = startX - targetX;
        double dz = startZ - targetZ;
        this.startRadius = (float) Math.sqrt(dx * dx + dz * dz);
        this.currentRadius = this.startRadius;
        this.currentAngle = (float) Math.atan2(dz, dx);
        this.shrinkRate = startRadius / Math.max(1, getMaxAge());
    }

    /**
     * Sets the shrink rate (how fast the particle spirals inward).
     *
     * @param rate radius reduction per tick
     * @return this for chaining
     */
    @Nonnull
    public FXVortex setShrinkRate(float rate) {
        this.shrinkRate = rate;
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        // Spiral inward
        currentAngle += angularSpeed;
        currentRadius -= shrinkRate;

        // Accelerate as radius decreases (conservation of angular momentum feel)
        if (currentRadius > 0.05f) {
            angularSpeed += 0.005f;
        }

        if (currentRadius <= 0.02f) {
            setDead();
            return;
        }

        // Calculate spiraling position
        double newX = targetX + currentRadius * Math.cos(currentAngle);
        double newZ = targetZ + currentRadius * Math.sin(currentAngle);
        // Y interpolates toward target Y
        double newY = getPosY() + (targetY - getPosY()) * 0.08;

        setPosition(newX, newY, newZ);

        // Fade alpha as approaching center
        float radiusFrac = currentRadius / startRadius;
        setAlpha(getAlpha() * (0.5f + 0.5f * radiusFrac));
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
        Matrix4f matrix = poseStack.last().pose();
        float x = (float) getInterpolatedX(partialTick);
        float y = (float) getInterpolatedY(partialTick);
        float z = (float) getInterpolatedZ(partialTick);
        float size = getScaleX() * 0.06f * (currentRadius / startRadius + 0.3f);

        float r = getColor().getRed() / 255f;
        float g = getColor().getGreen() / 255f;
        float b = getColor().getBlue() / 255f;
        float a = getAlpha();

        // Billboard quad
        RenderingUtils.vertex(buffer, matrix, x - size, y - size, z, r, g, b, a, 0, 0);
        RenderingUtils.vertex(buffer, matrix, x + size, y - size, z, r, g, b, a, 1, 0);
        RenderingUtils.vertex(buffer, matrix, x + size, y + size, z, r, g, b, a, 1, 1);
        RenderingUtils.vertex(buffer, matrix, x - size, y + size, z, r, g, b, a, 0, 1);
    }
}
