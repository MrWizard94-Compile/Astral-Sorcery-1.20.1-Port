package hellfirepvp.astralsorcery.common.block.marble;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Marble Stairs — standard stair variant with marble properties.
 *
 * <p>1.16 -> 1.20 changes:
 * StairsBlock -> StairBlock (class rename),
 * Constructor now takes Supplier&lt;BlockState&gt; instead of BlockState directly,
 * Material removed.</p>
 */
public class BlockMarbleStairs extends StairBlock {

    public BlockMarbleStairs(@Nonnull Supplier<BlockState> baseState) {
        super(baseState, Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(1.0F, 5.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }
}
