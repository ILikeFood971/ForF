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
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.ilikefood971.forf.util.Lives;
import net.ilikefood971.forf.util.Util;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


import static net.ilikefood971.forf.command.CommandUtil.NOT_STARTED;
import static net.ilikefood971.forf.util.Util.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LivesCommands {
    
    public static final SimpleCommandExceptionType INVALID_EXECUTOR = new SimpleCommandExceptionType(
            Text.translatable("forf.commands.lives.exceptions.invalidExecutor")
    );
    public static final SimpleCommandExceptionType NOT_YOURSELF = new SimpleCommandExceptionType(
            Text.translatable("forf.commands.lives.exceptions.notYourself")
    );
    public static final SimpleCommandExceptionType NOT_ENOUGH_LIVES = new SimpleCommandExceptionType(
            Text.translatable("forf.commands.lives.exceptions.notEnoughLives")
    );
    public static final SimpleCommandExceptionType TOO_MANY_LIVES = new SimpleCommandExceptionType(
            Text.translatable("forf.commands.lives.exceptions.tooManyLives")
    );
    
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
    
    private static int setPlayersLives(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!PERSISTENT_DATA.isStarted()) {
            throw NOT_STARTED.create();
        }
        int lives = IntegerArgumentType.getInteger(context, "lives");
        
        for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "players")) {
            Lives playerLives = new Lives(player);
            if (!Util.isForfPlayer(player)) {
                throw new SimpleCommandExceptionType(
                        Text.translatable("forf.commands.lives.exceptions.invalidTarget", player.getGameProfile().getName())
                ).create();
            }
            playerLives.set(lives);
            
            sendFeedback(
                    context,
                    Text.translatable("forf.commands.lives.success",
                            player.getGameProfile().getName(),
                            playerLives.get()
                    ),
                    true
            );
        }
        return 1;
    }
    
    private static int givePlayerLives(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!PERSISTENT_DATA.isStarted()) {
            throw NOT_STARTED.create();
        }
        
        ServerPlayerEntity executor = context.getSource().getPlayerOrThrow();
        ServerPlayerEntity recipient = EntityArgumentType.getPlayer(context, "recipient");

        Lives executorLives = new Lives(executor);
        Lives recipientLives = new Lives(recipient);
        
        int giftedLives = IntegerArgumentType.getInteger(context, "amount");

        // Check all the cases that aren't allowed
        if (executor.equals(recipient)) {
            throw NOT_YOURSELF.create();
        } else if (!Util.isForfPlayer(executor)) {
            throw INVALID_EXECUTOR.create();
        } else if (!Util.isForfPlayer(recipient)) {
            throw new SimpleCommandExceptionType(
                    Text.translatable("forf.commands.lives.exceptions.invalidTarget", recipient.getGameProfile().getName())
            ).create();
        } else if (executorLives.get() - recipientLives.get() <= 0) {
            throw NOT_ENOUGH_LIVES.create();
        } else if (recipientLives.get() + giftedLives > CONFIG.startingLives() && !CONFIG.overfill()) {
            throw TOO_MANY_LIVES.create();
        }
        
        // Transfer the lives
        recipientLives.increment(giftedLives);
        executorLives.decrement(giftedLives);
        
        sendFeedback(
                context,
                Text.translatable("forf.commands.lives.give", recipient.getGameProfile().getName(), giftedLives),
                true
        );
        
        return 1;
    }
}
