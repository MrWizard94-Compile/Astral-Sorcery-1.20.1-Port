/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.constellation.Constellation;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import javax.annotation.Nonnull;

/**
 * Defines all 16 Astral Sorcery constellations.
 * Each constellation is built with a star pattern (positions on a 31x31 grid)
 * and star connections forming the visual shape.
 *
 * <p>Constellations are grouped into:
 * <ul>
 *   <li>5 Major — always discoverable, provide core attunement effects</li>
 *   <li>7 Weak — require specific conditions to discover</li>
 *   <li>4 Minor — rare, powerful trait constellations</li>
 * </ul>
 *
 * <p>Called during FMLCommonSetupEvent to populate the ConstellationRegistry.</p>
 */
@SuppressWarnings("null")
public final class ConstellationsAS {

    private ConstellationsAS() {}

    // =========================================================================
    // Major Constellations (5)
    // =========================================================================

    @Nonnull public static Constellation.Major DISCIDIA;
    @Nonnull public static Constellation.Major ARMARA;
    @Nonnull public static Constellation.Major VICIO;
    @Nonnull public static Constellation.Major AEVITAS;
    @Nonnull public static Constellation.Major EVORSIO;

    // =========================================================================
    // Weak Constellations (7)
    // =========================================================================

    @Nonnull public static Constellation.Weak LUCERNA;
    @Nonnull public static Constellation.Weak MINERALIS;
    @Nonnull public static Constellation.Weak HOROLOGIUM;
    @Nonnull public static Constellation.Weak OCTANS;
    @Nonnull public static Constellation.Weak BOOTES;
    @Nonnull public static Constellation.Weak FORNAX;
    @Nonnull public static Constellation.Weak PELOTRIO;

    // =========================================================================
    // Minor Constellations (4)
    // =========================================================================

    @Nonnull public static Constellation.Minor GELU;
    @Nonnull public static Constellation.Minor ULTERIA;
    @Nonnull public static Constellation.Minor ALCARA;
    @Nonnull public static Constellation.Minor VORUX;

    /**
     * Initialize all constellations and register them.
     */
    public static void init() {
        DISCIDIA = buildDiscidia();
        ARMARA = buildArmara();
        VICIO = buildVicio();
        AEVITAS = buildAevitas();
        EVORSIO = buildEvorsio();

        LUCERNA = buildLucerna();
        MINERALIS = buildMineralis();
        HOROLOGIUM = buildHorologium();
        OCTANS = buildOctans();
        BOOTES = buildBootes();
        FORNAX = buildFornax();
        PELOTRIO = buildPelotrio();

        GELU = buildGelu();
        ULTERIA = buildUlteria();
        ALCARA = buildAlcara();
        VORUX = buildVorux();

        // Register all
        ConstellationRegistry.addConstellation(DISCIDIA);
        ConstellationRegistry.addConstellation(ARMARA);
        ConstellationRegistry.addConstellation(VICIO);
        ConstellationRegistry.addConstellation(AEVITAS);
        ConstellationRegistry.addConstellation(EVORSIO);
        ConstellationRegistry.addConstellation(LUCERNA);
        ConstellationRegistry.addConstellation(MINERALIS);
        ConstellationRegistry.addConstellation(HOROLOGIUM);
        ConstellationRegistry.addConstellation(OCTANS);
        ConstellationRegistry.addConstellation(BOOTES);
        ConstellationRegistry.addConstellation(FORNAX);
        ConstellationRegistry.addConstellation(PELOTRIO);
        ConstellationRegistry.addConstellation(GELU);
        ConstellationRegistry.addConstellation(ULTERIA);
        ConstellationRegistry.addConstellation(ALCARA);
        ConstellationRegistry.addConstellation(VORUX);
    }

    // =========================================================================
    // Major constellation builders
    // =========================================================================

    private static Constellation.Major buildDiscidia() {
        Constellation.Major c = new Constellation.Major("discidia");
        StarLocation s1 = c.addStar(15, 4);
        StarLocation s2 = c.addStar(8, 10);
        StarLocation s3 = c.addStar(22, 10);
        StarLocation s4 = c.addStar(15, 16);
        StarLocation s5 = c.addStar(10, 24);
        StarLocation s6 = c.addStar(20, 24);
        c.addConnection(s1, s2);
        c.addConnection(s1, s3);
        c.addConnection(s2, s4);
        c.addConnection(s3, s4);
        c.addConnection(s4, s5);
        c.addConnection(s4, s6);
        return c;
    }

