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
 * Key perk for the Armara constellation branch.
 * Effect: Passive Resistance I while allocated; knockback resistance via attribute.
 * Provides +4 armor, +2 armor toughness, and 20% knockback resistance.
 */
public class KeyArmara extends KeyPerk {

    public KeyArmara(int x, int y) {
        super(AstralSorcery.key("key_armara"), x, y);
        setRequiredConstellation(AstralSorcery.key("armara"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ARMOR.getKey(),
                ModifierType.ADDITION, 4.0f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ARMOR_TOUGHNESS.getKey(),
                ModifierType.ADDITION, 2.0f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_KNOCKBACK_RESIST.getKey(),
                ModifierType.ADDITION, 0.2f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        // Ambient Resistance I — refreshed each server tick, no particles
        player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.DAMAGE_RESISTANCE),
                40, 0, true, false));
    }
}
