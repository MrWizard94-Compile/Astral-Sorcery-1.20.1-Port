package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.BlackMarble;
import hellfirepvp.astralsorcery.common.block.BlockIlluminator;
import hellfirepvp.astralsorcery.common.block.BlockInfusedWood;
import hellfirepvp.astralsorcery.common.block.foliage.BlockGlowFlower;
import hellfirepvp.astralsorcery.common.block.marble.BlockBlackMarblePillar;
import hellfirepvp.astralsorcery.common.block.marble.BlockMarble;
import hellfirepvp.astralsorcery.common.block.marble.BlockMarblePillar;
import hellfirepvp.astralsorcery.common.block.marble.BlockMarbleSlab;
import hellfirepvp.astralsorcery.common.block.marble.BlockMarbleStairs;
import hellfirepvp.astralsorcery.common.block.ore.BlockAquamarineOre;
import hellfirepvp.astralsorcery.common.block.ore.BlockRockCrystalOre;
import hellfirepvp.astralsorcery.common.block.ore.BlockStarmetal;
import hellfirepvp.astralsorcery.common.block.ore.BlockStarmetalOre;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.block.tile.BlockAttunementAltar;
import hellfirepvp.astralsorcery.common.block.tile.BlockCelestialCrystalCluster;
import hellfirepvp.astralsorcery.common.block.tile.BlockChalice;
import hellfirepvp.astralsorcery.common.block.tile.BlockCollectorCrystal;
import hellfirepvp.astralsorcery.common.block.tile.BlockFlareLight;
import hellfirepvp.astralsorcery.common.block.tile.BlockFountain;
import hellfirepvp.astralsorcery.common.block.tile.BlockGateway;
import hellfirepvp.astralsorcery.common.block.tile.BlockGemCrystalCluster;
import hellfirepvp.astralsorcery.common.block.tile.BlockInfuser;
import hellfirepvp.astralsorcery.common.block.tile.BlockLens;
import hellfirepvp.astralsorcery.common.block.tile.BlockObservatory;
import hellfirepvp.astralsorcery.common.block.tile.BlockPrism;
import hellfirepvp.astralsorcery.common.block.tile.BlockRefractionTable;
import hellfirepvp.astralsorcery.common.block.tile.BlockRelay;
import hellfirepvp.astralsorcery.common.block.tile.BlockRitualLink;
import hellfirepvp.astralsorcery.common.block.tile.BlockRitualPedestal;
import hellfirepvp.astralsorcery.common.block.tile.BlockSpectralRelay;
import hellfirepvp.astralsorcery.common.block.tile.BlockStructural;
import hellfirepvp.astralsorcery.common.block.tile.BlockTelescope;
import hellfirepvp.astralsorcery.common.block.tile.BlockTranslucentBlock;
import hellfirepvp.astralsorcery.common.block.tile.BlockTreeBeacon;
import hellfirepvp.astralsorcery.common.block.tile.BlockTreeBeaconComponent;
import hellfirepvp.astralsorcery.common.block.tile.BlockVanishing;
import hellfirepvp.astralsorcery.common.block.tile.BlockWell;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * All Astral Sorcery block registrations.
 */
public class BlocksAS {

