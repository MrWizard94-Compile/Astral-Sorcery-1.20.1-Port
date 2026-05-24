package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockEntityBlock;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityGemCrystals;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Gem crystal cluster — grows in three stages (DAY/NIGHT/SKY) influenced by perk gem type.
 * Spawns during world gen.
 */
public class BlockGemCrystalCluster extends BlockEntityBlock {

    public static final EnumProperty<GrowthStageType> STAGE = EnumProperty.create("stage", GrowthStageType.class);

    private static final VoxelShape SHAPE_0     = Block.box(4, 0, 4, 12,  6, 12);
    private static final VoxelShape SHAPE_1     = Block.box(4, 0, 4, 12,  8, 12);
    private static final VoxelShape SHAPE_2_SKY = Block.box(5, 0, 5, 11, 10, 11);

    public BlockGemCrystalCluster() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
                .strength(3.0F, 3.0F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any().setValue(STAGE, GrowthStageType.DAY_0));
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
            case DAY_0, NIGHT_0, SKY_0 -> SHAPE_0;
            case DAY_1, NIGHT_1, SKY_1 -> SHAPE_1;
            default -> SHAPE_2_SKY;
        };
    }

    @Nonnull
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new BlockEntityGemCrystals(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level,
                                                                   @Nonnull BlockState state,
                                                                   @Nonnull BlockEntityType<T> type) {
        return createTicker(type, BlockEntityTypesAS.GEM_CRYSTAL_CLUSTER.get());
    }

    public enum GrowthStageType implements StringRepresentable {
        DAY_0, DAY_1, DAY_2,
        NIGHT_0, NIGHT_1, NIGHT_2,
        SKY_0, SKY_1, SKY_2;

        @Nonnull
        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
