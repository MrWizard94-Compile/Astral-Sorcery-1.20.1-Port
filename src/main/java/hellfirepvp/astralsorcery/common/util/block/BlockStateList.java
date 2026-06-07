package hellfirepvp.astralsorcery.common.util.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.data.config.base.ConfiguredBlockStateList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A list of block state predicates that can be serialized to/from Forge config
 * and Mojang codecs.
 *
 * <p>1.16 → 1.20 changes:
 * World → Level,
 * block.getStateContainer().getValidStates() → block.getStateDefinition().getPossibleStates()</p>
 */
public class BlockStateList implements BlockPredicate, Predicate<BlockState> {

    public static final Codec<BlockStateList> CODEC = RecordCodecBuilder.create(
            codecBuilder -> codecBuilder.group(
                    BlockState.CODEC.listOf()
                            .fieldOf("blockStates")
                            .forGetter(stateList -> {
                                List<BlockState> applicable = new ArrayList<>();
                                stateList.configuredMatches.forEach(predicate ->
                                        predicate.validMatch
                                                .ifLeft(applicable::addAll)
                                                .ifRight(block -> applicable.addAll(
                                                        block.getStateDefinition().getPossibleStates())));
                                return applicable;
                            })
            ).apply(codecBuilder, states -> {
                BlockStateList list = new BlockStateList();
                states.forEach(list::add);
                return list;
            }));

    private final List<SimpleBlockPredicate> configuredMatches = new ArrayList<>();

    @Nonnull
    public BlockStateList add(@Nonnull BlockState... states) {
        this.configuredMatches.add(new SimpleBlockPredicate(states));
        return this;
    }

    @Nonnull
    public BlockStateList add(@Nonnull Block block) {
        this.configuredMatches.add(new SimpleBlockPredicate(block));
        return this;
    }

    @Nonnull
    public ConfiguredBlockStateList getAsConfig(@Nonnull ForgeConfigSpec.Builder cfgBuilder,
                                                @Nonnull String key,
                                                @Nonnull String translationKey,
                                                @Nonnull String comment) {
        List<String> out = new ArrayList<>();
        configuredMatches.stream()
                .map(SimpleBlockPredicate::getAsConfigList)
                .forEach(out::addAll);
        return new ConfiguredBlockStateList(cfgBuilder
                .comment(comment)
                .translation(translationKey)
                .define(key, out));
    }

    @Nonnull
    public static BlockStateList fromConfig(@Nonnull List<String> serializedBlockPredicates) {
        BlockStateList list = new BlockStateList();
        for (String str : serializedBlockPredicates) {
            SimpleBlockPredicate predicate = SimpleBlockPredicate.fromConfig(str);
            if (predicate != null) {
                list.configuredMatches.add(predicate);
            }
        }
        return list;
    }

    @Override
    public boolean test(BlockState state) {
        return configuredMatches.stream().anyMatch(predicate -> predicate.test(state));
    }

    @Override
    public boolean test(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        return configuredMatches.stream().anyMatch(predicate -> predicate.test(level, pos, state));
    }
}
