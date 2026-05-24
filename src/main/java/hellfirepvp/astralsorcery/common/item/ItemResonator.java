package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Resonator — reveals starlight sources and area of influence.
 * Upgradeable via altar: STARLIGHT (base) → FLUID_FIELDS (domic) → AREA_SIZE (ichosic).
 * Upgrades are stored as an int[] in NBT; shift-right-click cycles modes.
 *
 * <p>1.16 → 1.20 changes: ActionResult → InteractionResultHolder, World → Level.</p>
 */
public class ItemResonator extends ItemAS {

    private static final String TAG_UPGRADES = "upgrades";
    private static final String TAG_SELECTED = "selected_upgrade";

    public ItemResonator() {
        super(new Properties().stacksTo(1));
    }

    // -------------------------------------------------------------------------
    // Upgrade storage helpers
    // -------------------------------------------------------------------------

    /** Returns true if the given upgrade is stored in the stack's NBT. */
    public static boolean hasUpgrade(@Nonnull ItemStack stack, @Nonnull ResonatorUpgrade upgrade) {
        if (upgrade == ResonatorUpgrade.STARLIGHT) return true; // always available
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_UPGRADES)) return false;
        int[] stored = tag.getIntArray(TAG_UPGRADES);
        for (int v : stored) {
            if (v == upgrade.ordinal()) return true;
        }
        return false;
    }

    /** Adds the upgrade to the stack and sets it as the selected mode. */
    public static void applyUpgrade(@Nonnull ItemStack stack, @Nonnull ResonatorUpgrade upgrade) {
        CompoundTag tag = stack.getOrCreateTag();
        int[] existing = tag.contains(TAG_UPGRADES) ? tag.getIntArray(TAG_UPGRADES) : new int[0];
        for (int v : existing) {
            if (v == upgrade.ordinal()) {
                tag.putInt(TAG_SELECTED, upgrade.ordinal());
                return;
            }
        }
        int[] updated = Arrays.copyOf(existing, existing.length + 1);
        updated[updated.length - 1] = upgrade.ordinal();
        tag.putIntArray(TAG_UPGRADES, updated);
        tag.putInt(TAG_SELECTED, upgrade.ordinal());
    }

    /** Returns the currently selected upgrade, defaulting to STARLIGHT. */
    @Nonnull
    public static ResonatorUpgrade getSelectedUpgrade(@Nonnull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_SELECTED)) return ResonatorUpgrade.STARLIGHT;
        int idx = tag.getInt(TAG_SELECTED);
        ResonatorUpgrade[] values = ResonatorUpgrade.values();
        return (idx >= 0 && idx < values.length) ? values[idx] : ResonatorUpgrade.STARLIGHT;
    }

    /**
     * Cycles to the next unlocked upgrade. Wraps around. Called on shift-right-click.
     * Returns true if the mode changed.
     */
    public static boolean cycleUpgrade(@Nonnull ItemStack stack) {
        ResonatorUpgrade current = getSelectedUpgrade(stack);
        ResonatorUpgrade[] values = ResonatorUpgrade.values();
        for (int i = 1; i <= values.length; i++) {
            ResonatorUpgrade next = values[(current.ordinal() + i) % values.length];
            if (hasUpgrade(stack, next)) {
                stack.getOrCreateTag().putInt(TAG_SELECTED, next.ordinal());
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Item behaviour
    // -------------------------------------------------------------------------

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && !level.isClientSide()) {
            cycleUpgrade(stack);
        }
        // TODO Phase 12: starlight visualization / area-of-influence preview on right-click
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // -------------------------------------------------------------------------
    // Upgrade enum
    // -------------------------------------------------------------------------

    public enum ResonatorUpgrade {
        /** Base mode: shows starlight source locations and intensity. */
        STARLIGHT("starlight"),
        /** Domic upgrade: visualizes liquid starlight field effects. */
        FLUID_FIELDS("liquid"),
        /** Ichosic upgrade: previews area-of-influence for multiblock machines. */
        AREA_SIZE("structure");

        public final String appendix;

        ResonatorUpgrade(String appendix) {
            this.appendix = appendix;
        }
    }
}
