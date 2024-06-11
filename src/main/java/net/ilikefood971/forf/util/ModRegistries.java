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

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.ilikefood971.forf.command.*;
import net.ilikefood971.forf.event.PlayerDeathEvent;
import net.ilikefood971.forf.event.PlayerJoinEvent;
import net.ilikefood971.forf.event.PlayerUseEntity;
import net.ilikefood971.forf.timer.PvPTimer;
import net.ilikefood971.forf.tracker.PlayerTrackerItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRegistries {
    public static void registerModStuff() {
        registerCommands();
        registerEvents();
        registerItems();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(StartCommand::register);
        CommandRegistrationCallback.EVENT.register(StopCommand::register);
        CommandRegistrationCallback.EVENT.register(JoinCommand::register);
        CommandRegistrationCallback.EVENT.register(LeaveCommand::register);

        CommandRegistrationCallback.EVENT.register(TimerCommands::register);
        CommandRegistrationCallback.EVENT.register(LivesCommands::register);

        CommandRegistrationCallback.EVENT.register(KillsCommand::register);
        CommandRegistrationCallback.EVENT.register(AssassinCommand::register);
    }

    private static void registerEvents() {
        ServerLivingEntityEvents.AFTER_DEATH.register(new PlayerDeathEvent());

        UseEntityCallback.EVENT.register(new PlayerUseEntity());

        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent();
        ServerPlayConnectionEvents.INIT.register(playerJoinEvent);
        ServerPlayConnectionEvents.JOIN.register(playerJoinEvent);
        ServerTickEvents.END_SERVER_TICK.register(new PvPTimer());
    }

    private static void registerItems() {
        Registry.register(Registries.ITEM, Identifier.of(Util.MOD_ID, "player_tracker"), PlayerTrackerItem.PLAYER_TRACKER);
    }
}
