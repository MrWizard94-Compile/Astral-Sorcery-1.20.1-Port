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
 * Constellation effect for <em>Armara</em> (Major — Defense / Protection).
 *
 * <p>When focused on a Ritual Pedestal this effect:
 * <ul>
 *   <li>Grants Resistance to all players within the area, reducing incoming
 *       damage</li>
 *   <li>At high potency the Resistance amplifier is increased, providing
 *       stronger damage reduction</li>
 * </ul>
 *
 * <p>Potency scales the Resistance amplifier level.</p>
 */
public class CEffectArmara extends ConstellationEffectProvider {

    private static final double DEFAULT_RANGE = 16.0;

    /** Duration of the Resistance buff in ticks. */
    private static final int RESISTANCE_DURATION = 100;

    public CEffectArmara() {
        super("armara");
    }

    @Override
    public void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                     @Nonnull ConstellationEffectProperties properties) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB area = createArea(pos, properties);
        double potency = properties.getPotency();

        applyProtection(serverLevel, area, potency);
    }

    /**
     * Grant Resistance to all players inside the area.
     */
    private void applyProtection(@Nonnull ServerLevel level, @Nonnull AABB area,
                                 double potency) {
        int amplifier = potency >= 2.0 ? 1 : 0;
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, RESISTANCE_DURATION, amplifier, true, false));
        }
    }

    @Nonnull
    @Override
    public ConstellationEffectProperties provideProperties() {
        return new ConstellationEffectProperties(DEFAULT_RANGE);
    }
}
