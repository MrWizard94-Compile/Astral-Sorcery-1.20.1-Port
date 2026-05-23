package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

/**
 * Ritual link — a node that extends the area-of-effect for active rituals.
 * Crafted at the altar (Attunement tier). Full TE logic (TileRitualLink) deferred.
 */
public class BlockRitualLink extends BlockAS {

    private static final VoxelShape SHAPE = Block.box(6, 2, 6, 10, 14, 10);

    public BlockRitualLink() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(1.5F, 6.0F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos,
                               @Nonnull CollisionContext context) {
        return SHAPE;
    }
}
