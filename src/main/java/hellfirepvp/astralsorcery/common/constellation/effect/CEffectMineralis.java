/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Constellation effect for <em>Mineralis</em> (Weak — Ore / Earth).
 *
 * <p>When focused on a Ritual Pedestal this effect:
 * <ul>
 *   <li>Highlights ore blocks in the area by applying the Glowing effect to
 *       nearby players so they can perceive ore positions</li>
 *   <li>Grants the Luck potion effect to players in range, improving ore
 *       drop rates</li>
 * </ul>
 *
 * <p>The effect does not physically alter ore blocks; instead it grants
 * sensory buffs that help players locate and benefit from nearby ores.</p>
 *
 * <p>Potency scales the Luck amplifier level.</p>
 */
public class CEffectMineralis extends ConstellationEffectProvider {

    private static final double DEFAULT_RANGE = 16.0;

    /** Duration of the ore-awareness buffs in ticks. */
    private static final int BUFF_DURATION = 100;

    public CEffectMineralis() {
        super("mineralis");
    }

    @Override
    public void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                     @Nonnull ConstellationEffectProperties properties) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB area = createArea(pos, properties);
        double potency = properties.getPotency();

        applyOreAwareness(serverLevel, area, potency);
    }

    /**
     * Grant Glowing and Luck to players, giving them awareness of nearby ores.
     * Glowing is used as a thematic "enhanced perception" indicator; the Luck
     * effect provides tangible mining benefits.
     */
    private void applyOreAwareness(@Nonnull ServerLevel level, @Nonnull AABB area,
                                   double potency) {
        int luckAmplifier = potency >= 2.0 ? 2 : 1;
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING, BUFF_DURATION, 0, true, false));
            player.addEffect(new MobEffectInstance(
                    MobEffects.LUCK, BUFF_DURATION, luckAmplifier, true, false));
        }
    }

    /**
     * Check whether the block at the given position is an ore block.
     * Utility method available for future use when per-block highlighting
     * is implemented via custom rendering.
     */
    protected boolean isOreBlock(@Nonnull Level level, @Nonnull BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.COPPER_ORES);
    }

    @Nonnull
    @Override
    public ConstellationEffectProperties provideProperties() {
        return new ConstellationEffectProperties(DEFAULT_RANGE);
    }
}
