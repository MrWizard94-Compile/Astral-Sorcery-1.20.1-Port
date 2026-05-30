package hellfirepvp.astralsorcery.common.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * An item entity that ignores explosion damage so special drops (crystals, gems)
 * don't get destroyed by nearby TNT or creepers.
 *
 * <p>1.16 → 1.20: attackEntityFrom → hurt; IPacket removed (handled by base class).</p>
 */
public class EntityItemExplosionResistant extends EntityItemHighlighted {

    public EntityItemExplosionResistant(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    public EntityItemExplosionResistant(EntityType<? extends ItemEntity> type, Level level,
                                         double x, double y, double z, ItemStack stack) {
        super(type, level);
        setPos(x, y, z);
        setItem(stack);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        return super.hurt(source, amount);
    }
}
