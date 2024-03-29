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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
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
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
//#endif

import java.util.UUID;
import java.util.function.Consumer;

public class Util {

    public static final String MOD_ID = "forf";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Config CONFIG = Config.loadFromFile();
    public static final FakeScoreboard FAKE_SCOREBOARD = new FakeScoreboard();
    public static PersistentData PERSISTENT_DATA;
    public static MinecraftServer SERVER;

    public static void addNewPlayer(UUID uuid) {
        PERSISTENT_DATA.getPlayersAndLives().put(uuid, 0);
    }

    public static void removePlayer(UUID uuid) {
        PERSISTENT_DATA.getPlayersAndLives().remove(uuid);
    }

    public static boolean isForfPlayer(ServerPlayerEntity player) {
        return isForfPlayer(player.getUuid());
    }

    public static boolean isForfPlayer(UUID uuid) {
        return PERSISTENT_DATA.getPlayersAndLives().containsKey(uuid);
    }

    // Version Utils

    public static void sendFeedback(CommandContext<ServerCommandSource> context, Text message, boolean broadcast) {
        context.getSource().sendFeedback(
                //#if MC >= 12000
                () ->
                        //#endif
                        message, broadcast);
    }

    public static void setScore(ServerPlayerEntity player, int lives) {
        //#if MC >= 12003
        ScoreAccess scoreAccess = FAKE_SCOREBOARD.getOrCreateScore(player, FAKE_SCOREBOARD.livesObjective);
        scoreAccess.setScore(lives);
        //#else
        //$$ ScoreboardPlayerScore playerScore = FAKE_SCOREBOARD.getPlayerScore(player.getEntityName(), FAKE_SCOREBOARD.livesObjective);
        //$$ playerScore.setScore(lives);
        //#endif
    }

    public static void forEachValueInLivesObjective(Consumer<
            //#if MC >= 12003
            ScoreboardEntry
            //#else
            //$$ ScoreboardPlayerScore
            //#endif
            > action) {
        for (
            //#if MC >= 12003
                ScoreboardEntry
                        //#else
                        //$$ ScoreboardPlayerScore
                        //#endif
                        entry : FAKE_SCOREBOARD.getScoreboardEntries(FAKE_SCOREBOARD.livesObjective)) {
            action.accept(entry);
        }
    }

    public static Packet<?> getScoreboardUpdatePacket(
            //#if MC >= 12003
            ScoreboardEntry
                    //#else
                    //$$ ScoreboardPlayerScore
                    //#endif
                    score) {
        //#if MC >= 12003
        return new ScoreboardScoreUpdateS2CPacket(
                score.owner(),
                Util.FAKE_SCOREBOARD.livesObjective.getName(),
                score.value(),
                Util.FAKE_SCOREBOARD.livesObjective.getDisplayName(),
                null
        );
        //#else
        //$$ return new ScoreboardPlayerUpdateS2CPacket(
        //$$         ServerScoreboard.UpdateMode.CHANGE,
        //$$         Util.FAKE_SCOREBOARD.livesObjective.getName(),
        //$$         score.getPlayerName(),
        //$$         score.getScore()
        //$$ );
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

    public static GameProfile getOfflineProfile(UUID uuid) {
        //#if MC >= 12002
        ProfileResult profileResult = SERVER.getSessionService().fetchProfile(uuid, false);
        if (profileResult == null) {
            throw new ProfileNotFoundException("Player Not Found: " + uuid);
        } else {
            return profileResult.profile();
        }
        //#else
        //$$ return SERVER.getSessionService().fillProfileProperties(new GameProfile(uuid, null), false);
        //#endif
    }

}
