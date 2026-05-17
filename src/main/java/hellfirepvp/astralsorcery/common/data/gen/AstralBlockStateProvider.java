/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.gen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Generates block-state and block-model JSON files for simple
 * Astral Sorcery blocks. Blocks with complex rendering (tile
 * entities, BERs) are excluded and handled manually.
 */
public class AstralBlockStateProvider extends BlockStateProvider {

    public AstralBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, AstralSorcery.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // --- Simple cube blocks (texture name = registry name) ---
        simpleBlockWithItem(BlocksAS.MARBLE_RAW.get(), cubeAll(BlocksAS.MARBLE_RAW.get()));
        simpleBlockWithItem(BlocksAS.MARBLE_ARCH.get(), cubeAll(BlocksAS.MARBLE_ARCH.get()));
        simpleBlockWithItem(BlocksAS.MARBLE_BRICKS.get(), cubeAll(BlocksAS.MARBLE_BRICKS.get()));
        simpleBlockWithItem(BlocksAS.MARBLE_CHISELED.get(), cubeAll(BlocksAS.MARBLE_CHISELED.get()));
        simpleBlockWithItem(BlocksAS.MARBLE_ENGRAVED.get(), cubeAll(BlocksAS.MARBLE_ENGRAVED.get()));
        simpleBlockWithItem(BlocksAS.MARBLE_RUNED.get(), cubeAll(BlocksAS.MARBLE_RUNED.get()));
        simpleBlockWithItem(BlocksAS.BLACK_MARBLE_RAW.get(), cubeAll(BlocksAS.BLACK_MARBLE_RAW.get()));
        simpleBlockWithItem(BlocksAS.INFUSED_WOOD.get(), cubeAll(BlocksAS.INFUSED_WOOD.get()));
        simpleBlockWithItem(BlocksAS.INFUSED_WOOD_ARCH.get(), cubeAll(BlocksAS.INFUSED_WOOD_ARCH.get()));
        simpleBlockWithItem(BlocksAS.INFUSED_WOOD_COLUMN.get(), cubeAll(BlocksAS.INFUSED_WOOD_COLUMN.get()));
        simpleBlockWithItem(BlocksAS.INFUSED_WOOD_ENGRAVED.get(), cubeAll(BlocksAS.INFUSED_WOOD_ENGRAVED.get()));
        simpleBlockWithItem(BlocksAS.INFUSED_WOOD_ENRICHED.get(), cubeAll(BlocksAS.INFUSED_WOOD_ENRICHED.get()));
        simpleBlockWithItem(BlocksAS.INFUSED_WOOD_PLANKS.get(), cubeAll(BlocksAS.INFUSED_WOOD_PLANKS.get()));
        simpleBlockWithItem(BlocksAS.ROCK_CRYSTAL_ORE.get(), cubeAll(BlocksAS.ROCK_CRYSTAL_ORE.get()));
        simpleBlockWithItem(BlocksAS.AQUAMARINE_ORE.get(), cubeAll(BlocksAS.AQUAMARINE_ORE.get()));
        simpleBlockWithItem(BlocksAS.ILLUMINATOR.get(), cubeAll(BlocksAS.ILLUMINATOR.get()));

        // --- Pillar block (top/side textures, rotatable) ---
        axisBlock(BlocksAS.MARBLE_PILLAR.get(),
                new ResourceLocation(AstralSorcery.MODID, "block/marble_pillar"),
                new ResourceLocation(AstralSorcery.MODID, "block/marble_pillar_top"));
        simpleBlockItem(BlocksAS.MARBLE_PILLAR.get(), models().getExistingFile(
                new ResourceLocation(AstralSorcery.MODID, "block/marble_pillar")));
    }
}
