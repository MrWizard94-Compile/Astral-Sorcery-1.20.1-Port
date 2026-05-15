package hellfirepvp.astralsorcery.common.crystal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * Crystal properties — size, purity, and cutting — that determine
 * crystal effectiveness in various roles (collection, transmission, crafting).
 *
 * <p>Properties are stored as integer percentages [0, 100].</p>
 *
 * <p>1.16 -> 1.20 changes:
 * CompoundNBT -> CompoundTag, PacketBuffer -> FriendlyByteBuf,
 * MathHelper -> Mth</p>
 */
public class CrystalProperties {

    public static final int MAX_SIZE = 900;
    public static final int MAX_PURITY = 100;
    public static final int MAX_CUTTING = 100;

    private static final String TAG_KEY = "crystalProperties";

    private int size;
    private int purity;
    private int cutting;

    public CrystalProperties(int size, int purity, int cutting) {
        this.size = Mth.clamp(size, 0, MAX_SIZE);
        this.purity = Mth.clamp(purity, 0, MAX_PURITY);
        this.cutting = Mth.clamp(cutting, 0, MAX_CUTTING);
    }

    /**
     * Generate random crystal properties for a freshly mined crystal.
     */
    @Nonnull
    public static CrystalProperties createRandomly(@Nonnull Random rand) {
        int size = 200 + rand.nextInt(400);  // 200-599
        int purity = 40 + rand.nextInt(50);   // 40-89
        int cutting = 20 + rand.nextInt(60);  // 20-79
        return new CrystalProperties(size, purity, cutting);
    }

    /**
     * Create maximum quality crystal properties (for creative/testing).
     */
    @Nonnull
    public static CrystalProperties createMaximum() {
        return new CrystalProperties(MAX_SIZE, MAX_PURITY, MAX_CUTTING);
    }

    // ---- Getters ----

    public int getSize() {
        return size;
    }

    public int getPurity() {
        return purity;
    }

    public int getCutting() {
        return cutting;
    }

    /**
     * Get the collection multiplier (for collector crystals).
     * Based on size and purity.
     */
    public float getCollectionMultiplier() {
        return ((float) size / MAX_SIZE) * ((float) purity / MAX_PURITY);
    }

    /**
     * Get the transmission loss factor (for lenses/prisms).
     * Based on cutting quality.
     */
    public float getTransmissionLoss() {
        return 1.0F - ((float) cutting / MAX_CUTTING) * 0.9F;
    }

    /**
     * Get the effective tool durability multiplier.
     */
    public float getDurabilityMultiplier() {
        return (float) size / (MAX_SIZE / 2.0F);
    }

    // ---- Modification ----

    /**
     * Damage the crystal (reduce size). Returns true if crystal is destroyed.
     */
    public boolean damage(int amount) {
        this.size -= amount;
        return this.size <= 0;
    }

    /**
     * Grow the crystal (increase size up to max).
     */
    public void grow(int amount) {
        this.size = Math.min(MAX_SIZE, this.size + amount);
    }

    // ---- Serialization ----

    @Nonnull
    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("size", size);
        tag.putInt("purity", purity);
        tag.putInt("cutting", cutting);
        return tag;
    }

    @Nonnull
    public static CrystalProperties readFromNBT(@Nonnull CompoundTag tag) {
        return new CrystalProperties(
                tag.getInt("size"),
                tag.getInt("purity"),
                tag.getInt("cutting"));
    }

    public void writeToBuffer(@Nonnull FriendlyByteBuf buf) {
        buf.writeInt(size);
        buf.writeInt(purity);
        buf.writeInt(cutting);
    }

    @Nonnull
    public static CrystalProperties readFromBuffer(@Nonnull FriendlyByteBuf buf) {
        return new CrystalProperties(buf.readInt(), buf.readInt(), buf.readInt());
    }

    // ---- ItemStack integration ----

    /**
     * Write crystal properties to an item stack's NBT.
     */
    public static void setOnStack(@Nonnull ItemStack stack, @Nonnull CrystalProperties properties) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TAG_KEY, properties.writeToNBT());
    }

    /**
     * Read crystal properties from an item stack's NBT.
     */
    @Nullable
    public static CrystalProperties getFromStack(@Nonnull ItemStack stack) {
        if (!stack.hasTag()) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_KEY)) {
            return null;
        }
        return readFromNBT(tag.getCompound(TAG_KEY));
    }

    /**
     * Check if a stack has crystal properties.
     */
    public static boolean hasProperties(@Nonnull ItemStack stack) {
        return stack.hasTag() && stack.getTag() != null && stack.getTag().contains(TAG_KEY);
    }

    @Override
    public String toString() {
        return "CrystalProperties{size=" + size + ", purity=" + purity + ", cutting=" + cutting + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrystalProperties other)) return false;
        return size == other.size && purity == other.purity && cutting == other.cutting;
    }

    @Override
    public int hashCode() {
        int result = size;
        result = 31 * result + purity;
        result = 31 * result + cutting;
        return result;
    }
}
