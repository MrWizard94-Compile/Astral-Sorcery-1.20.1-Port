package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityCelestialCrystals;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Celestial crystal cluster — grows in stages from 0 (sprout) to 4 (full).
 * Spawns during world gen in starlight-rich cave environments.
 */
public class BlockCelestialCrystalCluster extends BlockEntityBlock {

    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 4);

    private static final VoxelShape SHAPE_0 = Block.box(4, 0, 5, 12,  8, 11);
    private static final VoxelShape SHAPE_1 = Block.box(4, 0, 5, 12, 10, 11);
    private static final VoxelShape SHAPE_2 = Block.box(2, 0, 4, 12, 12, 14);
    private static final VoxelShape SHAPE_3 = Block.box(2, 0, 2, 14, 14, 14);
    private static final VoxelShape SHAPE_4 = Block.box(2, 0, 2, 14, 16, 14);

    public BlockCelestialCrystalCluster() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(3.0F, 3.0F)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 8)
                .noOcclusion()
                .requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos,
                               @Nonnull CollisionContext context) {
        return switch (state.getValue(STAGE)) {
            case 0 -> SHAPE_0;
            case 1 -> SHAPE_1;
            case 2 -> SHAPE_2;
            case 3 -> SHAPE_3;
            default -> SHAPE_4;
        };
    }

    @Nonnull
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityCelestialCrystals(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.CELESTIAL_CRYSTAL_CLUSTER.get());
    }
}
