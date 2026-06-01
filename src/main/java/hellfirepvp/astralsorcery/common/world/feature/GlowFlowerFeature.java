/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import javax.annotation.Nonnull;

/**
 * Generates clusters of glow flowers (torchflower) on the surface in areas
 * with high starlight exposure. These act as visual indicators of areas with
 * strong celestial energy — near marble veins or shrine locations.
 *
 * <p>Places 1-4 flowers in a small cluster within a 3-block radius of the
 * feature origin. Only places on grass blocks with open sky access.</p>
 *
 * <p>1.16 → 1.20: Uses FeaturePlaceContext. Torchflower exists as a vanilla
 * block in 1.20 — we repurpose it as our "glow flower" since it fits
 * thematically and avoids needing a custom block for this decorative feature.</p>
 */
public class GlowFlowerFeature extends Feature<NoneFeatureConfiguration> {

    public GlowFlowerFeature(@Nonnull Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@Nonnull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int placed = 0;
        int count = 1 + random.nextInt(4); // 1-4 flowers per cluster

        for (int attempt = 0; attempt < count * 3; attempt++) {
            if (placed >= count) break;

            int dx = random.nextInt(7) - 3;
            int dz = random.nextInt(7) - 3;
            BlockPos target = origin.offset(dx, 0, dz);

            // Find the surface
            BlockPos surface = findSurface(level, target);
            if (surface == null) continue;

            BlockState below = level.getBlockState(surface.below());
            if (!below.is(Blocks.GRASS_BLOCK) && !below.is(Blocks.PODZOL)) {
                continue;
            }

            if (!level.getBlockState(surface).isAir()) {
                continue;
            }

            if (!level.canSeeSky(surface)) {
                continue;
            }

            level.setBlock(surface, BlocksAS.GLOW_FLOWER.get().defaultBlockState(), 2);
            placed++;
        }

        return placed > 0;
    }

    /**
     * Finds the highest non-air block within a few blocks of the given position.
     */
    @javax.annotation.Nullable
    private BlockPos findSurface(@Nonnull WorldGenLevel level, @Nonnull BlockPos pos) {
        // Search up to 4 blocks above and below for the surface
        for (int dy = 4; dy >= -4; dy--) {
            BlockPos check = pos.offset(0, dy, 0);
            if (!level.getBlockState(check).isAir() && level.getBlockState(check.above()).isAir()) {
                return check.above();
            }
        }
        return null;
    }
}
