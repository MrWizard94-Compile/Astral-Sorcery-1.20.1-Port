/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.config;

import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;

/**
 * Client-only config (not synced, affects visual/audio settings only).
 * Creates {@code config/astralsorcery-client.toml}.
 *
 * <p>These settings never affect gameplay logic and are safe for
 * clients to customize without server permission.</p>
 */
public final class ClientConfig {

    public static final ClientConfig CONFIG;
    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CONFIG = new ClientConfig(builder);
        SPEC = builder.build();
    }

    // ========================================================================
    // Particle Effects
    // ========================================================================

    /** Overall particle density multiplier (0 = off, 1 = normal, 2 = double). */
    public final ForgeConfigSpec.DoubleValue particleDensity;

    /** Maximum concurrent particles before culling (performance). */
    public final ForgeConfigSpec.IntValue maxParticles;

    /** Whether to render starlight beam effects between network nodes. */
    public final ForgeConfigSpec.BooleanValue renderStarlightBeams;

    /** Whether to render constellation lines in the sky at night. */
    public final ForgeConfigSpec.BooleanValue renderConstellations;

    // ========================================================================
    // Sky Rendering
    // ========================================================================

    /** Whether the custom sky renderer is enabled. */
    public final ForgeConfigSpec.BooleanValue customSkyRenderer;

    /** Sky renderer star brightness multiplier. */
    public final ForgeConfigSpec.DoubleValue starBrightness;

    /** Whether to render the constellation glow effect on attuned players. */
    public final ForgeConfigSpec.BooleanValue renderPlayerGlow;

    // ========================================================================
    // UI / HUD
    // ========================================================================

    /** Whether to show the starlight charge indicator on the HUD. */
    public final ForgeConfigSpec.BooleanValue showStarlightHUD;

    /** HUD position X offset from default. */
    public final ForgeConfigSpec.IntValue hudOffsetX;

    /** HUD position Y offset from default. */
    public final ForgeConfigSpec.IntValue hudOffsetY;

    /** Whether to show constellation name labels in the sky. */
    public final ForgeConfigSpec.BooleanValue showConstellationLabels;

    // ========================================================================
    // Performance
    // ========================================================================

    /** Distance at which block entity special renderers stop rendering. */
    public final ForgeConfigSpec.IntValue besrRenderDistance;

    /** Whether to use batched rendering for particle effects (faster). */
    public final ForgeConfigSpec.BooleanValue batchedParticles;

    // ========================================================================
    // Constructor
    // ========================================================================

    private ClientConfig(@Nonnull ForgeConfigSpec.Builder builder) {
        // --- Particles ---
        builder.push("particles");
        particleDensity = builder
                .comment("Overall particle density (0 = off, 1 = normal, 2 = double)")
                .defineInRange("density", 1.0, 0.0, 3.0);
        maxParticles = builder
                .comment("Maximum number of concurrent particles (performance)")
                .defineInRange("maxParticles", 4000, 100, 20000);
        renderStarlightBeams = builder
                .comment("Render starlight beam effects between linked blocks")
                .define("renderBeams", true);
        renderConstellations = builder
                .comment("Render constellation line patterns in the night sky")
                .define("renderConstellations", true);
        builder.pop();

        // --- Sky ---
        builder.push("sky");
        customSkyRenderer = builder
                .comment("Enable the custom Astral Sorcery sky renderer")
                .define("customRenderer", true);
        starBrightness = builder
                .comment("Star brightness multiplier in the custom sky")
                .defineInRange("brightness", 1.0, 0.1, 3.0);
        renderPlayerGlow = builder
                .comment("Render glowing star particles around attuned players")
                .define("playerGlow", true);
        builder.pop();

        // --- UI ---
        builder.push("ui");
        showStarlightHUD = builder
                .comment("Show starlight level indicator on the HUD")
                .define("starlightHUD", true);
        hudOffsetX = builder
                .comment("HUD X offset from default position (pixels)")
                .defineInRange("offsetX", 0, -500, 500);
        hudOffsetY = builder
                .comment("HUD Y offset from default position (pixels)")
                .defineInRange("offsetY", 0, -500, 500);
        showConstellationLabels = builder
                .comment("Show constellation name labels when viewing through telescope/observatory")
                .define("constellationLabels", true);
        builder.pop();

        // --- Performance ---
        builder.push("performance");
        besrRenderDistance = builder
                .comment("Block entity renderer maximum render distance (blocks)")
                .defineInRange("besrDistance", 64, 16, 256);
        batchedParticles = builder
                .comment("Use batched rendering for particles (faster, may cause issues on some GPUs)")
                .define("batchedParticles", true);
        builder.pop();
    }
}