    private BlocksAS() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AstralSorcery.MODID);

    // ---- Marble family ----
    public static final RegistryObject<BlockMarble> MARBLE_RAW =
            BLOCKS.register("marble_raw", BlockMarble::new);
    public static final RegistryObject<BlockMarble> MARBLE_ARCH =
            BLOCKS.register("marble_arch", BlockMarble::new);
    public static final RegistryObject<BlockMarble> MARBLE_BRICKS =
            BLOCKS.register("marble_bricks", BlockMarble::new);
    public static final RegistryObject<BlockMarble> MARBLE_CHISELED =
            BLOCKS.register("marble_chiseled", BlockMarble::new);
    public static final RegistryObject<BlockMarble> MARBLE_ENGRAVED =
            BLOCKS.register("marble_engraved", BlockMarble::new);
    public static final RegistryObject<BlockMarble> MARBLE_RUNED =
            BLOCKS.register("marble_runed", BlockMarble::new);
    public static final RegistryObject<BlockMarblePillar> MARBLE_PILLAR =
            BLOCKS.register("marble_pillar", BlockMarblePillar::new);
    public static final RegistryObject<BlockMarbleSlab> MARBLE_SLAB =
            BLOCKS.register("marble_slab", BlockMarbleSlab::new);
    public static final RegistryObject<BlockMarbleStairs> MARBLE_STAIRS =
            BLOCKS.register("marble_stairs", () -> new BlockMarbleStairs(() -> MARBLE_RAW.get().defaultBlockState()));

    // ---- Black marble ----
    public static final RegistryObject<BlackMarble> BLACK_MARBLE_RAW =
            BLOCKS.register("black_marble_raw", BlackMarble::new);
    public static final RegistryObject<BlackMarble> BLACK_MARBLE_ARCH =
            BLOCKS.register("black_marble_arch", BlackMarble::new);
    public static final RegistryObject<BlackMarble> BLACK_MARBLE_BRICKS =
            BLOCKS.register("black_marble_bricks", BlackMarble::new);
    public static final RegistryObject<BlackMarble> BLACK_MARBLE_CHISELED =
            BLOCKS.register("black_marble_chiseled", BlackMarble::new);
    public static final RegistryObject<BlackMarble> BLACK_MARBLE_ENGRAVED =
            BLOCKS.register("black_marble_engraved", BlackMarble::new);
    public static final RegistryObject<BlackMarble> BLACK_MARBLE_RUNED =
            BLOCKS.register("black_marble_runed", BlackMarble::new);
    public static final RegistryObject<BlockBlackMarblePillar> BLACK_MARBLE_PILLAR =
            BLOCKS.register("black_marble_pillar", BlockBlackMarblePillar::new);

    // ---- Infused wood ----
    public static final RegistryObject<BlockInfusedWood> INFUSED_WOOD =
            BLOCKS.register("infused_wood", BlockInfusedWood::new);
    public static final RegistryObject<BlockInfusedWood> INFUSED_WOOD_ARCH =
            BLOCKS.register("infused_wood_arch", BlockInfusedWood::new);
    public static final RegistryObject<BlockInfusedWood> INFUSED_WOOD_COLUMN =
            BLOCKS.register("infused_wood_column", BlockInfusedWood::new);
    public static final RegistryObject<BlockInfusedWood> INFUSED_WOOD_ENGRAVED =
            BLOCKS.register("infused_wood_engraved", BlockInfusedWood::new);
    public static final RegistryObject<BlockInfusedWood> INFUSED_WOOD_ENRICHED =
            BLOCKS.register("infused_wood_enriched", BlockInfusedWood::new);
    public static final RegistryObject<BlockInfusedWood> INFUSED_WOOD_PLANKS =
            BLOCKS.register("infused_wood_planks", BlockInfusedWood::new);

    // ---- Ores ----
    public static final RegistryObject<BlockRockCrystalOre> ROCK_CRYSTAL_ORE =
            BLOCKS.register("rock_crystal_ore", BlockRockCrystalOre::new);
    public static final RegistryObject<BlockAquamarineOre> AQUAMARINE_ORE =
            BLOCKS.register("aquamarine_sand_ore", BlockAquamarineOre::new);
    public static final RegistryObject<BlockStarmetalOre> STARMETAL_ORE =
            BLOCKS.register("starmetal_ore", BlockStarmetalOre::new);
    public static final RegistryObject<BlockStarmetal> STARMETAL =
            BLOCKS.register("starmetal", BlockStarmetal::new);

    // ---- Foliage ----
    public static final RegistryObject<BlockGlowFlower> GLOW_FLOWER =
            BLOCKS.register("glow_flower", BlockGlowFlower::new);

    // ---- Crystal clusters (world gen) ----
    public static final RegistryObject<BlockCelestialCrystalCluster> CELESTIAL_CRYSTAL_CLUSTER =
            BLOCKS.register("celestial_crystal_cluster", BlockCelestialCrystalCluster::new);
    public static final RegistryObject<BlockGemCrystalCluster> GEM_CRYSTAL_CLUSTER =
            BLOCKS.register("gem_crystal_cluster", BlockGemCrystalCluster::new);

    // ---- Special / Internal ----
    public static final RegistryObject<BlockIlluminator> ILLUMINATOR =
            BLOCKS.register("illuminator", BlockIlluminator::new);
    public static final RegistryObject<BlockFlareLight> FLARE_LIGHT =
            BLOCKS.register("flare_light", BlockFlareLight::new);
    public static final RegistryObject<BlockStructural> STRUCTURAL =
            BLOCKS.register("structural", BlockStructural::new);
    public static final RegistryObject<BlockVanishing> VANISHING =
            BLOCKS.register("vanishing", BlockVanishing::new);
    public static final RegistryObject<BlockTranslucentBlock> TRANSLUCENT_BLOCK =
            BLOCKS.register("translucent_block", BlockTranslucentBlock::new);

    // ---- Tile entity blocks ----
    public static final RegistryObject<BlockAltar> ALTAR =
            BLOCKS.register("altar", BlockAltar::new);
    public static final RegistryObject<BlockAttunementAltar> ATTUNEMENT_ALTAR =
            BLOCKS.register("attunement_altar", BlockAttunementAltar::new);
    public static final RegistryObject<BlockCollectorCrystal> COLLECTOR_CRYSTAL =
            BLOCKS.register("collector_crystal", () -> new BlockCollectorCrystal(false));
    public static final RegistryObject<BlockCollectorCrystal> CELESTIAL_COLLECTOR_CRYSTAL =
            BLOCKS.register("celestial_collector_crystal", () -> new BlockCollectorCrystal(true));
    public static final RegistryObject<BlockLens> LENS =
            BLOCKS.register("lens", BlockLens::new);
    public static final RegistryObject<BlockPrism> PRISM =
            BLOCKS.register("prism", BlockPrism::new);
    public static final RegistryObject<BlockRelay> RELAY =
            BLOCKS.register("relay", BlockRelay::new);
    public static final RegistryObject<BlockWell> WELL =
            BLOCKS.register("well", BlockWell::new);
    public static final RegistryObject<BlockInfuser> INFUSER =
            BLOCKS.register("infuser", BlockInfuser::new);
    public static final RegistryObject<BlockRitualPedestal> RITUAL_PEDESTAL =
            BLOCKS.register("ritual_pedestal", BlockRitualPedestal::new);
    public static final RegistryObject<BlockChalice> CHALICE =
            BLOCKS.register("chalice", BlockChalice::new);
    public static final RegistryObject<BlockTelescope> TELESCOPE =
            BLOCKS.register("telescope", BlockTelescope::new);
    public static final RegistryObject<BlockGateway> GATEWAY =
            BLOCKS.register("gateway", BlockGateway::new);
    public static final RegistryObject<BlockFountain> FOUNTAIN =
            BLOCKS.register("fountain", BlockFountain::new);
    public static final RegistryObject<BlockObservatory> OBSERVATORY =
            BLOCKS.register("observatory", BlockObservatory::new);
    public static final RegistryObject<BlockTreeBeacon> TREE_BEACON =
            BLOCKS.register("tree_beacon", BlockTreeBeacon::new);
    public static final RegistryObject<BlockTreeBeaconComponent> TREE_BEACON_COMPONENT =
            BLOCKS.register("tree_beacon_component", BlockTreeBeaconComponent::new);
    public static final RegistryObject<BlockRefractionTable> REFRACTION_TABLE =
            BLOCKS.register("refraction_table", BlockRefractionTable::new);
    public static final RegistryObject<BlockRitualLink> RITUAL_LINK =
            BLOCKS.register("ritual_link", BlockRitualLink::new);
    public static final RegistryObject<BlockSpectralRelay> SPECTRAL_RELAY =
            BLOCKS.register("spectral_relay", BlockSpectralRelay::new);
}
