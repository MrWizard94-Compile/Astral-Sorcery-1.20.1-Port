/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.attunement;

import hellfirepvp.astralsorcery.common.crafting.nojson.CustomRecipe;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAttunementAltar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public abstract class AttunementRecipe<T extends AttunementRecipe.Active<?>> extends CustomRecipe {

    public AttunementRecipe(@Nonnull ResourceLocation key) {
        super(key);
    }

    public abstract boolean canStartCrafting(@Nonnull BlockEntityAttunementAltar altar);

    @Nonnull
    public abstract T createRecipe(@Nonnull BlockEntityAttunementAltar altar);

    @Nonnull
    public T deserialize(@Nonnull BlockEntityAttunementAltar altar, @Nonnull CompoundTag nbt,
                         @Nullable T previousInstance) {
        T activeRecipe = this.createRecipe(altar);
        activeRecipe.readFromNBT(nbt);
        return activeRecipe;
    }

    public abstract static class Active<T extends AttunementRecipe<? extends Active<T>>> {

        protected final Random rand = new Random();

        private final T recipe;
        private int tick = 0;

        public Active(@Nonnull T recipe) {
            this.recipe = recipe;
        }

        @Nonnull
        public final T getRecipe() {
            return recipe;
        }

        protected int getTick() {
            return tick;
        }

        public final void tick(@Nonnull LogicalSide side, @Nonnull BlockEntityAttunementAltar altar) {
            this.doTick(side, altar);
            this.tick++;
        }

        public abstract void stopCrafting(@Nonnull BlockEntityAttunementAltar altar);

        public abstract void startCrafting(@Nonnull BlockEntityAttunementAltar altar);

        public abstract void finishRecipe(@Nonnull BlockEntityAttunementAltar altar);

        public abstract void doTick(@Nonnull LogicalSide side, @Nonnull BlockEntityAttunementAltar altar);

        public abstract boolean isFinished(@Nonnull BlockEntityAttunementAltar altar);

        public boolean matches(@Nonnull BlockEntityAttunementAltar altar) {
            return this.recipe.canStartCrafting(altar);
        }

        public void writeToNBT(@Nonnull CompoundTag nbt) {
            nbt.putInt("tick", this.tick);
        }

        protected void readFromNBT(@Nonnull CompoundTag nbt) {
            this.tick = nbt.getInt("tick");
        }
    }
}
