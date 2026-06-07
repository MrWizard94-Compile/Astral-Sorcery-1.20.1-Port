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
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * A small shrine structure — the most common AS structure, generating
 * in most overworld biomes. A simple marble pillar/arch with a floating
 * collector crystal. Serves as the primary early-game discovery point
 * for new players finding Astral Sorcery content.
 *
 * <p>Smaller than ancient/desert shrines, higher spawn frequency.
 * No interior — just a decorative marble formation that drops
 * rock crystals and hinting at starlight mechanics.</p>
 *
 * <p>1.16 -> 1.20: Same codec-based structure system as other shrines.</p>
 */
public class SmallShrineStructure extends Structure {

    public static final Codec<SmallShrineStructure> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool")
                            .forGetter(s -> s.startPool),
                    HeightProvider.CODEC.fieldOf("start_height")
                            .forGetter(s -> s.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
                            .forGetter(s -> s.projectStartToHeightmap)
            ).apply(instance, SmallShrineStructure::new)
    );

    private final Holder<StructureTemplatePool> startPool;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;

    public SmallShrineStructure(@Nonnull StructureSettings settings,
                                @Nonnull Holder<StructureTemplatePool> startPool,
                                @Nonnull HeightProvider startHeight,
                                @Nonnull Optional<Heightmap.Types> projectStartToHeightmap) {
        super(settings);
        this.startPool = startPool;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
    }

    @Override
    @Nonnull
    public Optional<GenerationStub> findGenerationPoint(@Nonnull GenerationContext context) {
        if (!CommonConfig.CONFIG.generateShrines.get()) {
            return Optional.empty();
        }
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
                1, // max depth — small shrine is single piece
                blockPos,
                false,
                projectStartToHeightmap,
                40 // smaller max distance
        );
    }

    @Override
    @Nonnull
    public StructureType<?> type() {
        return StructureTypesAS.SMALL_SHRINE.get();
    }

    @Override
    public void afterPlace(@Nonnull WorldGenLevel world, @Nonnull StructureManager structureManager,
                           @Nonnull ChunkGenerator chunkGenerator, @Nonnull RandomSource random,
                           @Nonnull BoundingBox boundingBox, @Nonnull ChunkPos chunkPos,
                           @Nonnull PiecesContainer pieces) {
        ShrineMarkers.processMarkers(world, boundingBox, random);
    }
}
