/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import hellfirepvp.astralsorcery.common.entity.EntityShootingStar;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Handles celestial events that occur during the night:
 * <ul>
 *   <li>Shooting star spawns (rare, near players who can see the sky)</li>
 *   <li>Starlight distribution factor updates (daytime tracking)</li>
 *   <li>Constellation visibility rotation (per-night cycle)</li>
 * </ul>
 *
 * <p>Shooting stars spawn with low probability each tick during nighttime
 * for players who are outdoors and can see the sky. They fall from high
 * altitude and drop rare progression materials on impact.</p>
 *
 * <p>1.16 → 1.20: ServerWorld → ServerLevel, Random → RandomSource.</p>
 */
public class EventHandlerCelestial {

    /** Probability per tick per player of a shooting star spawn during clear nights. */
    private static final double SHOOTING_STAR_CHANCE = 0.0003; // ~1 per 55 minutes per player

    /** Minimum altitude above player for star spawn. */
    private static final int SPAWN_HEIGHT_OFFSET = 80;

    /** Horizontal spread range from the player for spawn location. */
    private static final int SPAWN_SPREAD = 64;

    /** Tick interval for celestial checks (not every tick, for performance). */
    private static final int CHECK_INTERVAL = 20; // once per second

    private int tickCounter = 0;

    @SubscribeEvent
    public void onWorldTick(@Nonnull TickEvent.LevelTickEvent event) {
        if (event.side != LogicalSide.SERVER) {
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Only process the overworld
        if (serverLevel.dimension() != Level.OVERWORLD) {
            return;
        }

        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        // Only during night with clear weather
        float distributionFactor = CelestialHandler.getStarlightDistributionFactor(serverLevel);
        if (distributionFactor < 0.5f) {
            return; // Day time or heavy weather — no shooting stars
        }

        // Check each online player for shooting star spawn
        RandomSource random = serverLevel.getRandom();
        for (ServerPlayer player : serverLevel.players()) {
            trySpawnShootingStar(serverLevel, player, random, distributionFactor);
        }
    }

    private void trySpawnShootingStar(@Nonnull ServerLevel level,
                                       @Nonnull ServerPlayer player,
                                       @Nonnull RandomSource random,
                                       float distributionFactor) {
        // Player must be able to see the sky
        BlockPos playerPos = player.blockPosition();
        if (!level.canSeeSky(playerPos.above())) {
            return;
        }

        // Higher starlight distribution = higher chance
        double effectiveChance = SHOOTING_STAR_CHANCE * distributionFactor * CHECK_INTERVAL;
        if (random.nextDouble() >= effectiveChance) {
            return;
        }

        // Spawn location: high above and offset from player
        double offsetX = (random.nextDouble() - 0.5) * 2 * SPAWN_SPREAD;
        double offsetZ = (random.nextDouble() - 0.5) * 2 * SPAWN_SPREAD;
        double spawnX = player.getX() + offsetX;
        double spawnY = Math.min(player.getY() + SPAWN_HEIGHT_OFFSET, level.getMaxBuildHeight() - 10);
        double spawnZ = player.getZ() + offsetZ;

        EntityShootingStar star = new EntityShootingStar(
                EntityTypesAS.SHOOTING_STAR.get(), level);
        star.setPos(spawnX, spawnY, spawnZ);

        // Random scale and color
        star.setStarScale(0.6f + random.nextFloat() * 0.8f);
        star.setStarColor(getRandomStarColor(random));

        // Trajectory: mostly downward with slight horizontal drift
        double velX = (random.nextDouble() - 0.5) * 0.4;
        double velZ = (random.nextDouble() - 0.5) * 0.4;
        star.initTrajectory(velX, velZ);

        level.addFreshEntity(star);
        AstralSorcery.log.debug("Spawned shooting star near player {} at ({}, {}, {})",
                player.getName().getString(), (int) spawnX, (int) spawnY, (int) spawnZ);
    }

    private int getRandomStarColor(@Nonnull RandomSource random) {
        return switch (random.nextInt(5)) {
            case 0 -> 0xCCDDFF; // Blue-white (starlight)
            case 1 -> 0xFFEECC; // Warm white (solar)
            case 2 -> 0xAADDFF; // Light blue (aquamarine)
            case 3 -> 0xFFCCDD; // Pink (celestial)
            default -> 0xFFFFDD; // Pale yellow (standard)
        };
    }
}
