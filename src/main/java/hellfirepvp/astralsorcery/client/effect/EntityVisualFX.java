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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.function.Consumer;

/**
 * Base class for all Astral Sorcery visual effects (particles, beams,
 * lightning, bursts, etc.). Manages position, motion, lifetime, color,
 * scale, alpha, and rendering delegation.
 *
 * <p>1.16 → 1.20: This replaces the old {@code EntityComplexFX} /
 * {@code EntityDynamicFX} / {@code EntityVisualFX} hierarchy with a
 * single unified base that uses composition (VFX controllers) rather
 * than deep inheritance.</p>
 *
 * <p>Subclasses implement {@link #renderEffect(PoseStack, VertexConsumer, float)}
 * to emit vertices. The effect manager handles batching by render type.</p>
 */
@OnlyIn(Dist.CLIENT)
public abstract class EntityVisualFX {

    // ---- Position & motion ----
    private double posX;
    private double posY;
    private double posZ;
    private double motionX;
    private double motionY;
    private double motionZ;
    private double prevPosX;
    private double prevPosY;
    private double prevPosZ;

    // ---- Lifecycle ----
    private int age;
    private int maxAge;
    private boolean dead;

    // ---- Visual properties ----
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float alpha = 1.0f;
    @Nonnull
    private Color color = Color.WHITE;
    private float gravity = 0.0f;

    // ---- Controllers (composition over inheritance) ----
    @Nullable
    private Consumer<EntityVisualFX> tickController;
    @Nullable
    private Consumer<EntityVisualFX> colorController;
    @Nullable
    private Consumer<EntityVisualFX> alphaController;
    @Nullable
    private Consumer<EntityVisualFX> scaleController;
    @Nullable
    private Consumer<EntityVisualFX> motionController;

    protected EntityVisualFX(double x, double y, double z) {
        this.posX = this.prevPosX = x;
        this.posY = this.prevPosY = y;
        this.posZ = this.prevPosZ = z;
        this.age = 0;
        this.maxAge = 40;
        this.dead = false;
    }

    // =========================================================================
    // Lifecycle
    // =========================================================================

    /**
     * Called every client tick. Updates position, applies controllers, ages.
     */
    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        // Apply controllers
        if (this.motionController != null) this.motionController.accept(this);
        if (this.colorController != null) this.colorController.accept(this);
        if (this.alphaController != null) this.alphaController.accept(this);
        if (this.scaleController != null) this.scaleController.accept(this);
        if (this.tickController != null) this.tickController.accept(this);

        // Apply motion
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;

        // Apply gravity
        this.motionY -= this.gravity;

        // Age
        this.age++;
        if (this.age >= this.maxAge) {
            this.dead = true;
        }
    }

    /**
     * @return true if this effect should be removed
     */
    public boolean isDead() {
        return this.dead;
    }

    /**
     * Mark this effect for removal.
     */
    public void setDead() {
        this.dead = true;
    }

    /**
     * @return the render type this effect draws into
     */
    @Nonnull
    public abstract RenderType getRenderType();

    /**
     * Emit vertices for this effect. Called during batch rendering.
     *
     * @param poseStack   the current pose stack (camera-relative)
     * @param buffer      the vertex consumer for this effect's render type
     * @param partialTick partial tick for interpolation
     */
    public abstract void renderEffect(@Nonnull PoseStack poseStack,
                                       @Nonnull VertexConsumer buffer,
                                       float partialTick);

    // =========================================================================
    // Position
    // =========================================================================

    public double getPosX() { return posX; }
    public double getPosY() { return posY; }
    public double getPosZ() { return posZ; }
    public double getPrevPosX() { return prevPosX; }
    public double getPrevPosY() { return prevPosY; }
    public double getPrevPosZ() { return prevPosZ; }

    public double getInterpolatedX(float partialTick) {
        return prevPosX + (posX - prevPosX) * partialTick;
    }

    public double getInterpolatedY(float partialTick) {
        return prevPosY + (posY - prevPosY) * partialTick;
    }

    public double getInterpolatedZ(float partialTick) {
        return prevPosZ + (posZ - prevPosZ) * partialTick;
    }

    @Nonnull
    public Vec3 getInterpolatedPosition(float partialTick) {
        return new Vec3(
                getInterpolatedX(partialTick),
                getInterpolatedY(partialTick),
                getInterpolatedZ(partialTick));
    }

    @Nonnull
    public EntityVisualFX setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        return this;
    }

    // =========================================================================
    // Motion
    // =========================================================================

    public double getMotionX() { return motionX; }
    public double getMotionY() { return motionY; }
    public double getMotionZ() { return motionZ; }

    @Nonnull
    public EntityVisualFX setMotion(double mx, double my, double mz) {
        this.motionX = mx;
        this.motionY = my;
        this.motionZ = mz;
        return this;
    }

    @Nonnull
    public EntityVisualFX setGravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    // =========================================================================
    // Lifecycle properties
    // =========================================================================

    public int getAge() { return age; }
    public int getMaxAge() { return maxAge; }

    /**
     * @return age progress [0, 1] where 0 = just spawned, 1 = about to die
     */
    public float getAgeProgress() {
        return maxAge > 0 ? (float) age / maxAge : 1.0f;
    }

    @Nonnull
    public EntityVisualFX setMaxAge(int maxAge) {
        this.maxAge = Math.max(1, maxAge);
        return this;
    }

    // =========================================================================
    // Visual properties
    // =========================================================================

    public float getScaleX() { return scaleX; }
    public float getScaleY() { return scaleY; }
    public float getAlpha() { return alpha; }
    @Nonnull
    public Color getColor() { return color; }

    @Nonnull
    public EntityVisualFX setScale(float scale) {
        this.scaleX = scale;
        this.scaleY = scale;
        return this;
    }

    @Nonnull
    public EntityVisualFX setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    @Nonnull
    public EntityVisualFX setAlpha(float alpha) {
        this.alpha = Math.max(0, Math.min(1, alpha));
        return this;
    }

    @Nonnull
    public EntityVisualFX setColor(@Nonnull Color color) {
        this.color = color;
        return this;
    }

    // =========================================================================
    // Controllers (composition-based behavior)
    // =========================================================================

    @Nonnull
    public EntityVisualFX setTickController(@Nullable Consumer<EntityVisualFX> controller) {
        this.tickController = controller;
        return this;
    }

    @Nonnull
    public EntityVisualFX setColorController(@Nullable Consumer<EntityVisualFX> controller) {
        this.colorController = controller;
        return this;
    }

    @Nonnull
    public EntityVisualFX setAlphaController(@Nullable Consumer<EntityVisualFX> controller) {
        this.alphaController = controller;
        return this;
    }

    @Nonnull
    public EntityVisualFX setScaleController(@Nullable Consumer<EntityVisualFX> controller) {
        this.scaleController = controller;
        return this;
    }

    @Nonnull
    public EntityVisualFX setMotionController(@Nullable Consumer<EntityVisualFX> controller) {
        this.motionController = controller;
        return this;
    }
}
