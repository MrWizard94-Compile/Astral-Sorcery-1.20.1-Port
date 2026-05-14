package hellfirepvp.astralsorcery.common.lib.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Replacement for ObserverLib's BlockArray.
 * A simple collection of block positions mapped to block states.
 * Used for structure definitions and tree discovery results.
 */
public class BlockArray {

    private final Map<BlockPos, BlockState> contents = new LinkedHashMap<>();

    /**
     * Add a block at the given position.
     *
     * @param pos   the relative position
     * @param state the block state
     */
    public void addBlock(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        this.contents.put(pos.immutable(), state);
    }

    /**
     * Add a block at the given coordinates.
     *
     * @param x     relative X
     * @param y     relative Y
     * @param z     relative Z
     * @param state the block state
     */
    public void addBlock(int x, int y, int z, @Nonnull BlockState state) {
        addBlock(new BlockPos(x, y, z), state);
    }

    /**
     * Returns an unmodifiable view of all block positions and their states.
     */
    @Nonnull
    public Map<BlockPos, BlockState> getContents() {
        return Collections.unmodifiableMap(this.contents);
    }

    /**
     * Get the block state at a position, or null if not defined.
     */
    @Nullable
    public BlockState getBlockState(@Nonnull BlockPos pos) {
        return this.contents.get(pos);
    }

    /**
     * Returns the number of blocks in this array.
     */
    public int size() {
        return this.contents.size();
    }

    /**
     * Whether this array contains any blocks.
     */
    public boolean isEmpty() {
        return this.contents.isEmpty();
    }
}
