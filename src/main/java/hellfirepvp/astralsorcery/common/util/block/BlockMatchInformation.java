package hellfirepvp.astralsorcery.common.util.block;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Stores block state match criteria for pattern matching and transmutation recipes.
 * Can match by block only or by block + specific property values.
 *
 * <p>1.16 -> 1.20 changes:
 * block.getRegistryName() -> ForgeRegistries.BLOCKS.getKey(block),
 * ForgeRegistries.BLOCKS.getValue(rl) unchanged,
 * CompoundNBT -> CompoundTag</p>
 */
public class BlockMatchInformation {

    @Nonnull
    private final Block block;

    @Nullable
    private final BlockState exactState;

    private final boolean matchExact;

    private BlockMatchInformation(@Nonnull Block block, @Nullable BlockState exactState, boolean matchExact) {
        this.block = block;
        this.exactState = exactState;
        this.matchExact = matchExact;
    }

    /**
     * Create a matcher that matches any state of the given block.
     */
    @Nonnull
    public static BlockMatchInformation ofBlock(@Nonnull Block block) {
        return new BlockMatchInformation(block, null, false);
    }

    /**
     * Create a matcher that matches only the exact block state.
     */
    @Nonnull
    public static BlockMatchInformation ofState(@Nonnull BlockState state) {
        return new BlockMatchInformation(state.getBlock(), state, true);
    }

    /**
     * Test if the given state matches this criteria.
     */
    public boolean matches(@Nonnull BlockState state) {
        if (matchExact && exactState != null) {
            return state == exactState;
        }
        return state.getBlock() == block;
    }

    @Nonnull
    public Block getBlock() {
        return block;
    }

    @Nullable
    public BlockState getExactState() {
        return exactState;
    }

    public boolean isExactMatch() {
        return matchExact;
    }

    /**
     * Get the display state for rendering (exact state or default state).
     */
    @Nonnull
    public BlockState getDisplayState() {
        return exactState != null ? exactState : block.defaultBlockState();
    }

    /**
     * Serialize to NBT.
     */
    @Nonnull
    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(block);
        tag.putString("block", blockKey != null ? blockKey.toString() : block.toString());
        tag.putBoolean("matchExact", matchExact);
        if (matchExact && exactState != null) {
            tag.putInt("stateId", Block.getId(exactState));
        }
        return tag;
    }

    /**
     * Deserialize from NBT.
     */
    @Nullable
    public static BlockMatchInformation readFromNBT(@Nonnull CompoundTag tag) {
        String blockKey = tag.getString("block");
        net.minecraft.resources.ResourceLocation rl = new net.minecraft.resources.ResourceLocation(blockKey);
        Block block = ForgeRegistries.BLOCKS.getValue(rl);
        if (block == null) {
            return null;
        }
        boolean matchExact = tag.getBoolean("matchExact");
        if (matchExact && tag.contains("stateId")) {
            BlockState state = Block.stateById(tag.getInt("stateId"));
            if (state.getBlock() == block) {
                return ofState(state);
            }
        }
        return ofBlock(block);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockMatchInformation other)) return false;
        if (matchExact != other.matchExact) return false;
        if (!block.equals(other.block)) return false;
        return Objects.equals(exactState, other.exactState);
    }

    @Override
    public int hashCode() {
        int result = block.hashCode();
        result = 31 * result + (exactState != null ? exactState.hashCode() : 0);
        result = 31 * result + (matchExact ? 1 : 0);
        return result;
    }
}
