package hellfirepvp.astralsorcery.common.util.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NBTComparator deep-containment logic.
 */
class NBTComparatorTest {

    @Test
    void emptyContainsEmpty() {
        assertTrue(NBTComparator.contains(new CompoundTag(), new CompoundTag()));
    }

    @Test
    void emptySubsetContainsInLarger() {
        CompoundTag subset = new CompoundTag();
        CompoundTag full = new CompoundTag();
        full.putString("extra", "data");
        assertTrue(NBTComparator.contains(subset, full));
    }

    @Test
    void missingKeyFails() {
        CompoundTag subset = new CompoundTag();
        subset.putString("required", "value");
        CompoundTag full = new CompoundTag();
        assertFalse(NBTComparator.contains(subset, full));
    }

    @Test
    void matchingPrimitivesSucceeds() {
        CompoundTag a = new CompoundTag();
        a.putInt("count", 42);
        a.putString("name", "test");

        CompoundTag b = new CompoundTag();
        b.putInt("count", 42);
        b.putString("name", "test");
        b.putBoolean("extra", true);

        assertTrue(NBTComparator.contains(a, b));
    }

    @Test
    void mismatchedValueFails() {
        CompoundTag a = new CompoundTag();
        a.putInt("count", 42);

        CompoundTag b = new CompoundTag();
        b.putInt("count", 99);

        assertFalse(NBTComparator.contains(a, b));
    }

    @Test
    void nestedCompoundContainment() {
        CompoundTag innerA = new CompoundTag();
        innerA.putString("key", "val");
        CompoundTag a = new CompoundTag();
        a.put("nested", innerA);

        CompoundTag innerB = new CompoundTag();
        innerB.putString("key", "val");
        innerB.putInt("extra", 5);
        CompoundTag b = new CompoundTag();
        b.put("nested", innerB);

        assertTrue(NBTComparator.contains(a, b));
    }

    @Test
    void listContainmentOrderIndependent() {
        ListTag listA = new ListTag();
        listA.add(StringTag.valueOf("alpha"));
        listA.add(StringTag.valueOf("beta"));

        ListTag listB = new ListTag();
        listB.add(StringTag.valueOf("beta"));
        listB.add(StringTag.valueOf("alpha"));
        listB.add(StringTag.valueOf("gamma"));

        CompoundTag a = new CompoundTag();
        a.put("items", listA);
        CompoundTag b = new CompoundTag();
        b.put("items", listB);

        assertTrue(NBTComparator.contains(a, b));
    }

    @Test
    void listSubsetTooLargeFails() {
        ListTag listA = new ListTag();
        listA.add(StringTag.valueOf("a"));
        listA.add(StringTag.valueOf("b"));
        listA.add(StringTag.valueOf("c"));

        ListTag listB = new ListTag();
        listB.add(StringTag.valueOf("a"));

        CompoundTag a = new CompoundTag();
        a.put("items", listA);
        CompoundTag b = new CompoundTag();
        b.put("items", listB);

        assertFalse(NBTComparator.contains(a, b));
    }
}
