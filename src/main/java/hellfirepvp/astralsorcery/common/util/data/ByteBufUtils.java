package hellfirepvp.astralsorcery.common.util.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Network serialization utilities for FriendlyByteBuf (packet buffer).
 * Provides typed read/write helpers for common data structures.
 *
 * <p>1.16 → 1.20 changes:
 * PacketBuffer → FriendlyByteBuf, CompoundNBT → CompoundTag,
 * CompressedStreamTools → NbtIo, ITextComponent → Component,
 * IFormattableTextComponent → MutableComponent,
 * ItemStack.read → ItemStack.of, stack.write → stack.save,
 * IForgeRegistryEntry/RegistryManager.ACTIVE → IForgeRegistry parameter,
 * RegistryKey → ResourceKey, getOrCreateKey → create,
 * getOrCreateRootKey → createRegistryKey,
 * block.getDefaultState → defaultBlockState,
 * state.get → getValue, state.with → setValue,
 * property.parseValue → getValue,
 * JsonParser().parse → JsonParser.parseString</p>
 */
public class ByteBufUtils {

    // ---- Optional / nullable ----

    @Nullable
    public static <T> T readOptional(@Nonnull FriendlyByteBuf buf,
                                     @Nonnull Function<FriendlyByteBuf, T> readFct) {
        if (buf.readBoolean()) {
            return readFct.apply(buf);
        }
        return null;
    }

    public static <T> void writeOptional(@Nonnull FriendlyByteBuf buf,
                                         @Nullable T object,
                                         @Nonnull BiConsumer<FriendlyByteBuf, T> applyFct) {
        writeOptional(buf, object, Function.identity(), applyFct);
    }

    public static <T, R> void writeOptional(@Nonnull FriendlyByteBuf buf,
                                            @Nullable T object,
                                            @Nonnull Function<T, R> converter,
                                            @Nonnull BiConsumer<FriendlyByteBuf, R> applyFct) {
        buf.writeBoolean(object != null);
        if (object != null) {
            applyFct.accept(buf, converter.apply(object));
        }
    }

    // ---- UUID ----

