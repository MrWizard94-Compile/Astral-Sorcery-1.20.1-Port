package hellfirepvp.astralsorcery.common.lib.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Replacement for ObserverLib's PatternBlockArray.
 * Defines a multiblock structure pattern with relative block positions
 * and their expected states. Supports both exact BlockState matching
 * and polymorphic MatchableState matching.
 *
 * Used by StructuresAS to define altar, pedestal, infuser, etc. patterns.
 */
public class PatternBlockArray {

    private final ResourceLocation registryName;
    private final Map<BlockPos, MatchableState> pattern = new LinkedHashMap<>();

    public PatternBlockArray(@Nonnull ResourceLocation registryName) {
        this.registryName = registryName;
    }

    @Nonnull
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }

    /**
     * Add a block to the pattern using an exact BlockState.
     */
    public void addBlock(@Nonnull BlockState state, int x, int y, int z) {
        addBlock(new SimpleMatchableBlock(state.getBlock()) {
            @Override
            @Nonnull
            public BlockState getDescriptiveState(long tick) {
                return state;
            }

            @Override
            public boolean matches(@Nonnull BlockState worldState) {
                return worldState == state || worldState.getBlock() == state.getBlock();
            }
        }, x, y, z);
    }

    /**
     * Add a block to the pattern using a MatchableState for polymorphic matching.
     */
    public void addBlock(@Nonnull MatchableState matchable, int x, int y, int z) {
        this.pattern.put(new BlockPos(x, y, z), matchable);
    }

    /**
     * Add a cube of the same block state to the pattern.
     * Coordinates are inclusive on both ends.
     */
    public void addBlockCube(@Nonnull BlockState state, int x1, int y1, int z1, int x2, int y2, int z2) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    addBlock(state, x, y, z);
                }
            }
        }
    }

    /**
     * Returns an unmodifiable view of the pattern entries.
     * Keys are relative positions; values are matchable states.
     */
    @Nonnull
    public Map<BlockPos, MatchableState> getPattern() {
        return Collections.unmodifiableMap(this.pattern);
    }

    /**
     * Returns a snapshot of the pattern as BlockPos → BlockState (at tick 0).
     * Useful for iteration when you need concrete states.
     */
    @Nonnull
    public Map<BlockPos, BlockState> getContents() {
        Map<BlockPos, BlockState> result = new LinkedHashMap<>();
        for (Map.Entry<BlockPos, MatchableState> entry : this.pattern.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getDescriptiveState(0));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Check if this pattern matches at the given origin in the world.
     *
     * @param level  the world
     * @param origin the position to check the pattern against (center block)
     * @return true if all pattern blocks match at the given origin
     */
    public boolean matches(@Nonnull Level level, @Nonnull BlockPos origin) {
        for (Map.Entry<BlockPos, MatchableState> entry : this.pattern.entrySet()) {
            BlockPos worldPos = origin.offset(entry.getKey());
            BlockState worldState = level.getBlockState(worldPos);
            if (!entry.getValue().matches(worldState)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the matchable state at a relative position, or null if not defined.
     */
    @Nullable
    public MatchableState getMatchableAt(@Nonnull BlockPos relativePos) {
        return this.pattern.get(relativePos);
    }

    /**
     * Returns the number of blocks in this pattern.
     */
    public int size() {
        return this.pattern.size();
    }
}
