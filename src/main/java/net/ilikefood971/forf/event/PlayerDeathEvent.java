package net.ilikefood971.forf.event;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.ilikefood971.forf.Forf;
import net.ilikefood971.forf.util.mixinInterfaces.IEntityDataSaver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@SuppressWarnings("DataFlowIssue")
public class PlayerDeathEvent implements ServerLivingEntityEvents.AfterDeath {
    
    // Remove one life from the player on death
    @Override
    public void afterDeath(LivingEntity entity, DamageSource damageSource) {
        // Check that they are actually a player as this gets called for all entity deaths
        if (entity instanceof ServerPlayerEntity) {
            // Cast the entity to player for later use
            PlayerEntity player = (PlayerEntity) entity;
            IEntityDataSaver data = (IEntityDataSaver) player;
            
            if (data.getLives() > 0) {
                data.removeLife();
                player.getServer().getPlayerManager().broadcast(Text.of("Life removed!"), false);
                
            // Check to see if the player ran out of lives
            } else if (data.getLives() == 0) {
                // Ban the player if spectators are not allowed
                if (!Forf.getCONFIG().spectators()) banPlayer(player);
                
                // If spectators allowed, switch the players gamemode
                ((ServerPlayerEntity) player).changeGameMode(Forf.getCONFIG().spectatorGamemode());
                player.sendMessage(Text.of("You have run out of lives! You are now a spectator"));
            }
            
           
        }
    }

    
    public void banPlayer(PlayerEntity player) {
        // Only run if it is on the server side
        if (player.getWorld().isClient()) return;
        
        // Get the game profile needed to ban player
        GameProfile profile = player.getGameProfile();
        BannedPlayerList banlist = player.getServer().getPlayerManager().getUserBanList();
        
        banlist.add(new BannedPlayerEntry(profile, null, null, null, "You have run out of lives and spectators are not allowed!"));
    }
}
