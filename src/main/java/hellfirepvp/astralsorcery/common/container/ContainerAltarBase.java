package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base container (menu) for all altar tiers.
 * Provides the crafting grid slots and player inventory binding.
 *
 * <p>1.16 -> 1.20 changes:
 * Container -> AbstractContainerMenu,
 * ContainerType -> MenuType,
 * PlayerInventory -> Inventory,
 * PlayerEntity -> Player,
 * transferStackInSlot -> quickMoveStack,
 * canInteractWith -> stillValid</p>
 */
public class ContainerAltarBase extends AbstractContainerMenu {

    // ContainerData slot indices — must match BlockEntityAltar.syncFromContainerData()
    static final int DATA_STARLIGHT   = 0;
    static final int DATA_CAPACITY    = 1;
    static final int DATA_IS_CRAFTING = 2;
    static final int DATA_RECIPE_TICK = 3;
    private static final int DATA_COUNT = 4;

    @Nonnull
    private final BlockEntityAltar altar;
    @Nonnull
    private final ContainerLevelAccess access;

    // Syncs dynamic display values server→client while the GUI is open.
    // get() is called server-side each tick; set() is called client-side on receive.
    private final ContainerData altarData = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case DATA_STARLIGHT   -> (int) altar.getStarlightStored();
                case DATA_CAPACITY    -> (int) altar.getStarlightCapacity();
                case DATA_IS_CRAFTING -> altar.isCrafting() ? 1 : 0;
                case DATA_RECIPE_TICK -> altar.getRecipeTick();
                default               -> 0;
            };
        }

        @Override
        public void set(int i, int value) {
            altar.syncFromContainerData(i, value);
        }

        @Override
        public int getCount() { return DATA_COUNT; }
    };

    protected ContainerAltarBase(@Nullable MenuType<?> type, int containerId,
                                 @Nonnull Inventory playerInv,
                                 @Nonnull BlockEntityAltar altar) {
        super(type, containerId);
        this.altar = altar;
        net.minecraft.world.level.Level altarLevel = altar.getLevel();
        this.access = altarLevel != null
                ? ContainerLevelAccess.create(altarLevel, altar.getBlockPos())
                : ContainerLevelAccess.NULL;

        // Register ContainerData for live server→client sync while menu is open
        this.addDataSlots(altarData);

        // Add altar crafting slots
        addAltarSlots();

        // Add player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9,
                        8 + col * 18, 84 + row * 18));
            }
        }

        // Add player hotbar (1 row of 9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    /**
     * Add altar-specific crafting grid slots. Override in tier subclasses
     * for different slot counts / layouts.
     *
     * <p>Uses Forge's {@link SlotItemHandler} instead of vanilla {@link Slot}
     * because TileInventory extends ItemStackHandler (IItemHandler), not Container.</p>
     */
    protected void addAltarSlots() {
        int activeSlots = altar.getActiveSlotCount();
        // Default 3x3 grid (Discovery tier) — subclasses override for larger grids
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = col + row * 3;
                if (slotIndex < activeSlots) {
                    this.addSlot(new SlotItemHandler(altar.getInventory(), slotIndex,
                            30 + col * 18, 17 + row * 18));
                }
            }
        }
    }

    @Nonnull
    public BlockEntityAltar getAltar() {
        return altar;
    }

    /**
     * Get the current starlight stored in the altar.
     * Used by the screen to render the starlight meter.
     */
    public float getStarlightStored() {
        return altar.getStarlightStored();
    }

    /**
     * Get the altar's maximum starlight capacity.
     * Used by the screen to render the starlight meter.
     */
    public float getStarlightCapacity() {
        return altar.getStarlightCapacity();
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        ItemStack remainder = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            remainder = stackInSlot.copy();

            int altarSlotCount = altar.getActiveSlotCount();

            if (index < altarSlotCount) {
                // Moving FROM altar TO player inventory
                if (!this.moveItemStackTo(stackInSlot, altarSlotCount, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving FROM player TO altar
                if (!this.moveItemStackTo(stackInSlot, 0, altarSlotCount, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return remainder;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return stillValid(this.access, player, altar.getBlockState().getBlock());
    }
}
