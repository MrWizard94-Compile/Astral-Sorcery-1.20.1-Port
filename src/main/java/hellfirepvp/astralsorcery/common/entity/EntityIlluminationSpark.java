/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * A floating illumination spark spawned by Illumination Powder or cave
 * illumination wand. Drifts upward slowly, emits light, and despawns
 * after a set duration. Placing a block where the spark hovers will
 * replace it with a permanent Illumination block.
 *
 * <p>No physics or collision — purely a light-emitting marker entity.</p>
 *
 * <p>1.16 → 1.20 changes:
 * DataParameter → EntityDataAccessor,
 * Entity constructor takes EntityType + Level.</p>
 */
public class EntityIlluminationSpark extends Entity {

    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(EntityIlluminationSpark.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ALPHA =
            SynchedEntityData.defineId(EntityIlluminationSpark.class, EntityDataSerializers.FLOAT);

    private int maxAge = 600; // 30 seconds default
    private int age = 0;
    private float driftSpeed = 0.005f;

    public EntityIlluminationSpark(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public EntityIlluminationSpark(EntityType<?> type, Level level,
                                    double x, double y, double z, int color) {
        this(type, level);
        this.setPos(x, y, z);
        this.entityData.set(DATA_COLOR, color);
        this.entityData.set(DATA_ALPHA, 1.0f);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_COLOR, 0xFFFFFF);
        this.entityData.define(DATA_ALPHA, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        // Slow upward drift
        this.setDeltaMovement(0, driftSpeed, 0);
        this.setPos(getX(), getY() + driftSpeed, getZ());

        // Fade out during last 100 ticks
        if (age > maxAge - 100) {
            float remaining = (float) (maxAge - age) / 100.0f;
            this.entityData.set(DATA_ALPHA, Math.max(0, remaining));
        }

        if (age >= maxAge) {
            this.discard();
        }
    }

    /**
     * Sets the maximum lifetime in ticks before despawn.
     *
     * @param maxAge lifetime in ticks
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = Math.max(1, maxAge);
    }

    /**
     * Sets the upward drift speed (blocks per tick).
     *
     * @param speed drift speed
     */
    public void setDriftSpeed(float speed) {
        this.driftSpeed = speed;
    }

    public int getColor() {
        return this.entityData.get(DATA_COLOR);
    }

    public float getAlpha() {
        return this.entityData.get(DATA_ALPHA);
    }

    public int getAge() {
        return age;
    }

    public int getMaxAge() {
        return maxAge;
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        this.age = tag.getInt("Age");
        this.maxAge = tag.getInt("MaxAge");
        this.driftSpeed = tag.getFloat("DriftSpeed");
        this.entityData.set(DATA_COLOR, tag.getInt("Color"));
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        tag.putInt("Age", age);
        tag.putInt("MaxAge", maxAge);
        tag.putFloat("DriftSpeed", driftSpeed);
        tag.putInt("Color", this.entityData.get(DATA_COLOR));
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
