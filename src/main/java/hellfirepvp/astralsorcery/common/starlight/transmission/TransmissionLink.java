package hellfirepvp.astralsorcery.common.starlight.transmission;

import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Represents a directed link in the starlight transmission network
 * from one node to another.
 *
 * <p>1.16 -> 1.20 changes: CompoundNBT -> CompoundTag</p>
 */
public class TransmissionLink {

    @Nonnull
    private final BlockPos from;
    @Nonnull
    private final BlockPos to;

    public TransmissionLink(@Nonnull BlockPos from, @Nonnull BlockPos to) {
        this.from = from.immutable();
        this.to = to.immutable();
    }

    @Nonnull
    public BlockPos getFrom() {
        return from;
    }

    @Nonnull
    public BlockPos getTo() {
        return to;
    }

    /**
     * Get the squared distance between link endpoints.
     */
    public double getLengthSquared() {
        return from.distSqr(to);
    }

    /**
     * Get the euclidean distance between link endpoints.
     */
    public double getLength() {
        return Math.sqrt(getLengthSquared());
    }

    @Nonnull
    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("from", NBTHelper.writeBlockPosToNBT(from, new CompoundTag()));
        tag.put("to", NBTHelper.writeBlockPosToNBT(to, new CompoundTag()));
        return tag;
    }

    @Nonnull
    public static TransmissionLink readFromNBT(@Nonnull CompoundTag tag) {
        BlockPos from = NBTHelper.readBlockPosFromNBT(tag.getCompound("from"));
        BlockPos to = NBTHelper.readBlockPosFromNBT(tag.getCompound("to"));
        return new TransmissionLink(from, to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransmissionLink other)) return false;
        return from.equals(other.from) && to.equals(other.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return "Link[" + from.toShortString() + " -> " + to.toShortString() + "]";
    }
}
