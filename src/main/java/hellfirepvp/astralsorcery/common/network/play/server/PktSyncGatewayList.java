/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.data.world.GatewayHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server → Client: sends the full gateway network list.
 * The client uses this to display the gateway selection UI
 * with all available teleportation targets.
 *
 * <p>Each entry contains: gateway UUID, display name, dimension, position.</p>
 *
 * <p>1.16 → 1.20: ResourceKey serialization uses registry + location.
 * BlockPos read/write stable.</p>
 */
public class PktSyncGatewayList {

    @Nonnull
    private final List<GatewayClientEntry> entries;

    public PktSyncGatewayList(@Nonnull List<GatewayHandler.GatewayEntry> serverEntries) {
        this.entries = new ArrayList<>();
        for (GatewayHandler.GatewayEntry entry : serverEntries) {
            entries.add(new GatewayClientEntry(
                    entry.id,
                    entry.getLabel(),
                    entry.dimension.location(),
                    entry.pos
            ));
        }
    }

    private PktSyncGatewayList(@Nonnull List<GatewayClientEntry> entries, boolean raw) {
        this.entries = entries;
    }

    @Nonnull
    public List<GatewayClientEntry> getEntries() {
        return entries;
    }

    public static void encode(@Nonnull PktSyncGatewayList pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.entries.size());
        for (GatewayClientEntry entry : pkt.entries) {
            buf.writeUUID(entry.gatewayId);
            buf.writeUtf(entry.displayName, 128);
            buf.writeResourceLocation(entry.dimension);
            buf.writeBlockPos(entry.position);
        }
    }

    @Nonnull
    public static PktSyncGatewayList decode(@Nonnull FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<GatewayClientEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            UUID id = buf.readUUID();
            String name = buf.readUtf(128);
            ResourceLocation dim = buf.readResourceLocation();
            BlockPos pos = buf.readBlockPos();
            entries.add(new GatewayClientEntry(id, name, dim, pos));
        }
        return new PktSyncGatewayList(entries, true);
    }

    public static void handle(@Nonnull PktSyncGatewayList pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side: store the gateway list for UI display
            GatewayClientCache.setEntries(pkt.entries);
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Client-side entry for display in the gateway UI.
     */
    public record GatewayClientEntry(
            @Nonnull UUID gatewayId,
            @Nonnull String displayName,
            @Nonnull ResourceLocation dimension,
            @Nonnull BlockPos position
    ) {}

    /**
     * Client-side cache for the gateway list received from server.
     * Accessed by the gateway selection screen.
     */
    public static class GatewayClientCache {
        @Nonnull
        private static List<GatewayClientEntry> cachedEntries = new ArrayList<>();

        public static void setEntries(@Nonnull List<GatewayClientEntry> entries) {
            cachedEntries = new ArrayList<>(entries);
        }

        @Nonnull
        public static List<GatewayClientEntry> getEntries() {
            return cachedEntries;
        }

        public static void clear() {
            cachedEntries.clear();
        }
    }
}
