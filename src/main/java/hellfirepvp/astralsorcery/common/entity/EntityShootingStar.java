/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.common.lib.LootAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A shooting star entity that falls from the sky during special celestial events.
 * When it impacts the ground, it creates a small crater and drops rare loot
 * (star fragments, stardust, rock crystal shards).
 *
 * <p>Shooting stars spawn at high altitude and travel diagonally downward
 * with visible trail particles. They emit light and sound effects on impact.</p>
 *
 * <p>1.16 → 1.20: Entity base class unchanged. Network synched data stable.</p>
 */
public class EntityShootingStar extends Entity {

    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(EntityShootingStar.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(EntityShootingStar.class, EntityDataSerializers.INT);

    private int age = 0;
    private int maxAge = 200; // 10 seconds max flight
    private boolean impacted = false;

    public EntityShootingStar(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_SCALE, 1.0f);
        this.entityData.define(DATA_COLOR, 0xFFFFFF);
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        if (!level().isClientSide()) {
            if (age >= maxAge || impacted) {
                this.discard();
                return;
            }

            // Apply gravity and forward motion
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x * 0.99, motion.y - 0.04, motion.z * 0.99);
            setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);

            // Check for ground impact
            BlockPos below = blockPosition().below();
            if (!level().getBlockState(below).isAir() && getY() < level().getMaxBuildHeight()) {
                onImpact();
            }
        }
    }

    private void onImpact() {
        if (impacted) return;
        impacted = true;

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            Vec3 pos = position();
            LootTable table = serverLevel.getServer().getLootData()
                    .getLootTable(LootAS.STARFALL_SHOOTING_STAR_REWARD);
            LootParams params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, pos)
                    .create(LootContextParamSets.CHEST);
            List<ItemStack> drops = table.getRandomItems(params);
            for (ItemStack drop : drops) {
                ItemEntity ie = new ItemEntity(serverLevel, pos.x, pos.y + 0.5, pos.z, drop);
                ie.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(ie);
            }
            PacketChannel.sendToAllTracking(
                    new PktPlayEffect(PktPlayEffect.EffectType.SHOOTING_STAR_IMPACT, blockPosition()),
                    serverLevel, blockPosition());
            discard();
        }
    }

    /**
     * Initializes the shooting star's trajectory from spawn point.
     * Called after entity creation to set velocity toward ground.
     */
    public void initTrajectory(double velocityX, double velocityZ) {
        setDeltaMovement(velocityX, -0.8, velocityZ);
    }

    public float getStarScale() {
        return this.entityData.get(DATA_SCALE);
    }

    public void setStarScale(float scale) {
        this.entityData.set(DATA_SCALE, Math.max(0.1f, scale));
    }

    public int getStarColor() {
        return this.entityData.get(DATA_COLOR);
    }

    public void setStarColor(int color) {
        this.entityData.set(DATA_COLOR, color);
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        this.age = tag.getInt("Age");
        this.maxAge = tag.getInt("MaxAge");
        this.impacted = tag.getBoolean("Impacted");
        this.entityData.set(DATA_SCALE, tag.getFloat("Scale"));
        this.entityData.set(DATA_COLOR, tag.getInt("Color"));
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        tag.putInt("Age", age);
        tag.putInt("MaxAge", maxAge);
        tag.putBoolean("Impacted", impacted);
        tag.putFloat("Scale", this.entityData.get(DATA_SCALE));
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
