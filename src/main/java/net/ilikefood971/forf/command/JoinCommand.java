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
                                        .executes(JoinCommand::run)
                        )
        );
    }
    
    private static int run(CommandContext<ServerCommandSource> context) {
        if (net.ilikefood971.forf.util.Util.PERSISTENT_DATA.started) {
            net.ilikefood971.forf.util.Util.sendFeedback(context, Text.translatable("forf.alreadyStarted"), false);
            return JoinResult.ALREADY_STARTED.getValue();
        }
        
        Collection<ServerPlayerEntity> players;
        try {
            players = EntityArgumentType.getPlayers(context, "players");
        } catch (IllegalArgumentException | CommandSyntaxException e) {
            players = new ArrayList<>();
            players.add(context.getSource().getPlayer());
        }
        for (ServerPlayerEntity player : players) {
            String playerName = player.getEntityName();
            String playerUuid = player.getUuidAsString();
            
            if (net.ilikefood971.forf.util.Util.PERSISTENT_DATA.forfPlayersUUIDs.contains(playerUuid)) {
                net.ilikefood971.forf.util.Util.sendFeedback(context, Text.translatable("forf.commands.message.join.alreadyAdded", playerName), false);
                return JoinResult.ALREADY_ADDED.getValue();
            }
            
            
            net.ilikefood971.forf.util.Util.PERSISTENT_DATA.forfPlayersUUIDs.add(playerUuid);
            net.ilikefood971.forf.util.Util.sendFeedback(context, Text.translatable("forf.commands.message.join.success", playerName), true);
        }
        return JoinResult.SUCCESS.getValue();
    }
    
    
    private enum JoinResult {
        SUCCESS(1),
        ALREADY_ADDED(-1),
        ALREADY_STARTED(-1);
        
        private final int value;
        
        JoinResult(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
}
