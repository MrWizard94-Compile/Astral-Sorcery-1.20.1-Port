package hellfirepvp.astralsorcery.common.item.lens;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * Colored Lens — modifies the behavior of a Lens block in the starlight network.
 * Each color provides a different effect when applied to a lens (e.g., fire damage,
 * block breaking, growth acceleration, etc.).
 *
 * <p>1.16 -> 1.20 changes:
 * IStringSerializable -> StringRepresentable,
 * addInformation -> appendHoverText,
 * ITextComponent -> Component</p>
 */
public class ItemColoredLens extends ItemAS {

    private final LensColor lensColor;

    public ItemColoredLens(@Nonnull LensColor lensColor) {
        super(defaultProperties().stacksTo(16));
        this.lensColor = lensColor;
    }

    /**
     * Get the lens color type of this item.
     *
     * @return the lens color
     */
    @Nonnull
    public LensColor getLensColor() {
        return lensColor;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack,
                                @Nullable Level level,
                                @Nonnull List<Component> tooltip,
                                @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(
                "astralsorcery.tooltip.colored_lens.type",
                Component.translatable("astralsorcery.lens.color." + lensColor.getSerializedName())));
    }

    /**
     * Defines the available lens color types and their associated effects.
     */
    public enum LensColor implements StringRepresentable {
        FIRE,
        BREAK,
        GROWTH,
        DAMAGE,
        REGENERATION,
        PUSH,
        SPECTRAL;

        @Override
        @Nonnull
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
