package hellfirepvp.astralsorcery.common.util.block;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory methods for common {@link BlockPredicate} instances.
 *
 * <p>1.16 -> 1.20 changes:
 * ITag&lt;Block&gt; -> TagKey&lt;Block&gt;,
 * state.isIn(tag) -> state.is(tag),
 * TileEntity -> BlockEntity, TileEntityType -> BlockEntityType,
 * World -> Level, RegistryKey -> ResourceKey,
 * LogicalSidedProvider -> ServerLifecycleHooks,
 * srv.forgeGetWorldMap() -> srv.forgeGetWorldMap() (still exists in Forge 1.20),
 * getChunkProvider().isChunkLoaded -> getChunkSource().hasChunk</p>
 */
public class BlockPredicates {

    @Nonnull
    public static BlockPredicate isInTag(@Nonnull TagKey<Block> blockTag) {
        return (world, pos, state) -> state.is(blockTag);
    }

    @Nonnull
    public static BlockPredicate isBlock(@Nonnull Block... blocks) {
        Set<Block> applicable = new HashSet<>(Arrays.asList(blocks));
        return (world, pos, state) -> applicable.contains(state.getBlock());
    }

    @Nonnull
    public static BlockPredicate isState(@Nonnull BlockState... states) {
        Set<BlockState> applicable = new HashSet<>(Arrays.asList(states));
        return (world, pos, state) -> applicable.contains(state);
    }

    @Nonnull
    public static <T extends BlockEntity> BlockPredicate doesTileExist(
            @Nonnull T tile, boolean loadTileWorldAndChunk) {
        if (tile.getLevel() == null) {
            return (world, pos, state) -> false;
        }
        ResourceKey<Level> dim = tile.getLevel().dimension();
        BlockEntityType<?> tileType = tile.getType();

        return (world, pos, state) -> {
            MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
            if (srv == null) {
                return true;
            }
            if (loadTileWorldAndChunk || srv.forgeGetWorldMap().containsKey(dim)) {
                Level foundWorld = srv.getLevel(dim);
                if (foundWorld == null) {
                    return !loadTileWorldAndChunk;
                }
                if (!loadTileWorldAndChunk
                        && !foundWorld.getChunkSource().hasChunk(
                                ChunkPos.getX(pos.asLong()), ChunkPos.getZ(pos.asLong()))) {
                    return true;
                }
                BlockEntity be = MiscUtils.getTileAt(foundWorld, pos, BlockEntity.class, true);
                return be != null && be.getType().equals(tileType);
            }
            return true;
        };
    }
}
