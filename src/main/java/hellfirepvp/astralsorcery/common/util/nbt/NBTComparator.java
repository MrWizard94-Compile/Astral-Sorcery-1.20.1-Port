package hellfirepvp.astralsorcery.common.util.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Deep NBT comparison with subset-containment semantics.
 * Checks if one CompoundTag contains all keys/values of another.
 *
 * <p>1.16 → 1.20 renames: CompoundNBT → CompoundTag, ListNBT → ListTag, INBT → Tag.</p>
 */
public class NBTComparator {

    /**
     * Check if {@code otherCompound} contains all keys and matching values from {@code thisCompound}.
     *
     * @param thisCompound  the expected subset
     * @param otherCompound the compound to check against
     * @return true if otherCompound contains all of thisCompound's data
     */
    public static boolean contains(@Nonnull CompoundTag thisCompound, @Nonnull CompoundTag otherCompound) {
        for (String key : thisCompound.getAllKeys()) {
            if (!otherCompound.contains(key)) {
                return false;
            }

            Tag thisNBT = thisCompound.get(key);
            Tag otherNBT = otherCompound.get(key);
            if (!compare(thisNBT, otherNBT)) {
                return false;
            }
        }
        return true;
    }

    private static boolean containList(@Nonnull ListTag base, @Nonnull ListTag other) {
        if (base.size() > other.size()) {
            return false;
        }

        List<Integer> matched = new ArrayList<>();
        lblMatching:
        for (Tag thisNbt : base) {
            for (int matchIndex = 0; matchIndex < other.size(); matchIndex++) {
                Tag matchNBT = other.get(matchIndex);

                if (!matched.contains(matchIndex)) {
                    if (compare(thisNbt, matchNBT)) {
                        matched.add(matchIndex);
                        continue lblMatching;
                    }
                }
            }

            return false;
        }

        return true;
    }

    private static boolean compare(Tag thisEntry, Tag thatEntry) {
        if (thisEntry instanceof CompoundTag thisCompound && thatEntry instanceof CompoundTag thatCompound) {
            return contains(thisCompound, thatCompound);
        } else if (thisEntry instanceof ListTag thisList && thatEntry instanceof ListTag thatList) {
            return containList(thisList, thatList);
        } else {
            return thisEntry.equals(thatEntry);
        }
    }
}
