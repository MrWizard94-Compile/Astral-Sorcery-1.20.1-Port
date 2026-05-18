/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Base class for constellation mantle (cape) effects.
 *
 * <p>Each concrete subclass represents one weak constellation and defines what
 * happens while the player wears the mantle attuned to that constellation.</p>
 *
 * <p>1.16 → 1.20 changes:
 * ForgeRegistryEntry removed (no longer a Forge-managed registry),
 * ITickHandler (observerlib) removed — ticking handled by PerkEffectHelper
 * or a dedicated Forge event listener in the subclass,
 * PlayerEntity → Player, ServerPlayerEntity → ServerPlayer,
 * EquipmentSlotType → EquipmentSlot</p>
 */
public abstract class MantleEffect {

    protected static final Random rand = new Random();

    @Nonnull
    private final IWeakConstellation constellation;

    protected MantleEffect(@Nonnull IWeakConstellation constellation) {
        this.constellation = constellation;
    }

    @Nonnull
    public IWeakConstellation getAssociatedConstellation() {
        return constellation;
    }

    public abstract Config getConfig();

    /** Called once per tick on the server while the player wears this mantle. */
    protected void tickServer(@Nonnull Player player) {}

    /** Whether this effect overrides {@link #tickServer} (or the client equivalent). */
    protected boolean usesTickMethods() {
        return false;
    }

    // -------------------------------------------------------------------------
    // Config
    // -------------------------------------------------------------------------

    /**
     * Per-constellation config block. Each subclass holds a static instance
     * that is built with the Forge config spec at class-load time.
     *
     * <p>Full config wiring (addConfigEntry → ServerConfig) is deferred until
     * the ServerConfig system is ported.</p>
     */
    public static class Config {

        public final ForgeConfigSpec.BooleanValue enabled;

        public Config(String constellationName) {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            builder.push("constellation.mantle." + constellationName);
            enabled = builder
                    .comment("Set to false to disable this mantle effect")
                    .define("enabled", true);
            builder.pop();
            builder.build(); // seals the spec; values are bound immediately
        }
    }
}
