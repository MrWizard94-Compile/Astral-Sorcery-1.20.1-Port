package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntitySpectralRelay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Spectral relay — transmits starlight to nearby altars when placed on its marble platform
 * and loaded with a Glass Lens.
 *
 * <p>1.16 → 1.20: now implements EntityBlock via BlockEntityBlock.
 * Multiblock validation done inline in BlockEntitySpectralRelay.</p>
 */
public class BlockSpectralRelay extends BlockEntityBlock {

    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 8, 12);

    public BlockSpectralRelay() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(2.0F, 6.0F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .lightLevel(state -> 4)
                .requiresCorrectToolForDrops());
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos,
                               @Nonnull CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level,
                         @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntitySpectralRelay relay) {
                ItemStack lens = relay.getInventory().getStackInSlot(0);
                if (!lens.isEmpty()) {
                    Block.popResource(level, pos, lens);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntitySpectralRelay(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.SPECTRAL_RELAY.get());
    }
}
