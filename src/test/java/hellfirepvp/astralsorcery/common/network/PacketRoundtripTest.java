/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.network;

import hellfirepvp.astralsorcery.common.network.play.server.PktParticleEvent;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

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
}
