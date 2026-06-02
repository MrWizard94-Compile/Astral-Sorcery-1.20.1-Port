package hellfirepvp.astralsorcery.common.cmd.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
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

import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class CommandConstellation {

    private static final SimpleCommandExceptionType UNKNOWN = new SimpleCommandExceptionType(
            Component.literal("Unknown constellation"));

    private CommandConstellation() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("constellation")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("memorize")
                        .then(Commands.argument("constellation", ResourceLocationArgument.id())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(
                                        ConstellationRegistry.getAllConstellations().stream()
                                                .map(IConstellation::getRegistryName)
                                                .collect(Collectors.toList()), builder))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            IConstellation cst = resolve(ctx.getArgument("constellation", ResourceLocation.class));
                                            return runMemorize(ctx.getSource(), target, cst);
                                        }))
                                .executes(ctx -> {
                                    IConstellation cst = resolve(ctx.getArgument("constellation", ResourceLocation.class));
                                    return runMemorize(ctx.getSource(), null, cst);
                                })))
                .then(Commands.literal("discover")
                        .then(Commands.argument("constellation", ResourceLocationArgument.id())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(
                                        ConstellationRegistry.getAllConstellations().stream()
                                                .map(IConstellation::getRegistryName)
                                                .collect(Collectors.toList()), builder))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            IConstellation cst = resolve(ctx.getArgument("constellation", ResourceLocation.class));
                                            return runDiscover(ctx.getSource(), target, cst);
                                        }))
                                .executes(ctx -> {
                                    IConstellation cst = resolve(ctx.getArgument("constellation", ResourceLocation.class));
                                    return runDiscover(ctx.getSource(), null, cst);
                                })));
    }

    private static IConstellation resolve(ResourceLocation key) throws CommandSyntaxException {
        IConstellation cst = ConstellationRegistry.getConstellation(key);
        if (cst == null) throw UNKNOWN.create();
        return cst;
    }

    private static int runMemorize(CommandSourceStack src, @Nullable ServerPlayer target,
                                   IConstellation cst) throws CommandSyntaxException {
        ServerPlayer resolved = target != null ? target : src.getPlayerOrException();
        boolean added = ResearchManager.memorizeConstellation(resolved, cst.getRegistryName());
        if (added) {
            src.sendSuccess(() -> Component.literal("Success!").withStyle(ChatFormatting.GREEN), true);
            return Command.SINGLE_SUCCESS;
        }
        src.sendSuccess(() -> Component.literal("Already memorized.").withStyle(ChatFormatting.YELLOW), true);
        return 0;
    }

    private static int runDiscover(CommandSourceStack src, @Nullable ServerPlayer target,
                                   IConstellation cst) throws CommandSyntaxException {
        ServerPlayer resolved = target != null ? target : src.getPlayerOrException();
        boolean added = ResearchManager.discoverConstellation(resolved, cst.getRegistryName());
        if (added) {
            src.sendSuccess(() -> Component.literal("Success!").withStyle(ChatFormatting.GREEN), true);
            return Command.SINGLE_SUCCESS;
        }
        src.sendSuccess(() -> Component.literal("Already discovered.").withStyle(ChatFormatting.YELLOW), true);
        return 0;
    }
}
