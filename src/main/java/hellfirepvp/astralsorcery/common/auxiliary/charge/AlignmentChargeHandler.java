/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.auxiliary.charge;

import hellfirepvp.astralsorcery.common.constellation.world.CelestialHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-player alignment charge — the secondary resource consumed by
 * mantle constellation effects and certain perks.
 *
 * <p>Charge regenerates each player tick, scaled by time-of-day and
 * whether the player is underground. Charge is lost when effects are used
 * (via {@link #drainCharge}) and is capped at {@link #MAX_CHARGE}.</p>
 *
 * <p>1.16 → 1.20 changes:
 * Replaced observerlib ITickHandler with @SubscribeEvent PlayerTickEvent;
 * PlayerEntity → Player, getUniqueID → getUUID,
 * getEntityWorld → level(), getPosition → blockPosition(),
 * DayTimeHelper → CelestialHandler, MathHelper → Mth,
 * Heightmap.Type → Heightmap.Types.
 * Perk attribute scaling of max/regen is not yet wired
 * (PerkAttributeTypesAS.ATTR_TYPE_ALIGNMENT_CHARGE_* not yet registered).
 * Client sync (PktSyncCharge) deferred.</p>
 */
public class AlignmentChargeHandler {

    public static final AlignmentChargeHandler INSTANCE = new AlignmentChargeHandler();
    public static final float MAX_CHARGE = 1000F;

    private static final Map<LogicalSide, Map<UUID, Float>> maximumCharge = new HashMap<>();
    private static final Map<LogicalSide, Map<UUID, Float>> currentCharge = new HashMap<>();

    private AlignmentChargeHandler() {}

    // ---- Query ----

    public float getMaximumCharge(Player player, LogicalSide side) {
        return maximumCharge.computeIfAbsent(side, s -> new HashMap<>())
                .computeIfAbsent(player.getUUID(), uuid -> MAX_CHARGE);
    }

    public float getCurrentCharge(Player player, LogicalSide side) {
        if (player.isCreative() || player.isSpectator()) {
            return getMaximumCharge(player, side);
        }
        return currentCharge.computeIfAbsent(side, s -> new HashMap<>())
                .computeIfAbsent(player.getUUID(), uuid -> MAX_CHARGE);
    }

    public float getFilledPercentage(Player player, LogicalSide side) {
        if (player.isCreative() || player.isSpectator()) return 1F;
        float max = getMaximumCharge(player, side);
        float cur = getCurrentCharge(player, side);
        return Mth.clamp(cur / max, 0F, 1F);
    }

    public boolean hasCharge(Player player, LogicalSide side, float charge) {
        if (player.isCreative() || player.isSpectator()) return true;
        return getCurrentCharge(player, side) >= charge;
    }

    /**
     * Attempts to drain {@code charge} from the player.
     * If {@code simulate} is true the drain is not actually applied.
     * Returns true if sufficient charge was available.
     */
    public boolean drainCharge(Player player, LogicalSide side, float charge, boolean simulate) {
        if (player.isCreative() || player.isSpectator()) return true;
        float current = getCurrentCharge(player, side);
        if (current < charge) return false;
        if (!simulate) {
            float result = Mth.clamp(current - charge, 0F, getMaximumCharge(player, side));
            currentCharge.computeIfAbsent(side, s -> new HashMap<>()).put(player.getUUID(), result);
        }
        return true;
    }

    // ---- Tick ----

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        LogicalSide side = event.side;

        float current = getCurrentCharge(player, side);
        float max = getMaximumCharge(player, side);
        if (current >= max) return;

        Level level = player.level();
        float regenPerTick = max / (6F * 20F);

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, player.blockPosition().getX(), player.blockPosition().getZ());
        boolean underground = surfaceY > player.blockPosition().getY() + 1;

        float dayMultiplier = underground ? 0.85F : 0.3F + 0.7F * CelestialHandler.getTimeOfDayFactor(level);
        float caveMultiplier = underground ? 0.25F : 1F;

        regenPerTick *= dayMultiplier;
        regenPerTick *= caveMultiplier;

        current = Math.min(current + regenPerTick, max);
        currentCharge.computeIfAbsent(side, s -> new HashMap<>()).put(player.getUUID(), current);
    }
}
