package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {

    // Hooks the single-enchantment level lookup used by most gameplay systems
    @Inject(
        method = "getItemEnchantmentLevel",
        at = @At("RETURN"),
        cancellable = true,
        remap = false
    )
    private static void getEnhancedEnchantmentLevel(Enchantment enchantment, ItemStack stack,
                                                     CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(DynamicEnchantmentHelper.getNewEnchantmentLevel(cir.getReturnValue(), enchantment, stack, null));
    }

    // Hooks the full enchantment map lookup
    @Inject(
        method = "getEnchantments",
        at = @At("RETURN"),
        remap = false
    )
    private static void applyDeserializedEnhancedEnchantments(ItemStack stack,
                                                               CallbackInfoReturnable<Map<Enchantment, Integer>> cir) {
        DynamicEnchantmentHelper.addNewLevels(cir.getReturnValue(), stack);
    }
}