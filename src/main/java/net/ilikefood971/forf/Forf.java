package net.ilikefood971.forf;

import blue.endless.jankson.Jankson;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.ilikefood971.forf.config.Config;
import net.ilikefood971.forf.util.ModRegistries;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Forf implements ModInitializer {
	// DONE Implement restrictions
		// Totems (DONE, datapack)
		// Villager Trading (DONE, PlayerUseEntity)
		// Gapples (DONE, RecipeManagerMixin)
		// Elytras (DONE, EndCityGeneratorMixin)
	// DONE Lives Tracker
	// DONE PvP Timer
	// DONE Config (Config Uses owo lib)
	// DONE Tablist Header
	// TODO Player Tracker
	// TODO Queue Lobby
	// TODO Extra Lives Quest
		//	A system that prevents players from playing until all players are online
//Most of the rules that are listed in the description of the forf videos
	public static final String MOD_ID = "forf";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Config CONFIG = Config.createAndLoad();
	public static MinecraftServer SERVER;
	
	public static Config getCONFIG() {
		CONFIG.load();
		CONFIG.save();
		return CONFIG;
	}
	
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		
		ServerLifecycleEvents.SERVER_STARTED.register(instance -> SERVER = instance);
		ServerLifecycleEvents.SERVER_STOPPED.register(instance -> CONFIG.save());
		
		ModRegistries.registerModStuff();
		LOGGER.info(MOD_ID + " initialized");
		
	}
}