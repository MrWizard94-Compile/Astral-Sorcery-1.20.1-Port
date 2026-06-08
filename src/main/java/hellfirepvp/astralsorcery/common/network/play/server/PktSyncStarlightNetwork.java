/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.starlight.ClientStarlightNetworkCache;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server -> Client packet that syncs starlight network topology for rendering
 * beam effects. Clients need to know the source position, connected targets,
 * and current starlight level to render transmission beams.
 *
 * <p>1.16 -> 1.20 changes:
 * PacketBuffer -> FriendlyByteBuf,
 * BlockPos read/write unchanged</p>
 */
public class PktSyncStarlightNetwork {

    @Nonnull
    private final BlockPos source;
    @Nonnull
    private final List<BlockPos> connectedTargets;
    private final double starlight;

    /**
     * @param source           the starlight source block position
     * @param connectedTargets list of connected receiver/relay positions
     * @param starlight        current starlight level at the source
     */
    public PktSyncStarlightNetwork(@Nonnull BlockPos source,
                                   @Nonnull List<BlockPos> connectedTargets,
                                   double starlight) {
        this.source = source;
        this.connectedTargets = new ArrayList<>(connectedTargets);
        this.starlight = starlight;
    }

    @Nonnull
    public BlockPos getSource() {
        return source;
    }

    @Nonnull
    public List<BlockPos> getConnectedTargets() {
        return Collections.unmodifiableList(connectedTargets);
    }

    public double getStarlight() {
        return starlight;
    }

    public static void encode(@Nonnull PktSyncStarlightNetwork msg, @Nonnull FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.source);
        buf.writeDouble(msg.starlight);
        buf.writeVarInt(msg.connectedTargets.size());
        for (BlockPos target : msg.connectedTargets) {
            buf.writeBlockPos(target);
        }
    }

    @Nonnull
    public static PktSyncStarlightNetwork decode(@Nonnull FriendlyByteBuf buf) {
        BlockPos source = buf.readBlockPos();
        double starlight = buf.readDouble();
        int count = buf.readVarInt();
        List<BlockPos> targets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            targets.add(buf.readBlockPos());
        }
        return new PktSyncStarlightNetwork(source, targets, starlight);
    }

    public static void handle(@Nonnull PktSyncStarlightNetwork msg,
                              @Nonnull Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> handleClient(msg));
        ctx.setPacketHandled(true);
    }

    private static void handleClient(@Nonnull PktSyncStarlightNetwork msg) {
        ClientStarlightNetworkCache.updateSource(msg.source, msg.connectedTargets, msg.starlight);
        AstralSorcery.log.debug("Synced starlight network: source={}, targets={}, starlight={}",
                msg.source.toShortString(), msg.connectedTargets.size(), msg.starlight);
    }
}
