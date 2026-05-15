/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.helper;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * Class: RecipeHelper
 * Created by HellFirePvP
 * Ported to 1.20.1 by Rob &amp; Corwin
 *
 * Static helper methods for reading and writing recipe data
 * shared across all custom recipe serializers. Provides consistent
 * serialization for {@link FluidStack} and {@link BlockState} in
 * both JSON (datapack) and network (packet) formats.
 *
 * <p>1.16 &rarr; 1.20 changes:
 * FluidAttributes.BUCKET_VOLUME &rarr; FluidType.BUCKET_VOLUME,
 * Registry.FLUID &rarr; ForgeRegistries.FLUIDS,
 * Registry.BLOCK &rarr; ForgeRegistries.BLOCKS,
 * PacketBuffer &rarr; FriendlyByteBuf,
 * block.getDefaultState &rarr; block.defaultBlockState,
 * Block.getStateId &rarr; Block.getId,
 * Block.getStateById &rarr; Block.stateById</p>
 */
public final class RecipeHelper {

    private RecipeHelper() {}

    // ---- FluidStack JSON ----

    /**
     * Reads a {@link FluidStack} from a JSON object containing "fluid" and optional "amount" keys.
     *
     * <p>Expected format: {@code {"fluid": "modid:fluid_name", "amount": 1000}}.
     * If "amount" is absent, defaults to {@link FluidType#BUCKET_VOLUME} (1000 mB).</p>
     *
     * @param json the JSON object to read from
     * @return the deserialized FluidStack, never null
     * @throws JsonSyntaxException if the "fluid" key is missing or the fluid is unknown
     */
    @Nonnull
    public static FluidStack readFluidStack(@Nonnull JsonObject json) {
        String fluidName = GsonHelper.getAsString(json, "fluid");
        ResourceLocation fluidKey = new ResourceLocation(fluidName);
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidKey);
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new JsonSyntaxException("Unknown fluid: " + fluidName);
        }
        int amount = GsonHelper.getAsInt(json, "amount", FluidType.BUCKET_VOLUME);
        return new FluidStack(fluid, amount);
    }

    // ---- FluidStack network ----

    /**
     * Writes a {@link FluidStack} to a network buffer using Forge's built-in serialization.
     *
     * @param buf   the buffer to write to
     * @param stack the FluidStack to serialize
     */
    public static void writeFluidStack(@Nonnull FriendlyByteBuf buf, @Nonnull FluidStack stack) {
        stack.writeToPacket(buf);
    }

    /**
     * Reads a {@link FluidStack} from a network buffer using Forge's built-in deserialization.
     *
     * @param buf the buffer to read from
     * @return the deserialized FluidStack
     */
    @Nonnull
    public static FluidStack readFluidStackFromNetwork(@Nonnull FriendlyByteBuf buf) {
        return FluidStack.readFromPacket(buf);
    }

    // ---- BlockState JSON ----

    /**
     * Reads a {@link BlockState} from a string of the form "modid:block_name".
     * Returns the block's default state. Does not parse property overrides.
     *
     * @param blockName the namespaced block identifier (e.g., "minecraft:iron_ore")
     * @return the default BlockState for the given block
     * @throws JsonSyntaxException if the block is unknown
     */
    @Nonnull
    public static BlockState readBlockState(@Nonnull String blockName) {
        ResourceLocation blockKey = new ResourceLocation(blockName);
        Block block = ForgeRegistries.BLOCKS.getValue(blockKey);
        if (block == null) {
            throw new JsonSyntaxException("Unknown block: " + blockName);
        }
        return block.defaultBlockState();
    }

    // ---- BlockState network ----

    /**
     * Writes a {@link BlockState} to a network buffer as its integer state ID.
     * Uses {@link Block#getId(BlockState)} for compact serialization.
     *
     * @param buf   the buffer to write to
     * @param state the BlockState to serialize
     */
    public static void writeBlockState(@Nonnull FriendlyByteBuf buf, @Nonnull BlockState state) {
        buf.writeVarInt(Block.getId(state));
    }

    /**
     * Reads a {@link BlockState} from a network buffer by its integer state ID.
     * Uses {@link Block#stateById(int)} for deserialization.
     *
     * @param buf the buffer to read from
     * @return the deserialized BlockState
     */
    @Nonnull
    public static BlockState readBlockStateFromNetwork(@Nonnull FriendlyByteBuf buf) {
        return Block.stateById(buf.readVarInt());
    }
}
