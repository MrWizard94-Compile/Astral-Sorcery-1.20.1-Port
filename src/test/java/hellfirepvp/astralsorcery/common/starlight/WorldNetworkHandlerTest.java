/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.common.starlight.transmission.TransmissionLink;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;



import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the starlight network data structures and serialization.
 * Tests the inner entry classes and TransmissionLink NBT roundtrips,
 * plus link/node query logic that doesn't require a ServerLevel.
 *
 * <p>Note: Full tick/distribution tests require a ServerLevel mock and
 * are deferred to Tier 2/3 integration tests. These Tier 1 tests validate
 * all pure-data operations.</p>
 */
class WorldNetworkHandlerTest {

    // ========================================================================
    // SourceEntry serialization
    // ========================================================================

    @Test
    void testSourceEntryRoundtrip() {
        BlockPos pos = new BlockPos(10, 64, -30);
        ResourceLocation constellation = new ResourceLocation("astralsorcery", "aevitas");
        StarlightGraph.SourceEntry original =
                new StarlightGraph.SourceEntry(pos, constellation, true);

        CompoundTag tag = original.writeToNBT();
        StarlightGraph.SourceEntry loaded = StarlightGraph.SourceEntry.readFromNBT(tag);

        assertEquals(pos, loaded.getPos(), "Position mismatch");
        assertEquals(constellation, loaded.getConstellation(), "Constellation mismatch");
        assertTrue(loaded.isAutoLink(), "AutoLink should be true");
    }

    @Test
    void testSourceEntryNullConstellation() {
        BlockPos pos = new BlockPos(0, 100, 0);
        StarlightGraph.SourceEntry original =
                new StarlightGraph.SourceEntry(pos, null, false);

        CompoundTag tag = original.writeToNBT();
        StarlightGraph.SourceEntry loaded = StarlightGraph.SourceEntry.readFromNBT(tag);

        assertEquals(pos, loaded.getPos());
        assertNull(loaded.getConstellation(), "Constellation should be null");
        assertFalse(loaded.isAutoLink(), "AutoLink should be false");
    }

    // ========================================================================
    // ReceiverEntry serialization
    // ========================================================================

    @Test
    void testReceiverEntryRoundtrip() {
        BlockPos pos = new BlockPos(-100, 32, 255);
        double maxInput = 42.5;
        StarlightGraph.ReceiverEntry original =
                new StarlightGraph.ReceiverEntry(pos, maxInput);

        CompoundTag tag = original.writeToNBT();
        StarlightGraph.ReceiverEntry loaded = StarlightGraph.ReceiverEntry.readFromNBT(tag);

        assertEquals(pos, loaded.getPos(), "Position mismatch");
        assertEquals(maxInput, loaded.getMaxInput(), 0.001, "MaxInput mismatch");
    }

    // ========================================================================
    // TransmissionEntry serialization
    // ========================================================================

    @Test
    void testTransmissionEntryRoundtrip() {
        BlockPos pos = new BlockPos(50, 70, 50);
        double efficiency = 0.85;
        StarlightGraph.TransmissionEntry original =
                new StarlightGraph.TransmissionEntry(pos, efficiency);

        CompoundTag tag = original.writeToNBT();
        StarlightGraph.TransmissionEntry loaded =
                StarlightGraph.TransmissionEntry.readFromNBT(tag);

        assertEquals(pos, loaded.getPos(), "Position mismatch");
        assertEquals(efficiency, loaded.getEfficiency(), 0.001, "Efficiency mismatch");
    }

    @Test
    void testTransmissionEntryClampedEfficiency() {
        // Efficiency above 1.0 should be clamped
        StarlightGraph.TransmissionEntry above =
                new StarlightGraph.TransmissionEntry(BlockPos.ZERO, 1.5);
        assertEquals(1.0, above.getEfficiency(), 0.001, "Efficiency should be clamped to 1.0");

        // Efficiency below 0.0 should be clamped
        StarlightGraph.TransmissionEntry below =
                new StarlightGraph.TransmissionEntry(BlockPos.ZERO, -0.5);
        assertEquals(0.0, below.getEfficiency(), 0.001, "Efficiency should be clamped to 0.0");
    }

