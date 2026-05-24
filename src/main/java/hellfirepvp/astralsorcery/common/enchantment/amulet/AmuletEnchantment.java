package hellfirepvp.astralsorcery.common.enchantment.amulet;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantment;
import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmuletEnchantment extends DynamicEnchantment {

    public AmuletEnchantment(DynamicEnchantmentType type, @Nonnull Enchantment enchantment, int levelAddition) {
        super(type, enchantment, levelAddition);
    }

    public AmuletEnchantment(DynamicEnchantmentType type, int levelAddition) {
        super(type, levelAddition);
    }

    @OnlyIn(Dist.CLIENT)
    public MutableComponent getDisplay() {
        String typeStr = this.getType().getDisplayName();
        String levelsStr = "astralsorcery.amulet.enchantment.level." + (this.levelAddition > 1 ? "more" : "one");

        if (this.getType().isEnchantmentSpecific()) {
            return Component.translatable(typeStr,
                    String.valueOf(this.getLevelAddition()),
                    Component.translatable(levelsStr),
                    this.getEnchantment().getFullname(this.getLevelAddition()));
        } else {
            return Component.translatable(typeStr,
                    String.valueOf(this.getLevelAddition()),
                    Component.translatable(levelsStr));
        }
    }

    public boolean canMerge(AmuletEnchantment other) {
        return this.type.equals(other.type) &&
                (!this.type.isEnchantmentSpecific() || this.enchantment.equals(other.enchantment));
    }

    public void merge(AmuletEnchantment src) {
        if (canMerge(src)) {
            this.levelAddition += src.levelAddition;
        }
    }

    public CompoundTag serialize() {
        CompoundTag cmp = new CompoundTag();
        cmp.putInt("type", this.type.ordinal());
        cmp.putInt("level", this.levelAddition);
        if (this.type.isEnchantmentSpecific()) {
            cmp.putString("ench", ForgeRegistries.ENCHANTMENTS.getKey(this.enchantment).toString());
        }
        return cmp;
    }

    @Nullable
    public static AmuletEnchantment deserialize(CompoundTag cmp) {
        int typeId = cmp.getInt("type");
        if (typeId < 0 || typeId >= DynamicEnchantmentType.values().length) {
            return null;
        }
        DynamicEnchantmentType type = DynamicEnchantmentType.values()[typeId];
        int level = Math.max(0, cmp.getInt("level"));
        if (type.isEnchantmentSpecific()) {
            ResourceLocation res = new ResourceLocation(cmp.getString("ench"));
            Enchantment e = ForgeRegistries.ENCHANTMENTS.getValue(res);
            if (e != null) {
                return new AmuletEnchantment(type, e, level);
            }
            return null;
        } else {
            return new AmuletEnchantment(type, level);
        }
    }
}
