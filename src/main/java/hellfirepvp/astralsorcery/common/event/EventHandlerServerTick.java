/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.auxiliary.TransmutationHelper;
import hellfirepvp.astralsorcery.common.auxiliary.link.LinkHandler;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperDamageCancelling;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperInvulnerability;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperSpawnDeny;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperTemporaryFlight;
import hellfirepvp.astralsorcery.common.util.tick.TickManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Server-side tick handler for global per-tick operations
 * not handled by other specialized event handlers.
 * Registered on the Forge event bus.
 *
 * <p>Handles:
 * <ul>
 *   <li>Transmutation progress decay (once per server tick via overworld)</li>
 *   <li>Server stop cleanup</li>
 * </ul>
 * </p>
 *
 * <p>Note: Starlight network ticking is handled by
 * {@link hellfirepvp.astralsorcery.common.starlight.StarlightNetworkRegistry}.</p>
 *
 * <p>1.16 → 1.20: TickEvent.WorldTickEvent → TickEvent.LevelTickEvent</p>
 */
public class EventHandlerServerTick {

    /** Central tick manager for all server-side ITickHandler instances. */
    public static final TickManager SERVER_TICK_MANAGER = new TickManager();

    @SubscribeEvent
    public void onServerTick(@Nonnull TickEvent.ServerTickEvent event) {
        SERVER_TICK_MANAGER.tick(event);
    }

    @SubscribeEvent
    public void onLevelTick(@Nonnull TickEvent.LevelTickEvent event) {
        if (event.side != LogicalSide.SERVER) {
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Transmutation decay only needs to run once per server tick (overworld gate)
        if (serverLevel.dimension() == Level.OVERWORLD) {
            TransmutationHelper.tickDecay();
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(@Nonnull PlayerEvent.PlayerLoggedOutEvent event) {
        LinkHandler.clearSession(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onServerStopping(@Nonnull ServerStoppingEvent event) {
        TransmutationHelper.clearAll();
        LinkHandler.clearAll();
        EventHelperDamageCancelling.clearServer();
        EventHelperInvulnerability.clearServer();
        EventHelperTemporaryFlight.clearServer();
        EventHelperSpawnDeny.clearServer();
    }
}