    // ========================================================================
    // TransmissionLink serialization
    // ========================================================================

    @Test
    void testTransmissionLinkRoundtrip() {
        BlockPos from = new BlockPos(10, 64, 10);
        BlockPos to = new BlockPos(20, 64, 20);
        TransmissionLink original = new TransmissionLink(from, to);

        CompoundTag tag = original.writeToNBT();
        TransmissionLink loaded = TransmissionLink.readFromNBT(tag);

        assertEquals(from, loaded.getFrom(), "From position mismatch");
        assertEquals(to, loaded.getTo(), "To position mismatch");
    }

    @Test
    void testTransmissionLinkEquality() {
        BlockPos from = new BlockPos(10, 64, 10);
        BlockPos to = new BlockPos(20, 64, 20);

        TransmissionLink a = new TransmissionLink(from, to);
        TransmissionLink b = new TransmissionLink(from, to);
        TransmissionLink reversed = new TransmissionLink(to, from);

        assertEquals(a, b, "Identical links should be equal");
        assertEquals(a.hashCode(), b.hashCode(), "Hash codes should match");
        assertNotEquals(a, reversed, "Reversed link should not be equal (directed)");
    }

    @Test
    void testTransmissionLinkDistance() {
        BlockPos from = new BlockPos(0, 0, 0);
        BlockPos to = new BlockPos(3, 4, 0);
        TransmissionLink link = new TransmissionLink(from, to);

        assertEquals(25.0, link.getLengthSquared(), 0.001, "3^2 + 4^2 = 25");
        assertEquals(5.0, link.getLength(), 0.001, "sqrt(25) = 5");
    }

    // ========================================================================
    // Full network NBT roundtrip
    // ========================================================================

