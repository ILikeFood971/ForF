/*
 * This file is part of the Friend or Foe project, licensed under the
 * GNU General Public License v3.0
 *
 * Copyright (C) 2023  ILikeFood971 and contributors
 *
 * Friend or Foe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Friend or Foe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Friend or Foe.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.ilikefood971.forf.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.ilikefood971.forf.util.Util;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

import static net.ilikefood971.forf.command.CommandUtil.ALREADY_STARTED;
import static net.ilikefood971.forf.util.Util.PERSISTENT_DATA;
import static net.ilikefood971.forf.util.Util.sendFeedback;
import static net.minecraft.server.command.CommandManager.*;

public class JoinCommand {

    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, RegistrationEnvironment environment) {
        dispatcher.register(
                literal("forf")
                        .then(
                                literal("join")
                                        .then(
                                                argument("players", EntityArgumentType.players())
                                                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(3))
                                                        .executes(JoinCommand::run)
                                        )
                                        .executes(JoinCommand::runSolo)
                        )
        );
    }

    @SuppressWarnings("SameReturnValue")
    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (PERSISTENT_DATA.isStarted()) {
            throw ALREADY_STARTED.create();
        }

        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");

        for (ServerPlayerEntity player : players) {
            joinPlayer(player);
        }
        sendFeedback(context, Text.translatable("forf.commands.join.success.multiple", players.size()), true);
        return 1;
    }

    @SuppressWarnings("SameReturnValue")
    private static int runSolo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

        if (PERSISTENT_DATA.isStarted()) {
            throw new SimpleCommandExceptionType(
                    Text.translatable("forf.commands.join.exceptions.alreadyAdded", player.getGameProfile().getName())
            ).create();
        }


        joinPlayer(player);
        sendFeedback(context, Text.translatable("forf.commands.join.success.solo", player.getGameProfile().getName()), true);

        return 1;
    }

    private static void joinPlayer(ServerPlayerEntity player) throws CommandSyntaxException {
        if (Util.isForfPlayer(player)) {
            throw new SimpleCommandExceptionType(
                    Text.translatable("forf.commands.join.exceptions.alreadyAdded", player.getGameProfile().getName())
            ).create();
        }
        Util.addNewPlayer(player);
    }
}
