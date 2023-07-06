package net.ilikefood971;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Forf implements ModInitializer {
	// TODO Implement PvP Timer
	// TODO Implement restrictions
	// TODO Player Tracker
	// TODO Queue Lobby
	public static final String MOD_ID = "forf";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("forf initialized");
	}
}