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
 * Key perk for the Lucerna constellation branch.
 * Effect: Passive Night Vision; enhanced starlight collection and XP via attributes.
 */
public class KeyLucerna extends KeyPerk {

    public KeyLucerna(int x, int y) {
        super(AstralSorcery.key("key_lucerna"), x, y);
        setRequiredConstellation(AstralSorcery.key("lucerna"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_STARLIGHT_COLLECTION.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.25f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_EXPERIENCE.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.20f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        // Night Vision — ambient, no particles; always active (not just at night)
        player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.NIGHT_VISION),
                300, 0, true, false));
    }
}
