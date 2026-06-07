package hellfirepvp.astralsorcery.common.world.feature;

import com.mojang.serialization.Codec;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import hellfirepvp.astralsorcery.common.world.WorldGenerationAS;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A placement modifier that reads its attempt count from Forge config at generation time,
 * replacing static JSON count/rarity values for rock crystal and aquamarine ore.
 *
 * <p>JSON format: {@code {"type": "astralsorcery:config_count", "target": "rock_crystal"}}
 * or {@code "target": "aquamarine"}</p>
 */
public class AstralConfigCountPlacement extends PlacementModifier {

    public enum Target implements StringRepresentable {
        ROCK_CRYSTAL("rock_crystal"),
        AQUAMARINE("aquamarine");

        private final String serializedName;

        Target(@Nonnull String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        @Nonnull
        public String getSerializedName() {
            return serializedName;
        }
    }

    public static final Codec<AstralConfigCountPlacement> CODEC =
            StringRepresentable.fromEnum(Target::values)
                    .fieldOf("target")
                    .xmap(AstralConfigCountPlacement::new, p -> p.target)
                    .codec();

    private final Target target;

    public AstralConfigCountPlacement(@Nonnull Target target) {
        this.target = target;
    }

    @Override
    @Nonnull
    public Stream<BlockPos> getPositions(@Nonnull PlacementContext context,
                                         @Nonnull RandomSource random,
                                         @Nonnull BlockPos pos) {
        int count = switch (target) {
            case ROCK_CRYSTAL -> CommonConfig.CONFIG.rockCrystalAttemptsPerChunk.get();
            case AQUAMARINE -> CommonConfig.CONFIG.aquamarineFrequency.get();
        };
        if (count <= 0) {
            return Stream.empty();
        }
        return IntStream.range(0, count).mapToObj(i -> pos);
    }

    @Override
    @Nonnull
    public PlacementModifierType<?> type() {
        return WorldGenerationAS.CONFIG_COUNT.get();
    }
}
