package hellfirepvp.astralsorcery.common.block.tile.fountain;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import javax.annotation.Nonnull;

/**
 * Abstract base for fountain prime blocks. A prime block placed directly below
 * the fountain block determines which FountainEffect is active.
 */
public abstract class BlockFountainPrime extends Block {

    protected BlockFountainPrime() {
        super(BlockBehaviour.Properties.of()
                .strength(1.5f, 10.0f)
                .sound(SoundType.AMETHYST)
                .lightLevel(s -> 4)
                .requiresCorrectToolForDrops());
    }

    @Nonnull
    public abstract ResourceLocation getEffectId();
}
