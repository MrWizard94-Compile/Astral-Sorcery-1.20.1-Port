package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Refraction table — used to apply constellation-based enhancements to items via parchment.
 * Crafted at the altar (Constellation tier). Full TE logic (TileRefractionTable) deferred.
 */
public class BlockRefractionTable extends BlockAS {

    public BlockRefractionTable() {
        super(Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0F, 6.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }
}
