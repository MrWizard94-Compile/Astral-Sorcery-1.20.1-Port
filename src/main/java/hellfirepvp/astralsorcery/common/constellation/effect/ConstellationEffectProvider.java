/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;

/**
 * Base class for all constellation ritual-pedestal effects.
 *
 * <p>Each concrete subclass represents one constellation and defines what
 * happens in the area around a Ritual Pedestal when that constellation is
 * focused through an attuned crystal.</p>
 *
 * <p>Subclasses implement {@link #tick(Level, BlockPos, ConstellationEffectProperties)}
 * which is invoked by the Ritual Pedestal block entity on each effect-application
 * interval while the pedestal is active and has sufficient starlight.</p>
 */
public abstract class ConstellationEffectProvider {

    private final String constellationName;

    protected ConstellationEffectProvider(@Nonnull String constellationName) {
        this.constellationName = constellationName;
    }

    /**
     * Apply this constellation's area effect.
     *
     * @param level      the server level the pedestal resides in
     * @param pos        the position of the ritual pedestal
     * @param properties the current effect properties (range, potency, etc.)
     */
    public abstract void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                              @Nonnull ConstellationEffectProperties properties);

    /**
     * Return the default properties for this effect (range, base potency).
     * Called once when the ritual first activates to seed the property bag.
     */
    @Nonnull
    public abstract ConstellationEffectProperties provideProperties();

    /**
     * Convenience helper: create an AABB centred on the given position with
     * a radius taken from the provided properties.
     */
    @Nonnull
    protected AABB createArea(@Nonnull BlockPos pos,
                              @Nonnull ConstellationEffectProperties properties) {
        double range = properties.getSize();
        return new AABB(pos).inflate(range);
    }

    /**
     * The simple name of the constellation this provider serves.
     */
    @Nonnull
    public String getConstellationName() {
        return constellationName;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{constellation=" + constellationName + "}";
    }
}
