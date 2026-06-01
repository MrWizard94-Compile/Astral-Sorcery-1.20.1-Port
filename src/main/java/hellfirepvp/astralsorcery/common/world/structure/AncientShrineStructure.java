/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityCollectorCrystal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An ancient shrine structure that generates on the surface.
 * Contains a small marble structure with a discovery table or
 * starlight collection device inside.
 *
 * <p>Uses jigsaw-based placement in 1.20 (template pool system).
 * The actual structure layout is defined in the template pool JSON.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * Complete rewrite — Structure registration is now codec-based.
 * Template structures use StructureTemplatePool + JigsawPlacement.
 * Structure placement is data-driven via structure_set JSONs.
 * DATA-mode structure blocks in templates are placed as actual structure
 * blocks (no BlockIgnoreStructureProcessor). afterPlace() scans for
 * them and calls the appropriate handler per marker metadata string.</p>
 */
public class AncientShrineStructure extends Structure {

    public static final Codec<AncientShrineStructure> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool")
                            .forGetter(s -> s.startPool),
                    Codec.intRange(0, 7).optionalFieldOf("max_depth", 1)
                            .forGetter(s -> s.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height")
                            .forGetter(s -> s.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
                            .forGetter(s -> s.projectStartToHeightmap)
            ).apply(instance, AncientShrineStructure::new)
    );

    private final Holder<StructureTemplatePool> startPool;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;

    public AncientShrineStructure(@Nonnull StructureSettings settings,
                                  @Nonnull Holder<StructureTemplatePool> startPool,
                                  int maxDepth,
                                  @Nonnull HeightProvider startHeight,
                                  @Nonnull Optional<Heightmap.Types> projectStartToHeightmap) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
    }

    @Override
    @Nonnull
    public Optional<GenerationStub> findGenerationPoint(@Nonnull GenerationContext context) {
        BlockPos blockPos = new BlockPos(
                context.chunkPos().getMinBlockX(),
                startHeight.sample(context.random(),
                        new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor())),
                context.chunkPos().getMinBlockZ()
        );

        return JigsawPlacement.addPieces(
                context,
                startPool,
                Optional.empty(),
                maxDepth,
                blockPos,
                false,
                projectStartToHeightmap,
                80
        );
    }

    @Override
    @Nonnull
    public StructureType<?> type() {
        return StructureTypesAS.ANCIENT_SHRINE.get();
    }

    @Override
    public void afterPlace(@Nonnull WorldGenLevel world, @Nonnull StructureManager structureManager,
                           @Nonnull ChunkGenerator chunkGenerator, @Nonnull RandomSource random,
                           @Nonnull BoundingBox boundingBox, @Nonnull ChunkPos chunkPos,
                           @Nonnull PiecesContainer pieces) {
        ShrineMarkers.processMarkers(world, boundingBox, random);
    }

    static void placeCrystal(@Nonnull WorldGenLevel world, @Nonnull BlockPos pos,
                              @Nonnull RandomSource random) {
        world.setBlock(pos, BlocksAS.COLLECTOR_CRYSTAL.get().defaultBlockState(), 3);
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof BlockEntityCollectorCrystal crystal) {
            List<IMajorConstellation> majors = new ArrayList<>(
                    ConstellationRegistry.getMajorConstellations());
            if (!majors.isEmpty()) {
                crystal.setAttunedConstellation(majors.get(random.nextInt(majors.size())));
            }
        }
    }

    static void placeChest(@Nonnull WorldGenLevel world, @Nonnull BlockPos pos,
                            @Nonnull RandomSource random, boolean brickFallback) {
        if (brickFallback && random.nextBoolean()) {
            world.setBlock(pos, BlocksAS.MARBLE_BRICKS.get().defaultBlockState(), 3);
            return;
        }
        if (!brickFallback && random.nextBoolean()) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        world.setBlock(pos, Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, Direction.NORTH), 3);
        RandomizableContainerBlockEntity.setLootTable(world, random, pos,
                new ResourceLocation(AstralSorcery.MODID, "shrine_chest"));
    }
}
