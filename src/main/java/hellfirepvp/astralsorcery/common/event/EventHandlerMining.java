/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.effect.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * Handles perk effects related to mining and block breaking:
 * mining speed bonuses, auto-smelt (Fornax), experience bonuses (Evorsio).
 *
 * <p>Mining speed is applied via PlayerEvent.BreakSpeed which modifies
 * the effective break speed multiplier. Auto-smelt is applied via
 * BlockEvent.BreakEvent which can modify drops.</p>
 *
 * <p>1.16 → 1.20 changes:
 * BreakEvent and BreakSpeed APIs unchanged.
 * Recipe lookup via level.getRecipeManager().</p>
 */
public class EventHandlerMining {

    /**
     * Applies mining speed bonuses from perks (Evorsio, Mineralis).
     * Modifies the effective break speed of the player.
     */
    @SubscribeEvent
    public void onBreakSpeed(@Nonnull PlayerEvent.BreakSpeed event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null || progress.getAllocatedPerks().isEmpty()) {
            return;
        }

        List<PerkAttributeModifier> mods = PerkAttributeHelper.collectModifiers(
                player, progress.getAllocatedPerks(),
                PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED.getKey());

        if (!mods.isEmpty()) {
            float baseSpeed = event.getOriginalSpeed();
            double modified = PerkAttributeHelper.applyModifiers(baseSpeed, mods);
            event.setNewSpeed((float) modified);
        }
    }

    /**
     * Handles block break events for auto-smelt (Fornax) and experience bonuses.
     * Auto-smelt converts raw ore drops to their smelted equivalents.
     */
    @SuppressWarnings("null")
    @SubscribeEvent
    public void onBlockBreak(@Nonnull BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            return;
        }

        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null || progress.getAllocatedPerks().isEmpty()) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        // Experience bonus: add bonus XP to block break
        applyExperienceBonus(player, progress, event);

        // Auto-smelt (Fornax perk): convert drops to smelted form
        if (hasFornaxPerk(progress)) {
            applyAutoSmelt(event, player, pos, state);
        }
    }

    /**
     * Applies experience bonuses from perks to block breaking.
     */
    private void applyExperienceBonus(@Nonnull ServerPlayer player,
                                       @Nonnull PlayerProgress progress,
                                       @Nonnull BlockEvent.BreakEvent event) {
        List<PerkAttributeModifier> expMods = PerkAttributeHelper.collectModifiers(
                player, progress.getAllocatedPerks(),
                PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE.getKey());

        if (!expMods.isEmpty()) {
            int baseExp = event.getExpToDrop();
            if (baseExp > 0) {
                double modified = PerkAttributeHelper.applyModifiers(baseExp, expMods);
                event.setExpToDrop((int) Math.ceil(modified));
            }
        }
    }

    /**
     * Applies the Fornax auto-smelt effect: cancels the break event, removes the block
     * without vanilla drops, then spawns the smelted equivalents manually.
     */
    @SuppressWarnings("null")
    private void applyAutoSmelt(@Nonnull BlockEvent.BreakEvent event,
                                 @Nonnull ServerPlayer player,
                                 @Nonnull BlockPos pos,
                                 @Nonnull BlockState state) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos,
                serverLevel.getBlockEntity(pos), player, player.getMainHandItem());

        if (drops.isEmpty()) {
            return;
        }

        boolean hasSmeltable = false;
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            Optional<SmeltingRecipe> recipe = findSmeltingRecipe(serverLevel, drop);
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().getResultItem(serverLevel.registryAccess()).copy();
                result.setCount(result.getCount() * drop.getCount());
                drops.set(i, result);
                hasSmeltable = true;
            }
        }

        if (!hasSmeltable) {
            return;
        }

        // Cancel normal break so vanilla doesn't spawn raw drops, then handle manually.
        event.setCanceled(true);
        serverLevel.removeBlock(pos, false);
        state.getBlock().popExperience(serverLevel, pos, event.getExpToDrop());
        for (ItemStack stack : drops) {
            Block.popResource(serverLevel, pos, stack);
        }
    }

    /**
     * Looks up the smelting recipe for the given input item.
     */
    @Nonnull
    private Optional<SmeltingRecipe> findSmeltingRecipe(@Nonnull ServerLevel level,
                                                         @Nonnull ItemStack input) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        net.minecraft.world.SimpleContainer container = new net.minecraft.world.SimpleContainer(input);
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, container, level);
    }

    private boolean hasFornaxPerk(@Nonnull PlayerProgress progress) {
        return progress.getAllocatedPerks().stream()
                .anyMatch(key -> key.getPath().contains("key_fornax"));
    }
}
