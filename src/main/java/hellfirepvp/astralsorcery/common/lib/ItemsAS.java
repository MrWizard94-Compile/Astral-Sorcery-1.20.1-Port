package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.item.ItemIlluminationPowder;
import hellfirepvp.astralsorcery.common.item.ItemStardust;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCelestialCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.ItemRockCrystalSimple;
import hellfirepvp.astralsorcery.common.item.gem.ItemAquamarine;
import hellfirepvp.astralsorcery.common.item.gem.ItemPerkGem;
import hellfirepvp.astralsorcery.common.item.wand.ItemWand;
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

    // ---- Crystals ----
    public static final RegistryObject<ItemRockCrystalSimple> ROCK_CRYSTAL =
            ITEMS.register("rock_crystal", ItemRockCrystalSimple::new);
    public static final RegistryObject<ItemCelestialCrystal> CELESTIAL_CRYSTAL =
            ITEMS.register("celestial_crystal", ItemCelestialCrystal::new);

    // ---- Materials ----
    public static final RegistryObject<ItemAquamarine> AQUAMARINE =
            ITEMS.register("aquamarine", ItemAquamarine::new);
    public static final RegistryObject<ItemStardust> STARDUST =
            ITEMS.register("stardust", ItemStardust::new);
    public static final RegistryObject<ItemIlluminationPowder> ILLUMINATION_POWDER =
            ITEMS.register("illumination_powder", ItemIlluminationPowder::new);

    // ---- Perk Gems ----
    public static final RegistryObject<ItemPerkGem> PERK_GEM_DAY =
            ITEMS.register("perk_gem_day", () -> new ItemPerkGem(ItemPerkGem.GemType.DAY));
    public static final RegistryObject<ItemPerkGem> PERK_GEM_NIGHT =
            ITEMS.register("perk_gem_night", () -> new ItemPerkGem(ItemPerkGem.GemType.NIGHT));
    public static final RegistryObject<ItemPerkGem> PERK_GEM_SKY =
            ITEMS.register("perk_gem_sky", () -> new ItemPerkGem(ItemPerkGem.GemType.SKY));

    // TODO: Add remaining items as they are implemented:
    // Crystal tools (pickaxe, axe, shovel, sword)
    // Constellation papers, attuned crystals
    // Parchment, infused glass, resonating gem
    // Mantles (constellation cloaks)
    // Seals, knowledge fragments
}
