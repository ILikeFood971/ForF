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
import net.ilikefood971.forf.assassin.AssassinHandler;
import net.ilikefood971.forf.data.DataHandler;
import net.ilikefood971.forf.data.PlayerData;
import net.ilikefood971.forf.data.PlayerDataSet;
import net.ilikefood971.forf.event.PlayerJoinEvent;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.text.Text;

import static net.ilikefood971.forf.command.CommandUtil.NOT_STARTED;
import static net.ilikefood971.forf.command.CommandUtil.permission;
import static net.ilikefood971.forf.util.Util.FAKE_SCOREBOARD;
import static net.ilikefood971.forf.util.Util.SERVER;
import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings("SameReturnValue")
public class StopCommand {
    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("forf")
                .then(
                        literal("stop")
                                .requires(permission("stop", 3))
                                .executes(StopCommand::run)
                )
        );
    }

    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // Send a message that says stopping forf but only send to ops if forf has already started
        if (!DataHandler.getInstance().isStarted()) {
            throw NOT_STARTED.create();
        }
        context.getSource().sendFeedback(() -> Text.translatable("forf.commands.stop.stopping"), true);

        DataHandler dataHandler = DataHandler.getInstance();
        dataHandler.setStarted(false);

        // Remove the Header from the tablist
        SERVER.getPlayerManager().sendToAll(PlayerJoinEvent.getEmptyHeaderPacket());

        AssassinHandler.getInstance().setAssassin(null);

        // Set all lives to 0
        PlayerDataSet.getInstance().getDataSet().forEach((uuid, playerData) -> {
            playerData.setLives(0);
            playerData.setPlayerType(PlayerData.PlayerType.UNKNOWN);
        });


        boolean pvp;
        if (SERVER instanceof MinecraftDedicatedServer dedicatedServer) {
            pvp = dedicatedServer.getProperties().pvp;
        } else pvp = true;

        SERVER.setPvpEnabled(pvp);
        FAKE_SCOREBOARD.clearListSlot();
        return 1;
    }
}
