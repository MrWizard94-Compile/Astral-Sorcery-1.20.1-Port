package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemsAS {

    private ItemsAS() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AstralSorcery.MODID);

    // Entries will be registered as item classes are ported in Phase 5.
    // Pattern: public static final RegistryObject<ItemFoo> FOO = ITEMS.register("foo", ItemFoo::new);
}
