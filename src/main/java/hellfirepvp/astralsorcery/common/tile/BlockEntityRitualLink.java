package hellfirepvp.astralsorcery.common.tile;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.common.auxiliary.link.LinkableTileEntity;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Block entity for the ritual link node.
 * Stores a link to another ritual link block and validates it each tick.
 *
 * <p>1.16 → 1.20: getWorld() → getLevel(), isRemote() → isClientSide(),
 * player.getEntityWorld() → (Level) player.level().</p>
 */
public class BlockEntityRitualLink extends BlockEntityTick implements LinkableTileEntity {

    private BlockPos linkedTo = null;

    public BlockEntityRitualLink(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.RITUAL_LINK.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        Level level = getLevel();
        if (level == null) return;

        if (!level.isClientSide()) {
            if (linkedTo != null) {
                final BlockPos target = linkedTo;
                MiscUtils.executeWithChunk(level, target, () -> {
                    BlockEntityRitualLink link = MiscUtils.getTileAt(level, target,
                            BlockEntityRitualLink.class, true);
                    if (link == null) {
                        linkedTo = null;
                        markForUpdate();
                    }
                });
            }
        }
    }

    @Nullable
    public BlockPos getLinkedTo() {
        return linkedTo;
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.linkedTo = NBTHelper.readFromSubTag(compound, "posLink", NBTHelper::readBlockPosFromNBT);
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        if (linkedTo != null) {
            NBTHelper.setAsSubTag(compound, "posLink", nbt -> NBTHelper.writeBlockPosToNBT(linkedTo, nbt));
        }
    }

    @Override
    public void onBlockLinkCreate(@Nonnull Player player, @Nonnull BlockPos other) {
        Level playerLevel = (Level) player.level();
        if (linkedTo != null) {
            BlockEntityRitualLink otherLink = MiscUtils.getTileAt(playerLevel, linkedTo,
                    BlockEntityRitualLink.class, true);
            if (otherLink != null) {
                otherLink.linkedTo = null;
                otherLink.markForUpdate();
            }
        }
        linkedTo = other;
        BlockEntityRitualLink otherLink = MiscUtils.getTileAt(playerLevel, other,
                BlockEntityRitualLink.class, true);
        if (otherLink != null) {
            otherLink.linkedTo = getBlockPos();
            otherLink.markForUpdate();
        }
        markForUpdate();
    }

    @Override
    public void onEntityLinkCreate(@Nonnull Player player, @Nonnull Entity linked) {
    }

    @Override
    public boolean tryLinkBlock(@Nonnull Player player, @Nonnull BlockPos other) {
        BlockEntityRitualLink otherLink = MiscUtils.getTileAt((Level) player.level(), other,
                BlockEntityRitualLink.class, true);
        return otherLink != null && otherLink.linkedTo == null && !other.equals(getBlockPos());
    }

    @Override
    public boolean tryLinkEntity(@Nonnull Player player, @Nonnull Entity other) {
        return false;
    }

    @Override
    public boolean tryUnlink(@Nonnull Player player, @Nonnull BlockPos other) {
        BlockEntityRitualLink otherLink = MiscUtils.getTileAt((Level) player.level(), other,
                BlockEntityRitualLink.class, true);
        if (otherLink == null || otherLink.linkedTo == null) return false;
        if (otherLink.linkedTo.equals(getBlockPos())) {
            linkedTo = null;
            otherLink.linkedTo = null;
            otherLink.markForUpdate();
            markForUpdate();
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public Level getLinkWorld() {
        return getLevel();
    }

    @Override
    @Nonnull
    public BlockPos getLinkPos() {
        return getBlockPos();
    }

    @Override
    @Nonnull
    public Component getUnLocalizedDisplayName() {
        return Component.translatable("block.astralsorcery.ritual_link");
    }

    @Override
    public boolean doesAcceptLinks() {
        return true;
    }

    @Override
    public boolean onSelect(@Nonnull Player player) {
        return true;
    }

    @Override
    @Nonnull
    public List<BlockPos> getLinkedPositions() {
        return linkedTo != null ? Lists.newArrayList(linkedTo) : Lists.newArrayList();
    }
}
