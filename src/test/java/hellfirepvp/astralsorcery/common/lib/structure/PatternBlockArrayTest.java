/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.lib.structure;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;

import javax.annotation.Nonnull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PatternBlockArray — the ObserverLib replacement.
 * Tests pattern construction, matching logic, and accessors.
 * Requires Minecraft block registry bootstrap for Blocks.STONE etc.
 */
class PatternBlockArrayTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void testAddBlockAndRetrieve() {
        PatternBlockArray pattern = new PatternBlockArray(
                new net.minecraft.resources.ResourceLocation("test", "simple"));
        BlockState stone = Blocks.STONE.defaultBlockState();
        pattern.addBlock(stone, 0, 0, 0);
        pattern.addBlock(stone, 1, 0, 0);
        pattern.addBlock(stone, 0, 1, 0);

        assertEquals(3, pattern.size());
        assertNotNull(pattern.getMatchableAt(new BlockPos(0, 0, 0)));
        assertNotNull(pattern.getMatchableAt(new BlockPos(1, 0, 0)));
        assertNotNull(pattern.getMatchableAt(new BlockPos(0, 1, 0)));
        assertNull(pattern.getMatchableAt(new BlockPos(2, 0, 0)));
    }

    @Test
    void testMatchableStateBlockMatching() {
        PatternBlockArray pattern = new PatternBlockArray(
                new net.minecraft.resources.ResourceLocation("test", "match"));
        BlockState stone = Blocks.STONE.defaultBlockState();
        pattern.addBlock(stone, 0, 0, 0);

        MatchableState matchable = pattern.getMatchableAt(BlockPos.ZERO);
        assertNotNull(matchable);

        // Same block should match
        assertTrue(matchable.matches(Blocks.STONE.defaultBlockState()));

        // Different block should not match
        assertFalse(matchable.matches(Blocks.DIRT.defaultBlockState()));
    }

    @Test
    void testAddBlockCube() {
        PatternBlockArray pattern = new PatternBlockArray(
                new net.minecraft.resources.ResourceLocation("test", "cube"));
        BlockState stone = Blocks.STONE.defaultBlockState();

        // 3x3x3 cube
        pattern.addBlockCube(stone, -1, -1, -1, 1, 1, 1);

        // Should have 27 blocks (3^3)
        assertEquals(27, pattern.size());

        // All positions in cube should exist
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    assertNotNull(pattern.getMatchableAt(new BlockPos(x, y, z)),
                            "Missing at " + x + "," + y + "," + z);
                }
            }
        }

        // Outside cube should be null
        assertNull(pattern.getMatchableAt(new BlockPos(2, 0, 0)));
    }

    @Test
    void testGetContentsReturnsCorrectStates() {
        PatternBlockArray pattern = new PatternBlockArray(
                new net.minecraft.resources.ResourceLocation("test", "contents"));
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();

        pattern.addBlock(stone, 0, 0, 0);
        pattern.addBlock(dirt, 1, 0, 0);

        Map<BlockPos, BlockState> contents = pattern.getContents();
        assertEquals(2, contents.size());
        assertEquals(Blocks.STONE, contents.get(BlockPos.ZERO).getBlock());
        assertEquals(Blocks.DIRT, contents.get(new BlockPos(1, 0, 0)).getBlock());
    }

    @Test
    void testSimpleMatchableBlockMatching() {
        SimpleMatchableBlock matchable = new SimpleMatchableBlock(Blocks.OAK_LOG) {
            @Override
            @Nonnull
            public BlockState getDescriptiveState(long tick) {
                return Blocks.OAK_LOG.defaultBlockState();
            }
        };

        // Same block matches
        assertTrue(matchable.matches(Blocks.OAK_LOG.defaultBlockState()));

        // Different block doesn't match
        assertFalse(matchable.matches(Blocks.STONE.defaultBlockState()));
    }

    @Test
    void testRegistryNamePreserved() {
        var name = new net.minecraft.resources.ResourceLocation("astralsorcery", "test_pattern");
        PatternBlockArray pattern = new PatternBlockArray(name);
        assertEquals(name, pattern.getRegistryName());
    }

    @Test
    void testEmptyPatternSize() {
        PatternBlockArray pattern = new PatternBlockArray(
                new net.minecraft.resources.ResourceLocation("test", "empty"));
        assertEquals(0, pattern.size());
        assertTrue(pattern.getPattern().isEmpty());
        assertTrue(pattern.getContents().isEmpty());
    }

    @Test
    void testOverlappingPositions() {
        PatternBlockArray pattern = new PatternBlockArray(
                new net.minecraft.resources.ResourceLocation("test", "overlap"));
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();

        // Add stone at 0,0,0
        pattern.addBlock(stone, 0, 0, 0);
        // Overwrite with dirt at same position
        pattern.addBlock(dirt, 0, 0, 0);

        // Should still be 1 block, now dirt
        assertEquals(1, pattern.size());
        MatchableState matchable = pattern.getMatchableAt(BlockPos.ZERO);
        assertNotNull(matchable);
        assertTrue(matchable.matches(Blocks.DIRT.defaultBlockState()));
    }
}
