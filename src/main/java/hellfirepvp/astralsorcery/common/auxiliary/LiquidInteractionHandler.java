/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.auxiliary;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles in-world liquid interaction recipe processing.
 * When liquid starlight flows adjacent to other fluids (water, lava),
 * eligible recipes may produce items at the interaction boundary.
 *
 * <p>Called from a block tick or fluid flow handler when liquid starlight
 * is placed or flows next to another fluid source/flowing block.</p>
 *
 * <p>Interactions are probabilistic — each tick at an interaction boundary,
 * there is a chance (based on recipe weight) that the reaction triggers.</p>
 *
 * <p>1.16 → 1.20 changes: FluidState access via getBlockState().getFluidState()
 * remains stable. Block/fluid tick mechanics unchanged.</p>
 */
public final class LiquidInteractionHandler {

    private LiquidInteractionHandler() {}

    /** Base probability per tick that any interaction fires at a boundary. */
    private static final float BASE_INTERACTION_CHANCE = 0.025f;

    /**
     * Checks for liquid interactions around a given position.
     * Call this when a liquid starlight block is placed or receives a random tick.
     *
     * @param level the server level
     * @param pos   the position of a fluid block (typically liquid starlight)
     * @return true if an interaction occurred
     */
    public static boolean checkInteraction(@Nonnull ServerLevel level, @Nonnull BlockPos pos) {
        FluidState centerFluid = level.getFluidState(pos);
        if (centerFluid.isEmpty()) {
            return false;
        }

        // Check all 6 adjacent positions for a different fluid
        for (Direction dir : Direction.values()) {
            BlockPos adjacent = pos.relative(dir);
            FluidState adjacentFluid = level.getFluidState(adjacent);
            if (adjacentFluid.isEmpty()) {
                continue;
            }

            // Skip if same fluid type
            if (centerFluid.getType() == adjacentFluid.getType()) {
                continue;
            }

            // Build fluid stacks for recipe matching
            FluidStack stack1 = new FluidStack(centerFluid.getType(), 1000);
            FluidStack stack2 = new FluidStack(adjacentFluid.getType(), 1000);

            // Find matching recipe
            LiquidInteraction recipe = findMatchingRecipe(level, stack1, stack2);
            if (recipe != null) {
                return tryInteraction(level, pos, adjacent, recipe);
            }
        }
        return false;
    }

    /**
     * Attempts to fire a liquid interaction at the boundary between two fluid positions.
     */
    private static boolean tryInteraction(@Nonnull ServerLevel level,
                                           @Nonnull BlockPos fluidPos1,
                                           @Nonnull BlockPos fluidPos2,
                                           @Nonnull LiquidInteraction recipe) {
        RandomSource random = level.getRandom();

        // Probability based on recipe weight and base chance
        float chance = BASE_INTERACTION_CHANCE * recipe.getWeight();
        if (random.nextFloat() >= chance) {
            return false;
        }

        // Spawn the output item between the two positions
        ItemStack output = recipe.getOutputItem();
        if (output.isEmpty()) {
            return false;
        }

        double x = (fluidPos1.getX() + fluidPos2.getX()) / 2.0 + 0.5;
        double y = Math.max(fluidPos1.getY(), fluidPos2.getY()) + 0.8;
        double z = (fluidPos1.getZ() + fluidPos2.getZ()) / 2.0 + 0.5;

        ItemEntity itemEntity = new ItemEntity(level, x, y, z, output.copy());
        itemEntity.setDefaultPickUpDelay();
        // Small upward velocity so it doesn't immediately fall back into fluid
        itemEntity.setDeltaMovement(
                (random.nextDouble() - 0.5) * 0.05,
                0.15 + random.nextDouble() * 0.1,
                (random.nextDouble() - 0.5) * 0.05);
        level.addFreshEntity(itemEntity);

        AstralSorcery.log.debug("Liquid interaction at ({}, {}, {}): produced {}",
                (int) x, (int) y, (int) z, output.getItem().getDescriptionId());
        return true;
    }

    /**
     * Finds a matching liquid interaction recipe for the given fluid pair.
     *
     * @param level  the level (for recipe manager access)
     * @param fluid1 first fluid
     * @param fluid2 second fluid
     * @return matching recipe, or null if no match
     */
    @Nullable
    private static LiquidInteraction findMatchingRecipe(@Nonnull Level level,
                                                         @Nonnull FluidStack fluid1,
                                                         @Nonnull FluidStack fluid2) {
        List<LiquidInteraction> recipes = level.getRecipeManager()
                .getAllRecipesFor(RecipeTypesAS.LIQUID_INTERACTION.get());

        List<LiquidInteraction> matching = new ArrayList<>();
        for (LiquidInteraction recipe : recipes) {
            if (recipe.matchesFluids(fluid1, fluid2)) {
                matching.add(recipe);
            }
        }

        if (matching.isEmpty()) {
            return null;
        }

        // If multiple recipes match, pick one weighted by recipe weight
        if (matching.size() == 1) {
            return matching.get(0);
        }

        float totalWeight = 0;
        for (LiquidInteraction recipe : matching) {
            totalWeight += recipe.getWeight();
        }

        float roll = level.getRandom().nextFloat() * totalWeight;
        float cumulative = 0;
        for (LiquidInteraction recipe : matching) {
            cumulative += recipe.getWeight();
            if (roll < cumulative) {
                return recipe;
            }
        }
        return matching.get(matching.size() - 1);
    }
}
