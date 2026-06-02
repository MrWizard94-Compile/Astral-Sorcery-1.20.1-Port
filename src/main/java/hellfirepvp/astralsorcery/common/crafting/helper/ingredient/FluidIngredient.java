package hellfirepvp.astralsorcery.common.crafting.helper.ingredient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.lib.IngredientSerializersAS;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Recipe ingredient that matches items containing a specified fluid (e.g. filled buckets).
 *
 * <p>Used in lightwell recipes to accept any item that holds the required fluid.
 * Matching is delegation to {@link FluidUtil#getFluidContained(ItemStack)}.</p>
 *
 * <p>1.16 → 1.20: removed {@code getValidItemStacksPacked()},
 * {@code getMatchingStacks()} → {@code getItems()},
 * {@code NonNullList} removed, {@code getRegistryName()} → {@code ForgeRegistries} lookup.</p>
 */
public class FluidIngredient extends Ingredient {

    private final List<FluidStack> fluids;
    private ItemStack[] cachedItems;

    public FluidIngredient(@Nonnull List<FluidStack> fluidStacks) {
        super(Stream.empty());
        this.fluids = fluidStacks;
    }

    public FluidIngredient(@Nonnull FluidStack... fluidStacks) {
        super(Stream.empty());
        this.fluids = Arrays.asList(fluidStacks);
    }

    @Nonnull
    public List<FluidStack> getFluids() {
        return fluids;
    }

    @Nonnull
    @Override
    public ItemStack[] getItems() {
        if (cachedItems == null) {
            cachedItems = fluids.stream()
                    .map(FluidUtil::getFilledBucket)
                    .toArray(ItemStack[]::new);
        }
        return cachedItems;
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        FluidStack contained = FluidUtil.getFluidContained(input).orElse(FluidStack.EMPTY);
        if (contained.isEmpty()) {
            return false;
        }
        for (FluidStack target : fluids) {
            if (contained.containsFluid(target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return fluids.isEmpty();
    }

    @Override
    protected void invalidate() {
        super.invalidate();
        cachedItems = null;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Nonnull
    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", CraftingHelper.getID(IngredientSerializersAS.FLUID_SERIALIZER).toString());

        JsonArray array = new JsonArray();
        for (FluidStack stack : fluids) {
            ResourceLocation fluidKey = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
            if (fluidKey == null) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("fluid", fluidKey.toString());
            entry.addProperty("amount", stack.getAmount());
            array.add(entry);
        }
        obj.add("fluid", array);
        return obj;
    }

    @Nonnull
    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return IngredientSerializersAS.FLUID_SERIALIZER;
    }

    // ---- Serializer ----

    public static final IIngredientSerializer<FluidIngredient> SERIALIZER =
            new IIngredientSerializer<>() {
                @Nonnull
                @Override
                public FluidIngredient parse(@Nonnull JsonObject json) {
                    JsonArray array = json.getAsJsonArray("fluid");
                    List<FluidStack> stacks = new java.util.ArrayList<>();
                    for (JsonElement el : array) {
                        JsonObject entry = el.getAsJsonObject();
                        ResourceLocation fluidId = new ResourceLocation(entry.get("fluid").getAsString());
                        int amount = entry.has("amount") ? entry.get("amount").getAsInt() : 1000;
                        net.minecraft.world.level.material.Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
                        if (fluid != null && !fluid.isSame(net.minecraft.world.level.material.Fluids.EMPTY)) {
                            stacks.add(new FluidStack(fluid, amount));
                        }
                    }
                    return new FluidIngredient(stacks);
                }

                @Nonnull
                @Override
                public FluidIngredient parse(@Nonnull FriendlyByteBuf buf) {
                    int count = buf.readVarInt();
                    List<FluidStack> stacks = new java.util.ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        stacks.add(FluidStack.readFromPacket(buf));
                    }
                    return new FluidIngredient(stacks);
                }

                @Override
                public void write(@Nonnull FriendlyByteBuf buf, @Nonnull FluidIngredient ingredient) {
                    buf.writeVarInt(ingredient.fluids.size());
                    for (FluidStack stack : ingredient.fluids) {
                        stack.writeToPacket(buf);
                    }
                }
            };
}
