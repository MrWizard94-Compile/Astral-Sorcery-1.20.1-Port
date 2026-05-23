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
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * Mantle effect for Armara (Major — Protection).
 *
 * <p>Builds up to {@code immunityStacks} damage-immunity tokens. Each token is consumed
 * to fully cancel one incoming hit (except creative-kill / void damage). A new token
 * recharges after {@code immunityRechargeTicks} ticks if the player has enough charge.</p>
 *
 * <p>1.16 → 1.20: DamageSource.canHarmInCreative() → source.is(DamageTypeTags.BYPASSES_INVULNERABILITY),
 * event.getEntityLiving() → event.getEntity(), getEntityWorld() → level().</p>
 */
public class MantleEffectArmara extends MantleEffect {

    public static final ArmaraConfig CONFIG = new ArmaraConfig();

    public MantleEffectArmara() {
        super(ConstellationsAS.ARMARA);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {
        bus.addListener(EventPriority.HIGH, this::onHurt);
    }

    @Override
    protected boolean usesTickMethods() {
        return true;
    }

    @Override
    protected void tickServer(@Nonnull Player player) {
        if (getCurrentImmunityStacks(player) >= CONFIG.immunityStacks.get()) {
            setCurrentImmunityRechargeTick(player, CONFIG.immunityRechargeTicks.get());
            return;
        }
        int tick = getCurrentImmunityRechargeTick(player) - 1;
        if (tick <= 0) {
            if (AlignmentChargeHandler.INSTANCE.hasCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerStack.get())) {
                setCurrentImmunityRechargeTick(player, CONFIG.immunityRechargeTicks.get());
                setCurrentImmunityStacks(player, getCurrentImmunityStacks(player) + 1);
                AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerStack.get(), false);
            }
        } else {
            setCurrentImmunityRechargeTick(player, tick);
        }
    }

    private void onHurt(LivingHurtEvent event) {
        LivingEntity hurt = event.getEntity();
        if (hurt.level().isClientSide()) return;
        if (ItemMantle.getEffect(hurt, ConstellationsAS.ARMARA) == null) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        int stacks = getCurrentImmunityStacks(hurt);
        if (stacks > 0) {
            setCurrentImmunityStacks(hurt, stacks - 1);
            event.setCanceled(true);
        }
    }

    private int getCurrentImmunityRechargeTick(LivingEntity entity) {
        return getData(entity).getInt("ITick");
    }

    private void setCurrentImmunityRechargeTick(LivingEntity entity, int tick) {
        getData(entity).putInt("ITick", tick);
    }

    private int getCurrentImmunityStacks(LivingEntity entity) {
        return getData(entity).getInt("IStacks");
    }

    private void setCurrentImmunityStacks(LivingEntity entity, int stacks) {
        getData(entity).putInt("IStacks", stacks);
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class ArmaraConfig extends Config {

        public ForgeConfigSpec.IntValue immunityStacks;
        public ForgeConfigSpec.IntValue immunityRechargeTicks;
        public ForgeConfigSpec.IntValue chargeCostPerStack;

        public ArmaraConfig() {
            super("armara");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.armara");
            immunityStacks = b
                    .comment("Maximum number of immunity stacks.")
                    .defineInRange("immunityStacks", 3, 0, 10);
            immunityRechargeTicks = b
                    .comment("Ticks between immunity stack recharges.")
                    .defineInRange("immunityRechargeTicks", 300, 20, 1_000_000);
            chargeCostPerStack = b
                    .comment("Alignment charge consumed per immunity stack created.")
                    .defineInRange("chargeCostPerStack", 750, 0, 1000);
            b.pop();
            b.build();
        }
    }
}
