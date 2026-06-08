package hellfirepvp.astralsorcery.common.util.nbt;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Core NBT serialization utilities used throughout Astral Sorcery.
 *
 * <p>1.16 → 1.20 changes:
 * CompoundNBT → CompoundTag, ListNBT → ListTag, INBT → Tag,
 * Constants.NBT → Tag.TAG_*, keySet → getAllKeys, getTagType → getElementType,
 * AxisAlignedBB → AABB, hasUniqueId → hasUUID, getUniqueId → getUUID,
 * block.getRegistryName() → ForgeRegistries.BLOCKS.getKey(block),
 * state.with → state.setValue, state.get → state.getValue,
 * ItemStack::read → ItemStack.of, stack::write → stack.save</p>
 */
public class NBTHelper {

    // ---- Persistent data (mod-scoped sub-tag) ----

    @Nonnull
    public static CompoundTag getPersistentData(@Nonnull Entity entity) {
        return getPersistentData(entity.getPersistentData());
    }

    @Nonnull
    public static CompoundTag getPersistentData(@Nonnull ItemStack item) {
        return getPersistentData(getData(item));
    }

    @Nonnull
    public static CompoundTag getPersistentData(@Nonnull CompoundTag base) {
        CompoundTag compound;
        if (hasPersistentData(base)) {
            compound = base.getCompound(AstralSorcery.MODID);
        } else {
            compound = new CompoundTag();
            base.put(AstralSorcery.MODID, compound);
        }
        return compound;
    }

    public static boolean hasPersistentData(@Nonnull Entity entity) {
        return hasPersistentData(entity.getPersistentData());
    }

    public static boolean hasPersistentData(@Nonnull ItemStack item) {
        CompoundTag tag = item.getTag();
        return tag != null && hasPersistentData(tag);
    }

    public static boolean hasPersistentData(@Nonnull CompoundTag base) {
        return base.contains(AstralSorcery.MODID) && base.get(AstralSorcery.MODID) instanceof CompoundTag;
    }

    public static void removePersistentData(@Nonnull Entity entity) {
        removePersistentData(entity.getPersistentData());
    }

    public static void removePersistentData(@Nonnull ItemStack item) {
        CompoundTag tag = item.getTag();
        if (tag != null) {
            removePersistentData(tag);
        }
    }

    public static void removePersistentData(@Nonnull CompoundTag base) {
        base.remove(AstralSorcery.MODID);
    }

    // ---- Deep merge ----

    public static void deepMerge(@Nonnull CompoundTag dst, @Nonnull CompoundTag src,
                                  boolean uniqueArrayEntries) {
        for (String s : src.getAllKeys()) {
            Tag nbtElement = src.get(s);
            if (nbtElement == null) continue;

            if (nbtElement.getId() == Tag.TAG_COMPOUND) {
                if (dst.contains(s, Tag.TAG_COMPOUND)) {
                    deepMerge(dst.getCompound(s), (CompoundTag) nbtElement, uniqueArrayEntries);
                } else {
                    dst.put(s, nbtElement.copy());
                }
            } else if (nbtElement.getId() == Tag.TAG_LIST) {
                if (dst.contains(s, Tag.TAG_LIST)) {
                    ListTag dstList = (ListTag) dst.get(s);
                    ListTag srcList = (ListTag) nbtElement;
                    if (dstList != null && dstList.getElementType() == srcList.getElementType()) {
                        deepMergeList(dstList, srcList);
                    } else {
                        dst.put(s, srcList.copy());
                    }
                } else {
                    dst.put(s, nbtElement.copy());
                }
            } else {
                dst.put(s, nbtElement.copy());
            }
        }
    }

