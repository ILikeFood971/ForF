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
import net.ilikefood971.forf.data.DataHandler;
import net.ilikefood971.forf.data.PlayerDataSet;
import net.ilikefood971.forf.util.Util;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

import static net.ilikefood971.forf.command.CommandUtil.permission;
import static net.minecraft.server.command.CommandManager.literal;

public class KillsCommand {
    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                literal("kills")
                        .requires(permission("kills", 0))
                        .executes(KillsCommand::run)
        );
    }

    @SuppressWarnings("SameReturnValue")
    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!DataHandler.getInstance().isStarted()) throw CommandUtil.NOT_STARTED.create();
        MutableText text = Text.translatable("forf.commands.kills.header");
        for (UUID uuid : PlayerDataSet.getInstance().getDataSet().keySet()) {
            GameProfile profile = Util.getProfile(uuid);
            String name = profile.getName();
            int kills = Util.getKills(uuid);

            text.append(Text.literal("\n" + name + ": ").formatted(Formatting.GRAY))
                    .append(Text.literal(String.valueOf(kills)).formatted(Formatting.YELLOW));
        }
        context.getSource().sendFeedback(() -> text, false);
        return 1;
    }
}
