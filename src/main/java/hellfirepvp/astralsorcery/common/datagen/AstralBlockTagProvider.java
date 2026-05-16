/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.datagen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Generates block tag JSONs for mining, tool requirements, etc.
 */
public class AstralBlockTagProvider extends BlockTagsProvider {

    public AstralBlockTagProvider(@Nonnull PackOutput output,
                                   @Nonnull CompletableFuture<HolderLookup.Provider> lookupProvider,
                                   @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, AstralSorcery.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {
        // Marble — mineable with pickaxe
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BlocksAS.MARBLE_RAW.get())
                .add(BlocksAS.MARBLE_BRICKS.get())
                .add(BlocksAS.MARBLE_PILLAR.get())
                .add(BlocksAS.MARBLE_CHISELED.get())
                .add(BlocksAS.MARBLE_ARCH.get())
                .add(BlocksAS.MARBLE_ENGRAVED.get())
                .add(BlocksAS.MARBLE_RUNED.get())
                .add(BlocksAS.MARBLE_STAIRS.get())
                .add(BlocksAS.MARBLE_SLAB.get())
                .add(BlocksAS.BLACK_MARBLE_RAW.get());

        // Functional blocks — pickaxe
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BlocksAS.ALTAR.get())
                .add(BlocksAS.ATTUNEMENT_ALTAR.get())
                .add(BlocksAS.WELL.get())
                .add(BlocksAS.LENS.get())
                .add(BlocksAS.PRISM.get())
                .add(BlocksAS.RELAY.get())
                .add(BlocksAS.CHALICE.get())
                .add(BlocksAS.TELESCOPE.get())
                .add(BlocksAS.INFUSER.get())
                .add(BlocksAS.RITUAL_PEDESTAL.get())
                .add(BlocksAS.GATEWAY.get())
                .add(BlocksAS.FOUNTAIN.get())
                .add(BlocksAS.ILLUMINATOR.get())
                .add(BlocksAS.COLLECTOR_CRYSTAL.get())
                .add(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get());

        // Wood blocks — mineable with axe
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(BlocksAS.INFUSED_WOOD.get())
                .add(BlocksAS.INFUSED_WOOD_PLANKS.get())
                .add(BlocksAS.INFUSED_WOOD_COLUMN.get())
                .add(BlocksAS.INFUSED_WOOD_ARCH.get())
                .add(BlocksAS.INFUSED_WOOD_ENGRAVED.get())
                .add(BlocksAS.INFUSED_WOOD_ENRICHED.get());

        // Ore blocks — needs iron tool
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BlocksAS.ROCK_CRYSTAL_ORE.get())
                .add(BlocksAS.AQUAMARINE_ORE.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(BlocksAS.ROCK_CRYSTAL_ORE.get());
    }
}