    private static void deepMergeList(@Nonnull ListTag dst, @Nonnull ListTag src) {
        for (int j = 0; j < src.size(); j++) {
            Tag toAdd = src.get(j);
            boolean found = false;
            for (int i = 0; i < dst.size(); i++) {
                Tag existing = dst.get(i);
                if (existing.equals(toAdd)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                dst.add(toAdd.copy());
            }
        }
    }

    // ---- Collection serialization ----

    @Nonnull
    public static <E, N extends Tag> List<E> readList(@Nonnull CompoundTag nbt, @Nonnull String key,
                                                       int type, @Nonnull Function<N, E> deserializer) {
        if (!nbt.contains(key, Tag.TAG_LIST)) {
            return new ArrayList<>();
        }
        return readList(nbt.getList(key, type), deserializer);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <E, N extends Tag> List<E> readList(@Nonnull ListTag nbt,
                                                       @Nonnull Function<N, E> deserializer) {
        return nbt.stream()
                .map(n -> deserializer.apply((N) n))
                .collect(Collectors.toList());
    }

    @Nonnull
    public static <E, N extends Tag> Set<E> readSet(@Nonnull CompoundTag nbt, @Nonnull String key,
                                                     int type, @Nonnull Function<N, E> deserializer) {
        if (!nbt.contains(key, Tag.TAG_LIST)) {
            return new HashSet<>();
        }
        return readSet(nbt.getList(key, type), deserializer);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <E, N extends Tag> Set<E> readSet(@Nonnull ListTag nbt,
                                                     @Nonnull Function<N, E> deserializer) {
        return nbt.stream()
                .map(n -> deserializer.apply((N) n))
                .collect(Collectors.toSet());
    }

    public static <E> void writeList(@Nonnull CompoundTag tag, @Nonnull String key,
                                      @Nonnull Collection<E> collection,
                                      @Nonnull Function<E, Tag> serializer) {
        tag.put(key, writeList(collection, serializer));
    }

    @Nonnull
    public static <E> ListTag writeList(@Nonnull Collection<E> collection,
                                         @Nonnull Function<E, Tag> serializer) {
        ListTag nbt = new ListTag();
        nbt.addAll(collection.stream()
                .map(serializer)
                .collect(Collectors.toList()));
        return nbt;
    }

    // ---- ItemStack helpers ----

    @Nonnull
    public static CompoundTag getData(@Nonnull ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound == null) {
            compound = new CompoundTag();
            stack.setTag(compound);
        }
        return compound;
    }

    // ---- Optional read/write ----

    public static <T> void writeOptional(@Nonnull CompoundTag nbt, @Nonnull String key,
                                          @Nullable T object,
                                          @Nonnull BiConsumer<CompoundTag, T> writer) {
        nbt.putBoolean(key + "_present", object != null);
        if (object != null) {
            CompoundTag write = new CompoundTag();
            writer.accept(write, object);
            nbt.put(key, write);
        }
    }

    @Nullable
    public static <T> T readOptional(@Nonnull CompoundTag nbt, @Nonnull String key,
                                      @Nonnull Function<CompoundTag, T> reader) {
        return readOptional(nbt, key, reader, null);
    }

    @Nullable
    public static <T> T readOptional(@Nonnull CompoundTag nbt, @Nonnull String key,
                                      @Nonnull Function<CompoundTag, T> reader, @Nullable T defaultValue) {
        if (nbt.getBoolean(key + "_present")) {
            CompoundTag read = nbt.getCompound(key);
            return reader.apply(read);
        }
        return defaultValue;
    }

    // ---- Enum serialization ----

    public static <T extends Enum<T>> void writeEnum(@Nonnull CompoundTag nbt, @Nonnull String key,
                                                      @Nonnull T enumValue) {
        nbt.putInt(key, enumValue.ordinal());
    }

    @Nonnull
    public static <T extends Enum<T>> T readEnum(@Nonnull CompoundTag nbt, @Nonnull String key,
                                                  @Nonnull Class<T> enumClazz) {
        if (!enumClazz.isEnum()) {
            throw new IllegalArgumentException("Passed class is not an enum!");
        }
        T[] constants = enumClazz.getEnumConstants();
        int index = nbt.getInt(key);
        if (index < 0 || index >= constants.length) {
            return constants[0]; // degrade to first entry on corrupt/out-of-range save data
        }
        return constants[index];
    }

    // ---- BlockState serialization ----

    public static void setBlockState(@Nonnull CompoundTag cmp, @Nonnull String key,
                                      @Nonnull BlockState state) {
        cmp.put(key, getBlockStateNBTTag(state));
    }

    @Nullable
    public static BlockState getBlockState(@Nonnull CompoundTag cmp, @Nonnull String key) {
        return getBlockStateFromTag(cmp.getCompound(key));
    }

    @Nonnull
    public static CompoundTag getBlockStateNBTTag(@Nonnull BlockState state) {
        ResourceLocation regName = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (regName == null) {
            state = Blocks.AIR.defaultBlockState();
            regName = new ResourceLocation("minecraft", "air");
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("registryName", regName.toString());
        ListTag properties = new ListTag();
        for (Property<?> property : state.getProperties()) {
            CompoundTag propTag = new CompoundTag();
            try {
                propTag.putString("value", getPropertyName(state, property));
            } catch (Exception exc) {
                continue;
            }
            propTag.putString("property", property.getName());
            properties.add(propTag);
        }
        tag.put("properties", properties);
        return tag;
    }

    private static <T extends Comparable<T>> String getPropertyName(@Nonnull BlockState state,
                                                                      @Nonnull Property<T> property) {
        return property.getName(state.getValue(property));
    }

    @Nullable
    public static BlockState getBlockStateFromTag(@Nonnull CompoundTag cmp) {
        return getBlockStateFromTag(cmp, null);
    }

    @Nullable
    public static BlockState getBlockStateFromTag(@Nonnull CompoundTag cmp,
                                                   @Nullable BlockState defaultState) {
        ResourceLocation key = new ResourceLocation(cmp.getString("registryName"));
        Block block = ForgeRegistries.BLOCKS.getValue(key);
        if (block == null || block == Blocks.AIR) return defaultState;
        BlockState state = block.defaultBlockState();
        Collection<Property<?>> properties = state.getProperties();
        ListTag list = cmp.getList("properties", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag propertyTag = list.getCompound(i);
            String valueStr = propertyTag.getString("value");
            String propertyStr = propertyTag.getString("property");
            state = applyProperty(state, properties, propertyStr, valueStr);
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState applyProperty(
            @Nonnull BlockState state, @Nonnull Collection<Property<?>> properties,
            @Nonnull String propertyName, @Nonnull String valueStr) {
        for (Property<?> prop : properties) {
            if (prop.getName().equalsIgnoreCase(propertyName)) {
                Property<T> match = (Property<T>) prop;
                try {
                    Optional<T> opt = match.getValue(valueStr);
                    if (opt.isPresent()) {
                        return state.setValue(match, opt.get());
                    }
                } catch (Exception ignored) {}
                break;
            }
        }
        return state;
    }

    // ---- Sub-tag helpers ----

    public static void setAsSubTag(@Nonnull CompoundTag compound, @Nonnull String tag,
                                    @Nonnull Consumer<CompoundTag> applyFct) {
        CompoundTag newTag = new CompoundTag();
        applyFct.accept(newTag);
        compound.put(tag, newTag);
    }

    @Nullable
    public static <T> T readFromSubTag(@Nonnull CompoundTag compound, @Nonnull String tag,
                                        @Nonnull Function<CompoundTag, T> readFct) {
        if (compound.contains(tag, Tag.TAG_COMPOUND)) {
            return readFct.apply(compound.getCompound(tag));
        }
        return null;
    }

    // ---- ResourceLocation ----

    public static void setResourceLocation(@Nonnull CompoundTag nbt, @Nonnull String tag,
                                            @Nonnull ResourceLocation key) {
        nbt.putString(tag, key.toString());
    }

    @Nullable
    public static ResourceLocation getResourceLocation(@Nonnull CompoundTag nbt, @Nonnull String tag) {
        if (nbt.contains(tag)) {
            return ResourceLocation.tryParse(nbt.getString(tag));
        }
        return null;
    }

    // ---- ItemStack / FluidStack ----

    public static void setStack(@Nonnull CompoundTag compound, @Nonnull String tag,
                                 @Nonnull ItemStack stack) {
        setAsSubTag(compound, tag, subTag -> stack.save(subTag));
    }

    @Nonnull
    public static ItemStack getStack(@Nonnull CompoundTag compound, @Nonnull String tag) {
        return ObjectUtils.firstNonNull(
                readFromSubTag(compound, tag, ItemStack::of),
                ItemStack.EMPTY);
    }

    public static void setFluid(@Nonnull CompoundTag compound, @Nonnull String tag,
                                 @Nonnull FluidStack stack) {
        setAsSubTag(compound, tag, stack::writeToNBT);
    }

    @Nonnull
    public static FluidStack getFluid(@Nonnull CompoundTag compound, @Nonnull String tag) {
        return ObjectUtils.firstNonNull(
                readFromSubTag(compound, tag, FluidStack::loadFluidStackFromNBT),
                FluidStack.EMPTY);
    }

    // ---- UUID ----

    public static void removeUUID(@Nonnull CompoundTag compound, @Nonnull String key) {
        compound.remove(key);
    }

    @Nonnull
    public static UUID getUUID(@Nonnull CompoundTag nbt, @Nonnull String key,
                                @Nonnull UUID defaultValue) {
        if (nbt.hasUUID(key)) {
            return nbt.getUUID(key);
        }
        return defaultValue;
    }

    // ---- BlockPos ----

    @Nonnull
    public static CompoundTag writeBlockPosToNBT(@Nonnull BlockPos pos, @Nonnull CompoundTag compound) {
        compound.putInt("bposX", pos.getX());
        compound.putInt("bposY", pos.getY());
        compound.putInt("bposZ", pos.getZ());
        return compound;
    }

    @Nonnull
    public static BlockPos readBlockPosFromNBT(@Nonnull CompoundTag compound) {
        int x = compound.getInt("bposX");
        int y = compound.getInt("bposY");
        int z = compound.getInt("bposZ");
        return new BlockPos(x, y, z);
    }

    // ---- AABB ----

    @Nonnull
    public static CompoundTag writeBoundingBox(@Nonnull AABB box, @Nonnull CompoundTag tag) {
        tag.putDouble("boxMinX", box.minX);
        tag.putDouble("boxMinY", box.minY);
        tag.putDouble("boxMinZ", box.minZ);
        tag.putDouble("boxMaxX", box.maxX);
        tag.putDouble("boxMaxY", box.maxY);
        tag.putDouble("boxMaxZ", box.maxZ);
        return tag;
    }

    @Nonnull
    public static AABB readBoundingBox(@Nonnull CompoundTag tag) {
        return new AABB(
                tag.getDouble("boxMinX"),
                tag.getDouble("boxMinY"),
                tag.getDouble("boxMinZ"),
                tag.getDouble("boxMaxX"),
                tag.getDouble("boxMaxY"),
                tag.getDouble("boxMaxZ"));
    }

    // ---- Vector3 serialization ----

    @Nonnull
    public static CompoundTag writeVector3(@Nonnull Vector3 v) {
        CompoundTag tag = new CompoundTag();
        writeVector3(v, tag);
        return tag;
    }

    @Nonnull
    public static CompoundTag writeVector3(@Nonnull Vector3 v, @Nonnull CompoundTag tag) {
        tag.putDouble("vecPosX", v.getX());
        tag.putDouble("vecPosY", v.getY());
        tag.putDouble("vecPosZ", v.getZ());
        return tag;
    }

    @Nonnull
    public static Vector3 readVector3(@Nonnull CompoundTag tag) {
        return new Vector3(
                tag.getDouble("vecPosX"),
                tag.getDouble("vecPosY"),
                tag.getDouble("vecPosZ"));
    }
}
