package hellfirepvp.astralsorcery.common.block.ore;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Starmetal Block — compact storage of starmetal ingots.
 *
 * <p>1.16 -> 1.20 changes: Material.IRON/defaultMetal replaced
 * by MapColor + requiresCorrectToolForDrops.</p>
 */
public class BlockStarmetal extends BlockAS {

    public BlockStarmetal() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BLUE)
                .strength(5.0F, 20.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
    }
}
