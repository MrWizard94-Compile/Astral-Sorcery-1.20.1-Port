/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson;

import net.minecraft.resources.ResourceLocation;

public abstract class CustomRecipe {

    private final ResourceLocation key;

    protected CustomRecipe(ResourceLocation key) {
        this.key = key;
    }

    public final ResourceLocation getKey() {
        return key;
    }
}
