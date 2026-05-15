package hellfirepvp.astralsorcery.common.tile.base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Base class for tickable block entities.
 * In 1.16 this implemented ITickableTileEntity; in 1.20 the block class
 * must provide a {@code BlockEntityTicker} via its {@code getTicker()} method.
 *
 * <p>1.16 -> 1.20 changes:
 * ITickableTileEntity.tick() -> static ticker method via Block#getTicker.
 * The actual tick logic lives in {@link #tick()} which is called by the
 * static ticker function created by the block class.</p>
 *
 * <p>Usage in Block class:
 * <pre>{@code
 * @Override
 * public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
 *     return createTickerHelper(type, MY_TYPE.get(), BlockEntityTick::tickStatic);
 * }
 * }</pre>
 * </p>
 */
public abstract class BlockEntityTick extends BlockEntitySynchronized {

    private boolean firstTick = true;

    protected BlockEntityTick(@Nonnull BlockEntityType<?> type, @Nonnull BlockPos pos,
                              @Nonnull BlockState state) {
        super(type, pos, state);
    }

    /**
     * Static ticker function to be used with Block#getTicker via
     * {@code createTickerHelper(type, expected, BlockEntityTick::tickStatic)}.
     */
    public static void tickStatic(@Nonnull Level level, @Nonnull BlockPos pos,
                                  @Nonnull BlockState state, @Nonnull BlockEntityTick be) {
        be.tick();
    }

    /**
     * Called every game tick. Override in subclasses for per-tick logic.
     * Replaces 1.16's ITickableTileEntity#tick().
     */
    public void tick() {
        if (firstTick) {
            firstTick = false;
            onFirstTick();
        }
    }

    /**
     * Called once on the first tick after the block entity is loaded.
     * Useful for deferred initialization that requires a fully loaded level.
     */
    protected void onFirstTick() {}

    /**
     * Whether this block entity ticks on the client side.
     * Override and return false for server-only BEs.
     */
    public boolean ticksOnClient() {
        return true;
    }

    /**
     * Whether this block entity ticks on the server side.
     * Override and return false for client-only BEs (rare).
     */
    public boolean ticksOnServer() {
        return true;
    }

    /**
     * Convenience: check if currently on client side.
     */
    protected boolean isClientSide() {
        return getLevel() != null && getLevel().isClientSide();
    }
}
