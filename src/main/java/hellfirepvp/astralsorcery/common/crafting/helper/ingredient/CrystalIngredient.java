package hellfirepvp.astralsorcery.common.crafting.helper.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.lib.IngredientSerializersAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Recipe ingredient that matches rock/celestial crystals with optional attunement/tier filters.
 *
 * <p>1.16 → 1.20: {@code IItemList} → {@code Ingredient.Value},
 * {@code StackList} → stream of {@code Ingredient.ItemValue},
 * {@code ItemsAS.FOO} → {@code ItemsAS.FOO.get()}.</p>
 */
public class CrystalIngredient extends Ingredient {

    private final boolean hasToBeAttuned;
    private final boolean hasToBeCelestial;
    private final boolean canBeAttuned;
    private final boolean canBeCelestialCrystal;

    public CrystalIngredient(boolean hasToBeAttuned, boolean hasToBeCelestial) {
        this(hasToBeAttuned, hasToBeCelestial, true, true);
    }

    public CrystalIngredient(boolean hasToBeAttuned, boolean hasToBeCelestial,
                              boolean canBeAttuned, boolean canBeCelestialCrystal) {
        super(Arrays.stream(buildValues(hasToBeAttuned, hasToBeCelestial, canBeAttuned, canBeCelestialCrystal)));
        this.hasToBeAttuned = hasToBeAttuned;
        this.hasToBeCelestial = hasToBeCelestial;
        this.canBeAttuned = canBeAttuned;
        this.canBeCelestialCrystal = canBeCelestialCrystal;
    }

    private static Value[] buildValues(boolean hasToBeAttuned, boolean hasToBeCelestial,
                                       boolean canBeAttuned, boolean canBeCelestialCrystal) {
        boolean attuned = hasToBeAttuned || canBeAttuned;
        boolean celestial = hasToBeCelestial || canBeCelestialCrystal;

        List<ItemStack> stacks = new ArrayList<>();
        if (hasToBeAttuned) {
            if (hasToBeCelestial) {
                stacks.add(new ItemStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get()));
            } else {
                stacks.add(new ItemStack(ItemsAS.ATTUNED_ROCK_CRYSTAL.get()));
                if (canBeCelestialCrystal) {
                    stacks.add(new ItemStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get()));
                }
            }
        } else {
            if (hasToBeCelestial) {
                stacks.add(new ItemStack(ItemsAS.CELESTIAL_CRYSTAL.get()));
                if (attuned) {
                    stacks.add(new ItemStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get()));
                }
            } else {
                stacks.add(new ItemStack(ItemsAS.ROCK_CRYSTAL.get()));
                if (celestial) {
                    stacks.add(new ItemStack(ItemsAS.CELESTIAL_CRYSTAL.get()));
                }
                if (attuned) {
                    stacks.add(new ItemStack(ItemsAS.ATTUNED_ROCK_CRYSTAL.get()));
                    stacks.add(new ItemStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get()));
                }
            }
        }

        return stacks.stream()
                .map(ItemValue::new)
                .toArray(Value[]::new);
    }

    public boolean hasToBeAttuned()        { return hasToBeAttuned; }
    public boolean hasToBeCelestial()      { return hasToBeCelestial; }
    public boolean canBeAttuned()          { return canBeAttuned; }
    public boolean canBeCelestialCrystal() { return canBeCelestialCrystal; }

    @Nonnull
    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        net.minecraft.resources.ResourceLocation serializerId =
                java.util.Objects.requireNonNull(CraftingHelper.getID(IngredientSerializersAS.CRYSTAL_SERIALIZER),
                        "CrystalIngredient serializer is not registered");
        obj.addProperty("type", serializerId.toString());
        obj.addProperty("hasToBeAttuned", hasToBeAttuned);
        obj.addProperty("hasToBeCelestial", hasToBeCelestial);
        obj.addProperty("canBeAttuned", canBeAttuned);
        obj.addProperty("canBeCelestialCrystal", canBeCelestialCrystal);
        return obj;
    }

    @Nonnull
    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return IngredientSerializersAS.CRYSTAL_SERIALIZER;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    // ---- Serializer ----

    public static final IIngredientSerializer<CrystalIngredient> SERIALIZER =
            new IIngredientSerializer<>() {
                @Nonnull
                @Override
                public CrystalIngredient parse(@Nonnull JsonObject json) {
                    boolean hasToBeAttuned     = json.has("hasToBeAttuned") && json.get("hasToBeAttuned").getAsBoolean();
                    boolean hasToBeCelestial   = json.has("hasToBeCelestial") && json.get("hasToBeCelestial").getAsBoolean();
                    boolean canBeAttuned       = !json.has("canBeAttuned") || json.get("canBeAttuned").getAsBoolean();
                    boolean canBeCelestial     = !json.has("canBeCelestialCrystal") || json.get("canBeCelestialCrystal").getAsBoolean();
                    return new CrystalIngredient(hasToBeAttuned, hasToBeCelestial, canBeAttuned, canBeCelestial);
                }

                @Nonnull
                @Override
                public CrystalIngredient parse(@Nonnull net.minecraft.network.FriendlyByteBuf buffer) {
                    boolean hasToBeAttuned   = buffer.readBoolean();
                    boolean hasToBeCelestial = buffer.readBoolean();
                    boolean canBeAttuned     = buffer.readBoolean();
                    boolean canBeCelestial   = buffer.readBoolean();
                    return new CrystalIngredient(hasToBeAttuned, hasToBeCelestial, canBeAttuned, canBeCelestial);
                }

                @Override
                public void write(@Nonnull net.minecraft.network.FriendlyByteBuf buffer,
                                  @Nonnull CrystalIngredient ingredient) {
                    buffer.writeBoolean(ingredient.hasToBeAttuned());
                    buffer.writeBoolean(ingredient.hasToBeCelestial());
                    buffer.writeBoolean(ingredient.canBeAttuned());
                    buffer.writeBoolean(ingredient.canBeCelestialCrystal());
                }
            };
}
