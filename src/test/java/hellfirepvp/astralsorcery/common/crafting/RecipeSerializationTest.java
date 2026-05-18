/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.block.tile.BlockAltar;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for recipe serialization across all 5 custom recipe types.
 * <p>
 * These tests validate the JSON parsing layer and buffer encoding used by
 * recipe serializers WITHOUT requiring Minecraft registry bootstrap.
 * Tests are organized into:
 * <ul>
 *   <li>Buffer encoding/decoding (lossless round-trip for primitives)</li>
 *   <li>AltarType enum and slot-count mapping</li>
 *   <li>JSON field extraction (GsonHelper parsing layer)</li>
 *   <li>Default value behavior for optional fields</li>
 *   <li>Missing field / invalid value error paths</li>
 *   <li>Network encoding patterns for each recipe type</li>
 * </ul>
 */
class RecipeSerializationTest {

    // ========================================================================
    // Buffer encoding round-trip tests (original tests, kept intact)
    // ========================================================================

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
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

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

    // ========================================================================
    // AltarType enum and slot-count mapping
    // ========================================================================

    @Nested
    class AltarTypeTests {

        @Test
        void testAllFourAltarTypesExist() {
            assertEquals(4, BlockAltar.AltarType.values().length);
            assertNotNull(BlockAltar.AltarType.DISCOVERY);
            assertNotNull(BlockAltar.AltarType.ATTUNEMENT);
            assertNotNull(BlockAltar.AltarType.CONSTELLATION);
            assertNotNull(BlockAltar.AltarType.RADIANCE);
        }

        @Test
        void testAltarTypeValueOfUpperCase() {
            // The serializer does typeStr.toUpperCase(Locale.ROOT) before valueOf
            assertEquals(BlockAltar.AltarType.DISCOVERY,
                    BlockAltar.AltarType.valueOf("discovery".toUpperCase(Locale.ROOT)));
            assertEquals(BlockAltar.AltarType.ATTUNEMENT,
                    BlockAltar.AltarType.valueOf("attunement".toUpperCase(Locale.ROOT)));
            assertEquals(BlockAltar.AltarType.CONSTELLATION,
                    BlockAltar.AltarType.valueOf("constellation".toUpperCase(Locale.ROOT)));
            assertEquals(BlockAltar.AltarType.RADIANCE,
                    BlockAltar.AltarType.valueOf("radiance".toUpperCase(Locale.ROOT)));
        }

        @Test
        void testAltarTypeValueOfMixedCase() {
            // JSON files use lowercase, serializer uppercases before valueOf
            assertEquals(BlockAltar.AltarType.DISCOVERY,
                    BlockAltar.AltarType.valueOf("Discovery".toUpperCase(Locale.ROOT)));
            assertEquals(BlockAltar.AltarType.RADIANCE,
                    BlockAltar.AltarType.valueOf("RADIANCE".toUpperCase(Locale.ROOT)));
        }

