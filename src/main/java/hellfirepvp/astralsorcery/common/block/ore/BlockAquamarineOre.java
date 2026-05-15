package hellfirepvp.astralsorcery.common.block.ore;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Aquamarine Ore (Sand) — found in riverbeds, drops aquamarine shale.
 * Uses sand-type properties with gravel sound.
 *
 * <p>1.16 -> 1.20 changes:
 * Material.SAND removed — use MapColor + sand sound type.</p>
 */
public class BlockAquamarineOre extends BlockAS {

    public BlockAquamarineOre() {
        super(Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.5F, 0.5F)
                .sound(SoundType.SAND));
    }
}
