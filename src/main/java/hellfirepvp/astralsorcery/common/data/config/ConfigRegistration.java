/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.config;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Registers all Forge config specs for Astral Sorcery.
 * Called once during mod construction (before any lifecycle events).
 *
 * <p>Config types:
 * <ul>
 *   <li>{@link ModConfig.Type#COMMON} — gameplay balance, synced server → client</li>
 *   <li>{@link ModConfig.Type#CLIENT} — visual/audio preferences, client-only</li>
 * </ul></p>
 *
 * <p>Config files are generated at:
 * <ul>
 *   <li>{@code config/astralsorcery-common.toml}</li>
 *   <li>{@code config/astralsorcery-client.toml}</li>
 * </ul></p>
 */
public final class ConfigRegistration {

    private ConfigRegistration() {}

    /**
     * Registers both config specs with Forge.
     * Must be called during mod construction (from {@code AstralSorcery} constructor).
     */
    public static void register() {
        ModLoadingContext ctx = ModLoadingContext.get();

        ctx.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, AstralSorcery.MODID + "-common.toml");
        ctx.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, AstralSorcery.MODID + "-client.toml");

        AstralSorcery.log.info("Registered config files: astralsorcery-common.toml, astralsorcery-client.toml");
    }
}
