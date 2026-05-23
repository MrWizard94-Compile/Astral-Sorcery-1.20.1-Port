/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.item.armor;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffectRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Constellation Mantle — a chestplate-slot armor piece infused with a
 * specific constellation's power. Each mantle provides:
 * <ul>
 *   <li>Base armor protection (7, equivalent to diamond chestplate)</li>
 *   <li>A passive constellation-specific effect while worn</li>
 *   <li>Enhanced effect at night or under open sky</li>
 * </ul>
 *
 * <p>Mantles are created via liquid infusion of a diamond chestplate
 * with a specific constellation focus.</p>
 *
 * <p>1.16 → 1.20: ArmorItem constructor signature changed. ArmorMaterial
 * is now an interface that must be implemented inline or as a class.
 * The old IArmorMaterial interface was renamed to ArmorMaterial.</p>
 */
public class ItemMantle extends ArmorItem {

    @Nonnull
    private final ResourceLocation constellation;

    public ItemMantle(@Nonnull ResourceLocation constellation) {
        super(MantleArmorMaterial.INSTANCE, Type.CHESTPLATE,
                new Properties().stacksTo(1).durability(528));
        this.constellation = constellation;
    }

    /**
     * Returns the constellation this mantle is attuned to.
     */
    @Nonnull
    public ResourceLocation getConstellation() {
        return constellation;
    }

    /**
     * Returns the {@link MantleEffect} for the given constellation if the entity
     * is wearing a mantle attuned to it, or {@code null} otherwise.
     * Used by mantle effect event handlers to gate their logic.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends MantleEffect> T getEffect(
            @Nullable net.minecraft.world.entity.LivingEntity entity,
            @Nullable IWeakConstellation constellation) {
        if (entity == null || constellation == null) return null;
        net.minecraft.world.item.ItemStack chest =
                entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        if (chest.isEmpty() || !(chest.getItem() instanceof ItemMantle mantle)) return null;
        if (!mantle.constellation.equals(constellation.getRegistryName())) return null;
        return (T) MantleEffectRegistry.getEffect(constellation);
    }

    @Nullable
    @Override
    public String getArmorTexture(@Nonnull ItemStack stack, @Nonnull Entity entity,
                                   @Nonnull EquipmentSlot slot, @Nullable String type) {
        return AstralSorcery.MODID + ":textures/models/armor/mantle_" +
                constellation.getPath() + ".png";
    }

    @Override
    public void onArmorTick(@Nonnull ItemStack stack, @Nonnull Level level,
                            @Nonnull Player player) {
        if (level.isClientSide()) return;
        if (player.tickCount % 20 != 0) return; // Tick effects once per second

        // Subclasses override for constellation-specific effects
        onMantleTick(stack, level, player);
    }

    /**
     * Called once per second when the mantle is worn. Override in subclasses
     * for constellation-specific passive effects.
     */
    protected void onMantleTick(@Nonnull ItemStack stack, @Nonnull Level level,
                                 @Nonnull Player player) {
        // Base mantle has no special effect — subclasses override
    }

    /**
     * Checks if the player is under open sky (for enhanced night effects).
     */
    protected boolean isUnderOpenSky(@Nonnull Player player) {
        return player.level().canSeeSky(player.blockPosition().above());
    }

    /**
     * Checks if it's currently nighttime in the player's dimension.
     */
    protected boolean isNighttime(@Nonnull Level level) {
        long dayTime = level.getDayTime() % 24000;
        return dayTime >= 13000 && dayTime <= 23000;
    }

    /**
     * Custom armor material for constellation mantles.
     * Equivalent to diamond armor for the chestplate slot.
     */
    public enum MantleArmorMaterial implements ArmorMaterial {
        INSTANCE;

        private static final int[] DURABILITY_PER_SLOT = {13, 15, 16, 11}; // feet, legs, chest, head
        private static final int[] DEFENSE_PER_SLOT = {3, 6, 7, 3};

        @Override
        public int getDurabilityForType(@Nonnull Type type) {
            return DURABILITY_PER_SLOT[type.getSlot().getIndex()] * 33;
        }

        @Override
        public int getDefenseForType(@Nonnull Type type) {
            return DEFENSE_PER_SLOT[type.getSlot().getIndex()];
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }

        @Nonnull
        @Override
        public net.minecraft.sounds.SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_DIAMOND;
        }

        @Nonnull
        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY; // Mantles are not repairable via crafting
        }

        @Nonnull
        @Override
        public String getName() {
            return AstralSorcery.MODID + ":mantle";
        }

        @Override
        public float getToughness() {
            return 2.0f;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.0f;
        }
    }
}
