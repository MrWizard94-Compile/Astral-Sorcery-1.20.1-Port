package hellfirepvp.astralsorcery.common.cmd.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
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

public class CommandExp implements Command<CommandSourceStack> {

    private static final CommandExp CMD = new CommandExp();

    private CommandExp() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("exp")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("exp", LongArgumentType.longArg(0))
                                .executes(CMD)));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        long exp = LongArgumentType.getLong(context, "exp");
        if (ResearchManager.setExp(player, exp)) {
            context.getSource().sendSuccess(
                    () -> Component.literal("Perk exp set to " + exp + ".").withStyle(ChatFormatting.GREEN), true);
            return Command.SINGLE_SUCCESS;
        }
        context.getSource().sendSuccess(
                () -> Component.literal("Failed — player has no progress data.").withStyle(ChatFormatting.RED), true);
        return 0;
    }
}
