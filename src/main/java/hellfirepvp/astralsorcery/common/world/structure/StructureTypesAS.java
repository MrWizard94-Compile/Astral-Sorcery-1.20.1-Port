/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.world.structure;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers all Astral Sorcery structure types.
 *
 * <p>In 1.20, {@link StructureType} is a codec wrapper registered via
 * DeferredRegister. The actual structure JSON references these types
 * and provides configuration (template pool, height, etc.).</p>
 *
 * <p>1.16 -> 1.20 changes:
 * StructureFeature&lt;C&gt; -> Structure (abstract) with StructureType&lt;S&gt; codec registry.
 * Registration is two-step: register StructureType here, define structure JSON in datapack.</p>
 */
public final class StructureTypesAS {

    private StructureTypesAS() {}

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, AstralSorcery.MODID);

    /**
     * Ancient shrine — surface marble structure with discovery content.
     * Structure JSON: worldgen/structure/ancient_shrine.json
     * Structure set: worldgen/structure_set/ancient_shrines.json
     */
    public static final RegistryObject<StructureType<AncientShrineStructure>> ANCIENT_SHRINE =
            STRUCTURE_TYPES.register("ancient_shrine",
                    () -> () -> AncientShrineStructure.CODEC);

    /**
     * Desert shrine — sandstone+marble structure in desert biomes.
     * Partially buried, contains starlight basin loot.
     * Structure JSON: worldgen/structure/desert_shrine.json
     */
    public static final RegistryObject<StructureType<DesertShrineStructure>> DESERT_SHRINE =
            STRUCTURE_TYPES.register("desert_shrine",
                    () -> () -> DesertShrineStructure.CODEC);

    /**
     * Small shrine — common overworld marble formation with collector crystal.
     * Primary early-game discovery point. High frequency, small footprint.
     * Structure JSON: worldgen/structure/small_shrine.json
     */
    public static final RegistryObject<StructureType<SmallShrineStructure>> SMALL_SHRINE =
            STRUCTURE_TYPES.register("small_shrine",
                    () -> () -> SmallShrineStructure.CODEC);
}
