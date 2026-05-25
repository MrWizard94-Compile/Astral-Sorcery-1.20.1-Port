/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.constellation.mantle;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    /**
     * Called once per tick on the server while the player wears this mantle.
     * Driven by {@link hellfirepvp.astralsorcery.common.event.EventHandlerMantleTick}.
     */
    protected void tickServer(@Nonnull Player player) {}

    /** Whether this effect uses {@link #tickServer}. */
    protected boolean usesTickMethods() {
        return false;
    }

    public final boolean shouldTick() {
        return usesTickMethods();
    }

    /**
     * Called once per tick on the client while the player wears this mantle.
     * Driven by {@link hellfirepvp.astralsorcery.client.event.EventHandlerClientMantleTick}.
     */
    @OnlyIn(Dist.CLIENT)
    protected void tickClient(@Nonnull Player player) {}

    /** Whether this effect uses {@link #tickClient}. Override to return true. */
    @OnlyIn(Dist.CLIENT)
    protected boolean usesClientTick() {
        return false;
    }

    public final boolean shouldTickClient() {
        return usesClientTick();
    }

    /** Dispatch the client tick. Skips disabled configs. */
    @OnlyIn(Dist.CLIENT)
    public final void onClientTick(@Nonnull Player player) {
        if (!getConfig().enabled.get()) return;
        tickClient(player);
    }

    /**
     * Override to register event listeners on the Forge event bus.
     * Called from {@link hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffectRegistry#init()}.
     */
    protected void attachEventListeners(@Nonnull IEventBus bus) {}

    /**
     * Dispatch the server tick. Skips fake players and disabled configs.
     * Called by EventHandlerMantleTick.
     */
    public final void onServerTick(@Nonnull Player player) {
        if (!getConfig().enabled.get()) return;
        if (!(player instanceof ServerPlayer)) return;
        if (player instanceof FakePlayer) return;
        tickServer(player);
    }

    /**
     * Returns the persistent NBT data stored on the mantle item in the chest slot.
     * Returns an empty tag (not stored) if the entity is null or not wearing a mantle.
     */
    @Nonnull
    protected static CompoundTag getData(@Nullable LivingEntity entity) {
        if (entity == null) return new CompoundTag();
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !(chest.getItem() instanceof ItemMantle)) return new CompoundTag();
        return NBTHelper.getPersistentData(chest);
    }

    // -------------------------------------------------------------------------
    // Config
    // -------------------------------------------------------------------------

    /**
     * Per-constellation config block. Each subclass holds a static instance
     * that is built with the Forge config spec at class-load time.
     *
     * <p>Each subclass builds its own standalone spec; values are read-only
     * defaults (not user-configurable via file) until specs are registered
     * with Forge's config system.</p>
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
