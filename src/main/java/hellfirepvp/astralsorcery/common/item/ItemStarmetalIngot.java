package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.entity.EntityStarmetal;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Starmetal Ingot — crafted from iron exposed to starlight.
 * Drops as an {@link EntityStarmetal} so it can be chiseled to produce stardust.
 */
public class ItemStarmetalIngot extends ItemAS {

    public ItemStarmetalIngot() {
        super(defaultProperties());
    }

    @Override
    @Nullable
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        EntityStarmetal entity = new EntityStarmetal(
                EntityTypesAS.ITEM_STARMETAL_INGOT.get(), level,
                location.getX(), location.getY(), location.getZ(), stack);
        entity.setPickUpDelay(10);
        return entity;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }
}
