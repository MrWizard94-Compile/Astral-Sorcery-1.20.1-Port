/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe;

import hellfirepvp.astralsorcery.common.data.config.CommonConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Tracks an in-progress altar crafting operation. Created when a player
 * initiates a craft at an altar and maintained by the BlockEntityAltar
 * until the craft completes or is interrupted.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>Player clicks altar with valid recipe → create ActiveSimpleAltarRecipe</li>
 *   <li>Each tick: accumulate starlight, advance progress</li>
 *   <li>If starlight starvation continues too long → abort craft</li>
 *   <li>If progress reaches completion → produce output, consume inputs</li>
 * </ol></p>
 *
 * <p>The state is serialized to NBT and stored in the altar's block entity
 * so it persists across chunk unloads/loads and server restarts.</p>
 *
 * <p>Key timing:
 * {@code totalTicks = recipe.getCraftDuration() * configMultiplier}
 * {@code starlightPerTick = recipe.getStarlightRequired() / totalTicks}
 * If the altar doesn't receive enough starlight per tick, the craft
 * stalls (ticksStalled increments) but doesn't progress. After 200 stalled
 * ticks without any starlight, the craft aborts.</p>
 */
public class ActiveSimpleAltarRecipe {

    private static final String TAG_RECIPE_ID = "recipeId";
    private static final String TAG_TICKS_CRAFTED = "ticksCrafted";
    private static final String TAG_STARLIGHT_ACCUMULATED = "starlightAccumulated";
    private static final String TAG_TICKS_STALLED = "ticksStalled";
    private static final String TAG_STATE = "state";

    /** Maximum ticks an altar can stall without any starlight before aborting. */
    private static final int MAX_STALL_TICKS = 200;

    @Nonnull
    private final ResourceLocation recipeId;
    @Nonnull
    private final SimpleAltarRecipe recipe;

    private int ticksCrafted;
    private double starlightAccumulated;
    private int ticksStalled;
    @Nonnull
    private CraftState state;

    /**
     * Creates a new active crafting session.
     *
     * @param recipe the recipe being crafted
     */
    public ActiveSimpleAltarRecipe(@Nonnull SimpleAltarRecipe recipe) {
        this.recipeId = recipe.getId();
        this.recipe = recipe;
        this.ticksCrafted = 0;
        this.starlightAccumulated = 0.0;
        this.ticksStalled = 0;
        this.state = CraftState.ACTIVE;
    }

    /**
     * Deserialization constructor.
     */
    private ActiveSimpleAltarRecipe(@Nonnull SimpleAltarRecipe recipe,
                                     int ticksCrafted,
                                     double starlightAccumulated,
                                     int ticksStalled,
                                     @Nonnull CraftState state) {
        this.recipeId = recipe.getId();
        this.recipe = recipe;
        this.ticksCrafted = ticksCrafted;
        this.starlightAccumulated = starlightAccumulated;
        this.ticksStalled = ticksStalled;
        this.state = state;
    }

    // ========================================================================
    // Tick processing
    // ========================================================================

    /**
     * Called each tick by the altar block entity. Advances the craft
     * based on available starlight.
     *
     * @param starlightAvailable starlight units available this tick
     * @return the current craft state after this tick
     */
    @Nonnull
    public CraftState tick(double starlightAvailable) {
        if (state != CraftState.ACTIVE) {
            return state;
        }

        int totalTicks = getTotalCraftTicks();
        double starlightNeeded = getStarlightPerTick();

        if (starlightAvailable >= starlightNeeded * 0.5) {
            // Enough starlight — advance craft
            ticksCrafted++;
            starlightAccumulated += Math.min(starlightAvailable, starlightNeeded);
            ticksStalled = 0;

            if (ticksCrafted >= totalTicks
                    && starlightAccumulated >= recipe.getStarlightRequired() * 0.95) {
                state = CraftState.COMPLETED;
            }
        } else {
            // Insufficient starlight — stall
            ticksStalled++;
            if (ticksStalled >= MAX_STALL_TICKS) {
                state = CraftState.ABORTED;
            }
        }

        return state;
    }

    /**
     * Immediately aborts the craft (e.g., structure broken, block removed).
     */
    public void abort() {
        this.state = CraftState.ABORTED;
    }

    // ========================================================================
    // Progress queries
    // ========================================================================

