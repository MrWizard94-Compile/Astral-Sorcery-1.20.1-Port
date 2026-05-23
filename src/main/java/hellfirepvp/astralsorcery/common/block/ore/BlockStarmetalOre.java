package hellfirepvp.astralsorcery.common.block.ore;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Starmetal Ore — mid-game ore found by transmuting iron ore under starlight.
 * Requires diamond-tier pickaxe to harvest.
 *
 * <p>1.16 -> 1.20 changes: Material.ROCK removed, harvestLevel/harvestTool
 * replaced by requiresCorrectToolForDrops + tags.</p>
 */
public class BlockStarmetalOre extends BlockAS {

    public BlockStarmetalOre() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0F, 15.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }
}
