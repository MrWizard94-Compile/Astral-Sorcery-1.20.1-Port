package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityLens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Lens — part of the starlight transmission network.
 * Redirects starlight beams. Has a FACING property for orientation.
 *
 * <p>1.16 -> 1.20 changes:
 * DirectionProperty / BlockStateProperties stable.
 * getStateForPlacement uses BlockPlaceContext (renamed from BlockItemUseContext).</p>
 */
public class BlockLens extends BlockEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape SHAPE = Block.box(4, 4, 4, 12, 12, 12);

    public BlockLens() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(1.0F, 5.0F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .lightLevel(state -> 3));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityLens(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.LENS.get());
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                                 @Nonnull Player player, @Nonnull InteractionHand hand,
                                 @Nonnull BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BlockEntityLens lens)) return InteractionResult.PASS;

        ItemStack inHand = player.getItemInHand(hand);
        ItemStack onLens = lens.getHeldCrystal();

        if (player.isShiftKeyDown()) {
            if (!onLens.isEmpty()) {
                lens.setHeldCrystal(ItemStack.EMPTY);
                if (!player.addItem(onLens)) {
                    Block.popResource(level, pos, onLens);
                }
                return InteractionResult.CONSUME;
            }
        } else if (inHand.getItem() instanceof ItemCrystalBase && onLens.isEmpty()) {
            lens.setHeldCrystal(inHand.copyWithCount(1));
            if (!player.isCreative()) inHand.shrink(1);
            return InteractionResult.CONSUME;
        } else if (inHand.isEmpty() && !onLens.isEmpty()) {
            lens.setHeldCrystal(ItemStack.EMPTY);
            player.setItemInHand(hand, onLens);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                         @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityLens lens) {
                ItemStack crystal = lens.getHeldCrystal();
                if (!crystal.isEmpty()) {
                    Block.popResource(level, pos, crystal);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
