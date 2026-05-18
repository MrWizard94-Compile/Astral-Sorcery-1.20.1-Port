/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin;

import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Upgrade recipe that promotes a Discovery altar to an Attunement altar.
 *
 * <p>1.16 → 1.20: TileAltar → BlockEntityAltar, ALTAR_ATTUNEMENT → ATTUNEMENT_ALTAR.get();
 * ResearchManager.informCraftedAltar deferred until ResearchManager is ported.</p>
 */
public class AttunementUpgradeRecipe extends SimpleAltarRecipe {

    public AttunementUpgradeRecipe(@Nonnull ResourceLocation id,
                                   @Nonnull BlockAltar.AltarType altarType,
                                   int craftDuration, double starlightRequired,
                                   @Nonnull ItemStack output,
                                   @Nonnull NonNullList<Ingredient> inputs,
                                   @Nullable ResourceLocation focusConstellation) {
        super(id, altarType, craftDuration, starlightRequired, output, inputs, focusConstellation);
    }

    public static AttunementUpgradeRecipe convertToThis(@Nonnull SimpleAltarRecipe other) {
        return new AttunementUpgradeRecipe(
                other.getId(), other.getAltarType(), other.getCraftDuration(),
                other.getStarlightRequired(), other.getOutput(),
                other.getIngredients(), other.getFocusConstellation());
    }

    // TODO: call ResearchManager.informCraftedAltar once ResearchManager is ported
    public void onRecipeCompletion(@Nonnull BlockEntityAltar altar,
                                   @Nonnull ActiveSimpleAltarRecipe activeRecipe) {
        Level level = altar.getLevel();
        if (level == null) return;
        level.setBlock(altar.getBlockPos(), BlocksAS.ATTUNEMENT_ALTAR.get().defaultBlockState(), Block.UPDATE_ALL);
    }
}
