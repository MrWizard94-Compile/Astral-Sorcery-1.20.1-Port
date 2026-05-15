/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Forge event handler that drives the starlight network tick loop
 * and handles player-related sync events.
 *
 * <p>Registered on the Forge event bus in {@code CommonProxy.attachEventHandlers()}.
 * Fires once per server tick for each loaded dimension, distributing
 * starlight through each dimension's {@link WorldNetworkHandler}.</p>
 *
 * <p>Also syncs the full network state to players on login and dimension change
 * so their client can render transmission beams immediately.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * WorldTickEvent -> TickEvent.LevelTickEvent (level-scoped ticking),
 * ServerWorld -> ServerLevel,
 * PlayerLoggedInEvent / PlayerChangedDimensionEvent stable</p>
 */
public class StarlightNetworkRegistry {

    /**
     * Ticks the starlight network for each server-side dimension.
     * Fires at the END of each level tick so all block entities have
     * already ticked and updated their production/reception values.
     */
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

        WorldNetworkHandler handler = WorldNetworkHandler.getOrCreate(serverLevel);
        handler.tick(serverLevel);
    }

    /**
     * Syncs the full starlight network to a player when they log in.
     */
    @SubscribeEvent
    public void onPlayerLogin(@Nonnull PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ServerLevel level = serverPlayer.serverLevel();
            WorldNetworkHandler handler = WorldNetworkHandler.getOrCreate(level);
            handler.syncAllToPlayer(serverPlayer, level);
            AstralSorcery.log.debug("Synced starlight network to player {} on login",
                    serverPlayer.getName().getString());
        }
    }

    /**
     * Syncs the new dimension's starlight network to a player on dimension change.
     */
    @SubscribeEvent
    public void onPlayerChangedDimension(@Nonnull PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ServerLevel level = serverPlayer.serverLevel();
            WorldNetworkHandler handler = WorldNetworkHandler.getOrCreate(level);
            handler.syncAllToPlayer(serverPlayer, level);
            AstralSorcery.log.debug("Synced starlight network to player {} after dimension change to {}",
                    serverPlayer.getName().getString(), event.getTo().location());
        }
    }

    /**
     * Syncs the starlight network after a player respawns (death, end portal).
     */
    @SubscribeEvent
    public void onPlayerRespawn(@Nonnull PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ServerLevel level = serverPlayer.serverLevel();
            WorldNetworkHandler handler = WorldNetworkHandler.getOrCreate(level);
            handler.syncAllToPlayer(serverPlayer, level);
        }
    }
}
