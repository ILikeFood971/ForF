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
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;

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
                                                argument("players", EntityArgumentType.players())
                                                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(3))
                                                        .executes(LeaveCommand::run)
                                        )
                                        .executes(LeaveCommand::run)
                        )
        );
    }
    
    private static int run(CommandContext<ServerCommandSource> context) {
        if (net.ilikefood971.forf.util.Util.PERSISTENT_DATA.started) {
            sendFeedback(context, Text.translatable("forf.alreadyStarted"), false);
            return -1;
        }
        
        Collection<ServerPlayerEntity> players;
        try {
            players = EntityArgumentType.getPlayers(context, "players");
        } catch (IllegalArgumentException | CommandSyntaxException e) {
            players = new ArrayList<>();
            players.add(context.getSource().getPlayer());
        }
        for (ServerPlayerEntity player : players) {
            String playerName = player.getGameProfile().getName();
            String playerUuid = player.getUuidAsString();
            
            if (!net.ilikefood971.forf.util.Util.PERSISTENT_DATA.forfPlayersUUIDs.contains(playerUuid)) {
                sendFeedback(context, Text.translatable("forf.commands.leave.alreadyLeft", playerName), false);
                return -1;
            }
            
            
            net.ilikefood971.forf.util.Util.PERSISTENT_DATA.forfPlayersUUIDs.remove(playerUuid);
            sendFeedback(context, Text.translatable("forf.commands.leave.success", playerName), true);
        }
        return 1;
    }
}
