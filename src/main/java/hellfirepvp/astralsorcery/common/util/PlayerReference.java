package hellfirepvp.astralsorcery.common.util;

import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable reference to a player by UUID + display name.
 * Used to persist player identity in NBT and sync across the network.
 *
 * <p>1.16 → 1.20 changes:
 * PlayerEntity → Player, ServerPlayerEntity → ServerPlayer,
 * getUniqueID() → getUUID(), IFormattableTextComponent → MutableComponent,
 * ITextComponent → Component, StringTextComponent → Component.literal(),
 * CompoundNBT → CompoundTag, PacketBuffer → FriendlyByteBuf,
 * putUniqueId/getUniqueId → putUUID/getUUID,
 * ITextComponent.Serializer.toJson/getComponentFromJson → Component.Serializer.toJson/fromJson,
 * LogicalSidedProvider → ServerLifecycleHooks,
 * getPlayerByUUID → getPlayer</p>
 */
public class PlayerReference {

    @Nonnull
    private final UUID playerUUID;
    @Nonnull
    private final MutableComponent playerName;

    public PlayerReference(@Nonnull UUID playerUUID, @Nonnull MutableComponent playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
    }

    @Nonnull
    public static PlayerReference of(@Nonnull Player player) {
        Component txt = player.getDisplayName();
        if (txt instanceof MutableComponent mutable) {
            return new PlayerReference(player.getUUID(), mutable);
        }
        return new PlayerReference(player.getUUID(), Component.literal("").append(txt));
    }

    public boolean isPlayer(@Nonnull Player player) {
        return this.getPlayerUUID().equals(player.getUUID());
    }

    @Nonnull
    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    @Nonnull
    public Component getPlayerName() {
        return this.playerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerReference that = (PlayerReference) o;
        return Objects.equals(playerUUID, that.playerUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUUID);
    }

    @Nullable
    public ServerPlayer getOnlinePlayer() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            throw new IllegalArgumentException(
                    "Called getOnlinePlayer on clientside or while no server is running!");
        }
        return server.getPlayerList().getPlayer(this.playerUUID);
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        this.writeToNBT(tag);
        return tag;
    }

    public void writeToNBT(@Nonnull CompoundTag tag) {
        tag.putUUID("playerUUID", this.playerUUID);
        tag.putString("playerName", Component.Serializer.toJson(this.playerName));
    }

    public void write(@Nonnull FriendlyByteBuf buf) {
        ByteBufUtils.writeUUID(buf, this.playerUUID);
        ByteBufUtils.writeTextComponent(buf, this.playerName);
    }

    @Nonnull
    public static PlayerReference deserialize(@Nonnull CompoundTag tag) {
        UUID uuid = tag.getUUID("playerUUID");
        MutableComponent name = Component.Serializer.fromJson(tag.getString("playerName"));
        if (name == null) {
            name = Component.literal("Unknown");
        }
        return new PlayerReference(uuid, name);
    }

    @Nonnull
    public static PlayerReference read(@Nonnull FriendlyByteBuf buf) {
        return new PlayerReference(ByteBufUtils.readUUID(buf), ByteBufUtils.readTextComponent(buf));
    }
}
