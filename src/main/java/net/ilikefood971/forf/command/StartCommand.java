package net.ilikefood971.forf.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.ilikefood971.forf.event.PlayerJoinEvent;
import net.ilikefood971.forf.util.ForfManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.ilikefood971.forf.Forf.getCONFIG;

@SuppressWarnings("SameReturnValue")
public class StartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("forf")
                .then(CommandManager.literal("start").requires((source) -> source.hasPermissionLevel(3)).executes(StartCommand::run)));
    }
    private static int run(CommandContext<ServerCommandSource> context) {
        
        if (getCONFIG().started()) {
            context.getSource().sendFeedback(() -> Text.literal("Friend or Foe has already started!"), false);
            return -1;
        }
        
        context.getSource().sendFeedback(() -> Text.literal("Setting up Friend or Foe with " + getCONFIG().startingLives() + " lives"), true);
        ForfManager.setupForf(context);
        getCONFIG().started(true);
        getCONFIG().save();
        
        for (ServerPlayerEntity serverPlayerEntity : context.getSource().getServer().getPlayerManager().getPlayerList()) {
            serverPlayerEntity.networkHandler.sendPacket(PlayerJoinEvent.getHeaderPacket());
        }
        
        return 1;
    }
}
