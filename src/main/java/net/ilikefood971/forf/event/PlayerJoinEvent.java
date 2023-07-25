package net.ilikefood971.forf.event;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.ilikefood971.forf.Forf;
import net.ilikefood971.forf.util.IEntityDataSaver;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;

public class PlayerJoinEvent implements ServerPlayConnectionEvents.Init {
    public static void setAllowSpectators(boolean spectators) {
        allowSpectators = spectators;
    }
    
    private static boolean allowSpectators;
    
    @SuppressWarnings("CommentedOutCode")
    @Override
    public void onPlayInit(ServerPlayNetworkHandler handler, MinecraftServer server) {
        // FIXME make a storage to store friend or foe data
        if (Forf.isStarted && !((IEntityDataSaver) handler.getPlayer()).isForfPlayer()) {
            handler.disconnect(Text.of("Friend or Foe has already started on this server and spectators are not allowed"));
        }
        
//        if (!Forf.isStarted) {
//            handler.getPlayer().changeGameMode(GameMode.SURVIVAL);
//        } else if (allowSpectators /*FIXME add an Allow Spectators Config*/) {
//            handler.getPlayer().changeGameMode(GameMode.SPECTATOR);
//        } else if ((ForfManager.getLives(handler.getPlayer())) <= 0) {
//            handler.disconnect(Text.of("You have run out of lives!"));
//        } else {
//            if (ForfManager.getLives(handler.getPlayer()) <= 0)  handler.disconnect(Text.of("Friend or Foe has already started on this server and spectators are not allowed"));
//        }
    }
}
