package hellfirepvp.astralsorcery.common.util.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.BlockSnapshot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Block entity access and chunk-tick utilities.
 */
public class TileUtils {

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T getTileAt(@Nullable net.minecraft.world.level.BlockGetter world,
                                  @Nullable BlockPos pos,
                                  @Nonnull Class<T> tileClass,
                                  boolean forceChunkLoad) {
        if (world == null || pos == null) return null;
        if (world instanceof LevelAccessor levelAccessor) {
            if (!levelAccessor.getChunkSource().hasChunk(
                    pos.getX() >> 4, pos.getZ() >> 4) && !forceChunkLoad) {
                return null;
            }
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (te == null) return null;
        if (tileClass.isInstance(te)) return (T) te;
        return null;
    }

    public static boolean canEntityTickAt(@Nonnull LevelAccessor world, @Nonnull BlockPos pos) {
        if (!world.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return false;
        }
        if (world.isClientSide() || !(world instanceof ServerLevel serverLevel)) {
            return true;
        }
        return serverLevel.isPositionEntityTicking(pos);
    }

    @Nonnull
    public static List<BlockSnapshot> captureBlockChanges(@Nonnull Level level,
                                                          @Nonnull Runnable r) {
        level.captureBlockSnapshots = true;
        r.run();
        level.captureBlockSnapshots = false;
        @SuppressWarnings("unchecked")
        List<BlockSnapshot> blockSnapshots =
                (List<BlockSnapshot>) level.capturedBlockSnapshots.clone();
        level.capturedBlockSnapshots.clear();
        return blockSnapshots;
    }
}
