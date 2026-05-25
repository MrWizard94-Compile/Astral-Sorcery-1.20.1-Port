package hellfirepvp.astralsorcery.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nullable;

public class LinearLuckBonus extends LootItemConditionalFunction {

    private LinearLuckBonus(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootAS.LINEAR_LUCK_BONUS.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        @Nullable ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool == null || tool.isEmpty()) {
            return stack;
        }

        int luck = 0;
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity instanceof Player player && player.hasEffect(MobEffects.LUCK)) {
            luck += player.getEffect(MobEffects.LUCK).getAmplifier() + 1;
        }
        luck += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
        luck += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, tool);

        int bonus = 0;
        for (int i = 0; i < luck; i++) {
            bonus += context.getRandom().nextInt(3) + 1;
        }
        stack.setCount(stack.getCount() + bonus);
        return stack;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<LinearLuckBonus> {
        @Override
        public LinearLuckBonus deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new LinearLuckBonus(conditions);
        }
    }
}
