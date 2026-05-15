package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nonnull;

/**
 * Black Marble — variant decorative stone found in structures.
 * Shares physical properties with regular marble but uses a different color.
 *
 * <p>1.16 -> 1.20 changes: Material.ROCK removed — MapColor direct usage.</p>
 */
public class BlackMarble extends BlockAS {

    public BlackMarble() {
        super(defaultProperties());
    }

    @Nonnull
    public static Properties defaultProperties() {
        return Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(2.0F, 8.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }
}
