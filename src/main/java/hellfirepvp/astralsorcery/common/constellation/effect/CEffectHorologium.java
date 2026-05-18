/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Constellation effect for <em>Horologium</em> (Weak — Time / Acceleration).
 *
 * <p>When focused on a Ritual Pedestal this effect:
 * <ul>
 *   <li>Accelerates tile-entity ticking in the area, effectively acting as a
 *       clock accelerator that speeds up machines, furnaces, and other
 *       processing blocks</li>
 *   <li>Also boosts crop growth at an accelerated rate compared to Aevitas</li>
 * </ul>
 *
 * <p>Potency scales the number of extra ticks applied to each block entity
 * and the number of crop growth attempts.</p>
 */
public class CEffectHorologium extends ConstellationEffectProvider {

    private static final double DEFAULT_RANGE = 8.0;

    /** Number of extra ticks applied to each found block entity. */
    private static final int BASE_EXTRA_TICKS = 2;

    /** Number of random crop growth attempts per effect tick. */
    private static final int BASE_GROWTH_ATTEMPTS = 6;

    /** Maximum number of block entities to accelerate per tick. */
    private static final int MAX_BE_SCAN_ATTEMPTS = 10;

    public CEffectHorologium() {
        super("horologium");
    }

    @Override
    public void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                     @Nonnull ConstellationEffectProperties properties) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        double potency = properties.getPotency();
        int range = (int) properties.getSize();

        accelerateBlockEntities(serverLevel, pos, range, potency);
        accelerateCropGrowth(serverLevel, pos, range, potency);
    }

    /**
     * Find random block entities within range and tick them extra times.
     */
    private void accelerateBlockEntities(@Nonnull ServerLevel level,
                                         @Nonnull BlockPos pos, int range,
                                         double potency) {
        int extraTicks = (int) Math.max(1, BASE_EXTRA_TICKS * potency);

        for (int i = 0; i < MAX_BE_SCAN_ATTEMPTS; i++) {
            BlockPos target = pos.offset(
                    level.getRandom().nextIntBetweenInclusive(-range, range),
                    level.getRandom().nextIntBetweenInclusive(-3, 3),
                    level.getRandom().nextIntBetweenInclusive(-range, range));

            BlockEntity blockEntity = level.getBlockEntity(target);
            if (blockEntity == null || blockEntity.isRemoved()) {
                continue;
            }

            // Avoid accelerating our own pedestal to prevent recursion
            if (target.equals(pos)) {
                continue;
            }

            // Use random tick-like acceleration on the block state
            BlockState state = level.getBlockState(target);
            for (int t = 0; t < extraTicks; t++) {
                if (state.isRandomlyTicking()) {
                    state.randomTick(level, target, level.getRandom());
                }
            }
        }
    }

    /**
     * Accelerate crop growth at a higher rate than Aevitas.
     */
    private void accelerateCropGrowth(@Nonnull ServerLevel level,
                                      @Nonnull BlockPos pos, int range,
                                      double potency) {
        int attempts = (int) (BASE_GROWTH_ATTEMPTS * potency);

        for (int i = 0; i < attempts; i++) {
            BlockPos target = pos.offset(
                    level.getRandom().nextIntBetweenInclusive(-range, range),
                    level.getRandom().nextIntBetweenInclusive(-3, 3),
                    level.getRandom().nextIntBetweenInclusive(-range, range));
            BlockState state = level.getBlockState(target);
            if (state.getBlock() instanceof BonemealableBlock growable) {
                if (growable.isValidBonemealTarget(level, target, state, false)) {
                    growable.performBonemeal(level, level.getRandom(), target, state);
                }
            }
        }
    }

    @Nonnull
    @Override
    public ConstellationEffectProperties provideProperties() {
        return new ConstellationEffectProperties(DEFAULT_RANGE);
    }
}
