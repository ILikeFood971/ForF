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
import net.ilikefood971.forf.timer.PvPTimer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.ilikefood971.forf.util.Util.sendFeedback;
import static net.minecraft.server.command.CommandManager.*;

public class TimerCommands {
    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, RegistrationEnvironment environment) {
        dispatcher.register(
                literal("forf")
                        .then(
                                literal("pvp")
                                        .then(
                                                literal("on")
                                                        .then(
                                                                argument("minutes", IntegerArgumentType.integer(1))
                                                                        .executes(TimerCommands::turnPvpOn)
                                                        )
                                                        .executes(TimerCommands::turnPvpOn)
                                        )
                                        .then(
                                                literal("off")
                                                        .then(
                                                                argument("minutes", IntegerArgumentType.integer(1))
                                                                        .executes(TimerCommands::turnPvpOff)
                                                        )
                                                        .executes(TimerCommands::turnPvpOff)
                                        )
                                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(3))
                        )
        );
    }
    
    private static int turnPvpOn(CommandContext<ServerCommandSource> context) {
        if (!net.ilikefood971.forf.util.Util.PERSISTENT_DATA.started && !net.ilikefood971.forf.util.Util.CONFIG.pvPTimer().enabled()) {
            sendFeedback(context, Text.translatable("forf.commands.timer.disabledAndNotStarted"), false);
            return -1;
        } else if (!net.ilikefood971.forf.util.Util.PERSISTENT_DATA.started) {
            sendFeedback(context, Text.translatable("forf.notStarted"), false);
            return -1;
        } else if (!net.ilikefood971.forf.util.Util.CONFIG.pvPTimer().enabled()) {
            sendFeedback(context, Text.translatable("forf.commands.timer.disabled"), false);
            return -1;
        }
        
        try {
            return PvPTimer.changePvpTimer(PvPTimer.PvPState.ON, context.getArgument("minutes", int.class) * 60) ? 1 : -1;
        } catch (IllegalArgumentException e) {
            return PvPTimer.changePvpTimer(PvPTimer.PvPState.ON) ? 1 : -1;
        }
    }
    
    private static int turnPvpOff(CommandContext<ServerCommandSource> context) {
        if (!net.ilikefood971.forf.util.Util.PERSISTENT_DATA.started && !net.ilikefood971.forf.util.Util.CONFIG.pvPTimer().enabled()) {
            sendFeedback(context, Text.translatable("forf.commands.timer.disabledAndNotStarted"), false);
            return -1;
        } else if (!net.ilikefood971.forf.util.Util.PERSISTENT_DATA.started) {
            sendFeedback(context, Text.translatable("forf.notStarted"), false);
            return -1;
        } else if (!net.ilikefood971.forf.util.Util.CONFIG.pvPTimer().enabled()) {
            sendFeedback(context, Text.translatable("forf.commands.timer.disabled"), false);
            return -1;
        }
        
        
        try {
            return PvPTimer.changePvpTimer(PvPTimer.PvPState.OFF, context.getArgument("minutes", int.class) * 60) ? 1 : -1;
        } catch (IllegalArgumentException e) {
            return PvPTimer.changePvpTimer(PvPTimer.PvPState.OFF) ? 1 : -1;
        }
    }
}
