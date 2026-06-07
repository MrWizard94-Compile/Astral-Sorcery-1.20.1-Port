/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.cmd.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgressManager;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * {@code /astral progress <player> <tier>} — force a player to a specific
 * progression tier. Requires permission level 2.
 *
 * <p>1.16 → 1.20: CommandSource → CommandSourceStack; EnumArgument (Forge)
 * replaced with StringArgumentType + suggestions; PlayerEntity → ServerPlayer.</p>
 */
public final class CommandProgress {

    private static final SimpleCommandExceptionType UNKNOWN_TIER = new SimpleCommandExceptionType(
            Component.literal("Unknown progression tier. Valid values: " +
                    Arrays.stream(ProgressionTier.values())
                            .map(ProgressionTier::name).collect(Collectors.joining(", "))));

    private CommandProgress() {}

    @Nonnull
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("setprogress")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("tier", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(ProgressionTier.values()).map(ProgressionTier::name), builder))
                                .executes(CommandProgress::execute)));
    }

    private static int execute(@Nonnull CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        String tierName = StringArgumentType.getString(ctx, "tier");

        ProgressionTier goal;
        try {
            goal = ProgressionTier.valueOf(tierName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw UNKNOWN_TIER.create();
        }

        var progress = PlayerProgressManager.getProgress(target);
        if (progress != null && progress.getTierReached().isThisLaterOrEqual(goal)) {
            ctx.getSource().sendFailure(
                    Component.literal(target.getGameProfile().getName() + "'s tier is already at or above " + goal.name())
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (ResearchManager.grantTier(target, goal)) {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Set " + target.getGameProfile().getName() + " to tier " + goal.name())
                            .withStyle(ChatFormatting.GREEN),
                    true);
            return Command.SINGLE_SUCCESS;
        } else {
            ctx.getSource().sendFailure(
                    Component.literal("Failed to set progression for " + target.getGameProfile().getName())
                            .withStyle(ChatFormatting.RED));
            return 0;
        }
    }
}
