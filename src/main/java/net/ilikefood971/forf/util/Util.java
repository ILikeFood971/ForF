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

package net.ilikefood971.forf.util;

import com.mojang.brigadier.context.CommandContext;
import net.ilikefood971.forf.PersistentData;
import net.ilikefood971.forf.config.Config;
//#if MC>=12020
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
//#endif
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
    
    
    public static final String MOD_ID = "forf";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Config CONFIG = Config.loadFromFile();
    public static PersistentData PERSISTENT_DATA;
    public static MinecraftServer SERVER;
    public static ScoreboardObjective livesObjective;
    
    public static void sendFeedback(CommandContext<ServerCommandSource> context, Text message, boolean broadcast) {
        context.getSource().sendFeedback(
                //#if MC >= 12000
                () ->
                //#endif
                message, broadcast);
    }
    
    public static void setListSlot() {
        SERVER.getScoreboard().setObjectiveSlot(
                //#if MC>=12020
                ScoreboardDisplaySlot.LIST
                //#else
                //$$ 0
                //#endif
                , livesObjective);
    }
    public static void clearListSlot() {
        SERVER.getScoreboard().setObjectiveSlot(
                //#if MC>=12020
                ScoreboardDisplaySlot.LIST
                //#else
                //$$ 0
                //#endif
                , null);
    }
    
    public static Text getParsedTextFromKey(String string, Object... args) {
        if (args.length > 0) return Text.Serializer.fromJson(Text.translatable(string, args).getString());
        return Text.Serializer.fromJson(Text.translatable(string).getString());
    }
}
