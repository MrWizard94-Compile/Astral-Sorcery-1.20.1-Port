/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import net.minecraft.server.level.ServerLevel;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;

/**
 * A floating celestial crystal entity that slowly descends from the sky.
 * Spawned by rare celestial events or when a shooting star impacts
 * near certain terrain features (high-altitude marble).
 *
 * <p>The entity hovers gently with a bobbing motion before eventually
 * becoming collectible. While descending, it emits constellation particles
 * visible from long range.</p>
 *
 * <p>Unlike a normal ItemEntity, this has custom physics (very slow fall,
 * no despawn timer, and emits light).</p>
 *
 * <p>1.16 → 1.20: Entity data accessor pattern unchanged.
 * Packet instantiation uses NetworkHooks.getEntitySpawningPacket().</p>
 */
public class EntityCelestialCrystal extends Entity {

    private static final EntityDataAccessor<Float> DATA_ROTATION_SPEED =
            SynchedEntityData.defineId(EntityCelestialCrystal.class, EntityDataSerializers.FLOAT);

    /** How long (ticks) before the crystal becomes collectible. */
    private static final int DESCEND_DURATION = 200;

    /** Fall speed in blocks per tick during descent. */
    private static final double FALL_SPEED = 0.02;

    private int ticksAlive = 0;
    private boolean collected = false;

    public EntityCelestialCrystal(@Nonnull EntityType<?> type, @Nonnull Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public EntityCelestialCrystal(@Nonnull Level level, double x, double y, double z) {
        this(EntityTypesAS.CELESTIAL_CRYSTAL.get(), level);
        setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ROTATION_SPEED, 2.0f);
    }

    @Override
    public void tick() {
        super.tick();
        ticksAlive++;

        if (level().isClientSide()) {
            // Client: bobbing animation handled by renderer
            return;
        }

        if (ticksAlive == 1 && level() instanceof ServerLevel serverLevel) {
            PacketChannel.sendToAllTracking(
                    new PktPlayEffect(PktPlayEffect.EffectType.CELESTIAL_CRYSTAL_DESCEND, blockPosition()),
                    serverLevel, blockPosition());
        }

        if (ticksAlive < DESCEND_DURATION) {
            // Slowly descend
            setDeltaMovement(0, -FALL_SPEED, 0);
            Vec3 pos = position();
            setPos(pos.x, pos.y + getDeltaMovement().y, pos.z);
        } else {
            // Stop moving, become collectible
            setDeltaMovement(Vec3.ZERO);
            if (!collected) {
                convertToItem();
            }
        }

        // Prevent existing forever if stuck
        if (ticksAlive > 6000) { // 5 minutes
            convertToItem();
        }
    }

    /**
     * Converts this entity into a standard collectible item entity.
     */
    private void convertToItem() {
        if (collected || level().isClientSide()) return;
        collected = true;

        ItemStack crystal = new ItemStack(ItemsAS.CELESTIAL_CRYSTAL.get());
        ItemEntity itemEntity = new ItemEntity(level(), getX(), getY(), getZ(), crystal);
        itemEntity.setDefaultPickUpDelay();
        level().addFreshEntity(itemEntity);
        discard();
    }

    public float getRotationSpeed() {
        return this.entityData.get(DATA_ROTATION_SPEED);
    }

    public void setRotationSpeed(float speed) {
        this.entityData.set(DATA_ROTATION_SPEED, speed);
    }

    public int getTicksAlive() {
        return ticksAlive;
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag compound) {
        this.ticksAlive = compound.getInt("ticksAlive");
        this.collected = compound.getBoolean("collected");
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag compound) {
        compound.putInt("ticksAlive", ticksAlive);
        compound.putBoolean("collected", collected);
    }

    @Nonnull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}
