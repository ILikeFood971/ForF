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
import net.ilikefood971.forf.event.PlayerJoinEvent;
import net.ilikefood971.forf.timer.PvPTimer;
import net.ilikefood971.forf.util.Lives;
import net.ilikefood971.forf.util.Util;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.ilikefood971.forf.command.CommandUtil.ALREADY_STARTED;
import static net.ilikefood971.forf.util.Util.*;
import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings("SameReturnValue")
public class StartCommand {

    public static final SimpleCommandExceptionType INSUFFICIENT_AMOUNT_PLAYERS = new SimpleCommandExceptionType(
            Text.translatable("forf.commands.start.exceptions.insufficientAmountPlayers")
    );
    public static final SimpleCommandExceptionType INSUFFICIENT_AMOUNT_LIVES = new SimpleCommandExceptionType(
            Text.translatable("forf.commands.start.exceptions.insufficientAmountLives")
    );

    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                literal("forf")
                        .then(
                                literal("start")
                                        .requires(source -> source.hasPermissionLevel(3))
                                        .executes(StartCommand::run)
                        )
        );
    }

    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Map<UUID, Integer> playersAndLives = PERSISTENT_DATA.getPlayersAndLives();

        // Check all conditions for failure
        if (PERSISTENT_DATA.isStarted()) {
            throw ALREADY_STARTED.create();
        } else if (CONFIG.startingLives() <= 0) {
            throw INSUFFICIENT_AMOUNT_LIVES.create();
        } else if (playersAndLives.isEmpty()) {
            throw INSUFFICIENT_AMOUNT_PLAYERS.create();
        }

        // Send feedback to the command sender
        if (CONFIG.startingLives() > 1 && playersAndLives.size() > 1) {
            sendFeedback(context, Text.translatable("forf.commands.start.multiplePlayersAndLives", CONFIG.startingLives(), playersAndLives.size()), true);
        } else if (playersAndLives.size() > 1) {
            sendFeedback(context, Text.translatable("forf.commands.start.multiplePlayers", playersAndLives.size()), true);
        } else if (CONFIG.startingLives() > 1) {
            sendFeedback(context, Text.translatable("forf.commands.start.multipleLives", CONFIG.startingLives()), true);
        } else {
            sendFeedback(context, Text.translatable("forf.commands.start.single"), true);
        }

        // Setup everything necessary for forf
        setupForf(context);
        return 1;
    }

    public static void setupForf(CommandContext<ServerCommandSource> context) {
        PERSISTENT_DATA.setStarted(true);
        PERSISTENT_DATA.setFirstKill(true);
        FAKE_SCOREBOARD.setListSlot();
        SERVER.getPlayerManager().sendToAll(PlayerJoinEvent.getHeaderPacket());

        Set<UUID> uuids = PERSISTENT_DATA.getPlayersAndLives().keySet();
        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();

        // Set all lives
        for (UUID uuid : uuids) {
            ServerPlayerEntity player = playerManager.getPlayer(uuid);
            if (player != null) { // Online Players
                Lives.set(player, CONFIG.startingLives());
            } else { // Offline Players
                PERSISTENT_DATA.getPlayersAndLives().put(uuid, CONFIG.startingLives());
            }
        }

        // Deal with all non forf players
        for (ServerPlayerEntity serverPlayerEntity : SERVER.getPlayerManager().getPlayerList()) {
            if (!Util.isForfPlayer(serverPlayerEntity)) {
                if (!CONFIG.spectators()) {
                    serverPlayerEntity.networkHandler.disconnect(Text.translatable("forf.disconnect.noSpectators"));
                } else if (CONFIG.spectators()) {
                    serverPlayerEntity.changeGameMode(CONFIG.spectatorGamemode());
                }
            }
        }

        if (CONFIG.pvPTimer().enabled()) {
            PvPTimer.changePvpTimer(PvPTimer.PvPState.OFF, CONFIG.pvPTimer().maxRandomOffTime() * 60);
            SERVER.setPvpEnabled(false); // Just in case previous forf data was left behind
        }
    }
}
