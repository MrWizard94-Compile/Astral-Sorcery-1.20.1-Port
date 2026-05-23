/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeGenItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract base class for all crystal items (rock crystal, celestial crystal, attuned variants).
 * Crystal properties (size, purity, shape, etc.) are stored as NBT via {@link CrystalAttributes}.
 *
 * <p>1.16 → 1.20: CompoundNBT → CompoundTag, handled inside CrystalAttributes.
 * CrystalGenerator (auto-generate on first inventory tick) deferred to Phase TBD.</p>
 */
public abstract class ItemCrystalBase extends ItemAS implements CrystalAttributeGenItem {

    public ItemCrystalBase(@Nonnull Item.Properties properties) {
        super(properties);
    }

    @Override
    @Nullable
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

    @Override
    public int getGeneratedPropertyTiers() {
        return 5;
    }

    @Override
    public int getMaxPropertyTiers() {
        return 7;
    }

    /**
     * Returns the attuned item variant that this crystal becomes after attunement,
     * or {@code null} if this crystal is already attuned (or has no tuned form).
     */
    @Nullable
    public Item getTunedItemVariant() {
        return null;
    }
}
