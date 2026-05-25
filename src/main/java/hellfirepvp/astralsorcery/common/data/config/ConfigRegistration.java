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
 *   <li>{@link ModConfig.Type#SERVER} — gameplay balance, per-world; Forge auto-syncs
 *       from server → client on join, so both sides always use identical values.</li>
 *   <li>{@link ModConfig.Type#CLIENT} — visual/audio preferences, client-only</li>
 * </ul></p>
 *
 * <p>Config files:
 * <ul>
 *   <li>{@code saves/<world>/serverconfig/astralsorcery-server.toml}</li>
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

        // SERVER type: config lives in world's serverconfig/ dir; Forge handles server→client sync
        ctx.registerConfig(ModConfig.Type.SERVER, CommonConfig.SPEC, AstralSorcery.MODID + "-server.toml");
        ctx.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, AstralSorcery.MODID + "-client.toml");

        AstralSorcery.log.info("Registered config files: astralsorcery-server.toml, astralsorcery-client.toml");
    }
}
