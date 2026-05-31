package hellfirepvp.astralsorcery.mixin.accessor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ServerItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin to expose the private {@code player} field of
 * {@link ServerItemCooldowns} so {@link hellfirepvp.astralsorcery.mixin.MixinCooldownTracker}
 * can fire {@link hellfirepvp.astralsorcery.common.event.CooldownSetEvent} with
 * the correct player reference.
 */
@Mixin(ServerItemCooldowns.class)
public interface ServerItemCooldownsAccessor {

    @Accessor(value = "player", remap = false)
    ServerPlayer astralsorcery$getPlayer();
}
