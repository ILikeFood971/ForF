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

import net.ilikefood971.forf.data.PlayerData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import static net.ilikefood971.forf.util.Util.CONFIG;
import static net.ilikefood971.forf.util.Util.PERSISTENT_DATA;

public class LivesHelper {
    private final ServerPlayerEntity player;

    public LivesHelper(ServerPlayerEntity player) {
        this.player = player;
    }

    public static int get(ServerPlayerEntity player) {
        PlayerData playerData = PERSISTENT_DATA.getPlayerDataSet().get(player.getUuid());
        if (playerData.getPlayerType() != PlayerData.PlayerType.PLAYER) {
            return 0;
        } else return playerData.getLives();
    }

    public static void set(ServerPlayerEntity player, int lives) {
        PlayerData playerData = PERSISTENT_DATA.getPlayerDataSet().get(player.getUuid());
        if (!CONFIG.overfill()) {
            // Prevent the player from going over if overfill is disabled
            lives = Math.min(lives, CONFIG.startingLives());
        }
        // Prevent the player from having negative lives
        lives = Math.max(lives, 0);

        Util.setScore(player, lives);

        // Check to see if the player ran out of lives
        if (lives == 0 && Util.PERSISTENT_DATA.isStarted()) {
            // Kick the player if spectators are not allowed
            if (!CONFIG.spectators()) {
                player.networkHandler.disconnect(Text.translatable("forf.disconnect.outOfLives"));
            } else {
                // If spectators allowed, switch the players gamemode
                playerData.setPlayerType(PlayerData.PlayerType.SPECTATOR);
                player.changeGameMode(CONFIG.spectatorGamemode());
                player.sendMessage(Text.translatable("forf.event.death.spectator"));
            }
        } else if (playerData.getPlayerType() == PlayerData.PlayerType.SPECTATOR) { // If they were a spectator then update that
            playerData.setPlayerType(PlayerData.PlayerType.PLAYER);
            player.changeGameMode(GameMode.DEFAULT);
        }
        playerData.setLives(lives);
    }

    public static void increment(ServerPlayerEntity player, int amount) {
        set(player, get(player) + amount);
    }

    public static void decrement(ServerPlayerEntity player, int amount) {
        set(player, get(player) - amount);
    }

    public int get() {
        return get(this.player);
    }

    public void set(int lives) {
        set(this.player, lives);
    }

    public void increment(int amount) {
        increment(this.player, amount);
    }

    public void decrement(int amount) {
        decrement(this.player, amount);
    }
}
