package hellfirepvp.astralsorcery.common.util;

import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Utilities for recipe lookups (smelting, recipe manager access).
 *
 * <p>1.16 -> 1.20 changes:
 * World -> Level,
 * IRecipeType -> RecipeType,
 * IRecipe -> Recipe,
 * IInventory -> Container,
 * Inventory(ItemStack) -> SimpleContainer(ItemStack),
 * getRecipe -> getRecipeFor,
 * getCraftingResult -> assemble (with RegistryAccess),
 * LogicalSidedProvider.INSTANCE.get(SERVER) -> ServerLifecycleHooks.getCurrentServer(),
 * ClientPlayNetHandler -> ClientPacketListener,
 * Minecraft.getInstance().getConnection() unchanged</p>
 */
public class RecipeHelper {

    @Nullable
    public static SimpleAltarRecipe findAltarRecipeResult(java.util.function.Predicate<ItemStack> match) {
        RecipeManager mgr = getRecipeManager();
        if (mgr == null) return null;
        java.util.List<SimpleAltarRecipe> recipes = mgr.getAllRecipesFor(RecipeTypesAS.ALTAR.get());
        for (SimpleAltarRecipe recipe : recipes) {
            if (match.test(recipe.getOutput())) {
                return recipe;
            }
        }
        return null;
    }

    @Nonnull
    public static Optional<Tuple<ItemStack, Float>> findSmeltingResult(@Nonnull Level level,
                                                                       @Nonnull BlockState input) {
        ItemStack stack = ItemUtils.createBlockStack(input);
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return findSmeltingResult(level, stack);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static Optional<Tuple<ItemStack, Float>> findSmeltingResult(@Nonnull Level level,
                                                                       @Nonnull ItemStack input) {
        RecipeManager mgr = level.getRecipeManager();
        Container inv = new SimpleContainer(input);

        Optional<? extends Recipe<Container>> optRecipe = mgr.getRecipeFor(RecipeType.SMELTING, inv, level);
        if (optRecipe.isEmpty()) {
            optRecipe = mgr.getRecipeFor(RecipeType.CAMPFIRE_COOKING, inv, level);
        }
        if (optRecipe.isEmpty()) {
            optRecipe = mgr.getRecipeFor(RecipeType.SMOKING, inv, level);
        }

        return optRecipe.map(recipe -> {
            ItemStack smeltResult = recipe.assemble(inv, level.registryAccess()).copy();
            float exp = 0;
            if (recipe instanceof AbstractCookingRecipe cookingRecipe) {
                exp = cookingRecipe.getExperience();
            }
            return new Tuple<>(smeltResult, exp);
        });
    }

    @Nullable
    public static RecipeManager getRecipeManager() {
        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            return getClientManager();
        } else {
            MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
            if (srv != null) {
                return srv.getRecipeManager();
            }
        }
        return null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    private static RecipeManager getClientManager() {
        ClientPacketListener conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            return conn.getRecipeManager();
        }
        return null;
    }
}
