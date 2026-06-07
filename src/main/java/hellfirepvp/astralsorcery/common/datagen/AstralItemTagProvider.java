/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.datagen;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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
        // forge:crystals — interop tag for other mods detecting crystal items
        tag(forgeTag("crystals"))
                .add(ItemsAS.ROCK_CRYSTAL.get(),
                     ItemsAS.CELESTIAL_CRYSTAL.get(),
                     ItemsAS.ATTUNED_ROCK_CRYSTAL.get(),
                     ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get());

        // forge:gems/aquamarine — enables gem crafting substitution by other mods
        tag(forgeTag("gems/aquamarine"))
                .add(ItemsAS.AQUAMARINE.get());

        // minecraft:beacon_payment_items — starmetal ingot accepted by beacons
        tag(ItemTags.BEACON_PAYMENT_ITEMS)
                .add(ItemsAS.STARMETAL_INGOT.get());

        // astralsorcery:perk_gems — used by perk recipes and JEI
        tag(ItemTags.create(AstralSorcery.key("perk_gems")))
                .add(ItemsAS.PERK_GEM_DAY.get(),
                     ItemsAS.PERK_GEM_NIGHT.get(),
                     ItemsAS.PERK_GEM_SKY.get());
    }

    private static TagKey<Item> forgeTag(String path) {
        return ItemTags.create(new ResourceLocation("forge", path));
    }
}
