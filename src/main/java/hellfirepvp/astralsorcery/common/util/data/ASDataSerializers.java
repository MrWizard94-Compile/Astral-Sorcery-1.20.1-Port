package hellfirepvp.astralsorcery.common.util.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

/**
 * Custom entity data serializers for syncing AS-specific data types
 * via the entity synched data system (SynchedEntityData).
 *
 * <p>1.16 → 1.20 changes:
 * IDataSerializer → EntityDataSerializer,
 * DataParameter → EntityDataAccessor,
 * PacketBuffer → FriendlyByteBuf,
 * createKey → createAccessor,
 * copyValue → copy,
 * new Long(value) → Long.valueOf(value)</p>
 */
public class ASDataSerializers {

    public static final EntityDataSerializer<Long> LONG = new EntityDataSerializer<>() {
        @Override
        public void write(@Nonnull FriendlyByteBuf buf, @Nonnull Long value) {
            buf.writeLongLE(value);
        }

        @Override
        @Nonnull
        public Long read(@Nonnull FriendlyByteBuf buf) {
            return buf.readLongLE();
        }

        @Override
        @Nonnull
        public EntityDataAccessor<Long> createAccessor(int id) {
            return new EntityDataAccessor<>(id, this);
        }

        @Override
        @Nonnull
        public Long copy(@Nonnull Long value) {
            return Long.valueOf(value);
        }
    };

    public static final EntityDataSerializer<Vector3> VECTOR = new EntityDataSerializer<>() {
        @Override
        public void write(@Nonnull FriendlyByteBuf buf, @Nonnull Vector3 value) {
            buf.writeDouble(value.getX());
            buf.writeDouble(value.getY());
            buf.writeDouble(value.getZ());
        }

        @Override
        @Nonnull
        public Vector3 read(@Nonnull FriendlyByteBuf buf) {
            return new Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        @Nonnull
        public EntityDataAccessor<Vector3> createAccessor(int id) {
            return new EntityDataAccessor<>(id, this);
        }

        @Override
        @Nonnull
        public Vector3 copy(@Nonnull Vector3 value) {
            return value.clone();
        }
    };

    public static final EntityDataSerializer<FluidStack> FLUID = new EntityDataSerializer<>() {
        @Override
        public void write(@Nonnull FriendlyByteBuf buf, @Nonnull FluidStack value) {
            ByteBufUtils.writeFluidStack(buf, value);
        }

        @Override
        @Nonnull
        public FluidStack read(@Nonnull FriendlyByteBuf buf) {
            return ByteBufUtils.readFluidStack(buf);
        }

        @Override
        @Nonnull
        public EntityDataAccessor<FluidStack> createAccessor(int id) {
            return new EntityDataAccessor<>(id, this);
        }

        @Override
        @Nonnull
        public FluidStack copy(@Nonnull FluidStack value) {
            return value.copy();
        }
    };
}
