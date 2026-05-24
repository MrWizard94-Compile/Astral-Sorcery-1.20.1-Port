package hellfirepvp.astralsorcery.common.item.wand;

import com.google.common.collect.Iterables;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperDamageCancelling;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.RaytraceAssist;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Blink Wand — teleports the player to a targeted block, or launches them in their
 * look direction (dash mode). Consumes alignment charge per use.
 *
 * <p>1.16 → 1.20: PlayerEntity → Player, ServerPlayerEntity → ServerPlayer,
 * setPositionAndUpdate → teleportTo, setMotion → setDeltaMovement,
 * getCooldownTracker → getCooldowns, ActionResult → InteractionResultHolder,
 * UseAction → UseAnim, getItemInUseCount → getUseItemRemainingTicks,
 * entity.getLook(1F) → entity.getLookAngle(), isElytraFlying → isFallFlying.
 * Client VFX (onUsingTick particles) deferred to Phase 12.</p>
 */
public class ItemBlinkWand extends ItemAS implements AlignmentChargeConsumer {

    private static final float COST_PER_BLINK = 700F;
    private static final float COST_PER_DASH  = 850F;

    public ItemBlinkWand() {
        super(defaultProperties().stacksTo(1));
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(this)) {
            return 0F;
        }
        if (getBlinkMode(stack) == BlinkMode.TELEPORT) {
            return COST_PER_BLINK;
        } else if (player.isUsingItem()) {
            ItemStack held = player.getUseItem();
            if (!held.isEmpty() && held.getItem() instanceof ItemBlinkWand) {
                int timeLeft = player.getUseItemRemainingTicks();
                float strength = 0.2F + Math.min(1F, Math.min(50, stack.getUseDuration() - timeLeft) / 50F) * 0.8F;
                return COST_PER_DASH * strength;
            }
        }
        return 0F;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isCrouching()) {
            BlinkMode nextMode = getBlinkMode(held).next();
            setBlinkMode(held, nextMode);
            player.displayClientMessage(nextMode.getDisplay(), true);
        } else if (!player.getCooldowns().isOnCooldown(this)) {
            player.startUsingItem(hand);
        }
        return InteractionResultHolder.consume(held);
    }

    @Override
    public UseAnim getUseAnimation(@Nonnull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@Nonnull ItemStack stack) {
        return 72_000;
    }

    @Override
    public void releaseUsing(@Nonnull ItemStack stack, @Nonnull Level level,
                             @Nonnull LivingEntity entity, int timeLeft) {
        if (level.isClientSide() || !(entity instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }

        BlinkMode mode = getBlinkMode(stack);
        if (mode == BlinkMode.TELEPORT) {
            Vec3 lookVec = player.getLookAngle();
            Vector3 origin = Vector3.atEntityCorner(player).addY(0.5F);
            Vector3 look = new Vector3(lookVec.x, lookVec.y, lookVec.z).normalize().multiply(40F).add(origin);
            List<BlockPos> blockLine = new ArrayList<>();
            RaytraceAssist rta = new RaytraceAssist(origin, look);
            rta.forEachBlockPos(pos -> MiscUtils.executeWithChunk(player.level(), pos, () -> {
                if (BlockUtils.isReplaceable(player.level(), pos) && BlockUtils.isReplaceable(player.level(), pos.above())) {
                    blockLine.add(pos);
                    return true;
                }
                return false;
            }, false));

            if (!blockLine.isEmpty()) {
                BlockPos at = Iterables.getLast(blockLine);
                if (origin.distance(at) > 5) {
                    if (AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_BLINK, false)) {
                        player.teleportTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
                        if (!player.isCreative()) {
                            player.getCooldowns().addCooldown(stack.getItem(), 40);
                        }
                    }
                }
            }
        } else if (mode == BlinkMode.LAUNCH) {
            float multiplier = entity.isFallFlying() ? 0.8F : 2.4F;
            float strength = 0.2F + Math.min(1F, Math.min(50, stack.getUseDuration() - timeLeft) / 50F) * multiplier;
            if (strength > 0.3F) {
                float chargeCost = COST_PER_DASH * 0.8F;
                if (AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, chargeCost, false)) {
                    Vec3 lookVec = player.getLookAngle();
                    Vector3 motion = new Vector3(lookVec.x, lookVec.y, lookVec.z).normalize().multiply(strength * 3F);
                    if (motion.getY() > 0) {
                        motion.setY(Mth.clamp(motion.getY() + (0.2F * strength), 0.2F * strength, Float.MAX_VALUE));
                    }
                    player.setDeltaMovement(motion.getX(), motion.getY(), motion.getZ());
                    player.fallDistance = 0F;
                    if (!entity.isFallFlying()) {
                        EventHelperDamageCancelling.markInvulnerableToNextDamage(player,
                                level.damageSources().fall());
                    }
                }
            }
        }
    }

    public static void setBlinkMode(@Nonnull ItemStack stack, @Nonnull BlinkMode mode) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlinkWand)) {
            return;
        }
        NBTHelper.getPersistentData(stack).putInt("blinkMode", mode.ordinal());
    }

    @Nonnull
    public static BlinkMode getBlinkMode(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlinkWand)) {
            return BlinkMode.LAUNCH;
        }
        CompoundTag nbt = NBTHelper.getPersistentData(stack);
        return MiscUtils.getEnumEntry(BlinkMode.class, nbt.getInt("blinkMode"));
    }

    public enum BlinkMode {

        LAUNCH("launch"),
        TELEPORT("teleport");

        private final String name;

        BlinkMode(String name) {
            this.name = name;
        }

        public Component getName() {
            return Component.translatable("astralsorcery.misc.blink.mode." + this.name);
        }

        public Component getDisplay() {
            return Component.translatable("astralsorcery.misc.blink.mode", this.getName());
        }

        @Nonnull
        BlinkMode next() {
            int next = (this.ordinal() + 1) % values().length;
            return MiscUtils.getEnumEntry(BlinkMode.class, next);
        }
    }
}
