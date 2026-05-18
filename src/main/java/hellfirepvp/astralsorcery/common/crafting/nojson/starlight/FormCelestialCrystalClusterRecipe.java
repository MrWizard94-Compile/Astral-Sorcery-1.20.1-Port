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
 * Transforms a max-size rock crystal into a celestial crystal cluster block
 * when placed in liquid starlight under starlight-exposed sky.
 * Full logic deferred until CrystalAttributes and the cluster block are ported.
 */
public class FormCelestialCrystalClusterRecipe extends LiquidStarlightRecipe {

    public FormCelestialCrystalClusterRecipe() {
        super(AstralSorcery.key("form_celestial_crystal_cluster"));
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
        return false; // deferred until CrystalAttributes ported
    }

    @Override
    public void doServerCraftTick(@Nonnull ItemEntity trigger, @Nonnull Level level, @Nonnull BlockPos at) {
        // Deferred until CrystalAttributes and cluster block system are ported.
    }
}
