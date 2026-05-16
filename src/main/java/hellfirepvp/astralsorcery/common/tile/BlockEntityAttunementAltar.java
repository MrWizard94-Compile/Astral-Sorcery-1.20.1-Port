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
 * Block entity for the Attunement Altar.
 * Attunes players and crystals to specific constellations.
 * Requires a valid multiblock structure with spectral relays.
 *
 * <p>Uses a held crystal ItemStack field rather than TileInventory,
 * since only a single crystal can be placed on the altar at a time.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * TileEntity -> BlockEntity, tick via BlockEntityTicker,
 * ResourceLocation for constellation keying</p>
 */
public class BlockEntityAttunementAltar extends BlockEntityTick {

    @Nonnull
    private ItemStack heldCrystal = ItemStack.EMPTY;

    private int attunementTick = 0;
    private int ticksExisted = 0;
    private boolean structureValid = false;
    private boolean isAttuning = false;

    @Nullable
    private ResourceLocation attunedConstellation = null;

    public BlockEntityAttunementAltar(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.ATTUNEMENT_ALTAR.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        ticksExisted++;
        if (isClientSide()) {
            // TODO: Client-side attunement visual effects (constellation lines, particles)
            return;
        }

        // TODO: Server-side attunement logic:
        // 1. Validate multiblock structure -> structureValid
        // 2. If structureValid && heldCrystal is present && attunedConstellation is set:
        //    - Set isAttuning = true
        //    - Increment attunementTick
        //    - Check sky visibility + constellation visibility
        // 3. On attunement completion:
        //    - Apply constellation to crystal NBT
        //    - Reset attunementTick, isAttuning
    }

    @Nonnull
    public ItemStack getHeldCrystal() {
        return heldCrystal;
    }

    public void setHeldCrystal(@Nonnull ItemStack stack) {
        this.heldCrystal = stack;
        markForUpdate();
    }

    public int getAttunementTick() {
        return attunementTick;
    }

    /**
     * Get the number of ticks this block entity has existed.
     * Used for renderer animations.
     */
    public int getTicksExisted() {
        return ticksExisted;
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public boolean isAttuning() {
        return isAttuning;
    }

    @Nullable
    public ResourceLocation getAttunedConstellation() {
        return attunedConstellation;
    }

    public void setAttunedConstellation(@Nullable ResourceLocation constellation) {
        this.attunedConstellation = constellation;
        markForUpdate();
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
        this.attunementTick = compound.getInt("attunementTick");
        this.structureValid = compound.getBoolean("structureValid");
        this.isAttuning = compound.getBoolean("isAttuning");
    }

    @Override
    public void writeSaveNBT(@Nonnull CompoundTag compound) {
        super.writeSaveNBT(compound);
        compound.putInt("attunementTick", attunementTick);
        compound.putBoolean("structureValid", structureValid);
        compound.putBoolean("isAttuning", isAttuning);
    }
}
