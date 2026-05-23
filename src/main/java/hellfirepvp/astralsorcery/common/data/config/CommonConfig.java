/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.config;

import hellfirepvp.astralsorcery.common.util.log.LogCategory;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;

/**
 * Common config (synced server → client, affects gameplay logic).
 * Defines all balance-critical values for Astral Sorcery systems.
 *
 * <p>Registered via {@link ConfigRegistration#register()} during mod construction.
 * Access values via {@code CommonConfig.CONFIG.maxNetworkRange.get()}.</p>
 *
 * <p>Forge config spec creates a TOML file at:
 * {@code config/astralsorcery-common.toml}</p>
 */
public final class CommonConfig {

    public static final CommonConfig CONFIG;
    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CONFIG = new CommonConfig(builder);
        SPEC = builder.build();
    }

    // ========================================================================
    // Starlight Network
    // ========================================================================

    /** Maximum range (blocks) for starlight transmission links. */
    public final ForgeConfigSpec.IntValue maxNetworkRange;

    /** Maximum number of connections a single node can have. */
    public final ForgeConfigSpec.IntValue maxNodeConnections;

    /** Base starlight output per tick for a collector crystal under open sky. */
    public final ForgeConfigSpec.DoubleValue baseCollectorOutput;

    /** Transmission loss per block of distance (fraction, 0 = no loss). */
    public final ForgeConfigSpec.DoubleValue transmissionLossPerBlock;

    /** Whether collector crystals can collect starlight during the day (reduced). */
    public final ForgeConfigSpec.BooleanValue allowDaytimeCollection;

    // ========================================================================
    // Altar Crafting
    // ========================================================================

    /** Starlight required to craft at the Discovery tier altar. */
    public final ForgeConfigSpec.IntValue altarStarlightDiscovery;

    /** Starlight required to craft at the Attunement tier altar. */
    public final ForgeConfigSpec.IntValue altarStarlightAttunement;

    /** Starlight required to craft at the Constellation tier altar. */
    public final ForgeConfigSpec.IntValue altarStarlightConstellation;

    /** Starlight required to craft at the Radiance tier altar. */
    public final ForgeConfigSpec.IntValue altarStarlightRadiance;

    /** Craft time multiplier (1.0 = normal, 2.0 = double time). */
    public final ForgeConfigSpec.DoubleValue altarCraftTimeMultiplier;

    // ========================================================================
    // Crystal Properties
    // ========================================================================

    /** Maximum crystal size when grown via starlight. */
    public final ForgeConfigSpec.IntValue maxCrystalSize;

    /** Chance per tick for crystal growth (0.0-1.0). */
    public final ForgeConfigSpec.DoubleValue crystalGrowthChance;

    /** Purity threshold above which crystals produce celestial variants. */
    public final ForgeConfigSpec.IntValue celestialPurityThreshold;

    // ========================================================================
    // Perk System
    // ========================================================================

    /** Maximum perk level a player can reach. */
    public final ForgeConfigSpec.IntValue maxPerkLevel;

    /** XP multiplier for perk leveling (1.0 = normal, 2.0 = double speed). */
    public final ForgeConfigSpec.DoubleValue perkExpMultiplier;

    /** Global perk effect multiplier (scales all perk bonuses). */
    public final ForgeConfigSpec.DoubleValue perkEffectMultiplier;

    /** Whether perks work in all dimensions or only the overworld. */
    public final ForgeConfigSpec.BooleanValue perksWorkInAllDimensions;

    // ========================================================================
    // Rituals & Infusion
    // ========================================================================

    /** Maximum range for ritual effects (blocks). */
    public final ForgeConfigSpec.IntValue ritualMaxRange;

    /** Starlight cost multiplier for liquid infusion. */
    public final ForgeConfigSpec.DoubleValue infusionCostMultiplier;

    /** Whether liquid infusion consumes the liquid. */
    public final ForgeConfigSpec.BooleanValue infusionConsumesLiquid;

    // ========================================================================
    // Lightwell
    // ========================================================================

    /** Base production rate for the lightwell (mB per tick). */
    public final ForgeConfigSpec.DoubleValue wellBaseProduction;

    /** Maximum fluid storage for the lightwell (mB). */
    public final ForgeConfigSpec.IntValue wellMaxStorage;

    /** Catalyst consumption chance per cycle (0.0-1.0). */
    public final ForgeConfigSpec.DoubleValue wellCatalystConsumptionChance;

    // ========================================================================
    // World Generation
    // ========================================================================

    /** Rock crystal ore vein size. */
    public final ForgeConfigSpec.IntValue rockCrystalVeinSize;

    /** Rock crystal ore minimum Y level. */
    public final ForgeConfigSpec.IntValue rockCrystalMinY;

    /** Rock crystal ore maximum Y level. */
    public final ForgeConfigSpec.IntValue rockCrystalMaxY;

    /** Number of rock crystal ore attempts per chunk. */
    public final ForgeConfigSpec.IntValue rockCrystalAttemptsPerChunk;

    /** Marble vein size. */
    public final ForgeConfigSpec.IntValue marbleVeinSize;

    /** Aquamarine ore frequency (attempts per chunk). */
    public final ForgeConfigSpec.IntValue aquamarineFrequency;

    /** Whether to generate ancient shrines in new worlds. */
    public final ForgeConfigSpec.BooleanValue generateShrines;

    // ========================================================================
    // Celestial & Starlight
    // ========================================================================

    /** Day-time starlight collection fraction (0.0 = none, 0.2 = default). */
    public final ForgeConfigSpec.DoubleValue daytimeStarlightFraction;

    /** Weather penalty for starlight (rain, 0.7 = default 30% reduction). */
    public final ForgeConfigSpec.DoubleValue rainStarlightPenalty;

    /** Weather penalty for starlight (thunderstorm, 0.5 = 50% reduction). */
    public final ForgeConfigSpec.DoubleValue thunderStarlightPenalty;

    /** Full moon starlight bonus multiplier. */
    public final ForgeConfigSpec.DoubleValue fullMoonBonus;

    // ========================================================================
    // Gateway & Teleportation
    // ========================================================================

    /** Maximum gateway teleportation range (0 = unlimited). */
    public final ForgeConfigSpec.IntValue gatewayMaxRange;

    /** Whether gateways can teleport across dimensions. */
    public final ForgeConfigSpec.BooleanValue gatewayCrossDimensional;

    /** Starlight cost per block of teleportation distance. */
    public final ForgeConfigSpec.DoubleValue gatewayCostPerBlock;

    // ========================================================================
    // Mob Spawning
    // ========================================================================

    /** If true, the Lucerna ritual spawn-deny effect blocks all mob types (not just monsters). */
    public final ForgeConfigSpec.BooleanValue mobSpawningDenyAllTypes;

    // ========================================================================
    // Debug Logging
    // ========================================================================

    /** Per-category debug logging toggles. */
    public final Map<LogCategory, ForgeConfigSpec.BooleanValue> debugLogging
            = new EnumMap<>(LogCategory.class);

    // ========================================================================
    // Constructor — builds the config spec
    // ========================================================================

    private CommonConfig(@Nonnull ForgeConfigSpec.Builder builder) {
        // --- Starlight Network ---
        builder.push("starlight_network");
        maxNetworkRange = builder
                .comment("Maximum range (blocks) for starlight transmission links")
                .defineInRange("maxRange", 16, 4, 64);
        maxNodeConnections = builder
                .comment("Maximum number of links a single transmission node can have")
                .defineInRange("maxConnections", 8, 1, 32);
        baseCollectorOutput = builder
                .comment("Base starlight output per tick for a collector crystal under open sky at night")
                .defineInRange("baseCollectorOutput", 1.0, 0.1, 10.0);
        transmissionLossPerBlock = builder
                .comment("Starlight fraction lost per block of transmission distance (0 = lossless)")
                .defineInRange("transmissionLossPerBlock", 0.005, 0.0, 0.1);
        allowDaytimeCollection = builder
                .comment("Whether collector crystals can collect reduced starlight during the day")
                .define("allowDaytimeCollection", true);
        builder.pop();

        // --- Altar Crafting ---
        builder.push("altar");
        altarStarlightDiscovery = builder
                .comment("Starlight required for Discovery tier crafts")
                .defineInRange("starlightDiscovery", 200, 10, 10000);
        altarStarlightAttunement = builder
                .comment("Starlight required for Attunement tier crafts")
                .defineInRange("starlightAttunement", 800, 10, 50000);
        altarStarlightConstellation = builder
                .comment("Starlight required for Constellation tier crafts")
                .defineInRange("starlightConstellation", 2000, 10, 100000);
        altarStarlightRadiance = builder
                .comment("Starlight required for Radiance tier crafts")
                .defineInRange("starlightRadiance", 4000, 10, 200000);
        altarCraftTimeMultiplier = builder
                .comment("Craft time multiplier (1.0 = normal)")
                .defineInRange("craftTimeMultiplier", 1.0, 0.1, 10.0);
        builder.pop();

        // --- Crystal Properties ---
        builder.push("crystal");
        maxCrystalSize = builder
                .comment("Maximum crystal size when fully grown")
                .defineInRange("maxSize", 900, 100, 9000);
        crystalGrowthChance = builder
                .comment("Chance per tick for a crystal to grow (starlight permitting)")
                .defineInRange("growthChance", 0.01, 0.001, 1.0);
        celestialPurityThreshold = builder
                .comment("Minimum purity for a crystal to become celestial quality")
                .defineInRange("celestialPurityThreshold", 85, 50, 100);
        builder.pop();

        // --- Perk System ---
        builder.push("perks");
        maxPerkLevel = builder
                .comment("Maximum level a player can reach in the perk system")
                .defineInRange("maxLevel", 40, 10, 100);
        perkExpMultiplier = builder
                .comment("XP gain multiplier for perk leveling")
                .defineInRange("expMultiplier", 1.0, 0.1, 10.0);
        perkEffectMultiplier = builder
                .comment("Global scaling factor for all perk attribute bonuses")
                .defineInRange("effectMultiplier", 1.0, 0.1, 5.0);
        perksWorkInAllDimensions = builder
                .comment("Whether perks apply in all dimensions (false = overworld only)")
                .define("allDimensions", true);
        builder.pop();

        // --- Rituals & Infusion ---
        builder.push("rituals");
        ritualMaxRange = builder
                .comment("Maximum block range for ritual effects")
                .defineInRange("maxRange", 16, 4, 64);
        infusionCostMultiplier = builder
                .comment("Starlight cost multiplier for infusion recipes")
                .defineInRange("infusionCostMultiplier", 1.0, 0.1, 10.0);
        infusionConsumesLiquid = builder
                .comment("Whether liquid infusion consumes the liquid starlight")
                .define("consumesLiquid", true);
        builder.pop();

        // --- Lightwell ---
        builder.push("lightwell");
        wellBaseProduction = builder
                .comment("Base fluid production rate (mB per tick)")
                .defineInRange("baseProduction", 0.5, 0.01, 10.0);
        wellMaxStorage = builder
                .comment("Maximum fluid storage capacity (mB)")
                .defineInRange("maxStorage", 2000, 100, 100000);
        wellCatalystConsumptionChance = builder
                .comment("Chance per cycle for the catalyst item to be consumed")
                .defineInRange("catalystConsumption", 0.002, 0.0, 1.0);
        builder.pop();

        // --- World Generation ---
        builder.push("worldgen");
        rockCrystalVeinSize = builder
                .comment("Rock crystal ore vein size")
                .defineInRange("rockCrystalVeinSize", 3, 1, 16);
        rockCrystalMinY = builder
                .comment("Minimum Y level for rock crystal ore generation")
                .defineInRange("rockCrystalMinY", -64, -64, 128);
        rockCrystalMaxY = builder
                .comment("Maximum Y level for rock crystal ore generation")
                .defineInRange("rockCrystalMaxY", 16, -64, 320);
        rockCrystalAttemptsPerChunk = builder
                .comment("Rock crystal ore placement attempts per chunk")
                .defineInRange("rockCrystalAttempts", 2, 0, 16);
        marbleVeinSize = builder
                .comment("Marble vein size")
                .defineInRange("marbleVeinSize", 32, 4, 128);
        aquamarineFrequency = builder
                .comment("Aquamarine ore attempts per chunk (riverbed)")
                .defineInRange("aquamarineFrequency", 3, 0, 16);
        generateShrines = builder
                .comment("Whether to generate ancient shrines in new worlds")
                .define("generateShrines", true);
        builder.pop();

        // --- Celestial & Starlight ---
        builder.push("celestial");
        daytimeStarlightFraction = builder
                .comment("Fraction of starlight available during daytime (0 = none)")
                .defineInRange("daytimeFraction", 0.2, 0.0, 1.0);
        rainStarlightPenalty = builder
                .comment("Starlight multiplier during rain (1.0 = no penalty)")
                .defineInRange("rainPenalty", 0.7, 0.0, 1.0);
        thunderStarlightPenalty = builder
                .comment("Starlight multiplier during thunderstorms")
                .defineInRange("thunderPenalty", 0.5, 0.0, 1.0);
        fullMoonBonus = builder
                .comment("Starlight multiplier during full moon")
                .defineInRange("fullMoonBonus", 1.2, 1.0, 3.0);
        builder.pop();

        // --- Gateway & Teleportation ---
        builder.push("gateway");
        gatewayMaxRange = builder
                .comment("Maximum gateway teleportation range in blocks (0 = unlimited)")
                .defineInRange("maxRange", 0, 0, 100000);
        gatewayCrossDimensional = builder
                .comment("Whether gateways can teleport between dimensions")
                .define("crossDimensional", true);
        gatewayCostPerBlock = builder
                .comment("Starlight cost per block of teleportation distance")
                .defineInRange("costPerBlock", 0.1, 0.0, 10.0);
        builder.pop();

        // --- Mob Spawning ---
        builder.push("mob_spawning");
        mobSpawningDenyAllTypes = builder
                .comment("If true, the Lucerna spawn-deny ritual blocks all mob types instead of only monsters")
                .define("denyAllTypes", false);
        builder.pop();

        // --- Debug Logging ---
        builder.push("debug");
        for (LogCategory category : LogCategory.values()) {
            debugLogging.put(category, builder
                    .comment("Enable debug logging for the " + category.name() + " category")
                    .define("log" + toTitleCase(category.name()), false));
        }
        builder.pop();
    }

    @SuppressWarnings("null")
    private static String toTitleCase(@Nonnull String upper) {
        if (upper.isEmpty()) return upper;
        String lower = upper.toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (String word : lower.split(" ")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return sb.toString();
    }
}
