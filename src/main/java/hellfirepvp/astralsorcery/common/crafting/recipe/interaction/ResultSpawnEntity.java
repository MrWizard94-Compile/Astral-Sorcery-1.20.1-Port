/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.recipe.interaction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Interaction result that spawns an entity at the reaction point.
 *
 * <p>1.16 → 1.20: World → Level, PacketBuffer → FriendlyByteBuf,
 * setLocationAndAngles → moveTo, addEntity → addFreshEntity,
 * ForgeRegistries.ENTITIES → ForgeRegistries.ENTITY_TYPES,
 * JSONUtils → GsonHelper, world.rand → level.random; JEI methods removed.</p>
 */
public class ResultSpawnEntity extends InteractionResult {

    private EntityType<?> entityType;

    ResultSpawnEntity() {
        super(InteractionResultRegistry.ID_SPAWN_ENTITY);
    }

    public static ResultSpawnEntity spawnEntity(EntityType<?> type) {
        if (!type.canSummon()) {
            throw new IllegalArgumentException("EntityType " + ForgeRegistries.ENTITY_TYPES.getKey(type) + " is not summonable!");
        }
        ResultSpawnEntity result = new ResultSpawnEntity();
        result.entityType = type;
        return result;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    @Override
    public void doResult(Level level, Vector3 at) {
        Entity e = this.entityType.create(level);
        if (!(e instanceof LivingEntity)) {
            return;
        }
        e.moveTo(at.getX(), at.getY(), at.getZ(), level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(e);
    }

    @Override
    public void read(JsonObject json) throws JsonParseException {
        ResourceLocation key = new ResourceLocation(GsonHelper.getAsString(json, "entityType"));
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(key);
        if (type == null) {
            throw new JsonParseException("Unknown entity type: " + key);
        }
        this.entityType = type;
    }

    @Override
    public void write(JsonObject json) {
        json.addProperty("entityType", ForgeRegistries.ENTITY_TYPES.getKey(this.entityType).toString());
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.entityType = ByteBufUtils.readRegistryEntry(buf, ForgeRegistries.ENTITY_TYPES);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        ByteBufUtils.writeRegistryEntry(buf, ForgeRegistries.ENTITY_TYPES, this.entityType);
    }
}
