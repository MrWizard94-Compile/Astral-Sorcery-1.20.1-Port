package hellfirepvp.astralsorcery.common.capability;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 8 mandatory smoke test: PlayerProgress NBT serialization roundtrip.
 * This test MUST pass before Phase 7 or Phase 9 work begins.
 */
class PlayerProgressSerializationTest {

    @Test
    void testEmptyRoundtrip() {
        PlayerProgress original = new PlayerProgress();
        CompoundTag nbt = original.writeToNBT();
        PlayerProgress loaded = new PlayerProgress();
        loaded.readFromNBT(nbt);

        assertEquals(ProgressionTier.DISCOVERY, loaded.getTierReached());
        assertFalse(loaded.wasOnceAttuned());
        assertNull(loaded.getAttunedConstellation());
        assertTrue(loaded.getDiscoveredConstellations().isEmpty());
        assertEquals(0, loaded.getPerkPoints());
        assertEquals(0L, loaded.getPerkExp());
    }

    @Test
    void testFullDataRoundtrip() {
        PlayerProgress original = new PlayerProgress();
        original.setTierReached(ProgressionTier.CONSTELLATION_CRAFT);
        original.setAttunedConstellation(new ResourceLocation("astralsorcery", "armara"));
        original.discoverConstellation(new ResourceLocation("astralsorcery", "discidia"));
        original.discoverConstellation(new ResourceLocation("astralsorcery", "armara"));
        original.discoverConstellation(new ResourceLocation("astralsorcery", "vicio"));
        original.setPerkPoints(7);
        original.setPerkExp(12500L);

        // Serialize
        CompoundTag nbt = original.writeToNBT();
        assertNotNull(nbt);

        // Deserialize into a fresh instance
        PlayerProgress loaded = new PlayerProgress();
        loaded.readFromNBT(nbt);

        // Verify all fields match
        assertEquals(ProgressionTier.CONSTELLATION_CRAFT, loaded.getTierReached());
        assertTrue(loaded.wasOnceAttuned());
        ResourceLocation loadedAttuned = loaded.getAttunedConstellation();
        assertNotNull(loadedAttuned);
        assertEquals("astralsorcery:armara", loadedAttuned.toString());
        assertEquals(3, loaded.getDiscoveredConstellations().size());
        assertTrue(loaded.hasDiscovered(new ResourceLocation("astralsorcery", "discidia")));
        assertTrue(loaded.hasDiscovered(new ResourceLocation("astralsorcery", "armara")));
        assertTrue(loaded.hasDiscovered(new ResourceLocation("astralsorcery", "vicio")));
        assertFalse(loaded.hasDiscovered(new ResourceLocation("astralsorcery", "evorsio")));
        assertEquals(7, loaded.getPerkPoints());
        assertEquals(12500L, loaded.getPerkExp());
    }

    @Test
    void testCopyFrom() {
        PlayerProgress source = new PlayerProgress();
        source.setTierReached(ProgressionTier.TRAIT_CRAFT);
        source.setAttunedConstellation(new ResourceLocation("astralsorcery", "evorsio"));
        source.discoverConstellation(new ResourceLocation("astralsorcery", "evorsio"));
        source.setPerkPoints(3);
        source.setPerkExp(5000L);

        PlayerProgress target = new PlayerProgress();
        target.copyFrom(source);

        assertEquals(ProgressionTier.TRAIT_CRAFT, target.getTierReached());
        assertTrue(target.wasOnceAttuned());
        ResourceLocation copiedAttuned = target.getAttunedConstellation();
        assertNotNull(copiedAttuned);
        assertEquals("astralsorcery:evorsio", copiedAttuned.toString());
        assertEquals(1, target.getDiscoveredConstellations().size());
        assertEquals(3, target.getPerkPoints());
        assertEquals(5000L, target.getPerkExp());
    }

    @Test
    void testInvalidTierOrdinalDefaults() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("tierReached", 999); // Invalid ordinal

        PlayerProgress loaded = new PlayerProgress();
        loaded.readFromNBT(nbt);

        assertEquals(ProgressionTier.DISCOVERY, loaded.getTierReached(),
                "Invalid tier ordinal should default to DISCOVERY");
    }

    @Test
    void testPerkAllocationRoundtrip() {
        PlayerProgress original = new PlayerProgress();
        original.setTierReached(ProgressionTier.ATTUNEMENT);
        original.allocatePerk(new ResourceLocation("astralsorcery", "perk_root_aevitas"));
        original.allocatePerk(new ResourceLocation("astralsorcery", "perk_aevitas_regen_1"));
        original.allocatePerk(new ResourceLocation("astralsorcery", "perk_aevitas_life_1"));
        original.setPerkPoints(2);
        original.setPerkExp(3200L);

        CompoundTag nbt = original.writeToNBT();
        PlayerProgress loaded = new PlayerProgress();
        loaded.readFromNBT(nbt);

        assertEquals(3, loaded.getAllocatedPerks().size());
        assertTrue(loaded.hasPerkAllocated(new ResourceLocation("astralsorcery", "perk_root_aevitas")));
        assertTrue(loaded.hasPerkAllocated(new ResourceLocation("astralsorcery", "perk_aevitas_regen_1")));
        assertTrue(loaded.hasPerkAllocated(new ResourceLocation("astralsorcery", "perk_aevitas_life_1")));
        assertFalse(loaded.hasPerkAllocated(new ResourceLocation("astralsorcery", "perk_nonexistent")));
        assertEquals(2, loaded.getPerkPoints());
        assertEquals(3200L, loaded.getPerkExp());
    }

    @Test
    void testDoubleSerializeIdempotent() {
        PlayerProgress original = new PlayerProgress();
        original.setTierReached(ProgressionTier.ATTUNEMENT);
        original.discoverConstellation(new ResourceLocation("astralsorcery", "aevitas"));

        CompoundTag nbt1 = original.writeToNBT();
        PlayerProgress middle = new PlayerProgress();
        middle.readFromNBT(nbt1);
        CompoundTag nbt2 = middle.writeToNBT();
        PlayerProgress loaded = new PlayerProgress();
        loaded.readFromNBT(nbt2);

        assertEquals(ProgressionTier.ATTUNEMENT, loaded.getTierReached());
        assertEquals(1, loaded.getDiscoveredConstellations().size());
        assertTrue(loaded.hasDiscovered(new ResourceLocation("astralsorcery", "aevitas")));
    }
}
