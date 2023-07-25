package net.ilikefood971.forf.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.ilikefood971.forf.util.ForfManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class JoinCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("forf")
                .then(CommandManager.literal("join").executes(JoinCommand::run)));
    }
    private static int run(CommandContext<ServerCommandSource> context) {
        if (!ForfManager.addForfPlayer(context.getSource().getPlayer())) {
            context.getSource().sendFeedback(() -> Text.literal(context.getSource().getName() + " is already added to Friend or Foe!"), false);
            
            return -1;
        }
        context.getSource().sendFeedback(() -> Text.literal("Added " + context.getSource().getName() + " to Friend or Foe"), true);
        return 1;
    }
}
