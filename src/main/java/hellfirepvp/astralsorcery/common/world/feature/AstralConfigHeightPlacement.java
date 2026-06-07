package hellfirepvp.astralsorcery.common.world.feature;

import com.mojang.serialization.Codec;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import hellfirepvp.astralsorcery.common.world.WorldGenerationAS;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * A placement modifier that reads its Y-level range from Forge config at generation time,
 * replacing the static {@code height_range} JSON placement for rock crystal ore.
 *
 * <p>JSON format: {@code {"type": "astralsorcery:rock_crystal_height"}}</p>
 */
public class AstralConfigHeightPlacement extends PlacementModifier {

    private static final AstralConfigHeightPlacement INSTANCE = new AstralConfigHeightPlacement();

    public static final Codec<AstralConfigHeightPlacement> CODEC = Codec.unit(INSTANCE);

    @Override
    @Nonnull
    public Stream<BlockPos> getPositions(@Nonnull PlacementContext context,
                                         @Nonnull RandomSource random,
                                         @Nonnull BlockPos pos) {
        int minY = CommonConfig.CONFIG.rockCrystalMinY.get();
        int maxY = CommonConfig.CONFIG.rockCrystalMaxY.get();
        if (minY >= maxY) {
            return Stream.empty();
        }
        int y = minY + random.nextInt(maxY - minY + 1);
        int worldMin = context.getMinGenY();
        int worldMax = worldMin + context.getGenDepth() - 1;
        y = Mth.clamp(y, worldMin, worldMax);
        return Stream.of(new BlockPos(pos.getX(), y, pos.getZ()));
    }

    @Override
    @Nonnull
    public PlacementModifierType<?> type() {
        return WorldGenerationAS.ROCK_CRYSTAL_HEIGHT.get();
    }
}
