package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.event.CooldownSetEvent;
import hellfirepvp.astralsorcery.mixin.accessor.ServerItemCooldownsAccessor;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ServerItemCooldowns;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Fires {@link CooldownSetEvent} when an item cooldown is set on a player,
 * allowing the Horologium mantle perk to reduce it before it is applied.
 *
 * <p>1.16 → 1.20: CooldownTracker.setCooldown → ItemCooldowns.addCooldown.
 * Server-side player reference is obtained via accessor on {@link ServerItemCooldowns}.</p>
 */
@Mixin(ItemCooldowns.class)
public class MixinCooldownTracker {

    @ModifyVariable(method = "addCooldown", at = @At("HEAD"), ordinal = 0, argsOnly = true, remap = false)
    public int fireCooldownEvent(int cooldownTicks) {
        ItemCooldowns tracker = (ItemCooldowns)(Object) this;
        if (tracker instanceof ServerItemCooldowns serverCooldowns) {
            CooldownSetEvent event = new CooldownSetEvent(
                    ((ServerItemCooldownsAccessor) serverCooldowns).astralsorcery$getPlayer(),
                    cooldownTicks);
            MinecraftForge.EVENT_BUS.post(event);
            cooldownTicks = Math.max(event.getResultCooldown(), 1);
        }
        return cooldownTicks;
    }
}
