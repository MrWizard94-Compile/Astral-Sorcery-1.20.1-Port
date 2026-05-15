package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import hellfirepvp.astralsorcery.common.tile.BlockEntityCollectorCrystal;
import hellfirepvp.astralsorcery.common.tile.BlockEntityTelescope;
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

    // TODO: Add remaining block entity types as they are implemented:
    // INFUSER, ATTUNEMENT_ALTAR, RITUAL_PEDESTAL, CHALICE, GATEWAY,
    // LENS, PRISM, RELAY, FOUNTAIN, OBSERVATORY, TREE_BEACON, etc.
}
