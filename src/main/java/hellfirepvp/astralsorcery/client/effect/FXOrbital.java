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
 * A visual effect that orbits around a center point in 3D space.
 * Used for constellation attunement effects around altars, collector crystals,
 * and player attunement visuals.
 *
 * <p>The particle follows an elliptical orbit with configurable radius,
 * speed, inclination, and vertical bobbing. Multiple orbitals with different
 * phases create the layered ring effects seen around active blocks.</p>
 */
@OnlyIn(Dist.CLIENT)
public class FXOrbital extends EntityVisualFX {

    /** Center position of the orbit. */
    private double centerX, centerY, centerZ;

    /** Orbit parameters. */
    private float radius;
    private float orbitSpeed; // radians per tick
    private float inclination; // tilt of the orbit plane (radians)
    private float verticalBob; // vertical oscillation amplitude
    private float currentAngle;

    /**
     * @param centerX     orbit center X
     * @param centerY     orbit center Y
     * @param centerZ     orbit center Z
     * @param radius      orbit radius in blocks
     * @param orbitSpeed  angular speed in radians per tick
     * @param startAngle  initial angle (radians)
     * @param inclination tilt angle of the orbit plane (0 = horizontal)
     */
    public FXOrbital(double centerX, double centerY, double centerZ,
                     float radius, float orbitSpeed, float startAngle,
                     float inclination) {
        super(centerX + radius * Math.cos(startAngle),
                centerY,
                centerZ + radius * Math.sin(startAngle));
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
        this.orbitSpeed = orbitSpeed;
        this.currentAngle = startAngle;
        this.inclination = inclination;
        this.verticalBob = 0.0f;
    }

    /**
     * Sets vertical bobbing amplitude.
     *
     * @param amplitude oscillation amplitude in blocks
     * @return this for chaining
     */
    @Nonnull
    public FXOrbital setVerticalBob(float amplitude) {
        this.verticalBob = amplitude;
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        currentAngle += orbitSpeed;
        if (currentAngle > Math.PI * 2) {
            currentAngle -= (float) (Math.PI * 2);
        }

        // Calculate new position on the tilted orbit plane
        double cosAngle = Math.cos(currentAngle);
        double sinAngle = Math.sin(currentAngle);

        double newX = centerX + radius * cosAngle;
        double newZ = centerZ + radius * sinAngle;
        double newY = centerY
                + radius * sinAngle * Math.sin(inclination)
                + verticalBob * (float) Math.sin(currentAngle * 2);

        setPosition(newX, newY, newZ);
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
        float size = getScaleX() * 0.08f;

        float r = getColor().getRed() / 255f;
        float g = getColor().getGreen() / 255f;
        float b = getColor().getBlue() / 255f;
        float a = getAlpha();

        // Billboard quad facing camera
        RenderingUtils.vertex(buffer, matrix, x - size, y - size, z, r, g, b, a, 0, 0);
        RenderingUtils.vertex(buffer, matrix, x + size, y - size, z, r, g, b, a, 1, 0);
        RenderingUtils.vertex(buffer, matrix, x + size, y + size, z, r, g, b, a, 1, 1);
        RenderingUtils.vertex(buffer, matrix, x - size, y + size, z, r, g, b, a, 0, 1);
    }
}
