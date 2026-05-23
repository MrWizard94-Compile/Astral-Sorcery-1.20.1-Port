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
 * Spectral relay — transmits starlight through it to distant collector crystals.
 * Crafted at the altar (Attunement tier). Full TE logic (TileSpectralRelay) deferred.
 */
public class BlockSpectralRelay extends BlockAS {

    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 8, 12);

    public BlockSpectralRelay() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(2.0F, 6.0F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .lightLevel(state -> 4)
                .requiresCorrectToolForDrops());
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos,
                               @Nonnull CollisionContext context) {
        return SHAPE;
    }
}
