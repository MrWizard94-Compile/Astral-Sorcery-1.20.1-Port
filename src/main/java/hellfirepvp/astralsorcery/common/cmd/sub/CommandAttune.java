package hellfirepvp.astralsorcery.common.cmd.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.stream.Collectors;

public class CommandAttune implements Command<CommandSourceStack> {

    private static final CommandAttune CMD = new CommandAttune();
    private static final SimpleCommandExceptionType NOT_MAJOR = new SimpleCommandExceptionType(
            Component.literal("Not a major constellation"));
    private static final SimpleCommandExceptionType UNKNOWN_CONSTELLATION = new SimpleCommandExceptionType(
            Component.literal("Unknown constellation"));

    private CommandAttune() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("attune")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("constellation", ResourceLocationArgument.id())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(
                                        ConstellationRegistry.getAllConstellations().stream()
                                                .filter(c -> c instanceof IMajorConstellation)
                                                .map(IConstellation::getRegistryName)
                                                .collect(Collectors.toList()), builder))
                                .executes(CMD)));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        ResourceLocation key = ResourceLocationArgument.getId(context, "constellation");
        IConstellation cst = ConstellationRegistry.getConstellation(key);
        if (cst == null) throw UNKNOWN_CONSTELLATION.create();
        if (!(cst instanceof IMajorConstellation major)) throw NOT_MAJOR.create();

        ResearchManager.attuneTo(player, key);
        context.getSource().sendSuccess(
                () -> Component.literal("Success! ").append(player.getDisplayName())
                        .append(" has been attuned to ")
                        .append(major.getConstellationName().withStyle(ChatFormatting.BLUE))
                        .withStyle(ChatFormatting.GREEN),
                true);
        return Command.SINGLE_SUCCESS;
    }
}
