package net.ilikefood971.forf.util;

import com.mojang.brigadier.context.CommandContext;
import io.wispforest.owo.offline.OfflineDataLookup;
import net.ilikefood971.forf.Forf;
import net.ilikefood971.forf.util.mixinInterfaces.IEntityDataSaver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.*;
import java.util.stream.Collectors;

public class ForfManager {
    
    public static void setupForf(CommandContext<ServerCommandSource> context) {
        Set<UUID> uuids = Forf.getCONFIG().forfPlayersUUIDs().stream()
                .map(UUID::fromString).collect(Collectors.toSet());
        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        Set<ServerPlayerEntity> players = new HashSet<>();
        
        for (UUID uuid : uuids) {
            ServerPlayerEntity player = playerManager.getPlayer(uuid);
            if (player != null) {
                ((IEntityDataSaver) player).setLives(Forf.getCONFIG().startingLives());
                player.changeGameMode(GameMode.DEFAULT);
            } else {
                
                NbtCompound forfNbtData = OfflineDataLookup.get(uuid);
                forfNbtData.getCompound("forf.data").putInt("lives", Forf.getCONFIG().startingLives());
                
                
                OfflineDataLookup.put(uuid, forfNbtData);
                
            }
            
        }
    }
    public static void stopForf(CommandContext<ServerCommandSource> context) {
        // Remove the Header from the tablist
        for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
            player.networkHandler.sendPacket(new PlayerListHeaderS2CPacket(Text.literal(""), Text.literal("")));
        }
        
        // Set all lives to -1
        Set<String> playersUUIDsAsString = Forf.getCONFIG().forfPlayersUUIDs();
        PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
        List<String> tempRemove = new ArrayList<>();
        
        // For every online player changing lives is easy
        for (String uuid : playersUUIDsAsString) {
            PlayerEntity player = playerManager.getPlayer(UUID.fromString(uuid));
            if (player != null) {
                tempRemove.add(uuid);
                ((IEntityDataSaver) player).setLives(-1);
            }
        }
        tempRemove.forEach(playersUUIDsAsString::remove);
        
        
        // For offline players we have to edit the files directly
        for (String uuid : playersUUIDsAsString) {
            NbtCompound forfNbtData = OfflineDataLookup.get(UUID.fromString(uuid));
            forfNbtData.getCompound("forf.data").putInt("lives", -1);
            OfflineDataLookup.put(UUID.fromString(uuid), forfNbtData);
            
        }
        
        Forf.getCONFIG().forfPlayersUUIDs().clear();
        Forf.getCONFIG().started(false);
    }
    
}
