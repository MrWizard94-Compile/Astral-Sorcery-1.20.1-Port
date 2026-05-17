/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.lib.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Validates multiblock structures against defined patterns.
 * Provides detailed validation results including which blocks
 * don't match, for both server-side logic and client-side
 * structure preview rendering.
 *
 * <p>This replaces ObserverLib's structure subscription/validation system
 * with a simpler poll-based approach. Block entities that care about
 * structure validity call {@link #validate} periodically (typically every
 * 20-40 ticks).</p>
 */
public final class StructureValidator {

    private StructureValidator() {}

    /**
     * Result of a structure validation check.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final int matchedBlocks;
        private final int totalBlocks;
        @Nullable
        private final BlockPos firstMismatch;

        private ValidationResult(boolean valid, int matched, int total,
                                 @Nullable BlockPos firstMismatch) {
            this.valid = valid;
            this.matchedBlocks = matched;
            this.totalBlocks = total;
            this.firstMismatch = firstMismatch;
        }

        public boolean isValid() { return valid; }
        public int getMatchedBlocks() { return matchedBlocks; }
        public int getTotalBlocks() { return totalBlocks; }

        /**
         * @return completion fraction [0, 1] — how much of the structure is built
         */
        public float getCompletionFraction() {
            return totalBlocks > 0 ? (float) matchedBlocks / totalBlocks : 0;
        }

        /**
         * @return the first world position that didn't match, or null if valid
         */
        @Nullable
        public BlockPos getFirstMismatch() { return firstMismatch; }
    }

    /**
     * Validate a pattern against the world at the given origin.
     * Returns a detailed result including match count.
     *
     * @param level   the world to check
     * @param origin  center position of the multiblock
     * @param pattern the pattern to validate against
     * @return validation result
     */
    @Nonnull
    public static ValidationResult validate(@Nonnull Level level,
                                             @Nonnull BlockPos origin,
                                             @Nonnull PatternBlockArray pattern) {
        int matched = 0;
        int total = pattern.size();
        BlockPos firstMismatch = null;

        for (Map.Entry<BlockPos, MatchableState> entry : pattern.getPattern().entrySet()) {
            BlockPos worldPos = origin.offset(entry.getKey());
            BlockState worldState = level.getBlockState(worldPos);

            if (entry.getValue().matches(worldState)) {
                matched++;
            } else if (firstMismatch == null) {
                firstMismatch = worldPos;
            }
        }

        boolean valid = (matched == total);
        return new ValidationResult(valid, matched, total, firstMismatch);
    }

    /**
     * Quick boolean check — equivalent to {@code validate(...).isValid()}
     * but short-circuits on first mismatch for better performance.
     *
     * @param level   the world
     * @param origin  center position
     * @param pattern the pattern to check
     * @return true if all blocks match
     */
    public static boolean isValid(@Nonnull Level level,
                                   @Nonnull BlockPos origin,
                                   @Nonnull PatternBlockArray pattern) {
        return pattern.matches(level, origin);
    }

    /**
     * Counts how many pattern blocks currently match in the world.
     * Useful for progress display during structure construction.
     *
     * @param level   the world
     * @param origin  center position
     * @param pattern the pattern to check
     * @return number of matching blocks
     */
    public static int countMatches(@Nonnull Level level,
                                    @Nonnull BlockPos origin,
                                    @Nonnull PatternBlockArray pattern) {
        int count = 0;
        for (Map.Entry<BlockPos, MatchableState> entry : pattern.getPattern().entrySet()) {
            BlockPos worldPos = origin.offset(entry.getKey());
            BlockState worldState = level.getBlockState(worldPos);
            if (entry.getValue().matches(worldState)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the expected block state at a specific relative offset within a pattern.
     * Used by structure preview renderers to show ghost blocks.
     *
     * @param pattern  the pattern
     * @param relative the relative position to query
     * @return the expected state, or null if no block is defined at that position
     */
    @Nullable
    public static BlockState getExpectedState(@Nonnull PatternBlockArray pattern,
                                              @Nonnull BlockPos relative) {
        MatchableState matchable = pattern.getMatchableAt(relative);
        if (matchable == null) return null;
        return matchable.getDescriptiveState(0);
    }
}
