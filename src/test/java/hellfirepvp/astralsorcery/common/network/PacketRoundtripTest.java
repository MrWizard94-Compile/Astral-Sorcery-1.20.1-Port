/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network;

import hellfirepvp.astralsorcery.common.network.play.client.PktAttunePlayer;
import hellfirepvp.astralsorcery.common.network.play.client.PktGatewayTeleport;
import hellfirepvp.astralsorcery.common.network.play.client.PktPerkAllocate;
import hellfirepvp.astralsorcery.common.network.play.client.PktPerkDeallocate;
import hellfirepvp.astralsorcery.common.network.play.client.PktRequestProgress;
import hellfirepvp.astralsorcery.common.network.play.server.PktAltarCraftingUpdate;
import hellfirepvp.astralsorcery.common.network.play.server.PktDiscoveryUpdate;
import hellfirepvp.astralsorcery.common.network.play.server.PktParticleEvent;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncConstellation;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncPerkData;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncStarlightCharge;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncStarlightNetwork;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Encode/decode roundtrip tests for network packets.
 * Verifies that all fields survive serialization without loss.
 */
class PacketRoundtripTest {

    @Test
    void testPktParticleEventRoundtrip() {
        BlockPos pos = new BlockPos(100, 64, -200);
        PktParticleEvent original = new PktParticleEvent(
                PktParticleEvent.ALTAR_CRAFT, pos, 1.5, 2.5, 3.5);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktParticleEvent.encode(original, buf);

        PktParticleEvent decoded = PktParticleEvent.decode(buf);

        assertEquals(PktParticleEvent.ALTAR_CRAFT, decoded.getEventType());
        assertEquals(pos, decoded.getPos());
        assertEquals(1.5, decoded.getData1(), 0.001);
        assertEquals(2.5, decoded.getData2(), 0.001);
        assertEquals(3.5, decoded.getData3(), 0.001);

        buf.release();
    }

    @Test
    void testPktParticleEventNoData() {
        BlockPos pos = new BlockPos(0, 0, 0);
        PktParticleEvent original = new PktParticleEvent(
                PktParticleEvent.GATEWAY_ACTIVATE, pos);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktParticleEvent.encode(original, buf);

        PktParticleEvent decoded = PktParticleEvent.decode(buf);

        assertEquals(PktParticleEvent.GATEWAY_ACTIVATE, decoded.getEventType());
        assertEquals(pos, decoded.getPos());
        assertEquals(0.0, decoded.getData1(), 0.001);
        assertEquals(0.0, decoded.getData2(), 0.001);
        assertEquals(0.0, decoded.getData3(), 0.001);

        buf.release();
    }

    @Test
    void testPktParticleEventNegativeCoordinates() {
        BlockPos pos = new BlockPos(-30000000, -64, 30000000);
        PktParticleEvent original = new PktParticleEvent(
                PktParticleEvent.STARLIGHT_BURST, pos, -1.0, Double.MAX_VALUE, Double.MIN_VALUE);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktParticleEvent.encode(original, buf);

        PktParticleEvent decoded = PktParticleEvent.decode(buf);

        assertEquals(PktParticleEvent.STARLIGHT_BURST, decoded.getEventType());
        assertEquals(pos, decoded.getPos());
        assertEquals(-1.0, decoded.getData1(), 0.001);
        assertEquals(Double.MAX_VALUE, decoded.getData2(), 0.001);
        assertEquals(Double.MIN_VALUE, decoded.getData3(), 0.001);

        buf.release();
    }

