package hellfirepvp.astralsorcery.common.data.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 8 smoke test: ChunkFluidEntry NBT serialization roundtrip.
 */
class ChunkFluidEntryTest {

    @Test
    void testEmptyRoundtrip() {
        ChunkFluidEntry original = new ChunkFluidEntry();
        CompoundTag nbt = original.writeToNBT();
        ChunkFluidEntry loaded = new ChunkFluidEntry();
        loaded.readFromNBT(nbt);

        assertTrue(loaded.isEmpty());
        assertEquals(0.0F, loaded.getRemaining(new ResourceLocation("astralsorcery", "aquamarine")));
    }

    @Test
    void testDataRoundtrip() {
        ResourceLocation aqua = new ResourceLocation("astralsorcery", "aquamarine");
        ResourceLocation crystal = new ResourceLocation("astralsorcery", "rock_crystal");

        ChunkFluidEntry original = new ChunkFluidEntry();
        original.setRemaining(aqua, 500.5F);
        original.setRemaining(crystal, 1200.0F);

        CompoundTag nbt = original.writeToNBT();
        ChunkFluidEntry loaded = new ChunkFluidEntry();
        loaded.readFromNBT(nbt);

        assertFalse(loaded.isEmpty());
        assertEquals(500.5F, loaded.getRemaining(aqua), 0.001F);
        assertEquals(1200.0F, loaded.getRemaining(crystal), 0.001F);
    }

    @Test
    void testDrain() {
        ResourceLocation aqua = new ResourceLocation("astralsorcery", "aquamarine");

        ChunkFluidEntry entry = new ChunkFluidEntry();
        entry.setRemaining(aqua, 100.0F);

        float drained = entry.drain(aqua, 30.0F);
        assertEquals(30.0F, drained, 0.001F);
        assertEquals(70.0F, entry.getRemaining(aqua), 0.001F);

        // Over-drain returns only what's available
        float overDrain = entry.drain(aqua, 200.0F);
        assertEquals(70.0F, overDrain, 0.001F);
        assertEquals(0.0F, entry.getRemaining(aqua), 0.001F);
        assertTrue(entry.isEmpty());
    }
}
