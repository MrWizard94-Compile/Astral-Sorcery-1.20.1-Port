/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.effect;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityIlluminator;
import hellfirepvp.astralsorcery.common.util.tile.TileUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Constellation effect for <em>Lucerna</em> (Weak — Light / Illumination).
 *
 * <p>When focused on a Ritual Pedestal this effect:
 * <ul>
 *   <li>Scans for dark areas within the effect range and places Illuminator
 *       blocks (Astral Sorcery light sources) to prevent mob spawning</li>
 *   <li>Grants Night Vision to all players within the area</li>
 * </ul>
 *
 * <p>Potency scales the number of light-placement attempts per tick.</p>
 */
public class CEffectLucerna extends ConstellationEffectProvider {

    private static final double DEFAULT_RANGE = 16.0;

    /** Block-light level threshold below which a light source is placed. */
    private static final int DARK_THRESHOLD = 7;

    /** Base number of placement attempts per tick. */
    private static final int BASE_LIGHT_ATTEMPTS = 2;

    /** Duration of Night Vision in ticks. */
    private static final int NIGHT_VISION_DURATION = 100;

    public CEffectLucerna() {
        super("lucerna");
    }

    @Override
    public void tick(@Nonnull Level level, @Nonnull BlockPos pos,
                     @Nonnull ConstellationEffectProperties properties) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB area = createArea(pos, properties);
        double potency = properties.getPotency();

        placeLightSources(serverLevel, pos, properties, potency);
        grantNightVision(serverLevel, area);
    }

    /**
     * Scan for dark positions within range and place Illuminator blocks.
     */
    private void placeLightSources(@Nonnull ServerLevel level, @Nonnull BlockPos pos,
                                   @Nonnull ConstellationEffectProperties properties,
                                   double potency) {
        int range = (int) properties.getSize();
        int attempts = (int) Math.max(1, BASE_LIGHT_ATTEMPTS * potency);

        for (int i = 0; i < attempts; i++) {
            BlockPos target = pos.offset(
                    level.getRandom().nextIntBetweenInclusive(-range, range),
                    level.getRandom().nextIntBetweenInclusive(-4, 4),
                    level.getRandom().nextIntBetweenInclusive(-range, range));

            if (!level.getBlockState(target).isAir()) {
                continue;
            }

            int blockLight = level.getBrightness(LightLayer.BLOCK, target);
            if (blockLight < DARK_THRESHOLD) {
                level.setBlockAndUpdate(target,
                        BlocksAS.ILLUMINATOR.get().defaultBlockState());
                // Mark as player-placed so the illuminator's tick runs
                BlockEntityIlluminator ill = TileUtils.getTileAt(level, target,
                        BlockEntityIlluminator.class, true);
                if (ill != null) ill.setPlayerPlaced(true);
            }
        }
    }

    /**
     * Grant Night Vision to all players inside the area.
     */
    private void grantNightVision(@Nonnull ServerLevel level, @Nonnull AABB area) {
        List<Player> players = level.getEntitiesOfClass(Player.class, area);
        for (Player player : players) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION, 0, true, false));
        }
    }

    @Nonnull
    @Override
    public ConstellationEffectProperties provideProperties() {
        return new ConstellationEffectProperties(DEFAULT_RANGE);
    }
}
