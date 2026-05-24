/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncSeed;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Client → Server: Requests the world seed for a specific dimension.
 * Server responds with {@link PktSyncSeed}.
 *
 * <p>1.16 → 1.20: Ported from the bidirectional PktRequestSeed pattern.
 * Split into separate request/response packets to match Forge 1.20 simplechannel conventions.
 * LogicalSidedProvider → ServerLifecycleHooks.getCurrentServer().</p>
 */
public class PktRequestSeed {

    private final int session;
    @Nonnull
    private final ResourceLocation dimLocation;

    public PktRequestSeed(int session, @Nonnull ResourceKey<Level> dim) {
        this.session = session;
        this.dimLocation = dim.location();
    }

    private PktRequestSeed(int session, @Nonnull ResourceLocation dimLocation) {
        this.session = session;
        this.dimLocation = dimLocation;
    }

    public static void encode(@Nonnull PktRequestSeed pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.session);
        buf.writeResourceLocation(pkt.dimLocation);
    }

    @Nonnull
    public static PktRequestSeed decode(@Nonnull FriendlyByteBuf buf) {
        int session = buf.readVarInt();
        ResourceLocation dimLoc = buf.readResourceLocation();
        return new PktRequestSeed(session, dimLoc);
    }

    public static void handle(@Nonnull PktRequestSeed pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;

            MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
            if (srv == null) return;

            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, pkt.dimLocation);
            ServerLevel level = srv.getLevel(dimKey);
            if (level == null) return;

            long seed = MiscUtils.getRandomWorldSeed(level);
            PacketChannel.sendToPlayer(new PktSyncSeed(pkt.session, dimKey, seed), sender);
        });
        ctx.get().setPacketHandled(true);
    }
}
