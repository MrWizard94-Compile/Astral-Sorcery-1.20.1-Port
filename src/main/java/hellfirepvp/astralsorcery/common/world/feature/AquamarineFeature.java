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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import javax.annotation.Nonnull;

/**
 * Generates aquamarine ore deposits in sand near water (river beds, lake beds).
 * Aquamarine is an early-game resource for crafting the first altar tier.
 *
 * <p>Placement logic: finds sand blocks near sea level that are adjacent
 * to water, then replaces them with aquamarine ore. Spawns in small
 * clusters of 1-4 blocks.</p>
 *
 * <p>1.16 -> 1.20 changes: same as other features</p>
 */
public class AquamarineFeature extends Feature<NoneFeatureConfiguration> {

    /** Maximum number of aquamarine blocks per cluster. */
    private static final int MAX_CLUSTER_SIZE = 4;

    /** How many positions to scan around the origin looking for valid sand. */
    private static final int SCAN_RADIUS = 4;

    public AquamarineFeature(@Nonnull Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@Nonnull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockState oreState = BlocksAS.AQUAMARINE_ORE.get().defaultBlockState();

        int placed = 0;
        int targetCount = 1 + random.nextInt(MAX_CLUSTER_SIZE);

        for (int attempt = 0; attempt < targetCount * 3 && placed < targetCount; attempt++) {
            int dx = random.nextInt(SCAN_RADIUS * 2 + 1) - SCAN_RADIUS;
            int dz = random.nextInt(SCAN_RADIUS * 2 + 1) - SCAN_RADIUS;

            // Search vertically for the surface sand block
            for (int dy = 3; dy >= -3; dy--) {
                BlockPos target = origin.offset(dx, dy, dz);
                if (isSandNearWater(level, target)) {
                    level.setBlock(target, oreState, 2);
                    placed++;
                    break;
                }
            }
        }

        return placed > 0;
    }

    /**
     * Checks if the target is a sand block that has water adjacent (including above).
     */
    private boolean isSandNearWater(@Nonnull WorldGenLevel level, @Nonnull BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.SAND) && !state.is(Blocks.RED_SAND)) {
            return false;
        }

        // Check if water is adjacent (any face including above)
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            BlockState adjacent = level.getBlockState(pos.relative(dir));
            if (adjacent.is(Blocks.WATER)) {
                return true;
            }
        }
        return false;
    }
}
