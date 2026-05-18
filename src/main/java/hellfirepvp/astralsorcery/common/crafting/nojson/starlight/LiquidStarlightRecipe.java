/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.starlight;

import hellfirepvp.astralsorcery.common.crafting.nojson.CustomRecipe;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class LiquidStarlightRecipe extends CustomRecipe {

    protected static final Random rand = new Random();
    private static final int WORLD_TIME_TOLERANCE = 10;

    public LiquidStarlightRecipe(@Nonnull ResourceLocation key) {
        super(key);
    }

    @Nonnull
    public abstract List<Ingredient> getInputForRender();

    @Nonnull
    public abstract List<Ingredient> getOutputForRender();

    public abstract boolean doesStartRecipe(@Nonnull ItemStack item);

    public abstract boolean matches(@Nonnull ItemEntity trigger, @Nonnull Level level,
                                    @Nonnull BlockPos at);

    public abstract void doServerCraftTick(@Nonnull ItemEntity trigger, @Nonnull Level level,
                                           @Nonnull BlockPos at);

    @Nonnull
    protected final List<Entity> getEntitiesInBlock(@Nonnull Level level, @Nonnull BlockPos pos) {
        return level.getEntities((Entity) null, new AABB(pos), e -> true);
    }

    @Nullable
    protected final ItemStack consumeItemEntityInBlock(@Nonnull Level level, @Nonnull BlockPos pos,
                                                      int count, @Nonnull Predicate<ItemStack> match) {
        List<Entity> entities = getEntitiesInBlock(level, pos).stream()
                .filter(e -> e instanceof ItemEntity)
                .collect(Collectors.toList());
        for (Entity e : entities) {
            ItemEntity ie = (ItemEntity) e;
            if (ie.isAlive() &&
                    !ie.getItem().isEmpty() &&
                    ie.getItem().getCount() >= count &&
                    match.test(ie.getItem())) {

                ItemStack stored = ie.getItem();
                ItemStack found = stored.copyWithCount(count);

                stored.shrink(count);
                ie.setItem(stored);
                return found;
            }
        }
        return null;
    }

    protected final int getAndIncrementCraftingTick(@Nonnull Entity e) {
        int tick = getCraftingTick(e);
        setCraftingTick(e, tick + 1);
        return tick;
    }

    protected final void setCraftingTick(@Nonnull Entity e, int tick) {
        long wTick = e.level().getGameTime();
        CompoundTag nbt = NBTHelper.getPersistentData(e);
        nbt.putInt("craftTick", tick);
        nbt.putLong("wCraftTick", wTick);
    }

    protected final int getCraftingTick(@Nonnull Entity e) {
        long wTick = e.level().getGameTime();
        CompoundTag nbt = NBTHelper.getPersistentData(e);
        if (!nbt.contains("wCraftTick")) {
            return 0;
        }
        long savedWTick = nbt.getLong("wCraftTick");
        if (Math.abs(wTick - savedWTick) > WORLD_TIME_TOLERANCE) {
            return 0;
        }
        return nbt.getInt("craftTick");
    }
}
