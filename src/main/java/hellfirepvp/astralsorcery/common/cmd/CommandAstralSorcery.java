/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.common.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import hellfirepvp.astralsorcery.common.capability.PlayerProgressHelper;
import hellfirepvp.astralsorcery.common.cmd.sub.CommandConstellation;
import hellfirepvp.astralsorcery.common.cmd.sub.CommandMaximizeAll;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncPlayerProgress;
import hellfirepvp.astralsorcery.common.perk.PerkLevelManager;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 * The main {@code /astralsorcery} (alias {@code /as}) command tree.
 * Provides subcommands for inspecting and modifying player progression,
 * starlight network state, and perk data.
 *
 * <p>Registered on the Forge event bus via {@link RegisterCommandsEvent}.</p>
 *
 * <p>1.16 -> 1.20 changes:
 * Brigadier API is stable. CommandSource -> CommandSourceStack.
 * ServerPlayerEntity -> ServerPlayer. ServerWorld -> ServerLevel.</p>
 */
public class CommandAstralSorcery {

    @SubscribeEvent
    public void onRegisterCommands(@Nonnull RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        var root = Commands.literal("astralsorcery")
                .then(buildProgress())
                .then(buildReset())
                .then(buildAttune())
                .then(buildPerkExp())
                .then(buildNetwork())
                .then(CommandConstellation.register())
                .then(CommandMaximizeAll.register());

        dispatcher.register(root);
        dispatcher.register(Commands.literal("as").redirect(dispatcher.register(root)));
    }

    // ---- /astralsorcery progress ----

    @Nonnull
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildProgress() {
        return Commands.literal("progress")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    PlayerProgress progress = PlayerProgressHelper.getProgress(player);
                    if (progress == null) {
                        ctx.getSource().sendFailure(Component.literal("No progress data found."));
                        return 0;
                    }

                    int level = PerkLevelManager.getLevelFromExp(progress.getPerkExp());
                    int totalPoints = PerkLevelManager.getPerkPointsForLevel(level);
                    int spent = progress.getAllocatedPerks().size();

                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "--- Astral Sorcery Progress ---"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Tier: " + progress.getTierReached().name()), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Constellation: " + (progress.getAttunedConstellation() != null
                                    ? progress.getAttunedConstellation().toString()
                                    : "none")), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Perk Level: " + level + " (exp: " + progress.getPerkExp() + ")"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Perk Points: " + spent + "/" + totalPoints + " allocated"), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Constellations discovered: " + progress.getDiscoveredConstellations().size()),
                            false);

                    return 1;
                });
    }

    // ---- /astralsorcery reset [player] ----

    @Nonnull
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildReset() {
        return Commands.literal("reset")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> doReset(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                        .executes(ctx -> doReset(ctx.getSource(),
                                net.minecraft.commands.arguments.EntityArgument.getPlayer(ctx, "player"))));
    }

    private static int doReset(CommandSourceStack src, ServerPlayer target) {
        hellfirepvp.astralsorcery.common.data.research.ResearchManager.wipeProgress(target);
        src.sendSuccess(() -> Component.literal("Wiped " + target.getGameProfile().getName() + "'s Astral Sorcery progress."), true);
        return 1;
    }

    // ---- /astralsorcery attune <constellation> [player] ----

    @Nonnull
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildAttune() {
        return Commands.literal("attune")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("constellation", ResourceLocationArgument.id())
                        .executes(ctx -> doAttune(ctx.getSource(),
                                ctx.getSource().getPlayerOrException(),
                                ResourceLocationArgument.getId(ctx, "constellation")))
                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                .executes(ctx -> doAttune(ctx.getSource(),
                                        net.minecraft.commands.arguments.EntityArgument.getPlayer(ctx, "player"),
                                        ResourceLocationArgument.getId(ctx, "constellation")))));
    }

    private static int doAttune(CommandSourceStack src, ServerPlayer target, ResourceLocation constellation) {
        hellfirepvp.astralsorcery.common.data.research.ResearchManager.attuneTo(target, constellation);
        src.sendSuccess(() -> Component.literal("Attuned " + target.getGameProfile().getName() + " to " + constellation), true);
        return 1;
    }

    // ---- /astralsorcery perkexp <amount> [player] ----

    @Nonnull
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildPerkExp() {
        return Commands.literal("perkexp")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                        .executes(ctx -> doPerkExp(ctx.getSource(),
                                ctx.getSource().getPlayerOrException(),
                                IntegerArgumentType.getInteger(ctx, "amount")))
                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                .executes(ctx -> doPerkExp(ctx.getSource(),
                                        net.minecraft.commands.arguments.EntityArgument.getPlayer(ctx, "player"),
                                        IntegerArgumentType.getInteger(ctx, "amount")))));
    }

    private static int doPerkExp(CommandSourceStack src, ServerPlayer target, int amount) {
        PlayerProgress progress = PlayerProgressHelper.getProgress(target);
        if (progress == null) {
            src.sendFailure(Component.literal("No progress data found."));
            return 0;
        }
        long newExp = progress.getPerkExp() + amount;
        progress.setPerkExp(newExp);
        int newLevel = PerkLevelManager.getLevelFromExp(newExp);
        PacketChannel.sendToPlayer(new PktSyncPlayerProgress(progress), target);
        src.sendSuccess(() -> Component.literal("Added " + amount + " perk exp to " + target.getGameProfile().getName()
                + ". Now level " + newLevel + " (total: " + newExp + ")"), true);
        return 1;
    }

    // ---- /astralsorcery network ----

    @Nonnull
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildNetwork() {
        return Commands.literal("network")
                .executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    WorldNetworkHandler handler = WorldNetworkHandler.getOrCreate(level);

                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "--- Starlight Network (" + level.dimension().location() + ") ---"),
                            false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Nodes: " + handler.getNodeCount()), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Links: " + handler.getLinkCount()), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Sources: " + handler.getSourcePositions().size()), false);
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Receivers: " + handler.getReceiverPositions().size()), false);

                    return 1;
                });
    }
}
