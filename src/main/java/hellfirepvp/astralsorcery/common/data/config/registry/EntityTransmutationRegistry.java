/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.data.config.registry;

import hellfirepvp.astralsorcery.common.data.config.registry.sets.EntityTransmutationEntry;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry of entity type transmutation mappings for the corrupted Pelotrio ritual.
 *
 * <p>1.16 → 1.20: CompoundNBT → CompoundTag; ServerWorld → ServerLevel;
 * writeWithoutTypeId → saveWithoutId; read → load; removeEntity → discard;
 * ForgeRegistries.ENTITIES → ForgeRegistries.ENTITY_TYPES.</p>
 */
public final class EntityTransmutationRegistry {

    public static final EntityTransmutationRegistry INSTANCE = new EntityTransmutationRegistry();

    private List<EntityTransmutationEntry> entries;

    private EntityTransmutationRegistry() {
        this.entries = buildDefaults();
    }

    // =========================================================================
    // Public API
    // =========================================================================

    public List<EntityTransmutationEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void setEntries(List<EntityTransmutationEntry> entries) {
        this.entries = new ArrayList<>(entries);
    }

    @Nullable
    public EntityType<?> getTransmuteTo(@Nonnull EntityType<?> from) {
        return entries.stream()
                .filter(e -> e.getFromEntity().equals(from))
                .map(EntityTransmutationEntry::getToEntity)
                .findFirst()
                .orElse(null);
    }

    /**
     * Transmutes {@code entity} to its configured target type.
     * Removes the original entity from the world and spawns the replacement.
     *
     * @return the new entity, or {@code null} if no mapping exists or spawning fails
     */
    @Nullable
    public LivingEntity transmuteEntity(@Nonnull ServerLevel level, @Nonnull LivingEntity entity) {
        EntityType<?> targetType = getTransmuteTo(entity.getType());
        if (targetType == null) return null;
        try {
            CompoundTag tag = entity.saveWithoutId(new CompoundTag());
            entity.discard();
            NBTHelper.removeUUID(tag, "UUID");
            Entity spawned = targetType.create(level);
            if (!(spawned instanceof LivingEntity living)) return null;
            living.load(tag);
            return living;
        } catch (Exception ignored) {
            return null;
        }
    }

    // =========================================================================
    // Defaults
    // =========================================================================

    private static List<EntityTransmutationEntry> buildDefaults() {
        List<EntityTransmutationEntry> list = new ArrayList<>();
        add(list, EntityType.SKELETON,  EntityType.WITHER_SKELETON);
        add(list, EntityType.VILLAGER,  EntityType.WITCH);
        add(list, EntityType.PIG,       EntityType.ZOMBIFIED_PIGLIN);
        add(list, EntityType.COW,       EntityType.ZOMBIE);
        add(list, EntityType.PARROT,    EntityType.GHAST);
        add(list, EntityType.CHICKEN,   EntityType.BLAZE);
        add(list, EntityType.SHEEP,     EntityType.STRAY);
        add(list, EntityType.HORSE,     EntityType.SKELETON_HORSE);
        return list;
    }

    private static void add(List<EntityTransmutationEntry> list,
                             EntityType<?> from, EntityType<?> to) {
        list.add(new EntityTransmutationEntry(from, to));
    }
}
