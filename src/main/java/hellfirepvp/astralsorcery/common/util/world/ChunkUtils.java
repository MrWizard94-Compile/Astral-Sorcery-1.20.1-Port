package hellfirepvp.astralsorcery.common.util.world;

import hellfirepvp.astralsorcery.common.util.FunctionUtils;
import hellfirepvp.astralsorcery.common.util.log.LogCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Chunk-safe execution utilities: run code only when the target chunk is already loaded,
 * with optional debug logging for unintended chunk loads.
 */
public class ChunkUtils {

    private static final Logger LOG = LogManager.getLogger();

    public static void executeWithChunk(@Nonnull LevelReader world,
                                        @Nonnull ChunkPos pos,
                                        @Nonnull Runnable run) {
        executeWithChunk(world, pos.getWorldPosition(),
                FunctionUtils.nullSupplier(run));
    }

    public static void executeWithChunk(@Nonnull LevelReader world,
                                        @Nonnull BlockPos pos,
                                        @Nonnull Runnable run) {
        executeWithChunk(world, pos, FunctionUtils.nullSupplier(run));
    }

    @Nullable
    public static <T> T executeWithChunk(@Nonnull LevelReader world,
                                         @Nonnull BlockPos pos,
                                         @Nonnull Supplier<T> run) {
        return executeWithChunkDefault(world, pos, run, null);
    }

    @Nullable
    public static <T> T executeWithChunk(@Nonnull LevelReader world,
                                         @Nonnull BlockPos pos,
                                         @Nonnull Supplier<T> run,
                                         @Nullable T defaultValue) {
        return executeWithChunkDefault(world, pos, run, defaultValue);
    }

    public static <T> void executeWithChunk(@Nonnull LevelReader world,
                                            @Nonnull BlockPos pos,
                                            @Nonnull T obj,
                                            @Nonnull Consumer<T> run) {
        executeWithChunk(world, pos,
                FunctionUtils.nullSupplier(FunctionUtils.apply(run, () -> obj)));
    }

    public static <T, U> void executeWithChunk(@Nonnull LevelReader world,
                                               @Nonnull BlockPos pos,
                                               @Nonnull T obj,
                                               @Nonnull U obj1,
                                               @Nonnull BiConsumer<T, U> run) {
        executeWithChunk(world, pos, obj, FunctionUtils.apply(run, () -> obj1));
    }

    @Nullable
    public static <T, R> R executeWithChunk(@Nonnull LevelReader world,
                                            @Nonnull BlockPos pos,
                                            @Nonnull T obj,
                                            @Nonnull Function<T, R> run) {
        return executeWithChunk(world, pos, FunctionUtils.apply(run, () -> obj));
    }

    @Nullable
    public static <T, R> R executeWithChunk(@Nonnull LevelReader world,
                                            @Nonnull BlockPos pos,
                                            @Nonnull T obj,
                                            @Nonnull Function<T, R> run,
                                            @Nullable R defaultValue) {
        return executeWithChunk(world, pos,
                FunctionUtils.apply(run, () -> obj), defaultValue);
    }

    @Nonnull
    public static <T> Function<T, T> mapWithChunk(@Nonnull LevelReader world,
                                                  @Nonnull Function<T, BlockPos> posFn) {
        return (val) -> executeWithChunk(world, posFn.apply(val), val, Function.identity());
    }

    /**
     * Internal implementation — separated to avoid overload ambiguity between
     * (Supplier, null) and (Function, null) when passing a null default value.
     */
    @Nullable
    private static <T> T executeWithChunkDefault(@Nonnull LevelReader world,
                                                 @Nonnull BlockPos pos,
                                                 @Nonnull Supplier<T> run,
                                                 @Nullable T defaultValue) {
        if (world instanceof ServerLevel serverLevel
                && LogCategory.UNINTENDED_CHUNK_LOADING.isEnabled()) {
            ServerChunkCache provider = serverLevel.getChunkSource();
            int prev = provider.getLoadedChunksCount();
            try {
                if (provider.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                    return run.get();
                }
            } finally {
                int current = serverLevel.getChunkSource().getLoadedChunksCount();
                if (current > prev) {
                    LOG.warn("Astral Sorcery loaded a chunk when it intended not to!");
                    LOG.warn("Previous chunk count: {}", prev);
                    LOG.warn("Current chunk count: {}", current);
                    LOG.warn("Loaded {} chunks!", current - prev);
                    LOG.warn("Stacktrace:", new Exception());
                }
            }
        } else if (world instanceof LevelAccessor levelAccessor) {
            ChunkSource provider = levelAccessor.getChunkSource();
            if (provider.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                return run.get();
            }
        } else {
            if (world.getChunk(pos.getX() >> 4, pos.getZ() >> 4,
                    ChunkStatus.FULL, false) != null) {
                return run.get();
            }
        }
        return defaultValue;
    }
}
