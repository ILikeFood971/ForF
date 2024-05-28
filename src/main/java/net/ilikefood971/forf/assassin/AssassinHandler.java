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

package net.ilikefood971.forf.assassin;

import net.ilikefood971.forf.data.PlayerData;
import net.ilikefood971.forf.data.PlayerDataSet;
import net.ilikefood971.forf.util.Util;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AssassinHandler {
    private static AssassinHandler instance;

    @Nullable
    private UUID assassin;

    public static void readNbt(NbtCompound nbt) {
        if (nbt.contains("assassin")) {
            getInstance().setAssassin(nbt.getUuid("assassin"));
        }
    }

    @NotNull
    public static AssassinHandler getInstance() {
        if (instance == null) {
            instance = new AssassinHandler();
        }
        return instance;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        if (assassin != null) {
            nbt.putUuid("assassin", assassin);
        }
        return nbt;
    }

    @Nullable
    public UUID getAssassin() {
        return assassin;
    }

    public void setAssassin(@Nullable UUID uuid) {
        if (assassin != null) {
            PlayerDataSet.getInstance().get(assassin).setPlayerType(PlayerData.PlayerType.PLAYER);
        }
        PlayerDataSet.getInstance().get(uuid).setPlayerType(PlayerData.PlayerType.ASSASSIN);
        assassin = uuid;
    }

    public boolean isAssassinOnline() {
        return assassin != null && Util.SERVER.getPlayerManager().getPlayer(assassin) != null;
    }
}
