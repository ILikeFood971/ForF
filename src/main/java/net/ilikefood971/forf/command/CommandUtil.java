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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.ilikefood971.forf.data.DataHandler;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;

public class CommandUtil {
    public static final SimpleCommandExceptionType NOT_STARTED = new SimpleCommandExceptionType(
            Text.translatable("forf.commands.exceptions.notStarted")
    );
    public static final SimpleCommandExceptionType ALREADY_STARTED = new SimpleCommandExceptionType(
            Text.translatable("forf.commands.exceptions.alreadyStarted")
    );

    public static Collection<GameProfile> getProfiles(CommandContext<ServerCommandSource> context, boolean solo, boolean late) throws CommandSyntaxException {
        if (DataHandler.getInstance().isStarted() && !late) {
            throw ALREADY_STARTED.create();
        }

        Collection<GameProfile> profiles;
        if (solo) {
            profiles = List.of(context.getSource().getPlayerOrThrow().getGameProfile());
        } else {
            profiles = GameProfileArgumentType.getProfileArgument(context, "players");
        }
        return profiles;
    }
}
