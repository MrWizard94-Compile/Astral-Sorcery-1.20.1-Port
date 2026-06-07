/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe.infusion;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.tile.BlockEntityInfuser;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Tracks an in-progress liquid infusion at a {@link BlockEntityInfuser}.
 *
 * <p>1.16 → 1.20: CompoundNBT → CompoundTag, ListNBT → ListTag,
 * Constants.NBT.TAG_COMPOUND → Tag.TAG_COMPOUND, World → Level,
 * putUniqueId/getUniqueId → putUUID/getUUID,
 * LogicalSidedProvider → ServerLifecycleHooks.getCurrentServer().</p>
 */
public class ActiveLiquidInfusionRecipe {

    private final LiquidInfusion recipeToCraft;
    private final UUID playerCraftingUUID;

    private int ticksCrafting = 0;
    private CompoundTag craftingData = new CompoundTag();

    public ActiveLiquidInfusionRecipe(@Nonnull LiquidInfusion recipeToCraft,
                                      @Nonnull UUID playerCraftingUUID) {
        this.recipeToCraft = recipeToCraft;
        this.playerCraftingUUID = playerCraftingUUID;
    }

    public boolean matches(@Nonnull BlockEntityInfuser infuser) {
        ItemStack input = infuser.getInventory().getStackInSlot(0);
        return this.recipeToCraft.matches(input);
    }

    public void tick() {
        this.ticksCrafting++;
    }

    public int getTotalCraftingTime() {
        return this.recipeToCraft.getCraftDuration();
    }

    public boolean isFinished() {
        return getTicksCrafting() >= getTotalCraftingTime();
    }

    public void createItemOutputs(@Nonnull BlockEntityInfuser infuser,
                                  @Nonnull Consumer<ItemStack> output) {
        ItemStack result = this.recipeToCraft.getOutput().copy();
        if (this.recipeToCraft.doesCopyNBTToOutputs()) {
            ItemStack inputStack = infuser.getInventory().getStackInSlot(0);
            if (!inputStack.isEmpty() && inputStack.hasTag()) {
                net.minecraft.nbt.CompoundTag srcTag = inputStack.getTag();
                result.setTag(srcTag != null ? srcTag.copy() : null);
            }
        }
        ResearchManager.informCraftedInfuser(infuser, this, result);
        output.accept(result);
    }

    public void consumeInputs(@Nonnull BlockEntityInfuser infuser) {
        net.minecraft.world.level.Level level = infuser.getLevel();
        ItemUtils.decrementItem(
                () -> infuser.getInventory().getStackInSlot(0),
                stack -> infuser.getInventory().setStackInSlot(0, stack),
                stack -> {
                    if (level != null) {
                        ItemUtils.dropItemNaturally(level,
                                infuser.getBlockPos().getX() + 0.5,
                                infuser.getBlockPos().getY() + 1.0,
                                infuser.getBlockPos().getZ() + 0.5, stack);
                    }
                });
    }

    @Nullable
    public Player tryGetCraftingPlayerServer() {
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv == null) return null;
        return srv.getPlayerList().getPlayer(this.playerCraftingUUID);
    }

    @Nonnull
    public LiquidInfusion getRecipeToCraft() {
        return recipeToCraft;
    }

    @Nonnull
    public UUID getPlayerCraftingUUID() {
        return playerCraftingUUID;
    }

    public int getTicksCrafting() {
        return ticksCrafting;
    }

    @Nonnull
    public CompoundTag getCraftingData() {
        return craftingData;
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag compound = new CompoundTag();
        compound.putString("recipeToCraft", getRecipeToCraft().getId().toString());
        compound.putUUID("playerCraftingUUID", getPlayerCraftingUUID());
        compound.putInt("ticksCrafting", getTicksCrafting());
        compound.put("craftingData", craftingData);
        return compound;
    }

    @Nullable
    public static ActiveLiquidInfusionRecipe deserialize(@Nonnull CompoundTag compound) {
        RecipeManager mgr = RecipeHelper.getRecipeManager();
        if (mgr == null) {
            return null;
        }

        ResourceLocation recipeKey = new ResourceLocation(compound.getString("recipeToCraft"));
        Optional<?> recipe = mgr.byKey(recipeKey);
        if (recipe.isEmpty() || !(recipe.get() instanceof LiquidInfusion)) {
            AstralSorcery.log.info("Recipe with unknown/invalid name found: " + recipeKey);
            return null;
        }

        LiquidInfusion infusionRecipe = (LiquidInfusion) recipe.get();
        UUID uuidCraft = compound.getUUID("playerCraftingUUID");
        int tick = compound.getInt("ticksCrafting");

        ActiveLiquidInfusionRecipe task = new ActiveLiquidInfusionRecipe(infusionRecipe, uuidCraft);
        task.ticksCrafting = tick;
        task.craftingData = compound.getCompound("craftingData");
        return task;
    }
}
