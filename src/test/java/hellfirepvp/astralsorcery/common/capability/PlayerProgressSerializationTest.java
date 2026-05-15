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
        assertNotNull(loaded.getAttunedConstellation());
        assertEquals("astralsorcery:armara", loaded.getAttunedConstellation().toString());
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
        assertEquals("astralsorcery:evorsio", target.getAttunedConstellation().toString());
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
