package hellfirepvp.astralsorcery.common.block.marble;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Marble Pillar — directional log-style block (rotates on placement).
 *
 * <p>1.16 → 1.20: RotatedPillarBlock API stable.
 * Material.ROCK → MapColor.QUARTZ + properties builder.</p>
 */
public class BlockMarblePillar extends RotatedPillarBlock {

    public BlockMarblePillar() {
        super(Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(1.0F, 5.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }
}
