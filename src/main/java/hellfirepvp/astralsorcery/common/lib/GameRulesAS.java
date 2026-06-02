package hellfirepvp.astralsorcery.common.lib;

import net.minecraft.world.level.GameRules;

/**
 * Astral Sorcery game rule constants.
 *
 * <p>Populated by registering via {@link GameRules.Builder} during
 * {@link net.minecraftforge.event.RegisterGameRulesEvent}. The key
 * is held here so callers can reference it without knowing where it
 * was registered.</p>
 *
 * <p>1.16 -> 1.20: {@code GameRules.RuleKey} -> {@code GameRules.Key},
 * registration moved from RegistryGameRules to RegisterGameRulesEvent.</p>
 */
public final class GameRulesAS {

    private GameRulesAS() {}

    /**
     * When true, bypasses the skylight requirement for starlight collection
     * and constellation visibility. Useful for underground base play.
     * Default: false.
     */
    public static GameRules.Key<GameRules.BooleanValue> IGNORE_SKYLIGHT_CHECK_RULE;
}
