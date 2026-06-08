package hellfirepvp.astralsorcery.common.constellation.engraving;

import hellfirepvp.astralsorcery.common.perk.DynamicModifierHelper;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Describes the engraving effects attached to one constellation.
 * Registered in {@link EngravingEffectRegistry} keyed by constellation ResourceLocation.
 *
 * <p>1.16 → 1.20 changes:
 * ForgeRegistryEntry dropped — plain class with ResourceLocation key;
 * EnchantmentType → EnchantmentCategory;
 * TypeEnchantableItem removed (no equivalent in 1.20);
 * Enchantment.canApply → canEnchant;
 * Effect → MobEffect, EffectInstance → MobEffectInstance;
 * PotionUtils.getEffectsFromStack → getMobEffects;
 * PotionUtils.appendEffects replaced by NBT write (no 1.20 equivalent).</p>
 */
public class EngravingEffect {

    private final ResourceLocation key;
    private final List<ApplicableEffect> effects = new ArrayList<>();

    public EngravingEffect(@Nonnull ResourceLocation key) {
        this.key = key;
    }

    @Nonnull
    public ResourceLocation getKey() {
        return key;
    }

    @Nonnull
    public EngravingEffect addEffect(@Nonnull ApplicableEffect effect) {
        this.effects.add(effect);
        return this;
    }

