package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class CreativeTabsAS {

    private CreativeTabsAS() {}

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AstralSorcery.MODID);

    public static final RegistryObject<CreativeModeTab> ASTRAL_SORCERY_TAB =
            CREATIVE_TABS.register("astralsorcery", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.astralsorcery"))
                    .icon(CreativeTabsAS::getTabIcon)
                    .displayItems(CreativeTabsAS::populateTab)
                    .build());

    @Nonnull
    private static ItemStack getTabIcon() {
        // TODO: Replace with actual AS item (e.g., ItemsAS.JOURNAL or BlocksAS.ALTAR_DISCOVERY)
        // once those are registered in Phase 5.
        return new ItemStack(Items.NETHER_STAR);
    }

    private static void populateTab(@Nonnull CreativeModeTab.ItemDisplayParameters parameters,
                                    @Nonnull CreativeModeTab.Output output) {
        // TODO: Add all AS items/blocks here once content is registered in Phase 5.
        // Pattern: output.accept(BlocksAS.MARBLE_RAW.get());
        //          output.accept(ItemsAS.AQUAMARINE.get());
    }
}
