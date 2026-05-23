package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Attuned Rock Crystal — a rock crystal that has been attuned to a major constellation
 * at the attunement altar. Used in constellation-specific crafting recipes.
 */
public class ItemAttunedRockCrystal extends ItemRockCrystalSimple {

    private static final String TAG_CONSTELLATION = "constellation";

    // CompoundTag.getString() has no @Nonnull annotation in Minecraft's mappings,
    // causing a false-positive null-type warning. getString() provably never returns null.
    @SuppressWarnings("null")
    @Nullable
    public static IMajorConstellation getConstellation(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_CONSTELLATION)) return null;
        ResourceLocation key = ResourceLocation.tryParse(tag.getString(TAG_CONSTELLATION));
        if (key == null) return null;
        IConstellation c = ConstellationRegistry.getConstellation(key);
        return c instanceof IMajorConstellation major ? major : null;
    }

    @SuppressWarnings("null")
    public static void setConstellation(ItemStack stack, IMajorConstellation constellation) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_CONSTELLATION, constellation.getRegistryName().toString());
    }
}
