/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Mantle effect for Vicio (Major — Movement).
 *
 * <p>While wearing the Vicio mantle the player may fly. Once per second of active
 * flight, {@code chargeCost} alignment charge is consumed. If charge runs out,
 * the flight ability is revoked until charge is available again.</p>
 *
 * <p>1.16 → 1.20: player.abilities.allowFlying → player.getAbilities().mayfly,
 * player.abilities.isFlying → player.getAbilities().flying,
 * player.sendPlayerAbilities() → player.onUpdateAbilities(),
 * KeyMantleFlight perk check and EventHelperTemporaryFlight deferred (perk system
 * is not yet wired for flight).</p>
 */
public class MantleEffectVicio extends MantleEffect {

    public static final VicioConfig CONFIG = new VicioConfig();

    public MantleEffectVicio() {
        super(ConstellationsAS.VICIO);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {}

    @Override
    protected boolean usesTickMethods() {
        return true;
    }

    @Override
    protected void tickServer(@Nonnull Player player) {
        boolean hasCharge = AlignmentChargeHandler.INSTANCE.hasCharge(
                player, LogicalSide.SERVER, CONFIG.chargeCost.get());

        if (hasCharge) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            // Drain charge once per second while actively flying
            if (player.getAbilities().flying && !player.onGround()
                    && player.tickCount % 20 == 0) {
                AlignmentChargeHandler.INSTANCE.drainCharge(
                        player, LogicalSide.SERVER, CONFIG.chargeCost.get(), false);
            }
        } else {
            if (player.getAbilities().mayfly && !player.getAbilities().instabuild) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class VicioConfig extends Config {

        public ForgeConfigSpec.IntValue chargeCost;

        public VicioConfig() {
            super("vicio");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.vicio");
            chargeCost = b
                    .comment("Alignment charge consumed per second of active flight.")
                    .defineInRange("chargeCost", 100, 1, 500);
            b.pop();
            b.build();
        }
    }
}
