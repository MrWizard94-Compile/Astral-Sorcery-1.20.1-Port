/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import javax.annotation.Nonnull;

/**
 * Mutable property bag controlling ritual effect parameters such as range,
 * potency, and whether the effect is enabled. Trait modifiers and lenses on
 * the pedestal can adjust these properties before each tick.
 *
 * <p>Instances are created per-tick by the ritual pedestal and passed into
 * {@link ConstellationEffectProvider#tick}.</p>
 */
public class ConstellationEffectProperties {

    private double size;
    private double potency;
    private boolean corruptionEffect;

    public ConstellationEffectProperties(double size) {
        this(size, 1.0);
    }

    public ConstellationEffectProperties(double size, double potency) {
        this.size = size;
        this.potency = potency;
        this.corruptionEffect = false;
    }

    /**
     * The radius (in blocks) of the ritual's area of effect.
     */
    public double getSize() {
        return size;
    }

    @Nonnull
    public ConstellationEffectProperties setSize(double size) {
        this.size = size;
        return this;
    }

    @Nonnull
    public ConstellationEffectProperties multiplySize(double multiplier) {
        this.size *= multiplier;
        return this;
    }

    /**
     * A general potency multiplier applied to the strength of the effect.
     * Higher values mean stronger/faster/more frequent applications.
     */
    public double getPotency() {
        return potency;
    }

    @Nonnull
    public ConstellationEffectProperties setPotency(double potency) {
        this.potency = potency;
        return this;
    }

    @Nonnull
    public ConstellationEffectProperties multiplyPotency(double multiplier) {
        this.potency *= multiplier;
        return this;
    }

    /**
     * Whether the effect is running in a corrupted/inverted mode.
     * Corruption may invert or alter the effect's behaviour.
     */
    public boolean isCorrupted() {
        return corruptionEffect;
    }

    @Nonnull
    public ConstellationEffectProperties setCorrupted(boolean corrupted) {
        this.corruptionEffect = corrupted;
        return this;
    }

    /**
     * Create a copy of these properties.
     */
    @Nonnull
    public ConstellationEffectProperties copy() {
        ConstellationEffectProperties copy = new ConstellationEffectProperties(size, potency);
        copy.corruptionEffect = this.corruptionEffect;
        return copy;
    }
}
