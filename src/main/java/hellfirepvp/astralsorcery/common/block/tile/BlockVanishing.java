package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * Vanishing block — a barrier-like block placed by the vanishing system.
 * Not obtainable in survival. Full TE logic (TileVanishing) deferred to later phase.
 */
public class BlockVanishing extends BlockAS {

    public BlockVanishing() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(-1.0F, 3_600_000.0F)
                .sound(SoundType.METAL)
                .noOcclusion());
    }
}
