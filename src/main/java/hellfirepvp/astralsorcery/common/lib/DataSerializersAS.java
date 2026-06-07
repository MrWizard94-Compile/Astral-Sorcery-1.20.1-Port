package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

/**
 * Custom entity data serializers for syncing Astral Sorcery types
 * via {@link net.minecraft.network.syncher.SynchedEntityData}.
 *
 * <p>Call {@link #init()} during common setup to register all serializers.
 * After registration the constants can be used in
 * {@code SynchedEntityData.defineId(EntityClass.class, DataSerializersAS.VECTOR)}.</p>
 *
 * <p>1.16 -> 1.20: {@code IDataSerializer} -> {@code EntityDataSerializer}.</p>
 */
public final class DataSerializersAS {

    private DataSerializersAS() {}

    public static EntityDataSerializer<Long> LONG;
    public static EntityDataSerializer<Vector3> VECTOR;
    public static EntityDataSerializer<FluidStack> FLUID;

    public static void init() {
        LONG = new EntityDataSerializer<>() {
            @Override
            public void write(@Nonnull FriendlyByteBuf buf, @Nonnull Long value) {
                buf.writeLong(value);
            }

            @Override
            @Nonnull
            public Long read(@Nonnull FriendlyByteBuf buf) {
                return buf.readLong();
            }

            @Override
            @Nonnull
            public Long copy(@Nonnull Long value) {
                return value;
            }
        };

        VECTOR = new EntityDataSerializer<>() {
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
            public Vector3 copy(@Nonnull Vector3 value) {
                return new Vector3(value.getX(), value.getY(), value.getZ());
            }
        };

        FLUID = new EntityDataSerializer<>() {
            @Override
            public void write(@Nonnull FriendlyByteBuf buf, @Nonnull FluidStack value) {
                value.writeToPacket(buf);
            }

            @Override
            @Nonnull
            public FluidStack read(@Nonnull FriendlyByteBuf buf) {
                return FluidStack.readFromPacket(buf);
            }

            @Override
            @Nonnull
            public FluidStack copy(@Nonnull FluidStack value) {
                return value.copy();
            }
        };

        EntityDataSerializers.registerSerializer(LONG);
        EntityDataSerializers.registerSerializer(VECTOR);
        EntityDataSerializers.registerSerializer(FLUID);
    }
}
