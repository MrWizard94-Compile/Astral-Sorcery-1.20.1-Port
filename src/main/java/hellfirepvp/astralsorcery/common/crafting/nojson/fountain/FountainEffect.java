package hellfirepvp.astralsorcery.common.crafting.nojson.fountain;

import hellfirepvp.astralsorcery.common.tile.BlockEntityFountain;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base class for fountain effects. A concrete effect is selected by the BlockFountainPrime
 * block placed directly below the fountain. The effect runs through STARTUP → PREPARATION → RUNNING
 * phases, consuming liquid starlight each tick while active.
 */
public abstract class FountainEffect<C> {

    public enum OperationSegment {
        /** No effect running — fountain has no prime or no starlight. */
        INACTIVE(-1),
        /** Initial ramp-up (60 ticks). */
        STARTUP(60),
        /** Wind-up before active work (200 ticks). */
        PREPARATION(200),
        /** Active effect phase — runs indefinitely. */
        RUNNING(-1);

        /** Ticks to spend in this segment before advancing; -1 means infinite. */
        public final int duration;

        OperationSegment(int duration) {
            this.duration = duration;
        }
    }

    private final ResourceLocation id;

    protected FountainEffect(@Nonnull ResourceLocation id) {
        this.id = id;
    }

    @Nonnull
    public ResourceLocation getId() {
        return id;
    }

    /** Called once when a prime is first detected; create and return a new context object. */
    @Nonnull
    public abstract C createContext(@Nonnull BlockEntityFountain fountain);

    /** Called every server tick while this effect is active. */
    public abstract void tick(@Nonnull BlockEntityFountain fountain, @Nonnull C context,
                               int operationTick, @Nonnull OperationSegment segment);

    /** Called when the segment advances (e.g., STARTUP → PREPARATION, PREPARATION → RUNNING). */
    public abstract void transition(@Nonnull BlockEntityFountain fountain, @Nonnull C context,
                                     @Nonnull OperationSegment prev, @Nonnull OperationSegment next);

    /** Called when this effect is replaced (prime block changed or fountain deactivated). */
    public abstract void onReplace(@Nonnull BlockEntityFountain fountain, @Nonnull C context,
                                    @Nullable FountainEffect<?> newEffect);
}
