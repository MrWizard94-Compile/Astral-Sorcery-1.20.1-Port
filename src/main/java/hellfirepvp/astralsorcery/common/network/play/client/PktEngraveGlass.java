package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.DrawnConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.tile.BlockEntityRefractionTable;
import hellfirepvp.astralsorcery.common.util.tile.TileUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Client → Server: Player has placed constellation icons on the refraction
 * table's drawing grid and wants to engrave the infused glass.
 * Server validates the request (has parchment, has un-engraved glass, is at
 * the right position) and applies the star map.
 */
public class PktEngraveGlass {

    @Nonnull
    private final ResourceKey<Level> dimension;
    @Nonnull
    private final BlockPos pos;
    @Nonnull
    private final List<DrawnConstellation> constellations;

    public PktEngraveGlass(@Nonnull ResourceKey<Level> dimension,
                           @Nonnull BlockPos pos,
                           @Nonnull List<DrawnConstellation> constellations) {
        this.dimension = dimension;
        this.pos = pos;
        this.constellations = constellations;
    }

    public static void encode(@Nonnull PktEngraveGlass pkt, @Nonnull FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.dimension.location());
        buf.writeBlockPos(pkt.pos);
        buf.writeInt(pkt.constellations.size());
        for (DrawnConstellation dc : pkt.constellations) {
            buf.writeInt(dc.getPoint().x);
            buf.writeInt(dc.getPoint().y);
            buf.writeResourceLocation(dc.getConstellation().getRegistryName());
        }
    }

    @Nonnull
    public static PktEngraveGlass decode(@Nonnull FriendlyByteBuf buf) {
        ResourceLocation dimLoc = buf.readResourceLocation();
        ResourceKey<Level> dimension = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimLoc);
        BlockPos pos = buf.readBlockPos();
        int count = buf.readInt();
        List<DrawnConstellation> constellations = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int x = buf.readInt();
            int y = buf.readInt();
            ResourceLocation cstKey = buf.readResourceLocation();
            IConstellation cst = ConstellationRegistry.getConstellation(cstKey);
            if (cst != null) {
                constellations.add(new DrawnConstellation(new Point(x, y), cst));
            }
        }
        return new PktEngraveGlass(dimension, pos, constellations);
    }

    public static void handle(@Nonnull PktEngraveGlass pkt,
                               @Nonnull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (pkt.constellations.isEmpty()) return;

            ServerLevel level = player.server.getLevel(pkt.dimension);
            if (level == null) return;

            if (player.distanceToSqr(
                    pkt.pos.getX() + 0.5, pkt.pos.getY() + 0.5, pkt.pos.getZ() + 0.5) > 64.0) {
                AstralSorcery.log.debug("PktEngraveGlass rejected: player too far from refraction table");
                return;
            }

            BlockEntityRefractionTable te = TileUtils.getTileAt(level, pkt.pos,
                    BlockEntityRefractionTable.class, true);
            if (te == null) return;

            if (!te.hasParchment() || !te.hasUnengravedGlass()) return;

            te.engraveGlass(pkt.constellations);
        });
        ctx.get().setPacketHandled(true);
    }
}
