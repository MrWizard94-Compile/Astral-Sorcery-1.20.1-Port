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
import hellfirepvp.astralsorcery.common.crystal.CrystalGenerator;
import hellfirepvp.astralsorcery.common.entity.EntityCrystal;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract base class for all crystal items (rock crystal, celestial crystal, attuned variants).
 * Crystal properties (size, purity, shape, etc.) are stored as NBT via {@link CrystalAttributes}.
 *
 * <p>1.16 → 1.20: CompoundNBT → CompoundTag, handled inside CrystalAttributes.</p>
 */
public abstract class ItemCrystalBase extends ItemAS implements CrystalAttributeGenItem {

    public ItemCrystalBase(@Nonnull Item.Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull Entity entity, int slot, boolean isSelected) {
        if (!level.isClientSide()) {
            CrystalAttributes attributes = getAttributes(stack);
            if (attributes == null) {
                attributes = CrystalGenerator.generateNewAttributes(stack);
                attributes.store(stack);
            }
        }
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

    /**
     * Drop crystals as {@link EntityCrystal} so they are explosion-resistant,
     * never despawn, and can be chiseled by a player holding a chisel.
     */
    @Override
    @Nullable
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        EntityCrystal crystal = new EntityCrystal(EntityTypesAS.ITEM_CRYSTAL.get(), level,
                location.getX(), location.getY(), location.getZ(), stack);
        crystal.setPickUpDelay(10);
        return crystal;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }
}
