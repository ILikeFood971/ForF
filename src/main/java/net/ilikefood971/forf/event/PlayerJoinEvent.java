package net.ilikefood971.forf.event;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.ilikefood971.forf.Forf;
import net.ilikefood971.forf.util.PlayerTrackerGui;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;

public class PlayerJoinEvent implements ServerPlayConnectionEvents.Init, ServerPlayConnectionEvents.Join {
    
    @Override
    public void onPlayInit(ServerPlayNetworkHandler handler, MinecraftServer server) {
        // Because the PlayerLoginMixin has already run, we can be sure that either forf hasn't started or they are an allowed player
        // FIXME
        // Check to see if it has started already and make sure they aren't a forf player
        /*if (CONFIG.started() && !CONFIG.forfPlayersUUIDs().contains(handler.getPlayer().getUuidAsString())) {
            handler.getPlayer().changeGameMode(CONFIG.spectatorGamemode());
        }*/
    }
    
    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        // Check to see if Friend or Foe has started
        if (Forf.getCONFIG().started()) {
            sender.sendPacket(getHeaderPacket());
        }
    }
    
    public static Packet getHeaderPacket() {
        try {
            // Create a Text object with the config's header
            StringReader stringReader = new StringReader(Forf.getCONFIG().tablistHeader());
            Text parsed = TextArgumentType.text().parse(stringReader);
            return new PlayerListHeaderS2CPacket(parsed, Text.literal(""));
        } catch (CommandSyntaxException e) {
            Forf.LOGGER.error(e.toString());
            return null;
        }
    }
}
