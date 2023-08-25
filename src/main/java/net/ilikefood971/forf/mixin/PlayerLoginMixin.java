package net.ilikefood971.forf.mixin;

import com.mojang.authlib.GameProfile;

import net.ilikefood971.forf.Forf;
import net.ilikefood971.forf.config.Config;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerManager.class)
public abstract class PlayerLoginMixin {
    
    
    
    
    @Inject(method = "checkCanJoin", at = @At("HEAD"), cancellable = true)
    public void checkCanJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
        Config config = Forf.getCONFIG();
        // When a player joins, make sure that they are allowed to join from the config
        if (config.started() && !config.forfPlayersUUIDs().contains(profile.getId().toString()) && !config.spectators()) {
            cir.setReturnValue(Text.of("Friend or Foe has already started on this server and spectators are not allowed"));
        }
    }
}
