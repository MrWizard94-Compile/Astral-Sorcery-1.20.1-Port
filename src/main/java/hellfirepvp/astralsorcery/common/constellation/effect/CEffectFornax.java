/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * Constellation effect for <em>Fornax</em> (Weak — Fire / Smelting).
 *
 * <p>When focused on a Ritual Pedestal this effect:
 * <ul>
 *   <li>Smelts item entities lying on the ground within the area, converting
 *       raw ores to ingots using furnace recipes</li>
 *   <li>Ignites flammable surfaces by placing fire blocks in the area</li>
 *   <li>Grants Fire Resistance to all players in the area</li>
 * </ul>
 *
 * <p>Potency scales the number of items smelted per tick and the number
 * of fire-placement attempts.</p>
 */
public class CEffectFornax extends ConstellationEffectProvider {

    private static final double DEFAULT_RANGE = 16.0;

    /** Maximum number of item entities to smelt per tick. */
    private static final int BASE_SMELT_COUNT = 3;

    /** Base number of fire placement attempts per tick. */
    private static final int BASE_FIRE_ATTEMPTS = 1;

    /** Duration of Fire Resistance in ticks. */
    private static final int FIRE_RESIST_DURATION = 100;

    public CEffectFornax() {
        super("fornax");
    }

    @Override
    public void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                     @Nonnull ConstellationEffectProperties properties) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB area = createArea(pos, properties);
        double potency = properties.getPotency();

        smeltGroundItems(serverLevel, area, potency);
        placeFires(serverLevel, pos, properties, potency);
        grantFireResistance(serverLevel, area);
    }

    /**
     * Smelt item entities on the ground using furnace recipes.
     */
    private void smeltGroundItems(@Nonnull ServerLevel level, @Nonnull AABB area,
                                  double potency) {
        int maxSmelt = (int) Math.max(1, BASE_SMELT_COUNT * potency);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);
        int smelted = 0;

        for (ItemEntity itemEntity : items) {
            if (smelted >= maxSmelt) break;
            if (itemEntity.isRemoved()) continue;

            ItemStack input = itemEntity.getItem();
            Optional<SmeltingRecipe> recipe = level.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING,
                            new SimpleContainer(input), level);

            if (recipe.isPresent()) {
                ItemStack result = recipe.get().getResultItem(level.registryAccess()).copy();
                result.setCount(input.getCount());

                itemEntity.setItem(result);
                smelted++;
            }
        }
    }

    /**
     * Place fire on random positions within range.
     */
    private void placeFires(@Nonnull ServerLevel level, @Nonnull BlockPos pos,
                            @Nonnull ConstellationEffectProperties properties,
                            double potency) {
        int range = (int) properties.getSize();
        int attempts = (int) Math.max(1, BASE_FIRE_ATTEMPTS * potency);

        for (int i = 0; i < attempts; i++) {
            BlockPos target = pos.offset(
                    level.getRandom().nextIntBetweenInclusive(-range, range),
                    level.getRandom().nextIntBetweenInclusive(-2, 2),
                    level.getRandom().nextIntBetweenInclusive(-range, range));

            BlockState below = level.getBlockState(target.below());
            if (level.getBlockState(target).isAir() && below.isSolidRender(level, target.below())) {
                level.setBlockAndUpdate(target, Blocks.FIRE.defaultBlockState());
            }
        }
    }

    /**
     * Grant Fire Resistance to all players inside the area.
     */
    private void grantFireResistance(@Nonnull ServerLevel level, @Nonnull AABB area) {
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.FIRE_RESISTANCE, FIRE_RESIST_DURATION, 0, true, false));
        }
    }

    @Nonnull
    @Override
    public ConstellationEffectProperties provideProperties() {
        return new ConstellationEffectProperties(DEFAULT_RANGE);
    }
}
