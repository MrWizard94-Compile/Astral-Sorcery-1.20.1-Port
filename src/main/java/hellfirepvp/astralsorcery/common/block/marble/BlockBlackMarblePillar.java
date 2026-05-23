package hellfirepvp.astralsorcery.common.block.marble;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Black Marble Pillar — directional log-style block (rotates on placement).
 *
 * <p>1.16 -> 1.20 changes: RotatedPillarBlock API stable.</p>
 */
public class BlockBlackMarblePillar extends RotatedPillarBlock {

    public BlockBlackMarblePillar() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(2.0F, 8.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }
}
