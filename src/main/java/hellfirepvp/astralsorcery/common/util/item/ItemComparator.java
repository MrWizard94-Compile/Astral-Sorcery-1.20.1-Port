package hellfirepvp.astralsorcery.common.util.item;

import com.google.common.collect.Sets;
import hellfirepvp.astralsorcery.common.util.nbt.NBTComparator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Multi-clause ItemStack comparison utility.
 * Supports comparing by item type, count (exact or minimum),
 * NBT (strict or subset containment), and capability compatibility.
 *
 * <p>1.16 → 1.20 changes: ItemStack import path updated.</p>
 */
public class ItemComparator {

    public static boolean compare(@Nonnull ItemStack thisStack,
                                  @Nonnull ItemStack sampleCompare,
                                  @Nonnull Clause... clauses) {
        Set<Clause> lClauses = Sets.newHashSet(clauses);

        if (lClauses.contains(Clause.ITEM)) {
            if (thisStack.isEmpty() && !sampleCompare.isEmpty()) {
                return false;
            }
            // Includes inverse case of the above.
            if (!thisStack.isEmpty() && !thisStack.getItem().equals(sampleCompare.getItem())) {
                return false;
            }
        }

        if (lClauses.contains(Clause.AMOUNT_EXACT)) {
            if (thisStack.getCount() != sampleCompare.getCount()) {
                return false;
            }
        } else if (lClauses.contains(Clause.AMOUNT_LEAST)) {
            if (thisStack.getCount() > sampleCompare.getCount()) {
                return false;
            }
        }

        CompoundTag thisTag = thisStack.getTag();
        CompoundTag sampleTag = sampleCompare.getTag();
        boolean thisHasTag = thisTag != null && !thisTag.isEmpty();
        boolean sampleHasTag = sampleTag != null && !sampleTag.isEmpty();

        if (lClauses.contains(Clause.NBT_STRICT)) {
            if (!thisHasTag && sampleHasTag) {
                return false;
            }
            if (thisHasTag && thisTag != null
                    && (!sampleHasTag || sampleTag == null || !thisTag.equals(sampleTag))) {
                return false;
            }
        } else if (lClauses.contains(Clause.NBT_LEAST)) {
            if (thisHasTag && thisTag != null) {
                if (!sampleHasTag) {
                    return false;
                }
                if (sampleTag != null && !NBTComparator.contains(thisTag, sampleTag)) {
                    return false;
                }
            }
        }

        if (lClauses.contains(Clause.CAPABILITIES_COMPATIBLE)) {
            if (!thisStack.areCapsCompatible(sampleCompare)) {
                return false;
            }
        }

        return true;
    }

    public enum Clause {

        ITEM,

        AMOUNT_EXACT,
        AMOUNT_LEAST,

        NBT_STRICT,
        NBT_LEAST,

        CAPABILITIES_COMPATIBLE;

        public static class Sets {
            public static final Clause[] ITEMSTACK_STRICT =
                    { ITEM, AMOUNT_EXACT, NBT_STRICT, CAPABILITIES_COMPATIBLE };
            public static final Clause[] ITEMSTACK_STRICT_NOAMOUNT =
                    { ITEM, NBT_STRICT, CAPABILITIES_COMPATIBLE };
            public static final Clause[] ITEMSTACK_CRAFTING =
                    { ITEM, NBT_LEAST };
        }
    }
}
