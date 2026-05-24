package hellfirepvp.astralsorcery.common.enchantment.dynamic;

import java.util.Locale;

public enum DynamicEnchantmentType {

    ADD_TO_SPECIFIC,
    ADD_TO_EXISTING_SPECIFIC,
    ADD_TO_EXISTING_ALL(false);

    private final boolean specific;

    DynamicEnchantmentType() {
        this(true);
    }

    DynamicEnchantmentType(boolean specific) {
        this.specific = specific;
    }

    public boolean isEnchantmentSpecific() {
        return specific;
    }

    public String getDisplayName() {
        return String.format("astralsorcery.amulet.enchantment.%s.name", this.name().toLowerCase(Locale.ROOT));
    }
}