    /**
     * Gets the crafting progress as a fraction [0.0, 1.0].
     */
    public float getProgress() {
        int total = getTotalCraftTicks();
        if (total <= 0) return 1.0f;
        return Math.min(1.0f, (float) ticksCrafted / total);
    }

    /**
     * Gets the starlight accumulation progress [0.0, 1.0].
     */
    public float getStarlightProgress() {
        double required = recipe.getStarlightRequired();
        if (required <= 0) return 1.0f;
        return (float) Math.min(1.0, starlightAccumulated / required);
    }

    /**
     * Gets the total number of ticks this craft takes (with config multiplier).
     */
    public int getTotalCraftTicks() {
        double multiplier = CommonConfig.CONFIG.altarCraftTimeMultiplier.get();
        return Math.max(1, (int) (recipe.getCraftDuration() * multiplier));
    }

    /**
     * Gets the starlight required per tick for smooth progress.
     */
    public double getStarlightPerTick() {
        int total = getTotalCraftTicks();
        if (total <= 0) return recipe.getStarlightRequired();
        return recipe.getStarlightRequired() / total;
    }

    /**
     * Gets the number of ticks elapsed.
     */
    public int getTicksCrafted() {
        return ticksCrafted;
    }

    /**
     * Gets the number of stalled ticks (no starlight).
     */
    public int getTicksStalled() {
        return ticksStalled;
    }

    @Nonnull
    public CraftState getState() {
        return state;
    }

    @Nonnull
    public ResourceLocation getRecipeId() {
        return recipeId;
    }

    @Nonnull
    public SimpleAltarRecipe getRecipe() {
        return recipe;
    }

    /**
     * Gets the output ItemStack (only valid after completion).
     */
    @Nonnull
    public ItemStack getOutputItem() {
        return recipe.getOutput();
    }

    /**
     * Whether this craft is still running (not completed or aborted).
     */
    public boolean isActive() {
        return state == CraftState.ACTIVE;
    }

    // ========================================================================
    // NBT serialization
    // ========================================================================

    @Nonnull
    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_RECIPE_ID, recipeId.toString());
        tag.putInt(TAG_TICKS_CRAFTED, ticksCrafted);
        tag.putDouble(TAG_STARLIGHT_ACCUMULATED, starlightAccumulated);
        tag.putInt(TAG_TICKS_STALLED, ticksStalled);
        tag.putString(TAG_STATE, state.name());
        return tag;
    }

    /**
     * Reads an ActiveSimpleAltarRecipe from NBT.
     * Requires the recipe to still be registered (returns null if recipe is gone).
     *
     * @param tag the saved NBT
     * @param recipe the resolved recipe (looked up by caller from RecipeManager)
     * @return the restored active recipe, or null if data is invalid
     */
    @Nullable
    public static ActiveSimpleAltarRecipe readFromNBT(@Nonnull CompoundTag tag,
                                                       @Nullable SimpleAltarRecipe recipe) {
        if (recipe == null) {
            return null; // Recipe was removed (datapack change)
        }
        int ticksCrafted = tag.getInt(TAG_TICKS_CRAFTED);
        double starlight = tag.getDouble(TAG_STARLIGHT_ACCUMULATED);
        int stalled = tag.getInt(TAG_TICKS_STALLED);

        CraftState state;
        try {
            state = CraftState.valueOf(tag.getString(TAG_STATE));
        } catch (IllegalArgumentException e) {
            state = CraftState.ABORTED;
        }

        return new ActiveSimpleAltarRecipe(recipe, ticksCrafted, starlight, stalled, state);
    }

    /**
     * Gets the recipe ID from a saved NBT tag without constructing the full object.
     * Useful for recipe lookup before full deserialization.
     */
    @Nullable
    public static ResourceLocation getRecipeIdFromNBT(@Nonnull CompoundTag tag) {
        if (!tag.contains(TAG_RECIPE_ID)) return null;
        return ResourceLocation.tryParse(tag.getString(TAG_RECIPE_ID));
    }

    // ========================================================================
    // Craft state
    // ========================================================================

    /**
     * State of an in-progress altar craft.
     */
    public enum CraftState {
        /** Craft is actively progressing. */
        ACTIVE,
        /** Craft completed successfully — output ready. */
        COMPLETED,
        /** Craft was aborted (starlight starvation or structure break). */
        ABORTED
    }
}
