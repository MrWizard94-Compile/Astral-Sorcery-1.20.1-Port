/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.event.effect;

import hellfirepvp.astralsorcery.client.effect.EffectHelper;
import hellfirepvp.astralsorcery.common.data.config.ClientConfig;
import hellfirepvp.astralsorcery.common.starlight.ClientStarlightNetworkCache;
import hellfirepvp.astralsorcery.common.tile.BlockEntityLens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.Color;
import java.util.Map;

/**
 * Every 48 client ticks, spawns {@link hellfirepvp.astralsorcery.client.effect.FXLightbeam}
 * effects between each starlight source and its reachable targets. Beam color is
 * tinted when the source block is a Lens with a colored lens installed.
 *
 * 1.16 → 1.20: ITickHandler → @SubscribeEvent on ClientTickEvent,
 * SyncDataHolder/ClientLightConnections → ClientStarlightNetworkCache,
 * getDistanceSq → distanceToSqr, getEntityWorld → level.
 */
@OnlyIn(Dist.CLIENT)
public class LightbeamRenderHelper {

    private int ticksExisted = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().isPaused()) return;
        if (!ClientConfig.CONFIG.renderStarlightBeams.get()) return;

        if (++ticksExisted < 48) return;
        ticksExisted = 0;

        Minecraft mc = Minecraft.getInstance();
        Entity renderView = mc.getCameraEntity();
        if (renderView == null) renderView = mc.player;
        if (renderView == null) return;
        ClientLevel level = mc.level;
        if (level == null) return;

        double maxDistSq = ClientConfig.CONFIG.besrRenderDistance.get();
        maxDistSq = maxDistSq * maxDistSq;

        final Entity view = renderView;
        final double distSqLimit = maxDistSq;

        for (Map.Entry<BlockPos, ClientStarlightNetworkCache.SourceRenderData> entry :
                ClientStarlightNetworkCache.getAllSources().entrySet()) {

            BlockPos at = entry.getKey();
            double dx = at.getX() + 0.5 - view.getX();
            double dy = at.getY() + 0.5 - view.getY();
            double dz = at.getZ() + 0.5 - view.getZ();
            if (dx * dx + dy * dy + dz * dz > distSqLimit) continue;

            Vec3 source = Vec3.atCenterOf(at);
            Color beamColor = null;

            if (level.getBlockEntity(at) instanceof BlockEntityLens lens) {
                beamColor = lens.getBeamColor();
            }

            for (BlockPos dst : entry.getValue().targets()) {
                Vec3 to = Vec3.atCenterOf(dst);
                if (beamColor != null) {
                    EffectHelper.lightbeam(source, to, beamColor, 0.06f, 50);
                } else {
                    EffectHelper.lightbeamStarlight(source, to, 50);
                }
            }
        }
    }
}
