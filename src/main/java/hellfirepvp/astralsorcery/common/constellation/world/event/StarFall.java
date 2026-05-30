package hellfirepvp.astralsorcery.common.constellation.world.event;

import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import net.minecraft.world.level.Level;

import java.util.Random;

/**
 * Star fall event — planned but not yet implemented in the original mod.
 * Stub retained for future visual implementation.
 */
public class StarFall extends CelestialEvent {

    @Override
    public void tick(Level level, Random rand, WorldContext ctx) {}

    @Override
    public boolean isActiveNow() {
        return false;
    }

    @Override
    public boolean isActiveDay() {
        return false;
    }

    @Override
    public float getEffectTick(float pTicks) {
        return 0;
    }

    @Override
    public long getSeedModifier() {
        return 0L;
    }
}
