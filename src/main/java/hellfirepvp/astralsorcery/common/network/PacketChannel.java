/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.network.play.client.PktAttunePlayer;
import hellfirepvp.astralsorcery.common.network.play.client.PktDiscoverConstellation;
import hellfirepvp.astralsorcery.common.network.play.client.PktGatewayTeleport;
import hellfirepvp.astralsorcery.common.network.play.client.PktObservatoryUpdate;
import hellfirepvp.astralsorcery.common.network.play.client.PktPerkAllocate;
import hellfirepvp.astralsorcery.common.network.play.client.PktPerkDeallocate;
import hellfirepvp.astralsorcery.common.network.play.client.PktPerkSealAction;
import hellfirepvp.astralsorcery.common.network.play.client.PktRequestGatewayList;
import hellfirepvp.astralsorcery.common.network.play.client.PktRequestProgress;
import hellfirepvp.astralsorcery.common.network.play.server.PktAltarCraftingUpdate;
import hellfirepvp.astralsorcery.common.network.play.server.PktDiscoveryUpdate;
import hellfirepvp.astralsorcery.common.network.play.server.PktParticleEvent;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncAttunement;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncBlockEntity;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncConfig;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncConstellation;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncGatewayList;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncKnowledgeFragments;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncPerkData;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncPlayerProgress;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncResearch;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncStarlightCharge;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncStarlightNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Central packet registry and channel for Astral Sorcery's custom networking.
 * Creates a single {@link SimpleChannel} and registers all mod packets with
 * auto-incrementing IDs.
 *
 * <p>Call {@link #init()} from {@code CommonProxy.onCommonSetup} inside
 * {@code event.enqueueWork()} to ensure thread-safe registration.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * NetworkRegistry.newSimpleChannel signature changed,
 * PacketDistributor replaces direct target sending,
 * INSTANCE field renamed to CHANNEL for clarity</p>
 */
public class PacketChannel {

    private static final String PROTOCOL_VERSION = "1";

    /**
     * The mod's network channel. All custom packets are sent and received through this.
     */
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            AstralSorcery.key("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    /**
     * Registers all mod packets. Must be called exactly once during common setup.
     */
    public static void init() {
        // Server -> Client packets
        register(PktSyncPlayerProgress.class,
                PktSyncPlayerProgress::encode,
                PktSyncPlayerProgress::decode,
                PktSyncPlayerProgress::handle);

        register(PktSyncBlockEntity.class,
                PktSyncBlockEntity::encode,
                PktSyncBlockEntity::decode,
                PktSyncBlockEntity::handle);

        register(PktSyncConstellation.class,
                PktSyncConstellation::encode,
                PktSyncConstellation::decode,
                PktSyncConstellation::handle);

        register(PktSyncStarlightNetwork.class,
                PktSyncStarlightNetwork::encode,
                PktSyncStarlightNetwork::decode,
                PktSyncStarlightNetwork::handle);

        register(PktParticleEvent.class,
                PktParticleEvent::encode,
                PktParticleEvent::decode,
                PktParticleEvent::handle);

        register(PktSyncPerkData.class,
                PktSyncPerkData::encode,
                PktSyncPerkData::decode,
                PktSyncPerkData::handle);

        register(PktDiscoveryUpdate.class,
                PktDiscoveryUpdate::encode,
                PktDiscoveryUpdate::decode,
                PktDiscoveryUpdate::handle);

        register(PktAltarCraftingUpdate.class,
                PktAltarCraftingUpdate::encode,
                PktAltarCraftingUpdate::decode,
                PktAltarCraftingUpdate::handle);

        register(PktSyncStarlightCharge.class,
                PktSyncStarlightCharge::encode,
                PktSyncStarlightCharge::decode,
                PktSyncStarlightCharge::handle);

        // Client -> Server packets
        register(PktRequestProgress.class,
                PktRequestProgress::encode,
                PktRequestProgress::decode,
                PktRequestProgress::handle);

        register(PktAttunePlayer.class,
                PktAttunePlayer::encode,
                PktAttunePlayer::decode,
                PktAttunePlayer::handle);

        register(PktPerkAllocate.class,
                PktPerkAllocate::encode,
                PktPerkAllocate::decode,
                PktPerkAllocate::handle);

        register(PktPerkDeallocate.class,
                PktPerkDeallocate::encode,
                PktPerkDeallocate::decode,
                PktPerkDeallocate::handle);

        register(PktGatewayTeleport.class,
                PktGatewayTeleport::encode,
                PktGatewayTeleport::decode,
                PktGatewayTeleport::handle);

        register(PktDiscoverConstellation.class,
                PktDiscoverConstellation::encode,
                PktDiscoverConstellation::decode,
                PktDiscoverConstellation::handle);

        register(PktObservatoryUpdate.class,
                PktObservatoryUpdate::encode,
                PktObservatoryUpdate::decode,
                PktObservatoryUpdate::handle);

        register(PktRequestGatewayList.class,
                PktRequestGatewayList::encode,
                PktRequestGatewayList::decode,
                PktRequestGatewayList::handle);

        // Additional server -> client packets
        register(PktPlayEffect.class,
                PktPlayEffect::encode,
                PktPlayEffect::decode,
                PktPlayEffect::handle);

        register(PktSyncGatewayList.class,
                PktSyncGatewayList::encode,
                PktSyncGatewayList::decode,
                PktSyncGatewayList::handle);

        register(PktSyncAttunement.class,
                PktSyncAttunement::encode,
                PktSyncAttunement::decode,
                PktSyncAttunement::handle);

        register(PktSyncResearch.class,
                PktSyncResearch::encode,
                PktSyncResearch::decode,
                PktSyncResearch::handle);

        register(PktSyncConfig.class,
                PktSyncConfig::encode,
                PktSyncConfig::decode,
                PktSyncConfig::handle);

        register(PktSyncKnowledgeFragments.class,
                PktSyncKnowledgeFragments::encode,
                PktSyncKnowledgeFragments::decode,
                PktSyncKnowledgeFragments::handle);

        register(PktPerkSealAction.class,
                PktPerkSealAction::encode,
                PktPerkSealAction::decode,
                PktPerkSealAction::handle);
    }

    /**
     * Sends a packet from the server to a specific player.
     *
     * @param player the target player
     * @param msg    the packet to send
     */
    public static <MSG> void sendToPlayer(@Nonnull MSG msg, @Nonnull ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    /**
     * Sends a packet from the server to all players currently tracking the given chunk.
     *
     * @param msg   the packet to send
     * @param level the server level
     * @param pos   a block position within the target chunk
     */
    public static <MSG> void sendToAllTracking(@Nonnull MSG msg,
                                               @Nonnull net.minecraft.server.level.ServerLevel level,
                                               @Nonnull net.minecraft.core.BlockPos pos) {
        CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(
                () -> level.getChunkAt(pos)), msg);
    }

    /**
     * Sends a packet from the server to all connected players.
     *
     * @param msg the packet to send
     */
    public static <MSG> void sendToAll(@Nonnull MSG msg) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
    }

    /**
     * Sends a packet from the client to the server.
     *
     * @param msg the packet to send
     */
    public static <MSG> void sendToServer(@Nonnull MSG msg) {
        CHANNEL.sendToServer(msg);
    }

    private static <MSG> void register(@Nonnull Class<MSG> clazz,
                                       @Nonnull BiConsumer<MSG, FriendlyByteBuf> encoder,
                                       @Nonnull Function<FriendlyByteBuf, MSG> decoder,
                                       @Nonnull BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        CHANNEL.registerMessage(packetId++, clazz, encoder, decoder, handler);
    }
}
