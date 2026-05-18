/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.helper;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

/**
 * Extended recipe interface for recipes that match against an {@link IItemHandler}
 * rather than a vanilla {@link Container}.
 *
 * <p>1.16 → 1.20: IInventory → Container, IRecipe → Recipe, World → Level.</p>
 */
public interface IHandlerRecipe<I extends IItemHandler> extends Recipe<Container> {

    boolean matches(I handler, Level level);

    @Override
    default boolean matches(Container inv, Level level) {
        return false;
    }
}
