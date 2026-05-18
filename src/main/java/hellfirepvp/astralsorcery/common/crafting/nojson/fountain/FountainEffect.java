/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.fountain;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Base class for fountain effects. The full implementation (liquid dispersion,
 * vortex mechanics, client VFX) is deferred to Phase 12.
 */
public abstract class FountainEffect<C> {

    private final ResourceLocation id;

    protected FountainEffect(@Nonnull ResourceLocation id) {
        this.id = id;
    }

    @Nonnull
    public ResourceLocation getId() {
        return id;
    }
}
