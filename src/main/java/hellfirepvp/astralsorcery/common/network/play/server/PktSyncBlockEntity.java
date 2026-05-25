/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntitySynchronized;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Server -> Client packet that syncs a block entity's custom NBT data.
 * Used for custom AS block entity types that extend {@link BlockEntitySynchronized}.
 * Calls both {@code readCustomNBT} and {@code readNetNBT} on the client.
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity,
 * CompoundNBT -> CompoundTag,
 * PacketBuffer -> FriendlyByteBuf,
 * world.getTileEntity -> level.getBlockEntity</p>
 */
public class PktSyncBlockEntity {

    @Nonnull
    private final BlockPos pos;
    @Nonnull
    private final CompoundTag data;

    /**
     * Creates a sync packet from a block entity's current state.
     *
     * @param be the block entity to sync
     */
    public PktSyncBlockEntity(@Nonnull BlockEntitySynchronized be) {
        this.pos = be.getBlockPos();
        CompoundTag compound = new CompoundTag();
        be.writeCustomNBT(compound);
        be.writeNetNBT(compound);
        this.data = compound;
    }

    private PktSyncBlockEntity(@Nonnull BlockPos pos, @Nonnull CompoundTag data) {
        this.pos = pos;
        this.data = data;
    }

    public static void encode(@Nonnull PktSyncBlockEntity msg, @Nonnull FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeNbt(msg.data);
    }

    @Nonnull
    public static PktSyncBlockEntity decode(@Nonnull FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        CompoundTag data = buf.readNbt();
        return new PktSyncBlockEntity(pos, data != null ? data : new CompoundTag());
    }

    public static void handle(@Nonnull PktSyncBlockEntity msg,
                              @Nonnull Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg))
        );
        ctx.setPacketHandled(true);
    }

    @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
    private static void handleClient(@Nonnull PktSyncBlockEntity msg) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        BlockEntity be = level.getBlockEntity(msg.pos);
        if (be instanceof BlockEntitySynchronized synced) {
            synced.readCustomNBT(msg.data);
            synced.readNetNBT(msg.data);
            AstralSorcery.log.trace("Synced block entity at {} from server", msg.pos);
        }
    }
}
