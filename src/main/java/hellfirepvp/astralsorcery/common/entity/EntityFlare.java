/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.common.util.DamageSourceAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktParticleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A constellation flare projectile launched from ritual configurations.
 * Travels in an arc, damages entities on hit, and leaves a visual burst
 * at the impact location. The damage type and visual color depend on
 * which constellation powered the ritual.
 *
 * <p>1.16 → 1.20 changes:
 * ProjectileItemEntity → ThrowableProjectile,
 * DataParameter → EntityDataAccessor,
 * onImpact → onHit/onHitEntity/onHitBlock split.</p>
 */
public class EntityFlare extends ThrowableProjectile {

    private static final EntityDataAccessor<String> DATA_CONSTELLATION =
            SynchedEntityData.defineId(EntityFlare.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> DATA_DAMAGE =
            SynchedEntityData.defineId(EntityFlare.class, EntityDataSerializers.FLOAT);

    private int maxAge = 100; // 5 seconds
    private int age = 0;

    public EntityFlare(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public EntityFlare(EntityType<? extends ThrowableProjectile> type, Level level,
                       @Nonnull LivingEntity owner,
                       @Nullable ResourceLocation constellation, float damage) {
        super(type, owner, level);
        this.entityData.set(DATA_CONSTELLATION,
                constellation != null ? constellation.toString() : "");
        this.entityData.set(DATA_DAMAGE, damage);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_CONSTELLATION, "");
        this.entityData.define(DATA_DAMAGE, 4.0f);
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (age >= maxAge) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(@Nonnull EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide()) {
            Entity target = result.getEntity();
            float damage = this.entityData.get(DATA_DAMAGE);
            target.hurt(DamageSourceAS.stellar(this.level()), damage);
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(@Nonnull BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide()) {
            BlockPos impactPos = result.getBlockPos();
            PacketChannel.sendToAllTracking(
                    new PktParticleEvent(PktParticleEvent.STARLIGHT_BURST, impactPos),
                    (net.minecraft.server.level.ServerLevel) level(), impactPos);
            this.discard();
        }
    }

    @Override
    protected void onHit(@Nonnull HitResult result) {
        super.onHit(result);
        if (!level().isClientSide() && result.getType() == HitResult.Type.MISS) {
            // Missed everything — despawn
        }
    }

    /**
     * Gets the constellation key associated with this flare (for VFX color).
     */
    @Nullable
    public ResourceLocation getConstellation() {
        String val = this.entityData.get(DATA_CONSTELLATION);
        return val.isEmpty() ? null : new ResourceLocation(val);
    }

    public float getDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = Math.max(1, maxAge);
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.age = tag.getInt("Age");
        this.maxAge = tag.getInt("MaxAge");
        this.entityData.set(DATA_CONSTELLATION, tag.getString("Constellation"));
        this.entityData.set(DATA_DAMAGE, tag.getFloat("Damage"));
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Age", age);
        tag.putInt("MaxAge", maxAge);
        tag.putString("Constellation", this.entityData.get(DATA_CONSTELLATION));
        tag.putFloat("Damage", this.entityData.get(DATA_DAMAGE));
    }

    @Override
    protected float getGravity() {
        return 0.01f; // Very low gravity — mostly straight travel
    }
}
