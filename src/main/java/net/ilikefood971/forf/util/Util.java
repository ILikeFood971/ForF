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
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//#if MC >= 12003
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreboardEntry;
//#else
//$$import net.minecraft.scoreboard.ScoreboardPlayerScore;
//$$import net.minecraft.scoreboard.ServerScoreboard;
//#endif

//#if MC >= 12002
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
//#endif

import java.util.function.Consumer;

public class Util {
    
    
    public static final String MOD_ID = "forf";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Config CONFIG = Config.loadFromFile();
    public static PersistentData PERSISTENT_DATA;
    public static MinecraftServer SERVER;
    public static final FakeScoreboard fakeScoreboard = new FakeScoreboard();

    // Version Utils
    
    public static void sendFeedback(CommandContext<ServerCommandSource> context, Text message, boolean broadcast) {
        context.getSource().sendFeedback(
                //#if MC >= 12000
                () ->
                //#endif
                message, broadcast);
    }
    
    public static Text getParsedTextFromKey(String string, Object... args) {
        if (args.length > 0) return Text.Serialization.fromJson(Text.translatable(string, args).getString());
        return Text.Serialization.fromJson(Text.translatable(string).getString());
    }

    public static void setScore(ServerPlayerEntity player, int lives) {
        //#if MC >= 12003
        ScoreAccess scoreAccess = fakeScoreboard.getOrCreateScore(player, fakeScoreboard.livesObjective);
        scoreAccess.setScore(lives);
        //#else
        //$$ScoreboardPlayerScore playerScore = fakeScoreboard.getPlayerScore(player.getEntityName(), fakeScoreboard.livesObjective);
        //$$playerScore.setScore(lives);
        //#endif
    }
    public static void forEachValueInLivesObjective(Consumer<
            //#if MC >= 12003
            ScoreboardEntry
            //#else
            //$$ScoreboardPlayerScore
            //#endif
            > action) {
        for (
                //#if MC >= 12003
                ScoreboardEntry
                //#else
                //$$ScoreboardPlayerScore
                //#endif
                        entry : fakeScoreboard.getScoreboardEntries(fakeScoreboard.livesObjective)) {
            action.accept(entry);
        }
    }

    public static Packet<?> getScoreboardUpdatePacket(
            //#if MC >= 12003
            ScoreboardEntry
            //#else
            //$$ScoreboardPlayerScore
            //#endif
                    score) {
        //#if MC >= 12003
        return new ScoreboardScoreUpdateS2CPacket(
                score.owner(),
                Util.fakeScoreboard.livesObjective.getName(),
                score.value(),
                Util.fakeScoreboard.livesObjective.getDisplayName(),
                null
        );
        //#else
        //$$return new ScoreboardPlayerUpdateS2CPacket(
        //$$        ServerScoreboard.UpdateMode.CHANGE,
        //$$        Util.fakeScoreboard.livesObjective.getName(),
        //$$        score.getPlayerName(),
        //$$        score.getScore()
        //$$);
        //#endif
    }

    @SuppressWarnings("SameReturnValue")
    public static ScoreboardDisplaySlot getScoreboardListSlot() {
        //#if MC >= 12002
        return ScoreboardDisplaySlot.LIST;
        //#else
        //$$ return 0;
        //#endif
    }

    public static boolean isListSlot(ScoreboardDisplaySlot slot) {
        //#if MC >= 12002
        return slot.equals(ScoreboardDisplaySlot.LIST);
        //#else
        //$$ return slot == 0;
        //#endif
    }
}
