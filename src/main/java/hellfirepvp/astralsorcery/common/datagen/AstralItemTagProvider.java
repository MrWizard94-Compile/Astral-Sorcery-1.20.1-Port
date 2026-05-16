/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.datagen;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Generates item tag JSONs. Copies block tags where appropriate.
 */
public class AstralItemTagProvider extends ItemTagsProvider {

    public AstralItemTagProvider(@Nonnull PackOutput output,
                                  @Nonnull CompletableFuture<HolderLookup.Provider> lookupProvider,
                                  @Nonnull CompletableFuture<TagLookup<Block>> blockTags,
                                  @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, AstralSorcery.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {
        // Item tags can be added here as needed.
        // Block tags that should also apply to item forms are
        // copied via copy(BlockTags.X, ItemTags.X) calls.
    }
}
