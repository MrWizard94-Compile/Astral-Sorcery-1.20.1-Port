/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.cmd.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockStateHelper;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;

/**
 * Debug command to serialize the held item or looked-at block as JSON/string.
 * Sends a clickable copy-to-clipboard message to the command source.
 *
 * <p>1.16 → 1.20: CommandSource → CommandSourceStack; PlayerEntity → ServerPlayer;
 * BlockRayTraceResult → BlockHitResult; getHeldItemMainhand → getMainHandItem;
 * getEntityWorld().isRemote → level(); StringTextComponent → Component.literal;
 * sendFeedback(msg, bool) → sendSuccess(supplier, bool).</p>
 */
public final class CommandSerialize {

    private CommandSerialize() {}

    @Nonnull
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("serialize")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("hand").executes(CommandSerialize::serializeHand))
                .then(Commands.literal("look").executes(CommandSerialize::serializeLook));
    }

    private static int serializeHand(@Nonnull CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String serialized = JsonHelper.serializeItemStack(player.getMainHandItem()).toString();
        sendCopyable(ctx, serialized);
        return Command.SINGLE_SUCCESS;
    }

    private static int serializeLook(@Nonnull CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        BlockHitResult hit = MiscUtils.rayTraceLookBlock(player);
        BlockState state = (hit == null || hit.getType() != HitResult.Type.BLOCK)
                ? Blocks.AIR.defaultBlockState()
                : player.level().getBlockState(hit.getBlockPos());
        sendCopyable(ctx, BlockStateHelper.serialize(state));
        return Command.SINGLE_SUCCESS;
    }

    private static void sendCopyable(@Nonnull CommandContext<CommandSourceStack> ctx,
                                      @Nonnull String text) {
        Component msg = Component.literal(text)
                .withStyle(ChatFormatting.GREEN)
                .withStyle(s -> s
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Click to copy")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text)));
        ctx.getSource().sendSuccess(() -> msg, true);
    }
}
