/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Constellation effect for <em>Aevitas</em> (Major — Life / Growth).
 *
 * <p>When focused on a Ritual Pedestal this effect:
 * <ul>
 *   <li>Heals all living entities (players and passive mobs) in range</li>
 *   <li>Applies the Regeneration potion effect to nearby players</li>
 *   <li>Accelerates crop and plant growth within the area of effect</li>
 * </ul>
 *
 * <p>Potency scales the healing amount and the number of growth attempts
 * per tick.</p>
 */
public class CEffectAevitas extends ConstellationEffectProvider {

    /** Default ritual range in blocks. */
    private static final double DEFAULT_RANGE = 16.0;

    /** Base heal amount per effect tick. */
    private static final float BASE_HEAL_AMOUNT = 1.0F;

    /** Base number of crop-growth attempts per tick. */
    private static final int BASE_GROWTH_ATTEMPTS = 3;

    /** Duration of the Regeneration buff in ticks. */
    private static final int REGEN_DURATION = 100;

    public CEffectAevitas() {
        super("aevitas");
    }

    @Override
    public void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                     @Nonnull ConstellationEffectProperties properties) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB area = createArea(pos, properties);
        double potency = properties.getPotency();

        healEntities(serverLevel, area, potency);
        applyRegeneration(serverLevel, area, potency);
        growCrops(serverLevel, pos, properties, potency);
    }

    /**
     * Heal living entities (players and passive mobs) inside the area.
     */
    private void healEntities(@Nonnull ServerLevel level, @Nonnull AABB area,
                              double potency) {
        float healAmount = (float) (BASE_HEAL_AMOUNT * potency);
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class, area,
                e -> e instanceof Player || e instanceof Animal);

        for (LivingEntity entity : entities) {
            if (entity.getHealth() < entity.getMaxHealth()) {
                entity.heal(healAmount);
            }
        }
    }

    /**
     * Apply Regeneration I to all players in the area.
     */
    private void applyRegeneration(@Nonnull ServerLevel level, @Nonnull AABB area,
                                   double potency) {
        int amplifier = potency >= 2.0 ? 1 : 0;
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION, REGEN_DURATION, amplifier, true, false));
        }
    }

    /**
     * Attempt to grow random crops/plants within the effect range.
     */
    private void growCrops(@Nonnull ServerLevel level, @Nonnull BlockPos pos,
                           @Nonnull ConstellationEffectProperties properties,
                           double potency) {
        int range = (int) properties.getSize();
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
