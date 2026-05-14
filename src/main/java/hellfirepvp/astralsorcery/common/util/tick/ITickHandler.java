package hellfirepvp.astralsorcery.common.util.tick;

import net.minecraftforge.event.TickEvent;

import javax.annotation.Nonnull;
import java.util.EnumSet;

/**
 * Replacement for ObserverLib's ITickHandler.
 * Provides a contract for tick-based handlers that can be registered with the TickManager.
 */
public interface ITickHandler {

    /**
     * Called each tick for each handled tick type.
     *
     * @param type    the tick event type firing
     * @param context additional context (varies by type)
     */
    void tick(@Nonnull TickEvent.Type type, @Nonnull Object... context);

    /**
     * Returns the set of tick event types this handler responds to.
     */
    @Nonnull
    EnumSet<TickEvent.Type> getHandledTypes();

    /**
     * Whether this handler fires on the given phase (START or END).
     *
     * @param phase the tick event phase
     * @return true if this handler should fire
     */
    boolean canFire(@Nonnull TickEvent.Phase phase);

    /**
     * A human-readable name for this tick handler (used for debugging).
     */
    @Nonnull
    String getName();
}
