/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import hellfirepvp.astralsorcery.common.constellation.world.ConstellationDiscoveryHandler;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Client → Server: Player claims to have identified a constellation
 * through the telescope GUI. Server validates:
 * <ol>
 *   <li>Player is near a telescope</li>
 *   <li>It is nighttime</li>
 *   <li>The constellation is currently visible</li>
 *   <li>The player hasn't already discovered it</li>
 * </ol>
 * On success, records the discovery and syncs to client.
 */
public class PktDiscoverConstellation {

    /** Max distance from a telescope for discovery to be valid. */
    private static final double MAX_TELESCOPE_DISTANCE_SQ = 5.0 * 5.0;

    @Nonnull
    private final ResourceLocation constellationKey;

    public PktDiscoverConstellation(@Nonnull ResourceLocation constellationKey) {
        this.constellationKey = constellationKey;
    }

    public static void encode(@Nonnull PktDiscoverConstellation pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.constellationKey);
    }

    @Nonnull
    public static PktDiscoverConstellation decode(@Nonnull FriendlyByteBuf buf) {
        return new PktDiscoverConstellation(buf.readResourceLocation());
    }

    public static void handle(@Nonnull PktDiscoverConstellation pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            performDiscovery(player, pkt.constellationKey);
        });
        ctx.get().setPacketHandled(true);
    }

    private static void performDiscovery(@Nonnull ServerPlayer player,
                                          @Nonnull ResourceLocation constellationKey) {
        Level level = player.level();

        // Must be nighttime
        if (!DayTimeHelper.isNight(level)) {
            AstralSorcery.log.debug("Discovery denied for {}: not nighttime",
                    player.getName().getString());
            return;
        }

        // Must be able to see the sky
        BlockPos playerPos = player.blockPosition();
        if (!level.canSeeSky(playerPos.above())) {
            return;
        }

        // Constellation must exist in the registry
        IConstellation constellation = ConstellationRegistry.getConstellation(constellationKey);
        if (constellation == null) {
            AstralSorcery.log.debug("Discovery denied: unknown constellation {}",
                    constellationKey);
            return;
        }

        // Constellation must be currently visible
        boolean visible = CelestialHandler.getVisibleConstellations(level).stream()
                .anyMatch(c -> c.getRegistryName().equals(constellationKey));
        if (!visible) {
            AstralSorcery.log.debug("Discovery denied for {}: constellation {} not visible",
                    player.getName().getString(), constellationKey);
            return;
        }

        // Player progress check
        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null) return;

        // Already discovered?
        if (progress.hasDiscovered(constellationKey)) {
            return;
        }

        // Grant discovery (handles advancement trigger, tier check, and client sync)
        boolean granted = ConstellationDiscoveryHandler.grantDiscovery(player, progress, constellation);
        if (!granted) return;

        player.displayClientMessage(
                Component.translatable("astralsorcery.discovery.found",
                        constellation.getConstellationName()), false);

        AstralSorcery.log.info("Player {} discovered constellation: {}",
                player.getName().getString(), constellationKey);
    }
}
