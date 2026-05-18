/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Allows rock crystals and celestial crystals to grow in size when submerged
 * in liquid starlight (no other entities in the block). Crystal growth logic
 * is deferred until CrystalAttributes system is ported.
 */
public class GrowCrystalSizeRecipe extends LiquidStarlightRecipe {

    public GrowCrystalSizeRecipe() {
        super(AstralSorcery.key("crystal_grow"));
    }

    @Override
    @Nonnull
    public List<Ingredient> getInputForRender() {
        return Collections.emptyList();
    }

    @Override
    @Nonnull
    public List<Ingredient> getOutputForRender() {
        return Collections.emptyList();
    }

    @Override
    public boolean doesStartRecipe(@Nonnull ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof ItemCrystalBase;
    }

    @Override
    public boolean matches(@Nonnull ItemEntity trigger, @Nonnull Level level, @Nonnull BlockPos at) {
        List<net.minecraft.world.entity.Entity> others = getEntitiesInBlock(level, at);
        others.remove(trigger);
        return others.isEmpty();
    }

    @Override
    public void doServerCraftTick(@Nonnull ItemEntity trigger, @Nonnull Level level, @Nonnull BlockPos at) {
        // Crystal growth via CrystalAttributes — deferred until crystal system is ported.
    }
}
