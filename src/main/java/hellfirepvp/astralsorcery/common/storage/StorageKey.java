/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.storage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Identity key for an item in the storage network. Equality is based solely on
 * the item's registry name — stack size and NBT are ignored. This allows all
 * stacks of the same item type to be merged into a single storage slot.
 *
 * <p>1.16 → 1.20: Item.getRegistryName() → ForgeRegistries.ITEMS.getKey(item).</p>
 */
public class StorageKey {

    @Nonnull
    private final Item item;

    private StorageKey(@Nonnull Item item) {
        this.item = item;
    }

    public static StorageKey from(@Nonnull ItemStack stack) {
        return new StorageKey(stack.getItem());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageKey that = (StorageKey) o;
        return Objects.equals(
                ForgeRegistries.ITEMS.getKey(this.item),
                ForgeRegistries.ITEMS.getKey(that.item));
    }

    @Override
    public int hashCode() {
        return Objects.hash(ForgeRegistries.ITEMS.getKey(this.item));
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag keyTag = new CompoundTag();
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(this.item);
        keyTag.putString("name", key != null ? key.toString() : "minecraft:air");
        return keyTag;
    }

    /**
     * Deserializes a StorageKey from NBT. Returns null if the referenced item
     * no longer exists in the registry (e.g., a removed mod item).
     */
    @Nullable
    public static StorageKey deserialize(@Nonnull CompoundTag nbt) {
        ResourceLocation rl = ResourceLocation.tryParse(nbt.getString("name"));
        if (rl == null) return null;
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return new StorageKey(item);
    }
}
