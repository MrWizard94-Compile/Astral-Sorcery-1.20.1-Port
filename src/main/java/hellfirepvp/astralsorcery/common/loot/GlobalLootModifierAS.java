/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.lib.EnchantmentsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.util.RandomSource;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Global loot modifier that adds Astral Sorcery drops to vanilla
 * loot tables (chests, mobs, etc.). Uses Forge's global loot
 * modifier system rather than modifying loot table JSONs directly.
 *
 * <p>Example uses:
 * <ul>
 *   <li>Stardust drops from endermen</li>
 *   <li>Constellation papers in dungeon chests</li>
 *   <li>Rock crystal fragments from deep mining</li>
 * </ul>
 *
 * <p>1.16 -> 1.20 changes:
 * Global loot modifiers now use Codec-based serialization.
 * DeferredRegister key is {@code ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS}.</p>
 */
public final class GlobalLootModifierAS {

    private GlobalLootModifierAS() {}

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    AstralSorcery.MODID);

    public static final RegistryObject<Codec<AstralLootModifier>> ASTRAL_LOOT =
            LOOT_MODIFIERS.register("astral_loot", () -> AstralLootModifier.CODEC);

    public static final RegistryObject<Codec<ScorchingHeatLootModifier>> SCORCHING_HEAT_LOOT =
            LOOT_MODIFIERS.register("scorching_heat_loot", () -> ScorchingHeatLootModifier.CODEC);

    public static final RegistryObject<Codec<MagnetDropsLootModifier>> MAGNET_DROPS_LOOT =
            LOOT_MODIFIERS.register("magnet_drops_loot", () -> MagnetDropsLootModifier.CODEC);

    /**
     * Global loot modifier that smelts all block drops when the breaking tool
     * has the Scorching Heat enchantment. Also spawns XP orbs at the drop origin
     * for each item that was smelted.
     */
    public static class ScorchingHeatLootModifier extends LootModifier {

        public static final Codec<ScorchingHeatLootModifier> CODEC =
                RecordCodecBuilder.create(instance ->
                        codecStart(instance).apply(instance, ScorchingHeatLootModifier::new));

        protected ScorchingHeatLootModifier(@Nonnull LootItemCondition[] conditions) {
            super(conditions);
        }

        @Override
        @Nonnull
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
                                                     LootContext context) {
            ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
            if (tool == null || tool.isEmpty()) {
                return generatedLoot;
            }
            int enchLevel = EnchantmentHelper.getEnchantments(tool).getOrDefault(EnchantmentsAS.SCORCHING_HEAT.get(), 0);
            if (enchLevel <= 0) {
                return generatedLoot;
            }

            ServerLevel serverLevel = context.getLevel();

            Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);

            ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
            for (ItemStack stack : generatedLoot) {
                Optional<Tuple<ItemStack, Float>> smeltOpt = RecipeHelper.findSmeltingResult(serverLevel, stack);
                if (smeltOpt.isPresent() && !smeltOpt.get().getA().isEmpty()) {
                    Tuple<ItemStack, Float> smelt = smeltOpt.get();
                    ItemStack smelted = smelt.getA().copy();
                    smelted.setCount(stack.getCount());
                    result.add(smelted);

                    if (origin != null) {
                        float xp = smelt.getB() * stack.getCount();
                        int xpInt = (int) xp;
                        if (context.getRandom().nextFloat() < (xp - xpInt)) {
                            xpInt++;
                        }
                        if (xpInt > 0) {
                            serverLevel.addFreshEntity(new ExperienceOrb(serverLevel,
                                    origin.x, origin.y, origin.z, xpInt));
                        }
                    }
                } else {
                    result.add(stack);
                }
            }
            return result;
        }

        @Override
        @Nonnull
        public Codec<ScorchingHeatLootModifier> codec() {
            return CODEC;
        }
    }

    /**
     * Loot modifier that pulls all block drops directly into the inventory of a player
     * who has the {@code key_magnet_drops} perk allocated. Items that don't fit remain
     * as normal world drops.
     *
     * <p>Only applies to block-break loot contexts (identified by the presence of
     * {@link LootContextParams#BLOCK_STATE}). Mob-kill drops are already handled by
     * {@code EventHandlerPerkEffects.onMagnetDrops} via {@code LivingDropsEvent}.</p>
     */
    public static class MagnetDropsLootModifier extends LootModifier {

        public static final Codec<MagnetDropsLootModifier> CODEC =
                RecordCodecBuilder.create(instance ->
                        codecStart(instance).apply(instance, MagnetDropsLootModifier::new));

        protected MagnetDropsLootModifier(@Nonnull LootItemCondition[] conditions) {
            super(conditions);
        }

        @Override
        @Nonnull
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
                                                     LootContext context) {
            // Only apply to block-break loot (not mob drops, chest loot, etc.)
            if (context.getParamOrNull(LootContextParams.BLOCK_STATE) == null) {
                return generatedLoot;
            }
            Entity breaker = context.getParamOrNull(LootContextParams.THIS_ENTITY);
            if (!(breaker instanceof Player player)) {
                return generatedLoot;
            }
            PlayerProgress progress = PlayerProgressHelper.getProgress(player);
            if (progress == null || !progress.hasPerkAllocated(AstralSorcery.key("key_magnet_drops"))) {
                return generatedLoot;
            }
            ObjectArrayList<ItemStack> remaining = new ObjectArrayList<>();
            for (ItemStack stack : generatedLoot) {
                ItemStack leftover = ItemUtils.dropItemToPlayer(player, stack.copy());
                if (!leftover.isEmpty()) {
                    remaining.add(leftover);
                }
            }
            return remaining;
        }

        @Override
        @Nonnull
        public Codec<MagnetDropsLootModifier> codec() {
            return CODEC;
        }
    }

    /**
     * Loot modifier that conditionally adds Astral Sorcery items to loot.
     * The drop chance and item selection are configured via the codec fields.
     */
    public static class AstralLootModifier extends LootModifier {

        /** Probability [0, 1] that an AS item is added to eligible loot. */
        private final float dropChance;

        public static final Codec<AstralLootModifier> CODEC = RecordCodecBuilder.create(instance ->
                codecStart(instance)
                        .and(Codec.floatRange(0f, 1f).fieldOf("drop_chance")
                                .forGetter(m -> m.dropChance))
                        .apply(instance, AstralLootModifier::new)
        );

        protected AstralLootModifier(@Nonnull LootItemCondition[] conditions, float dropChance) {
            super(conditions);
            this.dropChance = dropChance;
        }

        @Override
        @Nonnull
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
                                                     LootContext context) {
            RandomSource random = context.getRandom();
            if (random.nextFloat() >= dropChance) {
                return generatedLoot;
            }

            // Select an appropriate AS item based on loot context.
            // Entity kills get stardust; chest loot gets constellation papers
            // or aquamarine depending on a weighted roll.
            ItemStack bonus;
            if (context.hasParam(LootContextParams.THIS_ENTITY)) {
                // Mob kill — drop stardust
                bonus = new ItemStack(ItemsAS.STARDUST.get(), 1 + random.nextInt(2));
            } else {
                // Chest/block loot — weighted pick
                float roll = random.nextFloat();
                if (roll < 0.3f) {
                    bonus = new ItemStack(ItemsAS.CONSTELLATION_PAPER.get());
                } else if (roll < 0.6f) {
                    bonus = new ItemStack(ItemsAS.AQUAMARINE.get(), 1 + random.nextInt(3));
                } else {
                    bonus = new ItemStack(ItemsAS.STARDUST.get(), 1 + random.nextInt(2));
                }
            }

            generatedLoot.add(bonus);
            return generatedLoot;
        }

        @Override
        @Nonnull
        public Codec<AstralLootModifier> codec() {
            return CODEC;
        }
    }
}
