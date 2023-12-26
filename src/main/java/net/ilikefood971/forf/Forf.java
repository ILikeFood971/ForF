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

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.ilikefood971.forf.timer.PvPTimer;
import net.ilikefood971.forf.util.ModRegistries;

import static net.ilikefood971.forf.util.Util.*;

public class Forf implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        
        ServerLifecycleEvents.SERVER_STARTED.register((instance) -> {
            SERVER = instance;
            PERSISTENT_DATA = PersistentData.getServerState(SERVER);
            PvPTimer.serverStarted();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(instance -> {
            PERSISTENT_DATA.secondsLeft = PvPTimer.getSecondsLeft();
            PERSISTENT_DATA.pvPState = PvPTimer.getPvPState();
        });
        
        ModRegistries.registerModStuff();
        LOGGER.info(MOD_ID + " initialized");
    }
}