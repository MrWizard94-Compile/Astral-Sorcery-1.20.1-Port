/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes {@link ItemStack#isEnchanted()} return {@code true} whenever the
 * dynamically-enriched enchantment map is non-empty, so items that carry
 * only Astral Sorcery dynamic enchantments (Prism, Amulet, perk bonuses)
 * still show the enchantment glint.
 *
 * <p>Works in conjunction with {@link hellfirepvp.astralsorcery.mixin.MixinEnchantmentHelper},
 * which hooks {@link EnchantmentHelper#getEnchantments} to append dynamic
 * enchantments to the returned map.</p>
 *
 * <p>1.16 → 1.20: no method renames needed; isEnchanted() and
 * EnchantmentHelper.getEnchantments() signatures are unchanged.</p>
 */
@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(method = "isEnchanted", at = @At("HEAD"), cancellable = true, remap = false)
    public void addDynamicEnchantmentGlint(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack)(Object) this;
        if (!EnchantmentHelper.getEnchantments(stack).isEmpty()) {
            cir.setReturnValue(true);
        }
    }
}
