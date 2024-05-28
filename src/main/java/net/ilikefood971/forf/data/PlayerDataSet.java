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

package net.ilikefood971.forf.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerDataSet {

    private static PlayerDataSet instance;

    private final Map<UUID, PlayerData> dataSet = new HashMap<>();

    protected static void readNbt(NbtList nbt) {
        getInstance().dataSet.putAll(nbt.stream().map(PlayerData::createFromNbt).collect(Collectors.toMap(PlayerData::getUuid, playerData -> playerData)));
    }

    public static PlayerDataSet getInstance() {
        if (instance == null) {
            instance = new PlayerDataSet();
        }
        return instance;
    }

    public void add(PlayerData playerData) {
        dataSet.put(playerData.getUuid(), playerData);
    }

    @NotNull
    public PlayerData get(UUID uuid) {
        return dataSet.getOrDefault(uuid, new PlayerData(uuid, 0, PlayerData.PlayerType.UNKNOWN));
    }

    protected NbtElement toNbt() {
        Set<NbtCompound> data = dataSet.values().stream().map(PlayerData::toNbt).collect(Collectors.toSet());
        NbtList list = new NbtList();
        list.addAll(data);
        return list;
    }

    public Map<UUID, PlayerData> getDataSet() {
        return dataSet;
    }
}
