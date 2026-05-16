/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.effect;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.function.Consumer;

/**
 * Builder-style configuration for spawning visual effects.
 * Collects all properties before creating the actual effect entity.
 *
 * <p>Usage:
 * <pre>{@code
 * EffectProperties props = EffectProperties.create()
 *     .setColor(Color.CYAN)
 *     .setAlpha(0.8f)
 *     .setScale(2.0f)
 *     .setMaxAge(60)
 *     .setGravity(0.01f)
 *     .setAlphaFadeOut();
 * EffectManager.spawn(new FXParticle(x, y, z), props);
 * }</pre>
 */
@OnlyIn(Dist.CLIENT)
public class EffectProperties {

    @Nonnull
    private Color color = Color.WHITE;
    private float alpha = 1.0f;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private int maxAge = 40;
    private float gravity = 0.0f;
    private double motionX = 0;
    private double motionY = 0;
    private double motionZ = 0;

    @Nullable
    private Consumer<EntityVisualFX> alphaController;
    @Nullable
    private Consumer<EntityVisualFX> colorController;
    @Nullable
    private Consumer<EntityVisualFX> scaleController;
    @Nullable
    private Consumer<EntityVisualFX> motionController;
    @Nullable
    private Consumer<EntityVisualFX> tickController;

    private EffectProperties() {}

    @Nonnull
    public static EffectProperties create() {
        return new EffectProperties();
    }

    // ---- Setters (all return this for chaining) ----

    @Nonnull
    public EffectProperties setColor(@Nonnull Color color) {
        this.color = color;
        return this;
    }

    @Nonnull
    public EffectProperties setAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    @Nonnull
    public EffectProperties setScale(float scale) {
        this.scaleX = scale;
        this.scaleY = scale;
        return this;
    }

    @Nonnull
    public EffectProperties setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        return this;
    }

    @Nonnull
    public EffectProperties setMaxAge(int maxAge) {
        this.maxAge = Math.max(1, maxAge);
        return this;
    }

    @Nonnull
    public EffectProperties setGravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    @Nonnull
    public EffectProperties setMotion(double mx, double my, double mz) {
        this.motionX = mx;
        this.motionY = my;
        this.motionZ = mz;
        return this;
    }

    @Nonnull
    public EffectProperties setAlphaController(@Nullable Consumer<EntityVisualFX> controller) {
        this.alphaController = controller;
        return this;
    }

    @Nonnull
    public EffectProperties setColorController(@Nullable Consumer<EntityVisualFX> controller) {
        this.colorController = controller;
        return this;
    }

    @Nonnull
    public EffectProperties setScaleController(@Nullable Consumer<EntityVisualFX> controller) {
        this.scaleController = controller;
        return this;
    }

    @Nonnull
    public EffectProperties setMotionController(@Nullable Consumer<EntityVisualFX> controller) {
        this.motionController = controller;
        return this;
    }

    @Nonnull
    public EffectProperties setTickController(@Nullable Consumer<EntityVisualFX> controller) {
        this.tickController = controller;
        return this;
    }

    // ---- Common presets ----

    /**
     * Fade alpha from 1 to 0 over the effect's lifetime.
     */
    @Nonnull
    public EffectProperties setAlphaFadeOut() {
        this.alphaController = fx -> fx.setAlpha(1.0f - fx.getAgeProgress());
        return this;
    }

    /**
     * Fade alpha in then out (bell curve via sin).
     */
    @Nonnull
    public EffectProperties setAlphaFadeInOut() {
        this.alphaController = fx -> {
            float progress = fx.getAgeProgress();
            fx.setAlpha((float) Math.sin(progress * Math.PI));
        };
        return this;
    }

    /**
     * Scale grows over lifetime.
     */
    @Nonnull
    public EffectProperties setScaleGrow(float startScale, float endScale) {
        this.scaleController = fx -> {
            float progress = fx.getAgeProgress();
            float s = startScale + (endScale - startScale) * progress;
            fx.setScale(s);
        };
        return this;
    }

    /**
     * Scale shrinks over lifetime.
     */
    @Nonnull
    public EffectProperties setScaleShrink(float startScale) {
        return setScaleGrow(startScale, 0.0f);
    }

    // ---- Apply to effect ----

    /**
     * Apply all configured properties to an effect instance.
     *
     * @param effect the effect to configure
     */
    public void applyTo(@Nonnull EntityVisualFX effect) {
        effect.setColor(color);
        effect.setAlpha(alpha);
        effect.setScale(scaleX, scaleY);
        effect.setMaxAge(maxAge);
        effect.setGravity(gravity);
        effect.setMotion(motionX, motionY, motionZ);
        effect.setAlphaController(alphaController);
        effect.setColorController(colorController);
        effect.setScaleController(scaleController);
        effect.setMotionController(motionController);
        effect.setTickController(tickController);
    }

    // ---- Getters ----

    @Nonnull public Color getColor() { return color; }
    public float getAlpha() { return alpha; }
    public float getScaleX() { return scaleX; }
    public float getScaleY() { return scaleY; }
    public int getMaxAge() { return maxAge; }
    public float getGravity() { return gravity; }
}
