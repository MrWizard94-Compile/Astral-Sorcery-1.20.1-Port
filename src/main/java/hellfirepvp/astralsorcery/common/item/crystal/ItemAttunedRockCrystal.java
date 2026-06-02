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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Attuned Rock Crystal — a rock crystal attuned to a major constellation at
 * the attunement altar. Implements {@link ConstellationItem} so it can hold
 * a primary constellation (major) and an optional trait constellation (minor).
 */
public class ItemAttunedRockCrystal extends ItemRockCrystalSimple implements ConstellationItem {

    private static final String TAG_CONSTELLATION = "constellation";
    private static final String TAG_TRAIT = "trait";

    // CompoundTag.getString() has no @Nonnull annotation in Minecraft's mappings;
    // getString() provably never returns null.

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
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level,
                                @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        IWeakConstellation cst = getAttunedConstellation(stack);
        if (cst != null) {
            tooltip.add(Component.translatable(
                    "item.astralsorcery.rock_crystal.constellation",
                    cst.getConstellationName()));
        }
        IMinorConstellation trait = getTraitConstellation(stack);
        if (trait != null) {
            tooltip.add(Component.translatable(
                    "item.astralsorcery.rock_crystal.trait",
                    trait.getConstellationName()));
        }
    }

    @Override
    @Nullable
    public Item getTunedItemVariant() {
        return null; // already the attuned form
    }
}
