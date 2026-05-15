package hellfirepvp.astralsorcery.common.util.world;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Random;

/**
 * Computes sky noise distribution values used for constellation visibility
 * and starlight collection efficiency at a given world position.
 *
 * <p>1.16 -> 1.20 changes:
 * RegistryKey&lt;World&gt; -> ResourceKey&lt;Level&gt;,
 * MathHelper -> Mth,
 * ISeedReader -> WorldGenLevel</p>
 */
public class SkyCollectionHelper {

    private static final int ACCURACY = 32;
    private static final Random sharedRand = new Random();

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public static Optional<Float> getSkyNoiseDistributionClient(@Nonnull ResourceKey<Level> dim,
                                                                 @Nonnull BlockPos pos) {
        return WorldSeedCache.getSeedIfPresent(dim)
                .map(seed -> getDistributionInternal(seed, pos));
    }

    public static float getSkyNoiseDistribution(@Nonnull WorldGenLevel world,
                                                 @Nonnull BlockPos pos) {
        return getDistributionInternal(MiscUtils.getRandomWorldSeed(world), pos);
    }

    private static float getDistributionInternal(long seed, @Nonnull BlockPos pos) {
        BlockPos lowerAnchorPoint = new BlockPos(
                (int) Math.floor((float) pos.getX() / ACCURACY) * ACCURACY,
                0,
                (int) Math.floor((float) pos.getZ() / ACCURACY) * ACCURACY);
        float layer0 = getNoiseDistribution(seed,
                lowerAnchorPoint,
                lowerAnchorPoint.offset(ACCURACY, 0, 0),
                lowerAnchorPoint.offset(0, 0, ACCURACY),
                lowerAnchorPoint.offset(ACCURACY, 0, ACCURACY),
                pos);
        return layer0 * layer0;
    }

    private static float getNoiseDistribution(long seed,
                                               @Nonnull BlockPos lXlZ,
                                               @Nonnull BlockPos hXlZ,
                                               @Nonnull BlockPos lXhZ,
                                               @Nonnull BlockPos hXhZ,
                                               @Nonnull BlockPos exact) {
        float nll = getNoise(seed, lXlZ.getX(), lXlZ.getZ());
        float nhl = getNoise(seed, hXlZ.getX(), hXlZ.getZ());
        float nlh = getNoise(seed, lXhZ.getX(), lXhZ.getZ());
        float nhh = getNoise(seed, hXhZ.getX(), hXhZ.getZ());

        float xPart = Math.abs(((float) (exact.getX() - lXlZ.getX())) / ACCURACY);
        float zPart = Math.abs(((float) (exact.getZ() - lXlZ.getZ())) / ACCURACY);

        return cosInterpolate(
                cosInterpolate(nll, nhl, xPart),
                cosInterpolate(nlh, nhh, xPart),
                zPart);
    }

    private static float cosInterpolate(float l, float h, float partial) {
        float t2 = (1F - Mth.cos((float) (partial * Math.PI))) / 2F;
        return (l * (1F - t2) + h * t2);
    }

    private static float getNoise(long seed, int posX, int posZ) {
        sharedRand.setSeed(
                simpleHash(new int[]{
                        (int) (seed),
                        (int) (seed >> 32),
                        posX,
                        posZ
                }, 4)
        );
        sharedRand.nextLong();
        return sharedRand.nextFloat();
    }

    private static int simpleHash(int[] is, int count) {
        int hash = 80238287;
        for (int i = 0; i < count; i++) {
            hash = (hash << 4) ^ (hash >> 28) ^ (is[i] * 5449 % 130651);
        }
        return hash % 75327403;
    }
}
