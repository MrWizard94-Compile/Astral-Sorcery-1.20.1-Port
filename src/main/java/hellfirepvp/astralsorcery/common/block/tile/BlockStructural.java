package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * Structural block used internally by multiblock structures (e.g. the telescope upper portion).
 * Not obtainable in survival. Uses a BlockType enum state to drive per-structure behavior.
 */
public class BlockStructural extends BlockAS {

    public static final EnumProperty<BlockType> BLOCK_TYPE = EnumProperty.create("blocktype", BlockType.class);

    private static final VoxelShape SHAPE_TELESCOPE = Block.box(1, -16, 1, 15, 16, 15);

    public BlockStructural() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(-1.0F, 6_000_000.0F)
                .sound(SoundType.GLASS));
        registerDefaultState(stateDefinition.any().setValue(BLOCK_TYPE, BlockType.TELESCOPE));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BLOCK_TYPE);
    }

    @Nonnull
    @Override
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos,
                               @Nonnull CollisionContext context) {
        return switch (state.getValue(BLOCK_TYPE)) {
            case TELESCOPE -> SHAPE_TELESCOPE;
            default -> Shapes.block();
        };
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                                @Nonnull Block block, @Nonnull BlockPos fromPos, boolean isMoving) {
        if (state.getValue(BLOCK_TYPE) == BlockType.TELESCOPE && level.isEmptyBlock(pos.below())) {
            level.removeBlock(pos, isMoving);
        }
    }

    public enum BlockType implements StringRepresentable {
        DUMMY,
        TELESCOPE;

        @Nonnull
        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
