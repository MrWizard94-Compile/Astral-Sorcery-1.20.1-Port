package hellfirepvp.astralsorcery.common.block.base;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for blocks that need a custom BlockItem (ItemBlock) class
 * rather than the default one created during registration.
 *
 * <p>1.16 -> 1.20 changes:
 * BlockItem stable, Item.Properties unchanged conceptually.</p>
 */
public interface CustomItemBlock {

    /**
     * Create the custom BlockItem for this block.
     *
     * @param block      the block this item represents
     * @param properties the item properties to use
     * @return the custom block item, or null for default
     */
    @Nullable
    BlockItem createBlockItem(@Nonnull Block block, @Nonnull Item.Properties properties);
}
