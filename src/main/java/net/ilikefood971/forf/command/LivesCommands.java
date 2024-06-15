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
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.ilikefood971.forf.data.DataHandler;
import net.ilikefood971.forf.data.PlayerData;
import net.ilikefood971.forf.data.PlayerDataSet;
import net.ilikefood971.forf.util.LivesHelper;
import net.ilikefood971.forf.util.Util;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.ilikefood971.forf.command.CommandUtil.NOT_STARTED;
import static net.ilikefood971.forf.command.CommandUtil.permission;
import static net.ilikefood971.forf.util.Util.CONFIG;
import static net.ilikefood971.forf.util.Util.SERVER;
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
                                                                argument("players", GameProfileArgumentType.gameProfile())
                                                                        .then(
                                                                                argument("lives", IntegerArgumentType.integer(0))
                                                                                        .executes(LivesCommands::setPlayersLives)
                                                                        )
                                                        )
                                                        .requires(permission("lives.set", 3))
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
                                                        .requires(permission("lives.give", 0))
                                        )
                        )
        );
    }

    @SuppressWarnings("SameReturnValue")
    private static int setPlayersLives(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!DataHandler.getInstance().isStarted()) {
            throw NOT_STARTED.create();
        }
        int lives = IntegerArgumentType.getInteger(context, "lives");

        for (GameProfile profile : GameProfileArgumentType.getProfileArgument(context, "players")) {
            if (!Util.isForfPlayer(profile.getId())) {
                throw new SimpleCommandExceptionType(
                        Text.translatable("forf.commands.lives.exceptions.invalidTarget", profile.getName())
                ).create();
            }
            ServerPlayerEntity player = SERVER.getPlayerManager().getPlayer(profile.getId());
            int newLives;
            if (player != null) {
                LivesHelper playerLives = new LivesHelper(player);
                playerLives.set(lives);
                newLives = playerLives.get();
            } else {
                if (!CONFIG.overfill()) {
                    // Prevent the player from going over if overfill is disabled
                    lives = Math.min(lives, CONFIG.startingLives());
                }
                PlayerDataSet.getInstance().get(profile.getId()).setLives(lives);
                newLives = lives;
            }

            context.getSource().sendFeedback(() -> Text.translatable("forf.commands.lives.success", profile.getName(), newLives), true);
        }
        return 1;
    }

    @SuppressWarnings("SameReturnValue")
    private static int givePlayerLives(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!DataHandler.getInstance().isStarted()) {
            throw NOT_STARTED.create();
        }

        ServerPlayerEntity executor = context.getSource().getPlayerOrThrow();
        ServerPlayerEntity recipient = EntityArgumentType.getPlayer(context, "recipient");

        LivesHelper executorLives = new LivesHelper(executor);
        LivesHelper recipientLives = new LivesHelper(recipient);
        PlayerData executorData = PlayerDataSet.getInstance().get(executor.getUuid());
        PlayerData recipientData = PlayerDataSet.getInstance().get(recipient.getUuid());

        int giftedLives = IntegerArgumentType.getInteger(context, "amount");

        // Check all the cases that aren't allowed
        if (executor.equals(recipient)) {
            throw NOT_YOURSELF.create();
        } else if (executorData.getPlayerType() != PlayerData.PlayerType.PLAYER) {
            throw INVALID_EXECUTOR.create();
        } else if (recipientData.getPlayerType() != PlayerData.PlayerType.PLAYER) {
            throw new SimpleCommandExceptionType(
                    Text.translatable("forf.commands.lives.exceptions.invalidTarget", recipient.getGameProfile().getName())
            ).create();
        } else if (executorLives.get() - giftedLives <= 0) {
            throw NOT_ENOUGH_LIVES.create();
        } else if (recipientLives.get() + giftedLives > CONFIG.startingLives() && !CONFIG.overfill()) {
            throw TOO_MANY_LIVES.create();
        }

        // Transfer the lives
        recipientLives.increment(giftedLives);
        executorLives.decrement(giftedLives);

        context.getSource().sendFeedback(() -> Text.translatable("forf.commands.lives.give", recipient.getGameProfile().getName(), giftedLives), true);

        return 1;
    }
}
