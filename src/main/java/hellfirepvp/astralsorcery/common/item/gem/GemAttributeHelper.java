package hellfirepvp.astralsorcery.common.item.gem;

import hellfirepvp.astralsorcery.common.perk.DynamicModifierHelper;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.type.AttributeTypeRegistry;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Rolls random perk attribute modifiers onto a perk gem ItemStack.
 * Modifiers are stored in persistent NBT via DynamicModifierHelper.
 *
 * Type scaling: DAY gets more (but weaker) modifiers, NIGHT fewer (but stronger),
 * SKY is balanced. Once rolled the modifiers are permanent for that stack.
 */
public final class GemAttributeHelper {

    private static final Random RAND = new Random();

    private static final float CHANCE_3_MODIFIERS = 0.4F;
    private static final float CHANCE_4_MODIFIERS = 0.15F;
    private static final float INC_LOWER = 0.05F;
    private static final float INC_HIGHER = 0.08F;

    private GemAttributeHelper() {}

    public static void rollGem(@Nonnull ItemStack gem) {
        rollGem(gem, RAND);
    }

    public static void rollGem(@Nonnull ItemStack gem, @Nonnull Random random) {
        if (!DynamicModifierHelper.getStaticModifiers(gem).isEmpty()) {
            return;
        }
        ItemPerkGem.GemType gemType = ItemPerkGem.getGemType(gem);
        if (gemType == null) {
            return;
        }

        List<PerkAttributeType> allTypes = new ArrayList<>(AttributeTypeRegistry.getAllTypes());
        if (allTypes.isEmpty()) {
            return;
        }

        int rolls = getPotentialMods(random, gemType.countModifier);
        Set<PerkAttributeType> used = new HashSet<>();

        for (int i = 0; i < rolls; i++) {
            List<PerkAttributeType> available = new ArrayList<>(allTypes);
            available.removeAll(used);
            if (available.isEmpty()) {
                break;
            }
            PerkAttributeType type = available.get(random.nextInt(available.size()));
            used.add(type);

            float exp = gemType.amplifierModifier <= 0F ? 1F : 1F / gemType.amplifierModifier;
            float scale = Math.max(0F, Math.min(1F, (float) Math.pow(random.nextFloat(), exp)));
            float value = INC_LOWER + scale * (INC_HIGHER - INC_LOWER);

            DynamicModifierHelper.addModifier(gem, UUID.randomUUID(), type, ModifierType.ADDED_MULTIPLY, value);
        }
    }

    private static int getPotentialMods(@Nonnull Random random, float countModifier) {
        int mods = 2;
        if (random.nextFloat() < (CHANCE_3_MODIFIERS + countModifier)) {
            mods++;
            if (random.nextFloat() < (CHANCE_4_MODIFIERS + countModifier)) {
                mods++;
            }
        }
        return mods;
    }
}
