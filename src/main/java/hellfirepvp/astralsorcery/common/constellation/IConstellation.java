package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.constellation.engraving.EngravingEffect;
import hellfirepvp.astralsorcery.common.constellation.engraving.EngravingEffectRegistry;
import hellfirepvp.astralsorcery.common.constellation.star.StarConnection;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.tags.TagKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;

/**
 * Core constellation interface. Defines the contract for all constellation types
 * (major, weak, minor).
 *
 * <p>1.16 → 1.20 changes:
 * Removed IForgeRegistryEntry (removed in 1.20) — getRegistryName/setRegistryName
 * are now own methods on this interface.
 * IFormattableTextComponent → MutableComponent,
 * TranslationTextComponent → Component.translatable(),
 * CompoundNBT → CompoundTag,
 * PlayerEntity → Player,
 * IItemProvider → ItemLike,
 * ITag&lt;Item&gt; → TagKey&lt;Item&gt;,
 * Ingredient.fromStacks → Ingredient.of,
 * Ingredient.fromItems → Ingredient.of,
 * Ingredient.fromTag → Ingredient.of</p>
 */
public interface IConstellation extends Comparable<IConstellation> {

    // 0-indexed
    int STAR_GRID_INDEX = 31;
    // 1-indexed
    int STAR_GRID_WIDTH_HEIGHT = (STAR_GRID_INDEX + 1);

    /**
     * Get the registry name for this constellation.
     * In 1.16 this came from IForgeRegistryEntry; in 1.20 it is stored directly.
     */
    @Nonnull
    ResourceLocation getRegistryName();

    /**
     * Should only be called before registering the Constellation.
     * Throws {@link IllegalArgumentException} if the position already exists.
     */
    @Nonnull
    StarLocation addStar(int x, int y);

    /**
     * Should only be called before registering the Constellation.
     */
    @Nullable
    StarConnection addConnection(@Nonnull StarLocation star1, @Nonnull StarLocation star2);

    int getSortingId();

    @Nonnull
    List<StarLocation> getStars();

    @Nonnull
    List<StarConnection> getStarConnections();

    @Nonnull
    String getSimpleName();

    @Nonnull
    String getTranslationKey();

    @Nonnull
    default MutableComponent getConstellationName() {
        return Component.translatable(this.getTranslationKey());
    }

    @Nonnull
    default MutableComponent getConstellationTypeDescription() {
        String type = "unknown";
        if (this instanceof IMajorConstellation) {
            type = "major";
        } else if (this instanceof IWeakConstellation) {
            type = "weak";
        } else if (this instanceof IMinorConstellation) {
            type = "minor";
        }
        return Component.translatable(String.format("astralsorcery.journal.constellation.type.%s", type));
    }

    @Nonnull
    default MutableComponent getConstellationTag() {
        return Component.translatable(this.getTranslationKey() + ".tag");
    }

    @Nonnull
    default MutableComponent getConstellationDescription() {
        return Component.translatable(this.getTranslationKey() + ".description");
    }

    @Nonnull
    default MutableComponent getConstellationEnchantmentDescription() {
        return Component.translatable(this.getTranslationKey() + ".enchantments");
    }

    @Nonnull
    static String getDefaultSaveKey() {
        return "constellationName";
    }

    @Nonnull
    List<Ingredient> getConstellationSignatureItems();

    /**
     * Get the engraving effect for this constellation.
     * Returns null until the engraving effect registry is populated.
     */
    @Nullable
    default EngravingEffect getEngravingEffect() {
        return EngravingEffectRegistry.get(this.getRegistryName());
    }

    @Nonnull
    default IConstellation addSignatureItem(@Nonnull ItemStack item) {
        return this.addSignatureItem(() -> Ingredient.of(item));
    }

    @Nonnull
    default IConstellation addSignatureItem(@Nonnull ItemLike item) {
        return this.addSignatureItem(() -> Ingredient.of(item));
    }

    @Nonnull
    default IConstellation addSignatureItem(@Nonnull TagKey<Item> tag) {
        return this.addSignatureItem(() -> Ingredient.of(tag));
    }

    @Nonnull
    IConstellation addSignatureItem(@Nonnull Supplier<Ingredient> ingredient);

    @Nonnull
    Color getConstellationColor();

    @Nonnull
    default Color getTierRenderColor() {
        if (this instanceof IMinorConstellation) {
            return ColorsAS.CONSTELLATION_TYPE_MINOR;
        }
        if (this instanceof IMajorConstellation) {
            return ColorsAS.CONSTELLATION_TYPE_MAJOR;
        }
        return ColorsAS.CONSTELLATION_TYPE_WEAK;
    }

    boolean canDiscover(@Nullable Player player, @Nonnull PlayerProgress progress);

    default void writeToNBT(@Nonnull CompoundTag compound) {
        writeToNBT(compound, getDefaultSaveKey());
    }

    default void writeToNBT(@Nonnull CompoundTag compound, @Nonnull String key) {
        compound.putString(key, getRegistryName().toString());
    }

    @Nullable
    static IConstellation readFromNBT(@Nonnull CompoundTag compound) {
        return readFromNBT(compound, getDefaultSaveKey());
    }

    @Nullable
    static IConstellation readFromNBT(@Nonnull CompoundTag compound, @Nonnull String key) {
        ResourceLocation rl = ResourceLocation.tryParse(compound.getString(key));
        return rl != null ? ConstellationRegistry.getConstellation(rl) : null;
    }
}
