/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Key perk for the Evorsio constellation branch.
 * Effect: Passive Haste I (mining speed buff). Extra drops are handled
 * via the break-speed event in EventHandlerMining.
 * Provides +25% mining speed and +10% experience gain via attributes.
 */
public class KeyEvorsio extends KeyPerk {

    public KeyEvorsio(int x, int y) {
        super(AstralSorcery.key("key_evorsio"), x, y);
        setRequiredConstellation(AstralSorcery.key("evorsio"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.25f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.10f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        // Haste I — ambient, no particles; refreshed each tick
        player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.DIG_SPEED),
                40, 0, true, false));
    }
}
