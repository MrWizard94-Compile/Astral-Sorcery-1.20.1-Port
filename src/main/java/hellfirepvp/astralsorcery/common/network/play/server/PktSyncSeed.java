/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.util.world.WorldSeedCache;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Server → Client: Delivers the world seed for a specific dimension in response
 * to a {@link hellfirepvp.astralsorcery.common.network.play.client.PktRequestSeed}.
 * Client stores the seed in {@link WorldSeedCache} for constellation calculations.
 */
public class PktSyncSeed {

    private final int session;
    @Nonnull
    private final ResourceLocation dimLocation;
    private final long seed;

    public PktSyncSeed(int session, @Nonnull ResourceKey<Level> dim, long seed) {
        this.session = session;
        this.dimLocation = dim.location();
        this.seed = seed;
    }

    private PktSyncSeed(int session, @Nonnull ResourceLocation dimLocation, long seed) {
        this.session = session;
        this.dimLocation = dimLocation;
        this.seed = seed;
    }

    public static void encode(@Nonnull PktSyncSeed pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.session);
        buf.writeResourceLocation(pkt.dimLocation);
        buf.writeLong(pkt.seed);
    }

    @Nonnull
    public static PktSyncSeed decode(@Nonnull FriendlyByteBuf buf) {
        int session = buf.readVarInt();
        ResourceLocation dimLoc = buf.readResourceLocation();
        long seed = buf.readLong();
        return new PktSyncSeed(session, dimLoc, seed);
    }

    public static void handle(@Nonnull PktSyncSeed pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(pkt))
        );
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(@Nonnull PktSyncSeed pkt) {
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, pkt.dimLocation);
        WorldSeedCache.updateSeedCache(dimKey, pkt.session, pkt.seed);
    }
}
