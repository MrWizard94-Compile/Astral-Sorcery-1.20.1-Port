package hellfirepvp.astralsorcery.common.block.base;

import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

/**
 * Base class for all Astral Sorcery blocks.
 * Provides mod-specific defaults and shared functionality.
 *
 * <p>1.16 -> 1.20 changes:
 * Block.Properties unchanged conceptually,
 * AbstractBlock.Properties -> BlockBehaviour.Properties in internal naming,
 * harvestLevel/harvestTool removed (tag-based tool tiering),
 * setRequiresTool now via properties.requiresCorrectToolForDrops()</p>
 */
public class BlockAS extends Block {

    protected BlockAS(@Nonnull Properties properties) {
        super(properties);
    }
}
