/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe;

import hellfirepvp.astralsorcery.common.crafting.helper.RecipeCraftingContext;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

/**
 * Context for matching {@link LiquidInteraction} recipes.
 *
 * <p>1.16 → 1.20: no API changes; FluidStack import path unchanged.</p>
 */
public class LiquidInteractionContext extends RecipeCraftingContext<LiquidInteraction, IItemHandler> {

    private final FluidStack contentTank1;
    private final FluidStack contentTank2;

    public LiquidInteractionContext(FluidStack contentTank1, FluidStack contentTank2) {
        this.contentTank1 = contentTank1;
        this.contentTank2 = contentTank2;
    }

    public FluidStack getContentTank1() {
        return contentTank1;
    }

    public FluidStack getContentTank2() {
        return contentTank2;
    }
}
