/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.lib;

import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Static references to all Astral Sorcery animated sprite sheet resources.
 * Initialized once during client setup via {@link hellfirepvp.astralsorcery.client.registry.RegistrySprites#loadSprites()},
 * called immediately after {@link TexturesAS#init()}.
 *
 * <p>Each field wraps the corresponding {@link TexturesAS} texture as a
 * {@link SpriteSheetResource} with the animation grid dimensions
 * (rows × columns) matching the source PNG atlas.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class SpritesAS {

    private SpritesAS() {}

    // ---- Crystal burst effects ----
    public static SpriteSheetResource SPR_CRYSTAL_EFFECT_1;
    public static SpriteSheetResource SPR_CRYSTAL_EFFECT_2;
    public static SpriteSheetResource SPR_CRYSTAL_EFFECT_3;
    public static SpriteSheetResource SPR_GEM_CRYSTAL_BURST;
    public static SpriteSheetResource SPR_GEM_CRYSTAL_BURST_SKY;
    public static SpriteSheetResource SPR_GEM_CRYSTAL_BURST_DAY;
    public static SpriteSheetResource SPR_GEM_CRYSTAL_BURST_NIGHT;

    // ---- Perk tree node icons ----
    public static SpriteSheetResource SPR_PERK_INACTIVE;
    public static SpriteSheetResource SPR_PERK_ACTIVE;
    public static SpriteSheetResource SPR_PERK_ACTIVATEABLE;
    public static SpriteSheetResource SPR_PERK_HALO_INACTIVE;
    public static SpriteSheetResource SPR_PERK_HALO_ACTIVE;
    public static SpriteSheetResource SPR_PERK_HALO_ACTIVATEABLE;
    public static SpriteSheetResource SPR_PERK_SEAL;
    public static SpriteSheetResource SPR_PERK_SEAL_BREAK;
    public static SpriteSheetResource SPR_PERK_UNLOCK;

    // ---- VFX sprite sheets ----
    public static SpriteSheetResource SPR_COLLECTOR_EFFECT;
    public static SpriteSheetResource SPR_CRAFT_BURST;
    public static SpriteSheetResource SPR_CRAFT_FLARE;
    public static SpriteSheetResource SPR_ATTUNEMENT_FLARE;
    public static SpriteSheetResource SPR_RELAY_FLARE;
    public static SpriteSheetResource SPR_LIGHTBEAM;
    public static SpriteSheetResource SPR_LIGHTBEAM_TRANSFER;
    public static SpriteSheetResource SPR_ENTITY_FLARE;
    public static SpriteSheetResource SPR_GRAPPLING_HOOK;
    public static SpriteSheetResource SPR_HALO_INFUSION;
    public static SpriteSheetResource SPR_HALO_RITUAL;
    public static SpriteSheetResource SPR_FOUNTAIN_LIQUID;
    public static SpriteSheetResource SPR_FOUNTAIN_VORTEX;
    public static SpriteSheetResource SPR_DAZZLING_AURA;

    // ---- HUD overlays ----
    public static SpriteSheetResource SPR_OVERLAY_CHARGE;
    public static SpriteSheetResource SPR_OVERLAY_CHARGE_COLORLESS;
    public static SpriteSheetResource SPR_STARLIGHT_STORE;
}
