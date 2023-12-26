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
import net.ilikefood971.forf.event.PlayerJoinEvent;
import net.ilikefood971.forf.util.mixinInterfaces.IEntityDataSaver;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.ilikefood971.forf.util.Util.*;

public class ForfManager {
    
    public static void setupForf(CommandContext<ServerCommandSource> context) {
        Set<UUID> uuids = PERSISTENT_DATA.forfPlayersUUIDs.stream()
                .map(UUID::fromString).collect(Collectors.toSet());
        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        
        for (UUID uuid : uuids) {
            ServerPlayerEntity player = playerManager.getPlayer(uuid);
            if (player != null) {
                ((IEntityDataSaver) player).setLives(CONFIG.startingLives());
            } else {
                try {
                    Path pathToPlayerSaveData = SERVER.getSavePath(WorldSavePath.PLAYERDATA);
                    Path playerSavePath = pathToPlayerSaveData.resolve(uuid.toString() + ".dat");
                    
                    NbtCompound unfixedNbt = NbtIo.readCompressed(
                            //#if MC >= 12003
                            playerSavePath, NbtSizeTracker.ofUnlimitedBytes()
                            //#else
                            //$$playerSavePath.toFile()
                            //#endif
                    );
                    int dataVersion = unfixedNbt.contains("DataVersion", 3) ? unfixedNbt.getInt("DataVersion") : -1;
                    NbtCompound nbt = DataFixTypes.PLAYER.update(Schemas.getFixer(), unfixedNbt, dataVersion);
                    
                    if (nbt.getCompound("forf.data") == null) nbt.put("forf.data", new NbtCompound());
                    nbt.getCompound("forf.data").putInt("lives", CONFIG.startingLives());
                    
                    NbtIo.writeCompressed(nbt, playerSavePath
                            //#if MC < 12003
                            //$$.toFile()
                            //#endif
                    );
                    File newDataFile = new File(pathToPlayerSaveData.toFile(), uuid + ".dat");
                    File oldDataFile = new File(pathToPlayerSaveData.toFile(), uuid + ".dat_old");
                    Util.backupAndReplace(newDataFile.toPath(), playerSavePath, oldDataFile.toPath());
                    
                } catch (IOException e) {
                    LOGGER.info(e.toString());
                }
            }
            
        }
    }
    public static void stopForf(CommandContext<ServerCommandSource> context) {
        PERSISTENT_DATA.started = false;

        // Remove the Header from the tablist
        SERVER.getPlayerManager().sendToAll(PlayerJoinEvent.getEmptyHeaderPacket());

        // Set all lives to 0
        Set<String> playersUUIDsAsString = PERSISTENT_DATA.forfPlayersUUIDs;
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


        PERSISTENT_DATA.forfPlayersUUIDs.clear();
    }
    
}
