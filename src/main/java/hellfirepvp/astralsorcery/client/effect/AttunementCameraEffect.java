package hellfirepvp.astralsorcery.client.effect;

import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

/**
 * Client-side camera effect for player attunement at the Attunement Altar.
 *
 * <p>When active, the yaw rotates smoothly (1.8°/tick, one orbit per ~200 ticks)
 * and pitches slightly upward to create a dramatic orbital camera motion.
 * Hand rendering is suppressed during the effect.</p>
 *
 * <p>Activated/deactivated by {@link hellfirepvp.astralsorcery.common.network.play.server.PktAttunementActive}
 * when the server starts or stops the attunement recipe.</p>
 */
@OnlyIn(Dist.CLIENT)
public class AttunementCameraEffect {

    public static final AttunementCameraEffect INSTANCE = new AttunementCameraEffect();

    private static final float YAW_PER_TICK  = 1.8f;   // ~200 ticks per orbit
    private static final float TARGET_PITCH  = -25f;   // tilt upward
    private static final float PITCH_SPEED   = 0.5f;   // degrees per tick toward target

    private boolean active = false;
    private float   orbitYawOffset  = 0f;
    private float   pitchOffset     = 0f;

    private AttunementCameraEffect() {}

    public void startAttunement(@Nullable BlockPos pos) {
        active = true;
        orbitYawOffset = 0f;
        pitchOffset    = 0f;
    }

    public void stopAttunement() {
        active = false;
        orbitYawOffset = 0f;
        pitchOffset    = 0f;
    }

    public boolean isActive() { return active; }

    // Advance the animation each client tick (called from ClientRenderEventHandler)
    public void tick() {
        if (!active) return;
        orbitYawOffset += YAW_PER_TICK;
        if (orbitYawOffset >= 360f) orbitYawOffset -= 360f;
        // Ease pitch toward TARGET_PITCH
        float diff = TARGET_PITCH - pitchOffset;
        pitchOffset += diff * PITCH_SPEED * 0.1f;
    }

    @SubscribeEvent
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!active) return;
        event.setYaw(event.getYaw() + orbitYawOffset);
        event.setPitch(event.getPitch() + pitchOffset);
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        if (active) event.setCanceled(true);
    }
}
