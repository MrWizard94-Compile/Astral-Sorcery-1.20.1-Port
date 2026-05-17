/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.gen;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

/**
 * Generates item model JSON files for flat/hand-held Astral Sorcery
 * items. Block items whose models derive from the block model are
 * handled by {@link AstralBlockStateProvider} instead.
 */
public class AstralItemModelProvider extends ItemModelProvider {

    public AstralItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, AstralSorcery.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Simple flat items (generated from texture)
        simpleItem("aquamarine");
        simpleItem("stardust");
        simpleItem("constellation_paper");
        simpleItem("illumination_powder");
        simpleItem("rock_crystal");
        simpleItem("celestial_crystal");
        simpleItem("starmetal_ingot");
        simpleItem("starmetal_dust");
        simpleItem("glass_lens");
        simpleItem("perk_seal");
        simpleItem("wand");
        simpleItem("linking_tool");
        simpleItem("infused_glass");
        simpleItem("resonating_gem");
        simpleItem("formation_stone");
        simpleItem("knowledge_fragment");
    }

    private void simpleItem(@Nonnull String name) {
        withExistingParent(name, new ResourceLocation("item/generated"))
                .texture("layer0", new ResourceLocation(AstralSorcery.MODID, "item/" + name));
    }
}
