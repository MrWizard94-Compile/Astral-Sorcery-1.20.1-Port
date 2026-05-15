package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.block.base.LiquidStarlightOwned;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityWell;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Lightwell — converts rock crystals into liquid starlight over time.
 * Uses a PrecisionSingleFluidTank for sub-mB drip mechanics.
 *
 * <p>1.16 -> 1.20 changes:
 * Material removed, VoxelShapes.or -> Shapes.or,
 * ActionResultType -> InteractionResult</p>
 */
public class BlockWell extends BlockEntityBlock implements LiquidStarlightOwned {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),   // Base
            Block.box(0, 0, 0, 2, 14, 2),    // Corner pillar NW
            Block.box(14, 0, 0, 16, 14, 2),  // Corner pillar NE
            Block.box(0, 0, 14, 2, 14, 16),  // Corner pillar SW
            Block.box(14, 0, 14, 16, 14, 16),// Corner pillar SE
            Block.box(0, 1, 0, 16, 3, 16)    // Basin
    );

    public BlockWell() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(3.0F, 15.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    @Override
    public int getMaxLiquidStarlight() {
        return 2000; // 2 buckets
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return SHAPE;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level,
                                 @Nonnull BlockPos pos, @Nonnull Player player,
                                 @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        // TODO: Handle crystal insertion and fluid interaction
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityWell(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.WELL.get());
    }
}
