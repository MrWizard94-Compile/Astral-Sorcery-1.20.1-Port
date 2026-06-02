package hellfirepvp.astralsorcery.client.lib;

import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Static references to all Astral Sorcery GUI and effect textures.
 * Initialized once during client setup via {@link #init()}.
 *
 * <p>Paths follow {@code assets/astralsorcery/textures/<location>/<name>.png}
 * where location is one of {@link AssetLoader.TextureLocation}.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class TexturesAS {

    private TexturesAS() {}

    // ---- Non-book GUI ----

    public static AbstractRenderableTexture TEX_GUI_CONSTELLATION_PAPER;
    public static AbstractRenderableTexture TEX_GUI_PARCHMENT_BLANK;
    public static AbstractRenderableTexture TEX_GUI_HAND_TELESCOPE;
    public static AbstractRenderableTexture TEX_GUI_TELESCOPE;
    public static AbstractRenderableTexture TEX_GUI_OBSERVATORY;
    public static AbstractRenderableTexture TEX_GUI_REFRACTION_TABLE_EMPTY;
    public static AbstractRenderableTexture TEX_GUI_REFRACTION_TABLE_PARCHMENT;
    public static AbstractRenderableTexture TEX_GUI_LINE_CONNECTION;
    public static AbstractRenderableTexture TEX_GUI_TEXT_FIELD;
    public static AbstractRenderableTexture TEX_GUI_MENU_SLOT;
    public static AbstractRenderableTexture TEX_GUI_MENU_SLOT_GEM_CONTEXT;

    // ---- Book / journal ----

    public static AbstractRenderableTexture TEX_GUI_BOOKMARK;
    public static AbstractRenderableTexture TEX_GUI_BOOKMARK_STRETCHED;
    public static AbstractRenderableTexture TEX_GUI_BOOK_ARROWS;
    public static AbstractRenderableTexture TEX_GUI_BOOK_UNDERLINE;
    public static AbstractRenderableTexture TEX_GUI_BOOK_BLANK;
    public static AbstractRenderableTexture TEX_GUI_BOOK_FRAME_FULL;
    public static AbstractRenderableTexture TEX_GUI_BOOK_FRAME_LEFT;
    public static AbstractRenderableTexture TEX_GUI_BOOK_STRUCTURE_ICONS;
    public static AbstractRenderableTexture TEX_GUI_BOOK_GRID_SLOT;
    public static AbstractRenderableTexture TEX_GUI_BOOK_GRID_RECIPE;
    public static AbstractRenderableTexture TEX_GUI_BOOK_GRID_TRANSMUTATION;
    public static AbstractRenderableTexture TEX_GUI_BOOK_GRID_INFUSION;
    public static AbstractRenderableTexture TEX_GUI_BOOK_GRID_T1;
    public static AbstractRenderableTexture TEX_GUI_BOOK_GRID_T2;
    public static AbstractRenderableTexture TEX_GUI_BOOK_GRID_T3;
    public static AbstractRenderableTexture TEX_GUI_BOOK_GRID_T4;
    public static AbstractRenderableTexture TEX_GUI_STARFIELD_OVERLAY;
    public static AbstractRenderableTexture TEX_GUI_BACKGROUND_DEFAULT;
    public static AbstractRenderableTexture TEX_GUI_BACKGROUND_PERKS;
    public static AbstractRenderableTexture TEX_GUI_BACKGROUND_CONSTELLATIONS;

    // ---- Progression cluster backgrounds ----

    public static AbstractRenderableTexture TEX_GUI_CLUSTER_DISCOVERY;
    public static AbstractRenderableTexture TEX_GUI_CLUSTER_BASICCRAFT;
    public static AbstractRenderableTexture TEX_GUI_CLUSTER_ATTUNEMENT;
    public static AbstractRenderableTexture TEX_GUI_CLUSTER_CONSTELLATION;
    public static AbstractRenderableTexture TEX_GUI_CLUSTER_RADIANCE;
    public static AbstractRenderableTexture TEX_GUI_CLUSTER_BRILLIANCE;

    // ---- Perk tree ----

    public static AbstractRenderableTexture TEX_GUI_PERK_INACTIVE;
    public static AbstractRenderableTexture TEX_GUI_PERK_ACTIVE;
    public static AbstractRenderableTexture TEX_GUI_PERK_ACTIVATEABLE;
    public static AbstractRenderableTexture TEX_GUI_PERK_HALO_INACTIVE;
    public static AbstractRenderableTexture TEX_GUI_PERK_HALO_ACTIVE;
    public static AbstractRenderableTexture TEX_GUI_PERK_HALO_ACTIVATEABLE;

    // ---- Research frame ----

    public static AbstractRenderableTexture TEX_GUI_RESEARCH_FRAME_WOOD;

    // ---- Misc ----

    public static AbstractRenderableTexture TEX_BLACK;

    public static void init() {
        TEX_GUI_CONSTELLATION_PAPER         = load("constellation_paper");
        TEX_GUI_PARCHMENT_BLANK             = load("parchment_blank");
        TEX_GUI_HAND_TELESCOPE              = load("hand_telescope_frame");
        TEX_GUI_TELESCOPE                   = load("telescope_frame");
        TEX_GUI_OBSERVATORY                 = load("observatory_frame");
        TEX_GUI_REFRACTION_TABLE_EMPTY      = load("refraction_table_empty");
        TEX_GUI_REFRACTION_TABLE_PARCHMENT  = load("refraction_table_parchment");
        TEX_GUI_LINE_CONNECTION             = load("line_connection");
        TEX_GUI_TEXT_FIELD                  = load("text_field");
        TEX_GUI_MENU_SLOT                   = load("menu_slot");
        TEX_GUI_MENU_SLOT_GEM_CONTEXT       = load("menu_slot_gem_context");

        TEX_GUI_BOOKMARK                    = load("book/bookmark");
        TEX_GUI_BOOKMARK_STRETCHED          = load("book/bookmark_stretched");
        TEX_GUI_BOOK_ARROWS                 = load("book/arrows");
        TEX_GUI_BOOK_UNDERLINE              = load("book/underline");
        TEX_GUI_BOOK_BLANK                  = load("book/blank");
        TEX_GUI_BOOK_FRAME_FULL             = load("book/frame_full");
        TEX_GUI_BOOK_FRAME_LEFT             = load("book/frame_left");
        TEX_GUI_BOOK_STRUCTURE_ICONS        = load("book/structure_icons");
        TEX_GUI_BOOK_GRID_SLOT              = load("book/grid_slot");
        TEX_GUI_BOOK_GRID_RECIPE            = load("book/grid_recipe");
        TEX_GUI_BOOK_GRID_TRANSMUTATION     = load("book/grid_transmutation");
        TEX_GUI_BOOK_GRID_INFUSION          = load("book/grid_infusion");
        TEX_GUI_BOOK_GRID_T1                = load("book/grid_t1");
        TEX_GUI_BOOK_GRID_T2                = load("book/grid_t2");
        TEX_GUI_BOOK_GRID_T3                = load("book/grid_t3");
        TEX_GUI_BOOK_GRID_T4                = load("book/grid_t4");
        TEX_GUI_STARFIELD_OVERLAY           = load("book/starfield_overlay");
        TEX_GUI_BACKGROUND_DEFAULT          = load("book/background_default");
        TEX_GUI_BACKGROUND_PERKS            = load("book/background_perks");
        TEX_GUI_BACKGROUND_CONSTELLATIONS   = load("book/background_constellations");

        TEX_GUI_CLUSTER_DISCOVERY           = load("book/cluster_discovery");
        TEX_GUI_CLUSTER_BASICCRAFT          = load("book/cluster_basiccraft");
        TEX_GUI_CLUSTER_ATTUNEMENT          = load("book/cluster_attunement");
        TEX_GUI_CLUSTER_CONSTELLATION       = load("book/cluster_constellation");
        TEX_GUI_CLUSTER_RADIANCE            = load("book/cluster_radiance");
        TEX_GUI_CLUSTER_BRILLIANCE          = load("book/cluster_brilliance");

        TEX_GUI_PERK_INACTIVE               = loadPerk("inactive");
        TEX_GUI_PERK_ACTIVE                 = loadPerk("active");
        TEX_GUI_PERK_ACTIVATEABLE           = loadPerk("activateable");
        TEX_GUI_PERK_HALO_INACTIVE          = loadPerk("halo_inactive");
        TEX_GUI_PERK_HALO_ACTIVE            = loadPerk("halo_active");
        TEX_GUI_PERK_HALO_ACTIVATEABLE      = loadPerk("halo_activateable");

        TEX_GUI_RESEARCH_FRAME_WOOD         = load("research_frame_wood");

        TEX_BLACK = AssetLoader.loadTexture(AssetLoader.TextureLocation.MISC, "black");
    }

    private static AbstractRenderableTexture load(String name) {
        return AssetLoader.loadTexture(AssetLoader.TextureLocation.GUI, name);
    }

    private static AbstractRenderableTexture loadPerk(String name) {
        return AssetLoader.loadTexture(AssetLoader.TextureLocation.GUI, "perk/" + name);
    }
}
