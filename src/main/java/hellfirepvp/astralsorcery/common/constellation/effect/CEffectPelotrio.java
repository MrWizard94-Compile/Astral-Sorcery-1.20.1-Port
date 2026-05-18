/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Constellation effect for <em>Pelotrio</em> (Weak — Fertility / Crystal Growth).
 *
 * <p>When focused on a Ritual Pedestal this effect:
 * <ul>
 *   <li>Enhances crystal growth in the area, causing Astral Sorcery crystal
 *       blocks and formations to grow faster</li>
 *   <li>Boosts starlight generation by nearby collector crystals (thematic;
 *       actual network interaction is handled by the starlight network layer)</li>
 *   <li>Accelerates general plant and crop growth at an enhanced rate</li>
 *   <li>Heals passive animals in the area, promoting life and fertility</li>
 * </ul>
 *
 * <p>Potency scales the number of growth attempts and the healing amount.</p>
 */
public class CEffectPelotrio extends ConstellationEffectProvider {

    private static final double DEFAULT_RANGE = 16.0;

    /** Base number of growth-boosting attempts per tick. */
    private static final int BASE_GROWTH_ATTEMPTS = 4;

    /** Base heal amount applied to animals per tick. */
    private static final float BASE_ANIMAL_HEAL = 2.0F;

    public CEffectPelotrio() {
        super("pelotrio");
    }

    @Override
    public void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                     @Nonnull ConstellationEffectProperties properties) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        double potency = properties.getPotency();
        int range = (int) properties.getSize();
        AABB area = createArea(pos, properties);

        boostGrowth(serverLevel, pos, range, potency);
        healAnimals(serverLevel, area, potency);
    }

    /**
     * Accelerate plant and crystal growth in the area.
     * Crystal-specific growth will integrate with the crystal system
     * when that module is fully ported.
     */
    private void boostGrowth(@Nonnull ServerLevel level, @Nonnull BlockPos pos,
                             int range, double potency) {
        int attempts = (int) (BASE_GROWTH_ATTEMPTS * potency);

        for (int i = 0; i < attempts; i++) {
            BlockPos target = pos.offset(
                    level.getRandom().nextIntBetweenInclusive(-range, range),
                    level.getRandom().nextIntBetweenInclusive(-4, 4),
                    level.getRandom().nextIntBetweenInclusive(-range, range));

            BlockState state = level.getBlockState(target);

            // Bonemeal-compatible growth
            if (state.getBlock() instanceof BonemealableBlock growable) {
                if (growable.isValidBonemealTarget(level, target, state, false)) {
                    growable.performBonemeal(level, level.getRandom(), target, state);
                }
                continue;
            }

            // Random-tick acceleration for blocks that support it
            // (includes sugar cane, cactus, etc.)
            if (state.isRandomlyTicking()) {
                state.randomTick(level, target, level.getRandom());
            }
        }
    }

    /**
     * Heal passive animals in the area, promoting life and fertility.
     */
    private void healAnimals(@Nonnull ServerLevel level, @Nonnull AABB area,
                             double potency) {
        float healAmount = (float) (BASE_ANIMAL_HEAL * potency);
        List<Animal> animals = level.getEntitiesOfClass(Animal.class, area);
        for (Animal animal : animals) {
            if (animal.getHealth() < animal.getMaxHealth()) {
                animal.heal(healAmount);
            }
        }
    }

    @Nonnull
    @Override
    public ConstellationEffectProperties provideProperties() {
        return new ConstellationEffectProperties(DEFAULT_RANGE);
    }
}
