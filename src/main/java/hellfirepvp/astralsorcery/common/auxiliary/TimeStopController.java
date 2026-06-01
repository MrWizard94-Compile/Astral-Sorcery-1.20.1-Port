package hellfirepvp.astralsorcery.common.auxiliary;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks entities frozen by the Horologium mantle time-stop effect.
 *
 * <p>On each tick of a frozen entity, velocity is zeroed and AI disabled.
 * The freeze count decrements automatically through the entity's own tick,
 * so no separate server-tick handler is needed.
 * Players are never frozen — only non-player living entities.</p>
 */
public class TimeStopController {

    public static final TimeStopController INSTANCE = new TimeStopController();

    /** entity ID → ticks remaining */
    private final Map<Integer, Integer> frozen = new ConcurrentHashMap<>();

    private TimeStopController() {}

    /**
     * Freeze all given entities for {@code durationTicks} ticks.
     * Each entity has its AI disabled and velocity zeroed immediately.
     */
    public void freezeEntities(Collection<LivingEntity> entities, int durationTicks) {
        for (LivingEntity e : entities) {
            if (e instanceof Player) continue;
            frozen.put(e.getId(), durationTicks);
            if (e instanceof Mob mob) mob.setNoAi(true);
            e.setDeltaMovement(Vec3.ZERO);
        }
    }

    /** Returns true if the given entity is currently frozen. */
    public boolean isFrozen(int entityId) {
        return frozen.containsKey(entityId);
    }

    /** Clear all freeze state (on world unload). */
    public void clear() {
        frozen.clear();
    }

    @SubscribeEvent
    public void onEntityTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (entity instanceof Player) return;

        int id = entity.getId();
        Integer remaining = frozen.get(id);
        if (remaining == null) return;

        // Hold entity in place
        entity.setDeltaMovement(Vec3.ZERO);

        int next = remaining - 1;
        if (next <= 0) {
            frozen.remove(id);
            if (entity instanceof Mob mob) mob.setNoAi(false);
        } else {
            frozen.put(id, next);
        }
    }
}
