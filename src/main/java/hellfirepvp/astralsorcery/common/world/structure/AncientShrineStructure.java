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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import javax.annotation.Nonnull;
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
 * Structure placement is data-driven via structure_set JSONs.</p>
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
        // Use jigsaw placement to find a valid generation point
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
                80 // max distance from center
        );
    }

    @Override
    @Nonnull
    public StructureType<?> type() {
        return StructureTypesAS.ANCIENT_SHRINE.get();
    }
}
