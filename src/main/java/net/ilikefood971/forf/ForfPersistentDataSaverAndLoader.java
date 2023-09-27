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

package net.ilikefood971.forf;

import net.ilikefood971.forf.timer.PvPTimer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ilikefood971.forf.Forf.MOD_ID;

public class ForfPersistentDataSaverAndLoader extends PersistentState {
    public Set<String> forfPlayersUUIDs = new HashSet<>();
    public boolean started = false;
    public int secondsLeft = 0;
    public net.ilikefood971.forf.timer.PvPTimer.PvPState pvPState = PvPTimer.PvPState.OFF;
    
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList stringArray = new NbtList();
        forfPlayersUUIDs.forEach(string -> stringArray.add(NbtString.of(string)));
        
        nbt.put("forfPlayersUUIDS", stringArray);
        nbt.putBoolean("started", started);
        nbt.putInt("secondsLeft", PvPTimer.getSecondsLeft());
        nbt.putBoolean("pvPState", PvPTimer.getPvPState().getValue());
        return nbt;
    }
    private static ForfPersistentDataSaverAndLoader createFromNbt(NbtCompound tag) {
        ForfPersistentDataSaverAndLoader state = new ForfPersistentDataSaverAndLoader();
        state.forfPlayersUUIDs = tag.getList("forfPlayersUUIDS", NbtList.STRING_TYPE)
                .stream()
                .map(NbtElement::asString)
                .collect(Collectors.toSet());
        state.started = tag.getBoolean("started");
        state.secondsLeft = tag.getInt("secondsLeft");
        state.pvPState = PvPTimer.PvPState.convertToBoolean(tag.getBoolean("pvPState"));
        return state;
    }
    
    public static ForfPersistentDataSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        
        ForfPersistentDataSaverAndLoader state = persistentStateManager.getOrCreate(
                ForfPersistentDataSaverAndLoader::createFromNbt,
                ForfPersistentDataSaverAndLoader::new,
                MOD_ID
        );
        // If state is not marked dirty, when Minecraft closes, 'writeNbt' won't be called and therefore nothing will be saved.
        state.markDirty();
        
        return state;
    }
}
