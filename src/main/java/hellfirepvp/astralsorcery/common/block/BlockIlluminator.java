package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

/**
 * Illuminator — invisible light source block placed by Illumination Powder.
 * No collision, no visual model, full light emission.
 *
 * <p>1.16 -> 1.20 changes:
 * Material.AIR replaced by properties config,
 * getRenderType -> getRenderShape,
 * RenderType -> RenderShape</p>
 */
public class BlockIlluminator extends BlockAS {

    public BlockIlluminator() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .noCollission()
                .noOcclusion()
                .instabreak()
                .sound(SoundType.WOOL)
                .lightLevel(state -> 15)
                .replaceable());
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level,
                               @Nonnull BlockPos pos, @Nonnull CollisionContext ctx) {
        return Shapes.empty();
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
