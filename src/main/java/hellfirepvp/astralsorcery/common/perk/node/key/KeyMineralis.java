/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.ModifierType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Key perk for the Mineralis constellation branch.
 * Effect: Haste II underground and Night Vision in caves.
 * "Underground" is defined as being below the WORLD_SURFACE heightmap — correct
 * for 1.20.1 where terrain can exceed Y=60 in mountain biomes.
 * Reach and mining speed bonuses via attributes.
 */
public class KeyMineralis extends KeyPerk {

    public KeyMineralis(int x, int y) {
        super(AstralSorcery.key("key_mineralis"), x, y);
        setRequiredConstellation(AstralSorcery.key("mineralis"));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_MINING_SPEED.getKey(),
                ModifierType.ADDED_MULTIPLY, 0.30f));
        addModifier(new PerkAttributeModifier(
                PerkAttributeTypesAS.ATTR_TYPE_REACH.getKey(),
                ModifierType.ADDITION, 2.0f));
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        int surfaceY = player.level().getHeight(
                Heightmap.Types.WORLD_SURFACE, player.getBlockX(), player.getBlockZ());
        if (player.getBlockY() < surfaceY) {
            // Haste II underground — stronger than KeyEvorsio's Haste I
            player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.DIG_SPEED),
                    40, 1, true, false));
            // Night Vision underground — helps spot ores in the dark
            player.addEffect(new MobEffectInstance(Objects.requireNonNull(MobEffects.NIGHT_VISION),
                    300, 0, true, false));
        }
    }
}
