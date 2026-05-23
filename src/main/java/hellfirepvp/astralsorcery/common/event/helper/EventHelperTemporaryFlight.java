package hellfirepvp.astralsorcery.common.event.helper;

import hellfirepvp.astralsorcery.common.util.tick.ITickHandler;
import hellfirepvp.astralsorcery.common.util.tick.TimeoutList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.function.Consumer;

/**
 * Grants temporary flight to a player for a configurable number of ticks.
 * Flight is revoked when the timeout expires (unless the player is creative/spectator).
 *
 * <p>1.16 → 1.20: PlayerEntity → Player, ServerPlayerEntity → ServerPlayer,
 * interactionManager.getGameType().isSurvivalOrAdventure() → !isCreative() && !isSpectator(),
 * player.abilities.allowFlying → getAbilities().mayfly,
 * player.abilities.isFlying → getAbilities().flying,
 * player.sendPlayerAbilities() → player.onUpdateAbilities().</p>
 */
public class EventHelperTemporaryFlight {

    private static final TimeoutList<Player> temporaryFlight = new TimeoutList<>(player -> {
        if (player instanceof ServerPlayer serverPlayer
                && !serverPlayer.isCreative() && !serverPlayer.isSpectator()) {
            serverPlayer.getAbilities().mayfly = false;
            serverPlayer.getAbilities().flying = false;
            serverPlayer.onUpdateAbilities();
        }
    }, TickEvent.Type.SERVER);

    private EventHelperTemporaryFlight() {}

    public static void clearServer() {
        temporaryFlight.clear();
    }

    public static void attachListeners(IEventBus eventBus) {
        eventBus.addListener(EventHelperTemporaryFlight::onDisconnect);
    }

    private static void onDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            temporaryFlight.remove(serverPlayer);
        }
    }

    public static void attachTickListener(Consumer<ITickHandler> registrar) {
        registrar.accept(temporaryFlight);
    }

    public static boolean allowFlight(Player player) {
        return allowFlight(player, 20);
    }

    public static boolean allowFlight(Player player, int timeout) {
        return temporaryFlight.setOrAddTimeout(timeout, player);
    }
}
