package hellfirepvp.astralsorcery.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeTile;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyCrystalProperties extends LootItemConditionalFunction {

    private CopyCrystalProperties(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootAS.Functions.COPY_CRYSTAL_PROPERTIES.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (context.hasParam(LootContextParams.BLOCK_ENTITY)) {
            BlockEntity tile = context.getParam(LootContextParams.BLOCK_ENTITY);
            if (tile instanceof CrystalAttributeTile attrTile
                    && stack.getItem() instanceof CrystalAttributeItem attrItem) {
                CrystalAttributes attr = attrTile.getAttributes();
                if (attr == null) {
                    attr = attrTile.getMissingAttributes();
                }
                attrItem.setAttributes(stack, attr);
            }
        }
        return stack;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyCrystalProperties> {
        @Override
        public CopyCrystalProperties deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new CopyCrystalProperties(conditions);
        }
    }
}
