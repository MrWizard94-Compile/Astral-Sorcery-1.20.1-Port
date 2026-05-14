package hellfirepvp.astralsorcery.common.data.config.base;

import hellfirepvp.astralsorcery.common.util.block.BlockPredicate;
import hellfirepvp.astralsorcery.common.util.block.BlockStateList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * A lazily-resolved BlockStateList backed by a Forge config entry.
 * Re-resolves on next access after a config reload.
 *
 * <p>1.16 → 1.20 changes: World → Level</p>
 */
public class ConfiguredBlockStateList implements BlockPredicate, Predicate<BlockState> {

    private final ForgeConfigSpec.ConfigValue<List<String>> configList;
    @Nullable
    private BlockStateList resolvedConfiguration = null;

    public ConfiguredBlockStateList(@Nonnull ForgeConfigSpec.ConfigValue<List<String>> configList) {
        this.configList = configList;
    }

    @Override
    public boolean test(@Nonnull BlockState state) {
        if (resolvedConfiguration == null) {
            resolvedConfiguration = BlockStateList.fromConfig(configList.get());
        }
        return this.resolvedConfiguration.test(state);
    }

    @Override
    public boolean test(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        if (resolvedConfiguration == null) {
            resolvedConfiguration = BlockStateList.fromConfig(configList.get());
        }
        return this.resolvedConfiguration.test(state);
    }
}
