package hellfirepvp.astralsorcery.common.constellation.world.event;

import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import net.minecraft.world.level.Level;

import java.util.Random;

/**
 * Abstract base for celestial events (solar eclipse, lunar eclipse, star fall).
 * Each event computes its active state and tick progress from the world time.
 *
 * <p>1.16 → 1.20: World → Level.</p>
 */
public abstract class CelestialEvent {

    public abstract void tick(Level level, Random rand, WorldContext ctx);

    public abstract boolean isActiveNow();

    /** Whether today is the day (or night) on which this event occurs. */
    public abstract boolean isActiveDay();

    public abstract float getEffectTick(float pTicks);

    /** Seed modifier used when generating the deterministic random for this event. */
    public abstract long getSeedModifier();
}
