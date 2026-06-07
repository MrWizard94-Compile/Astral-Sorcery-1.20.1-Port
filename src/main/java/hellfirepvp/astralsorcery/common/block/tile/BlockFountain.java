package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.block.base.LiquidStarlightOwned;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityFountain;
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
 * Fountain — consumes liquid starlight for various area effects.
 * The effect depends on the fountain prime item inserted.
 *
 * <p>1.16 -> 1.20 changes: Same as other tile blocks.</p>
 */
public class BlockFountain extends BlockEntityBlock implements LiquidStarlightOwned {

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 15, 15);

    public BlockFountain() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(3.0F, 15.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(state -> 9));
    }

    @Override
    public int getMaxLiquidStarlight() {
        return 16000;
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
        return new BlockEntityFountain(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.FOUNTAIN.get());
    }
}
