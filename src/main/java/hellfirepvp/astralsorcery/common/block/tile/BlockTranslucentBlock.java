package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Translucent block — displays a faked block state via a block entity, used by the
 * vanishing/illusion effect system. Full TE logic (TileTranslucentBlock) deferred.
 */
public class BlockTranslucentBlock extends BlockAS {

    public BlockTranslucentBlock() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(-1.0F, 6_000_000.0F)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 12)
                .noOcclusion());
    }
}
