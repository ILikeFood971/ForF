package net.ilikefood971.forf.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.ilikefood971.forf.util.ForfManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

@SuppressWarnings("SameReturnValue")
public class StartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("forf")
                .then(CommandManager.literal("start").requires((source) -> source.hasPermissionLevel(3))
                        .then(CommandManager.argument("lives", IntegerArgumentType.integer(1)).executes(StartCommand::run))));
    }
    private static int run(CommandContext<ServerCommandSource> context) {
        int lives = IntegerArgumentType.getInteger(context, "lives");

        
        context.getSource().sendFeedback(() -> Text.literal("Setting up forf with " + lives + " lives"), true);
        ForfManager.setupForf(context, lives);
        return 1;
    }
}