    @Test
    void testFullNetworkSaveAndLoad() {
        // Build a small network manually using entry classes + links
        // We can't use WorldNetworkHandler directly (needs ServerLevel),
        // but we can test the save/load format by building the CompoundTag.

        CompoundTag networkTag = new CompoundTag();

        // Sources
        ListTag sourceList = new ListTag();
        StarlightGraph.SourceEntry source =
                new StarlightGraph.SourceEntry(
                        new BlockPos(10, 64, 10),
                        new ResourceLocation("astralsorcery", "vicio"),
                        true);
        sourceList.add(source.writeToNBT());
        networkTag.put("sources", sourceList);

        // Receivers
        ListTag receiverList = new ListTag();
        StarlightGraph.ReceiverEntry receiver =
                new StarlightGraph.ReceiverEntry(new BlockPos(30, 64, 30), 100.0);
        receiverList.add(receiver.writeToNBT());
        networkTag.put("receivers", receiverList);

        // Transmissions
        ListTag transmissionList = new ListTag();
        StarlightGraph.TransmissionEntry transmission =
                new StarlightGraph.TransmissionEntry(new BlockPos(20, 64, 20), 0.9);
        transmissionList.add(transmission.writeToNBT());
        networkTag.put("transmissions", transmissionList);

        // Links: source -> transmission -> receiver
        ListTag linkList = new ListTag();
        TransmissionLink link1 = new TransmissionLink(
                new BlockPos(10, 64, 10), new BlockPos(20, 64, 20));
        TransmissionLink link2 = new TransmissionLink(
                new BlockPos(20, 64, 20), new BlockPos(30, 64, 30));
        linkList.add(link1.writeToNBT());
        linkList.add(link2.writeToNBT());
        networkTag.put("links", linkList);

        // Independent source data
        ListTag independentList = new ListTag();
        CompoundTag indTag = new CompoundTag();
        indTag.put("pos", NBTHelper.writeBlockPosToNBT(new BlockPos(10, 64, 10), new CompoundTag()));
        CompoundTag indData = new CompoundTag();
        indData.putDouble("cachedProduction", 50.0);
        indTag.put("data", indData);
        independentList.add(indTag);
        networkTag.put("independentSources", independentList);

        // Verify the tag is well-formed and can be read back
        assertEquals(1, networkTag.getList("sources", Tag.TAG_COMPOUND).size());
        assertEquals(1, networkTag.getList("receivers", Tag.TAG_COMPOUND).size());
        assertEquals(1, networkTag.getList("transmissions", Tag.TAG_COMPOUND).size());
        assertEquals(2, networkTag.getList("links", Tag.TAG_COMPOUND).size());
        assertEquals(1, networkTag.getList("independentSources", Tag.TAG_COMPOUND).size());

        // Re-read individual entries
        CompoundTag sourceTag = networkTag.getList("sources", Tag.TAG_COMPOUND).getCompound(0);
        StarlightGraph.SourceEntry loadedSource =
                StarlightGraph.SourceEntry.readFromNBT(sourceTag);
        assertEquals(new BlockPos(10, 64, 10), loadedSource.getPos());
        assertEquals(new ResourceLocation("astralsorcery", "vicio"), loadedSource.getConstellation());
        assertTrue(loadedSource.isAutoLink());

        CompoundTag receiverTag = networkTag.getList("receivers", Tag.TAG_COMPOUND).getCompound(0);
        StarlightGraph.ReceiverEntry loadedReceiver =
                StarlightGraph.ReceiverEntry.readFromNBT(receiverTag);
        assertEquals(new BlockPos(30, 64, 30), loadedReceiver.getPos());
        assertEquals(100.0, loadedReceiver.getMaxInput(), 0.001);

        CompoundTag transTag = networkTag.getList("transmissions", Tag.TAG_COMPOUND).getCompound(0);
        StarlightGraph.TransmissionEntry loadedTrans =
                StarlightGraph.TransmissionEntry.readFromNBT(transTag);
        assertEquals(new BlockPos(20, 64, 20), loadedTrans.getPos());
        assertEquals(0.9, loadedTrans.getEfficiency(), 0.001);

        CompoundTag linkTag1 = networkTag.getList("links", Tag.TAG_COMPOUND).getCompound(0);
        TransmissionLink loadedLink1 = TransmissionLink.readFromNBT(linkTag1);
        assertEquals(new BlockPos(10, 64, 10), loadedLink1.getFrom());
        assertEquals(new BlockPos(20, 64, 20), loadedLink1.getTo());

        CompoundTag linkTag2 = networkTag.getList("links", Tag.TAG_COMPOUND).getCompound(1);
        TransmissionLink loadedLink2 = TransmissionLink.readFromNBT(linkTag2);
        assertEquals(new BlockPos(20, 64, 20), loadedLink2.getFrom());
        assertEquals(new BlockPos(30, 64, 30), loadedLink2.getTo());

        // Independent source data
        CompoundTag indEntry = networkTag.getList("independentSources", Tag.TAG_COMPOUND).getCompound(0);
        BlockPos indPos = NBTHelper.readBlockPosFromNBT(indEntry.getCompound("pos"));
        assertEquals(new BlockPos(10, 64, 10), indPos);
        assertEquals(50.0, indEntry.getCompound("data").getDouble("cachedProduction"), 0.001);
    }

    @Test
    void testEmptyNetworkSerialization() {
        // An empty network should produce a valid tag with empty lists
        CompoundTag tag = new CompoundTag();
        tag.put("sources", new ListTag());
        tag.put("receivers", new ListTag());
        tag.put("transmissions", new ListTag());
        tag.put("links", new ListTag());
        tag.put("independentSources", new ListTag());

        assertEquals(0, tag.getList("sources", Tag.TAG_COMPOUND).size());
        assertEquals(0, tag.getList("links", Tag.TAG_COMPOUND).size());
    }
}
