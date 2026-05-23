package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Tree beacon component — the internal structural block that the tree beacon multiblock
 * places inside the tree canopy. Not obtainable in survival.
 * Full TE logic (TileTreeBeaconComponent) deferred to later phase.
 */
public class BlockTreeBeaconComponent extends BlockAS {

    public BlockTreeBeaconComponent() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(-1.0F, 3_600_000.0F)
                .sound(SoundType.GRASS)
                .lightLevel(state -> 12)
                .noOcclusion());
    }
}
