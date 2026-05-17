/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.auxiliary;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.BlockTransmutation;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Manages block transmutation progress. When concentrated starlight
 * hits a block that has a matching {@link BlockTransmutation} recipe,
 * progress accumulates over time until the block transforms.
 *
 * <p>Each position tracked has a starlight counter. Once it reaches the
 * recipe's required starlight amount, the block state is replaced with
 * the recipe output. Progress decays slowly if no starlight arrives
 * (prevents permanent partial progress).</p>
 *
 * <p>Transmutation is triggered by the starlight network when a beam
 * terminates at a non-receiver block, or when liquid starlight flows
 * adjacent to a transmutable block.</p>
 */
public class TransmutationHelper {

    /** Starlight progress per position per dimension. */
    private static final Map<TransmutationKey, Double> progressMap = new HashMap<>();

    /** How much progress decays per tick with no new starlight. */
    private static final double DECAY_PER_TICK = 0.5;

    /** Positions that were advanced this tick (skip decay). */
    private static final Set<TransmutationKey> activeThisTick = new HashSet<>();

    private TransmutationHelper() {}

    /**
     * Adds starlight to a block position for transmutation.
     * Called by the starlight network when a beam hits a block,
     * or by liquid starlight fluid interactions.
     *
     * @param level    the server level
     * @param pos      the target block position
     * @param amount   starlight amount to add
     * @return true if the transmutation completed this tick
     */
    public static boolean addStarlight(@Nonnull ServerLevel level,
                                        @Nonnull BlockPos pos,
                                        double amount) {
        BlockState state = level.getBlockState(pos);
        BlockTransmutation recipe = findRecipe(level, state);
        if (recipe == null) {
            return false;
        }

        TransmutationKey key = new TransmutationKey(level.dimension().location().toString(), pos.immutable());
        double current = progressMap.getOrDefault(key, 0.0);
        current += amount;
        activeThisTick.add(key);

        if (current >= recipe.getStarlightRequired()) {
            // Transmutation complete!
            level.setBlock(pos, recipe.getOutputState(), 3);
            progressMap.remove(key);
            AstralSorcery.log.debug("Transmutation complete at {}: {} -> {}",
                    pos.toShortString(),
                    state.getBlock().getName().getString(),
                    recipe.getOutputState().getBlock().getName().getString());
            return true;
        }

        progressMap.put(key, current);
        return false;
    }

    /**
     * Gets the current progress ratio [0, 1] for a position.
     * Used by client rendering to show particle intensity.
     *
     * @param level the level
     * @param pos   the block position
     * @return progress ratio, or 0 if no progress
     */
    public static float getProgress(@Nonnull Level level, @Nonnull BlockPos pos) {
        TransmutationKey key = new TransmutationKey(level.dimension().location().toString(), pos.immutable());
        double current = progressMap.getOrDefault(key, 0.0);
        if (current <= 0) return 0f;

        BlockState state = level.getBlockState(pos);
        BlockTransmutation recipe = findRecipe(level, state);
        if (recipe == null) return 0f;

        return (float) Math.min(1.0, current / recipe.getStarlightRequired());
    }

    /**
     * Ticks decay for all tracked positions.
     * Called once per server tick (from StarlightNetworkRegistry or similar).
     */
    public static void tickDecay() {
        Iterator<Map.Entry<TransmutationKey, Double>> it = progressMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<TransmutationKey, Double> entry = it.next();
            if (activeThisTick.contains(entry.getKey())) {
                continue; // Was advanced this tick — no decay
            }
            double newProgress = entry.getValue() - DECAY_PER_TICK;
            if (newProgress <= 0) {
                it.remove();
            } else {
                entry.setValue(newProgress);
            }
        }
        activeThisTick.clear();
    }

    /**
     * Clears all tracked transmutation progress.
     * Called on server stop.
     */
    public static void clearAll() {
        progressMap.clear();
        activeThisTick.clear();
    }

    /**
     * Finds a matching transmutation recipe for the given block state.
     */
    @Nullable
    private static BlockTransmutation findRecipe(@Nonnull Level level, @Nonnull BlockState state) {
        List<BlockTransmutation> allRecipes = level.getRecipeManager()
                .getAllRecipesFor(RecipeTypesAS.BLOCK_TRANSMUTATION.get());
        for (BlockTransmutation recipe : allRecipes) {
            if (recipe.matchesInput(state)) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Key for tracking transmutation progress per dimension+position.
     */
    private record TransmutationKey(@Nonnull String dimension, @Nonnull BlockPos pos) {}
}
