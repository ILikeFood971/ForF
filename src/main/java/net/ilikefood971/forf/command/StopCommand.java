package net.ilikefood971.forf.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.ilikefood971.forf.Forf;
import net.ilikefood971.forf.util.ForfManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

@SuppressWarnings("SameReturnValue")
public class StopCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("forf")
                .then(CommandManager.literal("stop").requires((source) -> source.hasPermissionLevel(3)).executes(StopCommand::run)));
    }
    private static int run(CommandContext<ServerCommandSource> context) {
        Forf.getCONFIG().load();
        // Send a message that says stopping forf but only send to ops if forf has already started
        context.getSource().sendFeedback(() -> Text.literal("Stopping Friend or Foe"), Forf.getCONFIG().started());
        ForfManager.stopForf(context);
        Forf.getCONFIG().save();
        return 1;
    }
}
