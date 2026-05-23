package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.item.ItemChisel;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.ItemEnchantmentAmulet;
import hellfirepvp.astralsorcery.common.item.ItemFormationStone;
import hellfirepvp.astralsorcery.common.item.ItemHandTelescope;
import hellfirepvp.astralsorcery.common.item.ItemIlluminationPowder;
import hellfirepvp.astralsorcery.common.item.ItemNocturnalPowder;
import hellfirepvp.astralsorcery.common.item.ItemInfusedGlass;
import hellfirepvp.astralsorcery.common.item.ItemKnowledgeFragment;
import hellfirepvp.astralsorcery.common.item.ItemKnowledgeShare;
import hellfirepvp.astralsorcery.common.item.ItemLinkingTool;
import hellfirepvp.astralsorcery.common.item.ItemParchment;
import hellfirepvp.astralsorcery.common.item.ItemResonatingGem;
import hellfirepvp.astralsorcery.common.item.ItemResonator;
import hellfirepvp.astralsorcery.common.item.ItemStardust;
import hellfirepvp.astralsorcery.common.item.ItemStarmetalDust;
import hellfirepvp.astralsorcery.common.item.ItemStarmetalIngot;
import hellfirepvp.astralsorcery.common.item.ItemGlassLens;
import hellfirepvp.astralsorcery.common.item.ItemPerkSeal;
import hellfirepvp.astralsorcery.common.item.ItemTome;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantleAevitas;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantleArmara;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantleDiscidia;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantleEvorsio;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantleVicio;
import hellfirepvp.astralsorcery.common.item.base.ItemBlockAS;
import hellfirepvp.astralsorcery.common.item.crystal.ItemAttunedCelestialCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.ItemAttunedRockCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCelestialCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.ItemRockCrystalSimple;
import hellfirepvp.astralsorcery.common.item.gem.ItemAquamarine;
import hellfirepvp.astralsorcery.common.item.gem.ItemPerkGem;
import hellfirepvp.astralsorcery.common.item.lens.ItemColoredLens;
import hellfirepvp.astralsorcery.common.item.tool.ItemCrystalAxe;
import hellfirepvp.astralsorcery.common.item.tool.ItemCrystalPickaxe;
import hellfirepvp.astralsorcery.common.item.tool.ItemCrystalShovel;
import hellfirepvp.astralsorcery.common.item.tool.ItemCrystalSword;
import hellfirepvp.astralsorcery.common.item.useeffect.ItemShiftingStar;
import hellfirepvp.astralsorcery.common.item.useeffect.ItemShiftingStarAevitas;
import hellfirepvp.astralsorcery.common.item.useeffect.ItemShiftingStarArmara;
import hellfirepvp.astralsorcery.common.item.useeffect.ItemShiftingStarDiscidia;
import hellfirepvp.astralsorcery.common.item.useeffect.ItemShiftingStarEvorsio;
import hellfirepvp.astralsorcery.common.item.useeffect.ItemShiftingStarVicio;
import hellfirepvp.astralsorcery.common.item.useeffect.ItemShiftingStone;
import hellfirepvp.astralsorcery.common.item.wand.ItemArchitectWand;
import hellfirepvp.astralsorcery.common.item.wand.ItemBlinkWand;
import hellfirepvp.astralsorcery.common.item.wand.ItemExchangeWand;
import hellfirepvp.astralsorcery.common.item.wand.ItemGrappleWand;
import hellfirepvp.astralsorcery.common.item.wand.ItemIlluminationWand;
import hellfirepvp.astralsorcery.common.item.wand.ItemWand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * All Astral Sorcery item registrations.
 */
public class ItemsAS {

