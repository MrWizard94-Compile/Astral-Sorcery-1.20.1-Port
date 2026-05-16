/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.datagen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

/**
 * Generates blockstate and block model JSONs for simple AS blocks.
 * Complex blocks (altar, collectors, etc.) use handwritten models.
 */
public class AstralBlockStateProvider extends BlockStateProvider {

    public AstralBlockStateProvider(@Nonnull PackOutput output,
                                     @Nonnull ExistingFileHelper existingFileHelper) {
        super(output, AstralSorcery.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Simple cube blocks — auto-generate blockstate + model
        simpleBlockWithItem(BlocksAS.MARBLE_RAW.get(), cubeAll(BlocksAS.MARBLE_RAW.get()));
        simpleBlockWithItem(BlocksAS.MARBLE_BRICKS.get(), cubeAll(BlocksAS.MARBLE_BRICKS.get()));
        simpleBlockWithItem(BlocksAS.MARBLE_PILLAR.get(),
                models().cubeColumn(blockName(BlocksAS.MARBLE_PILLAR.get()),
                        modLoc("block/marble_pillar_side"),
                        modLoc("block/marble_pillar_top")));
        simpleBlockWithItem(BlocksAS.MARBLE_CHISELED.get(), cubeAll(BlocksAS.MARBLE_CHISELED.get()));
        simpleBlockWithItem(BlocksAS.MARBLE_ARCH.get(), cubeAll(BlocksAS.MARBLE_ARCH.get()));
        simpleBlockWithItem(BlocksAS.MARBLE_ENGRAVED.get(), cubeAll(BlocksAS.MARBLE_ENGRAVED.get()));
        simpleBlockWithItem(BlocksAS.MARBLE_RUNED.get(), cubeAll(BlocksAS.MARBLE_RUNED.get()));

        simpleBlockWithItem(BlocksAS.BLACK_MARBLE_RAW.get(), cubeAll(BlocksAS.BLACK_MARBLE_RAW.get()));

        simpleBlockWithItem(BlocksAS.INFUSED_WOOD.get(), cubeAll(BlocksAS.INFUSED_WOOD.get()));
        simpleBlockWithItem(BlocksAS.INFUSED_WOOD_PLANKS.get(), cubeAll(BlocksAS.INFUSED_WOOD_PLANKS.get()));
        simpleBlockWithItem(BlocksAS.INFUSED_WOOD_COLUMN.get(),
                models().cubeColumn(blockName(BlocksAS.INFUSED_WOOD_COLUMN.get()),
                        modLoc("block/infused_wood_column_side"),
                        modLoc("block/infused_wood_column_top")));
        simpleBlockWithItem(BlocksAS.INFUSED_WOOD_ARCH.get(), cubeAll(BlocksAS.INFUSED_WOOD_ARCH.get()));
        simpleBlockWithItem(BlocksAS.INFUSED_WOOD_ENGRAVED.get(), cubeAll(BlocksAS.INFUSED_WOOD_ENGRAVED.get()));
        simpleBlockWithItem(BlocksAS.INFUSED_WOOD_ENRICHED.get(), cubeAll(BlocksAS.INFUSED_WOOD_ENRICHED.get()));

        // Ore blocks
        simpleBlockWithItem(BlocksAS.ROCK_CRYSTAL_ORE.get(), cubeAll(BlocksAS.ROCK_CRYSTAL_ORE.get()));
        simpleBlockWithItem(BlocksAS.AQUAMARINE_ORE.get(), cubeAll(BlocksAS.AQUAMARINE_ORE.get()));
    }

    @Nonnull
    private String blockName(@Nonnull Block block) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        return key != null ? key.getPath() : block.getDescriptionId();
    }
}