    @Nonnull
    public List<ApplicableEffect> getApplicableEffects(@Nonnull ItemStack stack) {
        return this.effects.stream()
                .filter(e -> e.supports(stack))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // ApplicableEffect API
    // =========================================================================

    public interface ApplicableEffect {

        boolean supports(@Nonnull ItemStack stack);

        @Nonnull
        ItemStack apply(@Nonnull ItemStack stack, float percent, @Nonnull Random rand);
    }

    // =========================================================================
    // ModifierEffect — adds a DynamicModifier attribute to enchantable items
    // =========================================================================

    public static class ModifierEffect implements ApplicableEffect {

        private final Supplier<PerkAttributeType> modifier;
        private final ModifierType type;
        private final float min, max;

        private final List<EnchantmentCategory> applicableCategories = new ArrayList<>();
        private boolean formatToInteger = false;

        public ModifierEffect(@Nonnull Supplier<PerkAttributeType> modifier,
                              @Nonnull ModifierType type, float min, float max) {
            this.modifier = modifier;
            this.type = type;
            this.min = min;
            this.max = max;
        }

        @Nonnull
        public ModifierEffect addApplicableCategory(@Nonnull EnchantmentCategory category) {
            this.applicableCategories.add(category);
            return this;
        }

        @Nonnull
        public ModifierEffect formatResultAsInteger() {
            this.formatToInteger = true;
            return this;
        }

        @Override
        public boolean supports(@Nonnull ItemStack stack) {
            if (stack.isEmpty()) return false;
            if (!DynamicModifierHelper.getStaticModifiers(stack).isEmpty()) return false;
            Item item = stack.getItem();
            if (this.applicableCategories.isEmpty()) {
                for (EnchantmentCategory category : EnchantmentCategory.values()) {
                    if (category.canEnchant(item)) return true;
                }
                return false;
            }
            for (EnchantmentCategory category : this.applicableCategories) {
                if (category.canEnchant(item)) return true;
            }
            return false;
        }

        @Override
        @Nonnull
        public ItemStack apply(@Nonnull ItemStack stack, float percent, @Nonnull Random rand) {
            float rValue = percent * Math.max(0, this.max - this.min);
            if (this.formatToInteger) rValue = Math.round(rValue);
            DynamicModifierHelper.addModifier(stack, UUID.randomUUID(),
                    this.modifier.get(), this.type, this.min + rValue);
            return stack;
        }
    }

    // =========================================================================
    // EnchantmentEffect — adds an enchantment to the item
    // =========================================================================

    public static class EnchantmentEffect implements ApplicableEffect {

        private final Supplier<Enchantment> enchantment;
        private final int min, max;
        private boolean ignoreCompat = false;

        public EnchantmentEffect(@Nonnull Supplier<Enchantment> enchantment, int min, int max) {
            this.enchantment = enchantment;
            this.min = min;
            this.max = max;
        }

        @Nonnull
        public EnchantmentEffect setIgnoreCompatibility() {
            this.ignoreCompat = true;
            return this;
        }

        public boolean isIgnoreCompatibility() {
            return this.ignoreCompat;
        }

        @Override
        public boolean supports(@Nonnull ItemStack stack) {
            if (stack.isEmpty()) return false;
            Enchantment toApply = this.enchantment.get();
            if (stack.getItem() instanceof BookItem) {
                return toApply.isAllowedOnBooks();
            }
            if (!toApply.canEnchant(stack)) return false;
            Map<Enchantment, Integer> existing = EnchantmentHelper.getEnchantments(stack);
            for (Enchantment applied : existing.keySet()) {
                if (toApply.equals(applied)) return false;
                if (!this.ignoreCompat && !(stack.getItem() instanceof EnchantedBookItem)
                        && !toApply.isCompatibleWith(applied)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        @Nonnull
        public ItemStack apply(@Nonnull ItemStack stack, float percent, @Nonnull Random rand) {
            int level = this.min + Math.round(percent * Math.max(0, this.max - this.min));
            if (stack.getItem() instanceof BookItem) {
                stack = ItemUtils.changeItem(stack, Items.ENCHANTED_BOOK);
            }
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
            Enchantment newEnch = this.enchantment.get();
            if (!this.ignoreCompat) {
                for (Enchantment e : enchantments.keySet()) {
                    if (e.equals(newEnch) || !e.isCompatibleWith(newEnch)) {
                        return stack;
                    }
                }
            }
            enchantments.put(newEnch, level);
            EnchantmentHelper.setEnchantments(enchantments, stack);
            return stack;
        }
    }

    // =========================================================================
    // PotionEffect — adds a MobEffectInstance to potion items
    // =========================================================================

    public static class PotionEffect implements ApplicableEffect {

        private final Supplier<MobEffect> effect;
        private final int min, max;

        public PotionEffect(@Nonnull Supplier<MobEffect> effect, int min, int max) {
            this.effect = effect;
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean supports(@Nonnull ItemStack stack) {
            if (stack.isEmpty()) return false;
            if (!(stack.getItem() instanceof PotionItem)) return false;
            MobEffect eff = this.effect.get();
            return PotionUtils.getMobEffects(stack).stream()
                    .noneMatch(inst -> inst.getEffect() == eff);
        }

        @Override
        @Nonnull
        public ItemStack apply(@Nonnull ItemStack stack, float percent, @Nonnull Random rand) {
            MobEffect eff = this.effect.get();
            List<MobEffectInstance> existing = new ArrayList<>(PotionUtils.getMobEffects(stack));
            boolean hasEff = existing.stream().anyMatch(inst -> inst.getEffect() == eff);
            if (!hasEff) {
                int amp = this.min + Math.round(percent * Math.max(0, this.max - this.min));
                int dur = 3 * 60 * 20 + Math.round(rand.nextFloat() * 4 * 60 * 20);
                existing.add(new MobEffectInstance(eff, dur, amp, true, false, true));
            }
            // Write effects back via CustomPotionEffects NBT (PotionUtils has no setEffects in 1.20)
            ListTag effectTags = new ListTag();
            for (MobEffectInstance inst : existing) {
                effectTags.add(inst.save(new CompoundTag()));
            }
            stack.getOrCreateTag().put("CustomPotionEffects", effectTags);
            stack.getOrCreateTag().putInt("CustomPotionColor", 0xFF5500);
            stack.setHoverName(Component.translatable("potion.astralsorcery.crafted.name")
                    .withStyle(ChatFormatting.GOLD));
            return stack;
        }
    }
}
