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

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private int lives;
    private PlayerType playerType;

    public PlayerData(UUID uuid, int lives, PlayerType playerType) {
        this.uuid = uuid;
        this.lives = lives;
        this.playerType = playerType;
    }

    protected static PlayerData createFromNbt(NbtElement nbt) {
        if (nbt instanceof NbtCompound compound) {
            UUID uuid = compound.getUuid("uuid");
            int lives = compound.getInt("lives");
            PlayerType playerType = PlayerType.valueOf(compound.getString("playerType"));
            return new PlayerData(uuid, lives, playerType);
        } else
            throw new IllegalArgumentException("NbtElement must be a NbtCompound when creating a PlayerData object.");
    }

    protected NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("uuid", uuid);
        nbt.putInt("lives", lives);
        nbt.putString("playerType", playerType.name());
        return nbt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public enum PlayerType {
        PLAYER,
        SPECTATOR,
        UNKNOWN
    }
}
