package hellfirepvp.astralsorcery.common.enchantment.dynamic;

import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DynamicEnchantment {

    protected final DynamicEnchantmentType type;
    @Nullable
    protected final Enchantment enchantment;
    protected int levelAddition;

    public DynamicEnchantment(DynamicEnchantmentType type, @Nonnull Enchantment enchantment, int levelAddition) {
        if (!type.isEnchantmentSpecific()) {
            throw new IllegalArgumentException("Tried to create dynamic enchantment with a type that doesn't require an enchantment, but supplied an enchantment!");
        }
        this.type = type;
        this.enchantment = enchantment;
        this.levelAddition = levelAddition;
    }

    public DynamicEnchantment(DynamicEnchantmentType type, int levelAddition) {
        if (type.isEnchantmentSpecific()) {
            throw new IllegalArgumentException("Tried to create dynamic enchantment with a type that requires an enchantment without specifying such an enchantment!");
        }
        this.type = type;
        this.enchantment = null;
        this.levelAddition = levelAddition;
    }

    public DynamicEnchantmentType getType() {
        return type;
    }

    @Nullable
    public Enchantment getEnchantment() {
        return enchantment;
    }

    public int getLevelAddition() {
        return levelAddition;
    }

    public void setLevelAddition(int levelAddition) {
        this.levelAddition = levelAddition;
    }

    @Nonnull
    public DynamicEnchantment copy() {
        return this.copy(this.getLevelAddition());
    }

    @Nonnull
    public DynamicEnchantment copy(int level) {
        if (this.getType().isEnchantmentSpecific()) {
            return new DynamicEnchantment(this.getType(), this.getEnchantment(), level);
        } else {
            return new DynamicEnchantment(this.type, level);
        }
    }
}
