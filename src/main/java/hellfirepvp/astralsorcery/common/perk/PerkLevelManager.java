/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;

import javax.annotation.Nonnull;

/**
 * Manages perk level calculations from experience points.
 * Each level grants one perk point for allocation in the tree.
 *
 * <p>Leveling curve: quadratic with a linear component.
 * {@code expForLevel(n) = BASE * n^2 + LINEAR * n}</p>
 *
 * <p>The maximum level caps the number of allocatable perk points,
 * preventing unlimited power scaling.</p>
 */
public final class PerkLevelManager {

    private PerkLevelManager() {}

    /** Base quadratic coefficient for XP curve. */
    private static final long BASE_EXP = 150L;

    /** Linear coefficient for XP curve. */
    private static final long LINEAR_EXP = 100L;

    /** Maximum perk level (and thus maximum perk points from leveling). */
    public static final int MAX_LEVEL = 40;

    /** Starting perk points granted at level 0 (before any XP). */
    private static final int STARTING_POINTS = 1;

    /**
     * Computes the total experience required to reach a given level.
     *
     * @param level the target level (0-based)
     * @return total cumulative experience needed
     */
    public static long getTotalExpForLevel(int level) {
        if (level <= 0) return 0;
        level = Math.min(level, MAX_LEVEL);
        long total = 0;
        for (int i = 1; i <= level; i++) {
            total += getExpForLevelUp(i);
        }
        return total;
    }

    /**
     * Gets the experience required to advance from level (n-1) to level n.
     *
     * @param level the level being reached (1-based)
     * @return experience needed for this single level-up
     */
    public static long getExpForLevelUp(int level) {
        if (level <= 0) return 0;
        return BASE_EXP * (long) level * level + LINEAR_EXP * level;
    }

    /**
     * Computes the current level from total accumulated experience.
     *
     * @param totalExp the player's total perk experience
     * @return the current level (0 to MAX_LEVEL)
     */
    public static int getLevelFromExp(long totalExp) {
        if (totalExp <= 0) return 0;
        int level = 0;
        long remaining = totalExp;
        while (level < MAX_LEVEL) {
            long needed = getExpForLevelUp(level + 1);
            if (remaining < needed) {
                break;
            }
            remaining -= needed;
            level++;
        }
        return level;
    }

    /**
     * Gets the experience progress toward the next level as a fraction [0, 1).
     *
     * @param totalExp the player's total perk experience
     * @return fractional progress, or 1.0 if at max level
     */
    public static float getLevelProgress(long totalExp) {
        int level = getLevelFromExp(totalExp);
        if (level >= MAX_LEVEL) return 1.0f;

        long currentLevelExp = getTotalExpForLevel(level);
        long nextLevelExp = getExpForLevelUp(level + 1);
        if (nextLevelExp <= 0) return 1.0f;

        return (float) (totalExp - currentLevelExp) / (float) nextLevelExp;
    }

    /**
     * Gets the total number of perk points available to a player at a given level.
     * This is STARTING_POINTS + level (one point per level-up).
     *
     * @param level the player's current perk level
     * @return total perk points earned
     */
    public static int getPerkPointsForLevel(int level) {
        return STARTING_POINTS + Math.min(level, MAX_LEVEL);
    }

    /**
     * Convenience: gets the level and available perk points from a PlayerProgress.
     *
     * @param progress the player's progress data
     * @return the number of unspent perk points
     */
    public static int getAvailablePerkPoints(@Nonnull PlayerProgress progress) {
        int level = getLevelFromExp(progress.getPerkExp());
        int totalPoints = getPerkPointsForLevel(level);
        return Math.max(0, totalPoints - progress.getPerkPoints());
    }
}