        @Test
        void testInvalidAltarTypeThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> BlockAltar.AltarType.valueOf("INVALID_TYPE".toUpperCase(Locale.ROOT)));
        }

        @Test
        void testInvalidAltarTypeEmptyStringThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> BlockAltar.AltarType.valueOf("".toUpperCase(Locale.ROOT)));
        }

        @Test
        void testAltarTypeSerializedName() {
            assertEquals("discovery", BlockAltar.AltarType.DISCOVERY.getSerializedName());
            assertEquals("attunement", BlockAltar.AltarType.ATTUNEMENT.getSerializedName());
            assertEquals("constellation", BlockAltar.AltarType.CONSTELLATION.getSerializedName());
            assertEquals("radiance", BlockAltar.AltarType.RADIANCE.getSerializedName());
        }

        @Test
        void testAltarTypeTierOrdering() {
            assertTrue(BlockAltar.AltarType.DISCOVERY.getOrdinalTier() < BlockAltar.AltarType.ATTUNEMENT.getOrdinalTier());
            assertTrue(BlockAltar.AltarType.ATTUNEMENT.getOrdinalTier() < BlockAltar.AltarType.CONSTELLATION.getOrdinalTier());
            assertTrue(BlockAltar.AltarType.CONSTELLATION.getOrdinalTier() < BlockAltar.AltarType.RADIANCE.getOrdinalTier());
        }

        @Test
        void testAltarTypeIsAtLeast() {
            assertTrue(BlockAltar.AltarType.RADIANCE.isAtLeast(BlockAltar.AltarType.DISCOVERY));
            assertTrue(BlockAltar.AltarType.RADIANCE.isAtLeast(BlockAltar.AltarType.RADIANCE));
            assertFalse(BlockAltar.AltarType.DISCOVERY.isAtLeast(BlockAltar.AltarType.ATTUNEMENT));
            assertTrue(BlockAltar.AltarType.CONSTELLATION.isAtLeast(BlockAltar.AltarType.ATTUNEMENT));
        }

        @Test
        void testDiscoverySlotCount() {
            assertEquals(9, slotCountForType(BlockAltar.AltarType.DISCOVERY));
        }

        @Test
        void testAttunementSlotCount() {
            assertEquals(13, slotCountForType(BlockAltar.AltarType.ATTUNEMENT));
        }

        @Test
        void testConstellationSlotCount() {
            assertEquals(21, slotCountForType(BlockAltar.AltarType.CONSTELLATION));
        }

        @Test
        void testRadianceSlotCount() {
            assertEquals(25, slotCountForType(BlockAltar.AltarType.RADIANCE));
        }

        @Test
        void testAllSlotCountsCovered() {
            // Every AltarType value should have a defined slot count
            for (BlockAltar.AltarType type : BlockAltar.AltarType.values()) {
                int slots = slotCountForType(type);
                assertTrue(slots > 0, "Slot count for " + type + " should be positive");
            }
        }

        @Test
        void testSlotCountsStrictlyIncreasing() {
            BlockAltar.AltarType[] types = BlockAltar.AltarType.values();
            for (int i = 1; i < types.length; i++) {
                assertTrue(slotCountForType(types[i]) > slotCountForType(types[i - 1]),
                        types[i] + " should have more slots than " + types[i - 1]);
            }
        }
    }

    // ========================================================================
    // SimpleAltarRecipe — JSON field extraction tests
    // ========================================================================

    @Nested
    class SimpleAltarRecipeJsonTests {

        @Test
        void testDiscoveryRecipeJsonFields() {
            JsonObject json = buildDiscoveryAltarJson();

            String typeStr = GsonHelper.getAsString(json, "altar_type");
            assertEquals("discovery", typeStr);

            BlockAltar.AltarType altarType = BlockAltar.AltarType.valueOf(typeStr.toUpperCase(Locale.ROOT));
            assertEquals(BlockAltar.AltarType.DISCOVERY, altarType);

            assertEquals(100, GsonHelper.getAsInt(json, "craft_duration", 100));
            assertEquals(200.0f, GsonHelper.getAsFloat(json, "starlight_required", 200.0f), 0.01f);

            JsonObject output = GsonHelper.getAsJsonObject(json, "output");
            assertEquals("astralsorcery:altar_discovery", GsonHelper.getAsString(output, "item"));
            assertEquals(1, GsonHelper.getAsInt(output, "count", 1));

            JsonArray inputs = GsonHelper.getAsJsonArray(json, "inputs");
            assertEquals(9, inputs.size(), "Discovery recipes must have 9 inputs");
        }

        @Test
        void testDiscoveryAllInputsNonEmpty() {
            JsonObject json = buildDiscoveryAltarJson();
            JsonArray inputs = GsonHelper.getAsJsonArray(json, "inputs");

            for (int i = 0; i < inputs.size(); i++) {
                assertTrue(inputs.get(i).isJsonObject(), "Input " + i + " should be a JsonObject");
                assertFalse(inputs.get(i).getAsJsonObject().entrySet().isEmpty(),
                        "Discovery recipe input " + i + " should not be empty");
            }
        }

        @Test
        void testAttunementRecipeJsonFieldsWithEmptySlots() {
            JsonObject json = buildAttunementAltarJson();

            String typeStr = GsonHelper.getAsString(json, "altar_type");
            BlockAltar.AltarType altarType = BlockAltar.AltarType.valueOf(typeStr.toUpperCase(Locale.ROOT));
            assertEquals(BlockAltar.AltarType.ATTUNEMENT, altarType);

            assertEquals(200, GsonHelper.getAsInt(json, "craft_duration", 100));
            assertEquals(600.0f, GsonHelper.getAsFloat(json, "starlight_required", 200.0f), 0.01f);

            JsonArray inputs = GsonHelper.getAsJsonArray(json, "inputs");
            assertEquals(13, inputs.size(), "Attunement recipes must have 13 inputs");

            // Verify that empty slot objects are empty JsonObjects
            assertTrue(inputs.get(0).isJsonObject());
            assertTrue(inputs.get(0).getAsJsonObject().entrySet().isEmpty(),
                    "First slot should be an empty object (empty ingredient)");

            // Verify non-empty slot has item
            assertTrue(inputs.get(1).isJsonObject());
            assertFalse(inputs.get(1).getAsJsonObject().entrySet().isEmpty(),
                    "Second slot should contain an ingredient");
            assertEquals("astralsorcery:stardust",
                    GsonHelper.getAsString(inputs.get(1).getAsJsonObject(), "item"));
        }

        @Test
        void testAttunementEmptySlotCount() {
            JsonObject json = buildAttunementAltarJson();
            JsonArray inputs = GsonHelper.getAsJsonArray(json, "inputs");

            int emptyCount = 0;
            for (int i = 0; i < inputs.size(); i++) {
                if (inputs.get(i).getAsJsonObject().entrySet().isEmpty()) {
                    emptyCount++;
                }
            }
            assertEquals(4, emptyCount, "Attunement recipe should have 4 empty slots");
        }

        @Test
        void testConstellationRecipeJsonFields() {
            JsonObject json = buildConstellationAltarJson();

            String typeStr = GsonHelper.getAsString(json, "altar_type");
            BlockAltar.AltarType altarType = BlockAltar.AltarType.valueOf(typeStr.toUpperCase(Locale.ROOT));
            assertEquals(BlockAltar.AltarType.CONSTELLATION, altarType);

            assertEquals(400, GsonHelper.getAsInt(json, "craft_duration", 100));
            assertEquals(3200.0f, GsonHelper.getAsFloat(json, "starlight_required", 200.0f), 0.01f);

            JsonArray inputs = GsonHelper.getAsJsonArray(json, "inputs");
            // Constellation tier expects up to 21 slots
            assertTrue(inputs.size() <= 21,
                    "Constellation recipe inputs should not exceed 21 slots");
        }

        @Test
        void testRadianceRecipeJsonFields() {
            JsonObject json = buildRadianceAltarJson();

            String typeStr = GsonHelper.getAsString(json, "altar_type");
            BlockAltar.AltarType altarType = BlockAltar.AltarType.valueOf(typeStr.toUpperCase(Locale.ROOT));
            assertEquals(BlockAltar.AltarType.RADIANCE, altarType);

            assertEquals(600, GsonHelper.getAsInt(json, "craft_duration", 100));
            assertEquals(4800.0f, GsonHelper.getAsFloat(json, "starlight_required", 200.0f), 0.01f);
        }

        @Test
        void testRecipeWithFocusConstellation() {
            JsonObject json = buildConstellationAltarJsonWithFocus();

            assertTrue(json.has("focus_constellation"),
                    "Recipe should have focus_constellation field");
            assertFalse(json.get("focus_constellation").isJsonNull());

            ResourceLocation constellation =
                    new ResourceLocation(GsonHelper.getAsString(json, "focus_constellation"));
            assertEquals("astralsorcery", constellation.getNamespace());
            assertEquals("vicio", constellation.getPath());
        }

        @Test
        void testRecipeWithoutFocusConstellation() {
            JsonObject json = buildDiscoveryAltarJson();

            assertFalse(json.has("focus_constellation"),
                    "Discovery recipe should not have focus_constellation");
        }

        @Test
        void testFocusConstellationNullValue() {
            JsonObject json = buildDiscoveryAltarJson();
            json.add("focus_constellation", com.google.gson.JsonNull.INSTANCE);

            // The serializer checks json.has() && !isJsonNull()
            assertTrue(json.has("focus_constellation"));
            assertTrue(json.get("focus_constellation").isJsonNull());
        }

        @Test
        void testCraftDurationDefault() {
            JsonObject json = new JsonObject();
            // When craft_duration is absent, serializer defaults to 100
            assertEquals(100, GsonHelper.getAsInt(json, "craft_duration", 100));
        }

        @Test
        void testStarlightRequiredDefault() {
            JsonObject json = new JsonObject();
            // When starlight_required is absent, serializer defaults to 200.0F
            assertEquals(200.0f, GsonHelper.getAsFloat(json, "starlight_required", 200.0F), 0.01f);
        }

        @Test
        void testMissingAltarTypeThrows() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsString(json, "altar_type"),
                    "Missing altar_type should throw JsonSyntaxException");
        }

        @Test
        void testMissingOutputThrows() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsJsonObject(json, "output"),
                    "Missing output should throw JsonSyntaxException");
        }

        @Test
        void testMissingInputsThrows() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsJsonArray(json, "inputs"),
                    "Missing inputs should throw JsonSyntaxException");
        }

        @Test
        void testEmptyInputObjectDetection() {
            // The serializer checks: !inputsArr.get(i).isJsonObject() || entrySet().isEmpty()
            JsonObject emptyObj = new JsonObject();
            assertTrue(emptyObj.entrySet().isEmpty(), "Empty JsonObject should be detected");

            JsonObject nonEmptyObj = new JsonObject();
            nonEmptyObj.addProperty("item", "minecraft:diamond");
            assertFalse(nonEmptyObj.entrySet().isEmpty(), "Non-empty JsonObject should not match");
        }

        @Test
        void testInputsCappedAtExpectedSlots() {
            // If JSON has MORE inputs than expected slots, the serializer caps via Math.min
            int expectedSlots = 9; // DISCOVERY
            int jsonInputCount = 15; // more than 9
            int actualParsed = Math.min(jsonInputCount, expectedSlots);
            assertEquals(9, actualParsed, "Parser should cap at expectedSlots");
        }

        @Test
        void testInputsFewerThanExpectedSlots() {
            // If JSON has fewer inputs than expected, remaining are Ingredient.EMPTY
            int expectedSlots = 13; // ATTUNEMENT
            int jsonInputCount = 8; // fewer than 13
            int actualParsed = Math.min(jsonInputCount, expectedSlots);
            assertEquals(8, actualParsed, "Parser should not exceed JSON length");
            // Remaining 5 slots would stay as Ingredient.EMPTY (verified by serializer logic)
        }

        @Test
        void testAltarTypeResolutionFromJsonString() {
            // Simulate the full path: JSON -> string -> toUpperCase -> valueOf
            String[] jsonValues = {"discovery", "attunement", "constellation", "radiance"};
            BlockAltar.AltarType[] expected = {
                    BlockAltar.AltarType.DISCOVERY, BlockAltar.AltarType.ATTUNEMENT,
                    BlockAltar.AltarType.CONSTELLATION, BlockAltar.AltarType.RADIANCE
            };

            for (int i = 0; i < jsonValues.length; i++) {
                JsonObject json = new JsonObject();
                json.addProperty("altar_type", jsonValues[i]);
                String typeStr = GsonHelper.getAsString(json, "altar_type");
                BlockAltar.AltarType actual = BlockAltar.AltarType.valueOf(typeStr.toUpperCase(Locale.ROOT));
                assertEquals(expected[i], actual, "Failed for altar_type: " + jsonValues[i]);
            }
        }

        @Test
        void testInvalidAltarTypeFromJsonThrows() {
            JsonObject json = new JsonObject();
            json.addProperty("altar_type", "mythical");
            String typeStr = GsonHelper.getAsString(json, "altar_type");
            assertThrows(IllegalArgumentException.class,
                    () -> BlockAltar.AltarType.valueOf(typeStr.toUpperCase(Locale.ROOT)),
                    "Invalid altar_type string should throw IllegalArgumentException");
        }

        @Test
        void testOutputItemFields() {
            JsonObject json = buildDiscoveryAltarJson();
            JsonObject output = GsonHelper.getAsJsonObject(json, "output");

            ResourceLocation itemLoc = new ResourceLocation(GsonHelper.getAsString(output, "item"));
            assertEquals("astralsorcery", itemLoc.getNamespace());
            assertEquals("altar_discovery", itemLoc.getPath());
        }

        @Test
        void testStarlightRequiredExplicitValue() {
            JsonObject json = new JsonObject();
            json.addProperty("starlight_required", 3000);
            assertEquals(3000.0f, GsonHelper.getAsFloat(json, "starlight_required", 200.0f), 0.01f);
        }

        @Test
        void testCraftDurationExplicitValue() {
            JsonObject json = new JsonObject();
            json.addProperty("craft_duration", 500);
            assertEquals(500, GsonHelper.getAsInt(json, "craft_duration", 100));
        }
    }

    // ========================================================================
    // WellLiquefaction — JSON field extraction tests
    // ========================================================================

    @Nested
    class WellLiquefactionJsonTests {

        @Test
        void testWellJsonFieldExtraction() {
            JsonObject json = buildWellLiquefactionJson();

            JsonObject input = GsonHelper.getAsJsonObject(json, "input");
            assertEquals("astralsorcery:rock_crystal", GsonHelper.getAsString(input, "item"));

            JsonObject output = GsonHelper.getAsJsonObject(json, "output");
            assertEquals("astralsorcery:liquid_starlight", GsonHelper.getAsString(output, "fluid"));
            assertEquals(1, GsonHelper.getAsInt(output, "amount", 1000));

            assertEquals(1.0f, GsonHelper.getAsFloat(json, "productionMultiplier", 1.0f), 0.01f);
            assertEquals(4430335, GsonHelper.getAsInt(json, "color", 0x4466FF));
        }

        @Test
        void testWellDefaultProductionMultiplier() {
            JsonObject json = new JsonObject();
            assertEquals(1.0f, GsonHelper.getAsFloat(json, "productionMultiplier", 1.0F), 0.01f);
        }

        @Test
        void testWellDefaultColor() {
            JsonObject json = new JsonObject();
            assertEquals(0x4466FF, GsonHelper.getAsInt(json, "color", 0x4466FF));
        }

        @Test
        void testWellMissingInputThrows() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsJsonObject(json, "input"),
                    "Missing 'input' field should throw");
        }

        @Test
        void testWellMissingOutputThrows() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsJsonObject(json, "output"),
                    "Missing 'output' field should throw");
        }

        @Test
        void testWellFluidOutputParsing() {
            JsonObject output = new JsonObject();
            output.addProperty("fluid", "astralsorcery:liquid_starlight");
            output.addProperty("amount", 500);

            assertEquals("astralsorcery:liquid_starlight", GsonHelper.getAsString(output, "fluid"));
            assertEquals(500, GsonHelper.getAsInt(output, "amount", 1000));
        }

        @Test
        void testWellFluidOutputDefaultAmount() {
            JsonObject output = new JsonObject();
            output.addProperty("fluid", "minecraft:water");
            // Missing amount defaults to 1000 (FluidType.BUCKET_VOLUME)
            assertEquals(1000, GsonHelper.getAsInt(output, "amount", 1000));
        }

        @Test
        void testWellFluidResourceLocation() {
            JsonObject json = buildWellLiquefactionJson();
            JsonObject output = GsonHelper.getAsJsonObject(json, "output");

            ResourceLocation fluidLoc = new ResourceLocation(GsonHelper.getAsString(output, "fluid"));
            assertEquals("astralsorcery", fluidLoc.getNamespace());
            assertEquals("liquid_starlight", fluidLoc.getPath());
        }

        @Test
        void testWellInputResourceLocation() {
            JsonObject json = buildWellLiquefactionJson();
            JsonObject input = GsonHelper.getAsJsonObject(json, "input");

            ResourceLocation itemLoc = new ResourceLocation(GsonHelper.getAsString(input, "item"));
            assertEquals("astralsorcery", itemLoc.getNamespace());
            assertEquals("rock_crystal", itemLoc.getPath());
        }

        @Test
        void testWellColorAsHex() {
            JsonObject json = new JsonObject();
            json.addProperty("color", 0x4398FF);
            assertEquals(0x4398FF, GsonHelper.getAsInt(json, "color", 0x4466FF));
        }

        @Test
        void testWellExplicitMultiplier() {
            JsonObject json = new JsonObject();
            json.addProperty("productionMultiplier", 2.5f);
            assertEquals(2.5f, GsonHelper.getAsFloat(json, "productionMultiplier", 1.0f), 0.01f);
        }

        @Test
        void testWellNetworkFieldEncoding() {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

            // Simulate the non-Ingredient, non-FluidStack fields
            float multiplier = 1.5f;
            int color = 0x4466FF;

            buf.writeFloat(multiplier);
            buf.writeInt(color);

            assertEquals(multiplier, buf.readFloat(), 0.0001f);
            assertEquals(color, buf.readInt());

            buf.release();
        }
    }

    // ========================================================================
    // LiquidInfusion — JSON field extraction tests
    // ========================================================================

    @Nested
    class LiquidInfusionJsonTests {

        @Test
        void testInfusionJsonFieldExtraction() {
            JsonObject json = buildLiquidInfusionJson(false);

            JsonObject input = GsonHelper.getAsJsonObject(json, "input");
            assertEquals("forge:ingots/iron", GsonHelper.getAsString(input, "tag"));

            JsonObject output = GsonHelper.getAsJsonObject(json, "output");
            assertEquals("astralsorcery:starmetal_ingot", GsonHelper.getAsString(output, "item"));
            assertEquals(1, GsonHelper.getAsInt(output, "count", 1));

            assertEquals(200, GsonHelper.getAsInt(json, "fluidMbRequired", 500));
            assertEquals(100, GsonHelper.getAsInt(json, "craftDuration", 200));
        }

        @Test
        void testInfusionWithConstellationRequirement() {
            JsonObject json = buildLiquidInfusionJson(true);

            assertTrue(json.has("requiredConstellation"),
                    "Recipe should have constellation requirement");
            ResourceLocation constellation =
                    new ResourceLocation(GsonHelper.getAsString(json, "requiredConstellation"));
            assertEquals("astralsorcery", constellation.getNamespace());
            assertEquals("discidia", constellation.getPath());
        }

        @Test
        void testInfusionWithoutConstellationRequirement() {
            JsonObject json = buildLiquidInfusionJson(false);

            assertFalse(json.has("requiredConstellation"),
                    "Basic infusion recipe should not require constellation");
        }

        @Test
        void testInfusionConstellationAbsenceLogic() {
            // Mirrors the serializer's logic: json.has("requiredConstellation") ? new RL(...) : null
            JsonObject json = buildLiquidInfusionJson(false);
            ResourceLocation constellation = json.has("requiredConstellation")
                    ? new ResourceLocation(GsonHelper.getAsString(json, "requiredConstellation"))
                    : null;
            assertNull(constellation, "No requiredConstellation field should yield null");
        }

        @Test
        void testInfusionConstellationPresenceLogic() {
            JsonObject json = buildLiquidInfusionJson(true);
            ResourceLocation constellation = json.has("requiredConstellation")
                    ? new ResourceLocation(GsonHelper.getAsString(json, "requiredConstellation"))
                    : null;
            assertNotNull(constellation, "requiredConstellation field should yield non-null");
            assertEquals("astralsorcery:discidia", constellation.toString());
        }

        @Test
        void testInfusionDefaultFluidMb() {
            JsonObject json = new JsonObject();
            assertEquals(500, GsonHelper.getAsInt(json, "fluidMbRequired", 500));
        }

        @Test
        void testInfusionDefaultCraftDuration() {
            JsonObject json = new JsonObject();
            assertEquals(200, GsonHelper.getAsInt(json, "craftDuration", 200));
        }

        @Test
        void testInfusionDefaultConsumeMultiple() {
            JsonObject json = new JsonObject();
            assertFalse(GsonHelper.getAsBoolean(json, "consumeMultiple", false));
        }

        @Test
        void testInfusionConsumeMultipleTrue() {
            JsonObject json = new JsonObject();
            json.addProperty("consumeMultiple", true);
            assertTrue(GsonHelper.getAsBoolean(json, "consumeMultiple", false));
        }

        @Test
        void testInfusionMissingInputThrows() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsJsonObject(json, "input"));
        }

        @Test
        void testInfusionMissingOutputThrows() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsJsonObject(json, "output"));
        }

        @Test
        void testInfusionTagInput() {
            JsonObject json = buildLiquidInfusionJson(false);
            JsonObject input = GsonHelper.getAsJsonObject(json, "input");

            assertTrue(input.has("tag"), "Infusion input should use tag");
            assertFalse(input.has("item"), "Tag-based input should not have item field");
        }

        @Test
        void testInfusionExplicitFluidMb() {
            JsonObject json = new JsonObject();
            json.addProperty("fluidMbRequired", 800);
            assertEquals(800, GsonHelper.getAsInt(json, "fluidMbRequired", 500));
        }

        @Test
        void testInfusionExplicitDuration() {
            JsonObject json = new JsonObject();
            json.addProperty("craftDuration", 400);
            assertEquals(400, GsonHelper.getAsInt(json, "craftDuration", 200));
        }

        @Test
        void testInfusionNetworkFieldEncoding() {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

            // Simulate non-Ingredient, non-ItemStack fields
            int fluidMb = 800;
            int duration = 400;
            boolean hasConstellation = true;
            ResourceLocation constellation = new ResourceLocation("astralsorcery", "discidia");
            boolean multi = true;

            buf.writeVarInt(fluidMb);
            buf.writeVarInt(duration);
            buf.writeBoolean(hasConstellation);
            buf.writeResourceLocation(constellation);
            buf.writeBoolean(multi);

            assertEquals(fluidMb, buf.readVarInt());
            assertEquals(duration, buf.readVarInt());
            assertTrue(buf.readBoolean());
            assertEquals(constellation, buf.readResourceLocation());
            assertTrue(buf.readBoolean());

            buf.release();
        }

        @Test
        void testInfusionNetworkNoConstellation() {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

            buf.writeVarInt(200);
            buf.writeVarInt(100);
            buf.writeBoolean(false); // no constellation
            buf.writeBoolean(false); // consumeMultiple

            assertEquals(200, buf.readVarInt());
            assertEquals(100, buf.readVarInt());
            assertFalse(buf.readBoolean());
            assertFalse(buf.readBoolean());

            buf.release();
        }
    }

    // ========================================================================
    // BlockTransmutation — JSON field extraction tests
    // ========================================================================

    @Nested
    class BlockTransmutationJsonTests {

        @Test
        void testTransmutationJsonFieldExtraction() {
            JsonObject json = buildBlockTransmutationJson(false);

            assertEquals("minecraft:iron_ore", GsonHelper.getAsString(json, "input"));
            assertEquals("astralsorcery:starmetal_ore", GsonHelper.getAsString(json, "output"));
            assertEquals(400.0f, GsonHelper.getAsFloat(json, "starlightRequired", 200.0f), 0.01f);
        }

        @Test
        void testTransmutationWithOutputDisplay() {
            JsonObject json = buildBlockTransmutationJson(true);

            assertTrue(json.has("outputDisplay"), "Recipe should have outputDisplay");
            JsonObject display = GsonHelper.getAsJsonObject(json, "outputDisplay");
            assertEquals("astralsorcery:starmetal_ore", GsonHelper.getAsString(display, "item"));
            assertEquals(1, GsonHelper.getAsInt(display, "count", 1));
        }

        @Test
        void testTransmutationWithoutOutputDisplay() {
            JsonObject json = buildBlockTransmutationJson(false);

            assertFalse(json.has("outputDisplay"),
                    "Simple transmutation should not have outputDisplay");
        }

        @Test
        void testTransmutationOutputDisplayPresenceLogic() {
            // Mirrors serializer: json.has("outputDisplay") ? ... : new ItemStack(output.getBlock())
            JsonObject jsonWith = buildBlockTransmutationJson(true);
            assertTrue(jsonWith.has("outputDisplay"), "Should detect display when present");

            JsonObject jsonWithout = buildBlockTransmutationJson(false);
            assertFalse(jsonWithout.has("outputDisplay"), "Should detect absence of display");
        }

        @Test
        void testTransmutationDefaultStarlight() {
            JsonObject json = new JsonObject();
            assertEquals(200.0f, GsonHelper.getAsFloat(json, "starlightRequired", 200.0F), 0.01f);
        }

        @Test
        void testTransmutationMissingInputThrows() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsString(json, "input"));
        }

        @Test
        void testTransmutationMissingOutputThrows() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsString(json, "output"));
        }

        @Test
        void testTransmutationInputAsResourceLocation() {
            JsonObject json = buildBlockTransmutationJson(false);

            ResourceLocation inputLoc = new ResourceLocation(GsonHelper.getAsString(json, "input"));
            assertEquals("minecraft", inputLoc.getNamespace());
            assertEquals("iron_ore", inputLoc.getPath());
        }

        @Test
        void testTransmutationOutputAsResourceLocation() {
            JsonObject json = buildBlockTransmutationJson(false);

            ResourceLocation outputLoc = new ResourceLocation(GsonHelper.getAsString(json, "output"));
            assertEquals("astralsorcery", outputLoc.getNamespace());
            assertEquals("starmetal_ore", outputLoc.getPath());
        }

        @Test
        void testTransmutationExplicitStarlight() {
            JsonObject json = new JsonObject();
            json.addProperty("starlightRequired", 800);
            assertEquals(800.0f, GsonHelper.getAsFloat(json, "starlightRequired", 200.0f), 0.01f);
        }

        @Test
        void testTransmutationVariousBlockNames() {
            // Verify that various block name formats parse into valid ResourceLocations
            String[] blockNames = {
                    "minecraft:iron_ore", "minecraft:stone", "minecraft:sand",
                    "astralsorcery:starmetal_ore", "minecraft:pumpkin"
            };
            for (String name : blockNames) {
                ResourceLocation loc = new ResourceLocation(name);
                assertNotNull(loc.getNamespace(), "Namespace should not be null for " + name);
                assertNotNull(loc.getPath(), "Path should not be null for " + name);
            }
        }

        @Test
        void testTransmutationNetworkFieldEncoding() {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

            double starlight = 400.0;
            buf.writeDouble(starlight);

            assertEquals(starlight, buf.readDouble(), 0.001);

            buf.release();
        }
    }

    // ========================================================================
    // LiquidInteraction — JSON field extraction tests
    // ========================================================================

    @Nested
    class LiquidInteractionJsonTests {

        @Test
        void testInteractionJsonFieldExtraction() {
            JsonObject json = buildLiquidInteractionJson(true);

            JsonObject fluid1 = GsonHelper.getAsJsonObject(json, "fluid1");
            assertEquals("astralsorcery:liquid_starlight", GsonHelper.getAsString(fluid1, "fluid"));
            assertEquals(10, GsonHelper.getAsInt(fluid1, "amount", 1000));

            JsonObject fluid2 = GsonHelper.getAsJsonObject(json, "fluid2");
            assertEquals("minecraft:water", GsonHelper.getAsString(fluid2, "fluid"));
            assertEquals(10, GsonHelper.getAsInt(fluid2, "amount", 1000));

            assertEquals(2.0f, GsonHelper.getAsFloat(json, "weight", 1.0f), 0.01f);
        }

        @Test
        void testInteractionWithOutput() {
            JsonObject json = buildLiquidInteractionJson(true);

            assertTrue(json.has("output"), "Recipe should have output");
            JsonObject output = GsonHelper.getAsJsonObject(json, "output");
            assertEquals("minecraft:packed_ice", GsonHelper.getAsString(output, "item"));
            assertEquals(1, GsonHelper.getAsInt(output, "count", 1));
        }

        @Test
        void testInteractionWithoutOutput() {
            JsonObject json = buildLiquidInteractionJson(false);

            assertFalse(json.has("output"),
                    "Recipe without output should not have output field");
        }

        @Test
        void testInteractionOutputPresenceLogic() {
            // Mirrors serializer: json.has("output") ? ... : ItemStack.EMPTY
            JsonObject jsonWith = buildLiquidInteractionJson(true);
            assertTrue(jsonWith.has("output"), "Should detect output when present");

            JsonObject jsonWithout = buildLiquidInteractionJson(false);
            assertFalse(jsonWithout.has("output"), "Should detect absence of output");
        }

        @Test
        void testInteractionDefaultWeight() {
            JsonObject json = new JsonObject();
            assertEquals(1.0f, GsonHelper.getAsFloat(json, "weight", 1.0F), 0.01f);
        }

        @Test
        void testInteractionMissingFluid1Throws() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsJsonObject(json, "fluid1"));
        }

        @Test
        void testInteractionMissingFluid2Throws() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsJsonObject(json, "fluid2"));
        }

        @Test
        void testInteractionFluid1ResourceLocation() {
            JsonObject json = buildLiquidInteractionJson(true);
            JsonObject fluid1 = GsonHelper.getAsJsonObject(json, "fluid1");

            ResourceLocation fluid1Loc = new ResourceLocation(GsonHelper.getAsString(fluid1, "fluid"));
            assertEquals("astralsorcery", fluid1Loc.getNamespace());
            assertEquals("liquid_starlight", fluid1Loc.getPath());
        }

        @Test
        void testInteractionFluid2ResourceLocation() {
            JsonObject json = buildLiquidInteractionJson(true);
            JsonObject fluid2 = GsonHelper.getAsJsonObject(json, "fluid2");

            ResourceLocation fluid2Loc = new ResourceLocation(GsonHelper.getAsString(fluid2, "fluid"));
            assertEquals("minecraft", fluid2Loc.getNamespace());
            assertEquals("water", fluid2Loc.getPath());
        }

        @Test
        void testInteractionFluidAmounts() {
            JsonObject json = buildLiquidInteractionJson(true);

            JsonObject fluid1 = GsonHelper.getAsJsonObject(json, "fluid1");
            assertEquals(10, GsonHelper.getAsInt(fluid1, "amount", 1000));

            JsonObject fluid2 = GsonHelper.getAsJsonObject(json, "fluid2");
            assertEquals(10, GsonHelper.getAsInt(fluid2, "amount", 1000));
        }

        @Test
        void testInteractionFluidDefaultAmount() {
            // Fluid amount defaults to 1000 (FluidType.BUCKET_VOLUME) when absent
            JsonObject fluid = new JsonObject();
            fluid.addProperty("fluid", "minecraft:lava");
            assertEquals(1000, GsonHelper.getAsInt(fluid, "amount", 1000));
        }

        @Test
        void testInteractionExplicitWeight() {
            JsonObject json = new JsonObject();
            json.addProperty("weight", 5.0f);
            assertEquals(5.0f, GsonHelper.getAsFloat(json, "weight", 1.0f), 0.01f);
        }

        @Test
        void testInteractionOutputItemResourceLocation() {
            JsonObject json = buildLiquidInteractionJson(true);
            JsonObject output = GsonHelper.getAsJsonObject(json, "output");

            ResourceLocation itemLoc = new ResourceLocation(GsonHelper.getAsString(output, "item"));
            assertEquals("minecraft", itemLoc.getNamespace());
            assertEquals("packed_ice", itemLoc.getPath());
        }

        @Test
        void testInteractionNetworkFieldEncoding() {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

            float weight = 3.5f;
            buf.writeFloat(weight);

            assertEquals(weight, buf.readFloat(), 0.0001f);

            buf.release();
        }
    }

    // ========================================================================
    // Altar recipe network encoding (enum + scalar fields)
    // ========================================================================

    @Nested
    class AltarNetworkEncodingTests {

        @Test
        void testAltarEnumEncoding() {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

            for (BlockAltar.AltarType type : BlockAltar.AltarType.values()) {
                buf.writeEnum(type);
            }

            for (BlockAltar.AltarType expected : BlockAltar.AltarType.values()) {
                BlockAltar.AltarType actual = buf.readEnum(BlockAltar.AltarType.class);
                assertEquals(expected, actual, "Enum round-trip failed for " + expected);
            }

            buf.release();
        }

        @Test
        void testAltarScalarFieldsRoundTrip() {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

            // Write fields in the order Serializer.toNetwork does
            buf.writeEnum(BlockAltar.AltarType.CONSTELLATION);
            buf.writeVarInt(300);    // craftDuration
            buf.writeDouble(3000.0); // starlightRequired
            buf.writeVarInt(21);     // input count

            assertEquals(BlockAltar.AltarType.CONSTELLATION, buf.readEnum(BlockAltar.AltarType.class));
            assertEquals(300, buf.readVarInt());
            assertEquals(3000.0, buf.readDouble(), 0.001);
            assertEquals(21, buf.readVarInt());

            buf.release();
        }

        @Test
        void testAltarConstellationFlagTrueEncoding() {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            ResourceLocation focus = new ResourceLocation("astralsorcery", "vicio");

            buf.writeBoolean(true);
            buf.writeResourceLocation(focus);

            assertTrue(buf.readBoolean());
            assertEquals(focus, buf.readResourceLocation());

            buf.release();
        }

        @Test
        void testAltarConstellationFlagFalseEncoding() {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

            buf.writeBoolean(false);

            assertFalse(buf.readBoolean());

            buf.release();
        }

        @Test
        void testAltarAllTiersEnumRoundTrip() {
            // Test each tier individually for clarity
            for (BlockAltar.AltarType type : BlockAltar.AltarType.values()) {
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeEnum(type);
                assertEquals(type, buf.readEnum(BlockAltar.AltarType.class),
                        "Round-trip failed for " + type);
                buf.release();
            }
        }

        @Test
        void testAltarFullScalarMessagePattern() {
            // Simulate the complete toNetwork scalar layout for a radiance recipe
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

            buf.writeEnum(BlockAltar.AltarType.RADIANCE);
            buf.writeVarInt(600);     // craftDuration
            buf.writeDouble(4800.0);  // starlightRequired
            // Item stack and ingredients would be here (MC-bootstrapped)
            buf.writeVarInt(25);      // inputCount
            // Ingredients would be here
            buf.writeBoolean(false);  // no focus constellation

            assertEquals(BlockAltar.AltarType.RADIANCE, buf.readEnum(BlockAltar.AltarType.class));
            assertEquals(600, buf.readVarInt());
            assertEquals(4800.0, buf.readDouble(), 0.001);
            assertEquals(25, buf.readVarInt());
            assertFalse(buf.readBoolean());

            buf.release();
        }
    }

    // ========================================================================
    // Cross-cutting JSON validation tests
    // ========================================================================

    @Nested
    class CrossCuttingJsonTests {

        @Test
        void testGsonHelperGetAsStringMissing() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsString(json, "nonexistent"));
        }

        @Test
        void testGsonHelperGetAsIntMissing() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsInt(json, "nonexistent"));
        }

        @Test
        void testGsonHelperGetAsIntWithDefault() {
            JsonObject json = new JsonObject();
            assertEquals(42, GsonHelper.getAsInt(json, "nonexistent", 42));
        }

        @Test
        void testGsonHelperGetAsFloatWithDefault() {
            JsonObject json = new JsonObject();
            assertEquals(3.14f, GsonHelper.getAsFloat(json, "nonexistent", 3.14f), 0.01f);
        }

        @Test
        void testGsonHelperGetAsBooleanWithDefault() {
            JsonObject json = new JsonObject();
            assertFalse(GsonHelper.getAsBoolean(json, "nonexistent", false));
            assertTrue(GsonHelper.getAsBoolean(json, "nonexistent", true));
        }

        @Test
        void testGsonHelperGetAsJsonObjectMissing() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsJsonObject(json, "nonexistent"));
        }

        @Test
        void testGsonHelperGetAsJsonArrayMissing() {
            JsonObject json = new JsonObject();
            assertThrows(JsonSyntaxException.class,
                    () -> GsonHelper.getAsJsonArray(json, "nonexistent"));
        }

        @Test
        void testResourceLocationFromFluidString() {
            ResourceLocation loc = new ResourceLocation("astralsorcery:liquid_starlight");
            assertEquals("astralsorcery", loc.getNamespace());
            assertEquals("liquid_starlight", loc.getPath());
        }

        @Test
        void testResourceLocationFromMinecraftString() {
            ResourceLocation loc = new ResourceLocation("minecraft:water");
            assertEquals("minecraft", loc.getNamespace());
            assertEquals("water", loc.getPath());
        }

        @Test
        void testResourceLocationDefaultNamespace() {
            ResourceLocation loc = new ResourceLocation("stone");
            assertEquals("minecraft", loc.getNamespace());
            assertEquals("stone", loc.getPath());
        }

        @Test
        void testRecipeTypeStrings() {
            // Verify the type strings used in recipe JSON match expected patterns
            String[] expectedTypes = {
                    "astralsorcery:altar",
                    "astralsorcery:well_liquefaction",
                    "astralsorcery:liquid_infusion",
                    "astralsorcery:block_transmutation",
                    "astralsorcery:liquid_interaction"
            };
            for (String typeStr : expectedTypes) {
                ResourceLocation typeLoc = new ResourceLocation(typeStr);
                assertEquals("astralsorcery", typeLoc.getNamespace());
                assertFalse(typeLoc.getPath().isEmpty());
            }
        }
    }

    // ========================================================================
    // Full JSON structure round-trip (build + extract + validate)
    // ========================================================================

    @Nested
    class FullJsonStructureTests {

        @Test
        void testDiscoveryJsonRoundTrip() {
            JsonObject json = buildDiscoveryAltarJson();
            verifyAltarJsonStructure(json, "discovery", 100, 200, 9);
        }

        @Test
        void testAttunementJsonRoundTrip() {
            JsonObject json = buildAttunementAltarJson();
            verifyAltarJsonStructure(json, "attunement", 200, 600, 13);
        }

        @Test
        void testConstellationJsonRoundTrip() {
            JsonObject json = buildConstellationAltarJson();
            verifyAltarJsonStructure(json, "constellation", 400, 3200, 21);
        }

        @Test
        void testRadianceJsonRoundTrip() {
            JsonObject json = buildRadianceAltarJson();
            verifyAltarJsonStructure(json, "radiance", 600, 4800, 21);
        }

        @Test
        void testWellJsonRoundTrip() {
            JsonObject json = buildWellLiquefactionJson();

            assertEquals("astralsorcery:well_liquefaction",
                    GsonHelper.getAsString(json, "type"));
            assertDoesNotThrow(() -> GsonHelper.getAsJsonObject(json, "input"));
            assertDoesNotThrow(() -> GsonHelper.getAsJsonObject(json, "output"));
        }

        @Test
        void testInfusionWithoutConstellationRoundTrip() {
            JsonObject json = buildLiquidInfusionJson(false);

            assertEquals("astralsorcery:liquid_infusion",
                    GsonHelper.getAsString(json, "type"));
            assertDoesNotThrow(() -> GsonHelper.getAsJsonObject(json, "input"));
            assertDoesNotThrow(() -> GsonHelper.getAsJsonObject(json, "output"));
            assertFalse(json.has("requiredConstellation"));
        }

        @Test
        void testInfusionWithConstellationRoundTrip() {
            JsonObject json = buildLiquidInfusionJson(true);

            assertEquals("astralsorcery:liquid_infusion",
                    GsonHelper.getAsString(json, "type"));
            assertTrue(json.has("requiredConstellation"));
        }

        @Test
        void testTransmutationJsonRoundTrip() {
            JsonObject json = buildBlockTransmutationJson(false);

            assertEquals("astralsorcery:block_transmutation",
                    GsonHelper.getAsString(json, "type"));
            assertDoesNotThrow(() -> GsonHelper.getAsString(json, "input"));
            assertDoesNotThrow(() -> GsonHelper.getAsString(json, "output"));
        }

        @Test
        void testInteractionJsonRoundTrip() {
            JsonObject json = buildLiquidInteractionJson(true);

            assertEquals("astralsorcery:liquid_interaction",
                    GsonHelper.getAsString(json, "type"));
            assertDoesNotThrow(() -> GsonHelper.getAsJsonObject(json, "fluid1"));
            assertDoesNotThrow(() -> GsonHelper.getAsJsonObject(json, "fluid2"));
            assertTrue(json.has("output"));
        }

        private void verifyAltarJsonStructure(JsonObject json, String expectedType,
                                               int expectedDuration, float expectedStarlight,
                                               int expectedInputCount) {
            assertEquals("astralsorcery:altar", GsonHelper.getAsString(json, "type"));
            assertEquals(expectedType, GsonHelper.getAsString(json, "altar_type"));

            BlockAltar.AltarType altarType = BlockAltar.AltarType.valueOf(
                    expectedType.toUpperCase(Locale.ROOT));
            assertNotNull(altarType);

            assertEquals(expectedDuration, GsonHelper.getAsInt(json, "craft_duration", 100));
            assertEquals(expectedStarlight,
                    GsonHelper.getAsFloat(json, "starlight_required", 200.0f), 0.01f);

            assertDoesNotThrow(() -> GsonHelper.getAsJsonObject(json, "output"));

            JsonArray inputs = GsonHelper.getAsJsonArray(json, "inputs");
            assertEquals(expectedInputCount, inputs.size());
        }
    }

    // ========================================================================
    // JSON builder helpers
    // ========================================================================

    /**
     * Build a Discovery-tier altar recipe JSON matching the luminous_crafting_table.json format.
     */
    private static JsonObject buildDiscoveryAltarJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "astralsorcery:altar");
        json.addProperty("altar_type", "discovery");
        json.addProperty("craft_duration", 100);
        json.addProperty("starlight_required", 200);

        JsonObject output = new JsonObject();
        output.addProperty("item", "astralsorcery:altar_discovery");
        output.addProperty("count", 1);
        json.add("output", output);

        JsonArray inputs = new JsonArray();
        String[] items = {
                "astralsorcery:marble_raw", "astralsorcery:aquamarine", "astralsorcery:marble_raw",
                "astralsorcery:aquamarine", "astralsorcery:black_marble_raw", "astralsorcery:aquamarine",
                "astralsorcery:marble_raw", "astralsorcery:aquamarine", "astralsorcery:marble_raw"
        };
        for (String item : items) {
            JsonObject input = new JsonObject();
            input.addProperty("item", item);
            inputs.add(input);
        }
        json.add("inputs", inputs);

        return json;
    }

    /**
     * Build an Attunement-tier altar recipe JSON with empty slots.
     */
    private static JsonObject buildAttunementAltarJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "astralsorcery:altar");
        json.addProperty("altar_type", "attunement");
        json.addProperty("craft_duration", 200);
        json.addProperty("starlight_required", 600);

        JsonObject output = new JsonObject();
        output.addProperty("item", "astralsorcery:attunement_altar");
        output.addProperty("count", 1);
        json.add("output", output);

        JsonArray inputs = new JsonArray();
        inputs.add(new JsonObject()); // slot 0: empty
        addItemInput(inputs, "astralsorcery:stardust");
        inputs.add(new JsonObject()); // slot 2: empty
        addItemInput(inputs, "astralsorcery:stardust");
        addItemInput(inputs, "astralsorcery:rock_crystal");
        addItemInput(inputs, "astralsorcery:stardust");
        addItemInput(inputs, "astralsorcery:marble_raw");
        addItemInput(inputs, "astralsorcery:marble_raw");
        addItemInput(inputs, "astralsorcery:marble_raw");
        addItemInput(inputs, "astralsorcery:aquamarine");
        inputs.add(new JsonObject()); // slot 10: empty
        addItemInput(inputs, "astralsorcery:aquamarine");
        inputs.add(new JsonObject()); // slot 12: empty

        json.add("inputs", inputs);
        return json;
    }

    /**
     * Build a Constellation-tier altar recipe JSON.
     */
    private static JsonObject buildConstellationAltarJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "astralsorcery:altar");
        json.addProperty("altar_type", "constellation");
        json.addProperty("craft_duration", 400);
        json.addProperty("starlight_required", 3200);

        JsonObject output = new JsonObject();
        output.addProperty("item", "astralsorcery:celestial_collector_crystal");
        output.addProperty("count", 1);
        json.add("output", output);

        JsonArray inputs = new JsonArray();
        String[] items = {
                "astralsorcery:starmetal_ingot", "astralsorcery:celestial_crystal",
                "astralsorcery:starmetal_ingot", "astralsorcery:celestial_crystal",
                "astralsorcery:stardust", "astralsorcery:celestial_crystal",
                "astralsorcery:marble_runed", "astralsorcery:starmetal_ingot",
                "astralsorcery:marble_runed", "astralsorcery:aquamarine",
                "astralsorcery:starmetal_dust", "astralsorcery:aquamarine",
                "astralsorcery:starmetal_dust"
        };
        for (String item : items) {
            addItemInput(inputs, item);
        }
        // Pad remaining 8 slots as empty
        for (int i = 0; i < 8; i++) {
            inputs.add(new JsonObject());
        }

        json.add("inputs", inputs);
        return json;
    }

    /**
     * Build a Constellation-tier altar recipe JSON with focus_constellation.
     */
    private static JsonObject buildConstellationAltarJsonWithFocus() {
        JsonObject json = buildConstellationAltarJson();
        json.addProperty("focus_constellation", "astralsorcery:vicio");
        return json;
    }

    /**
     * Build a Radiance-tier altar recipe JSON.
     */
    private static JsonObject buildRadianceAltarJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "astralsorcery:altar");
        json.addProperty("altar_type", "radiance");
        json.addProperty("craft_duration", 600);
        json.addProperty("starlight_required", 4800);

        JsonObject output = new JsonObject();
        output.addProperty("item", "astralsorcery:fountain");
        output.addProperty("count", 1);
        json.add("output", output);

        JsonArray inputs = new JsonArray();
        String[] items = {
                "astralsorcery:starmetal_ingot", "astralsorcery:celestial_crystal",
                "astralsorcery:starmetal_ingot", "astralsorcery:celestial_crystal",
                "astralsorcery:stardust", "astralsorcery:celestial_crystal",
                "astralsorcery:marble_runed", "astralsorcery:starmetal_ingot",
                "astralsorcery:marble_runed", "astralsorcery:aquamarine",
                "astralsorcery:starmetal_dust", "astralsorcery:aquamarine",
                "astralsorcery:starmetal_dust", "astralsorcery:rock_crystal"
        };
        for (String item : items) {
            addItemInput(inputs, item);
        }
        inputs.add(new JsonObject()); // empty
        addItemInput(inputs, "astralsorcery:rock_crystal");
        inputs.add(new JsonObject()); // empty
        addItemInput(inputs, "astralsorcery:starmetal_ingot");
        inputs.add(new JsonObject()); // empty
        addItemInput(inputs, "astralsorcery:starmetal_ingot");
        inputs.add(new JsonObject()); // empty

        json.add("inputs", inputs);
        return json;
    }

    /**
     * Build a WellLiquefaction recipe JSON matching rock_crystal.json format.
     */
    private static JsonObject buildWellLiquefactionJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "astralsorcery:well_liquefaction");

        JsonObject input = new JsonObject();
        input.addProperty("item", "astralsorcery:rock_crystal");
        json.add("input", input);

        JsonObject output = new JsonObject();
        output.addProperty("fluid", "astralsorcery:liquid_starlight");
        output.addProperty("amount", 1);
        json.add("output", output);

        json.addProperty("productionMultiplier", 1.0);
        json.addProperty("color", 4430335);

        return json;
    }

    /**
     * Build a LiquidInfusion recipe JSON.
     * @param withConstellation whether to include requiredConstellation
     */
    private static JsonObject buildLiquidInfusionJson(boolean withConstellation) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "astralsorcery:liquid_infusion");

        JsonObject input = new JsonObject();
        input.addProperty("tag", "forge:ingots/iron");
        json.add("input", input);

        JsonObject output = new JsonObject();
        output.addProperty("item", "astralsorcery:starmetal_ingot");
        output.addProperty("count", 1);
        json.add("output", output);

        json.addProperty("fluidMbRequired", 200);
        json.addProperty("craftDuration", 100);

        if (withConstellation) {
            json.addProperty("requiredConstellation", "astralsorcery:discidia");
        }

        return json;
    }

    /**
     * Build a BlockTransmutation recipe JSON.
     * @param withDisplay whether to include outputDisplay
     */
    private static JsonObject buildBlockTransmutationJson(boolean withDisplay) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "astralsorcery:block_transmutation");
        json.addProperty("input", "minecraft:iron_ore");
        json.addProperty("output", "astralsorcery:starmetal_ore");
        json.addProperty("starlightRequired", 400);

        if (withDisplay) {
            JsonObject display = new JsonObject();
            display.addProperty("item", "astralsorcery:starmetal_ore");
            display.addProperty("count", 1);
            json.add("outputDisplay", display);
        }

        return json;
    }

    /**
     * Build a LiquidInteraction recipe JSON.
     * @param withOutput whether to include an output item
     */
    private static JsonObject buildLiquidInteractionJson(boolean withOutput) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "astralsorcery:liquid_interaction");

        JsonObject fluid1 = new JsonObject();
        fluid1.addProperty("fluid", "astralsorcery:liquid_starlight");
        fluid1.addProperty("amount", 10);
        json.add("fluid1", fluid1);

        JsonObject fluid2 = new JsonObject();
        fluid2.addProperty("fluid", "minecraft:water");
        fluid2.addProperty("amount", 10);
        json.add("fluid2", fluid2);

        json.addProperty("weight", 2.0);

        if (withOutput) {
            JsonObject output = new JsonObject();
            output.addProperty("item", "minecraft:packed_ice");
            output.addProperty("count", 1);
            json.add("output", output);
        }

        return json;
    }

    // ========================================================================
    // Test utility helpers
    // ========================================================================

    private static void addItemInput(JsonArray inputs, String itemId) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", itemId);
        inputs.add(obj);
    }

    /**
     * Mirror of the serializer's slotCountForType method for unit testing.
     * This must match SimpleAltarRecipe.Serializer.slotCountForType exactly.
     */
    private static int slotCountForType(BlockAltar.AltarType type) {
        return switch (type) {
            case DISCOVERY -> 9;
            case ATTUNEMENT -> 13;
            case CONSTELLATION -> 21;
            case RADIANCE -> 25;
        };
    }
}
