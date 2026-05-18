/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Constellation effect for <em>Evorsio</em> (Major — Destruction / Mining).
 *
 * <p>When focused on a Ritual Pedestal this effect:
 * <ul>
 *   <li>Breaks random blocks within the area of effect, acting like a
 *       slow quarry</li>
 *   <li>Only targets blocks with a destroy speed at or below a configurable
 *       hardness threshold (avoids bedrock and obsidian)</li>
 *   <li>Broken blocks drop their loot as if mined by a player</li>
 * </ul>
 *
 * <p>Potency scales the number of blocks broken per tick and the maximum
 * hardness of targetable blocks.</p>
 */
public class CEffectEvorsio extends ConstellationEffectProvider {

    private static final double DEFAULT_RANGE = 16.0;

    /** Maximum block hardness that can be broken at base potency. */
    private static final float BASE_MAX_HARDNESS = 3.0F;

    /** Base number of blocks to attempt breaking per tick. */
    private static final int BASE_BREAK_ATTEMPTS = 1;

    public CEffectEvorsio() {
        super("evorsio");
    }

    @Override
    public void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                     @Nonnull ConstellationEffectProperties properties) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        double potency = properties.getPotency();
        int range = (int) properties.getSize();

        breakBlocks(serverLevel, pos, range, potency);
    }

    /**
     * Attempt to break random blocks within range.
     * Only breaks blocks whose destroy speed is at or below the hardness threshold.
     */
    private void breakBlocks(@Nonnull ServerLevel level, @Nonnull BlockPos pos,
                             int range, double potency) {
        int attempts = (int) Math.max(1, BASE_BREAK_ATTEMPTS * potency);
        float maxHardness = (float) (BASE_MAX_HARDNESS * potency);

        for (int i = 0; i < attempts; i++) {
            BlockPos target = pos.offset(
                    level.getRandom().nextIntBetweenInclusive(-range, range),
                    level.getRandom().nextIntBetweenInclusive(-3, 3),
                    level.getRandom().nextIntBetweenInclusive(-range, range));

            BlockState state = level.getBlockState(target);
            if (state.isAir()) {
                continue;
            }

            float hardness = state.getDestroySpeed(level, target);
            // -1 means unbreakable (bedrock etc.), skip those
            if (hardness >= 0 && hardness <= maxHardness) {
                level.destroyBlock(target, true);
            }
        }
    }

    @Nonnull
    @Override
    public ConstellationEffectProperties provideProperties() {
        return new ConstellationEffectProperties(DEFAULT_RANGE);
    }
}
