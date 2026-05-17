package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Rock Crystal — the foundational crafting material of Astral Sorcery.
 * Found by mining Rock Crystal Ore (rare, deep underground). Each crystal
 * has unique properties stored in NBT:
 * <ul>
 *   <li><b>Size</b> [1-900]: determines starlight capacity</li>
 *   <li><b>Purity</b> [0-100]: starlight transmission efficiency</li>
 *   <li><b>Cutting</b> [0-100]: starlight collection rate</li>
 * </ul>
 *
 * <p>Crystal properties affect altar crafting quality, lens transmission
 * efficiency, and collector crystal output.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * ITextComponent -> Component,
 * addInformation -> appendHoverText,
 * CompoundNBT -> CompoundTag</p>
 */
public class ItemRockCrystalSimple extends ItemAS {

    private static final String TAG_SIZE = "crystalSize";
    private static final String TAG_PURITY = "crystalPurity";
    private static final String TAG_CUTTING = "crystalCutting";

    public ItemRockCrystalSimple() {
        super(defaultProperties().stacksTo(1));
    }

    /**
     * Creates an ItemStack with the specified crystal properties.
     */
    @Nonnull
    public static ItemStack createCrystal(@Nonnull Item item, int size, int purity, int cutting) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_SIZE, Math.max(1, Math.min(900, size)));
        tag.putInt(TAG_PURITY, Math.max(0, Math.min(100, purity)));
        tag.putInt(TAG_CUTTING, Math.max(0, Math.min(100, cutting)));
        return stack;
    }

    /**
     * Gets the crystal size from the stack's NBT.
     *
     * @param stack the crystal item stack
     * @return size [1-900], default 100
     */
    public static int getSize(@Nonnull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_SIZE) ? tag.getInt(TAG_SIZE) : 100;
    }

    /**
     * Gets the crystal purity from the stack's NBT.
     *
     * @param stack the crystal item stack
     * @return purity [0-100], default 50
     */
    public static int getPurity(@Nonnull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_PURITY) ? tag.getInt(TAG_PURITY) : 50;
    }

    /**
     * Gets the crystal cutting quality from the stack's NBT.
     *
     * @param stack the crystal item stack
     * @return cutting [0-100], default 50
     */
    public static int getCutting(@Nonnull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_CUTTING) ? tag.getInt(TAG_CUTTING) : 50;
    }

    /**
     * Calculates the overall quality factor for this crystal.
     * Used in crafting quality calculations and starlight throughput.
     *
     * @param stack the crystal item stack
     * @return quality factor [0.0 - 1.0]
     */
    public static float getQualityFactor(@Nonnull ItemStack stack) {
        float size = getSize(stack) / 900.0f;
        float purity = getPurity(stack) / 100.0f;
        float cutting = getCutting(stack) / 100.0f;
        return (size * 0.3f + purity * 0.4f + cutting * 0.3f);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level,
                                @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.astralsorcery.rock_crystal.size",
                getSize(stack)));
        tooltip.add(Component.translatable("item.astralsorcery.rock_crystal.purity",
                getPurity(stack)));
        tooltip.add(Component.translatable("item.astralsorcery.rock_crystal.cutting",
                getCutting(stack)));
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return getPurity(stack) >= 90;
    }
}
