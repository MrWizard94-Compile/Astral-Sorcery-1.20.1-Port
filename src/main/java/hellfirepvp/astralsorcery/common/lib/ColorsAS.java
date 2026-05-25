package hellfirepvp.astralsorcery.common.lib;

import java.awt.*;

/**
 * Color constants used throughout Astral Sorcery.
 * Additional colors will be added as systems are ported.
 *
 * <p>No 1.16 → 1.20 changes — pure Java Color constants.</p>
 */
public class ColorsAS {

    private ColorsAS() {}

    // Constellation tier colors (used by IConstellation.getTierRenderColor)
    public static final Color CONSTELLATION_TYPE_MAJOR = new Color(40, 67, 204);
    public static final Color CONSTELLATION_TYPE_WEAK = new Color(67, 44, 176);
    public static final Color CONSTELLATION_TYPE_MINOR = new Color(204, 145, 52);

    // General colors (populated as systems are ported)
    public static final Color CONSTELLATION_STAR = new Color(0xCCCCFF);
    public static final Color CONSTELLATION_CONNECTION = new Color(0x0033CC);

    public static final Color ROCK_CRYSTAL = new Color(0xDD80FF);
    public static final Color CELESTIAL_CRYSTAL = new Color(0x4466FF);

    public static final Color EFFECT_BLUE_LIGHT = new Color(0x5555FF);
    public static final Color EFFECT_BLUE_DARK = new Color(0x0000AA);

    // Mob effect colors
    public static final Color EFFECT_BLEED        = new Color(0xAA0000);
    public static final Color EFFECT_CHEAT_DEATH  = new Color(0xFFDD88);
    public static final Color EFFECT_DROP_MODIFIER = new Color(0x00AA66);

    public static final Color STARLIGHT = new Color(0xAADDFF);

    // Mantle effect highlight colors
    public static final Color MANTLE_LUCERNA_SPAWNER   = new Color(0x9C0100);
    public static final Color MANTLE_LUCERNA_INVENTORY = new Color(0x00A903);
    public static final Color CONSTELLATION_LUCERNA    = new Color(0xFFE709);
    public static final Color CONSTELLATION_MINERALIS  = new Color(0xCB7D0A);

    // Alignment charge bar overlay
    public static final Color OVERLAY_CHARGE_MISSING = new Color(0xA20900);
    public static final Color OVERLAY_CHARGE_USAGE   = new Color(0x00B903);
}
