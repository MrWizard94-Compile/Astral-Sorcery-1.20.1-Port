package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

/**
 * Creative mode tab registration for Astral Sorcery.
 * Uses the 1.20 builder-based CreativeModeTab API (replaces ItemGroup).
 */
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
        return new ItemStack(ItemsAS.WAND.get());
    }

    private static void populateTab(@Nonnull CreativeModeTab.ItemDisplayParameters parameters,
                                    @Nonnull CreativeModeTab.Output output) {
        // === Tools & Wands ===
        output.accept(ItemsAS.WAND.get());
        output.accept(ItemsAS.LINKING_TOOL.get());
        output.accept(ItemsAS.HAND_TELESCOPE.get());
        output.accept(ItemsAS.SHIFTING_STONE.get());

        // === Crystal Tools ===
        output.accept(ItemsAS.CRYSTAL_PICKAXE.get());
        output.accept(ItemsAS.CRYSTAL_AXE.get());
        output.accept(ItemsAS.CRYSTAL_SHOVEL.get());
        output.accept(ItemsAS.CRYSTAL_SWORD.get());

        // === Crystals & Materials ===
        output.accept(ItemsAS.ROCK_CRYSTAL.get());
        output.accept(ItemsAS.CELESTIAL_CRYSTAL.get());
        output.accept(ItemsAS.AQUAMARINE.get());
        output.accept(ItemsAS.STARDUST.get());
        output.accept(ItemsAS.RESONATING_GEM.get());
        output.accept(ItemsAS.INFUSED_GLASS.get());
        output.accept(ItemsAS.ILLUMINATION_POWDER.get());
        output.accept(ItemsAS.FORMATION_STONE.get());

        // === Constellation Items ===
        output.accept(ItemsAS.CONSTELLATION_PAPER.get());
        output.accept(ItemsAS.KNOWLEDGE_FRAGMENT.get());

        // === Perk Gems ===
        output.accept(ItemsAS.PERK_GEM_DAY.get());
        output.accept(ItemsAS.PERK_GEM_NIGHT.get());
        output.accept(ItemsAS.PERK_GEM_SKY.get());

        // === Colored Lenses ===
        output.accept(ItemsAS.COLORED_LENS_FIRE.get());
        output.accept(ItemsAS.COLORED_LENS_BREAK.get());
        output.accept(ItemsAS.COLORED_LENS_GROWTH.get());
        output.accept(ItemsAS.COLORED_LENS_DAMAGE.get());
        output.accept(ItemsAS.COLORED_LENS_REGENERATION.get());
        output.accept(ItemsAS.COLORED_LENS_PUSH.get());
        output.accept(ItemsAS.COLORED_LENS_SPECTRAL.get());

        // === Functional Blocks ===
        output.accept(ItemsAS.ALTAR_ITEM.get());
        output.accept(ItemsAS.ATTUNEMENT_ALTAR_ITEM.get());
        output.accept(ItemsAS.INFUSER_ITEM.get());
        output.accept(ItemsAS.WELL_ITEM.get());
        output.accept(ItemsAS.RITUAL_PEDESTAL_ITEM.get());
        output.accept(ItemsAS.CHALICE_ITEM.get());
        output.accept(ItemsAS.FOUNTAIN_ITEM.get());
        output.accept(ItemsAS.GATEWAY_ITEM.get());
        output.accept(ItemsAS.TELESCOPE_ITEM.get());

        // === Starlight Network ===
        output.accept(ItemsAS.COLLECTOR_CRYSTAL_ITEM.get());
        output.accept(ItemsAS.CELESTIAL_COLLECTOR_CRYSTAL_ITEM.get());
        output.accept(ItemsAS.LENS_ITEM.get());
        output.accept(ItemsAS.PRISM_ITEM.get());
        output.accept(ItemsAS.RELAY_ITEM.get());

        // === Building Blocks: Marble ===
        output.accept(ItemsAS.MARBLE_RAW_ITEM.get());
        output.accept(ItemsAS.MARBLE_BRICKS_ITEM.get());
        output.accept(ItemsAS.MARBLE_ARCH_ITEM.get());
        output.accept(ItemsAS.MARBLE_CHISELED_ITEM.get());
        output.accept(ItemsAS.MARBLE_ENGRAVED_ITEM.get());
        output.accept(ItemsAS.MARBLE_RUNED_ITEM.get());
        output.accept(ItemsAS.MARBLE_PILLAR_ITEM.get());
        output.accept(ItemsAS.MARBLE_SLAB_ITEM.get());
        output.accept(ItemsAS.MARBLE_STAIRS_ITEM.get());
        output.accept(ItemsAS.BLACK_MARBLE_RAW_ITEM.get());

        // === Building Blocks: Infused Wood ===
        output.accept(ItemsAS.INFUSED_WOOD_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_PLANKS_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_ARCH_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_COLUMN_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_ENGRAVED_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_ENRICHED_ITEM.get());

        // === Ores & Special ===
        output.accept(ItemsAS.ROCK_CRYSTAL_ORE_ITEM.get());
        output.accept(ItemsAS.AQUAMARINE_ORE_ITEM.get());
        output.accept(ItemsAS.ILLUMINATOR_ITEM.get());
    }
}
