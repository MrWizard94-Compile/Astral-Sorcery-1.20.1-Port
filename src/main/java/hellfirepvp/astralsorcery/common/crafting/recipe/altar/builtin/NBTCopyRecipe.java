/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin;

import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperties;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAltar;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Altar recipe that copies crystal attributes from the first crystal input
 * to the output item as CrystalProperties (size/purity/cutting).
 *
 * <p>Enabled by adding {@code "copy_crystal_properties": true} to the recipe JSON.</p>
 */
public class NBTCopyRecipe extends SimpleAltarRecipe {

    public NBTCopyRecipe(@Nonnull ResourceLocation id,
                         @Nonnull BlockAltar.AltarType altarType,
                         int craftDuration, double starlightRequired,
                         @Nonnull ItemStack output,
                         @Nonnull NonNullList<Ingredient> inputs,
                         @Nullable ResourceLocation focusConstellation) {
        super(id, altarType, craftDuration, starlightRequired, output, inputs, focusConstellation);
    }

    public static NBTCopyRecipe convertToThis(@Nonnull SimpleAltarRecipe other) {
        return new NBTCopyRecipe(
                other.getId(), other.getAltarType(), other.getCraftDuration(),
                other.getStarlightRequired(), other.getOutput(),
                other.getIngredients(), other.getFocusConstellation());
    }

    @Nonnull
    @Override
    public List<ItemStack> getOutputs(@Nonnull BlockEntityAltar altar) {
        ItemStack output = getOutput();

        TileInventory inv = altar.getInventory();
        ItemStack crystalStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack slot = inv.getStackInSlot(i);
            if (!slot.isEmpty() && slot.getItem() instanceof ItemCrystalBase) {
                crystalStack = slot;
                break;
            }
        }

        if (!crystalStack.isEmpty()) {
            CrystalAttributes attr = CrystalAttributes.getCrystalAttributes(crystalStack);
            if (attr != null && !attr.isEmpty()) {
                CrystalProperties props = attributesToProperties(attr);
                CrystalProperties.setOnStack(output, props);
            }
        }

        return Collections.singletonList(output);
    }

    private static CrystalProperties attributesToProperties(@Nonnull CrystalAttributes attr) {
        int size = tierToRange(attr, CrystalPropertiesAS.Properties.PROPERTY_SIZE, CrystalProperties.MAX_SIZE);
        int purity = tierToRange(attr, CrystalPropertiesAS.Properties.PROPERTY_PURITY, CrystalProperties.MAX_PURITY);
        int cutting = tierToRange(attr, CrystalPropertiesAS.Properties.PROPERTY_SHAPE, CrystalProperties.MAX_CUTTING);
        return new CrystalProperties(size, purity, cutting);
    }

    private static int tierToRange(@Nonnull CrystalAttributes attr,
                                   @Nullable CrystalProperty property,
                                   int max) {
        if (property == null) return 0;
        CrystalAttributes.Attribute found = attr.getAttribute(property);
        if (found == null) return 0;
        int tier = found.getTier();
        int maxTier = property.getMaxTier();
        return Mth.clamp(Math.round((float) tier / maxTier * max), 0, max);
    }
}
