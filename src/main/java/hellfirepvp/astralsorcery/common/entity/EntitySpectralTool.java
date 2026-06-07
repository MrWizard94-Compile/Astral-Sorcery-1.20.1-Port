/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.common.util.DamageSourceAS;
import hellfirepvp.astralsorcery.common.constellation.mantle.effect.MantleEffectPelotrio;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A spectral (ghost) tool entity summoned by the Pelotrio mantle effect.
 * Floats near the owner and periodically performs actions based on tool type:
 * sword → attacks nearby mobs, pickaxe → mines nearby blocks,
 * axe → fells nearby logs.
 *
 * <p>1.16 → 1.20 changes:
 * DataParameter → EntityDataAccessor,
 * DataSerializers → EntityDataSerializers,
 * ToolTask replaced by inline server-side tick logic,
 * world.addEntity() → level.addFreshEntity().</p>
 *
 * <p>Owner tracking uses UUID (persistent across restarts) not entity ID (transient).
 * Block drops pass the tool item to apply Fortune/Silk Touch correctly.</p>
 */
public class EntitySpectralTool extends Entity {

    private static final EntityDataAccessor<ItemStack> DATA_TOOL =
            SynchedEntityData.defineId(EntitySpectralTool.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(EntitySpectralTool.class, EntityDataSerializers.OPTIONAL_UUID);

    private int remainingTicks = 200;
    private float rotationDeg = 0.0f;
    private final float rotationSpeed = 5.0f;

    // Server-side AI state
    private int ticksUntilNextAction = 0;
    @Nullable private BlockPos targetBlock = null;

    public EntitySpectralTool(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_TOOL, ItemStack.EMPTY);
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
    }

    // ---- Data accessors ----

    public void setToolItem(@Nonnull ItemStack tool) {
        this.entityData.set(DATA_TOOL, tool);
    }

    @Nonnull
    public ItemStack getToolItem() {
        return this.entityData.get(DATA_TOOL);
    }

