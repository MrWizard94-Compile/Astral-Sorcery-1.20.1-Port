package hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin;

import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.item.ItemResonator;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Altar recipe that upgrades an existing resonator by adding a new ResonatorUpgrade mode.
 * The output is derived from the input resonator's current NBT — existing unlocked upgrades
 * are preserved and the new upgrade is appended and selected.
 */
public class ResonatorUpgradeRecipe extends SimpleAltarRecipe {

    private final ItemResonator.ResonatorUpgrade targetUpgrade;

    public ResonatorUpgradeRecipe(@Nonnull ResourceLocation id,
                                   @Nonnull BlockAltar.AltarType altarType,
                                   int craftDuration, double starlightRequired,
                                   @Nonnull NonNullList<Ingredient> inputs,
                                   @Nonnull ItemResonator.ResonatorUpgrade targetUpgrade) {
        super(id, altarType, craftDuration, starlightRequired, ItemStack.EMPTY, inputs, null);
        this.targetUpgrade = targetUpgrade;
    }

    @Nonnull
    public ItemResonator.ResonatorUpgrade getTargetUpgrade() {
        return targetUpgrade;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializersAS.RESONATOR_UPGRADE.get();
    }

    /**
     * Scans the altar inventory for a resonator, copies it, applies the target upgrade,
     * and returns the result. Falls back to a fresh resonator with all upgrades up to
     * targetUpgrade if no resonator is found.
     */
    @Nonnull
    @Override
    public List<ItemStack> getOutputs(@Nonnull BlockEntityAltar altar) {
        int slotCount = getExpectedSlotCount();
        for (int i = 0; i < slotCount; i++) {
            ItemStack slot = altar.getInventory().getStackInSlot(i);
            if (!slot.isEmpty() && slot.getItem() instanceof ItemResonator) {
                ItemStack result = slot.copy();
                result.setCount(1);
                ItemResonator.applyUpgrade(result, targetUpgrade);
                return Collections.singletonList(result);
            }
        }
        // Fallback: build from scratch with all prior upgrades applied in order
        ItemStack fallback = new ItemStack(ItemsAS.RESONATOR.get());
        for (ItemResonator.ResonatorUpgrade upgrade : ItemResonator.ResonatorUpgrade.values()) {
            ItemResonator.applyUpgrade(fallback, upgrade);
            if (upgrade == targetUpgrade) break;
        }
        return Collections.singletonList(fallback);
    }

    public static ResonatorUpgradeRecipe convertToThis(@Nonnull SimpleAltarRecipe other,
                                                         @Nonnull ItemResonator.ResonatorUpgrade targetUpgrade) {
        return new ResonatorUpgradeRecipe(
                other.getId(), other.getAltarType(), other.getCraftDuration(),
                other.getStarlightRequired(), other.getIngredients(), targetUpgrade);
    }
}
