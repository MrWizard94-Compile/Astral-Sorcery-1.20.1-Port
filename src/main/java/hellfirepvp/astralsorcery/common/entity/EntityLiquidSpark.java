/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

/**
 * A liquid starlight spark entity that orbits around lightwells and chalices.
 * These are purely visual ambient entities that indicate active starlight
 * collection or fluid processing. They float, bob, and orbit the source block.
 *
 * <p>Sparks have a short lifespan (3-5 seconds) and fade out naturally.
 * They emit a soft blue-white glow and leave a subtle particle trail.</p>
 *
 * <p>1.16 → 1.20: Entity base class unchanged.</p>
 */
public class EntityLiquidSpark extends Entity {

    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(EntityLiquidSpark.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ALPHA =
            SynchedEntityData.defineId(EntityLiquidSpark.class, EntityDataSerializers.FLOAT);

    private int age = 0;
    private int maxAge = 60; // 3 seconds default
    private BlockPos orbitCenter = BlockPos.ZERO;
    private float orbitRadius = 0.5f;
    private float orbitSpeed = 0.1f;
    private float orbitAngle = 0.0f;

    public EntityLiquidSpark(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_SCALE, 0.15f);
        this.entityData.define(DATA_ALPHA, 1.0f);
    }

    /**
     * Configures the spark's orbit parameters after spawning.
     *
     * @param center  the block position to orbit around
     * @param radius  orbit radius in blocks
     * @param speed   angular speed in radians per tick
     * @param maxLife maximum lifetime in ticks
     */
    public void configureOrbit(@Nonnull BlockPos center, float radius,
                                float speed, int maxLife) {
        this.orbitCenter = center;
        this.orbitRadius = radius;
        this.orbitSpeed = speed;
        this.maxAge = maxLife;
        this.orbitAngle = (float) (random.nextFloat() * Math.PI * 2);
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        if (age >= maxAge) {
            this.discard();
            return;
        }

        // Fade out in the last 20 ticks
        if (age > maxAge - 20) {
            float fade = (maxAge - age) / 20.0f;
            this.entityData.set(DATA_ALPHA, fade);
        }

        // Orbit motion
        orbitAngle += orbitSpeed;
        double cx = orbitCenter.getX() + 0.5;
        double cy = orbitCenter.getY() + 1.0 + Math.sin(age * 0.05) * 0.2;
        double cz = orbitCenter.getZ() + 0.5;

        double targetX = cx + Math.cos(orbitAngle) * orbitRadius;
        double targetZ = cz + Math.sin(orbitAngle) * orbitRadius;

        Vec3 toTarget = new Vec3(targetX - getX(), cy - getY(), targetZ - getZ());
        setDeltaMovement(toTarget.scale(0.3));
        setPos(getX() + getDeltaMovement().x,
               getY() + getDeltaMovement().y,
               getZ() + getDeltaMovement().z);
    }

    public float getSparkScale() {
        return this.entityData.get(DATA_SCALE);
    }

    public float getSparkAlpha() {
        return this.entityData.get(DATA_ALPHA);
    }

    public void setSparkScale(float scale) {
        this.entityData.set(DATA_SCALE, Math.max(0.01f, scale));
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
        this.orbitCenter = new BlockPos(
                tag.getInt("OrbitX"), tag.getInt("OrbitY"), tag.getInt("OrbitZ"));
        this.orbitRadius = tag.getFloat("OrbitRadius");
        this.orbitSpeed = tag.getFloat("OrbitSpeed");
        this.orbitAngle = tag.getFloat("OrbitAngle");
        this.entityData.set(DATA_SCALE, tag.getFloat("Scale"));
        this.entityData.set(DATA_ALPHA, tag.getFloat("Alpha"));
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        tag.putInt("Age", age);
        tag.putInt("MaxAge", maxAge);
        tag.putInt("OrbitX", orbitCenter.getX());
        tag.putInt("OrbitY", orbitCenter.getY());
        tag.putInt("OrbitZ", orbitCenter.getZ());
        tag.putFloat("OrbitRadius", orbitRadius);
        tag.putFloat("OrbitSpeed", orbitSpeed);
        tag.putFloat("OrbitAngle", orbitAngle);
        tag.putFloat("Scale", getSparkScale());
        tag.putFloat("Alpha", getSparkAlpha());
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 1024; // Visible within 32 blocks
    }
}
