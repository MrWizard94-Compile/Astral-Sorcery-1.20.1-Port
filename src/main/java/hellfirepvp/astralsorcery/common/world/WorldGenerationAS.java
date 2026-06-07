/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.world;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.world.feature.AquamarineFeature;
import hellfirepvp.astralsorcery.common.world.feature.AstralConfigCountPlacement;
import hellfirepvp.astralsorcery.common.world.feature.AstralConfigHeightPlacement;
import hellfirepvp.astralsorcery.common.world.feature.GlowFlowerFeature;
import hellfirepvp.astralsorcery.common.world.feature.MarbleVeinFeature;
import hellfirepvp.astralsorcery.common.world.feature.RockCrystalFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers all Astral Sorcery world generation features.
 *
 * <p>In 1.20, world generation uses a layered system:
 * <ol>
 *   <li>{@code Feature<C>} — the code that places blocks</li>
 *   <li>{@code ConfiguredFeature<C, F>} — a feature + its configuration (JSON)</li>
 *   <li>{@code PlacedFeature} — a configured feature + placement modifiers (JSON)</li>
 *   <li>{@code BiomeModifier} — Forge system to inject placed features into biomes (JSON)</li>
 * </ol>
 *
 * <p>This class handles step 1 (Feature registration). Steps 2-4 are
 * data-driven via JSON files in the datapack.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * Feature registration still uses DeferredRegister.
 * ConfiguredFeature/PlacedFeature moved to data-driven JSON (no longer code).
 * Biome modifications use Forge's BiomeModifier system (replaces BiomeLoadingEvent).</p>
 */
public final class WorldGenerationAS {

    private WorldGenerationAS() {}

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, AstralSorcery.MODID);

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, AstralSorcery.MODID);

    /**
     * Config-driven count modifier: reads rockCrystalAttemptsPerChunk or aquamarineFrequency.
     * Used in placed_feature JSONs for rock_crystal_ore and aquamarine_ore.
     */
    public static final RegistryObject<PlacementModifierType<AstralConfigCountPlacement>> CONFIG_COUNT =
            PLACEMENT_MODIFIERS.register("config_count",
                    () -> () -> AstralConfigCountPlacement.CODEC);

    /**
     * Config-driven height modifier: reads rockCrystalMinY and rockCrystalMaxY.
     * Used in the rock_crystal_ore placed_feature JSON.
     */
    public static final RegistryObject<PlacementModifierType<AstralConfigHeightPlacement>> ROCK_CRYSTAL_HEIGHT =
            PLACEMENT_MODIFIERS.register("rock_crystal_height",
                    () -> () -> AstralConfigHeightPlacement.CODEC);

    // ---- Feature types ----

    /**
     * Generates rock crystal ore clusters deep underground.
     * ConfiguredFeature: worldgen/configured_feature/rock_crystal_ore.json
     * PlacedFeature: worldgen/placed_feature/rock_crystal_ore.json
     */
    public static final RegistryObject<RockCrystalFeature> ROCK_CRYSTAL_ORE =
            FEATURES.register("rock_crystal_ore",
                    () -> new RockCrystalFeature(NoneFeatureConfiguration.CODEC));

    /**
     * Generates large marble veins underground.
     * ConfiguredFeature: worldgen/configured_feature/marble_vein.json
     * PlacedFeature: worldgen/placed_feature/marble_vein.json
     */
    public static final RegistryObject<MarbleVeinFeature> MARBLE_VEIN =
            FEATURES.register("marble_vein",
                    () -> new MarbleVeinFeature(NoneFeatureConfiguration.CODEC));

    /**
     * Generates aquamarine ore in sandy riverbeds.
     * ConfiguredFeature: worldgen/configured_feature/aquamarine_ore.json
     * PlacedFeature: worldgen/placed_feature/aquamarine_ore.json
     */
    public static final RegistryObject<AquamarineFeature> AQUAMARINE_ORE =
            FEATURES.register("aquamarine_ore",
                    () -> new AquamarineFeature(NoneFeatureConfiguration.CODEC));

    /**
     * Generates glow flower clusters on the surface near marble/crystal deposits.
     * ConfiguredFeature: worldgen/configured_feature/glow_flower.json
     * PlacedFeature: worldgen/placed_feature/glow_flower.json
     */
    public static final RegistryObject<GlowFlowerFeature> GLOW_FLOWER =
            FEATURES.register("glow_flower",
                    () -> new GlowFlowerFeature(NoneFeatureConfiguration.CODEC));
}
