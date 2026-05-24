package hellfirepvp.astralsorcery.common.tile.base;

import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.awt.Color;

/**
 * Block entity that stores a "faked" block state to display in place of the real block.
 * Used for the translucent block (Evorsio perk) and tree beacon component.
 *
 * <p>1.16 → 1.20: getDefaultState() → defaultBlockState(),
 * Constants.BlockFlags.DEFAULT_AND_RERENDER → flag 3,
 * CompoundNBT → CompoundTag, getWorld() → getLevel(), getPos() → getBlockPos().</p>
 */
public abstract class BlockEntityFakedState extends BlockEntityTick {

    private BlockState fakedState = Blocks.AIR.defaultBlockState();
    private Color overlayColor = Color.WHITE;

    protected BlockEntityFakedState(@Nonnull BlockEntityType<?> type,
                                    @Nonnull BlockPos pos,
                                    @Nonnull BlockState state) {
        super(type, pos, state);
    }

    public boolean revert() {
        if (getLevel() == null || getLevel().isClientSide()) {
            return false;
        }
        return getLevel().setBlock(getBlockPos(), this.fakedState, 3);
    }

    @Nonnull
    public BlockState getFakedState() {
        return fakedState;
    }

    @Nonnull
    public Color getOverlayColor() {
        return overlayColor;
    }

    public void setFakedState(@Nonnull BlockState fakedState) {
        this.fakedState = fakedState;
        this.markForUpdate();
    }

    public void setOverlayColor(@Nonnull Color overlayColor) {
        this.overlayColor = overlayColor;
        this.markForUpdate();
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.fakedState = NBTHelper.getBlockStateFromTag(compound.getCompound("fakedState"),
                Blocks.AIR.defaultBlockState());
        this.overlayColor = new Color(compound.getInt("color"), false);
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        NBTHelper.setBlockState(compound, "fakedState", this.fakedState);
        compound.putInt("color", this.overlayColor.getRGB());
    }
}
