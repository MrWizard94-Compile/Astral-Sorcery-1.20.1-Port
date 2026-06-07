/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.lib.EnchantmentsAS;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.effect.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
     * Handles block break events for Scorching Heat enchantment, auto-smelt (Fornax perk),
     * and experience bonuses.
     */
    @SubscribeEvent
    public void onBlockBreak(@Nonnull BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        // Scorching Heat enchantment: independent of perk progression
        if (hasScorchingHeat(player.getMainHandItem())) {
            applyScorchingHeatSmelt(event, player, pos, state);
            return; // block break was cancelled and re-handled; skip perk processing
        }

        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        if (progress == null || progress.getAllocatedPerks().isEmpty()) {
            return;
        }

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

    private static final net.minecraft.resources.ResourceLocation KEY_FORNAX =
            hellfirepvp.astralsorcery.AstralSorcery.key("key_fornax");

    private boolean hasFornaxPerk(@Nonnull PlayerProgress progress) {
        return progress.getAllocatedPerks().contains(KEY_FORNAX);
    }

    /**
     * Returns true if the stack has the Scorching Heat enchantment AND no Silk Touch.
     * Silk Touch is normally incompatible at the enchanting table, but guard defensively.
     */

    private boolean hasScorchingHeat(@Nonnull ItemStack tool) {
        if (tool.isEmpty()) return false;
        if (EnchantmentHelper.getEnchantments(tool).getOrDefault(Enchantments.SILK_TOUCH, 0) > 0) return false;
        return EnchantmentHelper.getEnchantments(tool).getOrDefault(EnchantmentsAS.SCORCHING_HEAT.get(), 0) > 0;
    }

    /**
     * Scorching Heat enchantment: smelts block drops in-place, with Fortune scaling.
     * Spawns smelting XP orbs for each converted stack.
     * Cancels the original break event and handles block removal + drops manually.
     */

    private void applyScorchingHeatSmelt(@Nonnull BlockEvent.BreakEvent event,
                                          @Nonnull ServerPlayer player,
                                          @Nonnull BlockPos pos,
                                          @Nonnull BlockState state) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        ItemStack tool = player.getMainHandItem();
        int fortuneLevel = EnchantmentHelper.getEnchantments(tool).getOrDefault(Enchantments.BLOCK_FORTUNE, 0);

        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos,
                serverLevel.getBlockEntity(pos), player, tool);
        if (drops.isEmpty()) return;

        boolean anyConverted = false;
        float totalSmeltXp = 0f;

        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (drop.isEmpty()) continue;

            Optional<Tuple<ItemStack, Float>> result = RecipeHelper.findSmeltingResult(serverLevel, drop);
            if (result.isEmpty()) continue;

            ItemStack smeltedItem = result.get().getA().copy();
            float recipeXp = result.get().getB();

            // Don't re-smelt block drops (e.g. stone → stone via smelting is fine, but
            // avoid converting blocks that smelt to themselves or nonsensical results)
            if (smeltedItem.getItem() instanceof BlockItem) continue;

            // Fortune multiplier: each extra fortune level adds a chance for +1 bonus stack
            int countMult = 1;
            if (fortuneLevel > 0) {
                int bonus = Math.max(0, serverLevel.getRandom().nextInt(fortuneLevel + 2) - 1);
                countMult = 1 + bonus;
            }

            smeltedItem.setCount(smeltedItem.getCount() * drop.getCount() * countMult);
            totalSmeltXp += recipeXp * drop.getCount() * countMult;
            drops.set(i, smeltedItem);
            anyConverted = true;
        }

        if (!anyConverted) return;

        event.setCanceled(true);
        serverLevel.removeBlock(pos, false);
        state.getBlock().popExperience(serverLevel, pos, event.getExpToDrop());

        Vec3 center = Vec3.atCenterOf(pos);
        for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
                Block.popResource(serverLevel, pos, stack);
            }
        }

        // Smelting experience orbs
        if (totalSmeltXp > 0f) {
            int iExp = (int) totalSmeltXp;
            if (totalSmeltXp - iExp > serverLevel.getRandom().nextFloat()) iExp++;
            if (iExp >= 1) {
                ExperienceOrb.award(serverLevel, center, iExp);
            }
        }
    }
}
