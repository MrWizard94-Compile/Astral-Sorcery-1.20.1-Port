package hellfirepvp.astralsorcery.common.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import javax.annotation.Nonnull;

/**
 * Handles DATA-mode structure block markers placed by jigsaw shrine templates.
 *
 * In 1.20, the jigsaw system does not strip DATA-mode structure blocks from
 * templates (unlike the 1.16 BlockIgnoreStructureProcessor approach). They are
 * placed as actual structure blocks in the world. afterPlace() scans the
 * bounding box for these blocks and dispatches on their metadata string.
 */
final class ShrineMarkers {

    private ShrineMarkers() {}

    static void processMarkers(@Nonnull WorldGenLevel world,
                               @Nonnull BoundingBox boundingBox,
                               @Nonnull RandomSource random) {
        for (int x = boundingBox.minX(); x <= boundingBox.maxX(); x++) {
            for (int y = boundingBox.minY(); y <= boundingBox.maxY(); y++) {
                for (int z = boundingBox.minZ(); z <= boundingBox.maxZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!world.getBlockState(pos).is(Blocks.STRUCTURE_BLOCK)) continue;
                    BlockEntity be = world.getBlockEntity(pos);
                    if (!(be instanceof StructureBlockEntity sbe)) continue;

                    switch (sbe.getMetaData()) {
                        case "crystal" ->
                                AncientShrineStructure.placeCrystal(world, pos, random);
                        case "shrine_chest" ->
                                AncientShrineStructure.placeChest(world, pos, random, false);
                        case "brick_shrine_chest" ->
                                AncientShrineStructure.placeChest(world, pos, random, true);
                        case "random_top" ->
                                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
