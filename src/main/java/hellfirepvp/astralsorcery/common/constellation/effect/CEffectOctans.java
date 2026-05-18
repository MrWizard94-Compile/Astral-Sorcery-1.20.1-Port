/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Constellation effect for <em>Octans</em> (Weak — Water / Ocean).
 *
 * <p>When focused on a Ritual Pedestal this effect:
 * <ul>
 *   <li>Grants Water Breathing to all players in the area, allowing
 *       indefinite underwater exploration</li>
 *   <li>Grants Conduit Power to all players in the area, providing
 *       underwater vision, mining speed, and preventing drowning</li>
 * </ul>
 *
 * <p>Potency scales the Conduit Power amplifier level.</p>
 */
public class CEffectOctans extends ConstellationEffectProvider {

    private static final double DEFAULT_RANGE = 16.0;

    /** Duration of the water buffs in ticks. */
    private static final int BUFF_DURATION = 100;

    public CEffectOctans() {
        super("octans");
    }

    @Override
    public void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                     @Nonnull ConstellationEffectProperties properties) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB area = createArea(pos, properties);
        double potency = properties.getPotency();

        applyWaterBuffs(serverLevel, area, potency);
    }

    /**
     * Grant Water Breathing and Conduit Power to all players in the area.
     */
    private void applyWaterBuffs(@Nonnull ServerLevel level, @Nonnull AABB area,
                                 double potency) {
        int conduitAmplifier = potency >= 2.0 ? 1 : 0;
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.WATER_BREATHING, BUFF_DURATION, 0, true, false));
            player.addEffect(new MobEffectInstance(
                    MobEffects.CONDUIT_POWER, BUFF_DURATION, conduitAmplifier, true, false));
        }
    }

    @Nonnull
    @Override
    public ConstellationEffectProperties provideProperties() {
        return new ConstellationEffectProperties(DEFAULT_RANGE);
    }
}
