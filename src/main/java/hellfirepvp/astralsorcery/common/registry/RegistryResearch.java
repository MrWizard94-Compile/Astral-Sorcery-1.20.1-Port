/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.resource.query.SpriteQuery;
import hellfirepvp.astralsorcery.common.data.journal.*;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.item.ItemResonator;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.Locale;
import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryResearch
 * Created by HellFirePvP
 * Date: 13.04.2020 / 15:22
 * Ported to 1.20.1
 */
public class RegistryResearch {

    public static void init() {
        registerDiscovery();
        registerCrafting();
        registerAttunement();
        registerConstellation();
        registerRadiance();
    }

    private static void registerRadiance() {
        ResearchNode resAttuneCrystalTrait = new ResearchNode(ItemsAS.ROCK_CRYSTAL.get(), "ATT_TRAIT", 0, 0)
                .addPage(text("ATT_TRAIT.1"))
                .addPage(text("ATT_TRAIT.2"))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resObservatory = new ResearchNode(BlocksAS.OBSERVATORY.get(), "OBSERVATORY", 1.5F, 0.25F)
                .addPage(text("OBSERVATORY.1"))
                .addPage(recipe(BlocksAS.OBSERVATORY.get()))
                .addTomeLookup(BlocksAS.OBSERVATORY.get(), 1, ResearchProgression.RADIANCE)
                .addPage(text("OBSERVATORY.3"))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resIrradiantStars = new ResearchNode(new ItemLike[] {
                ItemsAS.SHIFTING_STAR_AEVITAS.get(),
                ItemsAS.SHIFTING_STAR_ARMARA.get(),
                ItemsAS.SHIFTING_STAR_DISCIDIA.get(),
                ItemsAS.SHIFTING_STAR_EVORSIO.get(),
                ItemsAS.SHIFTING_STAR_VICIO.get()
        }, "ENH_SHIFTING_STAR", 3.25F, 0.5F)
                .addPage(text("ENH_SHIFTING_STAR.1"))
                .addPage(recipe(ItemsAS.SHIFTING_STAR_AEVITAS.get()))
                .addTomeLookup(ItemsAS.SHIFTING_STAR_AEVITAS.get(), 1, ResearchProgression.RADIANCE)
                .addPage(recipe(ItemsAS.SHIFTING_STAR_ARMARA.get()))
                .addTomeLookup(ItemsAS.SHIFTING_STAR_ARMARA.get(), 2, ResearchProgression.RADIANCE)
                .addPage(recipe(ItemsAS.SHIFTING_STAR_DISCIDIA.get()))
                .addTomeLookup(ItemsAS.SHIFTING_STAR_DISCIDIA.get(), 3, ResearchProgression.RADIANCE)
                .addPage(recipe(ItemsAS.SHIFTING_STAR_EVORSIO.get()))
                .addTomeLookup(ItemsAS.SHIFTING_STAR_EVORSIO.get(), 4, ResearchProgression.RADIANCE)
                .addPage(recipe(ItemsAS.SHIFTING_STAR_VICIO.get()))
                .addTomeLookup(ItemsAS.SHIFTING_STAR_VICIO.get(), 5, ResearchProgression.RADIANCE)
                .register(ResearchProgression.RADIANCE);

        ResearchNode resRelayCrafting = new ResearchNode(BlocksAS.ALTAR.get(), "CRAFTING_FOCUS_HINT", 2.5F, 2F)
                .addPage(text("CRAFTING_FOCUS_HINT.1"))
                .addPage(text("CRAFTING_FOCUS_HINT.2"))
                .addPage(text("CRAFTING_FOCUS_HINT.3"))
                .register(ResearchProgression.RADIANCE);

        // In the port mantles are split per-constellation; use MANTLE_AEVITAS as representative icon.
        ResearchNode resMantle = new ResearchNode(ItemsAS.MANTLE_AEVITAS.get(), "ATT_CAPE", 1.25F, 2.5F)
                .addPage(text("ATT_CAPE.1"))
                .addPage(recipe(stack -> !stack.isEmpty() && stack.is(ItemsAS.MANTLE_AEVITAS.get().asItem())))
                .addTomeLookup(ItemsAS.MANTLE_AEVITAS.get(), 1, ResearchProgression.RADIANCE)
                .addPage(text("ATT_CAPE.3"))
                .addPage(text("ATT_CAPE.4"))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resChalice = new ResearchNode(BlocksAS.CHALICE.get(), "C_CHALICE", 3, 3)
                .addPage(text("C_CHALICE.1"))
                .addPage(recipe(BlocksAS.CHALICE.get()))
                .addTomeLookup(BlocksAS.CHALICE.get(), 1, ResearchProgression.RADIANCE)
                .addPage(text("C_CHALICE.3"))
                .addPage(text("C_CHALICE.4"))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resFountain = new ResearchNode(BlocksAS.FOUNTAIN.get(), "BORE_CORE", 2.25F, 4F)
                .addPage(text("BORE_CORE.1"))
                .addPage(recipe(BlocksAS.FOUNTAIN.get()))
                .addTomeLookup(BlocksAS.FOUNTAIN.get(), 1, ResearchProgression.RADIANCE)
                .addPage(text("BORE_CORE.3"))
                .addPage(structure(null))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resLiquidBore = new ResearchNode(BlocksAS.FOUNTAIN_PRIME_LIQUID.get(), "BORE_HEAD_LIQUID", 1.5F, 5F)
                .addPage(text("BORE_HEAD_LIQUID.1"))
                .addPage(recipe(BlocksAS.FOUNTAIN_PRIME_LIQUID.get()))
                .addTomeLookup(BlocksAS.FOUNTAIN_PRIME_LIQUID.get(), 1, ResearchProgression.RADIANCE)
                .addPage(text("BORE_HEAD_LIQUID.3"))
                .addPage(text("BORE_HEAD_LIQUID.4"))
                .register(ResearchProgression.RADIANCE);

        ItemStack liquidResonator = new ItemStack(ItemsAS.RESONATOR.get());
        ItemResonator.applyUpgrade(liquidResonator, ItemResonator.ResonatorUpgrade.STARLIGHT);
        ItemResonator.applyUpgrade(liquidResonator, ItemResonator.ResonatorUpgrade.FLUID_FIELDS);
        ResearchNode resLiquidResonator = new ResearchNode(liquidResonator, "ICHOSIC", 0, 4.5F)
                .addPage(text("ICHOSIC.1"))
                .addPage(recipe(stack -> !stack.isEmpty() && stack.is(ItemsAS.RESONATOR.get().asItem())
                        && ItemResonator.getSelectedUpgrade(stack) == ItemResonator.ResonatorUpgrade.FLUID_FIELDS))
                .register(ResearchProgression.RADIANCE);

        ResearchNode resVortexBore = new ResearchNode(BlocksAS.FOUNTAIN_PRIME_VORTEX.get(), "BORE_HEAD_VORTEX", 3.5F, 4.75F)
                .addPage(text("BORE_HEAD_VORTEX.1"))
                .addPage(recipe(BlocksAS.FOUNTAIN_PRIME_VORTEX.get()))
                .addTomeLookup(BlocksAS.FOUNTAIN_PRIME_VORTEX.get(), 1, ResearchProgression.RADIANCE)
                .addPage(text("BORE_HEAD_VORTEX.3"))
                .register(ResearchProgression.RADIANCE);

        resIrradiantStars.addSourceConnectionFrom(resRelayCrafting);
        resMantle.addSourceConnectionFrom(resRelayCrafting);
        resObservatory.addSourceConnectionFrom(resRelayCrafting);
        resAttuneCrystalTrait.addSourceConnectionFrom(resObservatory);
        resChalice.addSourceConnectionFrom(resRelayCrafting);
        resFountain.addSourceConnectionFrom(resChalice);
        resVortexBore.addSourceConnectionFrom(resFountain);
        resLiquidBore.addSourceConnectionFrom(resFountain);
        resLiquidResonator.addSourceConnectionFrom(resLiquidBore);
    }

