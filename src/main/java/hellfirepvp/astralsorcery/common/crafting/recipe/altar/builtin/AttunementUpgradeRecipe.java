/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin;

import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.AltarUpgradeRecipe;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import net.minecraft.world.item.crafting.RecipeSerializer;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Upgrade recipe that promotes a Discovery altar to an Attunement altar.
 * Changes the altar block state to AltarType.ATTUNEMENT and grants the player
 * the ATTUNEMENT progression tier.
 */
public class AttunementUpgradeRecipe extends SimpleAltarRecipe implements AltarUpgradeRecipe {

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

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializersAS.ALTAR_UPGRADE.get();
    }

    @Override
    public void onRecipeCompletion(@Nonnull BlockEntityAltar altar) {
        Level level = altar.getLevel();
        if (level == null) return;
        BlockPos pos = altar.getBlockPos();
        BlockState upgraded = level.getBlockState(pos)
                .setValue(BlockAltar.ALTAR_TYPE, BlockAltar.AltarType.ATTUNEMENT);
        level.setBlock(pos, upgraded, Block.UPDATE_ALL);

        Player nearest = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16.0, null);
        ResearchManager.informCraftedAltar(nearest instanceof ServerPlayer sp ? sp : null,
                BlockAltar.AltarType.ATTUNEMENT);
    }
}
