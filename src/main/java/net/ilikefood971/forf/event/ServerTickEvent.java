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

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.ilikefood971.forf.util.mixinInterfaces.IPlayerTracker;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class ServerTickEvent implements ServerTickEvents.EndWorldTick {
    public static List<IPlayerTracker> openPlayerTrackers = new ArrayList<>();
    public static List<IPlayerTracker> toRemove = new ArrayList<>();
    private int ticksTillNext;
    @Override
    public void onEndTick(ServerWorld world) {
        //FIXME COMPASS
/*       for (IPlayerTracker playerTracker : openPlayerTrackers) {
            playerTracker.onEndTick(world);
        }
        openPlayerTrackers.removeAll(toRemove);
        toRemove.clear();
        */
    }
}
