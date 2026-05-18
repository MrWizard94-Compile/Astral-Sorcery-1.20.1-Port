/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson;

import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttuneCrystalRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunePlayerRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunementRecipe;

public class AttunementCraftingRegistry extends CustomRecipeRegistry<AttunementRecipe<?>> {

    public static final AttunementCraftingRegistry INSTANCE = new AttunementCraftingRegistry();

    @Override
    public void init() {
        this.register(new AttunePlayerRecipe());
        this.register(new AttuneCrystalRecipe());
    }
}
