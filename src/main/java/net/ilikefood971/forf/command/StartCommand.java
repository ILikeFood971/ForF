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
import net.ilikefood971.forf.Forf;
import net.ilikefood971.forf.event.PlayerJoinEvent;
import net.ilikefood971.forf.timer.PvPTimer;
import net.ilikefood971.forf.util.ForfManager;
import net.ilikefood971.forf.util.mixinInterfaces.IEntityDataSaver;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.ilikefood971.forf.Forf.*;
import static net.ilikefood971.forf.command.Util.*;
import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings("SameReturnValue")
public class StartCommand {
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
    
    private static int run(CommandContext<ServerCommandSource> context) {
        
        if (PERSISTENT_DATA.started) {
            sendFeedback(context, Text.translatable("forf.alreadyStarted"), false);
            return -1;
        }
        if (CONFIG.startingLives() <= 0) {
            sendFeedback(context, Text.translatable("forf.commands.message.start.insufficientAmountLives", CONFIG.startingLives()), false);
            return -1;
        }
        if (PERSISTENT_DATA.forfPlayersUUIDs.isEmpty()) {
            sendFeedback(context, Text.translatable("forf.commands.message.start.insufficientAmountPlayers"), false);
            return -1;
        }
        if (CONFIG.startingLives() > 1 && PERSISTENT_DATA.forfPlayersUUIDs.size() > 1) {
            sendFeedback(context, Text.translatable("forf.commands.message.start.multiplePlayersAndLives", CONFIG.startingLives(), PERSISTENT_DATA.forfPlayersUUIDs.size()), true);
        } else if (PERSISTENT_DATA.forfPlayersUUIDs.size() > 1) {
            sendFeedback(context, Text.translatable("forf.commands.message.start.multiplePlayers", PERSISTENT_DATA.forfPlayersUUIDs.size()), true);
        } else if (CONFIG.startingLives() > 1) {
            sendFeedback(context, Text.translatable("forf.commands.message.start.multipleLives", CONFIG.startingLives()), true);
        } else {
            sendFeedback(context, Text.translatable("forf.commands.message.start.single"), true);
        }
        
        ForfManager.setupForf(context);
        PERSISTENT_DATA.started = true;
        
        ServerScoreboard scoreboard = Forf.SERVER.getScoreboard();
        scoreboard.setObjectiveSlot(0, livesObjective);
        
        for (ServerPlayerEntity serverPlayerEntity : context.getSource().getServer().getPlayerManager().getPlayerList()) {
            if (!PERSISTENT_DATA.forfPlayersUUIDs.contains(serverPlayerEntity.getUuidAsString()) && !CONFIG.spectators()) {
                serverPlayerEntity.networkHandler.disconnect(Text.translatable("forf.disconnect.noSpectators"));
            } else if (!PERSISTENT_DATA.forfPlayersUUIDs.contains(serverPlayerEntity.getUuidAsString()) && CONFIG.spectators()) {
                serverPlayerEntity.changeGameMode(CONFIG.spectatorGamemode());
            }
            
            serverPlayerEntity.networkHandler.sendPacket(PlayerJoinEvent.getHeaderPacket());
            
            scoreboard.getPlayerScore(serverPlayerEntity.getEntityName(), livesObjective).setScore(((IEntityDataSaver) serverPlayerEntity).getLives());
        }
        
        if (CONFIG.pvPTimer().enabled()) {
            PvPTimer.changePvpTimer(PvPTimer.PvPState.OFF, CONFIG.pvPTimer().maxRandomOffTime() * 60);
            context.getSource().getServer().setPvpEnabled(false);
        }
        return 1;
    }
}
