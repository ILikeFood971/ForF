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
import net.ilikefood971.forf.Forf;
import net.ilikefood971.forf.util.mixinInterfaces.IEntityDataSaver;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.GameMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ForfManager {
    
    public static void setupForf(CommandContext<ServerCommandSource> context) {
        Set<UUID> uuids = Forf.PERSISTENT_DATA.forfPlayersUUIDs.stream()
                .map(UUID::fromString).collect(Collectors.toSet());
        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        
        for (UUID uuid : uuids) {
            ServerPlayerEntity player = playerManager.getPlayer(uuid);
            if (player != null) {
                ((IEntityDataSaver) player).setLives(Forf.CONFIG.startingLives());
                player.changeGameMode(GameMode.DEFAULT);
            } else {
                try {
                    Path pathToPlayerSaveData = Forf.SERVER.getSavePath(WorldSavePath.PLAYERDATA);
                    File playerSaveFile = pathToPlayerSaveData.resolve(uuid.toString() + ".dat").toFile();
                    
                    NbtCompound unfixedNbt = NbtIo.readCompressed(playerSaveFile);
                    int dataVersion = unfixedNbt.contains("DataVersion", 3) ? unfixedNbt.getInt("DataVersion") : -1;
                    NbtCompound nbt = DataFixTypes.PLAYER.update(Schemas.getFixer(), unfixedNbt, dataVersion);
                    
                    if (nbt.getCompound("forf.data") == null) nbt.put("forf.data", new NbtCompound());
                    nbt.getCompound("forf.data").putInt("lives", Forf.CONFIG.startingLives());
                    
                    File file = File.createTempFile(uuid + "-", ".dat", pathToPlayerSaveData.toFile());
                    NbtIo.writeCompressed(nbt, file);
                    File newDataFile = new File(pathToPlayerSaveData.toFile(), uuid + ".dat");
                    File oldDataFile = new File(pathToPlayerSaveData.toFile(), uuid + ".dat_old");
                    Util.backupAndReplace(newDataFile, file, oldDataFile);
                    
                } catch (IOException e) {
                    Forf.LOGGER.info(e.toString());
                }
            }
            
        }
    }
    public static void stopForf(CommandContext<ServerCommandSource> context) {
        // Remove the Header from the tablist
        for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
            player.networkHandler.sendPacket(new PlayerListHeaderS2CPacket(Text.translatable(""), Text.translatable("")));
        }
        
        // Set all lives to 0
        Set<String> playersUUIDsAsString = Forf.PERSISTENT_DATA.forfPlayersUUIDs;
        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        List<String> tempRemove = new ArrayList<>();
        
        // For every online player changing lives is easy
        for (String uuid : playersUUIDsAsString) {
            PlayerEntity player = playerManager.getPlayer(UUID.fromString(uuid));
            if (player != null) {
                tempRemove.add(uuid);
                ((IEntityDataSaver) player).setLives(0);
            }
        }
        tempRemove.forEach(playersUUIDsAsString::remove);
        
        
        Forf.PERSISTENT_DATA.forfPlayersUUIDs.clear();
        Forf.PERSISTENT_DATA.started = false;
    }
    
}
