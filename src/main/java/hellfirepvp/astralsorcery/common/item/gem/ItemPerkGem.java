package hellfirepvp.astralsorcery.common.item.gem;

import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * Perk Gem — socket items for the perk tree.
 * Different types provide different bonuses when socketed.
 *
 * <p>1.16 -> 1.20 changes:
 * IStringSerializable -> StringRepresentable</p>
 */
public class ItemPerkGem extends ItemAS {

    private final GemType type;

    public ItemPerkGem(@Nonnull GemType type) {
        super(defaultProperties());
        this.type = type;
    }

    @Nonnull
    public GemType getGemType() {
        return type;
    }

    public enum GemType implements StringRepresentable {
        DAY,
        NIGHT,
        SKY;

        @Override
        @Nonnull
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
