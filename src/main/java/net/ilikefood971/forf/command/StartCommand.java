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
import net.ilikefood971.forf.assassin.AssassinHandler;
import net.ilikefood971.forf.data.DataHandler;
import net.ilikefood971.forf.data.PlayerData;
import net.ilikefood971.forf.data.PlayerDataSet;
import net.ilikefood971.forf.event.PlayerJoinEvent;
import net.ilikefood971.forf.timer.PvPTimer;
import net.ilikefood971.forf.util.LivesHelper;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Set;
import java.util.UUID;

import static net.ilikefood971.forf.command.CommandUtil.ALREADY_STARTED;
import static net.ilikefood971.forf.util.Util.*;
import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings({"SameReturnValue", "unused"})
public class StartCommand {

    public static final SimpleCommandExceptionType INSUFFICIENT_AMOUNT_PLAYERS = new SimpleCommandExceptionType(
            Text.translatable("forf.commands.start.exceptions.insufficientAmountPlayers")
    );
    public static final SimpleCommandExceptionType INSUFFICIENT_AMOUNT_LIVES = new SimpleCommandExceptionType(
            Text.translatable("forf.commands.start.exceptions.insufficientAmountLives")
    );

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
        // Check all conditions for failure
        if (DataHandler.getInstance().isStarted()) {
            throw ALREADY_STARTED.create();
        } else if (CONFIG.startingLives() <= 0) {
            throw INSUFFICIENT_AMOUNT_LIVES.create();
        } else if (PlayerDataSet.getInstance().getDataSet().isEmpty()) {
            throw INSUFFICIENT_AMOUNT_PLAYERS.create();
        }

        // Send feedback to the command sender
        context.getSource().sendFeedback(() -> Text.translatable("forf.commands.start.starting", CONFIG.startingLives()), true);

        // Setup everything necessary for forf
        setupForf(context);
        return 1;
    }

    public static void setupForf(CommandContext<ServerCommandSource> context) {
        DataHandler.getInstance().setStarted(true);
        DataHandler.getInstance().setFirstKill(true);
        DataHandler.getInstance().setTenKillsLifeQuest(true);
        FAKE_SCOREBOARD.setListSlot();
        SERVER.getPlayerManager().sendToAll(PlayerJoinEvent.getHeaderPacket());

        Set<UUID> uuids = PlayerDataSet.getInstance().getDataSet().keySet();

        // Set all lives
        for (UUID uuid : uuids) {
            PlayerData playerData = PlayerDataSet.getInstance().get(uuid);
            if (playerData.getPlayerType() != PlayerData.PlayerType.PLAYER) {
                if (playerData.getPlayerType() == PlayerData.PlayerType.ASSASSIN) {
                    LivesHelper.set(uuid, CONFIG.assassinStartingLives());
                }
                continue;
            }
            LivesHelper.set(uuid, CONFIG.startingLives());
        }

        // Deal with all non forf players
        for (ServerPlayerEntity serverPlayerEntity : SERVER.getPlayerManager().getPlayerList()) {
            PlayerData playerData = PlayerDataSet.getInstance().get(serverPlayerEntity.getUuid());
            if (!playerData.getPlayerType().isForfPlayer()) {
                if (!CONFIG.spectators()) {
                    serverPlayerEntity.networkHandler.disconnect(Text.translatable("forf.disconnect.noSpectators"));
                } else if (CONFIG.spectators()) {
                    playerData.setPlayerType(PlayerData.PlayerType.SPECTATOR);
                    serverPlayerEntity.changeGameMode(CONFIG.spectatorGamemode());
                }
            }
        }

        if (CONFIG.pvPTimer().enabled()) {
            PvPTimer.changePvpTimer(PvPTimer.PvPState.OFF, CONFIG.pvPTimer().maxRandomOffTime() * 60);
            SERVER.setPvpEnabled(false); // Just in case previous forf data was left behind
        }
        if (AssassinHandler.getInstance().isAssassinOnline()) {
            PvPTimer.changePvpTimer(PvPTimer.PvPState.ASSASSIN, 0);
        }
    }
}
