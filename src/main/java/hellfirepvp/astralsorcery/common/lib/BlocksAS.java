package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.BlackMarble;
import hellfirepvp.astralsorcery.common.block.marble.BlockMarble;
import hellfirepvp.astralsorcery.common.block.marble.BlockMarblePillar;
import hellfirepvp.astralsorcery.common.block.marble.BlockMarbleSlab;
import hellfirepvp.astralsorcery.common.block.marble.BlockMarbleStairs;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.block.tile.BlockAttunementAltar;
import hellfirepvp.astralsorcery.common.block.tile.BlockChalice;
import hellfirepvp.astralsorcery.common.block.tile.BlockCollectorCrystal;
import hellfirepvp.astralsorcery.common.block.tile.BlockGateway;
import hellfirepvp.astralsorcery.common.block.tile.BlockInfuser;
import hellfirepvp.astralsorcery.common.block.tile.BlockLens;
import hellfirepvp.astralsorcery.common.block.tile.BlockRitualPedestal;
import hellfirepvp.astralsorcery.common.block.tile.BlockTelescope;
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
}
