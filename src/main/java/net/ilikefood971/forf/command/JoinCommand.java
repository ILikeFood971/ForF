package net.ilikefood971.forf.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.ilikefood971.forf.Forf;
import net.ilikefood971.forf.config.Config;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Set;

public class JoinCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("forf")
                .then(CommandManager.literal("join").executes(JoinCommand::run)));
    }
    
    private static int run(CommandContext<ServerCommandSource> context) {
        String playerName = context.getSource().getName();
        String playerUuid = context.getSource().getPlayer().getUuidAsString();
        Config config = Forf.getCONFIG();
        
        if (config.forfPlayersUUIDs().contains(playerUuid)) {
            sendFeedback(context, "You are already added to Friend or Foe!");
            return JoinResult.ALREADY_ADDED.getValue();
        }
        if (config.started()) {
            sendFeedback(context, "Friend or Foe has already started!");
            return JoinResult.ALREADY_STARTED.getValue();
        }
        
        config.forfPlayersUUIDs().add(playerUuid);
        config.save();
        sendFeedback(context, "Added " + playerName + " to Friend or Foe");
        return JoinResult.SUCCESS.getValue();
    }
    
    private static void sendFeedback(CommandContext<ServerCommandSource> context, String message) {
        context.getSource().sendFeedback(() -> Text.literal(message), false);
    }
    private enum JoinResult {
        SUCCESS(1),
        ALREADY_ADDED(-1),
        ALREADY_STARTED(-1);
        
        private final int value;
        
        JoinResult(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
}
