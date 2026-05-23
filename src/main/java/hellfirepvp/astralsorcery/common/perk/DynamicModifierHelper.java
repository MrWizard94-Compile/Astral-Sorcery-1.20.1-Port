package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Utility for reading and writing item-level dynamic perk attribute modifiers.
 * These are attribute bonuses engraved onto items via the telescope/engraving system.
 *
 * <p>Modifiers are stored in the mod's persistent data sub-tag under
 * {@link #KEY_MODIFIERS} as a list of compound tags, each serialized by
 * {@link PerkAttributeModifier#writeToNBT()}.</p>
 *
 * <p>getDynamicModifiers (queries AttributeModifierProvider / EquipmentAttributeModifierProvider)
 * and addModifierTooltip (client-only) are deferred to Phase 12.</p>
 *
 * <p>1.16 → 1.20: DynamicAttributeModifier consolidated into PerkAttributeModifier;
 * CompoundNBT/ListNBT → CompoundTag/ListTag; Constants.NBT.TAG_COMPOUND → Tag.TAG_COMPOUND.</p>
 */
public final class DynamicModifierHelper {

    public static final String KEY_MODIFIERS = "attribute_modifiers";

    private DynamicModifierHelper() {}

    /**
     * Appends a modifier to the stack's persistent NBT data.
     */
    public static void addModifier(@Nonnull ItemStack stack, @Nonnull UUID id,
                                   @Nonnull PerkAttributeType type,
                                   @Nonnull ModifierType modifierType, float value) {
        PerkAttributeModifier modifier = new PerkAttributeModifier(id, type.getKey(), modifierType, value);
        CompoundTag data = NBTHelper.getPersistentData(stack);
        ListTag list = data.getList(KEY_MODIFIERS, Tag.TAG_COMPOUND);
        list.add(modifier.writeToNBT());
        data.put(KEY_MODIFIERS, list);
    }

    /**
     * Returns the static modifiers stored directly in this stack's NBT, or empty if none.
     */
    @Nonnull
    public static List<PerkAttributeModifier> getStaticModifiers(@Nonnull ItemStack stack) {
        if (!NBTHelper.hasPersistentData(stack)) {
            return Collections.emptyList();
        }
        CompoundTag data = NBTHelper.getPersistentData(stack);
        if (!data.contains(KEY_MODIFIERS, Tag.TAG_LIST)) {
            return Collections.emptyList();
        }
        ListTag list = data.getList(KEY_MODIFIERS, Tag.TAG_COMPOUND);
        List<PerkAttributeModifier> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            result.add(PerkAttributeModifier.readFromNBT(list.getCompound(i)));
        }
        return result;
    }
}
