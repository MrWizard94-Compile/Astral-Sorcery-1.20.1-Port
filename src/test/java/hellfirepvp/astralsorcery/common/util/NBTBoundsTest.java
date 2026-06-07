/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.util;

import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import hellfirepvp.astralsorcery.common.crafting.recipe.ActiveSimpleAltarRecipe.CraftState;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for the bounds checks added to {@link NBTHelper#readEnum} and
 * {@link ByteBufUtils#readEnumValue}.
 *
 * <p>Background: corrupt or mismatched save data can produce ordinal values that fall
 * outside the range {@code [0, constants.length)}. Before the fix, both methods would
 * throw {@link ArrayIndexOutOfBoundsException}. After the fix they degrade gracefully
 * to {@code constants[0]}. These tests lock that contract in.</p>
 */
class NBTBoundsTest {

    // =========================================================================
    // NBTHelper.readEnum() — CompoundTag deserialization
    // =========================================================================

    @Test
    void readEnum_validOrdinal_returnsCorrectConstant() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("type", BlockAltar.AltarType.ATTUNEMENT.ordinal()); // ordinal 1
        assertEquals(BlockAltar.AltarType.ATTUNEMENT,
                NBTHelper.readEnum(tag, "type", BlockAltar.AltarType.class));
    }

    @Test
    void readEnum_firstOrdinal_returnsDiscovery() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("type", 0);
        assertEquals(BlockAltar.AltarType.DISCOVERY,
                NBTHelper.readEnum(tag, "type", BlockAltar.AltarType.class));
    }

    @Test
    void readEnum_lastOrdinal_returnsRadiance() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("type", BlockAltar.AltarType.RADIANCE.ordinal()); // ordinal 3
        assertEquals(BlockAltar.AltarType.RADIANCE,
                NBTHelper.readEnum(tag, "type", BlockAltar.AltarType.class));
    }

    @Test
    void readEnum_missingKey_fallsBackToFirstConstant() {
        // CompoundTag.getInt returns 0 for a missing key.
        // Ordinal 0 maps to the first constant — this is also safe.
        CompoundTag tag = new CompoundTag();
        BlockAltar.AltarType result = NBTHelper.readEnum(tag, "type", BlockAltar.AltarType.class);
        assertNotNull(result);
        // Ordinal 0 → DISCOVERY
        assertEquals(BlockAltar.AltarType.DISCOVERY, result);
    }

    @Test
    void readEnum_negativeOrdinal_fallsBackToFirstConstant() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("type", -1);
        // Before fix: ArrayIndexOutOfBoundsException. After fix: DISCOVERY.
        assertEquals(BlockAltar.AltarType.DISCOVERY,
                NBTHelper.readEnum(tag, "type", BlockAltar.AltarType.class));
    }

    @Test
    void readEnum_outOfRangeOrdinal_fallsBackToFirstConstant() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("type", 999);
        assertEquals(BlockAltar.AltarType.DISCOVERY,
                NBTHelper.readEnum(tag, "type", BlockAltar.AltarType.class));
    }

    @Test
    void readEnum_exactlyAtLength_fallsBackToFirstConstant() {
        // AltarType has 4 values (ordinals 0-3); ordinal 4 is exactly out of range.
        CompoundTag tag = new CompoundTag();
        tag.putInt("type", BlockAltar.AltarType.values().length);
        assertEquals(BlockAltar.AltarType.DISCOVERY,
                NBTHelper.readEnum(tag, "type", BlockAltar.AltarType.class));
    }

    @Test
    void readEnum_integerMaxValue_fallsBackToFirstConstant() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("type", Integer.MAX_VALUE);
        assertEquals(BlockAltar.AltarType.DISCOVERY,
                NBTHelper.readEnum(tag, "type", BlockAltar.AltarType.class));
    }

    @Test
    void readEnum_writeReadRoundTrip_allValues() {
        // Verify write-then-read is lossless for every valid ordinal.
        for (BlockAltar.AltarType expected : BlockAltar.AltarType.values()) {
            CompoundTag tag = new CompoundTag();
            NBTHelper.writeEnum(tag, "type", expected);
            BlockAltar.AltarType actual = NBTHelper.readEnum(tag, "type", BlockAltar.AltarType.class);
            assertEquals(expected, actual, "Round-trip failed for " + expected);
        }
    }

    @Test
    void readEnum_threeValueEnum_negativeOrdinal_fallsBackToActive() {
        // Also test with CraftState (3 values: ACTIVE, COMPLETED, ABORTED)
        CompoundTag tag = new CompoundTag();
        tag.putInt("state", -1);
        assertEquals(CraftState.ACTIVE,
                NBTHelper.readEnum(tag, "state", CraftState.class));
    }

    @Test
    void readEnum_threeValueEnum_outOfRangeOrdinal_fallsBackToActive() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("state", 100);
        assertEquals(CraftState.ACTIVE,
                NBTHelper.readEnum(tag, "state", CraftState.class));
    }

    @Test
    void readEnum_threeValueEnum_writeReadRoundTrip() {
        for (CraftState expected : CraftState.values()) {
            CompoundTag tag = new CompoundTag();
            NBTHelper.writeEnum(tag, "state", expected);
            CraftState actual = NBTHelper.readEnum(tag, "state", CraftState.class);
            assertEquals(expected, actual, "Round-trip failed for " + expected);
        }
    }

    // =========================================================================
    // ByteBufUtils.readEnumValue() — network packet deserialization
    // =========================================================================

    @Test
    void readEnumValue_validOrdinal_returnsCorrectConstant() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(BlockAltar.AltarType.CONSTELLATION.ordinal()); // ordinal 2
        assertEquals(BlockAltar.AltarType.CONSTELLATION,
                ByteBufUtils.readEnumValue(buf, BlockAltar.AltarType.class));
        buf.release();
    }

    @Test
    void readEnumValue_negativeOrdinal_fallsBackToFirstConstant() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(-1);
        // Before fix: ArrayIndexOutOfBoundsException. After fix: DISCOVERY.
        assertEquals(BlockAltar.AltarType.DISCOVERY,
                ByteBufUtils.readEnumValue(buf, BlockAltar.AltarType.class));
        buf.release();
    }

    @Test
    void readEnumValue_outOfRangeOrdinal_fallsBackToFirstConstant() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(999);
        assertEquals(BlockAltar.AltarType.DISCOVERY,
                ByteBufUtils.readEnumValue(buf, BlockAltar.AltarType.class));
        buf.release();
    }

    @Test
    void readEnumValue_exactlyAtLength_fallsBackToFirstConstant() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(BlockAltar.AltarType.values().length); // 4, out of range for 4-value enum
        assertEquals(BlockAltar.AltarType.DISCOVERY,
                ByteBufUtils.readEnumValue(buf, BlockAltar.AltarType.class));
        buf.release();
    }

    @Test
    void readEnumValue_integerMinValue_fallsBackToFirstConstant() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(Integer.MIN_VALUE);
        assertEquals(BlockAltar.AltarType.DISCOVERY,
                ByteBufUtils.readEnumValue(buf, BlockAltar.AltarType.class));
        buf.release();
    }

    @Test
    void readEnumValue_writeReadRoundTrip_allValues() {
        // Verify ByteBufUtils.writeEnumValue → readEnumValue is lossless for every valid ordinal.
        for (BlockAltar.AltarType expected : BlockAltar.AltarType.values()) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            ByteBufUtils.writeEnumValue(buf, expected);
            BlockAltar.AltarType actual = ByteBufUtils.readEnumValue(buf, BlockAltar.AltarType.class);
            assertEquals(expected, actual, "Round-trip failed for " + expected);
            buf.release();
        }
    }

    @Test
    void readEnumValue_craftState_negativeOrdinal_fallsBackToActive() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(-5);
        assertEquals(CraftState.ACTIVE,
                ByteBufUtils.readEnumValue(buf, CraftState.class));
        buf.release();
    }

    @Test
    void readEnumValue_craftState_writeReadRoundTrip() {
        for (CraftState expected : CraftState.values()) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            ByteBufUtils.writeEnumValue(buf, expected);
            CraftState actual = ByteBufUtils.readEnumValue(buf, CraftState.class);
            assertEquals(expected, actual, "Round-trip failed for " + expected);
            buf.release();
        }
    }
}
