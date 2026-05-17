/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;

/**
 * A spark entity spawned by Nocturnal Powder. Seeks out nearby hostile
 * mobs and damages them with starlight energy. After a short burst,
 * it dissipates. Can also be thrown to clear dark areas — the spark
 * will destroy spawner-viable light levels in a radius.
 *
 * <p>Behaviorally similar to a tracking particle: moves toward the
 * nearest hostile mob each tick, dealing damage on contact.</p>
 *
 * <p>1.16 → 1.20 changes:
 * DataParameter → EntityDataAccessor,
 * World → Level, getWorld() → level().</p>
 */
public class EntityNocturnalSpark extends Entity {

    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(EntityNocturnalSpark.class, EntityDataSerializers.INT);

    private int maxAge = 80; // 4 seconds
    private int age = 0;
    private float speed = 0.3f;
    private float damage = 6.0f;

    public EntityNocturnalSpark(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public EntityNocturnalSpark(EntityType<?> type, Level level,
                                 double x, double y, double z) {
        this(type, level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_TARGET_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        if (!level().isClientSide()) {
            Entity target = getTarget();
            if (target != null && target.isAlive()) {
                // Move toward target
                double dx = target.getX() - getX();
                double dy = target.getEyeY() - getY();
                double dz = target.getZ() - getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (dist < 0.5) {
                    // Hit target
                    target.hurt(this.damageSources().magic(), damage);
                    this.discard();
                    return;
                }

                if (dist > 0) {
                    double factor = speed / dist;
                    this.setDeltaMovement(dx * factor, dy * factor, dz * factor);
                    this.setPos(getX() + dx * factor, getY() + dy * factor, getZ() + dz * factor);
                }
            } else {
                // No target — search for one
                findTarget();
            }
        }

        if (age >= maxAge) {
            this.discard();
        }
    }

    private void findTarget() {
        // Find nearest hostile mob within 8 blocks
        Entity nearest = null;
        double nearestDist = 64.0; // 8^2
        for (Entity e : level().getEntities(this, this.getBoundingBox().inflate(8.0))) {
            if (e instanceof net.minecraft.world.entity.Mob mob
                    && mob.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER) {
                double dist = this.distanceToSqr(mob);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = mob;
                }
            }
        }
        if (nearest != null) {
            this.entityData.set(DATA_TARGET_ID, nearest.getId());
        }
    }

    @javax.annotation.Nullable
    private Entity getTarget() {
        int targetId = this.entityData.get(DATA_TARGET_ID);
        if (targetId == -1) return null;
        return level().getEntity(targetId);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = Math.max(1, maxAge);
    }

    public int getAge() {
        return age;
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        this.age = tag.getInt("Age");
        this.maxAge = tag.getInt("MaxAge");
        this.speed = tag.getFloat("Speed");
        this.damage = tag.getFloat("Damage");
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        tag.putInt("Age", age);
        tag.putInt("MaxAge", maxAge);
        tag.putFloat("Speed", speed);
        tag.putFloat("Damage", damage);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
