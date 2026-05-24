/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
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
 * player.sendPlayerAbilities() → player.onUpdateAbilities().</p>
 */
public class MantleEffectVicio extends MantleEffect {

    public static final VicioConfig CONFIG = new VicioConfig();

    public MantleEffectVicio() {
        super(ConstellationsAS.VICIO);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {
        bus.addListener(this::onEquipmentChange);
    }

    private void onEquipmentChange(@Nonnull LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.CHEST) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (player.getAbilities().instabuild) return;
        // If the new chest item is NOT a Vicio mantle, revoke any flight we granted
        if (!(event.getTo().getItem() instanceof ItemMantle mantle)
                || !mantle.getConstellation().equals(ConstellationsAS.VICIO.getRegistryName())) {
            if (player.getAbilities().mayfly) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    protected boolean usesTickMethods() {
        return true;
    }

    private static boolean hasMantleFlightPerk(@Nonnull Player player) {
        AbstractPerk perk = PerkTree.getPerk(AstralSorcery.key("key_mantle_flight"));
        if (perk == null) return false;
        PlayerProgress progress = PlayerProgressHelper.getProgress(player);
        return progress != null && progress.hasPerkAllocated(perk);
    }

    @Override
    protected void tickServer(@Nonnull Player player) {
        if (!hasMantleFlightPerk(player)) {
            if (player.getAbilities().mayfly && !player.getAbilities().instabuild) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            return;
        }

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
