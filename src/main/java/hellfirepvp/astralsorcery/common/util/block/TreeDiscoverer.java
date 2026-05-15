package hellfirepvp.astralsorcery.common.util.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Discovers connected tree blocks (logs + leaves) via flood-fill.
 * Used by tree-felling perks and Pelotrio constellation effects.
 *
 * <p>1.16 -> 1.20 changes:
 * BlockTags.LOGS/LEAVES are now TagKey&lt;Block&gt;,
 * state.isIn(tag) -> state.is(tag),
 * pos.offset(dir) -> pos.relative(dir)</p>
 */
public class TreeDiscoverer {

    private static final int MAX_SEARCH = 512;
    private static final int MAX_LOG_COUNT = 128;
    private static final Direction[] SEARCH_DIRECTIONS = Direction.values();

    private TreeDiscoverer() {}

    /**
     * Find all log blocks connected to the given position (flood-fill search).
     * Stops at {@value MAX_LOG_COUNT} logs to prevent runaway searches.
     *
     * @param level the level to search in
     * @param start the starting position (should be a log block)
     * @return list of connected log positions, including the start
     */
    @Nonnull
    public static List<BlockPos> findConnectedLogs(@Nonnull Level level, @Nonnull BlockPos start) {
        List<BlockPos> logs = new LinkedList<>();
        BlockState startState = level.getBlockState(start);

        if (!isLog(startState)) {
            return logs;
        }

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        int searched = 0;
        while (!queue.isEmpty() && logs.size() < MAX_LOG_COUNT && searched < MAX_SEARCH) {
            BlockPos current = queue.poll();
            searched++;

            BlockState state = level.getBlockState(current);
            if (isLog(state)) {
                logs.add(current);

                // Search all 6 directions + diagonals (26-connected for logs)
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            BlockPos neighbor = current.offset(dx, dy, dz);
                            if (!visited.contains(neighbor)) {
                                visited.add(neighbor);
                                BlockState neighborState = level.getBlockState(neighbor);
                                if (isLog(neighborState)) {
                                    queue.add(neighbor);
                                }
                            }
                        }
                    }
                }
            }
        }
        return logs;
    }

    /**
     * Find all tree blocks (logs + leaves) connected to the given log position.
     *
     * @param level the level
     * @param start the starting log position
     * @return list of all connected tree block positions
     */
    @Nonnull
    public static List<BlockPos> findConnectedTree(@Nonnull Level level, @Nonnull BlockPos start) {
        List<BlockPos> tree = new LinkedList<>();
        BlockState startState = level.getBlockState(start);

        if (!isLog(startState)) {
            return tree;
        }

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> logQueue = new ArrayDeque<>();
        logQueue.add(start);
        visited.add(start);

        // First pass: find all logs
        List<BlockPos> logs = new LinkedList<>();
        int searched = 0;
        while (!logQueue.isEmpty() && logs.size() < MAX_LOG_COUNT && searched < MAX_SEARCH) {
            BlockPos current = logQueue.poll();
            searched++;

            BlockState state = level.getBlockState(current);
            if (isLog(state)) {
                logs.add(current);
                tree.add(current);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            BlockPos neighbor = current.offset(dx, dy, dz);
                            if (!visited.contains(neighbor)) {
                                visited.add(neighbor);
                                if (isLog(level.getBlockState(neighbor))) {
                                    logQueue.add(neighbor);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Second pass: find leaves connected to found logs (limited distance)
        Queue<BlockPos> leafQueue = new ArrayDeque<>();
        for (BlockPos logPos : logs) {
            for (Direction dir : SEARCH_DIRECTIONS) {
                BlockPos adj = logPos.relative(dir);
                if (!visited.contains(adj)) {
                    visited.add(adj);
                    if (isLeaf(level.getBlockState(adj))) {
                        leafQueue.add(adj);
                    }
                }
            }
        }

        int leafDistance = 0;
        int maxLeafDistance = 6;
        while (!leafQueue.isEmpty() && leafDistance < maxLeafDistance) {
            int levelSize = leafQueue.size();
            for (int i = 0; i < levelSize; i++) {
                BlockPos current = leafQueue.poll();
                if (current == null) break;
                tree.add(current);

                for (Direction dir : SEARCH_DIRECTIONS) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        if (isLeaf(level.getBlockState(neighbor))) {
                            leafQueue.add(neighbor);
                        }
                    }
                }
            }
            leafDistance++;
        }

        return tree;
    }

    /**
     * Check if the given state is a log block.
     */
    public static boolean isLog(@Nonnull BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    /**
     * Check if the given state is a leaf block.
     */
    public static boolean isLeaf(@Nonnull BlockState state) {
        return state.is(BlockTags.LEAVES);
    }
}