    private static Constellation.Major buildArmara() {
        Constellation.Major c = new Constellation.Major("armara");
        StarLocation s1 = c.addStar(15, 5);
        StarLocation s2 = c.addStar(8, 12);
        StarLocation s3 = c.addStar(22, 12);
        StarLocation s4 = c.addStar(8, 22);
        StarLocation s5 = c.addStar(22, 22);
        StarLocation s6 = c.addStar(15, 28);
        c.addConnection(s1, s2);
        c.addConnection(s1, s3);
        c.addConnection(s2, s4);
        c.addConnection(s3, s5);
        c.addConnection(s4, s6);
        c.addConnection(s5, s6);
        return c;
    }

    private static Constellation.Major buildVicio() {
        Constellation.Major c = new Constellation.Major("vicio");
        StarLocation s1 = c.addStar(6, 15);
        StarLocation s2 = c.addStar(12, 8);
        StarLocation s3 = c.addStar(20, 8);
        StarLocation s4 = c.addStar(26, 15);
        StarLocation s5 = c.addStar(20, 22);
        StarLocation s6 = c.addStar(12, 22);
        c.addConnection(s1, s2);
        c.addConnection(s2, s3);
        c.addConnection(s3, s4);
        c.addConnection(s4, s5);
        c.addConnection(s5, s6);
        c.addConnection(s6, s1);
        return c;
    }

    private static Constellation.Major buildAevitas() {
        Constellation.Major c = new Constellation.Major("aevitas");
        StarLocation s1 = c.addStar(15, 4);
        StarLocation s2 = c.addStar(10, 12);
        StarLocation s3 = c.addStar(20, 12);
        StarLocation s4 = c.addStar(15, 18);
        StarLocation s5 = c.addStar(8, 26);
        StarLocation s6 = c.addStar(22, 26);
        StarLocation s7 = c.addStar(15, 28);
        c.addConnection(s1, s2);
        c.addConnection(s1, s3);
        c.addConnection(s2, s4);
        c.addConnection(s3, s4);
        c.addConnection(s4, s5);
        c.addConnection(s4, s6);
        c.addConnection(s5, s7);
        c.addConnection(s6, s7);
        return c;
    }

    private static Constellation.Major buildEvorsio() {
        Constellation.Major c = new Constellation.Major("evorsio");
        StarLocation s1 = c.addStar(15, 6);
        StarLocation s2 = c.addStar(8, 14);
        StarLocation s3 = c.addStar(22, 14);
        StarLocation s4 = c.addStar(6, 24);
        StarLocation s5 = c.addStar(15, 20);
        StarLocation s6 = c.addStar(24, 24);
        c.addConnection(s1, s2);
        c.addConnection(s1, s3);
        c.addConnection(s2, s4);
        c.addConnection(s2, s5);
        c.addConnection(s3, s5);
        c.addConnection(s3, s6);
        return c;
    }

    // =========================================================================
    // Weak constellation builders
    // =========================================================================

    private static Constellation.Weak buildLucerna() {
        Constellation.Weak c = new Constellation.Weak("lucerna");
        StarLocation s1 = c.addStar(15, 5);
        StarLocation s2 = c.addStar(10, 15);
        StarLocation s3 = c.addStar(20, 15);
        StarLocation s4 = c.addStar(15, 26);
        c.addConnection(s1, s2);
        c.addConnection(s1, s3);
        c.addConnection(s2, s4);
        c.addConnection(s3, s4);
        return c;
    }

    private static Constellation.Weak buildMineralis() {
        Constellation.Weak c = new Constellation.Weak("mineralis");
        StarLocation s1 = c.addStar(10, 8);
        StarLocation s2 = c.addStar(22, 8);
        StarLocation s3 = c.addStar(15, 16);
        StarLocation s4 = c.addStar(8, 24);
        StarLocation s5 = c.addStar(22, 24);
        c.addConnection(s1, s3);
        c.addConnection(s2, s3);
        c.addConnection(s3, s4);
        c.addConnection(s3, s5);
        return c;
    }

    private static Constellation.Weak buildHorologium() {
        Constellation.Weak c = new Constellation.Weak("horologium");
        StarLocation s1 = c.addStar(15, 6);
        StarLocation s2 = c.addStar(8, 15);
        StarLocation s3 = c.addStar(22, 15);
        StarLocation s4 = c.addStar(15, 24);
        StarLocation s5 = c.addStar(15, 15);
        c.addConnection(s1, s5);
        c.addConnection(s2, s5);
        c.addConnection(s3, s5);
        c.addConnection(s4, s5);
        return c;
    }