    @Test
    void testPktParticleEventAllEventTypes() {
        // Verify all event type constants roundtrip correctly
        int[] types = {
                PktParticleEvent.ALTAR_CRAFT,
                PktParticleEvent.CRYSTAL_FORM,
                PktParticleEvent.ATTUNEMENT_BEAM,
                PktParticleEvent.RITUAL_ACTIVATE,
                PktParticleEvent.WELL_COLLECT,
                PktParticleEvent.INFUSER_CRAFT,
                PktParticleEvent.GATEWAY_ACTIVATE,
                PktParticleEvent.STARLIGHT_BURST,
                PktParticleEvent.CONSTELLATION_DISCOVER,
                PktParticleEvent.FOUNTAIN_PRIME
        };

        for (int type : types) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            PktParticleEvent msg = new PktParticleEvent(type, BlockPos.ZERO);
            PktParticleEvent.encode(msg, buf);
            PktParticleEvent decoded = PktParticleEvent.decode(buf);
            assertEquals(type, decoded.getEventType(),
                    "Event type " + type + " failed roundtrip");
            buf.release();
        }
    }

    // ========================================================================
    // PktSyncPerkData roundtrip
    // ========================================================================

    @Test
    void testPktSyncPerkDataRoundtrip() {
        Set<ResourceLocation> perks = new HashSet<>();
        perks.add(new ResourceLocation("astralsorcery", "perk.root.aevitas"));
        perks.add(new ResourceLocation("astralsorcery", "perk.key.armor_1"));
        perks.add(new ResourceLocation("astralsorcery", "perk.major.damage_2"));

        PktSyncPerkData original = new PktSyncPerkData(perks, 154200L, 7);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncPerkData.encode(original, buf);
        PktSyncPerkData decoded = PktSyncPerkData.decode(buf);

        assertEquals(perks, decoded.getAllocatedPerks());
        assertEquals(154200L, decoded.getPerkExp());
        assertEquals(7, decoded.getAvailablePoints());

        buf.release();
    }

    @Test
    void testPktSyncPerkDataEmpty() {
        PktSyncPerkData original = new PktSyncPerkData(Set.of(), 0L, 0);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncPerkData.encode(original, buf);
        PktSyncPerkData decoded = PktSyncPerkData.decode(buf);

        assertTrue(decoded.getAllocatedPerks().isEmpty());
        assertEquals(0L, decoded.getPerkExp());
        assertEquals(0, decoded.getAvailablePoints());

        buf.release();
    }

    @Test
    void testPktSyncPerkDataLargeSet() {
        Set<ResourceLocation> perks = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            perks.add(new ResourceLocation("astralsorcery", "perk.node_" + i));
        }

        PktSyncPerkData original = new PktSyncPerkData(perks, Long.MAX_VALUE - 1, 41);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncPerkData.encode(original, buf);
        PktSyncPerkData decoded = PktSyncPerkData.decode(buf);

        assertEquals(50, decoded.getAllocatedPerks().size());
        assertEquals(Long.MAX_VALUE - 1, decoded.getPerkExp());
        assertEquals(41, decoded.getAvailablePoints());
        assertTrue(decoded.getAllocatedPerks().containsAll(perks));

        buf.release();
    }

    // ========================================================================
    // PktDiscoveryUpdate roundtrip
    // ========================================================================

    @Test
    void testPktDiscoveryUpdateRoundtrip() {
        ResourceLocation constellation = new ResourceLocation("astralsorcery", "discidia");
        PktDiscoveryUpdate original = new PktDiscoveryUpdate(constellation, true);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktDiscoveryUpdate.encode(original, buf);
        PktDiscoveryUpdate decoded = PktDiscoveryUpdate.decode(buf);

        assertEquals(constellation, decoded.getConstellationKey());
        assertTrue(decoded.shouldShowAnimation());

        buf.release();
    }

    @Test
    void testPktDiscoveryUpdateNoAnimation() {
        ResourceLocation constellation = new ResourceLocation("astralsorcery", "armara");
        PktDiscoveryUpdate original = new PktDiscoveryUpdate(constellation, false);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktDiscoveryUpdate.encode(original, buf);
        PktDiscoveryUpdate decoded = PktDiscoveryUpdate.decode(buf);

        assertEquals(constellation, decoded.getConstellationKey());
        assertFalse(decoded.shouldShowAnimation());

        buf.release();
    }

    // ========================================================================
    // PktAltarCraftingUpdate roundtrip
    // ========================================================================

    @Test
    void testPktAltarCraftingUpdateStarted() {
        BlockPos pos = new BlockPos(123, 80, -456);
        ResourceLocation recipe = new ResourceLocation("astralsorcery", "altar/starlight_wand");
        PktAltarCraftingUpdate original = new PktAltarCraftingUpdate(
                pos, PktAltarCraftingUpdate.CraftState.STARTED, recipe, 2.0f);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktAltarCraftingUpdate.encode(original, buf);
        PktAltarCraftingUpdate decoded = PktAltarCraftingUpdate.decode(buf);

        assertEquals(pos, decoded.getAltarPos());
        assertEquals(PktAltarCraftingUpdate.CraftState.STARTED, decoded.getState());
        assertEquals(recipe, decoded.getRecipeId());
        assertEquals(2.0f, decoded.getProgress(), 0.001f);

        buf.release();
    }

    @Test
    void testPktAltarCraftingUpdateProgress() {
        BlockPos pos = new BlockPos(0, 64, 0);
        ResourceLocation recipe = new ResourceLocation("astralsorcery", "altar/illumination_powder");
        PktAltarCraftingUpdate original = new PktAltarCraftingUpdate(
                pos, PktAltarCraftingUpdate.CraftState.PROGRESS, recipe, 0.73f);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktAltarCraftingUpdate.encode(original, buf);
        PktAltarCraftingUpdate decoded = PktAltarCraftingUpdate.decode(buf);

        assertEquals(PktAltarCraftingUpdate.CraftState.PROGRESS, decoded.getState());
        assertEquals(0.73f, decoded.getProgress(), 0.001f);

        buf.release();
    }

    @Test
    void testPktAltarCraftingUpdateAllStates() {
        for (PktAltarCraftingUpdate.CraftState state : PktAltarCraftingUpdate.CraftState.values()) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            PktAltarCraftingUpdate msg = new PktAltarCraftingUpdate(
                    BlockPos.ZERO, state,
                    new ResourceLocation("astralsorcery", "test"),
                    0.5f);
            PktAltarCraftingUpdate.encode(msg, buf);
            PktAltarCraftingUpdate decoded = PktAltarCraftingUpdate.decode(buf);
            assertEquals(state, decoded.getState(),
                    "CraftState " + state.name() + " failed roundtrip");
            buf.release();
        }
    }

    // ========================================================================
    // PktSyncStarlightCharge roundtrip
    // ========================================================================

    @Test
    void testPktSyncStarlightChargeRoundtrip() {
        PktSyncStarlightCharge original = new PktSyncStarlightCharge(0.75f, 100.0f, true);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncStarlightCharge.encode(original, buf);
        PktSyncStarlightCharge decoded = PktSyncStarlightCharge.decode(buf);

        assertEquals(0.75f, decoded.getChargeLevel(), 0.001f);
        assertEquals(100.0f, decoded.getMaxCharge(), 0.001f);
        assertTrue(decoded.isCharging());

        buf.release();
    }

    @Test
    void testPktSyncStarlightChargeZero() {
        PktSyncStarlightCharge original = new PktSyncStarlightCharge(0.0f, 50.0f, false);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncStarlightCharge.encode(original, buf);
        PktSyncStarlightCharge decoded = PktSyncStarlightCharge.decode(buf);

        assertEquals(0.0f, decoded.getChargeLevel(), 0.001f);
        assertEquals(50.0f, decoded.getMaxCharge(), 0.001f);
        assertFalse(decoded.isCharging());

        buf.release();
    }

    @Test
    void testPktSyncStarlightChargeFull() {
        PktSyncStarlightCharge original = new PktSyncStarlightCharge(1.0f, 200.0f, false);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncStarlightCharge.encode(original, buf);
        PktSyncStarlightCharge decoded = PktSyncStarlightCharge.decode(buf);

        assertEquals(1.0f, decoded.getChargeLevel(), 0.001f);
        assertEquals(200.0f, decoded.getMaxCharge(), 0.001f);
        assertFalse(decoded.isCharging());

        buf.release();
    }

    // ========================================================================
    // PktPerkAllocate roundtrip (Client -> Server)
    // ========================================================================

    @Test
    void testPktPerkAllocateRoundtrip() {
        ResourceLocation perkKey = new ResourceLocation("astralsorcery", "perk.root.discidia");
        PktPerkAllocate original = new PktPerkAllocate(perkKey);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktPerkAllocate.encode(original, buf);
        PktPerkAllocate decoded = PktPerkAllocate.decode(buf);

        // Access field via decode result — the key should match
        FriendlyByteBuf verifyBuf = new FriendlyByteBuf(Unpooled.buffer());
        PktPerkAllocate.encode(decoded, verifyBuf);
        // Re-decode to compare
        PktPerkAllocate reDecoded = PktPerkAllocate.decode(verifyBuf);

        FriendlyByteBuf finalBuf = new FriendlyByteBuf(Unpooled.buffer());
        PktPerkAllocate.encode(original, finalBuf);
        FriendlyByteBuf finalBuf2 = new FriendlyByteBuf(Unpooled.buffer());
        PktPerkAllocate.encode(reDecoded, finalBuf2);

        // Byte-level comparison of serialized forms
        assertEquals(finalBuf.readableBytes(), finalBuf2.readableBytes());
        while (finalBuf.isReadable()) {
            assertEquals(finalBuf.readByte(), finalBuf2.readByte());
        }

        finalBuf.release();
        finalBuf2.release();
        buf.release();
        verifyBuf.release();
    }

    // ========================================================================
    // PktPerkDeallocate roundtrip (Client -> Server)
    // ========================================================================

    @Test
    void testPktPerkDeallocateRoundtrip() {
        ResourceLocation perkKey = new ResourceLocation("astralsorcery", "perk.major.health_3");
        PktPerkDeallocate original = new PktPerkDeallocate(perkKey);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktPerkDeallocate.encode(original, buf);
        PktPerkDeallocate decoded = PktPerkDeallocate.decode(buf);

        // Roundtrip byte-level verification
        FriendlyByteBuf buf1 = new FriendlyByteBuf(Unpooled.buffer());
        FriendlyByteBuf buf2 = new FriendlyByteBuf(Unpooled.buffer());
        PktPerkDeallocate.encode(original, buf1);
        PktPerkDeallocate.encode(decoded, buf2);

        assertEquals(buf1.readableBytes(), buf2.readableBytes());
        while (buf1.isReadable()) {
            assertEquals(buf1.readByte(), buf2.readByte());
        }

        buf.release();
        buf1.release();
        buf2.release();
    }

    // ========================================================================
    // PktGatewayTeleport roundtrip (Client -> Server)
    // ========================================================================

    @Test
    void testPktGatewayTeleportRoundtrip() {
        BlockPos target = new BlockPos(5000, 128, -8000);
        String dim = "minecraft:overworld";
        PktGatewayTeleport original = new PktGatewayTeleport(target, dim);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktGatewayTeleport.encode(original, buf);
        PktGatewayTeleport decoded = PktGatewayTeleport.decode(buf);

        // Byte-level comparison
        FriendlyByteBuf buf1 = new FriendlyByteBuf(Unpooled.buffer());
        FriendlyByteBuf buf2 = new FriendlyByteBuf(Unpooled.buffer());
        PktGatewayTeleport.encode(original, buf1);
        PktGatewayTeleport.encode(decoded, buf2);

        assertEquals(buf1.readableBytes(), buf2.readableBytes());
        while (buf1.isReadable()) {
            assertEquals(buf1.readByte(), buf2.readByte());
        }

        buf.release();
        buf1.release();
        buf2.release();
    }

    @Test
    void testPktGatewayTeleportNetherDimension() {
        BlockPos target = new BlockPos(100, 40, 100);
        String dim = "minecraft:the_nether";
        PktGatewayTeleport original = new PktGatewayTeleport(target, dim);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktGatewayTeleport.encode(original, buf);
        PktGatewayTeleport decoded = PktGatewayTeleport.decode(buf);

        FriendlyByteBuf buf1 = new FriendlyByteBuf(Unpooled.buffer());
        FriendlyByteBuf buf2 = new FriendlyByteBuf(Unpooled.buffer());
        PktGatewayTeleport.encode(original, buf1);
        PktGatewayTeleport.encode(decoded, buf2);

        assertEquals(buf1.readableBytes(), buf2.readableBytes());

        buf.release();
        buf1.release();
        buf2.release();
    }

    @Test
    void testPktGatewayTeleportModdedDimension() {
        BlockPos target = new BlockPos(-1000, 200, 3000);
        String dim = "astralsorcery:starfield";
        PktGatewayTeleport original = new PktGatewayTeleport(target, dim);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktGatewayTeleport.encode(original, buf);
        PktGatewayTeleport decoded = PktGatewayTeleport.decode(buf);

        FriendlyByteBuf buf1 = new FriendlyByteBuf(Unpooled.buffer());
        FriendlyByteBuf buf2 = new FriendlyByteBuf(Unpooled.buffer());
        PktGatewayTeleport.encode(original, buf1);
        PktGatewayTeleport.encode(decoded, buf2);

        assertEquals(buf1.readableBytes(), buf2.readableBytes());
        while (buf1.isReadable()) {
            assertEquals(buf1.readByte(), buf2.readByte());
        }

        buf.release();
        buf1.release();
        buf2.release();
    }

    // ========================================================================
    // PktSyncConstellation roundtrip
    // ========================================================================

    @Test
    void testPktSyncConstellationDiscovered() {
        ResourceLocation constellation = new ResourceLocation("astralsorcery", "discidia");
        PktSyncConstellation original = new PktSyncConstellation(constellation, true);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncConstellation.encode(original, buf);
        PktSyncConstellation decoded = PktSyncConstellation.decode(buf);

        assertEquals(constellation, decoded.getConstellation());
        assertTrue(decoded.isDiscovered());

        buf.release();
    }

    @Test
    void testPktSyncConstellationRemoved() {
        ResourceLocation constellation = new ResourceLocation("astralsorcery", "vicio");
        PktSyncConstellation original = new PktSyncConstellation(constellation, false);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncConstellation.encode(original, buf);
        PktSyncConstellation decoded = PktSyncConstellation.decode(buf);

        assertEquals(constellation, decoded.getConstellation());
        assertFalse(decoded.isDiscovered());

        buf.release();
    }

    // ========================================================================
    // PktSyncStarlightNetwork roundtrip
    // ========================================================================

    @Test
    void testPktSyncStarlightNetworkRoundtrip() {
        BlockPos source = new BlockPos(100, 64, -200);
        List<BlockPos> targets = new ArrayList<>();
        targets.add(new BlockPos(105, 64, -200));
        targets.add(new BlockPos(110, 64, -200));
        targets.add(new BlockPos(100, 64, -205));

        PktSyncStarlightNetwork original = new PktSyncStarlightNetwork(source, targets, 0.85);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncStarlightNetwork.encode(original, buf);
        PktSyncStarlightNetwork decoded = PktSyncStarlightNetwork.decode(buf);

        assertEquals(source, decoded.getSource());
        assertEquals(3, decoded.getConnectedTargets().size());
        assertEquals(targets, decoded.getConnectedTargets());
        assertEquals(0.85, decoded.getStarlight(), 0.001);

        buf.release();
    }

    @Test
    void testPktSyncStarlightNetworkEmpty() {
        BlockPos source = new BlockPos(0, 0, 0);
        PktSyncStarlightNetwork original = new PktSyncStarlightNetwork(source, List.of(), 0.0);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncStarlightNetwork.encode(original, buf);
        PktSyncStarlightNetwork decoded = PktSyncStarlightNetwork.decode(buf);

        assertEquals(source, decoded.getSource());
        assertTrue(decoded.getConnectedTargets().isEmpty());
        assertEquals(0.0, decoded.getStarlight(), 0.001);

        buf.release();
    }

    @Test
    void testPktSyncStarlightNetworkManyTargets() {
        BlockPos source = new BlockPos(500, 80, 500);
        List<BlockPos> targets = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            targets.add(new BlockPos(500 + i * 5, 80, 500));
        }

        PktSyncStarlightNetwork original = new PktSyncStarlightNetwork(
                source, targets, Double.MAX_VALUE);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktSyncStarlightNetwork.encode(original, buf);
        PktSyncStarlightNetwork decoded = PktSyncStarlightNetwork.decode(buf);

        assertEquals(20, decoded.getConnectedTargets().size());
        assertEquals(targets.get(0), decoded.getConnectedTargets().get(0));
        assertEquals(targets.get(19), decoded.getConnectedTargets().get(19));
        assertEquals(Double.MAX_VALUE, decoded.getStarlight(), 0.001);

        buf.release();
    }

    // ========================================================================
    // PktRequestProgress roundtrip (signal packet)
    // ========================================================================

    @Test
    void testPktRequestProgressRoundtrip() {
        PktRequestProgress original = new PktRequestProgress();

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktRequestProgress.encode(original, buf);

        // Signal packet writes nothing — buffer should be empty
        assertEquals(0, buf.readableBytes(),
                "Signal packet should write zero bytes");

        PktRequestProgress decoded = PktRequestProgress.decode(buf);
        assertNotNull(decoded);

        buf.release();
    }

    // ========================================================================
    // PktAttunePlayer roundtrip (Client -> Server)
    // ========================================================================

    @Test
    void testPktAttunePlayerRoundtrip() {
        ResourceLocation constellation = new ResourceLocation("astralsorcery", "evorsio");
        PktAttunePlayer original = new PktAttunePlayer(constellation);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        PktAttunePlayer.encode(original, buf);
        PktAttunePlayer decoded = PktAttunePlayer.decode(buf);

        assertEquals(constellation, decoded.getConstellation());

        buf.release();
    }

    @Test
    void testPktAttunePlayerDifferentConstellations() {
        String[] constellations = {
                "discidia", "armara", "vicio", "aevitas", "evorsio",
                "lucerna", "mineralis", "horologium", "octans",
                "bootes", "fornax", "pelotrio"
        };

        for (String name : constellations) {
            ResourceLocation key = new ResourceLocation("astralsorcery", name);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            PktAttunePlayer msg = new PktAttunePlayer(key);
            PktAttunePlayer.encode(msg, buf);
            PktAttunePlayer decoded = PktAttunePlayer.decode(buf);
            assertEquals(key, decoded.getConstellation(),
                    "Constellation " + name + " failed roundtrip");
            buf.release();
        }
    }
}
