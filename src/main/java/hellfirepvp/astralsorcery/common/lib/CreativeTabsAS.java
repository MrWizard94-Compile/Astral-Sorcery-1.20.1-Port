package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.block.ItemBlockCollectorCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.ItemAttunedCelestialCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.ItemAttunedRockCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Creative mode tab registration for Astral Sorcery.
 * Tab 1 ("Astral Sorcery"): tools, wands, crystals, constellation items, perk gear, mantles, lenses.
 * Tab 2 ("Astral Sorcery: Blocks"): altars, network blocks, building blocks, ores.
 */
public class CreativeTabsAS {

    private CreativeTabsAS() {}

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AstralSorcery.MODID);

    public static final RegistryObject<CreativeModeTab> ASTRAL_SORCERY_TAB =
            CREATIVE_TABS.register("astralsorcery", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.astralsorcery"))
                    .icon(CreativeTabsAS::getItemsTabIcon)
                    .displayItems(CreativeTabsAS::populateItemsTab)
                    .build());

    public static final RegistryObject<CreativeModeTab> ASTRAL_SORCERY_BLOCKS_TAB =
            CREATIVE_TABS.register("astralsorcery_blocks", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.astralsorcery.blocks"))
                    .icon(CreativeTabsAS::getBlocksTabIcon)
                    .displayItems(CreativeTabsAS::populateBlocksTab)
                    .build());

    // -------------------------------------------------------------------------
    // Tab 1: Items — tools, crystals, papers, perks, armor, lenses
    // -------------------------------------------------------------------------

    @Nonnull
    private static ItemStack getItemsTabIcon() {
        return new ItemStack(ItemsAS.WAND.get());
    }

    private static void populateItemsTab(@Nonnull CreativeModeTab.ItemDisplayParameters parameters,
                                         @Nonnull CreativeModeTab.Output output) {
        // === Tools & Wands ===
        output.accept(ItemsAS.WAND.get());
        output.accept(ItemsAS.ARCHITECT_WAND.get());
        output.accept(ItemsAS.BLINK_WAND.get());
        output.accept(ItemsAS.EXCHANGE_WAND.get());
        output.accept(ItemsAS.GRAPPLE_WAND.get());
        output.accept(ItemsAS.ILLUMINATION_WAND.get());
        output.accept(ItemsAS.LINKING_TOOL.get());
        output.accept(ItemsAS.HAND_TELESCOPE.get());
        output.accept(ItemsAS.SHIFTING_STONE.get());
        output.accept(ItemsAS.CHISEL.get());
        output.accept(ItemsAS.RESONATOR.get());
        output.accept(ItemsAS.TOME.get());
        output.accept(ItemsAS.PARCHMENT.get());
        output.accept(ItemsAS.ENCHANTMENT_AMULET.get());
        output.accept(ItemsAS.KNOWLEDGE_SHARE.get());

        // === Crystal Tools — shown with max-quality properties pre-applied ===
        output.accept(maxToolStack(ItemsAS.CRYSTAL_PICKAXE.get()));
        output.accept(maxToolStack(ItemsAS.CRYSTAL_AXE.get()));
        output.accept(maxToolStack(ItemsAS.CRYSTAL_SHOVEL.get()));
        output.accept(maxToolStack(ItemsAS.CRYSTAL_SWORD.get()));
        output.accept(maxToolStack(ItemsAS.INFUSED_CRYSTAL_PICKAXE.get()));
        output.accept(maxToolStack(ItemsAS.INFUSED_CRYSTAL_AXE.get()));
        output.accept(maxToolStack(ItemsAS.INFUSED_CRYSTAL_SHOVEL.get()));
        output.accept(maxToolStack(ItemsAS.INFUSED_CRYSTAL_SWORD.get()));

        // === Crystals & Materials ===
        output.accept(maxCrystalStack(ItemsAS.ROCK_CRYSTAL.get()));
        output.accept(maxCrystalStack(ItemsAS.ATTUNED_ROCK_CRYSTAL.get())); // blank
        for (IConstellation cst : ConstellationRegistry.getMajorConstellations()) {
            if (cst instanceof IMajorConstellation major) {
                ItemStack stack = maxCrystalStack(ItemsAS.ATTUNED_ROCK_CRYSTAL.get());
                ((ItemAttunedRockCrystal) stack.getItem()).setAttunedConstellation(stack, major);
                output.accept(stack);
            }
        }
        output.accept(maxCrystalStack(ItemsAS.CELESTIAL_CRYSTAL.get()));
        output.accept(maxCrystalStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get())); // blank
        for (IConstellation cst : ConstellationRegistry.getMajorConstellations()) {
            if (cst instanceof IMajorConstellation major) {
                ItemStack stack = maxCrystalStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get());
                ((ItemAttunedCelestialCrystal) stack.getItem()).setAttunedConstellation(stack, major);
                output.accept(stack);
            }
        }
        output.accept(ItemsAS.AQUAMARINE.get());
        output.accept(ItemsAS.STARDUST.get());
        output.accept(ItemsAS.STARMETAL_INGOT.get());
        output.accept(ItemsAS.STARMETAL_DUST.get());
        output.accept(ItemsAS.RESONATING_GEM.get());
        output.accept(ItemsAS.GLASS_LENS.get());
        output.accept(ItemsAS.INFUSED_GLASS.get());
        output.accept(ItemsAS.ILLUMINATION_POWDER.get());
        output.accept(ItemsAS.NOCTURNAL_POWDER.get());
        output.accept(ItemsAS.FORMATION_STONE.get());
        output.accept(ItemsAS.BUCKET_LIQUID_STARLIGHT.get());

        // === Constellation Items ===
        output.accept(ItemsAS.CONSTELLATION_PAPER.get()); // blank
        for (IConstellation cst : ConstellationRegistry.getAllConstellations()) {
            ItemStack paper = new ItemStack(ItemsAS.CONSTELLATION_PAPER.get());
            ItemConstellationPaper.setConstellation(paper, cst.getRegistryName());
            output.accept(paper);
        }
        output.accept(ItemsAS.KNOWLEDGE_FRAGMENT.get());

        // === Perk Items ===
        output.accept(ItemsAS.DAZZLING_GEM.get());
        output.accept(ItemsAS.DAZZLING_FRAME.get());
        output.accept(ItemsAS.PERK_GEM_DAY.get());
        output.accept(ItemsAS.PERK_GEM_NIGHT.get());
        output.accept(ItemsAS.PERK_GEM_SKY.get());
        output.accept(ItemsAS.PERK_SEAL.get());

        // === Shifting Stars ===
        output.accept(ItemsAS.SHIFTING_STAR.get());
        output.accept(ItemsAS.SHIFTING_STAR_AEVITAS.get());
        output.accept(ItemsAS.SHIFTING_STAR_ARMARA.get());
        output.accept(ItemsAS.SHIFTING_STAR_DISCIDIA.get());
        output.accept(ItemsAS.SHIFTING_STAR_EVORSIO.get());
        output.accept(ItemsAS.SHIFTING_STAR_VICIO.get());

        // === Mantles (Armor) ===
        output.accept(ItemsAS.MANTLE_DISCIDIA.get());
        output.accept(ItemsAS.MANTLE_ARMARA.get());
        output.accept(ItemsAS.MANTLE_VICIO.get());
        output.accept(ItemsAS.MANTLE_AEVITAS.get());
        output.accept(ItemsAS.MANTLE_EVORSIO.get());
        output.accept(ItemsAS.MANTLE_BOOTES.get());
        output.accept(ItemsAS.MANTLE_FORNAX.get());
        output.accept(ItemsAS.MANTLE_HOROLOGIUM.get());
        output.accept(ItemsAS.MANTLE_LUCERNA.get());
        output.accept(ItemsAS.MANTLE_MINERALIS.get());
        output.accept(ItemsAS.MANTLE_OCTANS.get());
        output.accept(ItemsAS.MANTLE_PELOTRIO.get());

        // === Colored Lenses ===
        output.accept(ItemsAS.COLORED_LENS_FIRE.get());
        output.accept(ItemsAS.COLORED_LENS_BREAK.get());
        output.accept(ItemsAS.COLORED_LENS_GROWTH.get());
        output.accept(ItemsAS.COLORED_LENS_DAMAGE.get());
        output.accept(ItemsAS.COLORED_LENS_REGENERATION.get());
        output.accept(ItemsAS.COLORED_LENS_PUSH.get());
        output.accept(ItemsAS.COLORED_LENS_SPECTRAL.get());
    }

    // -------------------------------------------------------------------------
    // Tab 2: Blocks — altars, network, building blocks, ores
    // -------------------------------------------------------------------------

    @Nonnull
    private static ItemStack getBlocksTabIcon() {
        return new ItemStack(ItemsAS.ALTAR_DISCOVERY_ITEM.get());
    }

    private static void populateBlocksTab(@Nonnull CreativeModeTab.ItemDisplayParameters parameters,
                                          @Nonnull CreativeModeTab.Output output) {
        // === Functional Blocks ===
        output.accept(ItemsAS.ALTAR_DISCOVERY_ITEM.get());
        output.accept(ItemsAS.ALTAR_ATTUNEMENT_ITEM.get());
        output.accept(ItemsAS.ALTAR_CONSTELLATION_ITEM.get());
        output.accept(ItemsAS.ALTAR_RADIANCE_ITEM.get());
        output.accept(ItemsAS.ATTUNEMENT_ALTAR_ITEM.get());
        output.accept(ItemsAS.INFUSER_ITEM.get());
        output.accept(ItemsAS.WELL_ITEM.get());
        output.accept(ItemsAS.RITUAL_PEDESTAL_ITEM.get());
        output.accept(ItemsAS.CHALICE_ITEM.get());
        output.accept(ItemsAS.FOUNTAIN_ITEM.get());
        output.accept(ItemsAS.FOUNTAIN_PRIME_LIQUID_ITEM.get());
        output.accept(ItemsAS.FOUNTAIN_PRIME_ORE_ITEM.get());
        output.accept(ItemsAS.FOUNTAIN_PRIME_VORTEX_ITEM.get());
        output.accept(ItemsAS.GATEWAY_ITEM.get());
        output.accept(ItemsAS.TELESCOPE_ITEM.get());
        output.accept(ItemsAS.OBSERVATORY_ITEM.get());
        output.accept(ItemsAS.TREE_BEACON_ITEM.get());
        output.accept(ItemsAS.REFRACTION_TABLE_ITEM.get());
        output.accept(ItemsAS.RITUAL_LINK_ITEM.get());

        // === Starlight Network — plain then per-major-constellation variants ===
        output.accept(collectorStack(ItemsAS.COLLECTOR_CRYSTAL_ITEM.get(), null));
        for (IConstellation cst : ConstellationRegistry.getMajorConstellations()) {
            if (cst instanceof IMajorConstellation major) {
                output.accept(collectorStack(ItemsAS.COLLECTOR_CRYSTAL_ITEM.get(), major));
            }
        }
        output.accept(collectorStack(ItemsAS.CELESTIAL_COLLECTOR_CRYSTAL_ITEM.get(), null));
        for (IConstellation cst : ConstellationRegistry.getMajorConstellations()) {
            if (cst instanceof IMajorConstellation major) {
                output.accept(collectorStack(ItemsAS.CELESTIAL_COLLECTOR_CRYSTAL_ITEM.get(), major));
            }
        }
        output.accept(ItemsAS.LENS_ITEM.get());
        output.accept(ItemsAS.PRISM_ITEM.get());
        output.accept(ItemsAS.RELAY_ITEM.get());
        output.accept(ItemsAS.SPECTRAL_RELAY_ITEM.get());

        // === Crystal Clusters ===
        output.accept(ItemsAS.CELESTIAL_CRYSTAL_CLUSTER_ITEM.get());
        output.accept(ItemsAS.GEM_CRYSTAL_CLUSTER_ITEM.get());

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

        // === Building Blocks: Black Marble ===
        output.accept(ItemsAS.BLACK_MARBLE_RAW_ITEM.get());
        output.accept(ItemsAS.BLACK_MARBLE_ARCH_ITEM.get());
        output.accept(ItemsAS.BLACK_MARBLE_BRICKS_ITEM.get());
        output.accept(ItemsAS.BLACK_MARBLE_CHISELED_ITEM.get());
        output.accept(ItemsAS.BLACK_MARBLE_ENGRAVED_ITEM.get());
        output.accept(ItemsAS.BLACK_MARBLE_RUNED_ITEM.get());
        output.accept(ItemsAS.BLACK_MARBLE_PILLAR_ITEM.get());
        output.accept(ItemsAS.BLACK_MARBLE_SLAB_ITEM.get());
        output.accept(ItemsAS.BLACK_MARBLE_STAIRS_ITEM.get());

        // === Building Blocks: Infused Wood ===
        output.accept(ItemsAS.INFUSED_WOOD_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_PLANKS_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_ARCH_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_COLUMN_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_ENGRAVED_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_ENRICHED_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_INFUSED_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_SLAB_ITEM.get());
        output.accept(ItemsAS.INFUSED_WOOD_STAIRS_ITEM.get());

        // === Ores & Ore Blocks ===
        output.accept(ItemsAS.ROCK_CRYSTAL_ORE_ITEM.get());
        output.accept(ItemsAS.AQUAMARINE_ORE_ITEM.get());
        output.accept(ItemsAS.STARMETAL_ORE_ITEM.get());
        output.accept(ItemsAS.STARMETAL_ITEM.get());
        output.accept(ItemsAS.GLOW_FLOWER_ITEM.get());
        output.accept(ItemsAS.ILLUMINATOR_ITEM.get());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Create an attuned collector crystal stack. Pass null constellation for the plain (untuned) variant. */
    @Nonnull
    private static ItemStack collectorStack(@Nonnull ItemBlockCollectorCrystal item,
                                            @Nullable IMajorConstellation constellation) {
        ItemStack stack = new ItemStack(item);
        if (constellation != null) {
            item.setAttunedConstellation(stack, constellation);
        }
        return stack;
    }

    /** Create a crystal ItemStack with all physical properties at max tier. */
    @Nonnull
    private static ItemStack maxCrystalStack(@Nonnull net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        if (item instanceof ItemCrystalBase crystal) {
            CrystalAttributes maxAttrs = CrystalAttributes.Builder.newBuilder(true)
                    .addProperty(CrystalPropertiesAS.Properties.PROPERTY_SIZE, 3)
                    .addProperty(CrystalPropertiesAS.Properties.PROPERTY_PURITY, 2)
                    .addProperty(CrystalPropertiesAS.Properties.PROPERTY_SHAPE, 3)
                    .addProperty(CrystalPropertiesAS.Properties.PROPERTY_COLLECTOR_COLLECTION_RATE, 3)
                    .addProperty(CrystalPropertiesAS.Properties.PROPERTY_TOOL_DURABILITY, 3)
                    .addProperty(CrystalPropertiesAS.Properties.PROPERTY_TOOL_EFFICIENCY, 3)
                    .addProperty(CrystalPropertiesAS.Properties.PROPERTY_RITUAL_RANGE, 2)
                    .addProperty(CrystalPropertiesAS.Properties.PROPERTY_RITUAL_EFFECT, 3)
                    .build();
            crystal.setAttributes(stack, maxAttrs);
        }
        return stack;
    }

    /** Create a tool ItemStack with maximum crystal properties pre-applied (for creative tab display). */
    @Nonnull
    private static ItemStack maxToolStack(@Nonnull net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        CrystalProperties.setOnStack(stack, CrystalProperties.createMaximum());
        return stack;
    }
}
