package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.item.lens.ItemColoredLens;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityPrism;
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
 * Prism — splits and redirects starlight beams in the transmission network.
 * Has a configurable color filter via inserted colored lenses.
 *
 * <p>1.16 -> 1.20 changes: Same as BlockLens.</p>
 */
public class BlockPrism extends BlockEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape SHAPE = Block.box(3, 3, 3, 13, 13, 13);

    public BlockPrism() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(1.0F, 5.0F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .lightLevel(state -> 4));
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
        return new BlockEntityPrism(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.PRISM.get());
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                                 @Nonnull Player player, @Nonnull InteractionHand hand,
                                 @Nonnull BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BlockEntityPrism prism)) return InteractionResult.PASS;

        ItemStack inHand = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            // Shift-click: retrieve crystal first, then lens
            ItemStack crystal = prism.getHeldCrystal();
            if (!crystal.isEmpty()) {
                prism.setHeldCrystal(ItemStack.EMPTY);
                if (!player.addItem(crystal)) Block.popResource(level, pos, crystal);
                return InteractionResult.CONSUME;
            }
            ItemStack lens = prism.getInsertedLens();
            if (lens != null && !lens.isEmpty()) {
                prism.setInsertedLens(null);
                if (!player.addItem(lens)) Block.popResource(level, pos, lens);
                return InteractionResult.CONSUME;
            }
        } else if (inHand.getItem() instanceof ItemCrystalBase && prism.getHeldCrystal().isEmpty()) {
            prism.setHeldCrystal(inHand.copyWithCount(1));
            if (!player.isCreative()) inHand.shrink(1);
            return InteractionResult.CONSUME;
        } else if (inHand.getItem() instanceof ItemColoredLens && !prism.hasInsertedLens()) {
            prism.setInsertedLens(inHand.copyWithCount(1));
            if (!player.isCreative()) inHand.shrink(1);
            return InteractionResult.CONSUME;
        } else if (inHand.isEmpty()) {
            ItemStack crystal = prism.getHeldCrystal();
            if (!crystal.isEmpty()) {
                prism.setHeldCrystal(ItemStack.EMPTY);
                player.setItemInHand(hand, crystal);
                return InteractionResult.CONSUME;
            }
            ItemStack lens = prism.getInsertedLens();
            if (lens != null && !lens.isEmpty()) {
                prism.setInsertedLens(null);
                player.setItemInHand(hand, lens);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                         @Nonnull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlockEntityPrism prism) {
                ItemStack crystal = prism.getHeldCrystal();
                if (!crystal.isEmpty()) Block.popResource(level, pos, crystal);
                ItemStack lens = prism.getInsertedLens();
                if (lens != null && !lens.isEmpty()) Block.popResource(level, pos, lens);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
