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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.ilikefood971.forf.data.PlayerData;
import net.ilikefood971.forf.util.LivesHelper;
import net.ilikefood971.forf.util.Util;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.UUID;

import static net.ilikefood971.forf.command.CommandUtil.getProfiles;
import static net.minecraft.server.command.CommandManager.*;

public class LeaveCommand {

    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, RegistrationEnvironment environment) {
        dispatcher.register(
                literal("forf")
                        .then(
                                literal("leave")
                                        .then(
                                                argument("players", GameProfileArgumentType.gameProfile())
                                                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(3))
                                                        .executes(context -> run(context, false, false))
                                                        .then(
                                                                literal("late")
                                                                        .executes(context -> run(context, false, true))
                                                        )
                                        )
                                        .executes(context -> run(context, true, false))
                        )
        );
    }

    @SuppressWarnings("SameReturnValue")
    private static int run(CommandContext<ServerCommandSource> context, boolean solo, boolean late) throws CommandSyntaxException {
        Collection<GameProfile> profiles = getProfiles(context, solo, late);

        int changed = 0;
        for (GameProfile profile : profiles) {
            UUID id = profile.getId();
            if (profiles.size() == 1 && !Util.isForfPlayer(id)) {
                throw new SimpleCommandExceptionType(
                        Text.translatable("forf.commands.leave.exceptions.alreadyLeft", profile.getName())
                ).create();
            }
            ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(id);
            if (late && player != null) {
                LivesHelper.set(player, 0);
            }
            Util.PERSISTENT_DATA.getPlayerDataSet().get(id).setPlayerType(PlayerData.PlayerType.UNKNOWN);
            changed++;
        }

        Text feedback = solo ? Text.translatable("forf.commands.leave.success.solo", profiles.iterator().next().getName()) : Text.translatable("forf.commands.leave.success.multiple", changed);
        context.getSource().sendFeedback(() -> feedback, true);
        return 1;
    }
}
