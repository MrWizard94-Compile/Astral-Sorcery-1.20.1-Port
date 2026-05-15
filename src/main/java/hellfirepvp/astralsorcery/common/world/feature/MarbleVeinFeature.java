/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.world.feature;

import com.mojang.serialization.Codec;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import javax.annotation.Nonnull;

/**
 * Generates large marble veins underground. Marble is a decorative
 * and functional block used in many Astral Sorcery multiblock structures.
 *
 * <p>Marble veins are large (20-40 block radius) ellipsoidal clusters
 * that replace stone deep underground. They are common enough that
 * players should find marble within a few hundred blocks of spawn.</p>
 *
 * <p>1.16 -> 1.20 changes: same as RockCrystalFeature</p>
 */
public class MarbleVeinFeature extends Feature<NoneFeatureConfiguration> {

    /** Minimum radius of the marble ellipsoid. */
    private static final int MIN_RADIUS = 10;
    /** Maximum radius of the marble ellipsoid. */
    private static final int MAX_RADIUS = 22;

    public MarbleVeinFeature(@Nonnull Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@Nonnull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockState marbleState = BlocksAS.MARBLE_RAW.get().defaultBlockState();

        // Random ellipsoid radii
        int radiusX = MIN_RADIUS + random.nextInt(MAX_RADIUS - MIN_RADIUS + 1);
        int radiusY = Mth.clamp(radiusX / 3 + random.nextInt(4), 3, 12);
        int radiusZ = MIN_RADIUS + random.nextInt(MAX_RADIUS - MIN_RADIUS + 1);

        int placed = 0;

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dy = -radiusY; dy <= radiusY; dy++) {
                for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                    // Ellipsoid check: (dx/rx)^2 + (dy/ry)^2 + (dz/rz)^2 <= 1
                    double dist = ((double) dx * dx) / (radiusX * radiusX)
                            + ((double) dy * dy) / (radiusY * radiusY)
                            + ((double) dz * dz) / (radiusZ * radiusZ);

                    if (dist > 1.0) {
                        continue;
                    }

                    // Add some noise to the surface to avoid perfect ellipsoids
                    if (dist > 0.85 && random.nextFloat() > 0.6f) {
                        continue;
                    }

                    BlockPos target = origin.offset(dx, dy, dz);

                    if (canReplace(level, target)) {
                        level.setBlock(target, marbleState, 2);
                        placed++;
                    }
                }
            }
        }

        return placed > 0;
    }

    /**
     * Checks whether the block at the given position can be replaced by marble.
     * Only replaces natural stone variants.
     */
    private boolean canReplace(@Nonnull WorldGenLevel level, @Nonnull BlockPos pos) {
        BlockState existing = level.getBlockState(pos);
        return existing.is(net.minecraft.tags.BlockTags.STONE_ORE_REPLACEABLES)
                || existing.is(net.minecraft.tags.BlockTags.DEEPSLATE_ORE_REPLACEABLES)
                || existing.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD);
    }
}
