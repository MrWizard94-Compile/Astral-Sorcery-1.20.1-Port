/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for recipe-adjacent serialization utilities.
 * Verifies that basic encode/decode patterns used by recipe
 * serializers are lossless. FluidStack/Ingredient tests are
 * deferred to integration testing since they require MC bootstrap.
 */
class RecipeSerializationTest {

    @Test
    void testVarIntEncoding() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeVarInt(0);
        buf.writeVarInt(127);
        buf.writeVarInt(128);
        buf.writeVarInt(Integer.MAX_VALUE);

        assertEquals(0, buf.readVarInt());
        assertEquals(127, buf.readVarInt());
        assertEquals(128, buf.readVarInt());
        assertEquals(Integer.MAX_VALUE, buf.readVarInt());

        buf.release();
    }

    @Test
    void testFloatEncoding() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeFloat(0.0f);
        buf.writeFloat(1.5f);
        buf.writeFloat(-100.25f);
        buf.writeFloat(Float.MAX_VALUE);

        assertEquals(0.0f, buf.readFloat(), 0.0001f);
        assertEquals(1.5f, buf.readFloat(), 0.0001f);
        assertEquals(-100.25f, buf.readFloat(), 0.0001f);
        assertEquals(Float.MAX_VALUE, buf.readFloat(), 0.0001f);

        buf.release();
    }

    @Test
    void testResourceLocationEncoding() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ResourceLocation original = new ResourceLocation("astralsorcery", "test/recipe");

        buf.writeResourceLocation(original);
        ResourceLocation decoded = buf.readResourceLocation();

        assertEquals(original, decoded);
        buf.release();
    }

    @Test
    void testIntColorEncoding() {
        // Recipes store color as raw int — verify it roundtrips
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        int[] colors = { 0x4466FF, 0xFF0000, 0x00FF00, 0x000000, 0xFFFFFF, 0x80808080 };
        for (int color : colors) {
            buf.writeInt(color);
        }

        for (int color : colors) {
            assertEquals(color, buf.readInt(), "Color 0x" + Integer.toHexString(color) + " failed");
        }

        buf.release();
    }

    @Test
    void testProductionMultiplierEncoding() {
        // Recipe production multiplier is a float
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        float[] multipliers = { 0.1f, 0.5f, 1.0f, 2.5f, 10.0f };
        for (float m : multipliers) {
            buf.writeFloat(m);
        }

        for (float m : multipliers) {
            assertEquals(m, buf.readFloat(), 0.0001f);
        }

        buf.release();
    }

    @Test
    void testMixedRecipeFieldEncoding() {
        // Simulate writing all non-Ingredient fields of a recipe
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        // Simulate WellLiquefaction fields (minus Ingredient and FluidStack)
        ResourceLocation recipeId = new ResourceLocation("astralsorcery", "well/rock_crystal");
        float multiplier = 1.5f;
        int color = 0x4466FF;

        buf.writeResourceLocation(recipeId);
        buf.writeFloat(multiplier);
        buf.writeInt(color);

        assertEquals(recipeId, buf.readResourceLocation());
        assertEquals(multiplier, buf.readFloat(), 0.0001f);
        assertEquals(color, buf.readInt());

        buf.release();
    }
}
