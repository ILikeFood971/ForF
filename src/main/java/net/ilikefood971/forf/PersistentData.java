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
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.ilikefood971.forf.util.Util.MOD_ID;

public class PersistentData extends PersistentState {

    //#if MC>=12002
    private static final Type<PersistentData> type = new Type<>(
            PersistentData::new, // If there's no 'PersistentData' yet create one
            PersistentData::createFromNbt, // If there is a 'PersistentData' NBT, parse it with 'createFromNbt'
            DataFixTypes.LEVEL // Supposed to be an 'DataFixTypes' enum
    );
    //#endif

    private boolean started = false;
    private int secondsLeft = 0;
    private PvPTimer.PvPState pvPState = PvPTimer.PvPState.OFF;
    private Map<UUID, Integer> playersAndLives = new HashMap<>();
    private boolean firstKill = true;

    private static PersistentData createFromNbt(NbtCompound tag) {
        PersistentData state = new PersistentData();

        state.started = tag.getBoolean("started");
        state.secondsLeft = tag.getInt("secondsLeft");
        state.pvPState = PvPTimer.PvPState.convertToBoolean(tag.getBoolean("pvPState"));
        state.playersAndLives = listToMap(tag.getList("livesMap", NbtElement.COMPOUND_TYPE));
        state.firstKill = tag.getBoolean("firstKill");

        /*
         * Used to migrate old data
         * This will add the player and then lives data is added later from the EntityMixin
         */
        if (tag.contains("forfPlayersUUIDS")) {
            NbtList list = tag.getList("forfPlayersUUIDS", NbtElement.STRING_TYPE);
            for (NbtElement element : list) {
                state.playersAndLives.put(UUID.fromString(element.asString()), 0); // Assume 0 lives but will be edited by EntityMixin
            }
        }

        return state;
    }

    public static PersistentData getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();

        return persistentStateManager.getOrCreate(
                //#if MC>=12002
                type,
                //#else
                //$$ PersistentData::createFromNbt,
                //$$ PersistentData::new,
                //#endif
                MOD_ID
        );
    }

    /**
     * @param map the map to convert to nbt
     * @return a list of nbt compounds with the uuid and lives
     */
    private static NbtList mapToNbt(Map<UUID, Integer> map) {
        NbtList nbtList = new NbtList();
        for (Map.Entry<UUID, Integer> entry : map.entrySet()) {
            nbtList.add(playerLivesToCompound(entry.getKey(), entry.getValue()));
        }
        return nbtList;
    }

    private static NbtCompound playerLivesToCompound(UUID uuid, int lives) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putUuid("uuid", uuid);
        nbtCompound.putInt("lives", lives);
        return nbtCompound;
    }

    private static Map<UUID, Integer> listToMap(NbtList list) {
        Map<UUID, Integer> map = new HashMap<>();
        for (NbtElement element : list) {
            if (element.getType() != NbtElement.COMPOUND_TYPE)
                throw new RuntimeException("Found invalid nbt type when reading list!");
            NbtCompound compound = ((NbtCompound) element);
            map.put(compound.getUuid("uuid"), compound.getInt("lives"));
        }
        return map;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putBoolean("started", started);
        nbt.putInt("secondsLeft", PvPTimer.getSecondsLeft());
        nbt.putBoolean("pvPState", PvPTimer.getPvPState().getValue());
        nbt.put("livesMap", mapToNbt(playersAndLives));
        nbt.putBoolean("firstKill", firstKill);

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

    public Map<UUID, Integer> getPlayersAndLives() {
        return playersAndLives;
    }

    public boolean isFirstKill() {
        return firstKill;
    }

    public void setFirstKill(boolean firstKill) {
        this.firstKill = firstKill;
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
