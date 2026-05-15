package hellfirepvp.astralsorcery.common.block.base;

import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base class for Astral Sorcery blocks that have a block entity.
 * Implements the 1.20 {@link EntityBlock} interface and provides
 * standard ticker wiring for tickable block entities.
 *
 * <p>1.16 -> 1.20 changes:
 * Blocks with TileEntities now implement EntityBlock interface (not hasTileEntity override).
 * ITickableTileEntity is gone - ticking is handled via BlockEntityTicker returned from getTicker().
 * The block must override newBlockEntity() to create the BE instance.
 * createTicker() must provide the static tick function.</p>
 */
public abstract class BlockEntityBlock extends BlockAS implements EntityBlock {

    protected BlockEntityBlock(@Nonnull Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state);

    /**
     * Standard ticker helper — call from your block's {@code getTicker()} to wire
     * a tickable block entity. Returns null if the BE type doesn't match (safe for
     * mismatched type checks).
     *
     * <p>Example usage in subclass:
     * <pre>{@code
     * @Nullable @Override
     * public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
     *     return createTicker(type, MY_BE_TYPE.get());
     * }
     * }</pre>
     * </p>
     */
    @Nullable
    protected <T extends BlockEntity> BlockEntityTicker<T> createTicker(
            @Nonnull BlockEntityType<T> givenType,
            @Nonnull BlockEntityType<? extends BlockEntityTick> expectedType) {
        return createTickerHelper(givenType, expectedType, BlockEntityTick::tickStatic);
    }

    /**
     * Type-safe ticker creation helper. Borrowed from BaseEntityBlock pattern.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            @Nonnull BlockEntityType<A> givenType,
            @Nonnull BlockEntityType<E> expectedType,
            @Nonnull BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }
}
