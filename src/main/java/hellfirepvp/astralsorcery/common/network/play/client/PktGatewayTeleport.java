/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import hellfirepvp.astralsorcery.common.data.world.GatewayHandler;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktParticleEvent;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.tile.BlockEntityGateway;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Client → Server: Player requests teleportation via a Celestial Gateway.
 * Sends the target gateway position and dimension. Server validates:
 * <ol>
 *   <li>Player is near a valid source gateway</li>
 *   <li>Target gateway exists in the network</li>
 *   <li>Target dimension is loaded or loadable</li>
 * </ol>
 * Then teleports the player (cross-dimension if needed).
 */
public class PktGatewayTeleport {

    /** Maximum distance from a gateway for a teleport request to be valid. */
    private static final double MAX_SOURCE_DISTANCE_SQ = 6.0 * 6.0;

    @Nonnull
    private final BlockPos targetPos;
    @Nonnull
    private final String targetDimension;

    public PktGatewayTeleport(@Nonnull BlockPos targetPos, @Nonnull String targetDimension) {
        this.targetPos = targetPos;
        this.targetDimension = targetDimension;
    }

    public static void encode(@Nonnull PktGatewayTeleport pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.targetPos);
        buf.writeUtf(pkt.targetDimension);
    }

    @Nonnull
    public static PktGatewayTeleport decode(@Nonnull FriendlyByteBuf buf) {
        return new PktGatewayTeleport(buf.readBlockPos(), buf.readUtf(256));
    }

    public static void handle(@Nonnull PktGatewayTeleport pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            performTeleport(player, pkt.targetPos, pkt.targetDimension);
        });
        ctx.get().setPacketHandled(true);
    }

    private static void performTeleport(@Nonnull ServerPlayer player,
                                         @Nonnull BlockPos targetPos,
                                         @Nonnull String targetDimension) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        // Step 1: Verify player is near a source gateway
        ServerLevel sourceLevel = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        BlockEntityGateway sourceGateway = findNearbyGateway(sourceLevel, playerPos);
        if (sourceGateway == null) {
            AstralSorcery.log.debug("Gateway teleport denied for {}: not near a gateway",
                    player.getName().getString());
            return;
        }

        // Step 2: Verify target gateway exists in network
        GatewayHandler handler = GatewayHandler.get(server);
        ResourceKey<Level> targetDimKey = ResourceKey.create(
                Registries.DIMENSION, new ResourceLocation(targetDimension));

        GatewayHandler.GatewayEntry targetEntry = handler.getGatewayAt(targetDimKey, targetPos);
        if (targetEntry == null) {
            AstralSorcery.log.debug("Gateway teleport denied for {}: target gateway not in network at {} ({})",
                    player.getName().getString(), targetPos, targetDimension);
            return;
        }

        // Step 3: Cross-dimension gate check
        boolean sameLevel = sourceLevel.dimension().equals(targetDimKey);
        if (!sameLevel && !CommonConfig.CONFIG.gatewayCrossDimensional.get()) {
            AstralSorcery.log.debug("Gateway teleport denied for {}: cross-dimensional disabled by config",
                    player.getName().getString());
            return;
        }

        // Step 4: Range check (0 = unlimited)
        int maxRange = CommonConfig.CONFIG.gatewayMaxRange.get();
        if (maxRange > 0 && sameLevel) {
            double distSq = sourceGateway.getBlockPos().distSqr(targetPos);
            if (distSq > (long) maxRange * maxRange) {
                AstralSorcery.log.debug("Gateway teleport denied for {}: target too far ({} > {})",
                        player.getName().getString(), Math.sqrt(distSq), maxRange);
                return;
            }
        }

        // Step 5: Get or load the target dimension
        ServerLevel targetLevel = server.getLevel(targetDimKey);
        if (targetLevel == null) {
            AstralSorcery.log.warn("Gateway teleport denied: dimension {} not loaded",
                    targetDimension);
            return;
        }

        // Step 6: Teleport the player
        double destX = targetPos.getX() + 0.5;
        double destY = targetPos.getY() + 1.0; // Stand on top of gateway block
        double destZ = targetPos.getZ() + 0.5;

        if (sameLevel) {
            // Same dimension: simple teleport
            player.teleportTo(destX, destY, destZ);
        } else {
            // Cross-dimension teleport
            player.teleportTo(targetLevel, destX, destY, destZ,
                    player.getYRot(), player.getXRot());
        }

        AstralSorcery.log.debug("Gateway teleport: {} -> {} in {}",
                player.getName().getString(), targetPos, targetDimension);

        BlockPos sourcePos = BlockPos.containing(player.position());
        PacketChannel.sendToAllTracking(
                new PktParticleEvent(PktParticleEvent.GATEWAY_ACTIVATE, sourcePos),
                sourceLevel, sourcePos);
        PacketChannel.sendToAllTracking(
                new PktParticleEvent(PktParticleEvent.GATEWAY_ACTIVATE, targetPos),
                targetLevel, targetPos);
        PacketChannel.sendToAllTracking(
                new PktPlayEffect(PktPlayEffect.EffectType.GATEWAY_TELEPORT, sourcePos),
                sourceLevel, sourcePos);
        PacketChannel.sendToAllTracking(
                new PktPlayEffect(PktPlayEffect.EffectType.GATEWAY_TELEPORT, targetPos),
                targetLevel, targetPos);
        sourceLevel.playSound(null, sourcePos,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        targetLevel.playSound(null, targetPos,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.9F);
    }

    /**
     * Finds a gateway block entity within range of the given position.
     * Checks a small radius around the player for a valid source gateway.
     */
    @javax.annotation.Nullable
    private static BlockEntityGateway findNearbyGateway(@Nonnull ServerLevel level,
                                                         @Nonnull BlockPos center) {
        int searchRadius = 4;
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    BlockPos checkPos = center.offset(dx, dy, dz);
                    if (center.distSqr(checkPos) > MAX_SOURCE_DISTANCE_SQ) {
                        continue;
                    }
                    BlockEntity be = level.getBlockEntity(checkPos);
                    if (be instanceof BlockEntityGateway gateway && gateway.isStructureValid()) {
                        return gateway;
                    }
                }
            }
        }
        return null;
    }
}
