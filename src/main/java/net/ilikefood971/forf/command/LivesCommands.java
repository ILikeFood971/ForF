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
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.ilikefood971.forf.util.mixinInterfaces.IEntityDataSaver;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.ilikefood971.forf.util.Util.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LivesCommands {
    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                literal("forf")
                        .then(
                                literal("lives")
                                        .then(
                                                literal("set")
                                                        .then(
                                                                argument("players", EntityArgumentType.players())
                                                                        .then(
                                                                                argument("lives", IntegerArgumentType.integer(0))
                                                                                        .executes(LivesCommands::setPlayersLives)
                                                                        )
                                                        )
                                                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(3))
                                        )
                                        .then(
                                                literal("give")
                                                        .then(
                                                                argument("recipient", EntityArgumentType.player())
                                                                        .then(
                                                                                argument("amount", IntegerArgumentType.integer(1))
                                                                                        .executes(LivesCommands::givePlayerLives)
                                                                        )
                                                        )
                                        )
                        )
        );
    }
    
    private static int setPlayersLives(CommandContext<ServerCommandSource> context) {
        if (!PERSISTENT_DATA.started) {
            sendFeedback(context, Text.translatable("forf.notStarted"), false);
            return -1;
        }
        int lives = IntegerArgumentType.getInteger(context, "lives");
        try {
            for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "players")) {
                if (!PERSISTENT_DATA.forfPlayersUUIDs.contains(player.getUuidAsString())) {
                    sendFeedback(context, Text.translatable("forf.commands.lives.playerNotValid", player.getEntityName()), false);
                    continue;
                }
                ((IEntityDataSaver) player).setLives(lives);
                sendFeedback(context, Text.translatable("forf.commands.lives.success", player.getEntityName(), lives), true);
            }
            
        } catch (CommandSyntaxException e) {
            LOGGER.error(e.toString());
            return -1;
        }
        return 1;
    }
    
    private static int givePlayerLives(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!PERSISTENT_DATA.started) {
            sendFeedback(context, Text.translatable("forf.notStarted"), false);
            return -1;
        }
        
        ServerPlayerEntity executor = context.getSource().getPlayerOrThrow();
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "recipient");

        int amount = IntegerArgumentType.getInteger(context, "amount");
        int currentLives = ((IEntityDataSaver) executor).getLives();

        if (executor.equals(player)) {
            sendFeedback(context, Text.translatable("forf.commands.lives.notYourself"), false);
            return -1;
        } else if (!PERSISTENT_DATA.forfPlayersUUIDs.contains(executor.getUuidAsString())) {
            sendFeedback(context, Text.translatable("forf.commands.lives.notPlayer"), false);
            return -1;
        } else if (!PERSISTENT_DATA.forfPlayersUUIDs.contains(player.getUuidAsString())) {
            sendFeedback(context, Text.translatable("forf.commands.lives.playerNotValid"), false);
            return -1;
        } else if (currentLives - amount <= 0) {
            sendFeedback(context, Text.translatable("forf.commands.lives.notEnoughLives"), false);
            return -1;
        }
        
        ((IEntityDataSaver) player).setLives(currentLives + amount);
        ((IEntityDataSaver) executor).setLives(currentLives - amount);
        sendFeedback(context, Text.translatable("forf.commands.lives.give", player.getEntityName(), amount), true);
        
        return 1;
    }
}
