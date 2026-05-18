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
 * Constellation effect for <em>Vicio</em> (Major — Movement / Speed).
 *
 * <p>When focused on a Ritual Pedestal this effect:
 * <ul>
 *   <li>Grants Speed II to all players within the area</li>
 *   <li>Grants Jump Boost II to all players within the area</li>
 * </ul>
 *
 * <p>Potency scales the amplifier levels of both buffs.</p>
 */
public class CEffectVicio extends ConstellationEffectProvider {

    private static final double DEFAULT_RANGE = 16.0;

    /** Duration of the movement buffs in ticks. */
    private static final int BUFF_DURATION = 100;

    /** Base amplifier for speed and jump (0-indexed: 1 = level II). */
    private static final int BASE_AMPLIFIER = 1;

    public CEffectVicio() {
        super("vicio");
    }

    @Override
    public void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                     @Nonnull ConstellationEffectProperties properties) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB area = createArea(pos, properties);
        double potency = properties.getPotency();

        applyMovementBuffs(serverLevel, area, potency);
    }

    /**
     * Grant Speed and Jump Boost to all players inside the area.
     */
    private void applyMovementBuffs(@Nonnull ServerLevel level, @Nonnull AABB area,
                                    double potency) {
        int amplifier = potency >= 2.0 ? BASE_AMPLIFIER + 1 : BASE_AMPLIFIER;
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED, BUFF_DURATION, amplifier, true, false));
            player.addEffect(new MobEffectInstance(
                    MobEffects.JUMP, BUFF_DURATION, amplifier, true, false));
        }
    }

    @Nonnull
    @Override
    public ConstellationEffectProperties provideProperties() {
        return new ConstellationEffectProperties(DEFAULT_RANGE);
    }
}
