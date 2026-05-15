package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block entity for the Ritual Pedestal.
 * Holds an attuned crystal to produce constellation-based area effects.
 * The active effect depends on the crystal's attuned constellation.
 *
 * <p>Uses a held crystal ItemStack field rather than TileInventory,
 * since only a single crystal can be placed on the pedestal at a time.</p>
 *
 * <p>Note: Will implement starlight network interfaces in a later phase
 * to receive starlight for powering rituals.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * ResourceLocation for constellation keying</p>
 */
public class BlockEntityRitualPedestal extends BlockEntityTick {

    private static final int DEFAULT_EFFECT_RANGE = 16;

    @Nonnull
    private ItemStack heldCrystal = ItemStack.EMPTY;

    @Nullable
    private ResourceLocation attunedConstellation = null;

    private boolean ritualActive = false;
    private int effectRange = DEFAULT_EFFECT_RANGE;
    private boolean hasMultiblock = false;

    public BlockEntityRitualPedestal(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.RITUAL_PEDESTAL.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (isClientSide()) {
            // TODO: Client-side ritual effect particles (constellation-dependent)
            return;
        }

        // TODO: Server-side ritual logic:
        // 1. Validate multiblock structure -> hasMultiblock
        // 2. If hasMultiblock && heldCrystal is present && attunedConstellation is set:
        //    - Check starlight network supply (requires network interfaces)
        //    - Set ritualActive = true
        //    - Apply constellation effect within effectRange
        // 3. If not valid, set ritualActive = false
    }

    @Nonnull
    public ItemStack getHeldCrystal() {
        return heldCrystal;
    }

    public void setHeldCrystal(@Nonnull ItemStack stack) {
        this.heldCrystal = stack;
        markForUpdate();
    }

    @Nullable
    public ResourceLocation getAttunedConstellation() {
        return attunedConstellation;
    }

    public void setAttunedConstellation(@Nullable ResourceLocation constellation) {
        this.attunedConstellation = constellation;
        markForUpdate();
    }

    public boolean isRitualActive() {
        return ritualActive;
    }

    public int getEffectRange() {
        return effectRange;
    }

    public void setEffectRange(int range) {
        this.effectRange = Math.max(1, range);
    }

    public boolean hasMultiblock() {
        return hasMultiblock;
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        this.heldCrystal = compound.contains("heldCrystal")
                ? ItemStack.of(compound.getCompound("heldCrystal"))
                : ItemStack.EMPTY;
        if (compound.contains("attunedConstellation")) {
            this.attunedConstellation = new ResourceLocation(compound.getString("attunedConstellation"));
        } else {
            this.attunedConstellation = null;
        }
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        if (!heldCrystal.isEmpty()) {
            CompoundTag crystalTag = new CompoundTag();
            heldCrystal.save(crystalTag);
            compound.put("heldCrystal", crystalTag);
        }
        if (attunedConstellation != null) {
            compound.putString("attunedConstellation", attunedConstellation.toString());
        }
    }

    @Override
    public void readSaveNBT(@Nonnull CompoundTag compound) {
        super.readSaveNBT(compound);
        this.ritualActive = compound.getBoolean("ritualActive");
        this.effectRange = compound.contains("effectRange")
                ? compound.getInt("effectRange") : DEFAULT_EFFECT_RANGE;
        this.hasMultiblock = compound.getBoolean("hasMultiblock");
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putBoolean("ritualActive", ritualActive);
        compound.putInt("effectRange", effectRange);
        compound.putBoolean("hasMultiblock", hasMultiblock);
    }
}