    private ItemsAS() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AstralSorcery.MODID);

    // ---- Tools ----
    public static final RegistryObject<ItemWand> WAND =
            ITEMS.register("wand", ItemWand::new);
    public static final RegistryObject<ItemArchitectWand> ARCHITECT_WAND =
            ITEMS.register("architect_wand", ItemArchitectWand::new);
    public static final RegistryObject<ItemBlinkWand> BLINK_WAND =
            ITEMS.register("blink_wand", ItemBlinkWand::new);
    public static final RegistryObject<ItemExchangeWand> EXCHANGE_WAND =
            ITEMS.register("exchange_wand", ItemExchangeWand::new);
    public static final RegistryObject<ItemGrappleWand> GRAPPLE_WAND =
            ITEMS.register("grapple_wand", ItemGrappleWand::new);
    public static final RegistryObject<ItemIlluminationWand> ILLUMINATION_WAND =
            ITEMS.register("illumination_wand", ItemIlluminationWand::new);
    public static final RegistryObject<ItemChisel> CHISEL =
            ITEMS.register("chisel", ItemChisel::new);

    // ---- Crystals ----
    public static final RegistryObject<ItemRockCrystalSimple> ROCK_CRYSTAL =
            ITEMS.register("rock_crystal", ItemRockCrystalSimple::new);
    public static final RegistryObject<ItemAttunedRockCrystal> ATTUNED_ROCK_CRYSTAL =
            ITEMS.register("attuned_rock_crystal", ItemAttunedRockCrystal::new);
    public static final RegistryObject<ItemCelestialCrystal> CELESTIAL_CRYSTAL =
            ITEMS.register("celestial_crystal", ItemCelestialCrystal::new);
    public static final RegistryObject<ItemAttunedCelestialCrystal> ATTUNED_CELESTIAL_CRYSTAL =
            ITEMS.register("attuned_celestial_crystal", ItemAttunedCelestialCrystal::new);

    // ---- Materials ----
    public static final RegistryObject<ItemAquamarine> AQUAMARINE =
            ITEMS.register("aquamarine", ItemAquamarine::new);
    public static final RegistryObject<ItemStardust> STARDUST =
            ITEMS.register("stardust", ItemStardust::new);
    public static final RegistryObject<ItemStarmetalIngot> STARMETAL_INGOT =
            ITEMS.register("starmetal_ingot", ItemStarmetalIngot::new);
    public static final RegistryObject<ItemStarmetalDust> STARMETAL_DUST =
            ITEMS.register("starmetal_dust", ItemStarmetalDust::new);
    public static final RegistryObject<ItemGlassLens> GLASS_LENS =
            ITEMS.register("glass_lens", ItemGlassLens::new);
    public static final RegistryObject<ItemIlluminationPowder> ILLUMINATION_POWDER =
            ITEMS.register("illumination_powder", ItemIlluminationPowder::new);
    public static final RegistryObject<ItemNocturnalPowder> NOCTURNAL_POWDER =
            ITEMS.register("nocturnal_powder", ItemNocturnalPowder::new);
    public static final RegistryObject<ItemInfusedGlass> INFUSED_GLASS =
            ITEMS.register("infused_glass", ItemInfusedGlass::new);
    public static final RegistryObject<ItemResonatingGem> RESONATING_GEM =
            ITEMS.register("resonating_gem", ItemResonatingGem::new);
    public static final RegistryObject<ItemFormationStone> FORMATION_STONE =
            ITEMS.register("formation_stone", ItemFormationStone::new);
    public static final RegistryObject<ItemPerkSeal> PERK_SEAL =
            ITEMS.register("perk_seal", ItemPerkSeal::new);

    // ---- Perk Gems ----
    public static final RegistryObject<ItemPerkGem> PERK_GEM_DAY =
            ITEMS.register("perk_gem_day", () -> new ItemPerkGem(ItemPerkGem.GemType.DAY));
    public static final RegistryObject<ItemPerkGem> PERK_GEM_NIGHT =
            ITEMS.register("perk_gem_night", () -> new ItemPerkGem(ItemPerkGem.GemType.NIGHT));
    public static final RegistryObject<ItemPerkGem> PERK_GEM_SKY =
            ITEMS.register("perk_gem_sky", () -> new ItemPerkGem(ItemPerkGem.GemType.SKY));

    // ---- Crystal Tools ----
    public static final RegistryObject<ItemCrystalPickaxe> CRYSTAL_PICKAXE =
            ITEMS.register("crystal_pickaxe", ItemCrystalPickaxe::new);
    public static final RegistryObject<ItemCrystalAxe> CRYSTAL_AXE =
            ITEMS.register("crystal_axe", ItemCrystalAxe::new);
    public static final RegistryObject<ItemCrystalShovel> CRYSTAL_SHOVEL =
            ITEMS.register("crystal_shovel", ItemCrystalShovel::new);
    public static final RegistryObject<ItemCrystalSword> CRYSTAL_SWORD =
            ITEMS.register("crystal_sword", ItemCrystalSword::new);

    // ---- Constellation Items ----
    public static final RegistryObject<ItemConstellationPaper> CONSTELLATION_PAPER =
            ITEMS.register("constellation_paper", ItemConstellationPaper::new);
    public static final RegistryObject<ItemKnowledgeFragment> KNOWLEDGE_FRAGMENT =
            ITEMS.register("knowledge_fragment", ItemKnowledgeFragment::new);

    // ---- Specialized Tools ----
    public static final RegistryObject<ItemLinkingTool> LINKING_TOOL =
            ITEMS.register("linking_tool", ItemLinkingTool::new);
    public static final RegistryObject<ItemHandTelescope> HAND_TELESCOPE =
            ITEMS.register("hand_telescope", ItemHandTelescope::new);
    public static final RegistryObject<ItemShiftingStone> SHIFTING_STONE =
            ITEMS.register("shifting_stone", ItemShiftingStone::new);

    // ---- Constellation Mantles ----
    public static final RegistryObject<ItemMantleDiscidia> MANTLE_DISCIDIA =
            ITEMS.register("mantle_discidia", ItemMantleDiscidia::new);
    public static final RegistryObject<ItemMantleArmara> MANTLE_ARMARA =
            ITEMS.register("mantle_armara", ItemMantleArmara::new);
    public static final RegistryObject<ItemMantleVicio> MANTLE_VICIO =
            ITEMS.register("mantle_vicio", ItemMantleVicio::new);
    public static final RegistryObject<ItemMantleAevitas> MANTLE_AEVITAS =
            ITEMS.register("mantle_aevitas", ItemMantleAevitas::new);
    public static final RegistryObject<ItemMantleEvorsio> MANTLE_EVORSIO =
            ITEMS.register("mantle_evorsio", ItemMantleEvorsio::new);

    // ---- Colored Lenses ----
    public static final RegistryObject<ItemColoredLens> COLORED_LENS_FIRE =
            ITEMS.register("colored_lens_fire", () -> new ItemColoredLens(ItemColoredLens.LensColor.FIRE));
    public static final RegistryObject<ItemColoredLens> COLORED_LENS_BREAK =
            ITEMS.register("colored_lens_break", () -> new ItemColoredLens(ItemColoredLens.LensColor.BREAK));
    public static final RegistryObject<ItemColoredLens> COLORED_LENS_GROWTH =
            ITEMS.register("colored_lens_growth", () -> new ItemColoredLens(ItemColoredLens.LensColor.GROWTH));
    public static final RegistryObject<ItemColoredLens> COLORED_LENS_DAMAGE =
            ITEMS.register("colored_lens_damage", () -> new ItemColoredLens(ItemColoredLens.LensColor.DAMAGE));
    public static final RegistryObject<ItemColoredLens> COLORED_LENS_REGENERATION =
            ITEMS.register("colored_lens_regeneration", () -> new ItemColoredLens(ItemColoredLens.LensColor.REGENERATION));
    public static final RegistryObject<ItemColoredLens> COLORED_LENS_PUSH =
            ITEMS.register("colored_lens_push", () -> new ItemColoredLens(ItemColoredLens.LensColor.PUSH));
    public static final RegistryObject<ItemColoredLens> COLORED_LENS_SPECTRAL =
            ITEMS.register("colored_lens_spectral", () -> new ItemColoredLens(ItemColoredLens.LensColor.SPECTRAL));

    // ---- Shifting Stars ----
    public static final RegistryObject<ItemShiftingStar> SHIFTING_STAR =
            ITEMS.register("shifting_star", ItemShiftingStar::new);
    public static final RegistryObject<ItemShiftingStarAevitas> SHIFTING_STAR_AEVITAS =
            ITEMS.register("shifting_star_aevitas", ItemShiftingStarAevitas::new);
    public static final RegistryObject<ItemShiftingStarArmara> SHIFTING_STAR_ARMARA =
            ITEMS.register("shifting_star_armara", ItemShiftingStarArmara::new);
    public static final RegistryObject<ItemShiftingStarDiscidia> SHIFTING_STAR_DISCIDIA =
            ITEMS.register("shifting_star_discidia", ItemShiftingStarDiscidia::new);
    public static final RegistryObject<ItemShiftingStarEvorsio> SHIFTING_STAR_EVORSIO =
            ITEMS.register("shifting_star_evorsio", ItemShiftingStarEvorsio::new);
    public static final RegistryObject<ItemShiftingStarVicio> SHIFTING_STAR_VICIO =
            ITEMS.register("shifting_star_vicio", ItemShiftingStarVicio::new);

    // ---- Miscellaneous Items ----
    public static final RegistryObject<ItemEnchantmentAmulet> ENCHANTMENT_AMULET =
            ITEMS.register("enchantment_amulet", ItemEnchantmentAmulet::new);
    public static final RegistryObject<ItemParchment> PARCHMENT =
            ITEMS.register("parchment", ItemParchment::new);
    public static final RegistryObject<ItemTome> TOME =
            ITEMS.register("tome", ItemTome::new);
    public static final RegistryObject<ItemResonator> RESONATOR =
            ITEMS.register("resonator", ItemResonator::new);
    public static final RegistryObject<ItemKnowledgeShare> KNOWLEDGE_SHARE =
            ITEMS.register("knowledge_share", ItemKnowledgeShare::new);

    // ---- Block Items ----
    // Marble family
    public static final RegistryObject<BlockItem> MARBLE_RAW_ITEM =
            ITEMS.register("marble_raw", () -> new ItemBlockAS(BlocksAS.MARBLE_RAW.get()));
    public static final RegistryObject<BlockItem> MARBLE_ARCH_ITEM =
            ITEMS.register("marble_arch", () -> new ItemBlockAS(BlocksAS.MARBLE_ARCH.get()));
    public static final RegistryObject<BlockItem> MARBLE_BRICKS_ITEM =
            ITEMS.register("marble_bricks", () -> new ItemBlockAS(BlocksAS.MARBLE_BRICKS.get()));
    public static final RegistryObject<BlockItem> MARBLE_CHISELED_ITEM =
            ITEMS.register("marble_chiseled", () -> new ItemBlockAS(BlocksAS.MARBLE_CHISELED.get()));
    public static final RegistryObject<BlockItem> MARBLE_ENGRAVED_ITEM =
            ITEMS.register("marble_engraved", () -> new ItemBlockAS(BlocksAS.MARBLE_ENGRAVED.get()));
    public static final RegistryObject<BlockItem> MARBLE_RUNED_ITEM =
            ITEMS.register("marble_runed", () -> new ItemBlockAS(BlocksAS.MARBLE_RUNED.get()));
    public static final RegistryObject<BlockItem> MARBLE_PILLAR_ITEM =
            ITEMS.register("marble_pillar", () -> new ItemBlockAS(BlocksAS.MARBLE_PILLAR.get()));
    public static final RegistryObject<BlockItem> MARBLE_SLAB_ITEM =
            ITEMS.register("marble_slab", () -> new ItemBlockAS(BlocksAS.MARBLE_SLAB.get()));
    public static final RegistryObject<BlockItem> MARBLE_STAIRS_ITEM =
            ITEMS.register("marble_stairs", () -> new ItemBlockAS(BlocksAS.MARBLE_STAIRS.get()));

    // Black marble
    public static final RegistryObject<BlockItem> BLACK_MARBLE_RAW_ITEM =
            ITEMS.register("black_marble_raw", () -> new ItemBlockAS(BlocksAS.BLACK_MARBLE_RAW.get()));
    public static final RegistryObject<BlockItem> BLACK_MARBLE_ARCH_ITEM =
            ITEMS.register("black_marble_arch", () -> new ItemBlockAS(BlocksAS.BLACK_MARBLE_ARCH.get()));
    public static final RegistryObject<BlockItem> BLACK_MARBLE_BRICKS_ITEM =
            ITEMS.register("black_marble_bricks", () -> new ItemBlockAS(BlocksAS.BLACK_MARBLE_BRICKS.get()));
    public static final RegistryObject<BlockItem> BLACK_MARBLE_CHISELED_ITEM =
            ITEMS.register("black_marble_chiseled", () -> new ItemBlockAS(BlocksAS.BLACK_MARBLE_CHISELED.get()));
    public static final RegistryObject<BlockItem> BLACK_MARBLE_ENGRAVED_ITEM =
            ITEMS.register("black_marble_engraved", () -> new ItemBlockAS(BlocksAS.BLACK_MARBLE_ENGRAVED.get()));
    public static final RegistryObject<BlockItem> BLACK_MARBLE_RUNED_ITEM =
            ITEMS.register("black_marble_runed", () -> new ItemBlockAS(BlocksAS.BLACK_MARBLE_RUNED.get()));
    public static final RegistryObject<BlockItem> BLACK_MARBLE_PILLAR_ITEM =
            ITEMS.register("black_marble_pillar", () -> new ItemBlockAS(BlocksAS.BLACK_MARBLE_PILLAR.get()));

    // Infused wood family
    public static final RegistryObject<BlockItem> INFUSED_WOOD_ITEM =
            ITEMS.register("infused_wood", () -> new ItemBlockAS(BlocksAS.INFUSED_WOOD.get()));
    public static final RegistryObject<BlockItem> INFUSED_WOOD_ARCH_ITEM =
            ITEMS.register("infused_wood_arch", () -> new ItemBlockAS(BlocksAS.INFUSED_WOOD_ARCH.get()));
    public static final RegistryObject<BlockItem> INFUSED_WOOD_COLUMN_ITEM =
            ITEMS.register("infused_wood_column", () -> new ItemBlockAS(BlocksAS.INFUSED_WOOD_COLUMN.get()));
    public static final RegistryObject<BlockItem> INFUSED_WOOD_ENGRAVED_ITEM =
            ITEMS.register("infused_wood_engraved", () -> new ItemBlockAS(BlocksAS.INFUSED_WOOD_ENGRAVED.get()));
    public static final RegistryObject<BlockItem> INFUSED_WOOD_ENRICHED_ITEM =
            ITEMS.register("infused_wood_enriched", () -> new ItemBlockAS(BlocksAS.INFUSED_WOOD_ENRICHED.get()));
    public static final RegistryObject<BlockItem> INFUSED_WOOD_PLANKS_ITEM =
            ITEMS.register("infused_wood_planks", () -> new ItemBlockAS(BlocksAS.INFUSED_WOOD_PLANKS.get()));

    // Ores
    public static final RegistryObject<BlockItem> ROCK_CRYSTAL_ORE_ITEM =
            ITEMS.register("rock_crystal_ore", () -> new ItemBlockAS(BlocksAS.ROCK_CRYSTAL_ORE.get()));
    public static final RegistryObject<BlockItem> AQUAMARINE_ORE_ITEM =
            ITEMS.register("aquamarine_sand_ore", () -> new ItemBlockAS(BlocksAS.AQUAMARINE_ORE.get()));
    public static final RegistryObject<BlockItem> STARMETAL_ORE_ITEM =
            ITEMS.register("starmetal_ore", () -> new ItemBlockAS(BlocksAS.STARMETAL_ORE.get()));
    public static final RegistryObject<BlockItem> STARMETAL_ITEM =
            ITEMS.register("starmetal", () -> new ItemBlockAS(BlocksAS.STARMETAL.get()));

    // Foliage
    public static final RegistryObject<BlockItem> GLOW_FLOWER_ITEM =
            ITEMS.register("glow_flower", () -> new ItemBlockAS(BlocksAS.GLOW_FLOWER.get()));

    // Crystal clusters
    public static final RegistryObject<BlockItem> CELESTIAL_CRYSTAL_CLUSTER_ITEM =
            ITEMS.register("celestial_crystal_cluster", () -> new ItemBlockAS(BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get()));
    public static final RegistryObject<BlockItem> GEM_CRYSTAL_CLUSTER_ITEM =
            ITEMS.register("gem_crystal_cluster", () -> new ItemBlockAS(BlocksAS.GEM_CRYSTAL_CLUSTER.get()));

    // Craftable functional blocks
    public static final RegistryObject<BlockItem> REFRACTION_TABLE_ITEM =
            ITEMS.register("refraction_table", () -> new ItemBlockAS(BlocksAS.REFRACTION_TABLE.get()));
    public static final RegistryObject<BlockItem> RITUAL_LINK_ITEM =
            ITEMS.register("ritual_link", () -> new ItemBlockAS(BlocksAS.RITUAL_LINK.get()));
    public static final RegistryObject<BlockItem> SPECTRAL_RELAY_ITEM =
            ITEMS.register("spectral_relay", () -> new ItemBlockAS(BlocksAS.SPECTRAL_RELAY.get()));

    // Special
    public static final RegistryObject<BlockItem> ILLUMINATOR_ITEM =
            ITEMS.register("illuminator", () -> new ItemBlockAS(BlocksAS.ILLUMINATOR.get()));

    // Tile entity blocks
    public static final RegistryObject<BlockItem> ALTAR_ITEM =
            ITEMS.register("altar", () -> new ItemBlockAS(BlocksAS.ALTAR.get()));
    public static final RegistryObject<BlockItem> ATTUNEMENT_ALTAR_ITEM =
            ITEMS.register("attunement_altar", () -> new ItemBlockAS(BlocksAS.ATTUNEMENT_ALTAR.get()));
    public static final RegistryObject<BlockItem> COLLECTOR_CRYSTAL_ITEM =
            ITEMS.register("collector_crystal", () -> new ItemBlockAS(BlocksAS.COLLECTOR_CRYSTAL.get()));
    public static final RegistryObject<BlockItem> CELESTIAL_COLLECTOR_CRYSTAL_ITEM =
            ITEMS.register("celestial_collector_crystal", () -> new ItemBlockAS(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get()));
    public static final RegistryObject<BlockItem> LENS_ITEM =
            ITEMS.register("lens", () -> new ItemBlockAS(BlocksAS.LENS.get()));
    public static final RegistryObject<BlockItem> PRISM_ITEM =
            ITEMS.register("prism", () -> new ItemBlockAS(BlocksAS.PRISM.get()));
    public static final RegistryObject<BlockItem> RELAY_ITEM =
            ITEMS.register("relay", () -> new ItemBlockAS(BlocksAS.RELAY.get()));
    public static final RegistryObject<BlockItem> WELL_ITEM =
            ITEMS.register("well", () -> new ItemBlockAS(BlocksAS.WELL.get()));
    public static final RegistryObject<BlockItem> INFUSER_ITEM =
            ITEMS.register("infuser", () -> new ItemBlockAS(BlocksAS.INFUSER.get()));
    public static final RegistryObject<BlockItem> RITUAL_PEDESTAL_ITEM =
            ITEMS.register("ritual_pedestal", () -> new ItemBlockAS(BlocksAS.RITUAL_PEDESTAL.get()));
    public static final RegistryObject<BlockItem> CHALICE_ITEM =
            ITEMS.register("chalice", () -> new ItemBlockAS(BlocksAS.CHALICE.get()));
    public static final RegistryObject<BlockItem> TELESCOPE_ITEM =
            ITEMS.register("telescope", () -> new ItemBlockAS(BlocksAS.TELESCOPE.get()));
    public static final RegistryObject<BlockItem> GATEWAY_ITEM =
            ITEMS.register("gateway", () -> new ItemBlockAS(BlocksAS.GATEWAY.get()));
    public static final RegistryObject<BlockItem> FOUNTAIN_ITEM =
            ITEMS.register("fountain", () -> new ItemBlockAS(BlocksAS.FOUNTAIN.get()));
    public static final RegistryObject<BlockItem> OBSERVATORY_ITEM =
            ITEMS.register("observatory", () -> new ItemBlockAS(BlocksAS.OBSERVATORY.get()));
    public static final RegistryObject<BlockItem> TREE_BEACON_ITEM =
            ITEMS.register("tree_beacon", () -> new ItemBlockAS(BlocksAS.TREE_BEACON.get()));
}
