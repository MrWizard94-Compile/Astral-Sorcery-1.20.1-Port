package hellfirepvp.astralsorcery.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationTile;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;

public class CopyConstellation extends LootItemConditionalFunction {

    private CopyConstellation(LootItemCondition[] conditions) {
        super(conditions);
    }

    public static LootItemConditionalFunction.Builder<?> create() {
        return simpleBuilder(CopyConstellation::new);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootAS.COPY_CONSTELLATION.get();
    }

    @Override
    protected ItemStack run(@Nonnull ItemStack stack, @Nonnull LootContext context) {
        if (context.hasParam(LootContextParams.BLOCK_ENTITY)) {
            BlockEntity tile = context.getParam(LootContextParams.BLOCK_ENTITY);
            if (tile instanceof ConstellationTile cTile
                    && stack.getItem() instanceof ConstellationItem cItem) {
                IWeakConstellation main = cTile.getAttunedConstellation();
                IMinorConstellation trait = cTile.getTraitConstellation();
                cItem.setAttunedConstellation(stack, main);
                cItem.setTraitConstellation(stack, trait);
            }
        }
        return stack;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyConstellation> {
        @Override
        public CopyConstellation deserialize(@Nonnull JsonObject json, @Nonnull JsonDeserializationContext context, @Nonnull LootItemCondition[] conditions) {
            return new CopyConstellation(conditions);
        }
    }
}
