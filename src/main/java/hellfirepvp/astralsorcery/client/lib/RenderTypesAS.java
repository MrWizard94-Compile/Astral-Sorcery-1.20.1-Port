/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.lib;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * All custom {@link RenderType} definitions for Astral Sorcery rendering.
 *
 * <p>1.16 → 1.20 changes:
 * {@code RenderType.State.Builder} → {@code RenderType.CompositeState.builder()}.
 * {@code DefaultVertexFormats} → {@code DefaultVertexFormat} (singular).
 * {@code RenderState.X} → {@code RenderStateShard.X}.
 * All render state components are now inner classes of {@code RenderStateShard}.</p>
 *
 * <p>Every render type that AS uses falls into one of these categories:</p>
 * <ul>
 *   <li><b>Effect types</b> — particles, light beams, lightning, bursts, crystals</li>
 *   <li><b>Constellation types</b> — star/line overlays in sky, GUI, and perk tree</li>
 *   <li><b>Block overlay types</b> — translucent block highlights, structure previews</li>
 *   <li><b>GUI types</b> — flat 2D rendering in screens and journal</li>
 *   <li><b>Sky types</b> — custom sky dome, constellation sky layer</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public final class RenderTypesAS {

    private RenderTypesAS() {}

    // =========================================================================
    // Texture locations used by render types
    // =========================================================================

    public static final ResourceLocation TEX_PARTICLE_SMALL =
            AstralSorcery.key("textures/effect/particle_small.png");
    public static final ResourceLocation TEX_PARTICLE_LARGE =
            AstralSorcery.key("textures/effect/particle_large.png");
    public static final ResourceLocation TEX_STAR =
            AstralSorcery.key("textures/effect/star.png");
    public static final ResourceLocation TEX_LIGHTBEAM =
            AstralSorcery.key("textures/effect/lightbeam.png");
    public static final ResourceLocation TEX_LIGHTNING =
            AstralSorcery.key("textures/effect/lightning.png");
    public static final ResourceLocation TEX_BURST =
            AstralSorcery.key("textures/effect/burst.png");
    public static final ResourceLocation TEX_HALO =
            AstralSorcery.key("textures/effect/halo.png");
    public static final ResourceLocation TEX_CUBE =
            AstralSorcery.key("textures/effect/cube.png");
    public static final ResourceLocation TEX_CONSTELLATION_STAR =
            AstralSorcery.key("textures/gui/constellation_star.png");
    public static final ResourceLocation TEX_CONSTELLATION_CONNECTION =
            AstralSorcery.key("textures/gui/constellation_connection.png");
    public static final ResourceLocation TEX_SKY_CONSTELLATION =
            AstralSorcery.key("textures/environment/sky_constellation.png");
    public static final ResourceLocation TEX_BLANK =
            AstralSorcery.key("textures/effect/blank.png");

    // =========================================================================
    // Shared render state shards
    // =========================================================================

    /** Additive blending: src * srcAlpha + dst * 1. Used for glow effects. */
    private static final RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("as_additive_transparency",
                    () -> {
                        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                        com.mojang.blaze3d.systems.RenderSystem.blendFunc(
                                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
                    },
                    () -> {
                        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                    });

    /** Standard alpha blending: src * srcAlpha + dst * (1 - srcAlpha). */
    private static final RenderStateShard.TransparencyStateShard NORMAL_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("as_normal_transparency",
                    () -> {
                        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                    },
                    () -> com.mojang.blaze3d.systems.RenderSystem.disableBlend());

    /** No depth writes — used for overlays that shouldn't occlude. */
    private static final RenderStateShard.WriteMaskStateShard COLOR_WRITE =
            new RenderStateShard.WriteMaskStateShard(true, false);

    /** Full-bright lightmap override. */
    private static final RenderStateShard.LightmapStateShard LIGHTMAP_ENABLED =
            new RenderStateShard.LightmapStateShard(true);

    /** No lightmap — self-illuminated. */
    private static final RenderStateShard.LightmapStateShard LIGHTMAP_DISABLED =
            new RenderStateShard.LightmapStateShard(false);

    /** No culling — render both sides. */
    private static final RenderStateShard.CullStateShard NO_CULL =
            new RenderStateShard.CullStateShard(false);

    /** Depth test disabled. */
    private static final RenderStateShard.DepthTestStateShard NO_DEPTH_TEST =
            new RenderStateShard.DepthTestStateShard("always", 519); // GL_ALWAYS

    // =========================================================================
    // 12a: Effect render types — particles, beams, lightning, bursts
    // =========================================================================

    /**
     * Generic particle rendering: textured quads with additive blending,
     * no depth write, no culling. Used by most AS particle effects.
     */
    public static final RenderType EFFECT_FX_GENERIC_PARTICLE = createType(
            "effect_fx_generic_particle",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            TEX_PARTICLE_SMALL, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    /**
     * Same as generic particle but with depth testing enabled.
     * Used for particles that should be occluded by world geometry.
     */
    public static final RenderType EFFECT_FX_GENERIC_PARTICLE_DEPTH = createType(
            "effect_fx_generic_particle_depth",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            TEX_PARTICLE_SMALL, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    /**
     * Large particle rendering with separate texture.
     */
    public static final RenderType EFFECT_FX_LARGE_PARTICLE = createType(
            "effect_fx_large_particle",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            TEX_PARTICLE_LARGE, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    /**
     * Light beam rendering: elongated textured quads with additive blending.
     * Used for starlight transmission beams between network nodes.
     */
    public static final RenderType EFFECT_FX_LIGHTBEAM = createType(
            "effect_fx_lightbeam",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            TEX_LIGHTBEAM, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    /**
     * Lightning effect: jagged line segments with additive blending.
     */
    public static final RenderType EFFECT_FX_LIGHTNING = createType(
            "effect_fx_lightning",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            TEX_LIGHTNING, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    /**
     * Burst/explosion effect rendering.
     */
    public static final RenderType EFFECT_FX_BURST = createType(
            "effect_fx_burst",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            TEX_BURST, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    /**
     * Crystal rendering: translucent textured quads with normal transparency.
     * Used by collector crystals, celestial crystals, etc.
     */
    public static final RenderType EFFECT_FX_CRYSTAL = createType(
            "effect_fx_crystal",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX_LIGHTMAP)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            TEX_CUBE, false, false))
                    .setTransparencyState(NORMAL_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP_ENABLED)
                    .createCompositeState(false));

    /**
     * Halo/glow rendering: large additive quads behind crystals and altars.
     */
    public static final RenderType EFFECT_FX_HALO = createType(
            "effect_fx_halo",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            TEX_HALO, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    /**
     * Translucent block overlay: used for structure preview and ritual area highlights.
     * Uses block atlas texture with normal transparency.
     */
    public static final RenderType EFFECT_FX_BLOCK_TRANSLUCENT = createType(
            "effect_fx_block_translucent",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            2097152,
            true, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.RENDERTYPE_TRANSLUCENT)
                    .setTextureState(Shaders.BLOCK_SHEET)
                    .setTransparencyState(NORMAL_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP_ENABLED)
                    .setOutputState(Shaders.TRANSLUCENT)
                    .createCompositeState(true));

    // =========================================================================
    // Constellation render types — star/line overlays
    // =========================================================================

    /**
     * Constellation star rendering: textured quads at star positions.
     * Used in sky rendering, journal GUI, and perk tree background.
     */
    public static final RenderType CONSTELLATION_STAR = createType(
            "constellation_star",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            TEX_CONSTELLATION_STAR, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    /**
     * Constellation connection lines: thin quads connecting stars.
     */
    public static final RenderType CONSTELLATION_CONNECTION = createType(
            "constellation_connection",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            TEX_CONSTELLATION_CONNECTION, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    // =========================================================================
    // Sky render types
    // =========================================================================

    /**
     * Sky constellation layer: rendered on the custom sky dome.
     */
    public static final RenderType SKY_CONSTELLATION = createType(
            "sky_constellation",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            TEX_SKY_CONSTELLATION, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    // =========================================================================
    // GUI / flat-2D render types
    // =========================================================================

    /**
     * Flat position+color quads: used for GUI backgrounds, overlays, color fills.
     * No texture, normal alpha blending, no depth test.
     */
    public static final RenderType GUI_COLOR = createType(
            "gui_color",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR)
                    .setTransparencyState(NORMAL_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    /**
     * Flat textured quads: used for GUI icons, item overlays, etc.
     * Uses whatever texture is currently bound. Normal alpha blending.
     */
    public static final RenderType GUI_TEXTURED = createType(
            "gui_textured",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX)
                    .setTransparencyState(NORMAL_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    /**
     * Flat textured quads with additive blending for GUI glow effects.
     */
    public static final RenderType GUI_TEXTURED_ADDITIVE = createType(
            "gui_textured_additive",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR_TEX)
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    // =========================================================================
    // Utility: position+color (no texture) for world-space overlays
    // =========================================================================

    /**
     * World-space position+color quads with normal transparency.
     * Used for debug overlays, area markers, etc.
     */
    public static final RenderType POSITION_COLOR_NO_DEPTH = createType(
            "position_color_no_depth",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.POSITION_COLOR)
                    .setTransparencyState(NORMAL_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    /**
     * World-space lines for debug/structure outlines.
     */
    public static final RenderType OVERLAY_LINE = createType(
            "overlay_line",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(Shaders.RENDERTYPE_LINES)
                    .setTransparencyState(NORMAL_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setLineState(Shaders.lineState(2.0))
                    .setLightmapState(LIGHTMAP_DISABLED)
                    .createCompositeState(false));

    // =========================================================================
    // Dynamic texture render type factory
    // =========================================================================

    /**
     * Creates a render type for a specific texture with additive blending.
     * Used when different effects need the same state but a different texture.
     *
     * @param texture the texture resource location
     * @return a render type bound to that texture
     */
    @Nonnull
    public static RenderType effectFxTextured(@Nonnull ResourceLocation texture) {
        return createType(
                "effect_fx_textured_" + texture.getPath().replace('/', '_'),
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.QUADS,
                256,
                false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(Shaders.POSITION_COLOR_TEX)
                        .setTextureState(new RenderStateShard.TextureStateShard(
                                texture, false, false))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP_DISABLED)
                        .createCompositeState(false));
    }

    /**
     * Creates a render type for a specific texture with normal (alpha) blending.
     *
     * @param texture the texture resource location
     * @return a render type bound to that texture
     */
    @Nonnull
    public static RenderType guiTextured(@Nonnull ResourceLocation texture) {
        return createType(
                "gui_textured_" + texture.getPath().replace('/', '_'),
                DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.QUADS,
                256,
                false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(Shaders.POSITION_COLOR_TEX)
                        .setTextureState(new RenderStateShard.TextureStateShard(
                                texture, false, false))
                        .setTransparencyState(NORMAL_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setLightmapState(LIGHTMAP_DISABLED)
                        .createCompositeState(false));
    }

    // =========================================================================
    // Internal helper
    // =========================================================================

    @Nonnull
    private static RenderType createType(@Nonnull String name,
                                          @Nonnull VertexFormat format,
                                          @Nonnull VertexFormat.Mode mode,
                                          int bufferSize,
                                          boolean affectsCrumbling,
                                          boolean sortOnUpload,
                                          @Nonnull RenderType.CompositeState state) {
        return RenderType.create(
                AstralSorcery.MODID + ":" + name,
                format, mode, bufferSize,
                affectsCrumbling, sortOnUpload,
                state);
    }

    /**
     * Helper to access protected RenderStateShard constants.
     * In 1.20 these are protected; AS needs them for custom render types.
     */
    private static class Shaders extends RenderStateShard {
        private Shaders() { super("as_dummy", () -> {}, () -> {}); }

        static final ShaderStateShard POSITION_COLOR_TEX = POSITION_COLOR_TEX_SHADER;
        static final ShaderStateShard POSITION_COLOR = POSITION_COLOR_SHADER;
        static final ShaderStateShard POSITION_COLOR_TEX_LIGHTMAP = POSITION_COLOR_TEX_LIGHTMAP_SHADER;
        static final ShaderStateShard RENDERTYPE_TRANSLUCENT = RENDERTYPE_TRANSLUCENT_SHADER;
        static final ShaderStateShard RENDERTYPE_LINES = RENDERTYPE_LINES_SHADER;
        static final TextureStateShard BLOCK_SHEET = BLOCK_SHEET_MIPPED;
        static final OutputStateShard TRANSLUCENT = TRANSLUCENT_TARGET;

        static LineStateShard lineState(double width) {
            return new LineStateShard(java.util.OptionalDouble.of(width));
        }
    }
}
