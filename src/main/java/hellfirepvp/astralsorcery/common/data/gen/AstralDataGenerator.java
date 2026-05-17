/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.gen;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

/**
 * Main data-generator entry point. Registers all data providers
 * (tags, loot tables, recipes, block states, item models) with
 * the {@link GatherDataEvent} fired during the {@code runData}
 * Gradle task.
 */
@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AstralDataGenerator {

    private AstralDataGenerator() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Server-side data
        if (event.includeServer()) {
            AstralBlockTagsProvider blockTags = new AstralBlockTagsProvider(output, lookupProvider, existingFileHelper);
            generator.addProvider(true, blockTags);
            generator.addProvider(true, new AstralItemTagsProvider(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
            generator.addProvider(true, new AstralLootTableProvider(output));
            generator.addProvider(true, new AstralRecipeProvider(output));
        }

        // Client-side data
        if (event.includeClient()) {
            generator.addProvider(true, new AstralBlockStateProvider(output, existingFileHelper));
            generator.addProvider(true, new AstralItemModelProvider(output, existingFileHelper));
        }
    }
}
