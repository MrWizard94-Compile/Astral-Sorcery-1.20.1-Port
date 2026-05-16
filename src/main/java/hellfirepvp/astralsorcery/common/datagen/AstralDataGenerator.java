/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.datagen;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Data generator entry point for Astral Sorcery.
 * Generates recipe JSONs, loot tables, block/item models, and tags.
 *
 * <p>Run via {@code gradlew runData} using the 'data' run configuration.</p>
 *
 * <p>1.16 -> 1.20: DataGenerator now uses {@link PackOutput} instead of
 * direct path. Providers are added via {@code generator.addProvider()}
 * with the run-type boolean gating.</p>
 */
@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AstralDataGenerator {

    private AstralDataGenerator() {}

    @SubscribeEvent
    public static void onGatherData(@javax.annotation.Nonnull GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // Server-side data providers
        if (event.includeServer()) {
            generator.addProvider(true, new AstralRecipeProvider(output));
            generator.addProvider(true, new AstralLootTableProvider(output));

            AstralBlockTagProvider blockTags = new AstralBlockTagProvider(
                    output, event.getLookupProvider(), existingFileHelper);
            generator.addProvider(true, blockTags);
            generator.addProvider(true, new AstralItemTagProvider(
                    output, event.getLookupProvider(), blockTags.contentsGetter(), existingFileHelper));
        }

        // Client-side data providers
        if (event.includeClient()) {
            generator.addProvider(true, new AstralBlockStateProvider(output, existingFileHelper));
            generator.addProvider(true, new AstralItemModelProvider(output, existingFileHelper));
        }
    }
}
