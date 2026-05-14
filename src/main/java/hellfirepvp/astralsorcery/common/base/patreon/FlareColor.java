package hellfirepvp.astralsorcery.common.base.patreon;

import java.awt.*;

/**
 * Patreon flare particle colors. Pure data enum with two Color values
 * used for gradient rendering of supporter flare effects.
 */
public enum FlareColor {

    BLUE(0x157AFF, 0xC1D8FF),
    DARK_RED(0xFF0739, 0xFF5555),
    DAWN(0xFF5186, 0xE95C47),
    GOLD(0xFF9116, 0xFFF26E),
    GREEN(0x5BFF37, 0x63FFA3),
    MAGENTA(0xFC7FFC, 0xFFC6FF),
    RED(0xFF0F2B, 0xFF0F59),
    WHITE(0xBFFFFF, 0xFFFFFF),
    YELLOW(0xFFFF55, 0xFDC71F),
    ELDRITCH(0x620280, 0xAE22FF),
    DARK_GREEN(0x00C601, 0x22FF8F),
    FIRE(0xFF4006, 0xFF9900),
    WATER(0x89DFFF, 0x587ADD),
    EARTH(0xD0863D, 0xCEB392),
    AIR(0xFFFFD1, 0xB2DABD),
    STANDARD(0x9918B9, 0x5E5DD6);

    public final Color color1;
    public final Color color2;

    FlareColor(int c1, int c2) {
        this.color1 = new Color(c1);
        this.color2 = new Color(c2);
    }

    // Client-side SpriteQuery method will be added in Phase 12 (rendering)
    // when AssetLoader and SpriteQuery are ported.
}
