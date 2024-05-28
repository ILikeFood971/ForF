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
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.ilikefood971.forf.config.Config;
import net.ilikefood971.forf.data.PlayerDataSet;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.UserCache;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;
import java.util.function.Supplier;

public class Util {

    public static final String MOD_ID = "forf";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Config CONFIG = Config.loadFromFile();
    public static final FakeScoreboard FAKE_SCOREBOARD = new FakeScoreboard();
    public static MinecraftServer SERVER;

    public static boolean isForfPlayer(ServerPlayerEntity player) {
        return isForfPlayer(player.getUuid());
    }

    public static boolean isForfPlayer(UUID uuid) {
        return PlayerDataSet.getInstance().get(uuid).getPlayerType().isForfPlayer();
    }


    public static int getKills(UUID uuid) {
        GameProfile profile = getProfile(uuid);
        ServerPlayerEntity player = Util.SERVER.getPlayerManager().getPlayer(profile.getId());
        ServerStatHandler serverStatHandler;
        if (player != null) {
            serverStatHandler = player.getStatHandler();
        } else {
            File folder = Util.SERVER.getSavePath(WorldSavePath.STATS).toFile();
            File statsFile = new File(folder, profile.getId() + ".json");
            serverStatHandler = new ServerStatHandler(Util.SERVER, statsFile);
        }

        return serverStatHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAYER_KILLS));
    }

    public static void setScore(ServerPlayerEntity player, int lives) {
        ScoreAccess scoreAccess = FAKE_SCOREBOARD.getOrCreateScore(player, FAKE_SCOREBOARD.livesObjective);
        scoreAccess.setScore(lives);
    }

    public static GameProfile getProfile(UUID uuid) {
        ServerPlayerEntity player = SERVER.getPlayerManager().getPlayer(uuid);
        if (player != null) {
            return player.getGameProfile();
        }
        GameProfile profile;
        UserCache cache = SERVER.getUserCache();
        Supplier<GameProfile> profileFetcher = () -> {
            ProfileResult profileResult = SERVER.getSessionService().fetchProfile(uuid, false);
            return profileResult != null ? profileResult.profile() : null;
        };
        profile = cache != null ? cache.getByUuid(uuid).orElseGet(profileFetcher) : profileFetcher.get();
        if (profile == null) throw new ProfileNotFoundException("Player Not Found: " + uuid);
        return profile;
    }
}
