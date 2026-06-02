package hellfirepvp.astralsorcery.common.cmd.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CommandMaximizeAll implements Command<CommandSourceStack> {

    private static final CommandMaximizeAll CMD = new CommandMaximizeAll();

    private CommandMaximizeAll() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("maximize")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            ResearchManager.forceMaximizeAll(target);
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Maximized all for " + target.getGameProfile().getName() + ".").withStyle(ChatFormatting.GREEN),
                                    true);
                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ResearchManager.forceMaximizeAll(player);
        context.getSource().sendSuccess(
                () -> Component.literal("Maximized all research!").withStyle(ChatFormatting.GREEN), true);
        return Command.SINGLE_SUCCESS;
    }
}
