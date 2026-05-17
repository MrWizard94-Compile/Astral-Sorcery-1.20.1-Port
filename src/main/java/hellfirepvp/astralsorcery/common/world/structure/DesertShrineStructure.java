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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
 * A desert shrine structure that generates in desert biomes.
 * Contains sandstone and marble construction with a starlight
 * collection basin and loot. Tends to spawn partially buried.
 *
 * <p>Uses the same jigsaw-based placement as {@link AncientShrineStructure}
 * but with different template pools, biome restrictions, and height offset
 * (partially submerged in sand).</p>
 *
 * <p>1.16 -> 1.20: Same codec-based structure system.</p>
 */
public class DesertShrineStructure extends Structure {

    public static final Codec<DesertShrineStructure> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool")
                            .forGetter(s -> s.startPool),
                    Codec.intRange(0, 7).optionalFieldOf("max_depth", 1)
                            .forGetter(s -> s.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height")
                            .forGetter(s -> s.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
                            .forGetter(s -> s.projectStartToHeightmap),
                    Codec.intRange(-16, 0).optionalFieldOf("vertical_offset", -3)
                            .forGetter(s -> s.verticalOffset)
            ).apply(instance, DesertShrineStructure::new)
    );

    private final Holder<StructureTemplatePool> startPool;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int verticalOffset;

    public DesertShrineStructure(@Nonnull StructureSettings settings,
                                 @Nonnull Holder<StructureTemplatePool> startPool,
                                 int maxDepth,
                                 @Nonnull HeightProvider startHeight,
                                 @Nonnull Optional<Heightmap.Types> projectStartToHeightmap,
                                 int verticalOffset) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.verticalOffset = verticalOffset;
    }

    @Override
    @Nonnull
    public Optional<GenerationStub> findGenerationPoint(@Nonnull GenerationContext context) {
        BlockPos blockPos = new BlockPos(
                context.chunkPos().getMinBlockX(),
                startHeight.sample(context.random(),
                        new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()))
                        + verticalOffset,
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
        return StructureTypesAS.DESERT_SHRINE.get();
    }
}
