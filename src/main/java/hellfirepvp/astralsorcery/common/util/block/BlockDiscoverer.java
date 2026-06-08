package hellfirepvp.astralsorcery.common.util.block;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Block discovery/search utilities. Finds blocks matching criteria in the world,
 * performs flood-fill searches, and discovers chains of identical blocks.
 *
 * <p>1.16 -> 1.20 changes:
 * World -> Level, TileEntity -> BlockEntity,
 * Chunk -> LevelChunk,
 * BlockPos.add -> BlockPos.offset,
 * BlockPos.Mutable.setPos -> BlockPos.MutableBlockPos.set,
 * MathHelper -> Mth,
 * pos.offset(face) -> pos.relative(face),
 * chunk.getTileEntityMap() -> chunk.getBlockEntities(),
 * tile.getPos() -> blockEntity.getBlockPos(),
 * pos.withinDistance -> pos.closerThan</p>
 */
public class BlockDiscoverer {

    @Nonnull
    public static Set<BlockPos> discoverBlocksWithSameStateAroundChain(
            @Nonnull Level level, @Nonnull BlockPos origin, @Nonnull BlockState match,
            int length, @Nullable Direction originalBreakDirection,
            @Nonnull BlockPredicate addCheck) {
        Set<BlockPos> out = new HashSet<>();

        BlockPos offset = new BlockPos(origin);
        lbl:
        while (length > 0) {
            List<Direction> faces = new ArrayList<>();
            Collections.addAll(faces, Direction.values());
            if (originalBreakDirection != null && out.isEmpty()) {
                faces.remove(originalBreakDirection);
                faces.remove(originalBreakDirection.getOpposite());
            }
            Collections.shuffle(faces);
            for (Direction face : faces) {
                BlockPos at = offset.relative(face);
                if (out.contains(at)) {
                    continue;
                }
                BlockState test = level.getBlockState(at);
                if (BlockUtils.matchStateExact(match, test) && addCheck.test(level, at, test)) {
                    out.add(at);
                    length--;
                    offset = at;
                    continue lbl;
                }
            }
            break;
        }

        return out;
    }

    @Nonnull
    public static Set<BlockPos> searchForTileEntitiesAround(
            @Nonnull Level level, @Nonnull BlockPos origin, int distance,
            @Nonnull Predicate<BlockEntity> match) {
        Set<BlockPos> out = new HashSet<>();

        int minChX = (origin.getX() - distance) >> 4;
        int minChZ = (origin.getZ() - distance) >> 4;
        int maxChX = (origin.getX() + distance) >> 4;
        int maxChZ = (origin.getZ() + distance) >> 4;
        for (int chX = minChX; chX <= maxChX; chX++) {
            for (int chZ = minChZ; chZ <= maxChZ; chZ++) {
                LevelChunk ch = level.getChunk(chX, chZ);
                out.addAll(
                        ch.getBlockEntities()
                                .values()
                                .stream()
                                .filter(be -> be.getBlockPos().closerThan(origin, distance))
                                .filter(match)
                                .map(BlockEntity::getBlockPos)
                                .collect(Collectors.toList())
                );
            }
        }

        return out;
    }

    @Nonnull
    public static List<BlockPos> searchForBlocksAround(
            @Nonnull Level level, @Nonnull BlockPos origin, int cubeSize,
            @Nonnull BlockPredicate match) {
        List<BlockPos> out = new ArrayList<>();

        BlockPos.MutableBlockPos offset = new BlockPos.MutableBlockPos();
        for (int xx = -cubeSize; xx <= cubeSize; xx++) {
            for (int zz = -cubeSize; zz <= cubeSize; zz++) {
                for (int yy = -cubeSize; yy <= cubeSize; yy++) {
                    offset.set(origin.getX() + xx, origin.getY() + yy, origin.getZ() + zz);
                    MiscUtils.executeWithChunk(level, offset, () -> {
                        BlockState atState = level.getBlockState(offset);
                        if (match.test(level, offset, atState)) {
                            out.add(new BlockPos(offset));
                        }
                    });
                }
            }
        }
        return out;
    }

