package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.item.base.ItemAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Knowledge Share — inscribes one player's progression tier into the item;
 * another player can consume it to advance their own tier.
 *
 * <p>NBT layout: {@code storedTier} (int ordinal), {@code ownerUUID} (UUID),
 * {@code ownerName} (String). Shift-use or use-when-blank inscribes; plain
 * use when inscribed by another player consumes and applies the stored tier.</p>
 *
 * <p>1.16 → 1.20: ActionResult → InteractionResultHolder, World → Level,
 * PlayerEntity → Player, ServerPlayerEntity → ServerPlayer.</p>
 */
public class ItemKnowledgeShare extends ItemAS {

    private static final String TAG_TIER = "storedTier";
    private static final String TAG_OWNER = "ownerUUID";
    private static final String TAG_OWNER_NAME = "ownerName";

    public ItemKnowledgeShare() {
        super(new Properties().stacksTo(1));
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        UUID ownerId = getOwnerUUID(stack);
        boolean isBlank = ownerId == null;
        boolean isSelf = ownerId != null && ownerId.equals(player.getUUID());

        if (isBlank || player.isCrouching() || isSelf) {
            inscribe(stack, serverPlayer);
        } else {
            applyKnowledge(stack, serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private void inscribe(@Nonnull ItemStack stack, @Nonnull ServerPlayer player) {
        PlayerProgress progress = PlayerProgressManager.getProgress(player);
        if (progress == null) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_TIER, progress.getTierReached().name());
        tag.putUUID(TAG_OWNER, player.getUUID());
        tag.putString(TAG_OWNER_NAME, player.getName().getString());
    }

    private void applyKnowledge(@Nonnull ItemStack stack, @Nonnull ServerPlayer player) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_TIER)) return;
        ProgressionTier stored;
        if (tag.contains(TAG_TIER, Tag.TAG_STRING)) {
            // Current format: name-based (resilient to enum reorder)
            try {
                stored = ProgressionTier.valueOf(tag.getString(TAG_TIER));
            } catch (IllegalArgumentException e) {
                return; // unknown tier name — corrupted or future format
            }
        } else {
            // Legacy format: ordinal int — keep reading for backward compat
            int tierOrd = tag.getInt(TAG_TIER);
            ProgressionTier[] tiers = ProgressionTier.values();
            if (tierOrd < 0 || tierOrd >= tiers.length) return;
            stored = tiers[tierOrd];
        }
        ResearchManager.grantTier(player, stored);
        stack.shrink(1);
    }

    @Nullable
    private static UUID getOwnerUUID(@Nonnull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.hasUUID(TAG_OWNER)) return null;
        return tag.getUUID(TAG_OWNER);
    }
}
