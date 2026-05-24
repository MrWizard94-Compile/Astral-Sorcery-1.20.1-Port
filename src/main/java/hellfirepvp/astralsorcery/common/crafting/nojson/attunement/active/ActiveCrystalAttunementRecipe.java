/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.attunement.active;

import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttuneCrystalRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunementRecipe;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktParticleEvent;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAttunementAltar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Server-side active state for crystal attunement in the attunement altar.
 *
 * <p>While active, the crystal item entity is held stationary above the altar.
 * On completion, the crystal item is swapped to its attuned variant (if needed)
 * and the constellation is written into the item's NBT.</p>
 *
 * <p>Client-side visual effects (FXOrbitalCrystalAttunement, orbital particles,
 * loop sound) are deferred to Phase 12 (client rendering).</p>
 *
 * <p>1.16 → 1.20: world.getEntityByID() → level.getEntity(),
 * entity.prevPosX/Y/Z → entity.xo/yo/zo (protected — use setPos only),
 * entity.setMotion() → entity.setDeltaMovement(),
 * crystal.getThrowerId() → crystal.getThrower().</p>
 */
public class ActiveCrystalAttunementRecipe extends AttunementRecipe.Active<AttuneCrystalRecipe> {

    private static final int DURATION = 500;

    @Nullable
    private ResourceLocation constellation;
    private int entityId;

    public ActiveCrystalAttunementRecipe(@Nonnull AttuneCrystalRecipe recipe,
                                         @Nullable ResourceLocation constellation,
                                         int crystalEntityId) {
        super(recipe);
        this.constellation = constellation;
        this.entityId = crystalEntityId;
    }

    public ActiveCrystalAttunementRecipe(@Nonnull AttuneCrystalRecipe recipe,
                                         @Nonnull CompoundTag nbt) {
        super(recipe);
        this.readFromNBT(nbt);
    }

    @Override
    public void startCrafting(@Nonnull BlockEntityAttunementAltar altar) {}

    @Override
    public void stopCrafting(@Nonnull BlockEntityAttunementAltar altar) {}

    @Override
    public boolean isFinished(@Nonnull BlockEntityAttunementAltar altar) {
        return getTick() >= DURATION;
    }

    @Override
    public void doTick(@Nonnull LogicalSide side, @Nonnull BlockEntityAttunementAltar altar) {
        if (side.isClient()) return;
        Level level = altar.getLevel();
        if (level == null) return;
        ItemEntity crystal = getEntity(level);
        if (crystal == null) return;

        // Hold the crystal entity stationary above the altar center
        double hoverX = altar.getBlockPos().getX() + 0.5;
        double hoverY = altar.getBlockPos().getY() + 1.4;
        double hoverZ = altar.getBlockPos().getZ() + 0.5;
        crystal.setPos(hoverX, hoverY, hoverZ);
        crystal.setDeltaMovement(0, 0, 0);

        if (getTick() % 10 == 0) {
            PacketChannel.sendToAllTracking(
                    new PktParticleEvent(PktParticleEvent.ATTUNEMENT_BEAM, altar.getBlockPos()),
                    (ServerLevel) level, altar.getBlockPos());
        }
    }

    @Override
    public void finishRecipe(@Nonnull BlockEntityAttunementAltar altar) {
        ResourceLocation cstKey = constellation;
        if (cstKey == null) return;
        Level level = altar.getLevel();
        if (level == null) return;

        IConstellation cst = ConstellationRegistry.getConstellation(cstKey);
        if (cst == null) return;

        ItemEntity crystalEntity = getEntity(level);
        if (crystalEntity == null) return;

        ItemStack stack = crystalEntity.getItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemCrystalBase baseItem)) return;

        // If this crystal doesn't yet implement ConstellationItem, swap to its attuned variant
        if (!(baseItem instanceof ConstellationItem)) {
            Item tunedVariant = baseItem.getTunedItemVariant();
            if (tunedVariant == null) return;
            CompoundTag existingTag = stack.getTag();
            stack = new ItemStack(tunedVariant, stack.getCount());
            if (existingTag != null) stack.setTag(existingTag.copy());
        }

        if (stack.getItem() instanceof ConstellationItem ci) {
            IWeakConstellation attuned = ci.getAttunedConstellation(stack);
            IMinorConstellation trait = ci.getTraitConstellation(stack);
            if (attuned == null && cst instanceof IWeakConstellation weak) {
                ci.setAttunedConstellation(stack, weak);
            } else if (trait == null && cst instanceof IMinorConstellation minor) {
                ci.setTraitConstellation(stack, minor);
            }
            crystalEntity.setItem(stack);
            PacketChannel.sendToAllTracking(
                    new PktParticleEvent(PktParticleEvent.CONSTELLATION_DISCOVER, altar.getBlockPos()),
                    (ServerLevel) level, altar.getBlockPos());
        }
    }

    @Nullable
    private ItemEntity getEntity(@Nonnull Level level) {
        if (entityId < 0) return null;
        Entity entity = level.getEntity(entityId);
        if (entity instanceof ItemEntity ie && ie.isAlive()) {
            return ie;
        }
        return null;
    }

    @Override
    public void writeToNBT(@Nonnull CompoundTag nbt) {
        super.writeToNBT(nbt);
        if (constellation != null) {
            nbt.putString("constellation", constellation.toString());
        }
        nbt.putInt("entityId", entityId);
    }

    @Override
    protected void readFromNBT(@Nonnull CompoundTag nbt) {
        super.readFromNBT(nbt);
        if (nbt.contains("constellation")) {
            constellation = new ResourceLocation(nbt.getString("constellation"));
        }
        entityId = nbt.getInt("entityId");
    }
}
