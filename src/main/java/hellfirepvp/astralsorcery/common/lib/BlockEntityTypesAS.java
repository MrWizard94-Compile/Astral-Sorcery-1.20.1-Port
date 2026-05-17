package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAttunementAltar;
import hellfirepvp.astralsorcery.common.tile.BlockEntityChalice;
import hellfirepvp.astralsorcery.common.tile.BlockEntityCollectorCrystal;
import hellfirepvp.astralsorcery.common.tile.BlockEntityFountain;
import hellfirepvp.astralsorcery.common.tile.BlockEntityGateway;
import hellfirepvp.astralsorcery.common.tile.BlockEntityInfuser;
import hellfirepvp.astralsorcery.common.tile.BlockEntityLens;
import hellfirepvp.astralsorcery.common.tile.BlockEntityObservatory;
import hellfirepvp.astralsorcery.common.tile.BlockEntityPrism;
import hellfirepvp.astralsorcery.common.tile.BlockEntityRelay;
import hellfirepvp.astralsorcery.common.tile.BlockEntityRitualPedestal;
import hellfirepvp.astralsorcery.common.tile.BlockEntityTelescope;
import hellfirepvp.astralsorcery.common.tile.BlockEntityTreeBeacon;
import hellfirepvp.astralsorcery.common.tile.BlockEntityWell;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * All Astral Sorcery block entity type registrations.
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntityType -> BlockEntityType,
 * TileEntityType.Builder.create -> BlockEntityType.Builder.of,
 * build(null) remains the same</p>
 */
@SuppressWarnings("ConstantConditions") // build(null) is valid for non-datafix BEs
public class BlockEntityTypesAS {

    private BlockEntityTypesAS() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AstralSorcery.MODID);

    public static final RegistryObject<BlockEntityType<BlockEntityAltar>> ALTAR =
            BLOCK_ENTITY_TYPES.register("altar", () ->
                    BlockEntityType.Builder.of(BlockEntityAltar::new,
                            BlocksAS.ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityCollectorCrystal>> COLLECTOR_CRYSTAL =
            BLOCK_ENTITY_TYPES.register("collector_crystal", () ->
                    BlockEntityType.Builder.of(BlockEntityCollectorCrystal::new,
                            BlocksAS.COLLECTOR_CRYSTAL.get(),
                            BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityWell>> WELL =
            BLOCK_ENTITY_TYPES.register("well", () ->
                    BlockEntityType.Builder.of(BlockEntityWell::new,
                            BlocksAS.WELL.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityTelescope>> TELESCOPE =
            BLOCK_ENTITY_TYPES.register("telescope", () ->
                    BlockEntityType.Builder.of(BlockEntityTelescope::new,
                            BlocksAS.TELESCOPE.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityInfuser>> INFUSER =
            BLOCK_ENTITY_TYPES.register("infuser", () ->
                    BlockEntityType.Builder.of(BlockEntityInfuser::new,
                            BlocksAS.INFUSER.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityAttunementAltar>> ATTUNEMENT_ALTAR =
            BLOCK_ENTITY_TYPES.register("attunement_altar", () ->
                    BlockEntityType.Builder.of(BlockEntityAttunementAltar::new,
                            BlocksAS.ATTUNEMENT_ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityRitualPedestal>> RITUAL_PEDESTAL =
            BLOCK_ENTITY_TYPES.register("ritual_pedestal", () ->
                    BlockEntityType.Builder.of(BlockEntityRitualPedestal::new,
                            BlocksAS.RITUAL_PEDESTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityChalice>> CHALICE =
            BLOCK_ENTITY_TYPES.register("chalice", () ->
                    BlockEntityType.Builder.of(BlockEntityChalice::new,
                            BlocksAS.CHALICE.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityGateway>> GATEWAY =
            BLOCK_ENTITY_TYPES.register("gateway", () ->
                    BlockEntityType.Builder.of(BlockEntityGateway::new,
                            BlocksAS.GATEWAY.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityLens>> LENS =
            BLOCK_ENTITY_TYPES.register("lens", () ->
                    BlockEntityType.Builder.of(BlockEntityLens::new,
                            BlocksAS.LENS.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityPrism>> PRISM =
            BLOCK_ENTITY_TYPES.register("prism", () ->
                    BlockEntityType.Builder.of(BlockEntityPrism::new,
                            BlocksAS.PRISM.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityRelay>> RELAY =
            BLOCK_ENTITY_TYPES.register("relay", () ->
                    BlockEntityType.Builder.of(BlockEntityRelay::new,
                            BlocksAS.RELAY.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityFountain>> FOUNTAIN =
            BLOCK_ENTITY_TYPES.register("fountain", () ->
                    BlockEntityType.Builder.of(BlockEntityFountain::new,
                            BlocksAS.FOUNTAIN.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityObservatory>> OBSERVATORY =
            BLOCK_ENTITY_TYPES.register("observatory", () ->
                    BlockEntityType.Builder.of(BlockEntityObservatory::new,
                            BlocksAS.OBSERVATORY.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityTreeBeacon>> TREE_BEACON =
            BLOCK_ENTITY_TYPES.register("tree_beacon", () ->
                    BlockEntityType.Builder.of(BlockEntityTreeBeacon::new,
                            BlocksAS.TREE_BEACON.get()).build(null));
}
