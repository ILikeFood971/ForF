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
import net.ilikefood971.forf.util.mixinInterfaces.IEntityDataSaver;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;

import static net.ilikefood971.forf.util.Util.*;

public class PlayerJoinEvent implements ServerPlayConnectionEvents.Init, ServerPlayConnectionEvents.Join {
    
    @SuppressWarnings("ConstantValue")
    @Override
    public void onPlayInit(ServerPlayNetworkHandler handler, MinecraftServer server) {
        // Returning at any point without a disconnect is a pass
        
        // Because the PlayerLoginMixin has already run, we can be sure that either forf hasn't started, they are an allowed player, or spectators are allowed
        // What we don't know is whether they are out of lives
        String uUID = handler.getPlayer().getUuidAsString();
        // If forf hasn't stated, let them in
        if (!PERSISTENT_DATA.started) {
            ((IEntityDataSaver) handler.getPlayer()).setLives(0);
            return;
        }
        // If they are a player above 0 lives let them in
        if (PERSISTENT_DATA.started && ((IEntityDataSaver) handler.getPlayer()).getLives() > 0) {
            return;
        }
        // If spectators are allowed, and we know it's started from the above if statement not returning
        // Then check to see if they are a player, and they're 0 or fewer lives
        if (CONFIG.spectators() && (!PERSISTENT_DATA.forfPlayersUUIDs.contains(uUID) || ((IEntityDataSaver) handler.getPlayer()).getLives() <= 0)) {
            // If so then make them spectator gamemode
            handler.getPlayer().changeGameMode(CONFIG.spectatorGamemode());
            return;
        }
        // At this point we know that it's started, and they can't spectate
        // We also know they aren't a forf player with enough lives
        // We can conclude they must be a forf player out of lives or a non forf player
        if (PERSISTENT_DATA.forfPlayersUUIDs.contains(uUID)) {
            handler.disconnect(Text.translatable("forf.disconnect.outOfLives"));
            return;
        }
        handler.disconnect(Text.translatable("forf.disconnect.noSpectators"));
    }
    
    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        // Check to see if Friend or Foe has started
        if (PERSISTENT_DATA.started) {
           /* LOGGER.info("updating score");
            ScoreboardPlayerScore score = new ScoreboardPlayerScore(fakeScoreboard, fakeScoreboard.livesObjective, handler.getPlayer().getEntityName());
            LOGGER.info("old score: {}", score.getScore());
            score.setScore(((IEntityDataSaver) handler.getPlayer()).getLives());
            LOGGER.info("new score: {}", score.getScore());
            fakeScoreboard.updateScore(score);*/
            
            sender.sendPacket(getHeaderPacket());
        }
    }
    
    public static Packet<?> getHeaderPacket() {
        return new PlayerListHeaderS2CPacket(Text.Serializer.fromJson(CONFIG.tablistHeader()), Text.literal(""));
    }
    public static Packet<?> getEmptyHeaderPacket() {
        return new PlayerListHeaderS2CPacket(Text.literal(""), Text.literal(""));
    }
}
