/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.test;

import com.google.common.hash.Hashing;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * DataProvider that generates minimal structure NBT files consumed by the
 * Astral Sorcery GameTest suite.
 *
 * <p>Run via {@code gradlew runData} before executing GameTests via
 * {@code gradlew runGameTestServer}.  The generated files live at:
 * {@code data/astralsorcery/structures/gametests/*.nbt}</p>
 *
 * <p>Only one template is needed: a 9×5×9 stone-floor platform.  All
 * {@code @GameTest} methods in {@link AstralGameTests} reference this
 * template via {@code "astralsorcery:gametests/platform"}.</p>
 */
public class GameTestStructureProvider implements DataProvider {

    private static final String MODID = "astralsorcery";
    /** DataVersion NBT field value for Minecraft 1.20.1 structures. */
    private static final int DATA_VERSION = 3465;

    private final PackOutput output;

    public GameTestStructureProvider(@Nonnull PackOutput output) {
        this.output = output;
    }

    @Override
    @Nonnull
    public CompletableFuture<?> run(@Nonnull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        // 9×5×9: stone floor at y=0, four air layers y=1..4 as working space.
        // Center of the floor is (4,0,4); test blocks are placed at y=1.
        futures.add(save(cache, "gametests/platform", buildPlatform(9, 5, 9)));
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
    }

    /**
     * Builds a structure NBT compound consisting of a solid stone floor at Y=0
     * and air for the remaining height.
     */
    @Nonnull
    private CompoundTag buildPlatform(int width, int height, int depth) {
        CompoundTag root = new CompoundTag();
        root.putInt("DataVersion", DATA_VERSION);

        // size: [width, height, depth]
        ListTag size = new ListTag();
        size.add(IntTag.valueOf(width));
        size.add(IntTag.valueOf(height));
        size.add(IntTag.valueOf(depth));
        root.put("size", size);

        // palette: index 0 = minecraft:stone (used for the floor)
        ListTag palette = new ListTag();
        CompoundTag stoneEntry = new CompoundTag();
        stoneEntry.putString("Name", "minecraft:stone");
        palette.add(stoneEntry);
        root.put("palette", palette);

        // blocks: one stone block per (x, z) position at y=0
        ListTag blocks = new ListTag();
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                CompoundTag block = new CompoundTag();
                ListTag pos = new ListTag();
                pos.add(IntTag.valueOf(x));
                pos.add(IntTag.valueOf(0));
                pos.add(IntTag.valueOf(z));
                block.put("pos", pos);
                block.putInt("state", 0); // index into palette → minecraft:stone
                blocks.add(block);
            }
        }
        root.put("blocks", blocks);
        root.put("entities", new ListTag());

        return root;
    }

    /**
     * Writes a structure CompoundTag as a gzip-compressed NBT file through the
     * data generator's CachedOutput so incremental builds skip unchanged files.
     */
    @Nonnull
    private CompletableFuture<?> save(@Nonnull CachedOutput cache,
                                       @Nonnull String path,
                                       @Nonnull CompoundTag tag) {
        Path filePath = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(MODID)
                .resolve("structures")
                .resolve(path + ".nbt");

        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                NbtIo.writeCompressed(tag, baos);
                byte[] bytes = baos.toByteArray();
                cache.writeIfNeeded(filePath, bytes, Hashing.sha256().hashBytes(bytes));
            } catch (IOException e) {
                throw new CompletionException("Failed to save structure: " + path, e);
            }
        }, Util.backgroundExecutor());
    }

    @Override
    @Nonnull
    public String getName() {
        return "Astral Sorcery GameTest Structures";
    }
}
