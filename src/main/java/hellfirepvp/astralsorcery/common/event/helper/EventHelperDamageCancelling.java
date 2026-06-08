package hellfirepvp.astralsorcery.common.event.helper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Grants a player one-shot immunity to a specific damage type.
 * Callers (e.g. ItemBlinkWand after teleport) register immunity by damage-type resource location;
 * the listener cancels the next matching hurt event and then removes the entry.
 *
 * <p>Fall-damage immunity entries are also cleared when the player touches the ground,
 * so immunity never lingers past the landing that prompted the grant.</p>
 *
 * <p>1.16 → 1.20: DamageSource is no longer a singleton; comparison is done by
 * typeHolder().location() (ResourceLocation of the DamageType) instead of reference equality.
 * DamageSource.FALL replaced by DamageTypes.FALL.location().</p>
 */
public class EventHelperDamageCancelling {

    private static final Map<UUID, Set<ResourceLocation>> invulnerableTypes = new HashMap<>();

    private static final ResourceLocation FALL_TYPE = DamageTypes.FALL.location();

    private EventHelperDamageCancelling() {}

    public static void clearServer() {
        invulnerableTypes.clear();
    }

    public static void markInvulnerableToNextDamage(Player player, DamageSource source) {
        if (player.level().isClientSide()) return;
        source.typeHolder().unwrapKey().ifPresent(key ->
                invulnerableTypes.computeIfAbsent(player.getUUID(), uuid -> new HashSet<>())
                        .add(key.location()));
    }

    public static void attachListeners(IEventBus bus) {
        bus.addListener(EventHelperDamageCancelling::onLivingDamage);
        bus.addListener(EventHelperDamageCancelling::onPlayerTick);
    }

    private static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.level().isClientSide()) {
            if (player.onGround()) {
                Set<ResourceLocation> sources = invulnerableTypes.getOrDefault(
                        player.getUUID(), Collections.emptySet());
                sources.remove(FALL_TYPE);
                if (sources.isEmpty()) {
                    invulnerableTypes.remove(player.getUUID());
                }
            }
        }
    }

    private static void onLivingDamage(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ResourceLocation typeId = event.getSource().typeHolder().unwrapKey()
                .map(net.minecraft.resources.ResourceKey::location).orElse(null);
        if (typeId == null) return;
        Set<ResourceLocation> sources = invulnerableTypes.getOrDefault(
                player.getUUID(), Collections.emptySet());
        if (sources.remove(typeId)) {
            if (sources.isEmpty()) {
                invulnerableTypes.remove(player.getUUID());
            }
            event.setCanceled(true);
        }
    }
}
