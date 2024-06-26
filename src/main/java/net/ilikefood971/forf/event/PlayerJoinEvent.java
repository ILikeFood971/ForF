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

package net.ilikefood971.forf.event;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.ilikefood971.forf.assassin.AssassinHandler;
import net.ilikefood971.forf.data.DataHandler;
import net.ilikefood971.forf.data.PlayerDataSet;
import net.ilikefood971.forf.timer.PvPTimer;
import net.ilikefood971.forf.util.LivesHelper;
import net.ilikefood971.forf.util.Util;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import static net.ilikefood971.forf.util.Util.CONFIG;
import static net.ilikefood971.forf.util.Util.SERVER;

public class PlayerJoinEvent implements ServerPlayConnectionEvents.Init, ServerPlayConnectionEvents.Join {

    public static Packet<?> getHeaderPacket() {
        return new PlayerListHeaderS2CPacket(Text.Serialization.fromJson(CONFIG.tablistHeader(), SERVER.getRegistryManager()), Text.literal(""));
    }

    public static Packet<?> getEmptyHeaderPacket() {
        return new PlayerListHeaderS2CPacket(Text.literal(""), Text.literal(""));
    }

    @Override
    public void onPlayInit(ServerPlayNetworkHandler handler, MinecraftServer server) {
        // Returning at any point without a disconnect is a pass

        // Because the PlayerLoginMixin has already run, we can be sure that either forf hasn't started, they are an allowed player, or spectators are allowed
        // What we don't know is whether they are out of lives
        ServerPlayerEntity player = handler.getPlayer();
        LivesHelper lives = new LivesHelper(player);

        // If forf hasn't stated, let them in
        if (!DataHandler.getInstance().isStarted()) {
            return;
        }
        // If we get here, then we know forf is started
        // If they are a player above 0 lives let them in (the get() method checks if they are a forf player)
        if (lives.get() > 0) {
            return;
        }

        // If spectators are allowed, and we know it's started
        // Then check to see if they are a player, or they're 0 or fewer lives
        if (CONFIG.spectators() && (!Util.isForfPlayer(player) || lives.get() <= 0)) {
            // If so then set them to 0 lives
            lives.set(0);
            return;
        }
        // At this point we know that it's started, and they can't spectate
        // We also know they aren't a forf player with enough lives
        // We can conclude they must either be a forf player out of lives or a non forf player
        if (Util.isForfPlayer(player)) {
            handler.disconnect(Text.translatable("forf.disconnect.outOfLives"));
        } else {
            handler.disconnect(Text.translatable("forf.disconnect.noSpectators"));
        }
    }

    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();
        // Check to see if Friend or Foe has started
        if (DataHandler.getInstance().isStarted()) {
            sender.sendPacket(getHeaderPacket());
        }
        Util.setScore(player, LivesHelper.get(player));

        if (PlayerDataSet.getInstance().get(player.getUuid()).getPlayerType().isForfPlayer()) {
            player.changeGameMode(GameMode.DEFAULT); // In case they were a spectator before, but their lives got changed while they were offline
        } else if (DataHandler.getInstance().isStarted()) {
            player.changeGameMode(CONFIG.spectatorGamemode());
        }

        if (player.getUuid().equals(AssassinHandler.getInstance().getAssassin())) {
            player.sendMessage(Text.translatable("forf.assassin.youAreAssassin"), false);
            PvPTimer.changePvpTimer(PvPTimer.PvPState.ASSASSIN, 0);
        }
    }
}
