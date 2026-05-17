/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.structure.PatternBlockArray;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines all multiblock structure patterns for Astral Sorcery.
 * These patterns are used to validate whether the correct blocks
 * surround altars, pedestals, and other multiblock machines.
 *
 * <p>Patterns are defined as relative offsets from the center block
 * (the altar/pedestal block entity). Matching is done block-by-block
 * using {@link PatternBlockArray#matches(net.minecraft.world.level.Level, net.minecraft.core.BlockPos)}.</p>
 *
 * <p>Replaces ObserverLib pattern definitions. All patterns are built
 * lazily on first access (after block registry is frozen) to avoid
 * RegistryObject resolution during class loading.</p>
 */
public final class StructuresAS {

    private StructuresAS() {}

    private static final List<PatternBlockArray> ALL_PATTERNS = new ArrayList<>();

    // Patterns are built lazily to avoid RegistryObject resolution at class-load time
    private static boolean initialized = false;

    private static PatternBlockArray patternAltarDiscovery;
    private static PatternBlockArray patternAltarAttunement;
    private static PatternBlockArray patternAltarConstellation;
    private static PatternBlockArray patternAltarRadiance;
    private static PatternBlockArray patternAttunementAltar;
    private static PatternBlockArray patternInfuser;
    private static PatternBlockArray patternRitualPedestal;
    private static PatternBlockArray patternGateway;
    private static PatternBlockArray patternFountain;
    private static PatternBlockArray patternTreeBeacon;

    /**
     * Initialize all structure patterns. Must be called after registry freeze
     * (e.g., during FMLCommonSetupEvent).
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        patternAltarDiscovery = buildAltarDiscovery();
        patternAltarAttunement = buildAltarAttunement();
        patternAltarConstellation = buildAltarConstellation();
        patternAltarRadiance = buildAltarRadiance();
        patternAttunementAltar = buildAttunementAltar();
        patternInfuser = buildInfuser();
        patternRitualPedestal = buildRitualPedestal();
        patternGateway = buildGateway();
        patternFountain = buildFountain();
        patternTreeBeacon = buildTreeBeacon();
    }

    // ---- Accessors ----

    public static PatternBlockArray getAltarDiscovery() { ensureInit(); return patternAltarDiscovery; }
    public static PatternBlockArray getAltarAttunement() { ensureInit(); return patternAltarAttunement; }
    public static PatternBlockArray getAltarConstellation() { ensureInit(); return patternAltarConstellation; }
    public static PatternBlockArray getAltarRadiance() { ensureInit(); return patternAltarRadiance; }
    public static PatternBlockArray getAttunementAltar() { ensureInit(); return patternAttunementAltar; }
    public static PatternBlockArray getInfuser() { ensureInit(); return patternInfuser; }
    public static PatternBlockArray getRitualPedestal() { ensureInit(); return patternRitualPedestal; }
    public static PatternBlockArray getGateway() { ensureInit(); return patternGateway; }
    public static PatternBlockArray getFountain() { ensureInit(); return patternFountain; }
    public static PatternBlockArray getTreeBeacon() { ensureInit(); return patternTreeBeacon; }

    /**
     * Returns an unmodifiable list of all registered patterns.
     */
    public static List<PatternBlockArray> getAllPatterns() {
        ensureInit();
        return Collections.unmodifiableList(ALL_PATTERNS);
    }

    private static void ensureInit() {
        if (!initialized) {
            init();
        }
    }

    // ========================================================================
    // Pattern builders
    // ========================================================================

    /**
     * Discovery tier altar: simple marble frame around the altar.
     * <pre>
     * Layer y=-1 (below altar):
     *   M . M
     *   . . .
     *   M . M
     * where M = marble_raw
     * </pre>
     */
    private static PatternBlockArray buildAltarDiscovery() {
        PatternBlockArray pattern = register("altar_discovery");
        // 4 marble pillars at corners below the altar
        pattern.addBlock(BlocksAS.MARBLE_RAW.get().defaultBlockState(), -2, -1, -2);
        pattern.addBlock(BlocksAS.MARBLE_RAW.get().defaultBlockState(), 2, -1, -2);
        pattern.addBlock(BlocksAS.MARBLE_RAW.get().defaultBlockState(), -2, -1, 2);
        pattern.addBlock(BlocksAS.MARBLE_RAW.get().defaultBlockState(), 2, -1, 2);
        return pattern;
    }

    /**
     * Attunement tier altar: marble frame + marble pillars.
     * Extends discovery with a cross of marble blocks at y=-1.
     */
    private static PatternBlockArray buildAltarAttunement() {
        PatternBlockArray pattern = register("altar_attunement");
        // Include discovery base
        pattern.addBlock(BlocksAS.MARBLE_RAW.get().defaultBlockState(), -2, -1, -2);
        pattern.addBlock(BlocksAS.MARBLE_RAW.get().defaultBlockState(), 2, -1, -2);
        pattern.addBlock(BlocksAS.MARBLE_RAW.get().defaultBlockState(), -2, -1, 2);
        pattern.addBlock(BlocksAS.MARBLE_RAW.get().defaultBlockState(), 2, -1, 2);

        // Marble arch cross arms at y=-1
        pattern.addBlock(BlocksAS.MARBLE_ARCH.get().defaultBlockState(), -3, -1, 0);
        pattern.addBlock(BlocksAS.MARBLE_ARCH.get().defaultBlockState(), 3, -1, 0);
        pattern.addBlock(BlocksAS.MARBLE_ARCH.get().defaultBlockState(), 0, -1, -3);
        pattern.addBlock(BlocksAS.MARBLE_ARCH.get().defaultBlockState(), 0, -1, 3);

        // Corner pillars at y=0 and y=1
        for (int y = 0; y <= 1; y++) {
            pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), -3, y, -3);
            pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), 3, y, -3);
            pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), -3, y, 3);
            pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), 3, y, 3);
        }
        return pattern;
    }

    /**
     * Constellation tier altar: attunement + sodalite + additional pillars.
     * The original mod uses marble runed blocks for this tier.
     */
    private static PatternBlockArray buildAltarConstellation() {
        PatternBlockArray pattern = register("altar_constellation");
        // Base platform of marble at y=-1
        pattern.addBlockCube(BlocksAS.MARBLE_RAW.get().defaultBlockState(),
                -3, -1, -3, 3, -1, 3);

        // Runed marble at corners of the base
        pattern.addBlock(BlocksAS.MARBLE_RUNED.get().defaultBlockState(), -3, -1, -3);
        pattern.addBlock(BlocksAS.MARBLE_RUNED.get().defaultBlockState(), 3, -1, -3);
        pattern.addBlock(BlocksAS.MARBLE_RUNED.get().defaultBlockState(), -3, -1, 3);
        pattern.addBlock(BlocksAS.MARBLE_RUNED.get().defaultBlockState(), 3, -1, 3);

        // Tall pillars at corners (y=0 to y=3)
        for (int y = 0; y <= 3; y++) {
            pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), -4, y, -4);
            pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), 4, y, -4);
            pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), -4, y, 4);
            pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), 4, y, 4);
        }

        // Relay pedestals at cardinal directions
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), -4, 0, 0);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 4, 0, 0);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 0, 0, -4);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 0, 0, 4);
        return pattern;
    }

    /**
     * Radiance tier altar: full maxed-out multiblock with relays,
     * chalices, and celestial crystal mounts.
     */
    private static PatternBlockArray buildAltarRadiance() {
        PatternBlockArray pattern = register("altar_radiance");
        // Full marble platform
        pattern.addBlockCube(BlocksAS.MARBLE_RAW.get().defaultBlockState(),
                -4, -1, -4, 4, -1, 4);

        // Runed marble perimeter
        for (int i = -4; i <= 4; i++) {
            pattern.addBlock(BlocksAS.MARBLE_RUNED.get().defaultBlockState(), i, -1, -4);
            pattern.addBlock(BlocksAS.MARBLE_RUNED.get().defaultBlockState(), i, -1, 4);
            pattern.addBlock(BlocksAS.MARBLE_RUNED.get().defaultBlockState(), -4, -1, i);
            pattern.addBlock(BlocksAS.MARBLE_RUNED.get().defaultBlockState(), 4, -1, i);
        }

        // Grand pillars
        for (int y = 0; y <= 4; y++) {
            pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), -5, y, -5);
            pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), 5, y, -5);
            pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), -5, y, 5);
            pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), 5, y, 5);
        }

        // Relay positions at diagonals
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), -3, 0, -3);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 3, 0, -3);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), -3, 0, 3);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 3, 0, 3);

        // Cardinal relay pedestals
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), -5, 0, 0);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 5, 0, 0);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 0, 0, -5);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 0, 0, 5);
        return pattern;
    }

    /**
     * Attunement altar: the constellation attunement structure.
     * Uses a circular marble platform with sodalite (marble_chiseled) pillars.
     */
    private static PatternBlockArray buildAttunementAltar() {
        PatternBlockArray pattern = register("attunement_altar");
        // Circular marble platform at y=-1
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (dx * dx + dz * dz <= 10) { // Circular radius ~3.1
                    pattern.addBlock(BlocksAS.MARBLE_RAW.get().defaultBlockState(), dx, -1, dz);
                }
            }
        }

        // Chiseled marble pillars at cardinal +3 positions
        for (int y = 0; y <= 2; y++) {
            pattern.addBlock(BlocksAS.MARBLE_CHISELED.get().defaultBlockState(), 3, y, 0);
            pattern.addBlock(BlocksAS.MARBLE_CHISELED.get().defaultBlockState(), -3, y, 0);
            pattern.addBlock(BlocksAS.MARBLE_CHISELED.get().defaultBlockState(), 0, y, 3);
            pattern.addBlock(BlocksAS.MARBLE_CHISELED.get().defaultBlockState(), 0, y, -3);
        }
        return pattern;
    }

    /**
     * Starlight infuser: marble platform with a liquid starlight pool.
     */
    private static PatternBlockArray buildInfuser() {
        PatternBlockArray pattern = register("infuser");
        // Small marble platform at y=-1
        pattern.addBlockCube(BlocksAS.MARBLE_RAW.get().defaultBlockState(),
                -2, -1, -2, 2, -1, 2);

        // Chalice positions at corners
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), -2, 0, -2);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 2, 0, -2);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), -2, 0, 2);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 2, 0, 2);
        return pattern;
    }

    /**
     * Ritual pedestal: marble ring with pillar supports.
     */
    private static PatternBlockArray buildRitualPedestal() {
        PatternBlockArray pattern = register("ritual_pedestal");
        // Marble ring at y=-1
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    pattern.addBlock(BlocksAS.MARBLE_RAW.get().defaultBlockState(), dx, -1, dz);
                }
            }
        }
        // Pillar at each cardinal +2
        pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), 2, 0, 0);
        pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), -2, 0, 0);
        pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), 0, 0, 2);
        pattern.addBlock(BlocksAS.MARBLE_PILLAR.get().defaultBlockState(), 0, 0, -2);
        return pattern;
    }

    /**
     * Gateway: celestial gateway teleportation structure.
     * A ring of marble with engraved blocks at cardinal positions.
     */
    private static PatternBlockArray buildGateway() {
        PatternBlockArray pattern = register("gateway");
        // Outer ring at y=-1
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq >= 5 && distSq <= 10) {
                    pattern.addBlock(BlocksAS.MARBLE_RAW.get().defaultBlockState(), dx, -1, dz);
                }
            }
        }
        // Engraved blocks at cardinal positions
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 3, -1, 0);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), -3, -1, 0);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 0, -1, 3);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 0, -1, -3);
        return pattern;
    }

    /**
     * Fountain: starlight fountain prime.
     */
    private static PatternBlockArray buildFountain() {
        PatternBlockArray pattern = register("fountain");
        // Small marble base
        pattern.addBlockCube(BlocksAS.MARBLE_RAW.get().defaultBlockState(),
                -1, -1, -1, 1, -1, 1);
        // Engraved corners at y=0
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), -1, 0, -1);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 1, 0, -1);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), -1, 0, 1);
        pattern.addBlock(BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState(), 1, 0, 1);
        return pattern;
    }

    /**
     * Tree beacon: infused wood frame around a natural tree.
     */
    private static PatternBlockArray buildTreeBeacon() {
        PatternBlockArray pattern = register("tree_beacon");
        // Infused wood ring at y=0
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if ((Math.abs(dx) == 2) != (Math.abs(dz) == 2)) {
                    // Ring edges only (not corners for XOR ring)
                    continue;
                }
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    pattern.addBlock(BlocksAS.INFUSED_WOOD.get().defaultBlockState(), dx, 0, dz);
                }
            }
        }
        // Corner pillars of infused wood
        for (int y = 0; y <= 2; y++) {
            pattern.addBlock(BlocksAS.INFUSED_WOOD_COLUMN.get().defaultBlockState(), -2, y, -2);
            pattern.addBlock(BlocksAS.INFUSED_WOOD_COLUMN.get().defaultBlockState(), 2, y, -2);
            pattern.addBlock(BlocksAS.INFUSED_WOOD_COLUMN.get().defaultBlockState(), -2, y, 2);
            pattern.addBlock(BlocksAS.INFUSED_WOOD_COLUMN.get().defaultBlockState(), 2, y, 2);
        }
        return pattern;
    }

    // ========================================================================
    // Registration helper
    // ========================================================================

    private static PatternBlockArray register(String name) {
        PatternBlockArray pattern = new PatternBlockArray(AstralSorcery.key(name));
        ALL_PATTERNS.add(pattern);
        return pattern;
    }
}