    @Nullable
    public static BlockPos searchAreaForFirst(
            @Nonnull Level level, @Nonnull BlockPos center, int radius,
            @Nullable Vector3 offsetFrom, @Nonnull BlockPredicate acceptor) {
        for (int r = 0; r <= radius; r++) {
            Set<BlockPos> posList = new HashSet<>();
            for (int xx = -r; xx <= r; xx++) {
                for (int yy = -r; yy <= r; yy++) {
                    for (int zz = -r; zz <= r; zz++) {
                        BlockPos pos = center.offset(xx, yy, zz);
                        MiscUtils.executeWithChunk(level, pos, () -> {
                            BlockState state = level.getBlockState(pos);
                            if (acceptor.test(level, pos, state)) {
                                posList.add(pos);
                            }
                        });
                    }
                }
            }
            if (!posList.isEmpty()) {
                Vector3 offset = new Vector3(center).add(0.5, 0.5, 0.5);
                if (offsetFrom != null) {
                    offset = offsetFrom;
                }
                BlockPos closest = null;
                double prevDst = 0;
                for (BlockPos pos : posList) {
                    if (closest == null || offset.distance(pos) < prevDst) {
                        closest = pos;
                        prevDst = offset.distance(pos);
                    }
                }
                return closest;
            }
        }
        return null;
    }

    @Nonnull
    public static List<BlockPos> discoverBlocksWithSameStateAround(
            @Nonnull Level level, @Nonnull BlockPos origin, boolean onlyExposed,
            int cubeSize, int limit, boolean searchCorners) {
        List<BlockPos> result = MiscUtils.executeWithChunk(level, origin,
                () -> {
                    BlockState state = level.getBlockState(origin);
                    return discoverBlocksWithSameStateAround(
                            BlockPredicates.isState(state),
                            level, origin, onlyExposed, cubeSize, limit, searchCorners);
                },
                Lists.newArrayList());
        return result != null ? result : Lists.newArrayList();
    }

    @Nonnull
    public static List<BlockPos> discoverBlocksWithSameStateAround(
            @Nonnull BlockPredicate match, @Nonnull Level level, @Nonnull BlockPos origin,
            boolean onlyExposed, int cubeSize, int limit, boolean searchCorners) {
        List<BlockPos> foundResult = new ArrayList<>();
        foundResult.add(origin);
        Set<BlockPos> visited = new HashSet<>();

        Deque<BlockPos> searchNext = new LinkedList<>();
        searchNext.addFirst(origin);

        while (!searchNext.isEmpty()) {
            Deque<BlockPos> currentSearch = searchNext;
            searchNext = new LinkedList<>();

            for (BlockPos offsetPos : currentSearch) {
                if (searchCorners) {
                    for (int xx = -1; xx <= 1; xx++) {
                        for (int yy = -1; yy <= 1; yy++) {
                            for (int zz = -1; zz <= 1; zz++) {
                                BlockPos search = offsetPos.offset(xx, yy, zz);
                                if (visited.contains(search)) continue;
                                if (getCubeDistance(search, origin) > cubeSize) continue;
                                if (limit != -1 && foundResult.size() + 1 > limit) continue;

                                visited.add(search);

                                if (!onlyExposed || isExposedToAir(level, search)) {
                                    Deque<BlockPos> finalSearchNext = searchNext;
                                    MiscUtils.executeWithChunk(level, search, finalSearchNext, searchQueue -> {
                                        if (match.test(level, search, level.getBlockState(search))) {
                                            foundResult.add(search);
                                            searchQueue.add(search);
                                        }
                                    });
                                }
                            }
                        }
                    }
                } else {
                    for (Direction face : Direction.values()) {
                        BlockPos search = offsetPos.relative(face);
                        if (visited.contains(search)) continue;
                        if (getCubeDistance(search, origin) > cubeSize) continue;
                        if (limit != -1 && foundResult.size() + 1 > limit) continue;

                        visited.add(search);

                        if (!onlyExposed || isExposedToAir(level, search)) {
                            Deque<BlockPos> finalSearchNext = searchNext;
                            MiscUtils.executeWithChunk(level, search, finalSearchNext, searchQueue -> {
                                if (match.test(level, search, level.getBlockState(search))) {
                                    foundResult.add(search);
                                    searchQueue.add(search);
                                }
                            });
                        }
                    }
                }
            }
        }

        return foundResult;
    }

    private static int getCubeDistance(@Nonnull BlockPos p1, @Nonnull BlockPos p2) {
        return (int) Mth.absMax(
                Mth.absMax(p1.getX() - p2.getX(), p1.getY() - p2.getY()),
                p1.getZ() - p2.getZ());
    }

    private static boolean isExposedToAir(@Nonnull Level level, @Nonnull BlockPos pos) {
        for (Direction face : Direction.values()) {
            BlockPos offset = pos.relative(face);
            Boolean canReplace = MiscUtils.executeWithChunk(level, offset,
                    () -> BlockUtils.isReplaceable(level, offset), false);
            if (Boolean.TRUE.equals(canReplace)) {
                return true;
            }
        }
        return false;
    }
}
