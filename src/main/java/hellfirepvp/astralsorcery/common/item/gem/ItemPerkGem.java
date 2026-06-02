package hellfirepvp.astralsorcery.common.item.gem;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.perk.DynamicModifierHelper;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.socket.IPerkGem;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * Perk gem — socket item for the perk tree.
 * Modifiers are randomly rolled on first inventoryTick and stored in persistent NBT.
 * DAY gems get more but weaker modifiers; NIGHT fewer but stronger; SKY is balanced.
 */
public class ItemPerkGem extends ItemAS implements IPerkGem {

    private final GemType type;

    public ItemPerkGem(@Nonnull GemType type) {
        super(defaultProperties().stacksTo(1));
        this.type = type;
    }

    @Nonnull
    public GemType getGemType() {
        return type;
    }

    @Nullable
    public static GemType getGemType(@Nonnull ItemStack gem) {
        if (gem.isEmpty() || !(gem.getItem() instanceof ItemPerkGem)) {
            return null;
        }
        return ((ItemPerkGem) gem.getItem()).type;
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull Entity entity, int slot, boolean isSelected) {
        if (world.isClientSide()) {
            return;
        }
        if (DynamicModifierHelper.getStaticModifiers(stack).isEmpty()) {
            GemAttributeHelper.rollGem(stack);
        }
    }

    @Override
    @Nonnull
    public List<PerkAttributeModifier> getPerkModifiers(@Nonnull ItemStack stack) {
        return DynamicModifierHelper.getStaticModifiers(stack);
    }

    public enum GemType implements StringRepresentable {
        DAY(0.6F, 0.4F),
        NIGHT(0F, 2.0F),
        SKY(0.15F, 1.0F);

        /** Increases chance of rolling extra modifiers (2 base, up to 4). */
        public final float countModifier;
        /** Exponent applied to the random roll; >1 biases toward smaller values, <1 toward larger. */
        public final float amplifierModifier;

        GemType(float countModifier, float amplifierModifier) {
            this.countModifier = countModifier;
            this.amplifierModifier = amplifierModifier;
        }

        @Override
        @Nonnull
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
