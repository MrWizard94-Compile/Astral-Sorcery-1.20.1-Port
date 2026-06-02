/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.world;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.advancement.AstralAdvancementTriggers;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktDiscoveryUpdate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handles the constellation discovery process. When a player gazes at
 * the night sky through a telescope or the naked eye, this handler
 * determines which constellation they can potentially discover and
 * grants the discovery to their {@link PlayerProgress}.
 *
 * <p>Discovery requirements:
 * <ul>
 *   <li>Must be nighttime</li>
 *   <li>Sky must be clear (no rain/thunder)</li>
 *   <li>Constellation must be visible this night (moon phase + rotation)</li>
 *   <li>Player must have sufficient progression tier</li>
 *   <li>Player must not have already discovered it</li>
 * </ul></p>
 *
 * <p>1.16 → 1.20 changes:
 * ServerPlayerEntity → ServerPlayer,
 * PlayerEntity → Player</p>
 */
public final class ConstellationDiscoveryHandler {

    private ConstellationDiscoveryHandler() {}

    /**
     * Attempts to discover a constellation for the player.
     * Called when a player uses a telescope or hand telescope at night.
     *
     * @param player   the player attempting discovery
     * @param progress the player's current progress data
     * @return the discovered constellation, or null if none available
     */
    @Nullable
    public static IConstellation tryDiscoverConstellation(@Nonnull Player player,
                                                          @Nonnull PlayerProgress progress) {
        Level level = player.level();

        // Must be nighttime with clear sky
        if (!CelestialHandler.isNight(level) || !CelestialHandler.isClearSky(level)) {
            return null;
        }

        // Get visible constellations this night
        Set<IConstellation> visible = CelestialHandler.getVisibleConstellations(level);

        // Filter to undiscovered constellations the player can discover
        List<IConstellation> discoverable = new ArrayList<>();
        for (IConstellation constellation : visible) {
            ResourceLocation key = constellation.getRegistryName();
            if (progress.hasDiscovered(key)) {
                continue; // Already known
            }
            if (!constellation.canDiscover(player, progress)) {
                continue; // Tier requirement not met
            }
            discoverable.add(constellation);
        }

        if (discoverable.isEmpty()) {
            return null;
        }

        // Select one deterministically based on player + day seed
        long seed = CelestialHandler.getDayNumber(level) * 31L + player.getUUID().hashCode();
        int index = (int) (Math.abs(seed) % discoverable.size());
        IConstellation discovered = discoverable.get(index);

        // Grant the discovery
        progress.discoverConstellation(discovered.getRegistryName());

        // Sync to client if server player
        if (player instanceof ServerPlayer serverPlayer) {
            AstralAdvancementTriggers.DISCOVER_CONSTELLATION.trigger(serverPlayer, discovered);
            PacketChannel.sendToPlayer(
                    new PktDiscoveryUpdate(discovered.getRegistryName(), true), serverPlayer);
            AstralSorcery.log.info("Player {} discovered constellation: {}",
                    serverPlayer.getName().getString(), discovered.getRegistryName());
        }

        return discovered;
    }

    /**
     * Grants a specific constellation discovery to a player (admin/command usage).
     *
     * @param player        the player
     * @param progress      the player's progress
     * @param constellation the constellation to discover
     * @return true if the constellation was newly discovered
     */
    public static boolean grantDiscovery(@Nonnull Player player,
                                          @Nonnull PlayerProgress progress,
                                          @Nonnull IConstellation constellation) {
        ResourceLocation key = constellation.getRegistryName();
        if (progress.hasDiscovered(key)) {
            return false; // Already known
        }

        progress.discoverConstellation(key);

        if (player instanceof ServerPlayer serverPlayer) {
            AstralAdvancementTriggers.DISCOVER_CONSTELLATION.trigger(serverPlayer, constellation);
            PacketChannel.sendToPlayer(new PktDiscoveryUpdate(key, true), serverPlayer);
        }
        return true;
    }

    /**
     * Grants all constellation discoveries to a player (admin/command).
     *
     * @param player   the player
     * @param progress the player's progress
     * @return count of newly discovered constellations
     */
    public static int grantAllDiscoveries(@Nonnull Player player,
                                           @Nonnull PlayerProgress progress) {
        int count = 0;
        for (IConstellation constellation : ConstellationRegistry.getAllConstellations()) {
            if (grantDiscovery(player, progress, constellation)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks if a player can potentially discover any new constellation
     * on the current night.
     */
    public static boolean hasDiscoverableConstellations(@Nonnull Player player,
                                                        @Nonnull PlayerProgress progress) {
        Level level = player.level();
        if (!CelestialHandler.isNight(level) || !CelestialHandler.isClearSky(level)) {
            return false;
        }

        Set<IConstellation> visible = CelestialHandler.getVisibleConstellations(level);
        for (IConstellation constellation : visible) {
            if (!progress.hasDiscovered(constellation.getRegistryName())
                    && constellation.canDiscover(player, progress)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the number of constellations a player has discovered.
     */
    public static int getDiscoveredCount(@Nonnull PlayerProgress progress) {
        return progress.getDiscoveredConstellations().size();
    }

    /**
     * Gets the total number of constellations available to discover.
     */
    public static int getTotalConstellationCount() {
        return ConstellationRegistry.getAllConstellations().size();
    }
}
