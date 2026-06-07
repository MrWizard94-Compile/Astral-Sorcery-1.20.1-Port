package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityTreeBeacon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Tree Beacon — a nature-infused starlight collector that passively
 * absorbs starlight to accelerate nearby plant growth. Emits a tall
 * vertical light beam when charged.
 *
 * <p>Placed atop trees or in open areas with sky access for best
 * starlight collection. The growth boost effect has a 16-block
 * horizontal radius.</p>
 *
 * <p>1.16 → 1.20 changes: EntityBlock interface for BE creation.</p>
 */
public class BlockTreeBeacon extends BlockEntityBlock {

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 14, 14);

    public BlockTreeBeacon() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_GREEN)
                .strength(1.5F, 5.0F)
                .sound(SoundType.WOOD)
                .lightLevel(state -> 7)
                .noOcclusion());
    }

    @Nonnull
    @Override

    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityTreeBeacon(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.TREE_BEACON.get());
    }
}
