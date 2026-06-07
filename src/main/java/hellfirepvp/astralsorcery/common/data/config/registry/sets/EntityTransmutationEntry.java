/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.config.registry.sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Mapping of one entity type to another for the Pelotrio corruption ritual transmutation.
 *
 * <p>1.16 → 1.20: EntityClassification.MISC → MobCategory.MISC;
 * getRegistryName() → ForgeRegistries.ENTITY_TYPES.getKey().</p>
 */
public class EntityTransmutationEntry {

    private final EntityType<?> fromEntity;
    private final EntityType<?> toEntity;

    public EntityTransmutationEntry(@Nonnull EntityType<?> fromEntity, @Nonnull EntityType<?> toEntity) {
        this.fromEntity = fromEntity;
        this.toEntity = toEntity;
    }

    @Nonnull
    public EntityType<?> getFromEntity() {
        return fromEntity;
    }

    @Nonnull
    public EntityType<?> getToEntity() {
        return toEntity;
    }

    @Nonnull
    public String serialize() {
        ResourceLocation fromKey = ForgeRegistries.ENTITY_TYPES.getKey(fromEntity);
        ResourceLocation toKey   = ForgeRegistries.ENTITY_TYPES.getKey(toEntity);
        return (fromKey != null ? fromKey : "unknown") + ";" + (toKey != null ? toKey : "unknown");
    }

    @Nullable
    public static EntityTransmutationEntry deserialize(@Nonnull String str) {
        String[] split = str.split(";");
        if (split.length != 2) return null;
        EntityType<?> fromType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(split[0]));
        EntityType<?> toType   = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(split[1]));
        if (fromType == null || toType == null) return null;
        if (!toType.canSummon() || toType.getCategory() == MobCategory.MISC) return null;
        return new EntityTransmutationEntry(fromType, toType);
    }
}
