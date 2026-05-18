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
 * Key perk for the Fornax constellation branch.
 * Effect: Passive Fire Resistance; elemental resistance via attribute.
 * Auto-smelt on ore break is handled by EventHandlerMining.
 */
public class KeyFornax extends KeyPerk {

    public KeyFornax(int x, int y) {
        super(AstralSorcery.key("key_fornax"), x, y);
        setRequiredConstellation(AstralSorcery.key("fornax"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_ALL_ELEMENTAL_RESIST.getKey(),
                ModifierType.ADDITION, 0.30f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.FIRE_RESISTANCE),
                40, 0, true, false));
    }
}