    private static void registerConstellation() {
        ResearchNode resLenses = new ResearchNode(ItemsAS.GLASS_LENS.get(), "LENSES_EFFECTS", 6.25F, 1.75F)
                .addPage(text("LENSES_EFFECTS.1"))
                .addPage(text("LENSES_EFFECTS.2"))
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensFire = new ResearchNode(ItemsAS.COLORED_LENS_FIRE.get(), "IGNITION_LENS", 5.5F, 0.5F)
                .addPage(text("IGNITION_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_FIRE.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_FIRE.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensBreak = new ResearchNode(ItemsAS.COLORED_LENS_BREAK.get(), "BREAK_LENS", 6.75F, 0.25F)
                .addPage(text("BREAK_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_BREAK.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_BREAK.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensDamage = new ResearchNode(ItemsAS.COLORED_LENS_DAMAGE.get(), "DAMAGE_LENS", 7.5F, 1.25F)
                .addPage(text("DAMAGE_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_DAMAGE.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_DAMAGE.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensPush = new ResearchNode(ItemsAS.COLORED_LENS_PUSH.get(), "PUSH_LENS", 7.25F, 2.25F)
                .addPage(text("PUSH_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_PUSH.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_PUSH.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensRegeneration = new ResearchNode(ItemsAS.COLORED_LENS_REGENERATION.get(), "REGENERATION_LENS", 6.75F, 3F)
                .addPage(text("REGENERATION_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_REGENERATION.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_REGENERATION.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensGrowth = new ResearchNode(ItemsAS.COLORED_LENS_GROWTH.get(), "GROWTH_LENS", 5.75F, 2.75F)
                .addPage(text("GROWTH_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_GROWTH.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_GROWTH.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resColoredLensSpectral = new ResearchNode(ItemsAS.COLORED_LENS_SPECTRAL.get(), "SPECTRAL_LENS", 4.75F, 2)
                .addPage(text("SPECTRAL_LENS.1"))
                .addPage(recipe(ItemsAS.COLORED_LENS_SPECTRAL.get()))
                .addTomeLookup(ItemsAS.COLORED_LENS_SPECTRAL.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resInfuser = new ResearchNode(BlocksAS.INFUSER.get(), "INFUSER", 2, 1.75F)
                .addPage(text("INFUSER.1"))
                .addPage(recipe(BlocksAS.INFUSER.get()))
                .addTomeLookup(BlocksAS.INFUSER.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(text("INFUSER.3"))
                .addPage(structure(null))
                .addPage(recipeInfusion(ItemsAS.RESONATING_GEM.get()))
                .addTomeLookup(ItemsAS.RESONATING_GEM.get(), 4, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resEnchantmentAmulet = new ResearchNode(ItemsAS.ENCHANTMENT_AMULET.get(), "ENCHANTMENT_AMULET", 1.75F, 3.25F)
                .addPage(text("ENCHANTMENT_AMULET.1"))
                .addPage(JournalPageRecipe.fromName(AstralSorcery.key("altar/enchantment_amulet_init")))
                .addTomeLookup(ItemsAS.ENCHANTMENT_AMULET.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(text("ENCHANTMENT_AMULET.3"))
                .addPage(JournalPageRecipe.fromName(AstralSorcery.key("altar/enchantment_amulet_reroll")))
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resRitualLink = new ResearchNode(BlocksAS.RITUAL_LINK.get(), "RITUAL_LINK", 0.5F, 3.5F)
                .addPage(text("RITUAL_LINK.1"))
                .addPage(recipe(BlocksAS.RITUAL_LINK.get()))
                .addTomeLookup(BlocksAS.RITUAL_LINK.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resIlluminationWand = new ResearchNode(ItemsAS.ILLUMINATION_WAND.get(), "ILLUMINATION_WAND", 0.25F, 2.5F)
                .addPage(text("ILLUMINATION_WAND.1"))
                .addPage(recipe(ItemsAS.ILLUMINATION_WAND.get()))
                .addTomeLookup(ItemsAS.ILLUMINATION_WAND.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(text("ILLUMINATION_WAND.3"))
                .register(ResearchProgression.CONSTELLATION);

        // INFUSED_CRYSTAL tools not ported; use CRYSTAL tools as placeholders for icon/lookup.
        ResearchNode resInfusedTools = new ResearchNode(new ItemLike[] {
                ItemsAS.CRYSTAL_SWORD.get(),
                ItemsAS.CRYSTAL_PICKAXE.get(),
                ItemsAS.CRYSTAL_AXE.get(),
                ItemsAS.CRYSTAL_SHOVEL.get()
        }, "CHARGED_TOOLS", 0.25F, 1.25F)
                .addPage(text("CHARGED_TOOLS.1"))
                .addPage(text("CHARGED_TOOLS.2"))
                .addPage(recipeInfusion(ItemsAS.CRYSTAL_SWORD.get()))
                .addTomeLookup(ItemsAS.CRYSTAL_SWORD.get(), 2, ResearchProgression.CONSTELLATION)
                .addPage(recipeInfusion(ItemsAS.CRYSTAL_PICKAXE.get()))
                .addTomeLookup(ItemsAS.CRYSTAL_PICKAXE.get(), 3, ResearchProgression.CONSTELLATION)
                .addPage(recipeInfusion(ItemsAS.CRYSTAL_AXE.get()))
                .addTomeLookup(ItemsAS.CRYSTAL_AXE.get(), 4, ResearchProgression.CONSTELLATION)
                .addPage(recipeInfusion(ItemsAS.CRYSTAL_SHOVEL.get()))
                .addTomeLookup(ItemsAS.CRYSTAL_SHOVEL.get(), 5, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resTreeBeacon = new ResearchNode(BlocksAS.TREE_BEACON.get(), "TREEBEACON", 1.25F, 0.5F)
                .addPage(text("TREEBEACON.1"))
                .addPage(recipe(BlocksAS.TREE_BEACON.get()))
                .addTomeLookup(BlocksAS.TREE_BEACON.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(text("TREEBEACON.3"))
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resPrism = new ResearchNode(BlocksAS.PRISM.get(), "PRISM", 2.75F, 0)
                .addPage(text("PRISM.1"))
                .addPage(recipe(BlocksAS.PRISM.get()))
                .addTomeLookup(BlocksAS.PRISM.get(), 1, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resEngravingTable = new ResearchNode(BlocksAS.REFRACTION_TABLE.get(), "DRAWING_TABLE", 3.5F, 1)
                .addPage(text("DRAWING_TABLE.1"))
                .addPage(recipe(BlocksAS.REFRACTION_TABLE.get()))
                .addTomeLookup(BlocksAS.REFRACTION_TABLE.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(text("DRAWING_TABLE.3"))
                .addPage(text("DRAWING_TABLE.4"))
                .addPage(recipe(ItemsAS.INFUSED_GLASS.get()))
                .addTomeLookup(ItemsAS.INFUSED_GLASS.get(), 4, ResearchProgression.CONSTELLATION)
                .addPage(text("DRAWING_TABLE.6"))
                .register(ResearchProgression.CONSTELLATION);

        // In port there is only one altar block; ALTAR_RADIANCE maps to BlocksAS.ALTAR
        ResearchNode resAltar4 = new ResearchNode(BlocksAS.ALTAR.get(), "ALTAR4", 3.5F, 3)
                .addPage(text("ALTAR4.1"))
                .addPage(recipe(BlocksAS.ALTAR.get()))
                .addTomeLookup(BlocksAS.ALTAR.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(structure(null))
                .addPage(text("ALTAR4.4"))
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resCollectorCrystal = new ResearchNode(BlocksAS.COLLECTOR_CRYSTAL.get(), "COLL_CRYSTAL", 2.75F, 3.75F)
                .addPage(text("COLL_CRYSTAL.1"))
                .addPage(recipe(BlocksAS.COLLECTOR_CRYSTAL.get()))
                .addTomeLookup(BlocksAS.COLLECTOR_CRYSTAL.get(), 1, ResearchProgression.CONSTELLATION)
                .addTomeLookup(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get(), 1, ResearchProgression.CONSTELLATION)
                .addPage(text("COLL_CRYSTAL.3"))
                .addPage(text("COLL_CRYSTAL.4"))
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resCelestialCrystalCluster = new ResearchNode(BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get(), "CEL_CRYSTAL_GROW", 6.25F, 4)
                .addPage(text("CEL_CRYSTAL_GROW.1"))
                .addPage(text("CEL_CRYSTAL_GROW.2"))
                .addPage(text("CEL_CRYSTAL_GROW.3"))
                .addTomeLookup(BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get(), 0, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resCelestialCrystals = new ResearchNode(ItemsAS.CELESTIAL_CRYSTAL.get(), "CEL_CRYSTALS", 5, 3.75F)
                .addPage(text("CEL_CRYSTALS.1"))
                .addTomeLookup(ItemsAS.CELESTIAL_CRYSTAL.get(), 0, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.CONSTELLATION);

        ResearchNode resEnhancedCollectorCrystal = new ResearchNode(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get(), "ENHANCED_COLLECTOR", 4, 4.5F)
                .addPage(text("ENHANCED_COLLECTOR.1"))
                .addPage(structure(null))
                .register(ResearchProgression.CONSTELLATION);

        resColoredLensFire.addSourceConnectionFrom(resLenses);
        resColoredLensBreak.addSourceConnectionFrom(resLenses);
        resColoredLensDamage.addSourceConnectionFrom(resLenses);
        resColoredLensPush.addSourceConnectionFrom(resLenses);
        resColoredLensRegeneration.addSourceConnectionFrom(resLenses);
        resColoredLensGrowth.addSourceConnectionFrom(resLenses);
        resColoredLensSpectral.addSourceConnectionFrom(resLenses);
        resColoredLensSpectral.addSourceConnectionFrom(resInfuser);
        resEngravingTable.addSourceConnectionFrom(resColoredLensSpectral);

        resEnchantmentAmulet.addSourceConnectionFrom(resInfuser);
        resRitualLink.addSourceConnectionFrom(resInfuser);
        resIlluminationWand.addSourceConnectionFrom(resInfuser);
        resInfusedTools.addSourceConnectionFrom(resInfuser);
        resTreeBeacon.addSourceConnectionFrom(resInfuser);
        resPrism.addSourceConnectionFrom(resInfuser);
        resAltar4.addSourceConnectionFrom(resInfuser);
        resEngravingTable.addSourceConnectionFrom(resInfuser);
        resCollectorCrystal.addSourceConnectionFrom(resInfuser);

        resCelestialCrystals.addSourceConnectionFrom(resCelestialCrystalCluster);
        resAltar4.addSourceConnectionFrom(resCelestialCrystals);
        resEnhancedCollectorCrystal.addSourceConnectionFrom(resCollectorCrystal);
        resEnhancedCollectorCrystal.addSourceConnectionFrom(resCelestialCrystals);
    }

    private static void registerAttunement() {
        ResearchNode resTelescope = new ResearchNode(BlocksAS.TELESCOPE.get(), "TELESCOPE", 0.5F, 0)
                .addPage(text("TELESCOPE.1"))
                .addPage(recipe(BlocksAS.TELESCOPE.get()))
                .addTomeLookup(BlocksAS.TELESCOPE.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(text("TELESCOPE.3"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resKnowledgeShare = new ResearchNode(ItemsAS.KNOWLEDGE_SHARE.get(), "KNOWLEDGE_SHARE", 2.5F, 0.25F)
                .addPage(text("KNOWLEDGE_SHARE.1"))
                .addPage(recipe(ItemsAS.KNOWLEDGE_SHARE.get()))
                .addTomeLookup(ItemsAS.KNOWLEDGE_SHARE.get(), 1, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resLens = new ResearchNode(BlocksAS.LENS.get(), "LENS", 0, 1.25F)
                .addPage(text("LENS.1"))
                .addPage(recipe(BlocksAS.LENS.get()))
                .addTomeLookup(BlocksAS.LENS.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(text("LENS.3"))
                .addPage(text("LENS.4"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resLinkingTool = new ResearchNode(ItemsAS.LINKING_TOOL.get(), "LINKTOOL", 0.25F, 2.25F)
                .addPage(text("LINKTOOL.1"))
                .addPage(recipe(ItemsAS.LINKING_TOOL.get()))
                .addTomeLookup(ItemsAS.LINKING_TOOL.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(text("LINKTOOL.3"))
                .addPage(text("LINKTOOL.4"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resStarlightNetwork = new ResearchNode(BlocksAS.LENS.get(), "STARLIGHT_NETWORK", 1.5F, 1)
                .addPage(text("STARLIGHT_NETWORK.1"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resTransmutationOres = new ResearchNode(new ItemLike[] {
                Blocks.MAGMA_BLOCK,
                Blocks.SAND,
                Blocks.DIAMOND_ORE,
                Blocks.NETHER_WART_BLOCK,
                Blocks.PUMPKIN,
                Blocks.SANDSTONE
        }, "TRANSMUTATION_ORES", 2.75F, 1.5F)
                .addPage(text("TRANSMUTATION_ORES.1"))
                .addPage(recipeTransmutation(Blocks.CLAY))
                .addPage(recipeTransmutation(Blocks.EMERALD_ORE))
                .addPage(recipeTransmutation(Blocks.CAKE))
                .addPage(recipeTransmutation(Blocks.LAPIS_BLOCK))
                .addPage(recipeTransmutation(Blocks.END_STONE))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resStarmetalOre = new ResearchNode(BlocksAS.STARMETAL_ORE.get(), "STARMETAL_ORE", 4.25F, 1.75F)
                .addPage(text("STARMETAL_ORE.1"))
                .addPage(recipeTransmutation(BlocksAS.STARMETAL_ORE.get()))
                .addTomeLookup(ItemsAS.STARMETAL_INGOT.get(), 0, ResearchProgression.ATTUNEMENT)
                .addTomeLookup(BlocksAS.STARMETAL_ORE.get(), 0, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resCuttingTool = new ResearchNode(ItemsAS.CHISEL.get(), "CUTTING_TOOL", 5, 1)
                .addPage(text("CUTTING_TOOL.1"))
                .addPage(recipe(ItemsAS.CHISEL.get()))
                .addTomeLookup(ItemsAS.CHISEL.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(text("CUTTING_TOOL.3"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resStardust = new ResearchNode(ItemsAS.STARDUST.get(), "STARDUST", 6, 0.5F)
                .addPage(text("STARDUST.1"))
                .addPage(text("STARDUST.2"))
                .addTomeLookup(ItemsAS.STARDUST.get(), 0, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ItemStack structureResonator = new ItemStack(ItemsAS.RESONATOR.get());
        ItemResonator.applyUpgrade(structureResonator, ItemResonator.ResonatorUpgrade.STARLIGHT);
        ItemResonator.applyUpgrade(structureResonator, ItemResonator.ResonatorUpgrade.AREA_SIZE);
        ResearchNode resResonatorStructure = new ResearchNode(structureResonator, "RESONATOR_AREA_SIZE", 6.5F, 2)
                .addPage(text("RESONATOR_AREA_SIZE.1"))
                .addPage(recipe(stack -> !stack.isEmpty() && stack.is(ItemsAS.RESONATOR.get().asItem())
                        && ItemResonator.getSelectedUpgrade(stack) == ItemResonator.ResonatorUpgrade.AREA_SIZE))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resCelestialGateway = new ResearchNode(BlocksAS.GATEWAY.get(), "CELESTIAL_GATEWAY", 7.5F, 1.5F)
                .addPage(text("CELESTIAL_GATEWAY.1"))
                .addPage(recipe(BlocksAS.GATEWAY.get()))
                .addTomeLookup(BlocksAS.GATEWAY.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(text("CELESTIAL_GATEWAY.3"))
                .addPage(structure(null))
                .addPage(text("CELESTIAL_GATEWAY.5"))
                .register(ResearchProgression.ATTUNEMENT);

        // Single ALTAR block in port; maps to all 1.16 altar tiers
        ResearchNode resAltar3 = new ResearchNode(BlocksAS.ALTAR.get(), "ALTAR3", 7.25F, 0)
                .addPage(text("ALTAR3.1"))
                .addPage(recipe(BlocksAS.ALTAR.get()))
                .addTomeLookup(BlocksAS.ALTAR.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(structure(null))
                .addPage(text("ALTAR3.4"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resAttunePlayer = new ResearchNode(BlocksAS.ATTUNEMENT_ALTAR.get(), "ATT_PLAYER", 3.75F, 2.75F)
                .addPage(text("ATT_PLAYER.1"))
                .addPage(text("ATT_PLAYER.2"))
                .addPage(recipe(BlocksAS.ATTUNEMENT_ALTAR.get()))
                .addTomeLookup(BlocksAS.ATTUNEMENT_ALTAR.get(), 2, ResearchProgression.ATTUNEMENT)
                .addPage(text("ATT_PLAYER.4"))
                .addPage(structure(null))
                .addPage(text("ATT_PLAYER.6"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resPerks = new ResearchNode(BlocksAS.SPECTRAL_RELAY.get(), "ATT_PERKS", 4.5F, 3.5F)
                .addPage(text("ATT_PERKS.1"))
                .addPage(text("ATT_PERKS.2"))
                .addPage(text("ATT_PERKS.3"))
                .addPage(text("ATT_PERKS.4"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resShiftingStar = new ResearchNode(ItemsAS.SHIFTING_STAR.get(), "SHIFT_STAR", 5.75F, 3.25F)
                .addPage(text("SHIFT_STAR.1"))
                .addPage(recipe(ItemsAS.SHIFTING_STAR.get()))
                .addTomeLookup(ItemsAS.SHIFTING_STAR.get(), 1, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resPerkSeal = new ResearchNode(ItemsAS.PERK_SEAL.get(), "ATT_PERKS_SEAL", 5.5F, 4.5F)
                .addPage(text("ATT_PERKS_SEAL.1"))
                .addPage(recipe(ItemsAS.PERK_SEAL.get()))
                .addTomeLookup(ItemsAS.PERK_SEAL.get(), 1, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resPerkGems = new ResearchNode(new ItemLike[] {
                ItemsAS.PERK_GEM_DAY.get(),
                ItemsAS.PERK_GEM_NIGHT.get(),
                ItemsAS.PERK_GEM_SKY.get()
        }, "ATT_PERK_GEMS", 4.75F, 5.25F)
                .addPage(text("ATT_PERK_GEMS.1"))
                .addPage(text("ATT_PERK_GEMS.2"))
                .addPage(text("ATT_PERK_GEMS.3"))
                .addTomeLookup(ItemsAS.PERK_GEM_DAY.get(), 1, ResearchProgression.ATTUNEMENT)
                .addTomeLookup(ItemsAS.PERK_GEM_NIGHT.get(), 1, ResearchProgression.ATTUNEMENT)
                .addTomeLookup(ItemsAS.PERK_GEM_SKY.get(), 1, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resAttuneCrystal = new ResearchNode(ItemsAS.ROCK_CRYSTAL.get(), "ATT_CRYSTAL", 3.5F, 4)
                .addPage(text("ATT_CRYSTAL.1"))
                .addPage(text("ATT_CRYSTAL.2"))
                .addTomeLookup(ItemsAS.ATTUNED_ROCK_CRYSTAL.get(), 0, ResearchProgression.ATTUNEMENT)
                .addTomeLookup(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get(), 0, ResearchProgression.CONSTELLATION)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resRitualPedestal = new ResearchNode(BlocksAS.RITUAL_PEDESTAL.get(), "RIT_PEDESTAL", 3, 5)
                .addPage(text("RIT_PEDESTAL.1"))
                .addPage(recipe(BlocksAS.RITUAL_PEDESTAL.get()))
                .addTomeLookup(BlocksAS.RITUAL_PEDESTAL.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(structure(null))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resRitualPedestalAcceleration = new ResearchNode(BlocksAS.RITUAL_PEDESTAL.get(), "PED_ACCEL", 1.75F, 5.5F)
                .addPage(text("PED_ACCEL.1"))
                .addPage(text("PED_ACCEL.2"))
                .addPage(text("PED_ACCEL.3"))
                .addPage(text("PED_ACCEL.4"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resAlignmentCharge = new ResearchNode(new SpriteQuery(AssetLoader.TextureLocation.EFFECT, 6, 8, "relay_flare"),
                "QUICK_CHARGE", 0.75F, 4.5F)
                .addPage(text("QUICK_CHARGE.1"))
                .addPage(text("QUICK_CHARGE.2"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resToolChanneling = new ResearchNode(BlocksAS.SPECTRAL_RELAY.get(), "TOOL_CHANNEL", 1.25F, 3.25F)
                .addPage(text("TOOL_CHANNEL.1"))
                .addPage(text("TOOL_CHANNEL.2"))
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resBlinkWand = new ResearchNode(ItemsAS.BLINK_WAND.get(), "TRAVERSAL_WAND", 2.25F, 3.5F)
                .addPage(text("TRAVERSAL_WAND.1"))
                .addPage(recipe(ItemsAS.BLINK_WAND.get()))
                .addTomeLookup(ItemsAS.BLINK_WAND.get(), 1, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resGrappleWand = new ResearchNode(ItemsAS.GRAPPLE_WAND.get(), "GRAPPLE_WAND", 2.5F, 2.5F)
                .addPage(text("GRAPPLE_WAND.1"))
                .addPage(recipe(ItemsAS.GRAPPLE_WAND.get()))
                .addTomeLookup(ItemsAS.GRAPPLE_WAND.get(), 1, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        ResearchNode resToolWands = new ResearchNode(new ItemLike[] {
                ItemsAS.ARCHITECT_WAND.get(),
                ItemsAS.EXCHANGE_WAND.get()
        }, "TOOL_WANDS", 1.5F, 2.25F)
                .addPage(text("TOOL_WANDS.1"))
                .addPage(recipe(ItemsAS.ARCHITECT_WAND.get()))
                .addTomeLookup(ItemsAS.ARCHITECT_WAND.get(), 1, ResearchProgression.ATTUNEMENT)
                .addPage(text("TOOL_WANDS.3"))
                .addPage(recipe(ItemsAS.EXCHANGE_WAND.get()))
                .addTomeLookup(ItemsAS.EXCHANGE_WAND.get(), 3, ResearchProgression.ATTUNEMENT)
                .register(ResearchProgression.ATTUNEMENT);

        resStarlightNetwork.addSourceConnectionFrom(resLens);
        resStarlightNetwork.addSourceConnectionFrom(resLinkingTool);
        resTransmutationOres.addSourceConnectionFrom(resStarlightNetwork);
        resStarmetalOre.addSourceConnectionFrom(resTransmutationOres);
        resCuttingTool.addSourceConnectionFrom(resStarmetalOre);
        resStardust.addSourceConnectionFrom(resCuttingTool);
        resResonatorStructure.addSourceConnectionFrom(resStardust);
        resCelestialGateway.addSourceConnectionFrom(resStardust);
        resAltar3.addSourceConnectionFrom(resStardust);

        resAttunePlayer.addSourceConnectionFrom(resStarmetalOre);
        resPerks.addSourceConnectionFrom(resAttunePlayer);
        resShiftingStar.addSourceConnectionFrom(resPerks);
        resPerkSeal.addSourceConnectionFrom(resPerks);
        resPerkGems.addSourceConnectionFrom(resPerks);
        resAttuneCrystal.addSourceConnectionFrom(resAttunePlayer);
        resRitualPedestal.addSourceConnectionFrom(resAttuneCrystal);
        resRitualPedestalAcceleration.addSourceConnectionFrom(resRitualPedestal);

        resToolChanneling.addSourceConnectionFrom(resAlignmentCharge);
        resBlinkWand.addSourceConnectionFrom(resToolChanneling);
        resGrappleWand.addSourceConnectionFrom(resToolChanneling);
        resToolWands.addSourceConnectionFrom(resToolChanneling);
    }

    private static void registerCrafting() {
        ResearchNode resHandTelescope = new ResearchNode(ItemsAS.HAND_TELESCOPE.get(), "HAND_TELESCOPE", 0, 1)
                .addPage(text("HAND_TELESCOPE.1"))
                .addPage(recipe(ItemsAS.GLASS_LENS.get()))
                .addTomeLookup(ItemsAS.GLASS_LENS.get(), 1, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(ItemsAS.HAND_TELESCOPE.get()))
                .addTomeLookup(ItemsAS.HAND_TELESCOPE.get(), 2, ResearchProgression.BASIC_CRAFT)
                .addPage(text("HAND_TELESCOPE.4"))
                .addPage(text("HAND_TELESCOPE.5"))
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resIlluminationPowder = new ResearchNode(ItemsAS.ILLUMINATION_POWDER.get(), "ILLUM_POWDER", 1, 2)
                .addPage(text("ILLUM_POWDER.1"))
                .addPage(recipe(ItemsAS.ILLUMINATION_POWDER.get()))
                .addTomeLookup(ItemsAS.ILLUMINATION_POWDER.get(), 1, ResearchProgression.BASIC_CRAFT)
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resNocturnalPowder = new ResearchNode(ItemsAS.NOCTURNAL_POWDER.get(), "NOC_POWDER", 0, 2.5F)
                .addPage(text("NOC_POWDER.1"))
                .addPage(recipe(ItemsAS.NOCTURNAL_POWDER.get()))
                .addTomeLookup(ItemsAS.NOCTURNAL_POWDER.get(), 1, ResearchProgression.BASIC_CRAFT)
                .addPage(text("NOC_POWDER.3"))
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resIlluminator = new ResearchNode(BlocksAS.ILLUMINATOR.get(), "ILLUMINATOR", 0.75F, 3)
                .addPage(text("ILLUMINATOR.1"))
                .addPage(recipe(BlocksAS.ILLUMINATOR.get()))
                .addTomeLookup(BlocksAS.ILLUMINATOR.get(), 1, ResearchProgression.BASIC_CRAFT)
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resRockCrystals = new ResearchNode(ItemsAS.ROCK_CRYSTAL.get(), "ROCK_CRYSTALS", 2, 1.5F)
                .addPage(text("ROCK_CRYSTALS.1"))
                .addPage(text("ROCK_CRYSTALS.2"))
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resCrystalGrowth = new ResearchNode(ItemsAS.ROCK_CRYSTAL.get(), "CRYSTAL_GROWTH", 3, 2.5F)
                .addPage(text("CRYSTAL_GROWTH.1"))
                .addPage(text("CRYSTAL_GROWTH.2"))
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resTools = new ResearchNode(new ItemLike[] {
                ItemsAS.CRYSTAL_SWORD.get(),
                ItemsAS.CRYSTAL_PICKAXE.get(),
                ItemsAS.CRYSTAL_AXE.get(),
                ItemsAS.CRYSTAL_SHOVEL.get()
        }, "TOOLS", 4.5F, 3)
                .addPage(text("TOOLS.1"))
                .addPage(text("TOOLS.3"))
                .addPage(recipe(ItemsAS.CRYSTAL_SWORD.get()))
                .addTomeLookup(ItemsAS.CRYSTAL_SWORD.get(), 2, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(ItemsAS.CRYSTAL_PICKAXE.get()))
                .addTomeLookup(ItemsAS.CRYSTAL_PICKAXE.get(), 3, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(ItemsAS.CRYSTAL_AXE.get()))
                .addTomeLookup(ItemsAS.CRYSTAL_AXE.get(), 4, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(ItemsAS.CRYSTAL_SHOVEL.get()))
                .addTomeLookup(ItemsAS.CRYSTAL_SHOVEL.get(), 5, ResearchProgression.BASIC_CRAFT)
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resLightwell = new ResearchNode(BlocksAS.WELL.get(), "WELL", 3, 1)
                .addPage(text("WELL.1"))
                .addPage(recipe(BlocksAS.WELL.get()))
                .addTomeLookup(BlocksAS.WELL.get(), 1, ResearchProgression.BASIC_CRAFT)
                .addPage(text("WELL.3"))
                .addPage(text("WELL.4"))
                .addPage(text("WELL.5"))
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resInfusedWood = new ResearchNode(new ItemLike[] {
                BlocksAS.INFUSED_WOOD.get(),
                BlocksAS.INFUSED_WOOD_ARCH.get(),
                BlocksAS.INFUSED_WOOD_COLUMN.get(),
                BlocksAS.INFUSED_WOOD_ENGRAVED.get(),
                BlocksAS.INFUSED_WOOD_ENRICHED.get(),
                BlocksAS.INFUSED_WOOD_PLANKS.get(),
                BlocksAS.INFUSED_WOOD_STAIRS.get(),
                BlocksAS.INFUSED_WOOD_SLAB.get()
        }, "INFUSED_WOOD", 4, 0)
                .addPage(text("INFUSED_WOOD.1"))
                .addTomeLookup(BlocksAS.INFUSED_WOOD.get(), 0, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_PLANKS.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_PLANKS.get(), 1, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_ARCH.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_ARCH.get(), 2, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_COLUMN.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_COLUMN.get(), 3, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_ENGRAVED.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_ENGRAVED.get(), 4, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_ENRICHED.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_ENRICHED.get(), 5, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_STAIRS.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_STAIRS.get(), 6, ResearchProgression.BASIC_CRAFT)
                .addPage(recipe(BlocksAS.INFUSED_WOOD_SLAB.get()))
                .addTomeLookup(BlocksAS.INFUSED_WOOD_SLAB.get(), 7, ResearchProgression.BASIC_CRAFT)
                .register(ResearchProgression.BASIC_CRAFT);

        // Single ALTAR block in port; maps to ALTAR_ATTUNEMENT tier in 1.16
        ResearchNode resAltar2 = new ResearchNode(BlocksAS.ALTAR.get(), "ALTAR2", 4, 1.5F)
                .addPage(text("ALTAR2.1"))
                .addPage(recipe(BlocksAS.ALTAR.get()))
                .addTomeLookup(BlocksAS.ALTAR.get(), 1, ResearchProgression.BASIC_CRAFT)
                .addPage(structure(null))
                .addPage(text("ALTAR2.4"))
                .register(ResearchProgression.BASIC_CRAFT);

        ItemStack starlightResonator = new ItemStack(ItemsAS.RESONATOR.get());
        ItemResonator.applyUpgrade(starlightResonator, ItemResonator.ResonatorUpgrade.STARLIGHT);
        ResearchNode resResonator = new ResearchNode(starlightResonator, "SKY_RESO", 5, 0.5F)
                .addPage(text("SKY_RESO.1"))
                .addPage(recipe(stack -> !stack.isEmpty() && stack.is(ItemsAS.RESONATOR.get().asItem())
                        && ItemResonator.getSelectedUpgrade(stack) == ItemResonator.ResonatorUpgrade.STARLIGHT))
                .addPage(text("SKY_RESO.2"))
                .addPage(text("SKY_RESO.3"))
                .register(ResearchProgression.BASIC_CRAFT);

        ResearchNode resSpectralRelay = new ResearchNode(BlocksAS.SPECTRAL_RELAY.get(), "SPEC_RELAY", 6, 1)
                .addPage(text("SPEC_RELAY.1"))
                .addPage(recipe(BlocksAS.SPECTRAL_RELAY.get()))
                .addTomeLookup(BlocksAS.SPECTRAL_RELAY.get(), 1, ResearchProgression.BASIC_CRAFT)
                .addPage(structure(null))
                .addPage(text("SPEC_RELAY.4"))
                .register(ResearchProgression.BASIC_CRAFT);

        resNocturnalPowder.addSourceConnectionFrom(resIlluminationPowder);
        resIlluminator.addSourceConnectionFrom(resIlluminationPowder);
        resCrystalGrowth.addSourceConnectionFrom(resRockCrystals);
        resTools.addSourceConnectionFrom(resCrystalGrowth);
        resLightwell.addSourceConnectionFrom(resRockCrystals);
        resInfusedWood.addSourceConnectionFrom(resLightwell);
        resAltar2.addSourceConnectionFrom(resLightwell);
        resResonator.addSourceConnectionFrom(resLightwell);
        resSpectralRelay.addSourceConnectionFrom(resResonator);
    }

    private static void registerDiscovery() {
        ResearchNode resWelcome = new ResearchNode(ItemsAS.TOME.get(), "WELCOME", 1, 1)
                .addPage(text("WELCOME.1"))
                .addPage(text("WELCOME.2"))
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resShrines = new ResearchNode(ItemsAS.ROCK_CRYSTAL.get(), "SHRINES", 2, 0)
                .addPage(text("SHRINES.1"))
                .addPage(text("SHRINES.2"))
                .addPage(text("SHRINES.3"))
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resConstellationPaper = new ResearchNode(ItemsAS.CONSTELLATION_PAPER.get(), "CPAPER", 3, 1)
                .addPage(text("CPAPER.1"))
                .addPage(recipe(ItemsAS.TOME.get()))
                .addPage(text("CPAPER.3"))
                .addPage(recipe(ItemsAS.PARCHMENT.get()))
                .addTomeLookup(ItemsAS.CONSTELLATION_PAPER.get(), 0, ResearchProgression.DISCOVERY)
                .addTomeLookup(ItemsAS.TOME.get(), 1, ResearchProgression.DISCOVERY)
                .addTomeLookup(ItemsAS.PARCHMENT.get(), 3, ResearchProgression.DISCOVERY)
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resOres = new ResearchNode(new ItemLike[] {
                BlocksAS.ROCK_CRYSTAL_ORE.get(),
                BlocksAS.AQUAMARINE_ORE.get()
        }, "ORES", 2, 2)
                .addPage(text("ORES.1"))
                .addPage(text("ORES.2"))
                .addTomeLookup(ItemsAS.AQUAMARINE.get(), 0, ResearchProgression.DISCOVERY)
                .addTomeLookup(ItemsAS.ROCK_CRYSTAL.get(), 0, ResearchProgression.DISCOVERY)
                .addTomeLookup(BlocksAS.AQUAMARINE_ORE.get(), 0, ResearchProgression.DISCOVERY)
                .addTomeLookup(BlocksAS.ROCK_CRYSTAL_ORE.get(), 0, ResearchProgression.DISCOVERY)
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resResonatingWand = new ResearchNode(ItemsAS.WAND.get(), "WAND", 3, 3)
                .addPage(text("WAND.1"))
                .addPage(recipeVanilla(ItemsAS.WAND.get()))
                .addTomeLookup(ItemsAS.WAND.get(), 1, ResearchProgression.DISCOVERY)
                .addPage(text("WAND.3"))
                .addPage(text("WAND.4"))
                .register(ResearchProgression.DISCOVERY);

        // In the port there is only one altar block; ALTAR_DISCOVERY maps to BlocksAS.ALTAR
        ResearchNode resAltar1 = new ResearchNode(BlocksAS.ALTAR.get(), "ALTAR1", 4, 2)
                .addPage(text("ALTAR1.1"))
                .addPage(text("ALTAR1.2"))
                .addPage(recipeTransmutation(BlocksAS.ALTAR.get()))
                .addTomeLookup(BlocksAS.ALTAR.get(), 1, ResearchProgression.DISCOVERY)
                .addPage(text("ALTAR1.4"))
                .addPage(text("ALTAR1.5"))
                .addPage(text("ALTAR1.6"))
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resMarbles = new ResearchNode(new ItemLike[] {
                BlocksAS.MARBLE_RAW.get(),
                BlocksAS.MARBLE_PILLAR.get(),
                BlocksAS.MARBLE_ARCH.get(),
                BlocksAS.MARBLE_BRICKS.get(),
                BlocksAS.MARBLE_CHISELED.get(),
                BlocksAS.MARBLE_ENGRAVED.get(),
                BlocksAS.MARBLE_RUNED.get(),
                BlocksAS.MARBLE_SLAB.get(),
                BlocksAS.MARBLE_STAIRS.get()
        }, "MARBLETYPES", 0, 2.5F)
                .addPage(text("MARBLETYPES.1"))
                .addPage(text("MARBLETYPES.2"))
                .addTomeLookup(BlocksAS.MARBLE_RAW.get(), 1, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_PILLAR.get()))
                .addTomeLookup(BlocksAS.MARBLE_PILLAR.get(), 2, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_ARCH.get()))
                .addTomeLookup(BlocksAS.MARBLE_ARCH.get(), 3, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_BRICKS.get()))
                .addTomeLookup(BlocksAS.MARBLE_BRICKS.get(), 4, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_CHISELED.get()))
                .addTomeLookup(BlocksAS.MARBLE_CHISELED.get(), 5, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_ENGRAVED.get()))
                .addTomeLookup(BlocksAS.MARBLE_ENGRAVED.get(), 6, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_RUNED.get()))
                .addTomeLookup(BlocksAS.MARBLE_RUNED.get(), 7, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_SLAB.get()))
                .addTomeLookup(BlocksAS.MARBLE_SLAB.get(), 8, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.MARBLE_STAIRS.get()))
                .addTomeLookup(BlocksAS.MARBLE_STAIRS.get(), 9, ResearchProgression.DISCOVERY)
                .register(ResearchProgression.DISCOVERY);

        ResearchNode resSootyMarble = new ResearchNode(new ItemLike[] {
                BlocksAS.BLACK_MARBLE_RAW.get(),
                BlocksAS.BLACK_MARBLE_PILLAR.get(),
                BlocksAS.BLACK_MARBLE_ARCH.get(),
                BlocksAS.BLACK_MARBLE_BRICKS.get(),
                BlocksAS.BLACK_MARBLE_CHISELED.get(),
                BlocksAS.BLACK_MARBLE_ENGRAVED.get(),
                BlocksAS.BLACK_MARBLE_RUNED.get(),
                BlocksAS.BLACK_MARBLE_SLAB.get(),
                BlocksAS.BLACK_MARBLE_STAIRS.get()
        }, "SOOTYMARBLE", 1, 3)
                .addPage(text("SOOTYMARBLE.1"))
                .addPage(text("SOOTYMARBLE.2"))
                .addPage(recipe(BlocksAS.BLACK_MARBLE_RAW.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_RAW.get(), 2, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_PILLAR.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_PILLAR.get(), 3, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_ARCH.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_ARCH.get(), 4, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_BRICKS.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_BRICKS.get(), 5, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_CHISELED.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_CHISELED.get(), 6, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_ENGRAVED.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_ENGRAVED.get(), 7, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_RUNED.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_RUNED.get(), 8, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_SLAB.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_SLAB.get(), 9, ResearchProgression.DISCOVERY)
                .addPage(recipe(BlocksAS.BLACK_MARBLE_STAIRS.get()))
                .addTomeLookup(BlocksAS.BLACK_MARBLE_STAIRS.get(), 10, ResearchProgression.DISCOVERY)
                .register(ResearchProgression.DISCOVERY);

        resShrines.addSourceConnectionFrom(resWelcome);
        resConstellationPaper.addSourceConnectionFrom(resShrines);
        resMarbles.addSourceConnectionFrom(resWelcome);
        resSootyMarble.addSourceConnectionFrom(resMarbles);
        resOres.addSourceConnectionFrom(resWelcome);
        resResonatingWand.addSourceConnectionFrom(resOres);
        resAltar1.addSourceConnectionFrom(resResonatingWand);
    }

    private static JournalPage recipe(ItemLike outputItem) {
        return JournalPageRecipe.fromOutputPreferAltarRecipes(stack -> !stack.isEmpty() && stack.is(outputItem.asItem()));
    }

    private static JournalPage recipeVanilla(ItemLike outputItem) {
        return JournalPageRecipe.fromOutputPreferVanillaRecipes(stack -> !stack.isEmpty() && stack.is(outputItem.asItem()));
    }

    private static JournalPage recipeTransmutation(ItemLike outputItem) {
        return JournalPageBlockTransmutation.fromOutput(stack -> !stack.isEmpty() && stack.is(outputItem.asItem()));
    }

    private static JournalPage recipeInfusion(ItemLike outputItem) {
        return JournalPageLiquidInfusion.fromOutput(stack -> !stack.isEmpty() && stack.is(outputItem.asItem()));
    }

    private static JournalPage recipe(Predicate<ItemStack> itemStackFilter) {
        return JournalPageRecipe.fromOutputPreferAltarRecipes(itemStackFilter);
    }

    private static JournalPage recipeVanilla(Predicate<ItemStack> itemStackFilter) {
        return JournalPageRecipe.fromOutputPreferVanillaRecipes(itemStackFilter);
    }

    private static JournalPageBlockTransmutation recipeTransmutation(Predicate<ItemStack> itemStackFilter) {
        return JournalPageBlockTransmutation.fromOutput(itemStackFilter);
    }

    private static JournalPageLiquidInfusion recipeInfusion(Predicate<ItemStack> itemStackFilter) {
        return JournalPageLiquidInfusion.fromOutput(itemStackFilter);
    }

    private static JournalPage structure(@javax.annotation.Nullable net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate structureTemplate) {
        return new JournalPageStructure(structureTemplate);
    }

    private static JournalPage text(String identifier) {
        return new JournalPageText(AstralSorcery.MODID.toLowerCase(Locale.ROOT) + ".journal." + identifier + ".text");
    }
}
