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
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Generates block tag JSON files for Astral Sorcery blocks.
 * Covers mining tool requirements, tier requirements, and
 * structural tags (stairs, slabs).
 */
public class AstralBlockTagsProvider extends BlockTagsProvider {

    public AstralBlockTagsProvider(PackOutput output,
                                   CompletableFuture<HolderLookup.Provider> lookupProvider,
                                   @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, AstralSorcery.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {
        // Mineable with pickaxe
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BlocksAS.MARBLE_RAW.get())
                .add(BlocksAS.MARBLE_ARCH.get())
                .add(BlocksAS.MARBLE_BRICKS.get())
                .add(BlocksAS.MARBLE_CHISELED.get())
                .add(BlocksAS.MARBLE_ENGRAVED.get())
                .add(BlocksAS.MARBLE_RUNED.get())
                .add(BlocksAS.MARBLE_PILLAR.get())
                .add(BlocksAS.BLACK_MARBLE_RAW.get())
                .add(BlocksAS.ROCK_CRYSTAL_ORE.get())
                .add(BlocksAS.AQUAMARINE_ORE.get())
                .add(BlocksAS.ALTAR.get())
                .add(BlocksAS.ATTUNEMENT_ALTAR.get())
                .add(BlocksAS.RELAY.get())
                .add(BlocksAS.WELL.get())
                .add(BlocksAS.INFUSER.get());

        // Needs iron tool
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(BlocksAS.ROCK_CRYSTAL_ORE.get());

        // Mineable with axe
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(BlocksAS.INFUSED_WOOD.get())
                .add(BlocksAS.INFUSED_WOOD_ARCH.get())
                .add(BlocksAS.INFUSED_WOOD_COLUMN.get())
                .add(BlocksAS.INFUSED_WOOD_ENGRAVED.get())
                .add(BlocksAS.INFUSED_WOOD_ENRICHED.get())
                .add(BlocksAS.INFUSED_WOOD_PLANKS.get());

        // Stairs and slabs
        tag(BlockTags.STAIRS).add(BlocksAS.MARBLE_STAIRS.get());
        tag(BlockTags.SLABS).add(BlocksAS.MARBLE_SLAB.get());
    }
}
