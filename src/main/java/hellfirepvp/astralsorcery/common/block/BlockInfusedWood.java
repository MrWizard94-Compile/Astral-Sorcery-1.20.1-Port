package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nonnull;

/**
 * Infused Wood — wood infused with starlight, used in multiblock structures.
 * Has column, arch, engraved, enriched, and planks variants.
 *
 * <p>1.16 -> 1.20 changes: Material.WOOD removed.</p>
 */
public class BlockInfusedWood extends BlockAS {

    public BlockInfusedWood() {
        this(defaultProperties());
    }

    public BlockInfusedWood(@Nonnull Properties properties) {
        super(properties);
    }

    @Nonnull
    public static Properties defaultProperties() {
        return Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.5F, 4.0F)
                .sound(SoundType.WOOD);
    }
}
