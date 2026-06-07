package hellfirepvp.astralsorcery.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityGateway;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;

/**
 * Loot function that copies a gateway block entity's dye color to the dropped item stack.
 * Applied via the gateway block's loot table so breaking a dyed gateway preserves its color.
 */
public class CopyGatewayColor extends LootItemConditionalFunction {

    private CopyGatewayColor(LootItemCondition[] conditions) {
        super(conditions);
    }

    public static LootItemConditionalFunction.Builder<?> create() {
        return simpleBuilder(CopyGatewayColor::new);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootAS.COPY_GATEWAY_COLOR.get();
    }

    @Override
    protected ItemStack run(@Nonnull ItemStack stack, @Nonnull LootContext context) {
        if (context.hasParam(LootContextParams.BLOCK_ENTITY)) {
            BlockEntity tile = context.getParam(LootContextParams.BLOCK_ENTITY);
            if (tile instanceof BlockEntityGateway gateway) {
                BlockEntityGateway.setItemColor(stack, gateway.getColor());
            }
        }
        return stack;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyGatewayColor> {
        @Override
        public CopyGatewayColor deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context,
                                             @Nonnull LootItemCondition[] conditions) {
            return new CopyGatewayColor(conditions);
        }
    }
}
