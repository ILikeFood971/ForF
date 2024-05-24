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

package net.ilikefood971.forf.data;

import net.ilikefood971.forf.timer.PvPTimer;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.UUID;

import static net.ilikefood971.forf.util.Util.MOD_ID;

public class DataHandler extends PersistentState {

    private static final Type<DataHandler> type = new Type<>(DataHandler::new, // If there's no 'PersistentData' yet create one
            DataHandler::createFromNbt, // If there is a 'PersistentData' NBT, parse it with 'createFromNbt'
            DataFixTypes.LEVEL // Supposed to be an 'DataFixTypes' enum
    );


    private boolean started = false;
    private int secondsLeft = 0;
    private PvPTimer.PvPState pvPState = PvPTimer.PvPState.OFF;
    private PlayerDataSet playerDataSet = new PlayerDataSet();
    private boolean firstKill = true;
    private boolean tenKillsLifeQuest = true;

    private static DataHandler createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        DataHandler state = new DataHandler();

        state.started = tag.getBoolean("started");
        state.secondsLeft = tag.getInt("secondsLeft");
        state.pvPState = PvPTimer.PvPState.convertToBoolean(tag.getBoolean("pvPState"));
        state.playerDataSet = PlayerDataSet.createFromNbt(tag.getList("playerDataSet", NbtElement.COMPOUND_TYPE));
        state.firstKill = tag.getBoolean("firstKill");
        state.tenKillsLifeQuest = tag.getBoolean("tenKillsLifeQuest");

        /*
         * Used to migrate old data
         * This will add the player and then lives data is added later from the EntityMixin
         */
        if (tag.contains("livesMap")) {
            NbtList list = tag.getList("livesMap", NbtElement.COMPOUND_TYPE);
            for (NbtElement element : list) {
                state.playerDataSet.get(UUID.fromString(element.asString())); // Assume 0 lives but will be edited by EntityMixin
            }
        }

        return state;
    }

    public static DataHandler getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();

        return persistentStateManager.getOrCreate(type, MOD_ID);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.putBoolean("started", started);
        nbt.putInt("secondsLeft", PvPTimer.getSecondsLeft());
        nbt.putBoolean("pvPState", PvPTimer.getPvPState().getValue());
        nbt.put("playerDataSet", playerDataSet.toNbt());
        nbt.putBoolean("firstKill", firstKill);
        nbt.putBoolean("tenKillsLifeQuest", tenKillsLifeQuest);

        return nbt;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

    public PvPTimer.PvPState getPvPState() {
        return pvPState;
    }

    public void setPvPState(PvPTimer.PvPState pvPState) {
        this.pvPState = pvPState;
    }

    public PlayerDataSet getPlayerDataSet() {
        return playerDataSet;
    }

    public boolean isFirstKill() {
        return firstKill;
    }

    public void setFirstKill(boolean firstKill) {
        this.firstKill = firstKill;
    }

    public boolean isTenKillsLifeQuest() {
        return tenKillsLifeQuest;
    }

    public void setTenKillsLifeQuest(boolean tenKillsLifeQuest) {
        this.tenKillsLifeQuest = tenKillsLifeQuest;
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
