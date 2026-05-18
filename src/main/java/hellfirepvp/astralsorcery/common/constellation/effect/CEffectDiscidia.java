/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Constellation effect for <em>Discidia</em> (Major — Offense / Destruction).
 *
 * <p>When focused on a Ritual Pedestal this effect:
 * <ul>
 *   <li>Deals magic damage to all hostile mobs within the area</li>
 *   <li>Applies the Weakness potion effect to hostile mobs, reducing their
 *       attack damage</li>
 * </ul>
 *
 * <p>Potency scales the damage dealt per tick and the Weakness amplifier.</p>
 */
public class CEffectDiscidia extends ConstellationEffectProvider {

    private static final double DEFAULT_RANGE = 16.0;

    /** Base magic damage dealt per effect tick. */
    private static final float BASE_DAMAGE = 3.0F;

    /** Duration of the Weakness debuff in ticks. */
    private static final int WEAKNESS_DURATION = 100;

    public CEffectDiscidia() {
        super("discidia");
    }

    @Override
    public void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                     @Nonnull ConstellationEffectProperties properties) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB area = createArea(pos, properties);
        double potency = properties.getPotency();

        damageHostiles(serverLevel, area, potency);
        weakenHostiles(serverLevel, area, potency);
    }

    /**
     * Deal magic damage to all hostile mobs inside the area.
     */
    private void damageHostiles(@Nonnull ServerLevel level, @Nonnull AABB area,
                                double potency) {
        float damage = (float) (BASE_DAMAGE * potency);
        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, area);
        for (Monster monster : monsters) {
            monster.hurt(level.damageSources().magic(), damage);
        }
    }

    /**
     * Apply Weakness to all hostile mobs inside the area.
     */
    private void weakenHostiles(@Nonnull ServerLevel level, @Nonnull AABB area,
                                double potency) {
        int amplifier = potency >= 2.0 ? 1 : 0;
        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, area);
        for (Monster monster : monsters) {
            monster.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS, WEAKNESS_DURATION, amplifier, true, false));
        }
    }

    @Nonnull
    @Override
    public ConstellationEffectProperties provideProperties() {
        return new ConstellationEffectProperties(DEFAULT_RANGE);
    }
}
