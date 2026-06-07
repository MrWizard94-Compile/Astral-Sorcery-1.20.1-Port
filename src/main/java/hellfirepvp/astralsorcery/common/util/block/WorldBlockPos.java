package hellfirepvp.astralsorcery.common.util.block;

import hellfirepvp.astralsorcery.common.util.object.TransformReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

/**
 * A BlockPos that also knows which dimension (Level) it belongs to.
 * Used for cross-dimensional position tracking (gateways, starlight network).
 *
 * <p>1.16 → 1.20 changes:
 * World → Level, getDimensionKey() → dimension(),
 * Vector3i → Vec3i, TileEntity → BlockEntity,
 * LogicalSidedProvider.INSTANCE.get() → ServerLifecycleHooks.getCurrentServer()</p>
 */
public class WorldBlockPos extends BlockPos {

    private final TransformReference<ResourceKey<Level>, Level> worldReference;

    private WorldBlockPos(@Nonnull TransformReference<ResourceKey<Level>, Level> worldReference,
                           @Nonnull BlockPos pos) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.worldReference = worldReference;
    }

    private WorldBlockPos(@Nonnull ResourceKey<Level> type, @Nonnull BlockPos pos,
                           @Nonnull Function<ResourceKey<Level>, Level> worldProvider) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.worldReference = new TransformReference<>(type, worldProvider);
    }

    @Nonnull
    public static WorldBlockPos wrapServer(@Nonnull Level level, @Nonnull BlockPos pos) {
        return new WorldBlockPos(level.dimension(), pos, type -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            return server != null ? server.getLevel(type) : null;
        });
    }

    @Nonnull
    public static WorldBlockPos wrapBlockEntity(@Nonnull BlockEntity be) {
        Level level = be.getLevel();
        ResourceKey<Level> dim = level != null ? level.dimension() : Level.OVERWORLD;
        return new WorldBlockPos(dim, be.getBlockPos(), type -> be.getLevel());
    }

    @Nonnull
    public ResourceKey<Level> getWorldKey() {
        return this.worldReference.getReference();
    }

    @Nonnull
    private WorldBlockPos wrapInternal(@Nonnull BlockPos pos) {
        return new WorldBlockPos(this.worldReference, pos);
    }

    @Override
    @Nonnull
    public WorldBlockPos offset(int x, int y, int z) {
        return wrapInternal(super.offset(x, y, z));
    }

    @Override
    @Nonnull
    public WorldBlockPos offset(@Nonnull Vec3i vec) {
        return wrapInternal(super.offset(vec));
    }

    @Nullable
    public Level getWorld() {
        return this.worldReference.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WorldBlockPos that = (WorldBlockPos) o;
        return Objects.equals(getWorldKey(), that.getWorldKey());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getWorldKey().hashCode();
        return result;
    }
}
