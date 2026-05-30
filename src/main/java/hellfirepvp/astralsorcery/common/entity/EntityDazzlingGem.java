package hellfirepvp.astralsorcery.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Dazzling gem item entity — explosion-resistant and has an infinite lifespan.
 * Used for the Dazzling Gem item so it never despawns naturally.
 *
 * <p>1.16 → 1.20: world.isRemote() → level.isClientSide(); lifespan direct field access.</p>
 */
public class EntityDazzlingGem extends EntityItemExplosionResistant {

    public EntityDazzlingGem(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
        this.lifespan = Integer.MAX_VALUE;
    }

    public EntityDazzlingGem(EntityType<? extends ItemEntity> type, Level level,
                               double x, double y, double z, @Nonnull ItemStack stack) {
        super(type, level, x, y, z, stack);
        this.lifespan = Integer.MAX_VALUE;
    }
}
