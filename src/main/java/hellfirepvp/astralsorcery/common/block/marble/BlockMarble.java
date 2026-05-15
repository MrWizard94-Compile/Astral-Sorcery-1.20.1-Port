package hellfirepvp.astralsorcery.common.block.marble;

import hellfirepvp.astralsorcery.common.block.base.BlockAS;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nonnull;

/**
 * Sooty Marble — the base decorative stone generated in the world.
 * All marble variants share the same base properties.
 *
 * <p>1.16 -> 1.20 changes:
 * Material.ROCK removed — use MapColor directly + properties builder,
 * hardnessAndResistance -> strength,
 * setRequiresTool -> requiresCorrectToolForDrops</p>
 */
public class BlockMarble extends BlockAS {

    public BlockMarble() {
        this(defaultProperties());
    }

    public BlockMarble(@Nonnull Properties properties) {
        super(properties);
    }

    @Nonnull
    public static Properties defaultProperties() {
        return Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(1.0F, 5.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }
}
