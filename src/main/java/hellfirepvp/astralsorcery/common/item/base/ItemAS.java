package hellfirepvp.astralsorcery.common.item.base;

import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;

/**
 * Base class for all Astral Sorcery items.
 * Provides mod-specific defaults.
 *
 * <p>1.16 -> 1.20 changes:
 * Item.Properties.group(ItemGroup) removed — creative tabs
 * are now registered separately via CreativeModeTab system.
 * MaxStackSize -> stacksTo, setNoRepair -> no direct equivalent (use durability(0)),
 * Item.Properties unchanged conceptually.</p>
 */
public class ItemAS extends Item {

    public ItemAS(@Nonnull Properties properties) {
        super(properties);
    }

    public ItemAS() {
        this(defaultProperties());
    }

    @Nonnull
    public static Properties defaultProperties() {
        return new Properties();
    }
}
