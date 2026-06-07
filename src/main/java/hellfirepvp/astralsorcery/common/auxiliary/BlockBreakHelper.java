package hellfirepvp.astralsorcery.common.auxiliary;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility for controlled block breaking initiated by mod effects.
 * Prevents recursive break loops when perk effects trigger additional breaks
 * (e.g., vein mining or area break perks).
 *
 * <p>1.16 -> 1.20 changes:
 * World -> Level, ServerWorld -> ServerLevel,
 * ServerPlayerEntity -> ServerPlayer,
 * BlockState.getBlockHardness -> BlockState.getDestroySpeed,
 * ForgeHooks.onBlockBreakEvent unchanged,
 * Block.spawnDrops -> Block.dropResources</p>
 */
public class BlockBreakHelper {

    private static final ThreadLocal<Set<BlockPos>> activeBreaks = ThreadLocal.withInitial(HashSet::new);

    private BlockBreakHelper() {}

    /**
     * Check if a break is currently being processed at the given position
     * (guards against recursive break loops).
     *
     * @param pos the position to check
     * @return true if a break is already in progress at this position
     */
    public static boolean isBreaking(@Nonnull BlockPos pos) {
        return activeBreaks.get().contains(pos);
    }

    /**
     * Attempt to break a block as if the player mined it, with proper event
     * firing and drop spawning. Guards against recursive calls.
     *
     * @param level  the server level
     * @param pos    the position to break
     * @param player the player "responsible" for the break (for event context)
     * @return true if the block was successfully broken
     */
    public static boolean breakBlockAsPlayer(@Nonnull ServerLevel level,
                                             @Nonnull BlockPos pos,
                                             @Nonnull ServerPlayer player) {
        return breakBlockAsPlayer(level, pos, player, player.getMainHandItem(), true);
    }

    /**
     * Attempt to break a block as if the player mined it.
     *
     * @param level      the server level
     * @param pos        the position to break
     * @param player     the player responsible
     * @param tool       the tool to use for drop calculation
     * @param spawnDrops whether to spawn item drops
     * @return true if the block was successfully broken
     */
    public static boolean breakBlockAsPlayer(@Nonnull ServerLevel level,
                                             @Nonnull BlockPos pos,
                                             @Nonnull ServerPlayer player,
                                             @Nonnull ItemStack tool,
                                             boolean spawnDrops) {
        Set<BlockPos> breaking = activeBreaks.get();
        if (breaking.contains(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }

        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0) {
            return false; // Unbreakable
        }

        breaking.add(pos);
        try {
            // Fire Forge block break event
            int expToDrop = ForgeHooks.onBlockBreakEvent(level, player.gameMode.getGameModeForPlayer(), player, pos);
            if (expToDrop == -1) {
                return false; // Event cancelled
            }

            BlockEntity be = level.getBlockEntity(pos);

            if (spawnDrops) {
                Block.dropResources(state, level, pos, be, player, tool);
            }
            if (expToDrop > 0) {
                state.getBlock().popExperience(level, pos, expToDrop);
            }

            boolean removed = level.removeBlock(pos, false);
            if (removed) {
                state.getBlock().destroy(level, pos, state);
            }
            return removed;
        } finally {
            breaking.remove(pos);
        }
    }

    /**
     * Break a block without player context (e.g., from entity effects).
     * Does not fire player-specific events.
     *
     * @param level      the server level
     * @param pos        the position to break
     * @param spawnDrops whether to spawn item drops
     * @return true if the block was broken
     */
    public static boolean breakBlock(@Nonnull ServerLevel level,
                                     @Nonnull BlockPos pos,
                                     boolean spawnDrops) {
        Set<BlockPos> breaking = activeBreaks.get();
        if (breaking.contains(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }

        breaking.add(pos);
        try {
            if (spawnDrops) {
                BlockEntity be = level.getBlockEntity(pos);
                Block.dropResources(state, level, pos, be);
            }
            return level.destroyBlock(pos, false);
        } finally {
            breaking.remove(pos);
        }
    }
}
