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
import net.ilikefood971.forf.util.Util;
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

import static net.ilikefood971.forf.util.Util.MOD_ID;

public class PersistentData extends PersistentState {
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
        nbt.put("PlayerScores", Util.fakeScoreboard.toNbt());
        return nbt;
    }
    private static PersistentData createFromNbt(NbtCompound tag) {
        PersistentData state = new PersistentData();
        state.forfPlayersUUIDs = tag.getList("forfPlayersUUIDS", NbtList.STRING_TYPE)
                .stream()
                .map(NbtElement::asString)
                .collect(Collectors.toSet());
        state.started = tag.getBoolean("started");
        state.secondsLeft = tag.getInt("secondsLeft");
        state.pvPState = PvPTimer.PvPState.convertToBoolean(tag.getBoolean("pvPState"));
        Util.fakeScoreboard.readNbt(tag.getList("PlayerScores", NbtElement.COMPOUND_TYPE));
        return state;
    }
    //#if MC>=12020
    private static final Type<PersistentData> type = new Type<>(
            PersistentData::new, // If there's no 'StateSaverAndLoader' yet create one
            PersistentData::createFromNbt, // If there is a 'StateSaverAndLoader' NBT, parse it with 'createFromNbt'
            null // Supposed to be an 'DataFixTypes' enum
    );
    //#endif
    public static PersistentData getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        
        PersistentData state = persistentStateManager.getOrCreate(
                //#if MC>=12020
                type,
                //#else
                //$$PersistentData::createFromNbt,
                //$$PersistentData::new,
                //#endif
                MOD_ID
        );
        // If state is not marked dirty, when Minecraft closes, 'writeNbt' won't be called and therefore nothing will be saved.
        state.markDirty();
        
        return state;
    }
}
