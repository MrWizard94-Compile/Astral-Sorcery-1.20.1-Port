package hellfirepvp.astralsorcery.common.block.marble;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Black Marble Pillar — auto-selects top/middle/bottom cap variant based on
 * vertical neighbors. Waterloggable.
 *
 * <p>1.16 → 1.20: IWaterLoggable → SimpleWaterloggedBlock;
 * updatePostPlacement → updateShape;
 * world.getPendingFluidTicks().scheduleTick → level.scheduleTick.</p>
 */
public class BlockBlackMarblePillar extends Block implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<PillarType> PILLAR_TYPE = EnumProperty.create("pillartype", PillarType.class);

    private static final VoxelShape SHAPE_MIDDLE = Block.box(2, 0, 2, 14, 16, 14);
    private static final VoxelShape SHAPE_TOP    = Shapes.or(
            Block.box(2, 0, 2, 14, 12, 14),
            Block.box(0, 12, 0, 16, 16, 16));
    private static final VoxelShape SHAPE_BOTTOM = Shapes.or(
            Block.box(2, 4, 2, 14, 16, 14),
            Block.box(0, 0, 0, 16, 4, 16));

    public BlockBlackMarblePillar() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(2.0F, 8.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any()
                .setValue(PILLAR_TYPE, PillarType.MIDDLE)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PILLAR_TYPE, WATERLOGGED);
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return switch (state.getValue(PILLAR_TYPE)) {
            case TOP    -> SHAPE_TOP;
            case BOTTOM -> SHAPE_BOTTOM;
            default     -> SHAPE_MIDDLE;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        FluidState fluid = ctx.getLevel().getFluidState(pos);
        return defaultBlockState()
                .setValue(PILLAR_TYPE, getPillarType(ctx.getLevel(), pos))
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Nonnull
    @Override
    public BlockState updateShape(@Nonnull BlockState state, @Nonnull Direction direction,
                                  @Nonnull BlockState neighborState, @Nonnull LevelAccessor level,
                                  @Nonnull BlockPos pos, @Nonnull BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return state.setValue(PILLAR_TYPE, getPillarType(level, pos));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Nonnull
    @Override
    public FluidState getFluidState(@Nonnull BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    private static PillarType getPillarType(BlockGetter level, BlockPos pos) {
        boolean hasUp   = level.getBlockState(pos.above()).getBlock() instanceof BlockBlackMarblePillar;
        boolean hasDown = level.getBlockState(pos.below()).getBlock() instanceof BlockBlackMarblePillar;
        if (hasUp && hasDown) return PillarType.MIDDLE;
        if (hasUp)            return PillarType.BOTTOM;
        if (hasDown)          return PillarType.TOP;
        return PillarType.MIDDLE;
    }

    public enum PillarType implements StringRepresentable {
        TOP, MIDDLE, BOTTOM;

        @Nonnull
        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
