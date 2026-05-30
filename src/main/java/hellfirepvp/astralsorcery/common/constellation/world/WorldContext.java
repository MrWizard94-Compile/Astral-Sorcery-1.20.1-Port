package hellfirepvp.astralsorcery.common.constellation.world;

import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Per-dimension context holding constellation handler state derived from the world seed.
 * Created by {@link SkyHandler} when a world ticks for the first time.
 *
 * <p>1.16 → 1.20: World → Level.</p>
 */
public class WorldContext {

    private final long seed;
    private final ConstellationHandler constellationHandler;

    public WorldContext(long seed) {
        this.seed = seed;
        this.constellationHandler = new ConstellationHandler(this);
    }

    public long getSeed() {
        return seed;
    }

    @Nonnull
    public Random getRandom() {
        return getRandom(0L);
    }

    @Nonnull
    public Random getRandom(long modifier) {
        return new Random(seed + modifier);
    }

    @Nonnull
    public ConstellationHandler getConstellationHandler() {
        return constellationHandler;
    }

    public void tick(Level level) {
        constellationHandler.tick(level);
    }
}
