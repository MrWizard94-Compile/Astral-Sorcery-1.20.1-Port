/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

/**
 * Key perk for the Gelu constellation.
 * Effect: Cold-based defense — nearby hostile mobs are slowed.
 * Provides extra armor when standing on snow/ice blocks.
 *
 * <p>Passive +20% all elemental resistance, +1 armor toughness.</p>
 */
public class KeyGelu extends KeyPerk {

    public KeyGelu(int x, int y) {
        super(AstralSorcery.key("key_gelu"), x, y);
        setRequiredConstellation(AstralSorcery.key("gelu"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ALL_ELEMENTAL_RESIST.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.20f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ARMOR_TOUGHNESS.getKey(),
                ModifierType.ADDITION, 1.0f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        if (player.tickCount % 20 != 0) return; // Every second

        // Slow nearby hostile mobs within 6 block radius
        player.level().getEntitiesOfClass(
                net.minecraft.world.entity.monster.Monster.class,
                player.getBoundingBox().inflate(6.0),
                monster -> monster.isAlive() && !monster.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN)
        ).forEach(monster -> {
            monster.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                    40, 0, false, false));
        });
    }
}
