/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

/**
 * Key perk that lets the player auto-step up full-height blocks (1.0 block)
 * instead of the vanilla 0.6. Applied via the Forge STEP_HEIGHT_ADDITION
 * attribute as a transient modifier that disappears when the perk is removed.
 */
public class KeyStepAssist extends KeyPerk {

    private static final UUID   STEP_UUID = UUID.fromString("A8F4F2E3-1C3D-4A4B-9E6F-8B1C2D3E4F5A");
    private static final double STEP_ADD  = 0.4;

    public KeyStepAssist(int x, int y) {
        super(AstralSorcery.key("key_step_assist"), x, y);
    }

    @Override
    public boolean hasTickEffect() {
        return true;
    }

    @Override
    public void onPlayerTick(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        AttributeInstance attr = player.getAttribute(
                Objects.requireNonNull(ForgeMod.STEP_HEIGHT_ADDITION.get()));
        if (attr != null && attr.getModifier(STEP_UUID) == null) {
            attr.addTransientModifier(new AttributeModifier(
                    STEP_UUID, "KeyStepAssist", STEP_ADD, AttributeModifier.Operation.ADDITION));
        }
    }

    @Override
    public void onDeallocate(@Nonnull Player player) {
        if (player.level().isClientSide()) return;
        AttributeInstance attr = player.getAttribute(
                Objects.requireNonNull(ForgeMod.STEP_HEIGHT_ADDITION.get()));
        if (attr != null) {
            attr.removeModifier(STEP_UUID);
        }
    }
}
