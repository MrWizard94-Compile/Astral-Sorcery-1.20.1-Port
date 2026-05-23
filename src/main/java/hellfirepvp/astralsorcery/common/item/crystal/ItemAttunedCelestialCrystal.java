/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Attuned Celestial Crystal — a celestial crystal attuned to a major constellation.
 * Higher quality than attuned rock crystals; used in Radiance-tier recipes.
 * Implements {@link ConstellationItem} so it can hold a primary (major) and
 * optional trait (minor) constellation.
 */
public class ItemAttunedCelestialCrystal extends ItemCelestialCrystal implements ConstellationItem {

    private static final String TAG_CONSTELLATION = "constellation";
    private static final String TAG_TRAIT = "trait";

    @Nonnull
    @Override
    public Rarity getRarity(@Nonnull ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    @Nullable
    public IWeakConstellation getAttunedConstellation(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_CONSTELLATION)) return null;
        @SuppressWarnings("null")
        ResourceLocation key = ResourceLocation.tryParse(tag.getString(TAG_CONSTELLATION));
        if (key == null) return null;
        IConstellation c = ConstellationRegistry.getConstellation(key);
        return c instanceof IWeakConstellation weak ? weak : null;
    }

    @Override
    public boolean setAttunedConstellation(ItemStack stack, @Nullable IWeakConstellation cst) {
        if (cst == null) {
            CompoundTag tag = stack.getTag();
            if (tag != null) tag.remove(TAG_CONSTELLATION);
            return true;
        }
        if (!(cst instanceof IMajorConstellation)) return false;
        stack.getOrCreateTag().putString(TAG_CONSTELLATION, cst.getRegistryName().toString());
        return true;
    }

    @Override
    @Nullable
    public IMinorConstellation getTraitConstellation(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_TRAIT)) return null;
        @SuppressWarnings("null")
        ResourceLocation key = ResourceLocation.tryParse(tag.getString(TAG_TRAIT));
        if (key == null) return null;
        IConstellation c = ConstellationRegistry.getConstellation(key);
        return c instanceof IMinorConstellation minor ? minor : null;
    }

    @Override
    public boolean setTraitConstellation(ItemStack stack, @Nullable IMinorConstellation cst) {
        if (cst == null) {
            CompoundTag tag = stack.getTag();
            if (tag != null) tag.remove(TAG_TRAIT);
            return true;
        }
        stack.getOrCreateTag().putString(TAG_TRAIT, cst.getRegistryName().toString());
        return true;
    }

    @Override
    @Nullable
    public Item getTunedItemVariant() {
        return null; // already the attuned form
    }
}