    private static Constellation.Weak buildOctans() {
        Constellation.Weak c = new Constellation.Weak("octans");
        StarLocation s1 = c.addStar(15, 6);
        StarLocation s2 = c.addStar(6, 20);
        StarLocation s3 = c.addStar(24, 20);
        c.addConnection(s1, s2);
        c.addConnection(s2, s3);
        c.addConnection(s3, s1);
        return c;
    }

    private static Constellation.Weak buildBootes() {
        Constellation.Weak c = new Constellation.Weak("bootes");
        StarLocation s1 = c.addStar(15, 4);
        StarLocation s2 = c.addStar(12, 12);
        StarLocation s3 = c.addStar(18, 12);
        StarLocation s4 = c.addStar(15, 20);
        StarLocation s5 = c.addStar(15, 28);
        c.addConnection(s1, s2);
        c.addConnection(s1, s3);
        c.addConnection(s2, s4);
        c.addConnection(s3, s4);
        c.addConnection(s4, s5);
        return c;
    }

    private static Constellation.Weak buildFornax() {
        Constellation.Weak c = new Constellation.Weak("fornax");
        StarLocation s1 = c.addStar(8, 8);
        StarLocation s2 = c.addStar(22, 8);
        StarLocation s3 = c.addStar(15, 16);
        StarLocation s4 = c.addStar(8, 24);
        StarLocation s5 = c.addStar(22, 24);
        c.addConnection(s1, s2);
        c.addConnection(s1, s3);
        c.addConnection(s2, s3);
        c.addConnection(s3, s4);
        c.addConnection(s3, s5);
        return c;
    }

    private static Constellation.Weak buildPelotrio() {
        Constellation.Weak c = new Constellation.Weak("pelotrio");
        StarLocation s1 = c.addStar(15, 4);
        StarLocation s2 = c.addStar(6, 12);
        StarLocation s3 = c.addStar(24, 12);
        StarLocation s4 = c.addStar(10, 22);
        StarLocation s5 = c.addStar(20, 22);
        StarLocation s6 = c.addStar(15, 28);
        c.addConnection(s1, s2);
        c.addConnection(s1, s3);
        c.addConnection(s2, s4);
        c.addConnection(s3, s5);
        c.addConnection(s4, s6);
        c.addConnection(s5, s6);
        return c;
    }

    // =========================================================================
    // Minor constellation builders
    // =========================================================================

    private static Constellation.Minor buildGelu() {
        Constellation.Minor c = new Constellation.Minor("gelu");
        StarLocation s1 = c.addStar(15, 8);
        StarLocation s2 = c.addStar(10, 16);
        StarLocation s3 = c.addStar(20, 16);
        StarLocation s4 = c.addStar(15, 24);
        c.addConnection(s1, s2);
        c.addConnection(s1, s3);
        c.addConnection(s2, s4);
        c.addConnection(s3, s4);
        return c;
    }

    private static Constellation.Minor buildUlteria() {
        Constellation.Minor c = new Constellation.Minor("ulteria");
        StarLocation s1 = c.addStar(15, 6);
        StarLocation s2 = c.addStar(8, 18);
        StarLocation s3 = c.addStar(22, 18);
        StarLocation s4 = c.addStar(15, 26);
        c.addConnection(s1, s2);
        c.addConnection(s2, s4);
        c.addConnection(s1, s3);
        c.addConnection(s3, s4);
        return c;
    }

    private static Constellation.Minor buildAlcara() {
        Constellation.Minor c = new Constellation.Minor("alcara");
        StarLocation s1 = c.addStar(15, 5);
        StarLocation s2 = c.addStar(15, 15);
        StarLocation s3 = c.addStar(8, 25);
        StarLocation s4 = c.addStar(22, 25);
        c.addConnection(s1, s2);
        c.addConnection(s2, s3);
        c.addConnection(s2, s4);
        return c;
    }

    private static Constellation.Minor buildVorux() {
        Constellation.Minor c = new Constellation.Minor("vorux");
        StarLocation s1 = c.addStar(15, 8);
        StarLocation s2 = c.addStar(6, 14);
        StarLocation s3 = c.addStar(24, 14);
        StarLocation s4 = c.addStar(10, 24);
        StarLocation s5 = c.addStar(20, 24);
        c.addConnection(s1, s2);
        c.addConnection(s1, s3);
        c.addConnection(s2, s4);
        c.addConnection(s3, s5);
        c.addConnection(s4, s5);
        return c;
    }
}