    public static void writeUUID(@Nonnull FriendlyByteBuf buf, @Nonnull UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    @Nonnull
    public static UUID readUUID(@Nonnull FriendlyByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    // ---- Collections ----

    public static <T> void writeCollection(@Nonnull FriendlyByteBuf buf,
                                           @Nullable Collection<T> list,
                                           @Nonnull BiConsumer<FriendlyByteBuf, T> iterationFct) {
        if (list != null) {
            buf.writeInt(list.size());
            list.forEach(e -> iterationFct.accept(buf, e));
        } else {
            buf.writeInt(-1);
        }
    }

    @Nullable
    public static <T> List<T> readList(@Nonnull FriendlyByteBuf buf,
                                       @Nonnull Function<FriendlyByteBuf, T> readFct) {
        return readCollection(buf, ArrayList::new, List::add, readFct);
    }

    @Nullable
    public static <T> Set<T> readSet(@Nonnull FriendlyByteBuf buf,
                                     @Nonnull Function<FriendlyByteBuf, T> readFct) {
        return readCollection(buf, HashSet::new, Set::add, readFct);
    }

    @Nullable
    public static <T, C extends Collection<T>> C readCollection(
            @Nonnull FriendlyByteBuf buf,
            @Nonnull Supplier<C> newCollection,
            @Nonnull BiConsumer<C, T> addFn,
            @Nonnull Function<FriendlyByteBuf, T> readFct) {
        int size = buf.readInt();
        if (size == -1) {
            return null;
        }
        C collection = newCollection.get();
        for (int i = 0; i < size; i++) {
            addFn.accept(collection, readFct.apply(buf));
        }
        return collection;
    }

    // ---- Maps ----

    public static <K, V> void writeMap(@Nonnull FriendlyByteBuf buf,
                                       @Nullable Map<K, V> map,
                                       @Nonnull BiConsumer<FriendlyByteBuf, K> keySerializer,
                                       @Nonnull BiConsumer<FriendlyByteBuf, V> valueSerializer) {
        if (map != null) {
            buf.writeInt(map.size());
            for (Map.Entry<K, V> entry : map.entrySet()) {
                keySerializer.accept(buf, entry.getKey());
                valueSerializer.accept(buf, entry.getValue());
            }
        } else {
            buf.writeInt(-1);
        }
    }

    @Nullable
    public static <K, V> Map<K, V> readMap(@Nonnull FriendlyByteBuf buf,
                                           @Nonnull Function<FriendlyByteBuf, K> readKey,
                                           @Nonnull Function<FriendlyByteBuf, V> readValue) {
        int size = buf.readInt();
        if (size == -1) {
            return null;
        }
        Map<K, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(readKey.apply(buf), readValue.apply(buf));
        }
        return map;
    }

    // ---- Text components ----

    public static void writeTextComponent(@Nonnull FriendlyByteBuf buf, @Nonnull Component cmp) {
        writeString(buf, Component.Serializer.toJson(cmp));
    }

    @Nonnull
    public static MutableComponent readTextComponent(@Nonnull FriendlyByteBuf buf) {
        MutableComponent result = Component.Serializer.fromJson(readString(buf));
        return result != null ? result : Component.literal("");
    }

    // ---- Strings ----

    public static void writeString(@Nonnull FriendlyByteBuf buf, @Nonnull String toWrite) {
        byte[] str = toWrite.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(str.length);
        buf.writeBytes(str);
    }

    @Nonnull
    public static String readString(@Nonnull FriendlyByteBuf buf) {
        int length = buf.readInt();
        byte[] strBytes = new byte[length];
        buf.readBytes(strBytes, 0, length);
        return new String(strBytes, StandardCharsets.UTF_8);
    }

    // ---- Registry entries ----

    /**
     * Write a Forge registry entry by serializing its ResourceLocation key.
     * In 1.20, the old IForgeRegistryEntry pattern is gone — callers must
     * provide the specific IForgeRegistry instance.
     *
     * @param buf      the buffer to write to
     * @param registry the Forge registry containing the entry
     * @param entry    the entry to serialize
     * @param <T>      entry type
     */
    public static <T> void writeRegistryEntry(@Nonnull FriendlyByteBuf buf,
                                              @Nonnull IForgeRegistry<T> registry,
                                              @Nonnull T entry) {
        ResourceLocation key = registry.getKey(entry);
        if (key == null) {
            throw new IllegalArgumentException("Entry not found in registry: " + entry);
        }
        writeResourceLocation(buf, key);
    }

    @Nullable
    public static <T> T readRegistryEntry(@Nonnull FriendlyByteBuf buf,
                                          @Nonnull IForgeRegistry<T> registry) {
        ResourceLocation entryName = readResourceLocation(buf);
        return registry.getValue(entryName);
    }

    // ---- Vanilla registry keys (ResourceKey) ----

    public static void writeVanillaRegistryEntry(@Nonnull FriendlyByteBuf buf,
                                                 @Nonnull ResourceKey<?> key) {
        writeResourceLocation(buf, key.registry());
        writeResourceLocation(buf, key.location());
    }

    @Nonnull
    public static <T> ResourceKey<T> readVanillaRegistryEntry(@Nonnull FriendlyByteBuf buf) {
        ResourceLocation registryName = readResourceLocation(buf);
        ResourceLocation entryName = readResourceLocation(buf);
        return ResourceKey.create(ResourceKey.<T>createRegistryKey(registryName), entryName);
    }

    // ---- ResourceLocation ----

    public static void writeResourceLocation(@Nonnull FriendlyByteBuf buf,
                                             @Nonnull ResourceLocation key) {
        writeString(buf, key.toString());
    }

    @Nonnull
    public static ResourceLocation readResourceLocation(@Nonnull FriendlyByteBuf buf) {
        return new ResourceLocation(readString(buf));
    }

    // ---- Enums ----

    public static <T extends Enum<T>> void writeEnumValue(@Nonnull FriendlyByteBuf buf,
                                                          @Nonnull T value) {
        buf.writeInt(value.ordinal());
    }

    @Nonnull
    public static <T extends Enum<T>> T readEnumValue(@Nonnull FriendlyByteBuf buf,
                                                      @Nonnull Class<T> enumClazz) {
        if (!enumClazz.isEnum()) {
            throw new IllegalArgumentException("Passed class is not an enum!");
        }
        T[] constants = enumClazz.getEnumConstants();
        int index = buf.readInt();
        if (index < 0 || index >= constants.length) {
            return constants[0]; // degrade gracefully on desync or malformed packet
        }
        return constants[index];
    }

    // ---- JSON ----

    public static void writeJsonObject(@Nonnull FriendlyByteBuf buf, @Nonnull JsonObject object) {
        writeString(buf, object.toString());
    }

    @Nonnull
    public static JsonObject readJsonObject(@Nonnull FriendlyByteBuf buf) {
        return JsonParser.parseString(readString(buf)).getAsJsonObject();
    }

    // ---- BlockPos ----

    public static void writePos(@Nonnull FriendlyByteBuf buf, @Nonnull BlockPos pos) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    @Nonnull
    public static BlockPos readPos(@Nonnull FriendlyByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        return new BlockPos(x, y, z);
    }

    // ---- Vector3 ----

    public static void writeVector(@Nonnull FriendlyByteBuf buf, @Nonnull Vector3 vec) {
        buf.writeDouble(vec.getX());
        buf.writeDouble(vec.getY());
        buf.writeDouble(vec.getZ());
    }

    @Nonnull
    public static Vector3 readVector(@Nonnull FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        return new Vector3(x, y, z);
    }

    // ---- ItemStack ----

    public static void writeItemStack(@Nonnull FriendlyByteBuf byteBuf, @Nonnull ItemStack stack) {
        boolean defined = !stack.isEmpty();
        byteBuf.writeBoolean(defined);
        if (defined) {
            CompoundTag tag = new CompoundTag();
            stack.save(tag);
            writeNBTTag(byteBuf, tag);
        }
    }

    @Nonnull
    public static ItemStack readItemStack(@Nonnull FriendlyByteBuf byteBuf) {
        boolean defined = byteBuf.readBoolean();
        if (defined) {
            return ItemStack.of(readNBTTag(byteBuf));
        } else {
            return ItemStack.EMPTY;
        }
    }

    // ---- BlockState ----

    public static void writeBlockState(@Nonnull FriendlyByteBuf byteBuf,
                                       @Nonnull BlockState state) {
        ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockKey == null) {
            throw new IllegalArgumentException("Block not found in registry: " + state.getBlock());
        }
        writeResourceLocation(byteBuf, blockKey);

        Collection<Property<?>> properties = state.getProperties();
        byteBuf.writeInt(properties.size());
        for (Property<?> prop : properties) {
            writeString(byteBuf, prop.getName());
            writeString(byteBuf, getPropertyValueName(state, prop));
        }
    }

