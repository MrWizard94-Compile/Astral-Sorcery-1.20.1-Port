package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Forge event handlers for the Astral Sorcery mob effect system.
 * Implements the death/drop hooks that MobEffect cannot express natively.
 *
 * <p>EffectCheatDeath — cancels death and blasts nearby entities.
 * EffectDropModifier — multiplies or voids a mob's loot drops.</p>
 */
public class EventHandlerEffects {

    // -----------------------------------------------------------------------
    // EffectCheatDeath — cancel the killing blow
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onCheatDeath(@Nonnull LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        MobEffectInstance instance = entity.getEffect(EffectsAS.EFFECT_CHEAT_DEATH.get());
        if (instance == null) return;

        event.setCanceled(true);

        int level = instance.getAmplifier();
        entity.removeEffect(EffectsAS.EFFECT_CHEAT_DEATH.get());
        entity.setHealth(Math.min(entity.getMaxHealth(), 4 + level * 2));
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 500, 1, false, false, true));

        // Blast nearby living entities away and set them on fire
        entity.level().getEntitiesOfClass(LivingEntity.class,
                entity.getBoundingBox().inflate(3),
                e -> e.isAlive() && e != entity
        ).forEach(nearby -> {
            nearby.setSecondsOnFire(10);
            nearby.knockback(2.0,
                    nearby.getX() - entity.getX(),
                    nearby.getZ() - entity.getZ());
        });
    }

    // -----------------------------------------------------------------------
    // EffectDropModifier — multiply or void drops
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onDropModifier(@Nonnull LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof Mob)) return;
        if (!entity.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;

        MobEffectInstance instance = entity.getEffect(EffectsAS.EFFECT_DROP_MODIFIER.get());
        if (instance == null) return;

        int amplifier = instance.getAmplifier();
        entity.removeEffect(EffectsAS.EFFECT_DROP_MODIFIER.get());

        if (amplifier == 0) {
            // Amplifier 0 is the special void-all case
            event.getDrops().clear();
        } else {
            // Roll the loot table amplifier extra times and add results
            for (int i = 0; i < amplifier; i++) {
                List<ItemStack> extra = EntityUtils.generateLoot(entity, event.getSource(), null);
                for (ItemStack stack : extra) {
                    if (stack.isEmpty()) continue;
                    ItemEntity drop = entity.spawnAtLocation(stack);
                    if (drop != null) {
                        event.getDrops().add(drop);
                    }
                }
            }
        }
    }
}
