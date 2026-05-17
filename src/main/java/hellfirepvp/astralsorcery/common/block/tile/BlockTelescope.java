package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityTelescope;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
 * Telescope — used for constellation discovery. Opens the telescope GUI
 * to allow tracking constellations in the night sky.
 *
 * <p>1.16 -> 1.20 changes:
 * HORIZONTAL_FACING property stable.
 * getStateForPlacement renamed from getStateForPlacement.</p>
 */
public class BlockTelescope extends BlockEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public BlockTelescope() {
        super(Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.5F, 5.0F)
                .sound(SoundType.WOOD)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
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
            openTelescopeClient();
            return InteractionResult.SUCCESS;
        }
        // Server-side: mark telescope as used
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlockEntityTelescope telescope) {
            telescope.setUsed();
        }
        return InteractionResult.CONSUME;
    }

    /**
     * Opens the telescope screen client-side via dist-safe helper.
     * Isolated to avoid classloading client classes on the server.
     */
    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    private static void openTelescopeClient() {
        hellfirepvp.astralsorcery.client.screen.ClientScreenHandler.openTelescopeScreen();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityTelescope(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return null; // Telescope doesn't tick
    }
}
