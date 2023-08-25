package net.ilikefood971.forf.util;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.ilikefood971.forf.command.JoinCommand;
import net.ilikefood971.forf.command.StartCommand;
import net.ilikefood971.forf.command.StopCommand;
import net.ilikefood971.forf.event.*;
import net.ilikefood971.forf.mixin.CompassItemMixin;

public class ModRegistries {
    public static void registerModStuff() {
        registerCommands();
        registerEvents();
    }
    
    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(StartCommand::register);
        CommandRegistrationCallback.EVENT.register(StopCommand::register);
        CommandRegistrationCallback.EVENT.register(JoinCommand::register);
    }
    private static void registerEvents() {
        ServerPlayerEvents.COPY_FROM.register(new PlayerDeathEventCopyFrom());
        UseEntityCallback.EVENT.register(new PlayerUseEntity());
        ServerLivingEntityEvents.AFTER_DEATH.register(new PlayerDeathEvent());
        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent();
        ServerPlayConnectionEvents.INIT.register(playerJoinEvent);
        ServerPlayConnectionEvents.JOIN.register(playerJoinEvent);
        ServerTickEvents.END_WORLD_TICK.register(new ServerTickEvent());
    }
}
