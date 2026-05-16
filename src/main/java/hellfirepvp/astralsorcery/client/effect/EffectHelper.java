/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.effect;

import hellfirepvp.astralsorcery.client.effect.FXOrbital;
import hellfirepvp.astralsorcery.client.effect.FXVortex;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Random;

/**
 * Static convenience factory for spawning common Astral Sorcery effects.
 * Provides named presets (sparkle, burst, orbital, beam, etc.) that
 * configure and spawn effects through the EffectManager.
 *
 * <p>All methods are client-only. Call from client-side tick handlers
 * or block entity client ticks only.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class EffectHelper {

    private static final Random RAND = new Random();

    private EffectHelper() {}

    // =========================================================================
    // Sparkle / Generic particle
    // =========================================================================

    /**
     * Spawn a single fading sparkle particle at the given position.
     *
     * @param pos   world position
     * @param color particle color
     * @param scale particle scale
     * @param maxAge lifetime in ticks
     */
    @Nonnull
    public static FXParticle sparkle(@Nonnull Vec3 pos, @Nonnull Color color, float scale, int maxAge) {
        FXParticle particle = new FXParticle(pos.x, pos.y, pos.z);
        EffectProperties props = EffectProperties.create()
                .setColor(color)
                .setScale(scale)
                .setMaxAge(maxAge)
                .setAlphaFadeOut();
        props.applyTo(particle);
        EffectManager.getInstance().spawn(particle);
        return particle;
    }

    /**
     * Spawn a sparkle with slight random motion (floating dust effect).
     */
    @Nonnull
    public static FXParticle sparkleFloating(@Nonnull Vec3 pos, @Nonnull Color color, float scale, int maxAge) {
        FXParticle particle = new FXParticle(pos.x, pos.y, pos.z);
        double mx = (RAND.nextDouble() - 0.5) * 0.02;
        double my = (RAND.nextDouble()) * 0.01;
        double mz = (RAND.nextDouble() - 0.5) * 0.02;
        EffectProperties props = EffectProperties.create()
                .setColor(color)
                .setScale(scale)
                .setMaxAge(maxAge)
                .setMotion(mx, my, mz)
                .setAlphaFadeOut();
        props.applyTo(particle);
        EffectManager.getInstance().spawn(particle);
        return particle;
    }

    // =========================================================================
    // Burst
    // =========================================================================

    /**
     * Spawn a radial burst effect at the given position.
     *
     * @param pos       center position
     * @param color     burst color
     * @param scale     per-ray scale
     * @param rayCount  number of rays
     * @param speed     expansion speed (blocks/tick)
     */
    @Nonnull
    public static FXBurst burst(@Nonnull Vec3 pos, @Nonnull Color color, float scale, int rayCount, float speed) {
        FXBurst burst = new FXBurst(pos.x, pos.y, pos.z, rayCount, speed);
        EffectProperties props = EffectProperties.create()
                .setColor(color)
                .setScale(scale);
        props.applyTo(burst);
        EffectManager.getInstance().spawn(burst);
        return burst;
    }

    /**
     * Spawn a standard starlight burst (12 rays, blue-white, medium speed).
     */
    @Nonnull
    public static FXBurst burstStarlight(@Nonnull Vec3 pos) {
        return burst(pos, new Color(180, 200, 255), 0.15f, 12, 0.12f);
    }

    // =========================================================================
    // Light beam
    // =========================================================================

    /**
     * Spawn a light beam between two positions.
     *
     * @param from  start position
     * @param to    end position
     * @param color beam color
     * @param width beam width
     * @param maxAge lifetime in ticks
     */
    @Nonnull
    public static FXLightbeam lightbeam(@Nonnull Vec3 from, @Nonnull Vec3 to,
                                         @Nonnull Color color, float width, int maxAge) {
        FXLightbeam beam = new FXLightbeam(from.x, from.y, from.z, to.x, to.y, to.z);
        beam.setBeamWidth(width);
        EffectProperties props = EffectProperties.create()
                .setColor(color)
                .setMaxAge(maxAge)
                .setAlphaFadeInOut();
        props.applyTo(beam);
        EffectManager.getInstance().spawn(beam);
        return beam;
    }

    /**
     * Spawn a starlight-colored beam (blue-white, standard width).
     */
    @Nonnull
    public static FXLightbeam lightbeamStarlight(@Nonnull Vec3 from, @Nonnull Vec3 to, int maxAge) {
        return lightbeam(from, to, new Color(140, 180, 255), 0.06f, maxAge);
    }

    // =========================================================================
    // Lightning
    // =========================================================================

    /**
     * Spawn a lightning bolt between two positions.
     *
     * @param from         start position
     * @param to           end position
     * @param color        bolt color
     * @param segments     number of bolt segments (more = more jagged)
     * @param displacement max perpendicular offset
     * @param maxAge       lifetime in ticks
     */
    @Nonnull
    public static FXLightning lightning(@Nonnull Vec3 from, @Nonnull Vec3 to,
                                         @Nonnull Color color, int segments,
                                         float displacement, int maxAge) {
        FXLightning bolt = new FXLightning(from.x, from.y, from.z,
                to.x, to.y, to.z, segments, displacement, true);
        EffectProperties props = EffectProperties.create()
                .setColor(color)
                .setMaxAge(maxAge);
        props.applyTo(bolt);
        EffectManager.getInstance().spawn(bolt);
        return bolt;
    }

    /**
     * Spawn a standard starlight lightning bolt (blue-white, 6 segments).
     */
    @Nonnull
    public static FXLightning lightningStarlight(@Nonnull Vec3 from, @Nonnull Vec3 to) {
        return lightning(from, to, new Color(160, 190, 255), 6, 0.12f, 6);
    }

    // =========================================================================
    // Cube
    // =========================================================================

    /**
     * Spawn a rotating cube particle at the given position.
     *
     * @param pos          world position
     * @param color        cube color
     * @param scale        cube size
     * @param rotationDeg  degrees per tick rotation
     * @param maxAge       lifetime in ticks
     */
    @Nonnull
    public static FXCube cube(@Nonnull Vec3 pos, @Nonnull Color color, float scale,
                              float rotationDeg, int maxAge) {
        FXCube cube = new FXCube(pos.x, pos.y, pos.z, rotationDeg);
        EffectProperties props = EffectProperties.create()
                .setColor(color)
                .setScale(scale)
                .setMaxAge(maxAge)
                .setAlphaFadeOut();
        props.applyTo(cube);
        EffectManager.getInstance().spawn(cube);
        return cube;
    }

    // =========================================================================
    // Orbital ring
    // =========================================================================

    /**
     * Spawn a ring of sparkle particles orbiting a center point.
     * Each particle follows a circular path via its motion controller.
     *
     * @param center     orbit center
     * @param color      particle color
     * @param radius     orbit radius in blocks
     * @param count      number of particles in the ring
     * @param scale      particle scale
     * @param maxAge     lifetime in ticks
     * @param orbitSpeed radians per tick
     */
    public static void orbitalRing(@Nonnull Vec3 center, @Nonnull Color color,
                                    float radius, int count, float scale,
                                    int maxAge, float orbitSpeed) {
        for (int i = 0; i < count; i++) {
            float baseAngle = (float) (i * Math.PI * 2.0 / count);
            double startX = center.x + Math.cos(baseAngle) * radius;
            double startZ = center.z + Math.sin(baseAngle) * radius;

            FXParticle particle = new FXParticle(startX, center.y, startZ);
            final float angle = baseAngle;
            EffectProperties props = EffectProperties.create()
                    .setColor(color)
                    .setScale(scale)
                    .setMaxAge(maxAge)
                    .setAlpha(0.7f)
                    .setMotionController(fx -> {
                        float currentAngle = angle + fx.getAge() * orbitSpeed;
                        double targetX = center.x + Math.cos(currentAngle) * radius;
                        double targetZ = center.z + Math.sin(currentAngle) * radius;
                        fx.setPosition(targetX, center.y, targetZ);
                    });
            props.applyTo(particle);
            EffectManager.getInstance().spawn(particle);
        }
    }

    // =========================================================================
    // Orbital
    // =========================================================================

    /**
     * Spawn a ring of orbiting particles around a center point.
     * Uses FXOrbital for smooth circular motion.
     *
     * @param center     orbit center
     * @param color      particle color
     * @param radius     orbit radius in blocks
     * @param count      number of orbitals in the ring
     * @param speed      angular speed (radians/tick)
     * @param maxAge     lifetime in ticks
     */
    public static void orbitalRingFX(@Nonnull Vec3 center, @Nonnull Color color,
                                      float radius, int count, float speed, int maxAge) {
        for (int i = 0; i < count; i++) {
            float startAngle = (float) (i * Math.PI * 2.0 / count);
            float inclination = (RAND.nextFloat() - 0.5f) * 0.3f;

            FXOrbital orbital = new FXOrbital(
                    center.x, center.y, center.z,
                    radius, speed, startAngle, inclination);
            orbital.setVerticalBob(0.02f);
            EffectProperties props = EffectProperties.create()
                    .setColor(color)
                    .setScale(0.8f + RAND.nextFloat() * 0.4f)
                    .setMaxAge(maxAge)
                    .setAlphaFadeInOut();
            props.applyTo(orbital);
            EffectManager.getInstance().spawn(orbital);
        }
    }

    /**
     * Spawn a standard starlight orbital ring (6 particles, blue-white).
     */
    public static void orbitalStarlight(@Nonnull Vec3 center, float radius) {
        orbitalRingFX(center, new Color(150, 190, 255), radius, 6, 0.08f, 60);
    }

    // =========================================================================
    // Vortex
    // =========================================================================

    /**
     * Spawn a vortex of spiraling particles converging on a target.
     *
     * @param target   target center (particles spiral toward this)
     * @param color    particle color
     * @param radius   starting radius (spread around target)
     * @param count    number of vortex particles
     * @param speed    angular speed (radians/tick)
     * @param maxAge   lifetime in ticks
     */
    public static void vortex(@Nonnull Vec3 target, @Nonnull Color color,
                              float radius, int count, float speed, int maxAge) {
        for (int i = 0; i < count; i++) {
            float angle = RAND.nextFloat() * (float) Math.PI * 2.0f;
            float dist = radius * (0.5f + 0.5f * RAND.nextFloat());
            double startX = target.x + Math.cos(angle) * dist;
            double startZ = target.z + Math.sin(angle) * dist;
            double startY = target.y + (RAND.nextFloat() - 0.5f) * 0.5f;

            FXVortex vort = new FXVortex(
                    startX, startY, startZ,
                    target.x, target.y, target.z,
                    speed + RAND.nextFloat() * 0.02f);
            EffectProperties props = EffectProperties.create()
                    .setColor(color)
                    .setScale(0.6f + RAND.nextFloat() * 0.4f)
                    .setMaxAge(maxAge);
            props.applyTo(vort);
            EffectManager.getInstance().spawn(vort);
        }
    }

    /**
     * Spawn a standard starlight suction vortex.
     */
    public static void vortexStarlight(@Nonnull Vec3 target) {
        vortex(target, new Color(130, 170, 240), 1.5f, 8, 0.12f, 30);
    }

    // =========================================================================
    // Utility
    // =========================================================================

    /**
     * Spawn N random sparkle particles in a volume around a position.
     *
     * @param center center of the spawn volume
     * @param spread half-size of the spawn cube
     * @param color  particle color
     * @param count  number of particles
     * @param scale  particle scale
     * @param maxAge lifetime in ticks
     */
    public static void sparkleCloud(@Nonnull Vec3 center, float spread, @Nonnull Color color,
                                     int count, float scale, int maxAge) {
        for (int i = 0; i < count; i++) {
            double x = center.x + (RAND.nextDouble() - 0.5) * 2 * spread;
            double y = center.y + (RAND.nextDouble() - 0.5) * 2 * spread;
            double z = center.z + (RAND.nextDouble() - 0.5) * 2 * spread;
            sparkleFloating(new Vec3(x, y, z), color, scale, maxAge + RAND.nextInt(20));
        }
    }

    /**
     * @return a randomized blue-white starlight color
     */
    @Nonnull
    public static Color randomStarlightColor() {
        int r = 130 + RAND.nextInt(60);
        int g = 160 + RAND.nextInt(60);
        int b = 230 + RAND.nextInt(26);
        return new Color(r, g, b);
    }
}
