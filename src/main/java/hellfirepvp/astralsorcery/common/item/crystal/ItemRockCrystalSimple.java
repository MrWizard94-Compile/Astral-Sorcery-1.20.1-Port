package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
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
public class ItemRockCrystalSimple extends ItemCrystalBase {

    public ItemRockCrystalSimple() {
        super(defaultProperties().stacksTo(1));
    }

    /**
     * Creates an ItemStack with the specified crystal properties.
     * Properties are stored via {@link CrystalProperties#setOnStack} so that
     * tools, lenses, and altar recipes can read them via the same path.
     */
    @Nonnull
    public static ItemStack createCrystal(@Nonnull Item item, int size, int purity, int cutting) {
        ItemStack stack = new ItemStack(item);
        CrystalProperties.setOnStack(stack, new CrystalProperties(size, purity, cutting));
        return stack;
    }

    /** Returns the crystal's size [0–900], or 0 if none stored yet. */
    public static int getSize(@Nonnull ItemStack stack) {
        CrystalProperties props = CrystalProperties.getFromStack(stack);
        return props != null ? props.getSize() : 0;
    }

    /** Returns the crystal's purity [0–100], or 0 if none stored yet. */
    public static int getPurity(@Nonnull ItemStack stack) {
        CrystalProperties props = CrystalProperties.getFromStack(stack);
        return props != null ? props.getPurity() : 0;
    }

    /** Returns the crystal's cutting [0–100], or 0 if none stored yet. */
    public static int getCutting(@Nonnull ItemStack stack) {
        CrystalProperties props = CrystalProperties.getFromStack(stack);
        return props != null ? props.getCutting() : 0;
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

    @Override
    @Nullable
    public Item getTunedItemVariant() {
        return ItemsAS.ATTUNED_ROCK_CRYSTAL.get();
    }
}
