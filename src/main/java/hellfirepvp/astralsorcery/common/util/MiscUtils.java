package hellfirepvp.astralsorcery.common.util;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * Miscellaneous utilities that do not belong to a specific domain.
 * Most helpers have been extracted into focused utility classes:
 * {@link TileUtils} (tile/entity/block capture),
 * {@link hellfirepvp.astralsorcery.common.util.data.CollectionUtils} (collection transforms),
 * {@link hellfirepvp.astralsorcery.common.util.data.RandomUtils} (random selection),
 * {@link hellfirepvp.astralsorcery.common.util.FunctionUtils} (functional combinators),
 * {@link hellfirepvp.astralsorcery.common.util.world.ChunkUtils} (chunk-safe execution),
 * {@link hellfirepvp.astralsorcery.common.util.PlayerUtils} (player interaction),
 * {@link hellfirepvp.astralsorcery.common.util.RayTraceUtils} (ray tracing),
 * {@link hellfirepvp.astralsorcery.common.util.data.VectorUtils} (Vector3 geometry).
 */
public class MiscUtils {

    @Nullable
    public static ModContainer getCurrentlyActiveMod() {
        return ModLoadingContext.get().getActiveContainer();
    }

    @Nullable
    public static String capitalizeFirst(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toTitleCase(str.charAt(0)) + str.substring(1);
    }

    public static long getRandomWorldSeed(@Nonnull WorldGenLevel world) {
        return new Random(world.getSeed()).nextLong();
    }

    public static boolean isFluidBlock(@Nonnull BlockState state) {
        return state.getBlock() instanceof LiquidBlock;
    }

    @Nullable
    public static Fluid tryGetFluid(@Nonnull BlockState state) {
        if (!isFluidBlock(state)) {
            return null;
        }
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            return fluidState.getType();
        }
        return null;
    }
}
