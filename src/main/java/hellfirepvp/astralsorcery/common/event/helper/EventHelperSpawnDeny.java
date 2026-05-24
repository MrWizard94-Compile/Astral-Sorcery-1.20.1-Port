package hellfirepvp.astralsorcery.common.event.helper;

import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;
import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import hellfirepvp.astralsorcery.common.util.block.WorldBlockPos;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.tick.ITickHandler;
import hellfirepvp.astralsorcery.common.util.tick.TickTokenMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Maintains a map of spawn-deny regions placed by the Lucerna constellation effect.
 * Each region is keyed by its source block position and carries a radius (Double).
 *
 * <p>1.16 → 1.20: EntityClassification.MONSTER → MobCategory.MONSTER,
 * entity.getClassification(false) → entity.getType().getCategory(),
 * event.getWorld() → event.getLevel(),
 * entity.getEntityWorld().getDimensionKey() → entity.level().dimension().</p>
 */
public class EventHelperSpawnDeny {

    public static final TickTokenMap<WorldBlockPos, TickTokenMap.SimpleTickToken<Double>> spawnDenyRegions =
            new TickTokenMap<>(TickEvent.Type.SERVER);

    public static void clearServer() {
        spawnDenyRegions.clear();
    }

    public static void attachTickListener(Consumer<ITickHandler> registrar) {
        registrar.accept(spawnDenyRegions);
    }

    public static void attachListeners(IEventBus eventBus) {
        eventBus.addListener(EventHelperSpawnDeny::onSpawn);
    }

    private static void onSpawn(MobSpawnEvent.PositionCheck event) {
        if (event.getResult() == Event.Result.DENY
                || event.getSpawner() != null) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (entity.getTags().contains(ConstellationEffectRegistry.ENTITY_TAG_LUCERNA_SKIP_ENTITY)) {
            return;
        }

        if (CommonConfig.CONFIG.mobSpawningDenyAllTypes.get()
                || entity.getType().getCategory() == MobCategory.MONSTER) {
            Vector3 entityPos = Vector3.atEntityCorner(entity);
            for (Map.Entry<WorldBlockPos, TickTokenMap.SimpleTickToken<Double>> entry
                    : spawnDenyRegions.entrySet()) {
                if (!entry.getKey().getWorldKey().equals(entity.level().dimension())) {
                    continue;
                }
                if (entityPos.distance(entry.getKey()) <= entry.getValue().getValue()) {
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }
        }
    }
}
