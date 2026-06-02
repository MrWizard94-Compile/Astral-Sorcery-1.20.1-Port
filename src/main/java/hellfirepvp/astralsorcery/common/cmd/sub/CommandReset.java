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

public class CommandReset implements Command<CommandSourceStack> {

    private static final CommandReset CMD = new CommandReset();

    private CommandReset() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("reset")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(CMD));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ResearchManager.wipeProgress(player);
        String name = player.getGameProfile().getName();
        context.getSource().sendSuccess(
                () -> Component.literal("Wiped " + name + "'s Astral Sorcery progress.").withStyle(ChatFormatting.GREEN),
                true);
        return Command.SINGLE_SUCCESS;
    }
}
