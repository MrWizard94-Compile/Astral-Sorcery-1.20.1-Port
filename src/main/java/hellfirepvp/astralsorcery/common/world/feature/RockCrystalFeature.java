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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import javax.annotation.Nonnull;

/**
 * Generates rock crystal ore clusters deep underground.
 * Rock crystals are a key progression resource — they spawn rarely
 * in small clusters (1-3 ore blocks) below Y=20 in stone-type blocks.
 *
 * <p>Unlike vanilla ores, rock crystal clusters are deliberately rare
 * and spawn individually rather than in standard ore veins. Each placement
 * attempt has a chance to spawn 1-3 ore blocks in adjacent positions.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * Feature&lt;NoFeatureConfig&gt; -> Feature&lt;NoneFeatureConfiguration&gt;,
 * IWorld -> WorldGenLevel,
 * Random -> RandomSource,
 * place(ISeedReader, ...) -> place(FeaturePlaceContext&lt;C&gt;)</p>
 */
public class RockCrystalFeature extends Feature<NoneFeatureConfiguration> {

    /** Maximum cluster size (number of ore blocks per placement). */
    private static final int MAX_CLUSTER_SIZE = 3;

    public RockCrystalFeature(@Nonnull Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@Nonnull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockState oreState = BlocksAS.ROCK_CRYSTAL_ORE.get().defaultBlockState();

        int placed = 0;
        int clusterSize = 1 + random.nextInt(MAX_CLUSTER_SIZE);

        for (int i = 0; i < clusterSize; i++) {
            BlockPos target;
            if (i == 0) {
                target = origin;
            } else {
                // Random adjacent position (±1 on each axis)
                target = origin.offset(
                        random.nextInt(3) - 1,
                        random.nextInt(3) - 1,
                        random.nextInt(3) - 1
                );
            }

            if (canReplace(level, target)) {
                level.setBlock(target, oreState, 2);
                placed++;
            }
        }

        return placed > 0;
    }

    /**
     * Checks whether the block at the given position can be replaced by ore.
     * Only replaces stone-type blocks (stone, deepslate, etc.).
     */
    private boolean canReplace(@Nonnull WorldGenLevel level, @Nonnull BlockPos pos) {
        BlockState existing = level.getBlockState(pos);
        // Replace natural stone variants
        return existing.is(net.minecraft.tags.BlockTags.STONE_ORE_REPLACEABLES)
                || existing.is(net.minecraft.tags.BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }
}
