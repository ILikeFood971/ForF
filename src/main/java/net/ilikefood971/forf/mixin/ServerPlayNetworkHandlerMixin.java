package net.ilikefood971.forf.mixin;

import net.ilikefood971.forf.event.ServerTickEvent;
import net.ilikefood971.forf.util.mixinInterfaces.IPlayerTracker;
import net.ilikefood971.forf.util.mixinInterfaces.IScreenSender;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    /*
    @Shadow public ServerPlayerEntity player;
    
    @Inject(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;syncState()V", shift = At.Shift.BEFORE), cancellable = true)
    public void noClickSlotOfGui(ClickSlotC2SPacket packet, CallbackInfo ci) {
        this.player.getServer().getPlayerManager().getPlayerList().forEach(player -> {
            if (player.currentScreenHandler instanceof GenericContainerScreenHandler && player.currentScreenHandler.syncId == packet.getSyncId() && packet.getStack() != null) {
                
                this.player.currentScreenHandler.syncState();
                ServerTickEvent.openPlayerTrackers.forEach(playerTracker -> playerTracker.slotChanged(packet.getStack()));
                if (packet.)
                ci.cancel();
            }
        });
    }*/
}
