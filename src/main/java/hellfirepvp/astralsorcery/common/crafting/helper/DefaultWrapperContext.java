/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.helper;

import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Generic context wrapping an {@link IItemHandler} and the world it belongs to.
 *
 * <p>1.16 → 1.20: World → Level; IHandlerRecipe raw type replaced with
 * the wildcard-bounded {@code Recipe<?>} through {@link RecipeCraftingContext}.</p>
 */
public class DefaultWrapperContext extends RecipeCraftingContext<net.minecraft.world.item.crafting.Recipe<?>, IItemHandler> {

    private final IItemHandler handler;
    @Nullable
    private final Level level;

    public DefaultWrapperContext(@Nonnull IItemHandler handler, @Nullable Level level) {
        this.handler = handler;
        this.level = level;
    }

    @Nonnull
    public IItemHandler getHandler() {
        return handler;
    }

    @Nullable
    public Level getLevel() {
        return level;
    }
}
