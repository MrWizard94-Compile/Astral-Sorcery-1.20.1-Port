/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.item.block;

import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Item class for Collector Crystal block items (both rock and celestial variants).
 * Carries {@link CrystalAttributes} and attuned constellation on the item stack so
 * they can be transferred to the tile entity when the block is placed.
 *
 * <p>1.16 → 1.20: BlockItem stable; removed maxStackSize(1) from Item.Properties
 * (handled elsewhere); Group/creative tab removed (registered via creative tab event).</p>
 */
public class ItemBlockCollectorCrystal extends BlockItem implements CrystalAttributeItem, ConstellationItem {

    private static final String TAG_CONSTELLATION = "constellation";
    private static final String TAG_TRAIT = "trait";

    public ItemBlockCollectorCrystal(@Nonnull Block block) {
        super(block, new Item.Properties().stacksTo(1));
    }

    // ========================================================================
    // CrystalAttributeItem
    // ========================================================================

    @Nullable
    @Override
    public CrystalAttributes getAttributes(@Nonnull ItemStack stack) {
        return CrystalAttributes.getCrystalAttributes(stack);
    }

    @Override
    public void setAttributes(@Nonnull ItemStack stack, @Nullable CrystalAttributes attributes) {
        if (attributes != null) {
            attributes.store(stack);
        } else {
            CrystalAttributes.storeNull(stack);
        }
    }

    // ========================================================================
    // ConstellationItem
    // ========================================================================

    @Nullable
    @Override
    public IWeakConstellation getAttunedConstellation(@Nonnull ItemStack stack) {
        CompoundTag data = NBTHelper.getPersistentData(stack);
        if (!data.contains(TAG_CONSTELLATION)) return null;
        IConstellation cst = ConstellationRegistry.getConstellation(
                new ResourceLocation(data.getString(TAG_CONSTELLATION)));
        return cst instanceof IWeakConstellation wc ? wc : null;
    }

    @Override
    public boolean setAttunedConstellation(@Nonnull ItemStack stack, @Nullable IWeakConstellation cst) {
        CompoundTag data = NBTHelper.getPersistentData(stack);
        if (cst != null) {
            data.putString(TAG_CONSTELLATION, cst.getRegistryName().toString());
        } else {
            data.remove(TAG_CONSTELLATION);
        }
        return true;
    }

    @Nullable
    @Override
    public IMinorConstellation getTraitConstellation(@Nonnull ItemStack stack) {
        CompoundTag data = NBTHelper.getPersistentData(stack);
        if (!data.contains(TAG_TRAIT)) return null;
        IConstellation cst = ConstellationRegistry.getConstellation(
                new ResourceLocation(data.getString(TAG_TRAIT)));
        return cst instanceof IMinorConstellation mc ? mc : null;
    }

    @Override
    public boolean setTraitConstellation(@Nonnull ItemStack stack, @Nullable IMinorConstellation cst) {
        CompoundTag data = NBTHelper.getPersistentData(stack);
        if (cst != null) {
            data.putString(TAG_TRAIT, cst.getRegistryName().toString());
        } else {
            data.remove(TAG_TRAIT);
        }
        return true;
    }
}
