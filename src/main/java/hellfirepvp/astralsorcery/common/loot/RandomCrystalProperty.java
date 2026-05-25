package hellfirepvp.astralsorcery.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeGenItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalGenerator;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RandomCrystalProperty extends LootItemConditionalFunction {

    private RandomCrystalProperty(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootAS.RANDOM_CRYSTAL_PROPERTIES.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (stack.getItem() instanceof CrystalAttributeGenItem genItem) {
            CrystalAttributes attr = CrystalGenerator.generateNewAttributes(stack);
            genItem.setAttributes(stack, attr);
        }
        return stack;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<RandomCrystalProperty> {
        @Override
        public RandomCrystalProperty deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new RandomCrystalProperty(conditions);
        }
    }
}
