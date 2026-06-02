package hellfirepvp.astralsorcery.common.item.tool;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;

/**
 * Infused Crystal Sword — enhanced version of the Crystal Sword.
 * Special ability: on hitting an entity, fires a Celestial Strike —
 * a lightning bolt visual with area damage (radius 5, up to 12 damage).
 * 120-tick cooldown. Does not trigger on spectator/creative players.
 */
public class ItemInfusedCrystalSword extends ItemCrystalSword {

    private static final float BASE_AREA_DAMAGE = 12.0F;
    private static final float AREA_RADIUS = 5.0F;
    private static final int COOLDOWN_TICKS = 120;

    public ItemInfusedCrystalSword() {
        super();
    }

    @Override
    public boolean hurtEnemy(@Nonnull ItemStack stack, @Nonnull LivingEntity target,
                              @Nonnull LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        if (attacker instanceof ServerPlayer serverPlayer
                && !serverPlayer.getCooldowns().isOnCooldown(this)) {

            ServerLevel serverLevel = (ServerLevel) serverPlayer.level();

            // Visual: lightning bolt (visual only — no fire/blast damage from bolt itself)
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
            bolt.moveTo(target.getX(), target.getY(), target.getZ());
            bolt.setVisualOnly(true);
            serverLevel.addFreshEntity(bolt);

            // Area damage centred on the target
            AABB area = target.getBoundingBox().inflate(AREA_RADIUS);
            for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
                if (nearby == attacker) continue;
                if (nearby instanceof Player p && (p.isSpectator() || p.isCreative())) continue;
                float dist = (float) nearby.distanceTo(target);
                float factor = 1.0F - Mth.clamp(dist / AREA_RADIUS, 0F, 1F);
                if (factor > 0.05F) {
                    nearby.hurt(serverPlayer.damageSources().playerAttack(serverPlayer),
                            BASE_AREA_DAMAGE * factor);
                }
            }

            serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }

        return result;
    }
}
