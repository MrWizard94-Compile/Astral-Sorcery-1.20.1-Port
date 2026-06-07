/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.datagen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
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
        // ---- Pickaxe-mineable ----
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                // Marble variants
                .add(BlocksAS.MARBLE_RAW.get())
                .add(BlocksAS.MARBLE_BRICKS.get())
                .add(BlocksAS.MARBLE_PILLAR.get())
                .add(BlocksAS.MARBLE_CHISELED.get())
                .add(BlocksAS.MARBLE_ARCH.get())
                .add(BlocksAS.MARBLE_ENGRAVED.get())
                .add(BlocksAS.MARBLE_RUNED.get())
                .add(BlocksAS.MARBLE_STAIRS.get())
                .add(BlocksAS.MARBLE_SLAB.get())
                // Black marble variants
                .add(BlocksAS.BLACK_MARBLE_RAW.get())
                .add(BlocksAS.BLACK_MARBLE_ARCH.get())
                .add(BlocksAS.BLACK_MARBLE_BRICKS.get())
                .add(BlocksAS.BLACK_MARBLE_CHISELED.get())
                .add(BlocksAS.BLACK_MARBLE_ENGRAVED.get())
                .add(BlocksAS.BLACK_MARBLE_RUNED.get())
                .add(BlocksAS.BLACK_MARBLE_PILLAR.get())
                .add(BlocksAS.BLACK_MARBLE_SLAB.get())
                .add(BlocksAS.BLACK_MARBLE_STAIRS.get())
                // Ore and metal blocks
                .add(BlocksAS.ROCK_CRYSTAL_ORE.get())
                .add(BlocksAS.AQUAMARINE_ORE.get())
                .add(BlocksAS.STARMETAL_ORE.get())
                .add(BlocksAS.STARMETAL.get())
                // Crystal clusters
                .add(BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get())
                .add(BlocksAS.GEM_CRYSTAL_CLUSTER.get())
                // Fountain primes
                .add(BlocksAS.FOUNTAIN_PRIME_LIQUID.get())
                .add(BlocksAS.FOUNTAIN_PRIME_ORE.get())
                .add(BlocksAS.FOUNTAIN_PRIME_VORTEX.get())
                // Functional blocks
                .add(BlocksAS.ALTAR.get())
                .add(BlocksAS.ATTUNEMENT_ALTAR.get())
                .add(BlocksAS.WELL.get())
                .add(BlocksAS.LENS.get())
                .add(BlocksAS.PRISM.get())
                .add(BlocksAS.RELAY.get())
                .add(BlocksAS.SPECTRAL_RELAY.get())
                .add(BlocksAS.CHALICE.get())
                .add(BlocksAS.TELESCOPE.get())
                .add(BlocksAS.OBSERVATORY.get())
                .add(BlocksAS.INFUSER.get())
                .add(BlocksAS.RITUAL_PEDESTAL.get())
                .add(BlocksAS.REFRACTION_TABLE.get())
                .add(BlocksAS.RITUAL_LINK.get())
                .add(BlocksAS.GATEWAY.get())
                .add(BlocksAS.FOUNTAIN.get())
                .add(BlocksAS.ILLUMINATOR.get())
                .add(BlocksAS.TREE_BEACON_COMPONENT.get())
                .add(BlocksAS.COLLECTOR_CRYSTAL.get())
                .add(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get());

        // ---- Axe-mineable (infused wood) ----
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(BlocksAS.INFUSED_WOOD.get())
                .add(BlocksAS.INFUSED_WOOD_PLANKS.get())
                .add(BlocksAS.INFUSED_WOOD_COLUMN.get())
                .add(BlocksAS.INFUSED_WOOD_ARCH.get())
                .add(BlocksAS.INFUSED_WOOD_ENGRAVED.get())
                .add(BlocksAS.INFUSED_WOOD_ENRICHED.get())
                .add(BlocksAS.INFUSED_WOOD_SLAB.get())
                .add(BlocksAS.INFUSED_WOOD_STAIRS.get());

        // ---- Tool tier requirements ----
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(BlocksAS.ROCK_CRYSTAL_ORE.get())
                .add(BlocksAS.STARMETAL_ORE.get())
                .add(BlocksAS.STARMETAL.get());

        // ---- AS marble blocks — used by gateway validation ----
        tag(BlockTags.create(new ResourceLocation(AstralSorcery.MODID, "marble_blocks")))
                .add(BlocksAS.MARBLE_RAW.get(), BlocksAS.MARBLE_BRICKS.get(),
                     BlocksAS.MARBLE_PILLAR.get(), BlocksAS.MARBLE_CHISELED.get(),
                     BlocksAS.MARBLE_ARCH.get(), BlocksAS.MARBLE_ENGRAVED.get(),
                     BlocksAS.MARBLE_RUNED.get(), BlocksAS.MARBLE_SLAB.get(),
                     BlocksAS.MARBLE_STAIRS.get(),
                     BlocksAS.BLACK_MARBLE_RAW.get(), BlocksAS.BLACK_MARBLE_ARCH.get(),
                     BlocksAS.BLACK_MARBLE_BRICKS.get(), BlocksAS.BLACK_MARBLE_CHISELED.get(),
                     BlocksAS.BLACK_MARBLE_ENGRAVED.get(), BlocksAS.BLACK_MARBLE_RUNED.get(),
                     BlocksAS.BLACK_MARBLE_PILLAR.get(), BlocksAS.BLACK_MARBLE_SLAB.get(),
                     BlocksAS.BLACK_MARBLE_STAIRS.get());

        // ---- Slab and stair type tags ----
        tag(BlockTags.SLABS)
                .add(BlocksAS.MARBLE_SLAB.get())
                .add(BlocksAS.BLACK_MARBLE_SLAB.get())
                .add(BlocksAS.INFUSED_WOOD_SLAB.get());

        tag(BlockTags.STAIRS)
                .add(BlocksAS.MARBLE_STAIRS.get())
                .add(BlocksAS.BLACK_MARBLE_STAIRS.get())
                .add(BlocksAS.INFUSED_WOOD_STAIRS.get());
    }
}
