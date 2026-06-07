/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.crafting.nojson.attunement.active;

import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunePlayerRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunementRecipe;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktAttunementActive;
import hellfirepvp.astralsorcery.common.network.play.server.PktParticleEvent;
import hellfirepvp.astralsorcery.common.tile.BlockEntityAttunementAltar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Server-side active state for player attunement in the attunement altar.
 */
public class ActivePlayerAttunementRecipe extends AttunementRecipe.Active<AttunePlayerRecipe> {

    private static final int DURATION = 800;

    @Nullable
    private ResourceLocation constellation;
    @Nullable
    private UUID playerUUID;

    public ActivePlayerAttunementRecipe(@Nonnull AttunePlayerRecipe recipe,
                                        @Nullable ResourceLocation constellation,
                                        @Nullable UUID playerUUID) {
        super(recipe);
        this.constellation = constellation;
        this.playerUUID = playerUUID;
    }

    public ActivePlayerAttunementRecipe(@Nonnull AttunePlayerRecipe recipe,
                                        @Nonnull CompoundTag nbt) {
        super(recipe);
        this.readFromNBT(nbt);
    }

    @Override
    public void startCrafting(@Nonnull BlockEntityAttunementAltar altar) {
        UUID uuid = playerUUID;
        if (uuid == null) return;
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv == null) return;
        Player player = srv.getPlayerList().getPlayer(uuid);
        if (player instanceof ServerPlayer sp && player.isAlive()) {
            double x = altar.getBlockPos().getX() + 0.5;
            double y = altar.getBlockPos().getY() + 1.2;
            double z = altar.getBlockPos().getZ() + 0.5;
            sp.connection.teleport(x, y, z, 0f, 0f);
            sp.setInvulnerable(true);
            PacketChannel.sendToPlayer(new PktAttunementActive(true, altar.getBlockPos()), sp);
        }
    }

    @Override
    public void stopCrafting(@Nonnull BlockEntityAttunementAltar altar) {
        UUID uuid = playerUUID;
        if (uuid == null) return;
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv == null) return;
        Player player = srv.getPlayerList().getPlayer(uuid);
        if (player instanceof ServerPlayer sp) {
            sp.setInvulnerable(false);
            PacketChannel.sendToPlayer(new PktAttunementActive(false, altar.getBlockPos()), sp);
        }
    }

    @Override
    public boolean isFinished(@Nonnull BlockEntityAttunementAltar altar) {
        return getTick() >= DURATION;
    }

    @Override
    public void finishRecipe(@Nonnull BlockEntityAttunementAltar altar) {
        ResourceLocation cst = constellation;
        UUID uuid = playerUUID;
        if (cst == null || uuid == null) return;
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv == null) return;
        Player player = srv.getPlayerList().getPlayer(uuid);
        if (player instanceof ServerPlayer sp) {
            ResearchManager.attuneTo(sp, cst);
        }
        Level level = altar.getLevel();
        if (level instanceof ServerLevel sl) {
            PacketChannel.sendToAllTracking(
                    new PktParticleEvent(PktParticleEvent.CONSTELLATION_DISCOVER, altar.getBlockPos()),
                    sl, altar.getBlockPos());
        }
    }

    @Override
    public void doTick(@Nonnull LogicalSide side, @Nonnull BlockEntityAttunementAltar altar) {
        if (side.isClient()) return;
        Level level = altar.getLevel();
        if (level == null) return;

        UUID uuid = playerUUID;
        if (uuid != null) {
            MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
            if (srv != null) {
                Player player = srv.getPlayerList().getPlayer(uuid);
                if (player != null && player.isAlive()) {
                    player.setInvulnerable(true);
                }
            }
        }

        if (getTick() % 10 == 0) {
            PacketChannel.sendToAllTracking(
                    new PktParticleEvent(PktParticleEvent.ATTUNEMENT_BEAM, altar.getBlockPos()),
                    (ServerLevel) level, altar.getBlockPos());
        }
    }

    @Override
    public void writeToNBT(@Nonnull CompoundTag nbt) {
        super.writeToNBT(nbt);
        if (constellation != null) {
            nbt.putString("constellation", constellation.toString());
        }
        if (playerUUID != null) {
            nbt.putUUID("player", playerUUID);
        }
    }

    @Override
    protected void readFromNBT(@Nonnull CompoundTag nbt) {
        super.readFromNBT(nbt);
        if (nbt.contains("constellation")) {
            constellation = ResourceLocation.tryParse(nbt.getString("constellation"));
        }
        if (nbt.hasUUID("player")) {
            playerUUID = nbt.getUUID("player");
        }
    }
}
