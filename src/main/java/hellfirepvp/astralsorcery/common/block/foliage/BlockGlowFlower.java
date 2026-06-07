package hellfirepvp.astralsorcery.common.block.foliage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

/**
 * Glow Flower — a faintly luminous flower found in low-light biomes.
 * Emits a small amount of light and can be used in liquid starlight crafting.
 *
 * <p>1.16 -> 1.20 changes: BushBlock API stable;
 * IWorldReader -> LevelReader in canSurvive (default inherited from BushBlock).</p>
 */
public class BlockGlowFlower extends BushBlock {

    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 10, 11);

    public BlockGlowFlower() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(0.0F)
                .sound(SoundType.GRASS)
                .noCollission()
                .lightLevel(state -> 5));
    }

    @Override
    @Nonnull

    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return SHAPE;
    }
}
