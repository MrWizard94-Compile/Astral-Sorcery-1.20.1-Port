/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.datagen;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

/**
 * Generates item model JSONs for AS items that use simple flat textures.
 * Items with complex 3D models (crystal tools, wand) use handwritten models.
 */
public class AstralItemModelProvider extends ItemModelProvider {

    public AstralItemModelProvider(@Nonnull PackOutput output,
                                    @Nonnull ExistingFileHelper existingFileHelper) {
        super(output, AstralSorcery.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Simple flat items
        simpleItem("aquamarine");
        simpleItem("stardust");
        simpleItem("illumination_powder");
        simpleItem("constellation_paper");
        simpleItem("knowledge_fragment");
        simpleItem("infused_glass");
        simpleItem("resonating_gem");
        simpleItem("formation_stone");
        simpleItem("shifting_stone");

        // Perk gems
        simpleItem("perk_gem_day");
        simpleItem("perk_gem_night");
        simpleItem("perk_gem_sky");

        // Crystal items
        simpleItem("rock_crystal");
        simpleItem("celestial_crystal");

        // Colored lenses
        simpleItem("colored_lens_fire");
        simpleItem("colored_lens_break");
        simpleItem("colored_lens_growth");
        simpleItem("colored_lens_damage");
        simpleItem("colored_lens_regeneration");
        simpleItem("colored_lens_push");
        simpleItem("colored_lens_spectral");

        // Tools (handheld parent for 3D orientation)
        handheldItem("linking_tool");
        handheldItem("hand_telescope");
    }

    /**
     * Generates a simple item/generated model with a single texture layer.
     */
    private void simpleItem(@Nonnull String name) {
        withExistingParent(name, new ResourceLocation("item/generated"))
                .texture("layer0", modLoc("item/" + name));
    }

    /**
     * Generates a handheld item model (rotated like tools/weapons).
     */
    private void handheldItem(@Nonnull String name) {
        withExistingParent(name, new ResourceLocation("item/handheld"))
                .texture("layer0", modLoc("item/" + name));
    }
}
