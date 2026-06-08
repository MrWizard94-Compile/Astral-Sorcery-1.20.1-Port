/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe.altar;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.AttunementUpgradeRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.ConstellationBaseAverageStatsRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.ConstellationBaseItemRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.ConstellationBaseMergeStatsRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.ConstellationBaseNBTCopyRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.ConstellationCopyStatsRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.ConstellationItemRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.ConstellationUpgradeRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.CrystalCountRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.NBTCopyRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin.TraitUpgradeRecipe;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Registry of named {@link SimpleAltarRecipe} sub-types.
 * A {@code Type} wraps a converter function that promotes a plain
 * {@link SimpleAltarRecipe} (as decoded from JSON) into a specialised subclass.
 *
 * <p>1.16 → 1.20: ResourceLocation import moved to net.minecraft.resources;
 * AstralSorcery.key() unchanged.</p>
 */
public class AltarRecipeTypeHandler {

    public static final Type<AttunementUpgradeRecipe> ALTAR_UPGRADE_ATTUNEMENT =
            new Type<>(AstralSorcery.key("attunement_upgrade"));
    public static final Type<ConstellationUpgradeRecipe> ALTAR_UPGRADE_CONSTELLATION =
            new Type<>(AstralSorcery.key("constellation_upgrade"));
    public static final Type<TraitUpgradeRecipe> ALTAR_UPGRADE_TRAIT =
            new Type<>(AstralSorcery.key("trait_upgrade"));

    public static final Type<ConstellationBaseAverageStatsRecipe> CONSTELLATION_CRYSTAL_AVERAGE =
            new Type<>(AstralSorcery.key("constellation_base_average"));
    public static final Type<ConstellationBaseMergeStatsRecipe> CONSTELLATION_CRYSTAL_MERGE =
            new Type<>(AstralSorcery.key("constellation_base_merge"));
    public static final Type<ConstellationBaseItemRecipe> CONSTELLATION_ITEM_BASE =
            new Type<>(AstralSorcery.key("constellation_base"));
    public static final Type<ConstellationItemRecipe> CONSTELLATION_ITEM =
            new Type<>(AstralSorcery.key("constellation_item"));
    public static final Type<CrystalCountRecipe> CRYSTAL_SET_COUNT =
            new Type<>(AstralSorcery.key("crystal_count"));
    public static final Type<ConstellationBaseNBTCopyRecipe> CONSTELLATION_BASE_NBT_COPY =
            new Type<>(AstralSorcery.key("constellation_base_nbt_copy"));
    public static final Type<ConstellationCopyStatsRecipe> CONSTELLATION_COPY_CRYSTAL =
            new Type<>(AstralSorcery.key("constellation_copy_stats"));
    public static final Type<NBTCopyRecipe> NBT_COPY =
            new Type<>(AstralSorcery.key("nbt_copy"));
    public static final Type<SimpleAltarRecipe> DEFAULT =
            new Type<>(AstralSorcery.key("default"));

    private static final Map<ResourceLocation, Type<?>> CONVERTER_MAP = new HashMap<>();

    public static <T extends SimpleAltarRecipe> Type<T> registerConverter(
            @Nonnull ResourceLocation name, @Nonnull Function<SimpleAltarRecipe, T> converter) {
        Type<T> type = new Type<>(name, converter);
        CONVERTER_MAP.put(name, type);
        return type;
    }

    private static <T extends SimpleAltarRecipe> void registerInternal(
            @Nonnull Type<T> type, @Nonnull Function<SimpleAltarRecipe, T> converter) {
        type.converter = converter;
        CONVERTER_MAP.put(type.key, type);
    }

    @SuppressWarnings("unchecked")
    public static <T extends SimpleAltarRecipe> T convert(
            @Nonnull SimpleAltarRecipe recipe, @Nonnull ResourceLocation alternativeBase) {
        return (T) CONVERTER_MAP.getOrDefault(alternativeBase, DEFAULT).convert(recipe);
    }

    public static void init() {
        registerInternal(ALTAR_UPGRADE_ATTUNEMENT,    AttunementUpgradeRecipe::convertToThis);
        registerInternal(ALTAR_UPGRADE_CONSTELLATION, ConstellationUpgradeRecipe::convertToThis);
        registerInternal(ALTAR_UPGRADE_TRAIT,         TraitUpgradeRecipe::convertToThis);

        registerInternal(CONSTELLATION_CRYSTAL_AVERAGE, ConstellationBaseAverageStatsRecipe::convertToThis);
        registerInternal(CONSTELLATION_CRYSTAL_MERGE,   ConstellationBaseMergeStatsRecipe::convertToThis);
        registerInternal(CONSTELLATION_ITEM_BASE,       ConstellationBaseItemRecipe::convertToThis);
        registerInternal(CONSTELLATION_ITEM,            ConstellationItemRecipe::convertToThis);
        registerInternal(CRYSTAL_SET_COUNT,             CrystalCountRecipe::convertToThis);
        registerInternal(CONSTELLATION_BASE_NBT_COPY,   ConstellationBaseNBTCopyRecipe::convertToThis);
        registerInternal(CONSTELLATION_COPY_CRYSTAL,    ConstellationCopyStatsRecipe::convertToThis);
        registerInternal(NBT_COPY,                      NBTCopyRecipe::convertToThis);
        registerInternal(DEFAULT,                       Function.identity());
    }

    public static class Type<T extends SimpleAltarRecipe> {

        private final ResourceLocation key;
        private Function<SimpleAltarRecipe, T> converter;

        private Type(@Nonnull ResourceLocation key) {
            this.key = key;
        }

        private Type(@Nonnull ResourceLocation key, @Nonnull Function<SimpleAltarRecipe, T> converter) {
            this.key = key;
            this.converter = converter;
        }

        public T convert(@Nonnull SimpleAltarRecipe recipe) {
            return this.converter.apply(recipe);
        }

        @Nonnull
        public ResourceLocation getKey() {
            return key;
        }
    }
}
