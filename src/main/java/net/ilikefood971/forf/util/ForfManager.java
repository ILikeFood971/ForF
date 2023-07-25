package net.ilikefood971.forf.util;

import com.mojang.brigadier.context.CommandContext;
import net.ilikefood971.forf.Forf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class ForfManager {
    public static boolean addForfPlayer(PlayerEntity player) {
        return ((IEntityDataSaver) player).setForf(true);
    }
    
    public static void setupForf(CommandContext<ServerCommandSource> context, int lives) {
        List<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager().getPlayerList();
        for (PlayerEntity player : players) {
            ((IEntityDataSaver) player).setLives(lives);
        }
    }
    public static void stopForf(CommandContext<ServerCommandSource> context) {
        List<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager().getPlayerList();
        for (PlayerEntity player : players) {
            ((IEntityDataSaver) player).setLives(-1);
        }
        Forf.isStarted = false;
    }
    
}
