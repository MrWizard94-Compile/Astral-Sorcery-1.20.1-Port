/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import javax.annotation.Nonnull;

/**
 * Mantle effect for Fornax (Weak — Fire).
 *
 * <ul>
 *   <li>Wearing player takes only {@code damageReductionInFire * dmg} from fire.</li>
 *   <li>Wearing player heals {@code healPercentFromFireDamage * dmg} when hit by fire.</li>
 *   <li>An attacker who is on fire and wears this mantle deals
 *       {@code damageIncreaseInFire * dmg}.</li>
 * </ul>
 *
 * <p>1.16 → 1.20: DamageSource.isFireDamage() → DamageSource.is(DamageTypeTags.IS_FIRE),
 * event.getEntityLiving() → event.getEntity(),
 * DamageSource.getTrueSource() → DamageSource.getEntity().</p>
 */
public class MantleEffectFornax extends MantleEffect {

    public static final FornaxConfig CONFIG = new FornaxConfig();

    public MantleEffectFornax() {
        super(ConstellationsAS.FORNAX);
    }

    @Override
    protected void attachEventListeners(@Nonnull IEventBus bus) {
        bus.addListener(this::onHurt);
    }

    private void onHurt(LivingHurtEvent event) {
        LivingEntity attacked = event.getEntity();
        if (attacked.level().isClientSide()) return;

        // Attacker on fire + wearing Fornax → bonus damage
        if (event.getSource().getEntity() instanceof LivingEntity attacker
                && attacker.isOnFire()
                && ItemMantle.getEffect(attacker, ConstellationsAS.FORNAX) != null) {
            event.setAmount(event.getAmount() * CONFIG.damageIncreaseInFire.get().floatValue());
        }

        // Defender wearing Fornax + fire damage → heal + reduce
        if (event.getSource().is(DamageTypeTags.IS_FIRE)
                && ItemMantle.getEffect(attacked, ConstellationsAS.FORNAX) != null) {
            if (CONFIG.healPercentFromFireDamage.get() > 0) {
                attacked.heal(event.getAmount() * CONFIG.healPercentFromFireDamage.get().floatValue());
            }
            event.setAmount(event.getAmount() * CONFIG.damageReductionInFire.get().floatValue());
        }
    }

    @Override
    protected boolean usesTickMethods() {
        return false;
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class FornaxConfig extends Config {

        public ForgeConfigSpec.DoubleValue damageReductionInFire;
        public ForgeConfigSpec.DoubleValue damageIncreaseInFire;
        public ForgeConfigSpec.DoubleValue healPercentFromFireDamage;

        public FornaxConfig() {
            super("fornax");
            ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
            b.push("constellation.mantle.fornax");
            damageReductionInFire = b
                    .comment("Multiplier applied to fire damage taken (< 1 = reduction).")
                    .defineInRange("damageReductionInFire", 0.4, 0.0, 1.0);
            damageIncreaseInFire = b
                    .comment("Multiplier applied to damage dealt while wearer is on fire.")
                    .defineInRange("damageIncreaseInFire", 1.6, 1.0, 3.0);
            healPercentFromFireDamage = b
                    .comment("Fraction of incoming fire damage converted to healing.")
                    .defineInRange("healPercentFromFireDamage", 0.6, 0.0, 3.0);
            b.pop();
            b.build();
        }
    }
}
