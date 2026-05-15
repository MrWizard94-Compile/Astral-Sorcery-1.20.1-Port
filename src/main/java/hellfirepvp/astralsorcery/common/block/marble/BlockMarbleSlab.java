package hellfirepvp.astralsorcery.common.block.marble;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Marble Slab — standard half-slab variant.
 *
 * <p>1.16 -> 1.20 changes: SlabBlock API stable. Material removed.</p>
 */
public class BlockMarbleSlab extends SlabBlock {

    public BlockMarbleSlab() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(1.0F, 5.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }
}
