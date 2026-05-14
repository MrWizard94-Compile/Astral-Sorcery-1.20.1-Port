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

    public static final Color STARLIGHT = new Color(0xAADDFF);
}