    private static <T extends Comparable<T>> String getPropertyValueName(
            @Nonnull BlockState state, @Nonnull Property<T> prop) {
        return prop.getName(state.getValue(prop));
    }

    @Nonnull
    public static BlockState readBlockState(@Nonnull FriendlyByteBuf byteBuf) {
        ResourceLocation blockKey = readResourceLocation(byteBuf);
        Block block = ForgeRegistries.BLOCKS.getValue(blockKey);
        if (block == null) {
            throw new IllegalArgumentException("Unknown block: " + blockKey);
        }
        BlockState state = block.defaultBlockState();

        int propertyCount = byteBuf.readInt();
        for (int i = 0; i < propertyCount; i++) {
            String propName = readString(byteBuf);
            String valueStr = readString(byteBuf);
            state = applyProperty(state, propName, valueStr);
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState applyProperty(
            @Nonnull BlockState state, @Nonnull String propName, @Nonnull String valueStr) {
        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equalsIgnoreCase(propName)) {
                Property<T> typedProp = (Property<T>) prop;
                Optional<T> value = typedProp.getValue(valueStr);
                if (value.isPresent()) {
                    return state.setValue(typedProp, value.get());
                }
                break;
            }
        }
        return state;
    }

    // ---- FluidStack ----

    public static void writeFluidStack(@Nonnull FriendlyByteBuf byteBuf,
                                       @Nonnull FluidStack stack) {
        stack.writeToPacket(byteBuf);
    }

    @Nonnull
    public static FluidStack readFluidStack(@Nonnull FriendlyByteBuf byteBuf) {
        return FluidStack.readFromPacket(byteBuf);
    }

    // ---- NBT ----

    public static void writeNBTTag(@Nonnull FriendlyByteBuf byteBuf, @Nonnull CompoundTag tag) {
        try (DataOutputStream dos = new DataOutputStream(new ByteBufOutputStream(byteBuf))) {
            NbtIo.write(tag, dos);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write NBT tag to packet buffer", e);
        }
    }

    @Nonnull
    public static CompoundTag readNBTTag(@Nonnull FriendlyByteBuf byteBuf) {
        try (DataInputStream dis = new DataInputStream(new ByteBufInputStream(byteBuf))) {
            return NbtIo.read(dis);
        } catch (Exception ignored) {
            // Fall through to error
        }
        throw new IllegalStateException("Could not load NBT Tag from incoming byte buffer!");
    }
}