    public void setOwnerUUID(@Nonnull UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.of(uuid));
    }

    @Nonnull
    public Optional<UUID> getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID);
    }

    public float getRotation() {
        return rotationDeg;
    }

    public void setRemainingTicks(int ticks) {
        this.remainingTicks = Math.max(1, ticks);
    }

    // ---- Tick ----

    @Override
    public void tick() {
        super.tick();

        remainingTicks--;
        if (remainingTicks <= 0) {
            discard();
            return;
        }

        rotationDeg += rotationSpeed;
        if (rotationDeg >= 360.0f) rotationDeg -= 360.0f;

        if (!level().isClientSide()) {
            Entity owner = resolveOwner();
            if (owner == null) {
                discard();
                return;
            }

            // Drift toward owner's head when too far away
            if (tickCount % 5 == 0) {
                Vec3 toOwner = owner.position().add(0, 1.2, 0).subtract(position());
                if (toOwner.length() > 4.0) {
                    setDeltaMovement(getDeltaMovement().add(toOwner.normalize().scale(0.08)));
                }
            }

            // Perform tool action on cooldown
            if (ticksUntilNextAction > 0) {
                ticksUntilNextAction--;
            } else {
                performToolAction();
            }
        }

        Vec3 motion = getDeltaMovement();
        double bobY = Math.sin(tickCount * 0.1) * 0.005;
        setDeltaMovement(motion.x * 0.92, bobY, motion.z * 0.92);
        move(MoverType.SELF, getDeltaMovement());
    }

    @Nullable
    private Entity resolveOwner() {
        Optional<UUID> uuid = getOwnerUUID();
        if (uuid.isEmpty() || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getPlayerByUUID(uuid.get());
    }

    private void performToolAction() {
        ItemStack tool = getToolItem();
        if (tool.isEmpty()) {
            ticksUntilNextAction = 10;
            return;
        }
        if (tool.getItem() instanceof SwordItem) {
            performSwordAttack();
            ticksUntilNextAction = MantleEffectPelotrio.CONFIG.ticksPerSwordAttack.get();
        } else if (tool.getItem() instanceof PickaxeItem) {
            performMine(false);
            ticksUntilNextAction = MantleEffectPelotrio.CONFIG.ticksPerPickaxeBlockBreak.get();
        } else if (tool.getItem() instanceof AxeItem) {
            performMine(true);
            ticksUntilNextAction = MantleEffectPelotrio.CONFIG.ticksPerAxeLogBreak.get();
        } else {
            ticksUntilNextAction = 20;
        }
    }

    private void performSwordAttack() {
        AABB searchBox = getBoundingBox().inflate(5.0);
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e.getId() != this.getId() && !(e instanceof Player) && !e.isDeadOrDying());
        if (targets.isEmpty()) return;

        LivingEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (LivingEntity e : targets) {
            double d = distanceToSqr(e);
            if (d < nearestDist) {
                nearestDist = d;
                nearest = e;
            }
        }
        if (nearest != null) {
            nearest.hurt(DamageSourceAS.stellar(level()),
                    MantleEffectPelotrio.CONFIG.swordDamage.get().floatValue());
            Vec3 toTarget = nearest.position().subtract(position()).normalize().scale(0.3);
            setDeltaMovement(getDeltaMovement().add(toTarget));
        }
    }

    private void performMine(boolean axeMode) {
        Entity owner = resolveOwner();
        if (owner == null) return;
        BlockPos origin = owner.blockPosition();

        // Try committed target block first
        if (targetBlock != null) {
            BlockPos target = targetBlock; // non-null local for @Nonnull call sites
            BlockState state = level().getBlockState(target);
            if (!state.isAir() && isValidMineTarget(state, target, axeMode)) {
                // Drop resources with the tool's enchantments (Fortune/Silk Touch)
                Block.dropResources(state, (ServerLevel) level(), target,
                        level().getBlockEntity(target), null, getToolItem());
                level().removeBlock(target, false);
                targetBlock = null;
                return;
            }
            targetBlock = null;
        }

        // Search for a new target in a 5×3×5 area around the owner
        outer:
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos check = origin.offset(dx, dy, dz);
                    if (isValidMineTarget(level().getBlockState(check), check, axeMode)) {
                        targetBlock = check;
                        Vec3 toBlock = Vec3.atCenterOf(check).subtract(position()).normalize().scale(0.15);
                        setDeltaMovement(getDeltaMovement().add(toBlock));
                        break outer;
                    }
                }
            }
        }
    }

    private boolean isValidMineTarget(@Nonnull BlockState state, @Nonnull BlockPos pos, boolean axeMode) {
        if (state.isAir()) return false;
        if (axeMode) {
            return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES);
        }
        // Pickaxe targets any non-liquid, breakable block — use pos for context-dependent hardness
        return state.getFluidState().isEmpty() && state.getDestroySpeed(level(), pos) > 0;
    }

    // ---- Serialization ----

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        this.remainingTicks = tag.getInt("RemainingTicks");
        this.rotationDeg = tag.getFloat("Rotation");
        this.ticksUntilNextAction = tag.getInt("ActionCooldown");
        if (tag.contains("ToolItem")) {
            setToolItem(ItemStack.of(tag.getCompound("ToolItem")));
        }
        if (tag.hasUUID("OwnerUUID")) {
            setOwnerUUID(tag.getUUID("OwnerUUID"));
        }
        if (tag.contains("TargetBlockX")) {
            targetBlock = new BlockPos(tag.getInt("TargetBlockX"),
                    tag.getInt("TargetBlockY"), tag.getInt("TargetBlockZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        tag.putInt("RemainingTicks", remainingTicks);
        tag.putFloat("Rotation", rotationDeg);
        tag.putInt("ActionCooldown", ticksUntilNextAction);
        ItemStack tool = getToolItem();
        if (!tool.isEmpty()) {
            tag.put("ToolItem", tool.save(new CompoundTag()));
        }
        getOwnerUUID().ifPresent(uuid -> tag.putUUID("OwnerUUID", uuid));
        BlockPos tb2 = targetBlock;
        if (tb2 != null) {
            tag.putInt("TargetBlockX", tb2.getX());
            tag.putInt("TargetBlockY", tb2.getY());
            tag.putInt("TargetBlockZ", tb2.getZ());
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}
