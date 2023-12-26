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
import net.ilikefood971.forf.util.ForfManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


import static net.ilikefood971.forf.command.Util.ALREADY_STARTED;
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
        if (PERSISTENT_DATA.started) {
            throw ALREADY_STARTED.create();
        }
        if (CONFIG.startingLives() <= 0) {
            throw INSUFFICIENT_AMOUNT_LIVES.create();
        }
        if (PERSISTENT_DATA.forfPlayersUUIDs.isEmpty()) {
            throw INSUFFICIENT_AMOUNT_PLAYERS.create();
        }
        if (CONFIG.startingLives() > 1 && PERSISTENT_DATA.forfPlayersUUIDs.size() > 1) {
            sendFeedback(context, Text.translatable("forf.commands.start.multiplePlayersAndLives", CONFIG.startingLives(), PERSISTENT_DATA.forfPlayersUUIDs.size()), true);
        } else if (PERSISTENT_DATA.forfPlayersUUIDs.size() > 1) {
            sendFeedback(context, Text.translatable("forf.commands.start.multiplePlayers", PERSISTENT_DATA.forfPlayersUUIDs.size()), true);
        } else if (CONFIG.startingLives() > 1) {
            sendFeedback(context, Text.translatable("forf.commands.start.multipleLives", CONFIG.startingLives()), true);
        } else {
            sendFeedback(context, Text.translatable("forf.commands.start.single"), true);
        }
        
        PERSISTENT_DATA.started = true;
        fakeScoreboard.setListSlot();
        SERVER.getPlayerManager().sendToAll(PlayerJoinEvent.getHeaderPacket());
        
        ForfManager.setupForf(context);
        
        
        for (ServerPlayerEntity serverPlayerEntity : context.getSource().getServer().getPlayerManager().getPlayerList()) {
            if (!PERSISTENT_DATA.forfPlayersUUIDs.contains(serverPlayerEntity.getUuidAsString()) && !CONFIG.spectators()) {
                serverPlayerEntity.networkHandler.disconnect(Text.translatable("forf.disconnect.noSpectators"));
            } else if (!PERSISTENT_DATA.forfPlayersUUIDs.contains(serverPlayerEntity.getUuidAsString()) && CONFIG.spectators()) {
                serverPlayerEntity.changeGameMode(CONFIG.spectatorGamemode());
            }
        }
        
        if (CONFIG.pvPTimer().enabled()) {
            PvPTimer.changePvpTimer(PvPTimer.PvPState.OFF, CONFIG.pvPTimer().maxRandomOffTime() * 60);
            context.getSource().getServer().setPvpEnabled(false);
        }
        return 1;
    }
}
