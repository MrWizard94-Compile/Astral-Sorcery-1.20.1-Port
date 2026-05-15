package hellfirepvp.astralsorcery.common.item.base;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

/**
 * Default BlockItem for Astral Sorcery blocks.
 * Used when a block doesn't specify a custom item via {@link CustomItemBlock}.
 *
 * <p>1.16 -> 1.20 changes:
 * BlockItem constructor stable. Item.Properties.group removed (creative tabs).</p>
 */
public class ItemBlockAS extends BlockItem {

    public ItemBlockAS(@Nonnull Block block) {
        this(block, new Item.Properties());
    }

    public ItemBlockAS(@Nonnull Block block, @Nonnull Item.Properties properties) {
        super(block, properties);
    }
}
