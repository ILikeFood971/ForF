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

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.ilikefood971.forf.util.Util;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

import static net.ilikefood971.forf.command.CommandUtil.ALREADY_STARTED;
import static net.ilikefood971.forf.util.Util.PERSISTENT_DATA;
import static net.ilikefood971.forf.util.Util.sendFeedback;
import static net.minecraft.server.command.CommandManager.*;

public class LeaveCommand {

    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, RegistrationEnvironment environment) {
        dispatcher.register(
                literal("forf")
                        .then(
                                literal("leave")
                                        .then(
                                                argument("players", GameProfileArgumentType.gameProfile())
                                                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(3))
                                                        .executes(LeaveCommand::run)
                                        )
                                        .executes(LeaveCommand::runSolo)
                        )
        );
    }

    @SuppressWarnings("SameReturnValue")
    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (PERSISTENT_DATA.isStarted()) {
            throw ALREADY_STARTED.create();
        }

        Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "players");
        int changed = 0;
        for (GameProfile profile : profiles) {
            try {
                leavePlayer(profile);
                changed++;
            } catch (CommandSyntaxException e) {
                if (profiles.size() == 1) {
                    throw e;
                }
            }
        }

        sendFeedback(context, Text.translatable("forf.commands.leave.success.multiple", changed), true);
        return 1;
    }

    @SuppressWarnings("SameReturnValue")
    private static int runSolo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (PERSISTENT_DATA.isStarted()) {
            throw ALREADY_STARTED.create();
        }

        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        leavePlayer(player.getGameProfile());

        sendFeedback(context, Text.translatable("forf.commands.leave.success.solo", player.getGameProfile().getName()), true);
        return 1;
    }

    private static void leavePlayer(GameProfile profile) throws CommandSyntaxException {
        if (!Util.isForfPlayer(profile.getId())) {
            throw new SimpleCommandExceptionType(
                    Text.translatable("forf.commands.leave.exceptions.alreadyLeft", profile.getName())
            ).create();
        }
        Util.removePlayer(profile.getId());
    }
}
