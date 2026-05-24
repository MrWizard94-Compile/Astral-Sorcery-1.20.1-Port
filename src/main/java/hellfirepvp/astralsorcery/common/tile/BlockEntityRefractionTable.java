package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.constellation.engraving.EngravedStarMap;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.item.ItemInfusedGlass;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.tile.base.BlockEntityTick;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import hellfirepvp.astralsorcery.common.util.tile.NamedInventoryTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import hellfirepvp.astralsorcery.common.util.data.Vector3;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Block entity for the refraction table.
 * At night with sky access, applies constellation-based engraving from infused glass to the input item.
 *
 * <p>1.16 → 1.20: getWorld() → getLevel(), isRemote() → isClientSide(),
 * attemptDamageItem → hurt(int, RandomSource, ServerPlayer),
 * SoundEvents.BLOCK_GLASS_BREAK → SoundEvents.GLASS_BREAK,
 * SoundCategory → SoundSource,
 * ITextComponent → Component, TranslationTextComponent → Component.translatable.</p>
 */
public class BlockEntityRefractionTable extends BlockEntityTick implements NamedInventoryTile {

    private static final float RUN_TIME = 10 * 20F;

    private int runTick = 0;

    private ItemStack glassStack = ItemStack.EMPTY;
    private int parchmentCount = 0;
    private ItemStack inputStack = ItemStack.EMPTY;

    public BlockEntityRefractionTable(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        super(BlockEntityTypesAS.REFRACTION_TABLE.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        Level level = getLevel();
        if (level == null) return;

        if (!level.isClientSide()) {
            if (DayTimeHelper.isNight(level) &&
                    doesSeeSky() &&
                    isValidGlassStack(getGlassStack())) {

                EngravedStarMap starMap = ItemInfusedGlass.getEngraving(getGlassStack());
                if (starMap != null &&
                        !hasParchment() &&
                        !getInputStack().isEmpty() &&
                        starMap.canAffect(getInputStack())) {
                    runTick++;
                    if (runTick > RUN_TIME) {
                        setInputStack(starMap.applyEffects(getInputStack()));
                        ItemStack glass = getGlassStack();
                        if (glass.hurt(1, level.getRandom(), null)) {
                            glass.shrink(1);
                            setGlassStack(glass);
                            SoundHelper.playSoundAround(SoundEvents.GLASS_BREAK, SoundSource.BLOCKS,
                                    level, worldPosition,
                                    level.getRandom().nextFloat() * 0.5F + 1F,
                                    level.getRandom().nextFloat() * 0.2F + 0.8F);
                        }
                        resetWorkTick();
                    }
                    markForUpdate();
                } else {
                    resetWorkTick();
                }
            } else {
                resetWorkTick();
            }
        }
    }

    private void resetWorkTick() {
        if (runTick > 0) {
            runTick = 0;
            markForUpdate();
        }
    }

    public int addParchment(int toAdd) {
        if (inputStack.isEmpty()) {
            int overflow = Math.max(parchmentCount + toAdd - 64, 0);
            int addable = toAdd - overflow;
            parchmentCount += addable;
            markForUpdate();
            return overflow;
        }
        return toAdd;
    }

    public int getParchmentCount() {
        return parchmentCount;
    }

    public boolean hasParchment() {
        return parchmentCount > 0;
    }

    public void engraveGlass(List<hellfirepvp.astralsorcery.common.constellation.DrawnConstellation> constellations) {
        Level level = getLevel();
        if (level == null) return;
        if (hasParchment() && hasUnengravedGlass()) {
            parchmentCount--;
            ItemInfusedGlass.setEngraving(getGlassStack(),
                    EngravedStarMap.buildStarMap(level, constellations));
            markForUpdate();
        }
    }

    @Nonnull
    public ItemStack setInputStack(@Nonnull ItemStack inputStack) {
        ItemStack prevInput = this.inputStack.copy();
        if (parchmentCount > 0) {
            prevInput = new ItemStack(ItemsAS.PARCHMENT.get(), parchmentCount);
            parchmentCount = 0;
        }
        this.inputStack = inputStack.copy();
        markForUpdate();
        return prevInput;
    }

    @Nonnull
    public ItemStack getInputStack() {
        return hasParchment() ? new ItemStack(ItemsAS.PARCHMENT.get(), getParchmentCount()) : inputStack.copy();
    }

    public static boolean isValidGlassStack(@Nonnull ItemStack glassStack) {
        return !glassStack.isEmpty() && glassStack.getItem() instanceof ItemInfusedGlass;
    }

    @Nonnull
    public ItemStack setGlassStack(@Nonnull ItemStack glassStack) {
        if (!glassStack.isEmpty() && !isValidGlassStack(glassStack)) {
            return ItemStack.EMPTY;
        }
        ItemStack prevStack = this.glassStack;
        this.glassStack = glassStack;
        markForUpdate();
        return prevStack;
    }

    @Nonnull
    public ItemStack getGlassStack() {
        return glassStack;
    }

    public boolean hasUnengravedGlass() {
        return isValidGlassStack(getGlassStack()) &&
                ItemInfusedGlass.getEngraving(getGlassStack()) == null;
    }

    public float getRunProgress() {
        return Mth.clamp(runTick / RUN_TIME, 0F, 1F);
    }

    public void dropContents() {
        Level level = getLevel();
        if (level == null) return;
        Vector3 at = new Vector3(worldPosition).add(0.5, 0.5, 0.5);
        if (!getGlassStack().isEmpty()) {
            ItemUtils.dropItemNaturally(level, at.getX(), at.getY(), at.getZ(), getGlassStack());
            setGlassStack(ItemStack.EMPTY);
        }
        if (!getInputStack().isEmpty()) {
            ItemUtils.dropItemNaturally(level, at.getX(), at.getY(), at.getZ(), getInputStack());
            setInputStack(ItemStack.EMPTY);
        }
        markForUpdate();
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.astralsorcery.refraction_table");
    }

    @Override
    public void readCustomNBT(@Nonnull CompoundTag compound) {
        super.readCustomNBT(compound);
        runTick = compound.getInt("runTick");
        parchmentCount = compound.getInt("parchmentCount");
        inputStack = NBTHelper.getStack(compound, "inputStack");
        glassStack = NBTHelper.getStack(compound, "glassStack");
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.putInt("runTick", runTick);
        compound.putInt("parchmentCount", parchmentCount);
        NBTHelper.setStack(compound, "inputStack", inputStack);
        NBTHelper.setStack(compound, "glassStack", glassStack);
    }
}
