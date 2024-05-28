/*
 * This file is part of the Friend or Foe project, licensed under the
 * GNU General Public License v3.0
 *
 * Copyright (C) 2024  ILikeFood971 and contributors
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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.ilikefood971.forf.assassin.AssassinHandler;
import net.ilikefood971.forf.timer.PvPTimer;
import net.ilikefood971.forf.util.LivesHelper;
import net.ilikefood971.forf.util.Util;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AssassinCommand {
    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                literal("forf")
                        .then(
                                literal("assassin")
                                        .then(
                                                literal("set")
                                                        .then(
                                                                argument("player", GameProfileArgumentType.gameProfile())
                                                                        .executes(AssassinCommand::run)
                                                        )
                                        )
                                        .then(
                                                literal("clear")
                                                        .executes(context -> {
                                                            AssassinHandler.getInstance().setAssassin(null);
                                                            context.getSource().sendFeedback(() -> Text.translatable("forf.commands.assassin.clear"), false);
                                                            return 0;
                                                        })
                                        )
                        )
        );
    }

    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
        if (profiles.size() > 1) {
            throw new SimpleCommandExceptionType(Text.translatable("forf.commands.join.exceptions.assassinMultiple")).create();
        }
        GameProfile assassin = profiles.iterator().next();

        AssassinHandler.getInstance().setAssassin(assassin.getId());
        context.getSource().sendFeedback(() -> Text.translatable("forf.commands.assassin.set", assassin.getName()), false);
        if (AssassinHandler.getInstance().isAssassinOnline()) PvPTimer.changePvpTimer(PvPTimer.PvPState.ASSASSIN);
        LivesHelper.set(assassin.getId(), Util.CONFIG.assassinStartingLives());
        return 0;
    }
}
